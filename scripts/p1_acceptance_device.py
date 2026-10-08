#!/usr/bin/env python3
"""固定 serial 的 P1 驗收 runner；只改選取／啟用狀態，不清除 app 資料。"""

from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import math
import re
import subprocess
import sys
import time
import uuid
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Callable


REPO_ROOT = Path(__file__).resolve().parents[1]
APP = "io.github.juhjuhx.kuyin"
HOST = APP + ".acceptancehost"
IME = APP + "/com.example.ime.KuYinInputMethodService"
APP_RUNNER = APP + ".test/androidx.test.runner.AndroidJUnitRunner"
HOST_RUNNER = HOST + ".test/androidx.test.runner.AndroidJUnitRunner"
EXISTING_CLASSES = ",".join([
    "com.example.androidkeyboard.runtime.ImeRuntimeSmokeTest",
    "com.example.androidkeyboard.runtime.NativeCase4Test",
    "com.example.androidkeyboard.runtime.ContinuousSentenceTest",
    "com.example.ExampleInstrumentedTest",
])
EXPECTED_PACKAGES = {
    "app-debug.apk": APP,
    "app-debug-androidTest.apk": APP + ".test",
    "acceptance-host-debug.apk": HOST,
    "acceptance-host-debug-androidTest.apk": HOST + ".test",
}
FROZEN = {
    "app-debug.apk": (29158832, "b223620743831e0370a31a65dcc998e6a74dfe70d45e020036c4f8b4468e39c9"),
    "app-debug-androidTest.apk": (965496, "3938b3e3c1fb711ed6141d491a3841839e098dfb2c2ae81924b94f0d6106b8bd"),
}
CASE_IDS = tuple(
    [f"A{i:02}" for i in range(1, 5)]
    + [f"B{i:02}" for i in range(1, 4)]
    + [f"C{i:02}" for i in range(1, 6)]
    + [f"D{i:02}" for i in range(1, 19)]
)
RESULTS = {"PASS", "FAIL", "INCONCLUSIVE", "NOT_TESTED"}
STATE_NAME = "captured-state.json"
CASE_PREFIX = "P1_ACCEPTANCE_CASE="


class RunnerError(Exception):
    """必須停止、保留證據，不能自動移除 app 或改寫其他設定。"""


def utc_now() -> str:
    return dt.datetime.now(dt.timezone.utc).isoformat()


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def write_json_new(path: Path, value: Any) -> None:
    with path.open("x", encoding="utf-8") as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)
        stream.write("\n")


def verify_artifacts(package_dir: Path) -> dict[str, Any]:
    manifest_path = package_dir / "artifact-manifest.json"
    try:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        raise RunnerError(f"artifact manifest 無法讀取：{exc}") from exc
    if not isinstance(manifest, dict):
        raise RunnerError("artifact manifest root 必須是 object")
    entries = manifest.get("artifacts")
    if not isinstance(entries, list):
        raise RunnerError("artifact-manifest.json 必須有 artifacts list")
    apks = [entry for entry in entries if isinstance(entry, dict)
            and str(entry.get("file", "")).endswith(".apk")]
    if len(apks) != 4 or {item.get("file") for item in apks} != set(EXPECTED_PACKAGES):
        raise RunnerError("manifest 必須恰好列出指定四個 APK，不能遺漏／重複／改名")
    verified = []
    for entry in apks:
        name = entry["file"]
        path = package_dir / name
        if path.is_symlink() or not path.is_file():
            raise RunnerError(f"APK 必須是一般檔案：{path}")
        size, digest = path.stat().st_size, sha256(path)
        if type(entry.get("bytes")) is not int or entry["bytes"] != size:
            raise RunnerError(f"APK bytes 不符：{name}")
        if not isinstance(entry.get("sha256"), str) or entry["sha256"].lower() != digest:
            raise RunnerError(f"APK SHA256 不符：{name}")
        if entry.get("application_id") != EXPECTED_PACKAGES[name]:
            raise RunnerError(f"APK application_id 不符：{name}")
        if name in FROZEN and (size, digest) != FROZEN[name]:
            raise RunnerError(f"frozen 產品／既有 test APK 不是核定 bytes：{name}")
        verified.append({"file": name, "bytes": size, "sha256": digest,
                         "application_id": entry["application_id"]})
    return {"package_dir": str(package_dir), "manifest_sha256": sha256(manifest_path),
            "artifacts": sorted(verified, key=lambda item: item["file"])}


def enabled_entries(raw: str) -> dict[str, str]:
    value = raw.strip()
    if value in ("", "null"):
        return {}
    result = {}
    for entry in value.split(":"):
        component = entry.split(";", 1)[0]
        if not re.fullmatch(r"[A-Za-z0-9_.]+/[A-Za-z0-9_.]+", component) or component in result:
            raise RunnerError("enabled_input_methods 格式不明／重複；STOP，不猜測還原")
        result[component] = entry
    return result


def restore_commands(state: dict[str, Any], current_default: str,
                     current_enabled_raw: str) -> list[list[str]]:
    original = state["original_default_ime_raw"].strip()
    if not original or original == "null" or "/" not in original:
        raise RunnerError("原 default IME 是 null／格式不明；不能猜測還原")
    before = enabled_entries(state["original_enabled_imes_raw"])
    current = enabled_entries(current_enabled_raw)
    if original not in before or original not in current:
        raise RunnerError("原 default IME 已不在啟用列表；STOP，需要人工處理")
    if current_default.strip() not in {original, IME}:
        raise RunnerError("default IME 被其他操作改變；STOP，不覆寫外部變更")
    if {key: value for key, value in before.items() if key != IME} != {
            key: value for key, value in current.items() if key != IME}:
        raise RunnerError("其他 IME／subtype 狀態改變；STOP，不覆寫整串設定")
    if IME in before and current.get(IME) != before[IME]:
        raise RunnerError("原 KU-Yin enabled/subtypes 改變；STOP，需要人工核對")
    commands = []
    if current_default.strip() != original:
        commands.append(["shell", "ime", "set", original])
    if IME not in before and IME in current:
        commands.append(["shell", "ime", "disable", IME])
    return commands


@dataclass
class CommandResult:
    returncode: int
    stdout: str
    stderr: str = ""
    timed_out: bool = False


class Adb:
    def __init__(self, serial: str, log_path: Path, timeout: float = 30,
                 executor: Callable[..., Any] = subprocess.run):
        self.serial, self.log_path, self.timeout = serial, log_path, timeout
        self.executor = executor

    def run(self, args: list[str], *, timeout: float | None = None,
            check: bool = True) -> CommandResult:
        argv = ["adb", "-s", self.serial, *args]
        limit = self.timeout if timeout is None else timeout
        started = utc_now()
        try:
            process = self.executor(argv, capture_output=True, text=True,
                                    timeout=limit, check=False)
            result = CommandResult(process.returncode, process.stdout, process.stderr)
        except subprocess.TimeoutExpired as exc:
            def decoded(value: Any) -> str:
                return value.decode(errors="replace") if isinstance(value, bytes) else value or ""
            result = CommandResult(124, decoded(exc.stdout), decoded(exc.stderr), True)
        except OSError as exc:
            result = CommandResult(127, "", str(exc))
        record = {"started_at": started, "finished_at": utc_now(), "argv": argv,
                  "timeout_seconds": limit, **result.__dict__}
        with self.log_path.open("a", encoding="utf-8") as stream:
            stream.write(json.dumps(record, ensure_ascii=False) + "\n")
        if check and (result.returncode or result.timed_out):
            raise RunnerError(f"ADB 命令失敗／timeout：{argv!r}；{result.stderr or result.stdout}")
        return result


def read_identity(adb: Adb) -> dict[str, str]:
    if adb.run(["get-state"]).stdout.strip() != "device":
        raise RunnerError("指定 serial 不是已授權 device 狀態；STOP")
    fields = {
        "model": "ro.product.model", "manufacturer": "ro.product.manufacturer",
        "api": "ro.build.version.sdk", "abis": "ro.product.cpu.abilist",
        "primary_abi": "ro.product.cpu.abi", "fingerprint": "ro.build.fingerprint",
        "qemu": "ro.kernel.qemu",
    }
    identity = {key: adb.run(["shell", "getprop", prop]).stdout.strip()
                for key, prop in fields.items()}
    if not identity["api"].isdigit() or int(identity["api"]) < 24:
        raise RunnerError("API 必須 ≥24 且可明確讀取")
    if not identity["model"] or not identity["primary_abi"] or not identity["fingerprint"]:
        raise RunnerError("device model／ABI／fingerprint 缺失，無法固定裝置身分")
    return identity


def check_identity(adb: Adb, state: dict[str, Any]) -> None:
    if read_identity(adb) != state["device_identity"]:
        raise RunnerError("serial 相同但裝置 identity／API／ABI 已改變；STOP")


def read_ime_state(adb: Adb) -> tuple[str, str]:
    default = adb.run(["shell", "settings", "get", "secure", "default_input_method"]).stdout
    enabled = adb.run(["shell", "settings", "get", "secure", "enabled_input_methods"]).stdout
    return default, enabled


def wait_for(adb: Adb, predicate: Callable[[], bool], description: str,
             seconds: float = 10) -> None:
    deadline = time.monotonic() + seconds
    while True:
        if predicate():
            return
        if time.monotonic() >= deadline:
            raise RunnerError(f"等待 timeout：{description}")
        time.sleep(0.25)


def restore(adb: Adb, state: dict[str, Any]) -> dict[str, Any]:
    check_identity(adb, state)
    default, enabled = read_ime_state(adb)
    for command in restore_commands(state, default, enabled):
        adb.run(command)
    expected = (state["original_default_ime_raw"].strip(),
                state["original_enabled_imes_raw"].strip())
    def matches() -> bool:
        return tuple(value.strip() for value in read_ime_state(adb)) == expected
    wait_for(adb, matches, "原 default／enabled IME（含 subtype 原字串）還原")
    return {"result": "PASS", "restored_at": utc_now(),
            "default_ime": expected[0], "enabled_raw_matched": True}


def select(adb: Adb, state: dict[str, Any]) -> None:
    check_identity(adb, state)
    default, enabled = read_ime_state(adb)
    restore_commands(state, default, enabled)  # 在修改前排除其他人的狀態變更。
    def registered() -> bool:
        installed = adb.run(["shell", "ime", "list", "-s", "-a"]).stdout.splitlines()
        candidates = [line.strip() for line in installed if line.strip().startswith(APP + "/")]
        if candidates and candidates != [IME]:
            raise RunnerError(f"產品 canonical IME 不唯一／不是核定 service：{candidates!r}")
        return candidates == [IME]
    wait_for(adb, registered, "核定 canonical IME registered")
    adb.run(["shell", "ime", "enable", IME])
    wait_for(adb, lambda: IME in enabled_entries(read_ime_state(adb)[1]), "KU-Yin enabled")
    adb.run(["shell", "ime", "set", IME])
    wait_for(adb, lambda: read_ime_state(adb)[0].strip() == IME, "KU-Yin selected")


def parse_instrumentation(output: str, *, returncode: int = 0,
                          expected_tests: int | None = None) -> dict[str, Any]:
    """解析 AndroidJUnitRunner protocol；final -1 是正常成功，status -1 是 test error。"""
    codes = [int(value) for value in re.findall(r"^INSTRUMENTATION_CODE:\s*(-?\d+)\s*$", output, re.M)]
    blocks, fields, event_owners, active_test = [], {}, {}, None
    for index, line in enumerate(output.splitlines()):
        if CASE_PREFIX in line:
            event_owners[index] = active_test
        match = re.match(r"^INSTRUMENTATION_STATUS:\s*([^=]+)=(.*)$", line)
        if match:
            fields[match.group(1).strip()] = match.group(2)
        match = re.match(r"^INSTRUMENTATION_STATUS_CODE:\s*(-?\d+)\s*$", line)
        if match:
            block = {**fields, "status_code": int(match.group(1))}
            blocks.append(block)
            if block.get("class") and block.get("test"):
                if block["status_code"] == 1:
                    active_test = (block["class"], block["test"])
                elif block["status_code"] in (0, -1, -2, -3, -4):
                    active_test = None
            fields = {}
    reported_counts = [int(value) for value in re.findall(
        r"^INSTRUMENTATION_STATUS:\s*numtests=(\d+)\s*$", output, re.M)]
    banners = [int(value) for value in re.findall(r"\bOK \((\d+) tests?\)", output)]
    failures = bool(re.search(
        r"FAILURES!!!|INSTRUMENTATION_FAILED|INSTRUMENTATION_ABORTED|^Error:|"
        r"^INSTRUMENTATION_RESULT:\s*(?:shortMsg|Error)=|Process crashed",
        output, re.M))
    completed = [block for block in blocks if block.get("class") and block.get("test")
                 and block["status_code"] in (0, -1, -2, -3, -4)]
    terminal = [block["status_code"] for block in completed]
    skipped = sum(code in (-3, -4) for code in terminal)
    failed = sum(code in (-1, -2) for code in terminal)
    errors = []
    if returncode != 0:
        errors.append(f"adb_exit={returncode}")
    if codes != [-1]:
        errors.append(f"final_instrumentation_codes={codes!r}")
    if failures or failed:
        errors.append("instrumentation failure/error/crash")
    if any(block["status_code"] not in (0, 1, 2, -1, -2, -3, -4) for block in blocks):
        errors.append("未知 instrumentation status code")
    if not terminal or not reported_counts or not banners:
        errors.append("缺少 completed status／numtests／OK test summary")
    if len({(block["class"], block["test"]) for block in completed}) != len(completed):
        errors.append("重複的 JUnit terminal test identity")
    if terminal and set(reported_counts) != {len(terminal)}:
        errors.append("reported numtests 與實際 terminal test 數不符")
    if len(banners) != 1 or banners[0] != len(terminal) - terminal.count(-3):
        errors.append("OK summary 與實際 terminal／ignored 數不符")
    if expected_tests is not None:
        if len(terminal) != expected_tests or set(reported_counts) != {expected_tests} or banners != [expected_tests]:
            errors.append(f"必須實際完成 {expected_tests} tests")
        if skipped:
            errors.append("既有 5 tests 不得 skip／assumption")
    result = "FAIL" if errors else "INCONCLUSIVE" if skipped else "PASS"
    emissions, case_errors = [], []
    test_status = {(block["class"], block["test"]): block["status_code"] for block in completed}
    for index, line in enumerate(output.splitlines()):
        if CASE_PREFIX not in line:
            continue
        payload = line.split(CASE_PREFIX, 1)[1].strip()
        try:
            emission = json.loads(payload)
            if not isinstance(emission, dict) or emission.get("case_id") not in CASE_IDS:
                raise ValueError("未知／缺少 case_id")
            if emission.get("result") not in RESULTS:
                raise ValueError("result 不是四種合法模型")
            if not emission.get("scope") or not emission.get("evidence"):
                raise ValueError("缺少 scope／evidence")
            owner = event_owners.get(index)
            emission["junit_test"] = "#".join(owner) if owner else None
            emission["junit_terminal_status"] = test_status.get(owner)
            emissions.append(emission)
        except (ValueError, TypeError) as exc:
            case_errors.append(str(exc))
    if case_errors and result != "FAIL":
        result = "INCONCLUSIVE"
    return {"result": result, "completed_tests": len(terminal), "passed_tests": terminal.count(0),
            "skipped_tests": skipped, "failed_tests": failed, "reported_counts": reported_counts,
            "final_codes": codes, "errors": errors, "case_parse_errors": case_errors,
            "case_emissions": emissions}


def summarize_cases(emissions: list[dict[str, Any]]) -> dict[str, dict[str, Any]]:
    rank = {"PASS": 0, "NOT_TESTED": 1, "INCONCLUSIVE": 2, "FAIL": 3}
    def observed_result(event: dict[str, Any]) -> str:
        result = event["result"]
        if event.get("junit_terminal_status") in (-1, -2):
            return "FAIL"
        if result == "PASS" and "junit_terminal_status" in event and event["junit_terminal_status"] != 0:
            return "INCONCLUSIVE"
        return result
    summary = {}
    for case_id in CASE_IDS:
        events = [event for event in emissions if event["case_id"] == case_id]
        observed = max((observed_result(event) for event in events), key=rank.get, default="NOT_TESTED")
        limits = []
        if case_id == "B01":
            limits.append("ADDITIONAL_WIDTH_AND_ORIENTATION_OPEN：單一可見 layout 不證第二寬度／方向")
        if case_id == "B02":
            limits.append("NATIVE_CONTEXT_IDENTITY_OPEN：candidate UI 不證 live native context／reset count")
        if case_id == "B03":
            limits.append("NATIVE_CONTEXT_AND_SENTENCE_SEMANTICS_OPEN：UI 不證整句語義／排名與 context 身分")
        if case_id.startswith("C"):
            limits.append("NATIVE_LEARNING_OBSERVER_OPEN：mode／composition surface 不證明 native learning")
        if case_id == "C04":
            limits.append("TYPE_NULL 真 null IC 與可見／bound prerequisite 必須獨立觀察")
        if case_id == "D17":
            limits.append("HOST_FALSE_OBSERVABLE；IME_FALSE_IPC_PROPAGATION_INCONCLUSIVE（oneway Binder）")
        if case_id == "A03":
            limits.append("MANUAL_ACTUAL_ROTATION；未核定 composition preservation 不可新造契約")
        if case_id == "A04":
            limits.append("TEST_ONLY_STARTUP_FAULT_FIXTURE_MISSING_OPEN")
        full = "INCONCLUSIVE" if observed == "PASS" and limits else observed
        summary[case_id] = {"case_id": case_id, "observable_result": observed,
                            "full_acceptance_result": full, "scope_limits": limits,
                            "emissions": events}
    return summary


def load_state(args: argparse.Namespace) -> dict[str, Any]:
    try:
        state = json.loads((args.evidence_dir / STATE_NAME).read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        raise RunnerError(f"先 prepare；captured state 無法讀取：{exc}") from exc
    if not isinstance(state, dict) or state.get("schema") != 1 or state.get("serial") != args.serial or state.get("target_ime") != IME:
        raise RunnerError("captured state schema／serial／IME 不符；STOP")
    if not all(isinstance(state.get(key), str) for key in
               ("original_default_ime_raw", "original_enabled_imes_raw")) or not isinstance(state.get("device_identity"), dict):
        raise RunnerError("captured state IME raw／identity 格式不符；STOP")
    return state


def check_artifact_state(args: argparse.Namespace, state: dict[str, Any]) -> dict[str, Any]:
    verified = verify_artifacts(args.package_dir)
    if verified != state["artifact_verification"]:
        raise RunnerError("prepare 後 artifact path／manifest／四 APK 改變；STOP，使用新 evidence-dir")
    return verified


def execute(args: argparse.Namespace) -> int:
    evidence_dir = args.evidence_dir.resolve()
    if evidence_dir == REPO_ROOT or REPO_ROOT in evidence_dir.parents:
        raise RunnerError("evidence-dir 必須在工作 repo 外；不提交 user/device state")
    args.evidence_dir, args.package_dir = evidence_dir, args.package_dir.resolve()
    evidence_dir.mkdir(parents=True, exist_ok=True)
    operation_dir = evidence_dir / (args.command + "-" + dt.datetime.now().strftime("%Y%m%dT%H%M%S") + "-" + uuid.uuid4().hex[:8])
    operation_dir.mkdir()
    adb = Adb(args.serial, operation_dir / "commands.jsonl", args.adb_timeout)
    print(f"evidence={operation_dir}")
    print("native_learning_observer=OPEN; D17_IME_false_IPC_scope=INCONCLUSIVE")
    if args.command == "prepare":
        if (evidence_dir / STATE_NAME).exists():
            raise RunnerError("captured-state.json 已存在；不覆寫，請使用新 evidence-dir")
        verified = verify_artifacts(args.package_dir)
        identity = read_identity(adb)
        if (identity["qemu"] == "1") != (args.device_kind == "EMULATOR"):
            raise RunnerError("device-kind 與 ro.kernel.qemu 不符；STOP，核對裝置種類")
        default, enabled = read_ime_state(adb)
        entries = enabled_entries(enabled)
        if not default.strip() or default.strip() == "null" or default.strip() not in entries:
            raise RunnerError("原 default IME 是 null／未啟用；STOP，不能猜測還原")
        state = {"schema": 1, "captured_at": utc_now(), "serial": args.serial,
                 "device_kind": args.device_kind, "device_identity": identity, "target_ime": IME,
                 "original_default_ime_raw": default, "original_enabled_imes_raw": enabled,
                 "artifact_verification": verified, "sequence_status": "NOT_EXECUTED"}
        write_json_new(evidence_dir / STATE_NAME, state)
        write_json_new(operation_dir / "prepare-result.json", {"result": "PASS", "state_captured": True,
                                                               "device_sequence": "NOT_EXECUTED"})
        print("PREPARED：四 APK 驗證、原 IME raw 狀態保存；sequence=NOT_EXECUTED")
        return 0
    state = load_state(args)
    if args.command == "restore":
        outcome = restore(adb, state)
        write_json_new(operation_dir / "restore-result.json", outcome)
        print("RESTORED：原 default 與 enabled/subtypes raw 字串核對一致")
        return 0
    if args.command == "install":
        check_artifact_state(args, state)
        check_identity(adb, state)
        for name in EXPECTED_PACKAGES:
            outcome = adb.run(["install", "-r", "-t", str(args.package_dir / name)],
                              timeout=args.install_timeout, check=False)
            combined = outcome.stdout + outcome.stderr
            if outcome.returncode or outcome.timed_out or "Success" not in outcome.stdout or re.search(r"Failure|INSTALL_FAILED|INSTALL_PARSE_FAILED", combined):
                raise RunnerError(f"安裝 {name} 失敗（signature mismatch 也 STOP）；不 uninstall／clear：{combined}")
        write_json_new(operation_dir / "install-result.json", {"result": "PASS", "installed": list(EXPECTED_PACKAGES)})
        print("INSTALLED：-r -t 保留資料；尚未 select／run")
        return 0
    if args.command == "select":
        try:
            check_artifact_state(args, state)
            select(adb, state)
        except (RunnerError, KeyboardInterrupt) as primary:
            primary_message = str(primary) or "使用者中斷"
            try:
                restore_outcome = restore(adb, state)
            except (RunnerError, KeyboardInterrupt) as secondary:
                secondary_message = str(secondary) or "還原被中斷"
                write_json_new(operation_dir / "select-result.json", {
                    "result": "FAIL", "failure": primary_message,
                    "restore": {"result": "FAIL", "error": secondary_message,
                                "manual_action_required": True},
                })
                raise RunnerError(f"select 失敗：{primary_message}；restore 失敗：{secondary_message}") from primary
            write_json_new(operation_dir / "select-result.json", {
                "result": "FAIL", "failure": primary_message, "restore": restore_outcome,
            })
            raise
        write_json_new(operation_dir / "select-result.json", {"result": "PASS", "selected_ime": IME})
        print("SELECTED：測試 app 不負責修改系統 IME；手動操作後必須 restore")
        return 0
    suites, emissions, failure, restored = [], [], None, {"result": "NOT_TESTED"}
    try:
        check_artifact_state(args, state)
        check_identity(adb, state)
        default, enabled = read_ime_state(adb)
        restore_commands(state, default, enabled)
        if default.strip() != IME or IME not in enabled_entries(enabled):
            raise RunnerError("run prerequisite：先明示 select KU-Yin；test 不自行 enable／set")
        layers = [("existing-app-5-tests", APP_RUNNER, ["-e", "class", EXISTING_CLASSES], 5),
                  ("acceptance-host-suite", HOST_RUNNER, [], None)]
        for label, runner, extras, expected in layers:
            outcome = adb.run(["shell", "am", "instrument", "-w", "-r", *extras, runner],
                              timeout=args.instrumentation_timeout, check=False)
            output = outcome.stdout + "\n" + outcome.stderr
            (operation_dir / (label + ".txt")).write_text(output, encoding="utf-8")
            parsed = parse_instrumentation(output, returncode=outcome.returncode, expected_tests=expected)
            suites.append({"layer": label, **parsed})
            emissions.extend(parsed["case_emissions"])
            print(f"{label}: {parsed['result']}; completed={parsed['completed_tests']}; skipped={parsed['skipped_tests']}")
            if parsed["result"] == "FAIL":
                raise RunnerError(f"{label} protocol／tests 失敗；不繼續後續 suite")
    except (RunnerError, KeyboardInterrupt) as exc:
        failure = str(exc) or "使用者中斷"
    finally:
        try:
            restored = restore(adb, state)
        except (RunnerError, KeyboardInterrupt) as exc:
            restored = {"result": "FAIL", "error": str(exc) or "還原被中斷",
                        "manual_action_required": True}
    cases = summarize_cases(emissions)
    for case_id, record in cases.items():
        write_json_new(operation_dir / (case_id + ".json"), record)
    full_results = [record["full_acceptance_result"] for record in cases.values()]
    result = "FAIL" if failure or restored["result"] != "PASS" or "FAIL" in full_results else (
        "PASS" if full_results == ["PASS"] * len(CASE_IDS) and all(suite["result"] == "PASS" for suite in suites)
        else "INCONCLUSIVE")
    summary = {"schema": 1, "serial": args.serial, "device_kind": state["device_kind"],
               "device_identity": state["device_identity"], "sequence_status": "EXECUTED" if suites else "NOT_EXECUTED",
               "sequence_result": result, "full_acceptance_complete": result == "PASS", "failure": failure,
               "native_learning_observer": "OPEN", "D17_IME_false_IPC_scope": "INCONCLUSIVE",
               "suites": suites, "cases": cases, "restore": restored,
               "artifact_verification": state["artifact_verification"], "finished_at": utc_now()}
    write_json_new(operation_dir / "run-summary.json", summary)
    print(f"sequence={summary['sequence_status']}; result={result}; full_acceptance_complete={summary['full_acceptance_complete']}")
    print(f"restore={restored['result']}; summary={operation_dir / 'run-summary.json'}")
    return 2 if result == "FAIL" else 3 if result == "INCONCLUSIVE" else 0


def parser() -> argparse.ArgumentParser:
    result = argparse.ArgumentParser(description=__doc__)
    result.add_argument("--serial", required=True, help="明示且唯一的 ADB serial；不採第一台裝置")
    result.add_argument("--evidence-dir", type=Path, required=True, help="工作 repo 外的證據目錄；captured state 不覆寫")
    result.add_argument("--package-dir", type=Path, default=REPO_ROOT / "build/p1-alpha-acceptance-v2-fixture")
    result.add_argument("--adb-timeout", type=float, default=30)
    result.add_argument("--install-timeout", type=float, default=120)
    result.add_argument("--instrumentation-timeout", type=float, default=600)
    subcommands = result.add_subparsers(dest="command", required=True)
    prepare = subcommands.add_parser("prepare", help="先驗四 APK 並只讀封存原裝置／IME 狀態")
    prepare.add_argument("--device-kind", required=True, choices=["PHYSICAL", "EMULATOR"])
    subcommands.add_parser("install", help="安裝四 APK（-r -t）；signature mismatch STOP")
    subcommands.add_parser("select", help="顯式 enable／select KU-Yin，失敗時嘗試安全還原")
    subcommands.add_parser("run", help="既有 5 tests → host suite；finally 還原、逐 case 留證據")
    subcommands.add_parser("restore", help="只還原 captured default／新增 KU-Yin enable 狀態並核對 raw")
    return result


def main(argv: list[str] | None = None) -> int:
    args = parser().parse_args(argv)
    if not args.serial.strip() or args.serial != args.serial.strip():
        parser().error("serial 不可空白／含前後空白")
    if not all(math.isfinite(limit) and limit > 0 for limit in
               (args.adb_timeout, args.install_timeout, args.instrumentation_timeout)):
        parser().error("所有 timeout 必須是有限且 >0 的秒數")
    try:
        return execute(args)
    except (RunnerError, OSError, ValueError, KeyError) as exc:
        print(f"STOP：{exc}", file=sys.stderr)
        print("保留 evidence；不自動 uninstall／pm clear／覆寫其他 IME 設定。", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
