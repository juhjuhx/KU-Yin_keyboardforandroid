#!/usr/bin/env python3
"""固定 serial 的 P1 驗收 runner；只改選取／啟用狀態，不清除 app 資料。"""

from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import math
import os
import re
import subprocess
import sys
import tempfile
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
    "acceptance-host-debug.apk": (861628, "8e53e8933e1b436b3192dab4319a7da1fe58e98afba038447924f51e1481d829"),
    "acceptance-host-debug-androidTest.apk": (1028719, "5db907bd2e131742ce817139785d2f4a9a5cb7254ccd13839bfcbe6eb6277bd2"),
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
EXPECTED_APP_TESTS = 5
EXPECTED_HOST_TESTS = 26
PROTOCOL_MANIFEST = Path(__file__).with_name("p1_instrumentation_manifest.json")
# Versioned source contract for the frozen 6595b684 fixture; updates require review.
CANONICAL_MANIFEST_SHA256 = "ccb2d32d62dd1aea5d84db2b7d53853c108b0f4dff99727dbe9d6c916fc9e8de"


class RunnerError(Exception):
    """必須停止、保留證據，不能自動移除 app 或改寫其他設定。"""


def diagnostic(message: str) -> None:
    """Console I/O is advisory; it must not interrupt restoration/evidence."""
    try:
        print(message, file=sys.stderr)
    except (OSError, ValueError):
        pass


def progress(message: str) -> None:
    try:
        print(message)
    except (OSError, ValueError):
        pass


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
        stream.flush()
        os.fsync(stream.fileno())


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
            raise RunnerError(f"frozen 四個 APK 不是核定 bytes：{name}")
        verified.append({"file": name, "bytes": size, "sha256": digest,
                         "application_id": entry["application_id"]})
    instrumentation_manifest()
    return {"package_dir": str(package_dir), "manifest_sha256": sha256(manifest_path),
            "instrumentation_manifest_sha256": sha256(PROTOCOL_MANIFEST),
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
        commands.append(["shell", "ime", "set", "--user", str(state["android_user_id"]), original])
    if IME not in before and IME in current:
        commands.append(["shell", "ime", "disable", "--user", str(state["android_user_id"]), IME])
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
        self.android_user_id: int | None = None
        self.android_api: int | None = None
        self.recovery_mode = False
        self.log_errors: list[str] = []
        self.recovery_records: list[dict[str, Any]] = []

    def log_record(self, record: dict[str, Any]) -> None:
        try:
            with self.log_path.open("a", encoding="utf-8") as stream:
                stream.write(json.dumps(record, ensure_ascii=False) + "\n")
                stream.flush()
                os.fsync(stream.fileno())
        except OSError as exc:
            self.log_errors.append(str(exc))
            self.recovery_records.append(record)
            if not self.recovery_mode:
                raise
            diagnostic(f"RECOVERY_LOG_ERROR={exc}")

    def run(self, args: list[str], *, timeout: float | None = None,
            check: bool = True) -> CommandResult:
        mutation = args[0] == "install" or args[:3] in (
            ["shell", "ime", "enable"], ["shell", "ime", "disable"], ["shell", "ime", "set"],
            ["shell", "am", "instrument"],
        )
        if mutation:
            check_current_user(self)
            if "--user" not in args or args[args.index("--user") + 1] != str(self.android_user_id):
                raise RunnerError("ANDROID_USER_SCOPE_REQUIRED：mutation 必須明示 captured numeric user")
        argv = ["adb", "-s", self.serial, *args]
        limit = self.timeout if timeout is None else timeout
        started = utc_now()
        # Durable write-ahead evidence: normal execution cannot outrun a broken sink.
        self.log_record({"phase": "intent", "started_at": started, "argv": argv, "timeout_seconds": limit})
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
        self.log_record({"phase": "completed", **record})
        if check and (result.returncode or result.timed_out):
            raise RunnerError(f"ADB 命令失敗／timeout：{argv!r}；{result.stderr or result.stdout}")
        if mutation:
            check_current_user(self)
        return result


def read_current_user(adb: Adb) -> int:
    raw = adb.run(["shell", "am", "get-current-user"]).stdout.strip()
    if not re.fullmatch(r"0|[1-9][0-9]*", raw) or int(raw) > 2147483647:
        raise RunnerError("ANDROID_USER_UNREADABLE：current user 必須是明確非負整數")
    return int(raw)


def check_current_user(adb: Adb) -> None:
    if type(adb.android_user_id) is not int or read_current_user(adb) != adb.android_user_id:
        raise RunnerError("ANDROID_USER_CHANGED：停止 mutation；不得還原別人的 user")


def user_capabilities(adb: Adb) -> dict[str, Any]:
    """Exact command synopsis, not a global occurrence of --user elsewhere."""
    helps = {name: adb.run(["shell", name, "help"]).stdout for name in ("settings", "am", "ime", "pm")}
    def supported(name: str, command: str) -> bool:
        lines = helps[name].splitlines()
        for index, line in enumerate(lines):
            if re.match(r"\s*" + re.escape(command) + r"(?=\s|\[|$)", line):
                continuation = [line]
                for following in lines[index + 1:index + 4]:
                    if not following.lstrip().startswith("["):
                        break
                    continuation.append(following)
                if re.search(r"(?<![\w-])--user\s+(?:<USER_ID>|USER_ID)(?=[\s|\]])", " ".join(continuation)):
                    return True
        return False
    flags = {"settings": supported("settings", "get"), "instrument": supported("am", "instrument"),
             "install": supported("pm", "install"),
             **{command: supported("ime", command) for command in ("enable", "disable", "set")}}
    if not all(flags.values()):
        raise RunnerError(f"BLOCKED_USER_SCOPING_UNSUPPORTED：{flags!r}；不採 implicit user fallback")
    return {"flags": flags, "help_sha256": {name: hashlib.sha256(raw.encode()).hexdigest() for name, raw in helps.items()}}


def read_identity(adb: Adb) -> dict[str, Any]:
    user = read_current_user(adb)
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
    if read_current_user(adb) != user:
        raise RunnerError("ANDROID_USER_CHANGED：capture 過程中 user 改變")
    identity["android_user_id"] = user
    return identity


def check_identity(adb: Adb, state: dict[str, Any]) -> None:
    if read_identity(adb) != state["device_identity"]:
        raise RunnerError("serial 相同但裝置 identity／API／ABI 已改變；STOP")
    check_current_user(adb)
    user_capabilities(adb)
    check_subtype_boundary(adb, state)


def read_ime_state(adb: Adb) -> tuple[str, str]:
    check_current_user(adb)
    default = adb.run(["shell", "settings", "--user", str(adb.android_user_id), "get", "secure", "default_input_method"]).stdout
    enabled = adb.run(["shell", "settings", "--user", str(adb.android_user_id), "get", "secure", "enabled_input_methods"]).stdout
    check_current_user(adb)
    return default, enabled


def read_selected_subtype(adb: Adb) -> str:
    check_current_user(adb)
    raw = adb.run(["shell", "settings", "--user", str(adb.android_user_id),
                   "get", "secure", "selected_input_method_subtype"]).stdout
    value = raw.strip()
    if not re.fullmatch(r"-?(0|[1-9][0-9]*)", value) or not -2147483648 <= int(value) <= 2147483647:
        raise RunnerError("BLOCKED_SUBTYPE_RESTORE_UNSUPPORTED：selected subtype 不可明確讀取")
    check_current_user(adb)
    return raw


def subtype_restore_plan(adb: Adb, original: str, selected_raw: str) -> dict[str, Any]:
    """No generic shell subtype setter exists: only the provable no-subtype case."""
    check_current_user(adb)
    if adb.android_api not in (35, 36):
        return {"ready": False, "method": None, "reason": "未核對的 Android API subtype profile"}
    listing = adb.run(["shell", "ime", "list", "--user", str(adb.android_user_id), "-a"]).stdout
    check_current_user(adb)
    lines = listing.splitlines()
    starts = [i for i, line in enumerate(lines) if line == original + ":"]
    counts, ids = [], []
    if len(starts) == 1:
        block = []
        for line in lines[starts[0] + 1:]:
            if re.fullmatch(r"\S+/\S+:", line):
                break
            block.append(line)
        text = "\n".join(block)
        counts = re.findall(r"^\s*InputMethodSubtype array: count=(.*)$", text, re.M)
        ids = re.findall(r"^\s*mId=([^\s]*)", text, re.M)
    ready = selected_raw.strip() == "-1" and counts == ["0"] and ids == [original]
    return {"ready": ready, "method": "SENTINEL_WITH_ZERO_SUBTYPES" if ready else None,
            "reason": None if ready else "原 subtype 非-1／非零／缺失／重複／OEM格式不明",
            "original_ime": original, "selected_raw": selected_raw, "counts": counts,
            "ime_listing_sha256": hashlib.sha256(listing.encode()).hexdigest()}


def check_subtype_boundary(adb: Adb, state: dict[str, Any]) -> None:
    plan = subtype_restore_plan(adb, state["original_default_ime_raw"].strip(),
                                state["original_selected_input_method_subtype_raw"])
    if not plan["ready"]:
        raise RunnerError(f"BLOCKED_SUBTYPE_RESTORE_UNSUPPORTED：{plan['reason']}")


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
    check_subtype_boundary(adb, state)
    default, enabled = read_ime_state(adb)
    selected = read_selected_subtype(adb)
    if default.strip() == state["original_default_ime_raw"].strip() and selected.strip() != state["original_selected_input_method_subtype_raw"].strip():
        raise RunnerError("RESTORE_SUBTYPE_MISMATCH：不以相同 default 假稱 subtype 還原")
    for command in restore_commands(state, default, enabled):
        adb.run(command)
    expected = (state["original_default_ime_raw"].strip(),
                state["original_enabled_imes_raw"].strip(),
                state["original_selected_input_method_subtype_raw"].strip())
    def matches() -> bool:
        return tuple(value.strip() for value in (*read_ime_state(adb), read_selected_subtype(adb))) == expected
    wait_for(adb, matches, "原 default／enabled／selected subtype 三欄還原")
    check_subtype_boundary(adb, state)
    check_identity(adb, state)
    return {"result": "PASS", "restored_at": utc_now(),
            "default_ime": expected[0], "enabled_raw_matched": True, "selected_subtype_matched": True,
            "selected_subtype_raw": state["original_selected_input_method_subtype_raw"],
            "android_user_id": adb.android_user_id}


def select(adb: Adb, state: dict[str, Any]) -> None:
    check_identity(adb, state)
    check_subtype_boundary(adb, state)
    default, enabled = read_ime_state(adb)
    selected = read_selected_subtype(adb)
    if default.strip() == state["original_default_ime_raw"].strip() and selected.strip() != state["original_selected_input_method_subtype_raw"].strip():
        raise RunnerError("原 selected subtype 已由外部變更；STOP")
    restore_commands(state, default, enabled)  # 在修改前排除其他人的狀態變更。
    def registered() -> bool:
        check_current_user(adb)
        installed = adb.run(["shell", "ime", "list", "--user", str(adb.android_user_id), "-s", "-a"]).stdout.splitlines()
        check_current_user(adb)
        candidates = [line.strip() for line in installed if line.strip().startswith(APP + "/")]
        if candidates and candidates != [IME]:
            raise RunnerError(f"產品 canonical IME 不唯一／不是核定 service：{candidates!r}")
        return candidates == [IME]
    wait_for(adb, registered, "核定 canonical IME registered")
    adb.run(["shell", "ime", "enable", "--user", str(adb.android_user_id), IME])
    wait_for(adb, lambda: IME in enabled_entries(read_ime_state(adb)[1]), "KU-Yin enabled")
    adb.run(["shell", "ime", "set", "--user", str(adb.android_user_id), IME])
    wait_for(adb, lambda: read_ime_state(adb)[0].strip() == IME, "KU-Yin selected")


def instrumentation_manifest() -> dict[str, Any]:
    try:
        raw = PROTOCOL_MANIFEST.read_bytes()
        if hashlib.sha256(raw).hexdigest() != CANONICAL_MANIFEST_SHA256:
            raise ValueError("canonical frozen source contract digest mismatch")
        value = json.loads(raw)
        if value["schema"] != 1:
            raise ValueError("schema")
        for layer, count in (("app", EXPECTED_APP_TESTS), ("host", EXPECTED_HOST_TESTS)):
            entries = value[layer]["tests"]
            identities = [item["identity"] for item in entries]
            if value[layer]["allow_skips"] is not (layer == "host"):
                raise ValueError("skip policy")
            if value[layer]["expected_count"] != count or len(entries) != count or len(set(identities)) != count:
                raise ValueError("count／duplicate identity")
            if any(not re.fullmatch(r"[A-Za-z0-9_.]+#[A-Za-z0-9_]+", name) for name in identities):
                raise ValueError("test identity")
            for item in entries:
                cases = item["cases"]
                if not isinstance(cases, list) or (layer == "host" and not cases) or (layer == "app" and cases) or any(case.get("case_id") not in CASE_IDS for case in cases):
                    raise ValueError("case contract")
        return value
    except (OSError, ValueError, TypeError, KeyError) as exc:
        raise RunnerError(f"instrumentation manifest 不可用：{exc}") from exc


def parse_instrumentation(output: str, *, returncode: int = 0,
                          expected_tests: int | None = None,
                          expected_identities: list[str] | None = None,
                          allow_skips: bool = False,
                          case_contract: dict[str, list[dict[str, str]]] | None = None) -> dict[str, Any]:
    """JUnit lifecycle + source-owned sendStatus(2) observations; final -1 succeeds."""
    errors, case_errors, emissions = [], [], []
    fields, completed, reported_counts, codes = {}, [], [], []
    active, started, ended, observed = None, set(), set(), {}
    terminal_seen = False
    result_fields = {}

    def unique_object(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
        value = {}
        for key, item in pairs:
            if key in value:
                raise ValueError("duplicate JSON key: " + key)
            value[key] = item
        return value

    def case_key(value: dict[str, Any]) -> tuple[str, Any]:
        return value['case_id'], value.get('subcase_id')

    def record_case(payload: str) -> None:
        try:
            emission = json.loads(payload, object_pairs_hook=unique_object)
            if not isinstance(emission, dict) or emission.get('case_id') not in CASE_IDS:
                raise ValueError('未知／缺少 case_id')
            if emission.get('result') not in RESULTS or not emission.get('scope') or not emission.get('evidence'):
                raise ValueError('缺少合法 result／scope／evidence')
            if 'subcase_id' in emission and not isinstance(emission['subcase_id'], str):
                raise ValueError('subcase_id 必須是字串')
            emission['junit_test'] = active
            bundle_has_identity = 'class' in fields or 'test' in fields
            bundle_matches = not bundle_has_identity or (bool(fields.get('class') and fields.get('test')) and fields['class'] + '#' + fields['test'] == active)
            valid = active is not None and not terminal_seen and bundle_matches
            if not valid:
                errors.append('case emitted outside active test')
            if case_contract is not None:
                allowed = {case_key(item) for item in case_contract.get(active, [])}
                if case_key(emission) not in allowed:
                    valid = False
                    errors.append(f'case owner／subcase contract mismatch: {active!r} {case_key(emission)!r}')
            key = (active, *case_key(emission))
            if key in observed:
                valid = False
                observed[key]['protocol_invalid'] = True
                errors.append(f'duplicate case/subcase emission: {key!r}')
            else:
                observed[key] = emission
            emission['protocol_invalid'] = not valid
            emissions.append(emission)
            if emission['result'] == 'FAIL':
                errors.append('case reported FAIL')
        except (ValueError, TypeError, KeyError) as exc:
            case_errors.append(str(exc))

    for line in output.splitlines():
        if terminal_seen and (line.startswith('INSTRUMENTATION_') or CASE_PREFIX in line):
            errors.append('protocol event after terminal result')
        if CASE_PREFIX in line and not line.startswith('INSTRUMENTATION_STATUS: stream=' + CASE_PREFIX):
            errors.append('case emitted outside sendStatus(2) stream bundle')
        wire_patterns = (
            r'INSTRUMENTATION_STATUS:\s*([^=]+)=(.*)',
            r'INSTRUMENTATION_STATUS_CODE:\s*(-?\d+)\s*',
            r'INSTRUMENTATION_RESULT:\s*([^=]+)=(.*)',
            r'INSTRUMENTATION_CODE:\s*(-?\d+)\s*',
        )
        if line.startswith('INSTRUMENTATION_') and not any(re.fullmatch(pattern, line) for pattern in wire_patterns):
            errors.append('malformed protocol control line: ' + line)
        match = re.fullmatch(r'INSTRUMENTATION_STATUS:\s*([^=]+)=(.*)', line)
        if match:
            name, value = match.group(1).strip(), match.group(2)
            if not name:
                errors.append('empty instrumentation bundle key')
            if name in fields:
                errors.append('duplicate instrumentation bundle key: ' + name)
            fields[name] = value
            if name == 'numtests':
                if not re.fullmatch(r'[0-9]+', value):
                    errors.append('invalid numtests')
                else:
                    reported_counts.append(int(value))
        match = re.fullmatch(r'INSTRUMENTATION_RESULT:\s*([^=]+)=(.*)', line)
        if match:
            name, value = match.group(1).strip(), match.group(2)
            if not name or name in result_fields:
                errors.append('empty／duplicate instrumentation result key')
            result_fields[name] = value
        match = re.fullmatch(r'INSTRUMENTATION_STATUS_CODE:\s*(-?\d+)\s*', line)
        if match:
            code = int(match.group(1))
            stream = fields.get('stream', '')
            if CASE_PREFIX in stream:
                if code != 2 or not stream.startswith(CASE_PREFIX):
                    errors.append('case observation requires status code 2 stream bundle')
                else:
                    record_case(stream[len(CASE_PREFIX):].strip())
            identity = fields.get('class', '') + '#' + fields.get('test', '')
            identified = bool(fields.get('class') and fields.get('test'))
            if code == 1:
                if not identified or active is not None or identity in started:
                    errors.append('invalid／duplicate start or previous test unfinished')
                if identified:
                    started.add(identity)
                    active = identity
            elif code in (0, -1, -2, -3, -4):
                if not identified or active != identity or identity in ended:
                    errors.append('finish without matching unique start')
                if identified:
                    completed.append({'identity': identity, 'status_code': code})
                    ended.add(identity)
                if active == identity:
                    active = None
            elif code == 2:
                if identified and active != identity:
                    errors.append('status observation identity mismatch')
            else:
                errors.append('未知 instrumentation status code')
            fields = {}
        match = re.fullmatch(r'INSTRUMENTATION_CODE:\s*(-?\d+)\s*', line)
        if match:
            codes.append(int(match.group(1)))
            terminal_seen = True
    if active is not None or started != ended or fields:
        errors.append('premature EOF／unfinished JUnit lifecycle')
    terminal = [item['status_code'] for item in completed]
    skipped = sum(code in (-3, -4) for code in terminal)
    failed = sum(code in (-1, -2) for code in terminal)
    banners = [int(value) for value in re.findall(
        r'^(?:INSTRUMENTATION_RESULT:\s*stream=)?\s*OK \((\d+) tests?\)\s*$', output, re.M)]
    if returncode:
        errors.append(f'adb_exit={returncode}')
    if codes != [-1]:
        errors.append(f'final_instrumentation_codes={codes!r}')
    if failed or re.search(r'FAILURES!!!|INSTRUMENTATION_FAILED|INSTRUMENTATION_ABORTED|^Error:|'
                           r'^INSTRUMENTATION_RESULT:\s*(?:shortMsg|Error)=|Process crashed', output, re.M):
        errors.append('instrumentation failure/error/crash')
    if not terminal or not reported_counts or set(reported_counts) != {len(terminal)}:
        errors.append('reported numtests 與 completed tests 不符／缺失')
    if len(ended) != len(completed):
        errors.append('重複 JUnit terminal identity')
    if banners != [len(terminal) - terminal.count(-3)]:
        errors.append('OK summary 與 terminal／ignored count 不符')
    if expected_tests is not None:
        if len(terminal) != expected_tests or set(reported_counts) != {expected_tests}:
            errors.append(f'必須實際完成 {expected_tests} tests')
        if skipped and not allow_skips:
            errors.append('此 layer 不得 skip／assumption')
    if expected_identities is not None:
        expected = set(expected_identities)
        if ended != expected or len(completed) != len(expected_identities):
            errors.append(f'JUnit identities 不符：missing={sorted(expected - ended)!r}; unexpected={sorted(ended - expected)!r}')
    statuses = {item['identity']: item['status_code'] for item in completed}
    for emission in emissions:
        emission['junit_terminal_status'] = statuses.get(emission['junit_test'])
    missing = []
    if case_contract is not None:
        for owner, requirements in case_contract.items():
            for requirement in requirements:
                observation = observed.get((owner, *case_key(requirement)))
                if observation is None or observation.get('protocol_invalid'):
                    missing.append({'junit_test': owner, **requirement})
    if case_errors:
        errors.extend('case parse: '+message for message in case_errors)
    incomplete = bool(skipped or missing or any(item['result'] in ('INCONCLUSIVE', 'NOT_TESTED') for item in emissions))
    result = 'FAIL' if errors else 'INCONCLUSIVE' if incomplete else 'PASS'
    return {'result': result, 'completed_tests': len(terminal), 'passed_tests': terminal.count(0),
            'skipped_tests': skipped, 'failed_tests': failed, 'reported_counts': reported_counts,
            'final_codes': codes, 'errors': errors, 'case_parse_errors': case_errors,
            'missing_required_cases': missing, 'case_emissions': emissions}


def summarize_cases(emissions: list[dict[str, Any]]) -> dict[str, dict[str, Any]]:
    rank = {"PASS": 0, "NOT_TESTED": 1, "INCONCLUSIVE": 2, "FAIL": 3}
    def observed_result(event: dict[str, Any]) -> str:
        result = event["result"]
        if event.get("protocol_invalid"):
            return "FAIL"
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
    if not isinstance(state, dict) or state.get("schema") != 2 or state.get("serial") != args.serial or state.get("target_ime") != IME:
        raise RunnerError("captured state schema／serial／IME 不符；STOP")
    if not all(isinstance(state.get(key), str) for key in
               ("original_default_ime_raw", "original_enabled_imes_raw", "original_selected_input_method_subtype_raw")) or not isinstance(state.get("device_identity"), dict):
        raise RunnerError("captured state IME raw／identity 格式不符；STOP")
    if state.get("preflight_status") != "READY" or state.get("original_selected_input_method_subtype_raw", "").strip() != "-1":
        raise RunnerError("BLOCKED_SUBTYPE_RESTORE_UNSUPPORTED：此 capture 未通過安全預檢")
    return state


def check_artifact_state(args: argparse.Namespace, state: dict[str, Any]) -> dict[str, Any]:
    verified = verify_artifacts(args.package_dir)
    if verified != state["artifact_verification"]:
        raise RunnerError("prepare 後 artifact path／manifest／四 APK 改變；STOP，使用新 evidence-dir")
    return verified


def recover_state(adb: Adb, state: dict[str, Any]) -> dict[str, Any]:
    """Bounded restoration may survive a broken log; identity checks never relax."""
    previous = adb.recovery_mode
    adb.recovery_mode = True
    try:
        outcome = restore(adb, state)
        if adb.log_errors:
            outcome = {**outcome, "result": "FAIL", "state_restored": True,
                       "error": "COMMAND_EVIDENCE_IO_FAILURE", "logging_errors": list(adb.log_errors)}
    except (Exception, KeyboardInterrupt) as exc:
        outcome = {"result": "FAIL", "error": str(exc) or "還原被中斷",
                   "exception_type": type(exc).__name__, "manual_action_required": True}
    finally:
        adb.recovery_mode = previous
    if adb.recovery_records:
        outcome["recovery_command_records"] = list(adb.recovery_records)
    return outcome


def persist_outcome(path: Path, outcome: dict[str, Any]) -> None:
    """Never call a non-durable result clean PASS. Private fallback is evidence only."""
    try:
        write_json_new(path, outcome)
    except OSError as exc:
        failed = {**outcome, "result": "FAIL", "evidence_write_error": str(exc),
                  "intended_path": str(path)}
        diagnostic("EVIDENCE_WRITE_ERROR=" + str(exc))
        try:
            fd, name = tempfile.mkstemp(prefix="KUYin-p1g-recovery-", suffix=".json")
            with os.fdopen(fd, "w", encoding="utf-8") as stream:
                json.dump(failed, stream, ensure_ascii=False, indent=2)
                stream.flush()
                os.fsync(stream.fileno())
            diagnostic("FALLBACK_EVIDENCE=" + name)
        except OSError as secondary:
            diagnostic(f"FALLBACK_EVIDENCE_FAILED={secondary}; OUTCOME={json.dumps(failed, ensure_ascii=False)}")
        raise RunnerError(f"EVIDENCE_WRITE_FAILED：{exc}；沒有完整 durable proof，不可 PASS") from exc


def bind_state(adb: Adb, state: dict[str, Any]) -> None:
    if type(state.get("android_user_id")) is not int or state["android_user_id"] < 0:
        raise RunnerError("captured state 缺 numeric android_user_id；舊 session 不可用新 runner mutation")
    adb.android_user_id = state["android_user_id"]
    adb.android_api = int(state["device_identity"].get("api", 0))


def execute(args: argparse.Namespace) -> int:
    evidence_dir = args.evidence_dir.resolve()
    if evidence_dir == REPO_ROOT or REPO_ROOT in evidence_dir.parents:
        raise RunnerError("evidence-dir 必須在工作 repo 外；不提交 user/device state")
    args.evidence_dir, args.package_dir = evidence_dir, args.package_dir.resolve()
    operation_dir = evidence_dir / (args.command + "-" + dt.datetime.now().strftime("%Y%m%dT%H%M%S") + "-" + uuid.uuid4().hex[:8])
    adb = Adb(args.serial, operation_dir / "commands.jsonl", args.adb_timeout)
    try:
        evidence_dir.mkdir(parents=True, exist_ok=True)
        operation_dir.mkdir()
    except OSError as primary:
        if args.command == "prepare":
            raise
        # Previous select may have changed state; an unusable evidence sink cannot
        # prevent explicit or finally restoration. Missing logs remain a FAIL.
        try:
            state = load_state(args)
            bind_state(adb, state)
            restored = recover_state(adb, state)
        except (Exception, KeyboardInterrupt) as secondary:
            restored = {"result": "FAIL", "error": str(secondary) or "還原中斷", "manual_action_required": True}
        report = {"result": "FAIL", "failure": str(primary), "exception_type": type(primary).__name__, "restore": restored}
        diagnostic(f"PRIMARY_ERROR={primary}; RESTORE_ERROR={restored.get('error')}; RESTORE_RESULT={restored['result']}")
        persist_outcome(operation_dir / "io-preflight-failure.json", report)
        raise RunnerError(f"operation evidence 失敗：{primary}") from primary
    progress(f"evidence={operation_dir}")
    progress("native_learning_observer=OPEN; D17_IME_false_IPC_scope=INCONCLUSIVE")
    if args.command == "prepare":
        if (evidence_dir / STATE_NAME).exists():
            raise RunnerError("captured-state.json 已存在；不覆寫，請使用新 evidence-dir")
        verified = verify_artifacts(args.package_dir)
        identity = read_identity(adb)
        adb.android_user_id = identity["android_user_id"]
        adb.android_api = int(identity["api"])
        capabilities = user_capabilities(adb)
        if (identity["qemu"] == "1") != (args.device_kind == "EMULATOR"):
            raise RunnerError("device-kind 與 ro.kernel.qemu 不符；STOP，核對裝置種類")
        default, enabled = read_ime_state(adb)
        selected_raw = read_selected_subtype(adb)
        entries = enabled_entries(enabled)
        if not default.strip() or default.strip() == "null" or default.strip() not in entries:
            raise RunnerError("原 default IME 是 null／未啟用；STOP，不能猜測還原")
        subtype_plan = subtype_restore_plan(adb, default.strip(), selected_raw)
        state = {"schema": 2, "captured_at": utc_now(), "serial": args.serial,
                 "device_kind": args.device_kind, "device_identity": identity, "target_ime": IME,
                 "android_user_id": adb.android_user_id, "user_capabilities": capabilities,
                 "original_default_ime_raw": default, "original_enabled_imes_raw": enabled,
                 "original_selected_input_method_subtype_raw": selected_raw,
                 "subtype_restore": subtype_plan, "preflight_status": "READY" if subtype_plan["ready"] else "BLOCKED",
                 "artifact_verification": verified, "sequence_status": "NOT_EXECUTED"}
        write_json_new(evidence_dir / STATE_NAME, state)
        if not subtype_plan["ready"]:
            write_json_new(operation_dir / "prepare-result.json", {"result": "BLOCKED_SUBTYPE_RESTORE_UNSUPPORTED", "subtype_restore": subtype_plan})
            raise RunnerError(f"BLOCKED_SUBTYPE_RESTORE_UNSUPPORTED：{subtype_plan['reason']}；已保留原狀態，不可 mutation")
        write_json_new(operation_dir / "prepare-result.json", {"result": "PASS", "state_captured": True,
                                                               "device_sequence": "NOT_EXECUTED"})
        progress("PREPARED：四 APK 驗證、原 IME raw 狀態保存；sequence=NOT_EXECUTED")
        return 0
    state = load_state(args)
    bind_state(adb, state)
    if args.command == "restore":
        outcome = recover_state(adb, state)
        persist_outcome(operation_dir / "restore-result.json", outcome)
        if outcome["result"] != "PASS":
            raise RunnerError(f"restore 未完整通過：{outcome.get('error')}")
        progress("RESTORED：原 default 與 enabled/subtypes raw 字串核對一致")
        return 0
    if args.command == "install":
        attempted, installed = [], []
        try:
            check_artifact_state(args, state)
            check_identity(adb, state)
            for name in EXPECTED_PACKAGES:
                attempted.append(name)
                outcome = adb.run(["install", "--user", str(adb.android_user_id), "-r", "-t", str(args.package_dir / name)],
                                  timeout=args.install_timeout, check=False)
                combined = outcome.stdout + outcome.stderr
                if outcome.returncode or outcome.timed_out or "Success" not in outcome.stdout or re.search(r"Failure|INSTALL_FAILED|INSTALL_PARSE_FAILED", combined):
                    raise RunnerError(f"安裝 {name} 失敗（signature mismatch 也 STOP）；不 uninstall／clear：{combined}")
                installed.append(name)
            write_json_new(operation_dir / "install-result.json", {"result": "PASS", "installed": installed})
        except (Exception, KeyboardInterrupt) as primary:
            primary_message = str(primary) or "使用者中斷"
            restored = recover_state(adb, state)
            report = {"result": "FAIL", "failure": primary_message, "exception_type": type(primary).__name__,
                      "attempted": attempted, "confirmed_installed": installed, "restore": restored,
                      "partial_installation_possible": bool(attempted)}
            diagnostic(f"PRIMARY_ERROR={primary_message}; RESTORE_ERROR={restored.get('error')}; RESTORE_RESULT={restored['result']}")
            persist_outcome(operation_dir / "install-failure.json", report)
            raise RunnerError(f"install 失敗：{primary_message}；restore={restored['result']}") from primary
        progress("INSTALLED：-r -t 保留資料；尚未 select／run")
        return 0
    if args.command == "select":
        try:
            check_artifact_state(args, state)
            select(adb, state)
            write_json_new(operation_dir / "select-result.json", {"result": "PASS", "selected_ime": IME})
        except (Exception, KeyboardInterrupt) as primary:
            primary_message = str(primary) or "使用者中斷"
            restored = recover_state(adb, state)
            report = {"result": "FAIL", "failure": primary_message, "exception_type": type(primary).__name__,
                      "restore": restored, "original_selected_subtype_raw": state["original_selected_input_method_subtype_raw"]}
            diagnostic(f"PRIMARY_ERROR={primary_message}; RESTORE_ERROR={restored.get('error')}; RESTORE_RESULT={restored['result']}")
            persist_outcome(operation_dir / "select-failure.json", report)
            raise RunnerError(f"select 失敗：{primary_message}；restore={restored['result']}：{restored.get('error')}") from primary
        progress("SELECTED：測試 app 不負責修改系統 IME；手動操作後必須 restore")
        return 0
    suites, emissions, failure, restored = [], [], None, {"result": "NOT_TESTED"}
    try:
        check_artifact_state(args, state)
        check_identity(adb, state)
        default, enabled = read_ime_state(adb)
        restore_commands(state, default, enabled)
        if default.strip() != IME or IME not in enabled_entries(enabled):
            raise RunnerError("run prerequisite：先明示 select KU-Yin；test 不自行 enable／set")
        manifest = instrumentation_manifest()
        layers = [("existing-app-5-tests", APP_RUNNER, ["-e", "class", EXISTING_CLASSES], "app", EXPECTED_APP_TESTS),
                  ("acceptance-host-suite", HOST_RUNNER, [], "host", EXPECTED_HOST_TESTS)]
        for label, runner, extras, layer, expected in layers:
            outcome = adb.run(["shell", "am", "instrument", "--user", str(adb.android_user_id), "-w", "-r", *extras, runner],
                              timeout=args.instrumentation_timeout, check=False)
            output = outcome.stdout + "\n" + outcome.stderr
            (operation_dir / (label + ".txt")).write_text(output, encoding="utf-8")
            parsed = parse_instrumentation(output, returncode=outcome.returncode, expected_tests=expected,
                                           expected_identities=[item["identity"] for item in manifest[layer]["tests"]],
                                           allow_skips=manifest[layer]["allow_skips"],
                                           case_contract={item["identity"]: item["cases"] for item in manifest[layer]["tests"]})
            suites.append({"layer": label, **parsed})
            emissions.extend(parsed["case_emissions"])
            progress(f"{label}: {parsed['result']}; completed={parsed['completed_tests']}; skipped={parsed['skipped_tests']}")
            if parsed["result"] == "FAIL":
                raise RunnerError(f"{label} protocol／tests 失敗；不繼續後續 suite")
    except (Exception, KeyboardInterrupt) as exc:
        failure = str(exc) or "使用者中斷"
        diagnostic(f"PRIMARY_ERROR={failure}")
    finally:
        restored = recover_state(adb, state)
        diagnostic(f"RESTORE_ERROR={restored.get('error')}; RESTORE_RESULT={restored['result']}")
    cases = summarize_cases(emissions)
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
    summary["original_selected_subtype_raw"] = state["original_selected_input_method_subtype_raw"]
    persist_outcome(operation_dir / "run-summary.json", summary)
    for case_id, record in cases.items():
        persist_outcome(operation_dir / (case_id + ".json"), record)
    progress(f"sequence={summary['sequence_status']}; result={result}; full_acceptance_complete={summary['full_acceptance_complete']}")
    progress(f"restore={restored['result']}; summary={operation_dir / 'run-summary.json'}")
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
        diagnostic(f"STOP：{exc}")
        diagnostic("保留 evidence；不自動 uninstall／pm clear／覆寫其他 IME 設定。")
        return 1


if __name__ == "__main__":
    sys.exit(main())
