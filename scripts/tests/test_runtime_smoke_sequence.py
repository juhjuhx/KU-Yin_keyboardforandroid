"""Exercise CI orchestration with fake tools; never invokes ADB or Gradle."""
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
COMPONENT = "io.github.juhjuhx.kuyin/com.example.ime.KuYinInputMethodService"
LATIN = "com.android.inputmethod.latin/.LatinIME"
FAKE_TOOL = r'''#!/usr/bin/env python3
import json, os, pathlib, sys
args = sys.argv[1:]
name = pathlib.Path(sys.argv[0]).name
root = pathlib.Path(os.environ["FAKE_ROOT"])
with (root / "calls.jsonl").open("a") as f:
    f.write(json.dumps([name, args]) + "\n")
state = root / "selected"
component = "io.github.juhjuhx.kuyin/com.example.ime.KuYinInputMethodService"
if name == "adb":
    if args == ["devices", "-l"]:
        print("List of devices attached\nemulator-5554 device model:sdk")
        sys.exit(0)
    if args[:2] == ["-s", "emulator-5554"]:
        args = args[2:]
    if "--user" in args:
        user_option = args.index("--user")
        assert args[user_option + 1] == "0"
        args = args[:user_option] + args[user_option + 2:]
    if args == ["shell", "getprop", "ro.kernel.qemu"]:
        print(os.environ.get("FAKE_QEMU", "1"))
    elif args == ["shell", "am", "get-current-user"]:
        switch = os.environ.get("FAKE_USER_SWITCH")
        changed = ((root / "app-ran").exists() and switch == "after_app") or ((root / "install-ran").exists() and switch == "during_install")
        print("5" if changed else "0")
    elif "dumpsys" in args and (root / "app-ran").exists():
        sys.exit(int(os.environ.get("FAKE_DUMP_EXIT", "0")))
    elif args == ["shell", "test", "-d", "/data/local/tmp/kuyin-r2"]:
        sys.exit(int(os.environ.get("FAKE_PROBE_EXIT", "1")))
    elif args == ["shell", "if [ -d /data/local/tmp/kuyin-r2 ]; then echo PRESENT; else echo ABSENT; fi"]:
        probe_exit = int(os.environ.get("FAKE_PROBE_EXIT", "0"))
        if probe_exit: sys.exit(probe_exit)
        print("ABSENT")
    elif args == ["shell", "ime", "list", "-s", "-a"] or args == ["shell", "ime", "list", "-s"]:
        print(component)
    elif args[:3] == ["shell", "ime", "set"]:
        state.write_text(args[3])
    elif args == ["shell", "settings", "get", "secure", "default_input_method"]:
        print(state.read_text() if state.exists() else "com.android.inputmethod.latin/.LatinIME")
elif name == "gradlew":
    if ":app:installDebug" in args:
        (root / "install-ran").write_text("executed")
    if "connectedDebugAndroidTest" in args:
        # Aggregate task reaches host after product-suite cleanup resets the IME.
        print("SETUP_IME_NOT_SELECTED: aggregate suite lost its precondition", file=sys.stderr)
        sys.exit(1)
    if ":app:connectedDebugAndroidTest" in args:
        assert state.read_text() == component
        (root / "app-ran").write_text("executed")
        if os.environ.get("FAKE_TERMINAL_WRITE_FAILURE") == "1":
            (root / "build/r2-runtime-evidence/terminal.txt").mkdir()
        state.write_text("com.android.inputmethod.latin/.LatinIME")
        sys.exit(int(os.environ.get("FAKE_APP_EXIT", "0")))
    if ":acceptance-host:connectedDebugAndroidTest" in args:
        if state.read_text() != component:
            print("SETUP_IME_NOT_SELECTED", file=sys.stderr)
            sys.exit(1)
'''


class RuntimeSmokeSequenceTest(unittest.TestCase):
    def run_fixture(self, **options):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "scripts").mkdir()
            (root / "app").mkdir()
            (root / "bin").mkdir()
            (root / "app/build.gradle.kts").write_text('applicationId = "io.github.juhjuhx.kuyin"\n')
            (root / "scripts/bootstrap_native_deps.sh").write_text("#!/bin/sh\nexit 0\n")
            (root / "scripts/runtime_smoke.sh").write_bytes((ROOT / "scripts/runtime_smoke.sh").read_bytes())
            for target in (root / "bin/adb", root / "gradlew"):
                target.write_text(FAKE_TOOL)
                target.chmod(0o755)
            env = dict(os.environ, CI="true", FAKE_ROOT=tmp,
                       PATH=str(root / "bin") + os.pathsep + os.environ["PATH"], **options)
            proc = subprocess.run(["bash", "scripts/runtime_smoke.sh"], cwd=root,
                                  env=env, capture_output=True, text=True, timeout=10)
            calls = root / "calls.jsonl"
            return proc, [json.loads(line) for line in calls.read_text().splitlines()] if calls.exists() else []

    def test_reselects_product_between_both_complete_suites(self):
        proc, calls = self.run_fixture()
        self.assertEqual(0, proc.returncode, proc.stderr)
        app = next(i for i, (name, args) in enumerate(calls) if name == "gradlew" and ":app:connectedDebugAndroidTest" in args)
        host = next(i for i, (name, args) in enumerate(calls) if name == "gradlew" and ":acceptance-host:connectedDebugAndroidTest" in args)
        self.assertLess(app, host)
        self.assertTrue(any(name == "adb" and args == ["-s", "emulator-5554", "shell", "ime", "set", "--user", "0", COMPONENT]
                            for name, args in calls[app + 1:host]))
        for name, args in calls:
            if name == "adb" and args != ["devices", "-l"]:
                self.assertEqual(["-s", "emulator-5554"], args[:2])

    def test_product_suite_failure_stops_host_suite(self):
        proc, calls = self.run_fixture(FAKE_APP_EXIT="7")
        self.assertEqual(7, proc.returncode)
        self.assertFalse(any(name == "gradlew" and ":acceptance-host:connectedDebugAndroidTest" in args
                             for name, args in calls))

    def test_refuses_physical_device_before_mutation(self):
        proc, calls = self.run_fixture(FAKE_QEMU="0")
        self.assertNotEqual(0, proc.returncode)
        self.assertFalse(any(name == "gradlew" or "set" in args or "enable" in args
                             for name, args in calls))

    def test_records_actual_android_user_before_installation(self):
        proc, calls = self.run_fixture()
        self.assertEqual(0, proc.returncode, proc.stderr)
        user = next((i for i, (name, args) in enumerate(calls)
                     if name == "adb" and args[-2:] == ["am", "get-current-user"]), None)
        install = next(i for i, (name, args) in enumerate(calls)
                       if name == "gradlew" and ":app:installDebug" in args)
        self.assertIsNotNone(user, "current Android user must be observed before device mutation")
        self.assertLess(user, install)

    def test_app_failure_captures_service_and_window_evidence_without_starting_host(self):
        proc, calls = self.run_fixture(FAKE_APP_EXIT="7")
        self.assertEqual(7, proc.returncode)
        app = next(i for i, (name, args) in enumerate(calls)
                   if name == "gradlew" and ":app:connectedDebugAndroidTest" in args)
        subsequent = [args for name, args in calls[app + 1:] if name == "adb"]
        self.assertTrue(any("dumpsys" in args and "input_method" in args for args in subsequent))
        self.assertTrue(any("dumpsys" in args and "window" in args for args in subsequent))
        self.assertTrue(any("dumpsys" in args and "service" in args and COMPONENT in args for args in subsequent))
        self.assertFalse(any(name == "gradlew" and ":acceptance-host:connectedDebugAndroidTest" in args
                             for name, args in calls))

    def test_android_user_switch_blocks_subsequent_host_installation(self):
        proc, calls = self.run_fixture(FAKE_USER_SWITCH="after_app")
        self.assertNotEqual(0, proc.returncode)
        app = next(i for i, (name, args) in enumerate(calls)
                   if name == "gradlew" and ":app:connectedDebugAndroidTest" in args)
        self.assertFalse(any(name == "gradlew" or (name == "adb" and "set" in args)
                             for name, args in calls[app + 1:]))

    def test_capture_error_does_not_overwrite_original_app_failure(self):
        proc, _ = self.run_fixture(FAKE_APP_EXIT="7", FAKE_DUMP_EXIT="13")
        self.assertEqual(7, proc.returncode)

    def test_terminal_evidence_write_failure_is_not_success(self):
        proc, _ = self.run_fixture(FAKE_TERMINAL_WRITE_FAILURE="1")
        self.assertEqual(2, proc.returncode, "successful suites need durable terminal evidence")

    def test_user_switch_during_install_blocks_ime_mutation(self):
        proc, calls = self.run_fixture(FAKE_USER_SWITCH="during_install")
        self.assertNotEqual(0, proc.returncode)
        self.assertFalse(any(name == "adb" and ("enable" in args or "set" in args)
                             for name, args in calls))

    def test_live_snapshot_probe_transport_failure_is_not_success(self):
        proc, _ = self.run_fixture(FAKE_PROBE_EXIT="13")
        self.assertNotEqual(0, proc.returncode, "ADB transport failure must differ from snapshot absence")

    def test_explicit_host_probe_filters_only_host_and_labels_limited_scope(self):
        proc, calls = self.run_fixture(KUYIN_R2_HOST_PROBE="true")
        self.assertEqual(0, proc.returncode, proc.stderr)
        app = next(args for name, args in calls if name == "gradlew" and ":app:connectedDebugAndroidTest" in args)
        host = next(args for name, args in calls if name == "gradlew" and ":acceptance-host:connectedDebugAndroidTest" in args)
        self.assertFalse(any("RunnerArguments.class=" in arg for arg in app))
        expected = "-Pandroid.testInstrumentationRunnerArguments.class=io.github.juhjuhx.kuyin.acceptancehost.CoreTypingAcceptanceTest#zh06CandidateThenContinuedPhrasePreservesFirstCommit,io.github.juhjuhx.kuyin.acceptancehost.CoreTypingAcceptanceTest#zh01NoToneWoIsSelectableAndCommitted"
        self.assertIn(expected, host)
        self.assertIn("FOCUSED_DIAGNOSTIC_NOT_ACCEPTANCE", proc.stdout)

    def test_invalid_probe_mode_is_rejected_before_device_commands(self):
        proc, calls = self.run_fixture(KUYIN_R2_HOST_PROBE="typo")
        self.assertEqual(2, proc.returncode)
        self.assertEqual([], calls)

    def test_geometry_scope_executes_original_b01_after_all_app_tests(self):
        proc, calls = self.run_fixture(KUYIN_R2_HOST_PROBE="geometry")
        self.assertEqual(0, proc.returncode, proc.stderr)
        app = next(args for name, args in calls if name == "gradlew" and ":app:connectedDebugAndroidTest" in args)
        host = next(args for name, args in calls if name == "gradlew" and ":acceptance-host:connectedDebugAndroidTest" in args)
        self.assertFalse(any("RunnerArguments.class=" in arg for arg in app))
        self.assertIn("-Pandroid.testInstrumentationRunnerArguments.class=io.github.juhjuhx.kuyin.acceptancehost.DachenAcceptanceTest#b01CanonicalKeysAndShiRiHaveStrictPreeditEvidence", host)
        self.assertIn("FOCUSED_DIAGNOSTIC_NOT_ACCEPTANCE", proc.stdout)

    def test_identity_regression_scope_keeps_complete_app_prerequisite(self):
        proc, calls = self.run_fixture(KUYIN_R2_HOST_PROBE="identity")
        self.assertEqual(0, proc.returncode, proc.stderr)
        app = next(args for name, args in calls if name == "gradlew" and ":app:connectedDebugAndroidTest" in args)
        host = next(args for name, args in calls if name == "gradlew" and ":acceptance-host:connectedDebugAndroidTest" in args)
        self.assertFalse(any("RunnerArguments.class=" in arg for arg in app))
        self.assertIn("-Pandroid.testInstrumentationRunnerArguments.class=io.github.juhjuhx.kuyin.acceptancehost.StaleUiObjectIdentityRegressionTest", host)
        self.assertIn("FOCUSED_DIAGNOSTIC_NOT_ACCEPTANCE", proc.stdout)


if __name__ == "__main__":
    unittest.main()
