"""只用 pure fixtures／fake subprocess；不呼叫真 adb 或 Gradle。"""

import importlib.util
import contextlib
import io
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch


SOURCE = Path(__file__).resolve().parents[1] / "p1_acceptance_device.py"
SPEC = importlib.util.spec_from_file_location("p1_acceptance_device", SOURCE)
device = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = device
SPEC.loader.exec_module(device)


def instrumentation(codes=(0,), *, events=(), final=-1):
    lines = []
    for index, code in enumerate(codes):
        lines += ["INSTRUMENTATION_STATUS: class=example.Tests",
                  f"INSTRUMENTATION_STATUS: test=test{index}",
                  f"INSTRUMENTATION_STATUS: numtests={len(codes)}",
                  "INSTRUMENTATION_STATUS_CODE: 1"]
        if index < len(events):
            lines += ["INSTRUMENTATION_STATUS: stream=" + device.CASE_PREFIX + json.dumps(events[index]),
                      "INSTRUMENTATION_STATUS_CODE: 0"]
        lines += ["INSTRUMENTATION_STATUS: class=example.Tests",
                  f"INSTRUMENTATION_STATUS: test=test{index}",
                  f"INSTRUMENTATION_STATUS: numtests={len(codes)}",
                  f"INSTRUMENTATION_STATUS_CODE: {code}"]
    lines += [f"INSTRUMENTATION_RESULT: stream=OK ({len(codes) - codes.count(-3)} tests)",
              f"INSTRUMENTATION_CODE: {final}"]
    return "\n".join(lines)


def captured(default="original.ime/.Service\n", enabled="original.ime/.Service;123;-45\n"):
    return {"original_default_ime_raw": default, "original_enabled_imes_raw": enabled}


def fake_package(directory):
    package = Path(directory) / "package"
    package.mkdir()
    entries, pins = [], {}
    for name, application_id in device.EXPECTED_PACKAGES.items():
        path = package / name
        path.write_bytes(("synthetic " + name).encode())
        size, digest = path.stat().st_size, device.sha256(path)
        entries.append({"file": name, "bytes": size, "sha256": digest, "application_id": application_id})
        if name in device.FROZEN:
            pins[name] = size, digest
    (package / "artifact-manifest.json").write_text(json.dumps({"artifacts": entries}))
    return package, pins


class InstrumentationParserTest(unittest.TestCase):
    def test_existing_five_completed_and_normal_negative_final_code(self):
        parsed = device.parse_instrumentation(instrumentation((0,) * 5), expected_tests=5)
        self.assertEqual("PASS", parsed["result"])
        self.assertEqual(5, parsed["passed_tests"])

    def test_exit_zero_does_not_hide_runner_error(self):
        output = "INSTRUMENTATION_RESULT: shortMsg=Process crashed.\nINSTRUMENTATION_CODE: 0\n"
        self.assertEqual("FAIL", device.parse_instrumentation(output)["result"])

    def test_failure_banner_and_status_fail_even_when_final_minus_one(self):
        parsed = device.parse_instrumentation(instrumentation((-2,)) + "\nFAILURES!!!")
        self.assertEqual("FAIL", parsed["result"])

    def test_existing_wrong_count_and_skip_do_not_pass(self):
        self.assertEqual("FAIL", device.parse_instrumentation(instrumentation((0,) * 4), expected_tests=5)["result"])
        self.assertEqual("FAIL", device.parse_instrumentation(instrumentation((0, 0, 0, 0, -4)), expected_tests=5)["result"])

    def test_host_assumption_is_inconclusive(self):
        self.assertEqual("INCONCLUSIVE", device.parse_instrumentation(instrumentation((-4,)))["result"])

    def test_case_status_is_not_counted_as_a_junit_completion(self):
        event = {"case_id": "D03", "result": "PASS", "scope": "soft-key-action", "evidence": {"id": 2, "count": 1}}
        parsed = device.parse_instrumentation(instrumentation((0,), events=[event]))
        self.assertEqual("PASS", parsed["result"])
        self.assertEqual(1, parsed["completed_tests"])
        self.assertEqual("D03", parsed["case_emissions"][0]["case_id"])
        self.assertEqual(0, parsed["case_emissions"][0]["junit_terminal_status"])

    def test_pass_emit_from_assumption_test_is_not_a_case_pass(self):
        event = {"case_id": "D03", "result": "PASS", "scope": "UI", "evidence": "snapshot"}
        parsed = device.parse_instrumentation(instrumentation((-4,), events=[event]))
        summary = device.summarize_cases(parsed["case_emissions"])
        self.assertEqual("INCONCLUSIVE", summary["D03"]["observable_result"])
        self.assertEqual("PASS", summary["D03"]["emissions"][0]["result"])

    def test_malformed_case_never_counts_as_pass(self):
        output = instrumentation() + "\nP1_ACCEPTANCE_CASE={broken"
        self.assertEqual("INCONCLUSIVE", device.parse_instrumentation(output)["result"])

    def test_missing_emit_is_not_tested_and_scope_is_separate(self):
        events = [{"case_id": case, "result": "PASS", "scope": "observed UI", "evidence": "snapshot"}
                  for case in ("D03", "D17", "C01")]
        cases = device.summarize_cases(events)
        self.assertEqual("NOT_TESTED", cases["A01"]["full_acceptance_result"])
        self.assertEqual("PASS", cases["D03"]["full_acceptance_result"])
        self.assertEqual("PASS", cases["C01"]["observable_result"])
        self.assertEqual("INCONCLUSIVE", cases["C01"]["full_acceptance_result"])
        self.assertEqual("INCONCLUSIVE", cases["D17"]["full_acceptance_result"])

    def test_b_surface_pass_does_not_close_full_manual_or_native_scope(self):
        events = [{"case_id": case, "result": "PASS", "scope": "single visible UI surface",
                   "evidence": "synthetic snapshot", "junit_terminal_status": 0}
                  for case in ("B01", "B02", "B03")]
        cases = device.summarize_cases(events)
        for case in ("B01", "B02", "B03"):
            with self.subTest(case=case):
                self.assertEqual("PASS", cases[case]["observable_result"])
                self.assertEqual("INCONCLUSIVE", cases[case]["full_acceptance_result"])
                self.assertTrue(cases[case]["scope_limits"])


class RestorationTest(unittest.TestCase):
    def test_newly_enabled_kuyin_only_disables_that_component(self):
        state = captured()
        current = state["original_enabled_imes_raw"].strip() + ":" + device.IME + "\n"
        self.assertEqual([["shell", "ime", "set", "original.ime/.Service"],
                          ["shell", "ime", "disable", device.IME]],
                         device.restore_commands(state, device.IME, current))

    def test_original_enabled_subtypes_not_disabled_or_rewritten(self):
        original = "original.ime/.Service;123;-45:" + device.IME + ";42\n"
        self.assertEqual([["shell", "ime", "set", "original.ime/.Service"]],
                         device.restore_commands(captured(enabled=original), device.IME, original))

    def test_already_restored_is_idempotent(self):
        state = captured()
        self.assertEqual([], device.restore_commands(state, state["original_default_ime_raw"], state["original_enabled_imes_raw"]))

    def test_external_change_or_null_original_stops_without_commands(self):
        for state, current_default, current_enabled in [
            (captured(default="null\n"), device.IME, "original.ime/.Service"),
            (captured(), "other.ime/.Service", "original.ime/.Service"),
            (captured(), device.IME, "original.ime/.Service;999:" + device.IME),
            (captured(enabled="original.ime/.Service:" + device.IME + ";42"), device.IME,
             "original.ime/.Service:" + device.IME + ";43"),
        ]:
            with self.subTest(current=current_enabled), self.assertRaises(device.RunnerError):
                device.restore_commands(state, current_default, current_enabled)

    def test_unsafe_or_unknown_component_cannot_reach_shell(self):
        with self.assertRaises(device.RunnerError):
            device.enabled_entries("original.ime/.Service:bad/$(touch x)")


class FakeAdbTest(unittest.TestCase):
    def test_prepare_verifies_all_apks_then_captures_raw_without_ime_mutation(self):
        calls = []
        properties = {"ro.product.model": "Synthetic", "ro.product.manufacturer": "Fixture",
                      "ro.build.version.sdk": "36", "ro.product.cpu.abilist": "arm64-v8a",
                      "ro.product.cpu.abi": "arm64-v8a", "ro.build.fingerprint": "fake/fingerprint",
                      "ro.kernel.qemu": "0"}
        def fake(argv, **kwargs):
            calls.append(argv)
            if argv[3:] == ["get-state"]:
                output = "device\n"
            elif argv[3:5] == ["shell", "getprop"]:
                output = properties[argv[5]] + "\n"
            elif argv[-1] == "default_input_method":
                output = "original.ime/.Service\r\n"
            elif argv[-1] == "enabled_input_methods":
                output = "original.ime/.Service;123;-45\r\n"
            else:
                self.fail(f"prepare 不得送 mutation command：{argv!r}")
            return subprocess.CompletedProcess(argv, 0, output, "")
        with tempfile.TemporaryDirectory() as directory:
            package, pins = fake_package(directory)
            evidence = Path(directory) / "evidence"
            args = device.parser().parse_args([
                "--serial", "FAKE", "--package-dir", str(package), "--evidence-dir", str(evidence),
                "prepare", "--device-kind", "PHYSICAL"])
            real_adb = device.Adb
            with patch.object(device, "FROZEN", pins), patch.object(device, "Adb", side_effect=lambda serial, log, timeout:
                    real_adb(serial, log, timeout, executor=fake)):
                self.assertEqual(0, device.execute(args))
            state = json.loads((evidence / device.STATE_NAME).read_text())
            self.assertEqual("original.ime/.Service;123;-45\r\n", state["original_enabled_imes_raw"])
            self.assertEqual(4, len(state["artifact_verification"]["artifacts"]))
            self.assertEqual("NOT_EXECUTED", state["sequence_status"])
            self.assertTrue(all(argv[:3] == ["adb", "-s", "FAKE"] for argv in calls))

    def test_artifact_tamper_or_manifest_cannot_override_product_pin(self):
        with tempfile.TemporaryDirectory() as directory:
            package, pins = fake_package(directory)
            with self.assertRaises(device.RunnerError):
                device.verify_artifacts(package)
            with patch.object(device, "FROZEN", pins):
                self.assertEqual(4, len(device.verify_artifacts(package)["artifacts"]))
                (package / "acceptance-host-debug.apk").write_bytes(b"tampered")
                with self.assertRaises(device.RunnerError):
                    device.verify_artifacts(package)

    def test_every_fake_subprocess_has_serial_and_timeout(self):
        calls = []
        def fake(argv, **kwargs):
            calls.append((argv, kwargs))
            return subprocess.CompletedProcess(argv, 0, "device\n", "")
        with tempfile.TemporaryDirectory() as directory:
            adb = device.Adb("EXACT-SERIAL", Path(directory) / "commands.jsonl", executor=fake)
            adb.run(["get-state"])
            adb.run(["shell", "settings", "get", "secure", "default_input_method"], timeout=11)
            self.assertTrue(all(argv[:3] == ["adb", "-s", "EXACT-SERIAL"] for argv, _ in calls))
            self.assertEqual([30, 11], [kwargs["timeout"] for _, kwargs in calls])
            self.assertTrue(all(kwargs["check"] is False for _, kwargs in calls))

    def test_timeout_is_failure_and_keeps_partial_evidence(self):
        def fake(argv, **kwargs):
            raise subprocess.TimeoutExpired(argv, kwargs["timeout"], output=b"partial protocol")
        with tempfile.TemporaryDirectory() as directory:
            log = Path(directory) / "commands.jsonl"
            adb = device.Adb("EXACT-SERIAL", log, executor=fake)
            outcome = adb.run(["shell", "am", "instrument"], check=False)
            self.assertTrue(outcome.timed_out)
            self.assertEqual(124, outcome.returncode)
            self.assertIn("partial protocol", log.read_text())

    def test_state_file_is_never_overwritten(self):
        with tempfile.TemporaryDirectory() as directory:
            state = Path(directory) / "captured-state.json"
            device.write_json_new(state, {"original": True})
            with self.assertRaises(FileExistsError):
                device.write_json_new(state, {"original": False})
            self.assertTrue(json.loads(state.read_text())["original"])

    def test_run_failure_still_tries_restore_and_preserves_not_tested_cases(self):
        with tempfile.TemporaryDirectory() as directory:
            evidence = Path(directory)
            state = {"schema": 1, "serial": "FAKE", "target_ime": device.IME,
                     **captured(), "device_identity": {}, "device_kind": "EMULATOR",
                     "artifact_verification": {}}
            device.write_json_new(evidence / device.STATE_NAME, state)
            args = device.parser().parse_args(["--serial", "FAKE", "--evidence-dir", directory, "run"])
            with patch.object(device, "check_artifact_state"), patch.object(device, "check_identity"), \
                 patch.object(device, "read_ime_state", return_value=("wrong.ime/.Service", "original.ime/.Service")), \
                 patch.object(device, "restore", return_value={"result": "PASS"}) as restore:
                self.assertEqual(2, device.execute(args))
            restore.assert_called_once()
            summaries = list(evidence.glob("run-*/run-summary.json"))
            summary = json.loads(summaries[0].read_text())
            self.assertEqual("NOT_EXECUTED", summary["sequence_status"])
            self.assertEqual("NOT_TESTED", summary["cases"]["D03"]["observable_result"])

    def test_run_artifact_error_still_enters_finally_restore(self):
        with tempfile.TemporaryDirectory() as directory:
            evidence = Path(directory)
            state = {"schema": 1, "serial": "FAKE", "target_ime": device.IME,
                     **captured(), "device_identity": {}, "device_kind": "EMULATOR",
                     "artifact_verification": {}}
            device.write_json_new(evidence / device.STATE_NAME, state)
            args = device.parser().parse_args(["--serial", "FAKE", "--evidence-dir", directory, "run"])
            with patch.object(device, "check_artifact_state", side_effect=device.RunnerError("artifact changed")), \
                 patch.object(device, "restore", return_value={"result": "PASS"}) as restore:
                self.assertEqual(2, device.execute(args))
            restore.assert_called_once()

    def test_select_keeps_primary_and_restore_errors_in_console_and_evidence(self):
        with tempfile.TemporaryDirectory() as directory:
            evidence = Path(directory)
            state = {"schema": 1, "serial": "FAKE", "target_ime": device.IME,
                     **captured(), "device_identity": {}, "device_kind": "EMULATOR",
                     "artifact_verification": {}}
            device.write_json_new(evidence / device.STATE_NAME, state)
            errors = io.StringIO()
            with patch.object(device, "check_artifact_state", side_effect=device.RunnerError("PRIMARY_ARTIFACT_CHANGED")), \
                 patch.object(device, "restore", side_effect=device.RunnerError("SECONDARY_EXTERNAL_IME_CHANGED")), \
                 contextlib.redirect_stderr(errors), contextlib.redirect_stdout(io.StringIO()):
                self.assertEqual(1, device.main(["--serial", "FAKE", "--evidence-dir", directory, "select"]))
            self.assertIn("PRIMARY_ARTIFACT_CHANGED", errors.getvalue())
            self.assertIn("SECONDARY_EXTERNAL_IME_CHANGED", errors.getvalue())
            result = json.loads(next(evidence.glob("select-*/select-result.json")).read_text())
            self.assertEqual("PRIMARY_ARTIFACT_CHANGED", result["failure"])
            self.assertEqual("SECONDARY_EXTERNAL_IME_CHANGED", result["restore"]["error"])
            self.assertTrue(result["restore"]["manual_action_required"])

    def test_signature_failure_stops_without_uninstall_or_second_install(self):
        calls = []
        def fake(argv, **kwargs):
            calls.append(argv)
            return subprocess.CompletedProcess(argv, 1, "Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE]", "")
        with tempfile.TemporaryDirectory() as directory:
            evidence = Path(directory)
            state = {"schema": 1, "serial": "FAKE", "target_ime": device.IME,
                     **captured(), "device_identity": {}, "device_kind": "EMULATOR",
                     "artifact_verification": {}}
            device.write_json_new(evidence / device.STATE_NAME, state)
            args = device.parser().parse_args(["--serial", "FAKE", "--evidence-dir", directory, "install"])
            real_adb = device.Adb
            with patch.object(device, "check_artifact_state"), patch.object(device, "check_identity"), \
                 patch.object(device, "Adb", side_effect=lambda serial, log, timeout: real_adb(serial, log, timeout, executor=fake)):
                with self.assertRaises(device.RunnerError):
                    device.execute(args)
            self.assertEqual(1, len(calls))
            self.assertEqual(["adb", "-s", "FAKE", "install", "-r", "-t"], calls[0][:6])


if __name__ == "__main__":
    unittest.main()
