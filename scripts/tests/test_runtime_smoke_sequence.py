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
    if args == ["shell", "getprop", "ro.kernel.qemu"]:
        print(os.environ.get("FAKE_QEMU", "1"))
    elif args == ["shell", "ime", "list", "-s", "-a"] or args == ["shell", "ime", "list", "-s"]:
        print(component)
    elif args[:3] == ["shell", "ime", "set"]:
        state.write_text(args[3])
    elif args == ["shell", "settings", "get", "secure", "default_input_method"]:
        print(state.read_text() if state.exists() else "com.android.inputmethod.latin/.LatinIME")
elif name == "gradlew":
    if "connectedDebugAndroidTest" in args:
        # Aggregate task reaches host after product-suite cleanup resets the IME.
        print("SETUP_IME_NOT_SELECTED: aggregate suite lost its precondition", file=sys.stderr)
        sys.exit(1)
    if ":app:connectedDebugAndroidTest" in args:
        assert state.read_text() == component
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
        self.assertTrue(any(name == "adb" and args == ["-s", "emulator-5554", "shell", "ime", "set", COMPONENT]
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


if __name__ == "__main__":
    unittest.main()
