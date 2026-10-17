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
import json, os, pathlib, sys, hashlib, base64
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
    elif os.environ.get("FAKE_R23") == "1" and args == ["shell", "getprop", "sys.boot_completed"]:
        print("1")
    elif os.environ.get("FAKE_R23") == "1" and args == ["shell", "getprop", "init.svc.bootanim"]:
        print("stopped")
    elif os.environ.get("FAKE_R23") == "1" and args == ["shell", "cat", "/proc/sys/kernel/random/boot_id"]:
        print("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
    elif os.environ.get("FAKE_R23") == "1" and args[:2] == ["shell", "getprop"]:
        print("synthetic")
    elif os.environ.get("FAKE_R23") == "1" and "screencap" in args:
        sys.stdout.buffer.write(base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII='))
    elif os.environ.get("FAKE_R23") == "1" and "uiautomator" in args:
        pkg = 'io.github.juhjuhx.kuyin.acceptancehost'
        ic = 'false' if os.environ.get('FAKE_NULL_IC') == '1' else 'true'
        print(f'<hierarchy><node resource-id="{pkg}:id/editor" focused="true"/><node resource-id="{pkg}:id/case_id" text="R23-H1"/><node resource-id="{pkg}:id/input_connection_present" text="{ic}"/></hierarchy>')
    elif os.environ.get("FAKE_R23") == "1" and args[:2] == ["shell", "pidof"]:
        print("22")
    elif os.environ.get("FAKE_R23") == "1" and args == ["shell", "pm", "path", "android"]:
        print("package:/system/framework/framework-res.apk")
    elif os.environ.get("FAKE_R23") == "1" and "dumpsys" in args and "window" in args:
        if 'displays' in args:
            print("WINDOW MANAGER DISPLAY CONTENTS\n mCurrentFocus=Window{abc u0 io.github.juhjuhx.kuyin.acceptancehost/.AcceptanceEditorActivity}")
        else:
            print("WINDOW MANAGER WINDOWS (dumpsys window windows)")
            print("  Window #0 Window{abc u0 io.github.juhjuhx.kuyin.acceptancehost/.AcceptanceEditorActivity}:\n    isVisible=true\n  mSystemBooted=true mDisplayEnabled=true")
            if os.environ.get("FAKE_ANR") == "1":
                print("  Window #1 Window{def u0 Application Not Responding: com.android.systemui}:\n    mPolicyVisibility=true isOnScreen=true\n    isVisible=true")
    elif os.environ.get("FAKE_R23") == "1" and "lastanr" in args:
        print("ACTIVITY MANAGER LAST ANR\n <no ANR has occurred since boot>")
    elif os.environ.get("FAKE_R23") == "1" and "dumpsys" in args and "activity" in args:
        print("ACTIVITY MANAGER ACTIVITIES (dumpsys activity activities)\n  RootTask #1")
    elif os.environ.get("FAKE_R23") == "1" and "dumpsys" in args and "input_method" in args and "--proto" in args:
        sys.stdout.buffer.write(b'\x1a\x08\x0a\x03ime\x80\x01\x01')
    elif "dumpsys" in args and "input_method" in args and "--proto" in args:
        sys.stdout.buffer.write(b'\x1a\x05\x0a\x03ime')
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
    elif args == ["shell", "pm", "path", "io.github.juhjuhx.kuyin"]:
        if not (root / "install-ran").exists():
            sys.exit(1)
        print("package:/data/app/synthetic/kuyin/base.apk")
    elif args[:2] == ["shell", "sha256sum"]:
        print(os.environ.get("FAKE_INSTALLED_APK_HASH", hashlib.sha256((root/"app/build/outputs/apk/debug/app-debug.apk").read_bytes()).hexdigest()), args[2])
    elif args and args[0] == "install":
        if 'acceptance-host-debug.apk' not in args[-1]: (root / "install-ran").write_text("executed")
        print("Success")
    elif args[:3] == ["shell", "am", "instrument"]:
        manifest = json.loads((root / "scripts/r2_current_instrumentation_manifest.json").read_text())
        layer = 'host' if '.acceptancehost.test/' in args[-1] else 'app'
        tests = manifest[layer]['tests']
        if '-e' in args and 'class' in args:
            wanted = args[args.index('class') + 1]
            tests = [test for test in tests if test['identity'] == wanted]
        for index, test in enumerate(tests, 1):
            cls, method = test["identity"].split("#")
            for code in [1, -2 if os.environ.get("FAKE_APP_EXIT", "0") != "0" and index == 1 else 0]:
                print(f"INSTRUMENTATION_STATUS: class={cls}\nINSTRUMENTATION_STATUS: test={method}\nINSTRUMENTATION_STATUS: numtests={len(tests)}\nINSTRUMENTATION_STATUS: current={index}\nINSTRUMENTATION_STATUS_CODE: {code}")
                if code == 1:
                    for case in test['cases']:
                        event = dict(case, result='PASS', scope='SYNTHETIC_PROTOCOL_UNIT_ONLY', evidence={'synthetic': True})
                        print('INSTRUMENTATION_STATUS: stream=P1_ACCEPTANCE_CASE=' + json.dumps(event))
                        print('INSTRUMENTATION_STATUS_CODE: 2')
        print("INSTRUMENTATION_RESULT: stream=" + ("FAILURES!!!" if os.environ.get("FAKE_APP_EXIT", "0") != "0" else f"OK ({len(tests)} tests)"))
        print("INSTRUMENTATION_CODE: -1")
        (root / "app-ran").write_text("executed")
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
        if os.environ.get("FAKE_PACKAGE_REMOVED") == "1":
            (root / "install-ran").unlink()
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
            for filename in ["r2_ci_protocol.py", "p1_acceptance_device.py", "r2_current_instrumentation_manifest.json"] + (["emulator_health.py"] if (ROOT / "scripts/emulator_health.py").exists() else []):
                (root / "scripts" / filename).write_bytes((ROOT / "scripts" / filename).read_bytes())
            for target in (root / "bin/adb", root / "gradlew"):
                target.write_text(FAKE_TOOL)
                target.chmod(0o755)
            if options.get("KUYIN_R22_BINDING_ARM"):
                import zipfile
                apk = root / "app/build/outputs/apk/debug/app-debug.apk"
                apk.parent.mkdir(parents=True)
                with zipfile.ZipFile(apk, 'w') as archive:
                    archive.writestr('assets/synthetic', 'fixture only')
                signer = root / "fake-sdk/build-tools/0/apksigner"
                signer.parent.mkdir(parents=True)
                signer.write_text('#!/bin/sh\nexit 0\n')
                signer.chmod(0o755)
                options['ANDROID_HOME'] = str(root / 'fake-sdk')
            env = dict(os.environ, CI="true", FAKE_ROOT=tmp,
                       PATH=str(root / "bin") + os.pathsep + os.environ["PATH"], **options)
            proc = subprocess.run(["bash", "scripts/runtime_smoke.sh"], cwd=root,
                                  # Binding snapshots spawn many isolated fake Python/SDK tools; this is
                                  # only their host-process watchdog, never an Android UI timeout.
                                  env=env, capture_output=True, text=True,
                                  timeout=30 if options.get("KUYIN_R22_BINDING_ARM") or options.get('KUYIN_R23_HEALTH') else 10)
            proc.evidence = {str(path.relative_to(root)): path.read_text() for path in (root / "build").rglob("*-protocol.json")}
            proc.health = {str(path.relative_to(root)): json.loads(path.read_text()) for path in (root / 'build').rglob('result.json')}
            proc.checkpoints = [path.name for path in (root / 'build').rglob('*-time.txt')]
            calls = root / "calls.jsonl"
            return proc, [json.loads(line) for line in calls.read_text().splitlines()] if calls.exists() else []

    def test_r23_blocking_system_anr_stops_before_any_install(self):
        proc, calls = self.run_fixture(KUYIN_R23_HEALTH="true", FAKE_R23="1", FAKE_ANR="1")
        self.assertNotEqual(0, proc.returncode, "A blocking SystemUI ANR must stop the CI pipeline")
        self.assertFalse(any(name == "gradlew" or (name == "adb" and any(a in args for a in ["install", "set", "enable", "instrument"])) for name, args in calls), "H0 failure must precede every APK install/IME mutation/test")
        self.assertEqual('UNHEALTHY', proc.health['build/r2-runtime-evidence/h0/result.json']['status'])

    def test_r23_healthy_editor_precedes_product_install_and_tests(self):
        proc, calls = self.run_fixture(KUYIN_R23_HEALTH="true", FAKE_R23="1")
        self.assertEqual(0, proc.returncode, proc.stderr)
        self.assertEqual('HEALTHY', proc.health['build/r2-runtime-evidence/h1/result.json']['status'])
        editor = next(i for i, (_, args) in enumerate(calls) if 'start' in args)
        product = next(i for i, (name, args) in enumerate(calls) if name == 'gradlew' and ':app:installDebug' in args)
        self.assertLess(editor, product)

    def test_r23_null_editor_connection_blocks_product_install_and_binding(self):
        proc, calls = self.run_fixture(KUYIN_R23_HEALTH="true", FAKE_R23="1", FAKE_NULL_IC="1")
        self.assertNotEqual(0, proc.returncode)
        self.assertEqual('INCONCLUSIVE', proc.health['build/r2-runtime-evidence/h1/result.json']['status'])
        self.assertFalse(any(name == 'gradlew' or 'instrument' in args or 'set' in args or 'enable' in args for name, args in calls))

    def test_preinstalled_arm_installs_both_apks_before_select_and_never_gradle_connected(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="preinstalled", KUYIN_R22_APP_ONLY="true")
        self.assertEqual(0, proc.returncode, proc.stderr)
        direct = [i for i, (name, args) in enumerate(calls) if name == "adb" and "instrument" in args]
        self.assertEqual(1, len(direct), "B must execute the exact App suite directly, after preinstallation")
        first_select = next(i for i, (name, args) in enumerate(calls) if name == "adb" and "set" in args)
        installs = [i for i, (name, args) in enumerate(calls) if name == "adb" and "install" in args]
        self.assertEqual(2, len(installs))
        self.assertTrue(all(i < first_select < direct[0] for i in installs))
        self.assertFalse(any(name == "gradlew" and any("connectedDebugAndroidTest" in arg or ":installDebug" in arg for arg in args)
                             for name, args in calls))

    def test_fresh_device_package_absence_is_observed_without_aborting_install(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="preinstalled", KUYIN_R22_APP_ONLY="true", FAKE_REAL_PACKAGE_PATH="1")
        self.assertEqual(0, proc.returncode, proc.stderr)
        self.assertEqual(1, sum(name == "adb" and "instrument" in args for name, args in calls))

    def test_direct_failure_is_classified_even_if_secondary_dump_fails(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="preinstalled", FAKE_APP_EXIT="7", FAKE_DUMP_EXIT="13")
        self.assertNotEqual(0, proc.returncode)
        evidence = proc.evidence.get("build/r22-preinstalled/app-protocol.json")
        self.assertIsNotNone(evidence, "Primary JUnit failure must be durably parsed before secondary dumps")
        result = json.loads(evidence)
        self.assertEqual('FAIL', result['result'])
        self.assertEqual(1, result['failed_tests'])

    def test_legacy_final_package_absence_does_not_falsify_passing_junit(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="legacy", KUYIN_R22_APP_ONLY="true", FAKE_PACKAGE_REMOVED="1")
        self.assertEqual(0, proc.returncode, proc.stderr)

    def test_installed_target_hash_mismatch_blocks_before_ime_selection(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="preinstalled", FAKE_INSTALLED_APK_HASH="0" * 64)
        self.assertNotEqual(0, proc.returncode)
        self.assertFalse(any(name == "adb" and ("set" in args or "enable" in args or "instrument" in args) for name, args in calls))

    def test_app_only_control_rejects_user_switch_after_app(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="legacy", KUYIN_R22_APP_ONLY="true", FAKE_USER_SWITCH="after_app")
        self.assertNotEqual(0, proc.returncode)
        self.assertFalse(any(name == "gradlew" and ":acceptance-host:connectedDebugAndroidTest" in args for name, args in calls))
        app = next(i for i, (name, args) in enumerate(calls) if name == 'gradlew' and ':app:connectedDebugAndroidTest' in args)
        self.assertFalse(any(name == 'adb' and 'dumpsys' in args and ('input_method' in args or 'window' in args or 'service' in args)
                             for name, args in calls[app+1:]), 'Wrong-user EXIT must not invoke client/global dumps')

    def test_app_and_host_preselection_checkpoints_are_both_retained(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="preinstalled")
        self.assertEqual(0, proc.returncode, proc.stderr)
        self.assertIn('app-before-select-time.txt', proc.checkpoints)
        self.assertIn('host-before-select-time.txt', proc.checkpoints)
        self.assertEqual(3, sum(name == 'adb' and 'instrument' in args for name, args in calls))

    def test_invalid_binding_arm_blocks_even_device_inventory(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="unsafe")
        self.assertEqual(2, proc.returncode)
        self.assertEqual([], calls)

    def test_preinstalled_app_failure_preserves_failure_and_never_installs_host(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="preinstalled", FAKE_APP_EXIT="7")
        self.assertEqual(2, proc.returncode, proc.stderr)
        self.assertEqual(1, sum(name == "adb" and "instrument" in args for name, args in calls))
        self.assertFalse(any("acceptance-host" in str(args) for name, args in calls if name == "adb" and "install" in args))

    def test_preinstalled_path_refuses_physical_device(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="preinstalled", FAKE_QEMU="0")
        self.assertEqual(2, proc.returncode)
        self.assertFalse(any(name == "gradlew" or "install" in args or "instrument" in args or "set" in args for name, args in calls))

    def test_binding_critical_window_never_full_dumps_client_input_method(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="legacy", KUYIN_R22_APP_ONLY="true")
        self.assertEqual(0, proc.returncode, proc.stderr)
        app = next(i for i, (name, args) in enumerate(calls) if name == "gradlew" and ":app:connectedDebugAndroidTest" in args)
        full_dumps = [args for name, args in calls[:app] if name == "adb" and "dumpsys" in args and "input_method" in args and "--proto" not in args]
        self.assertEqual([], full_dumps, "Full IMMS text dump can call client IMM.isActive/checkFocus")

    def test_binding_diagnostics_include_package_process_and_imms_events(self):
        proc, calls = self.run_fixture(KUYIN_R22_BINDING_ARM="legacy", KUYIN_R22_APP_ONLY="true")
        self.assertEqual(0, proc.returncode, proc.stderr)
        event_calls = [args for name, args in calls if name == "adb" and "logcat" in args and "events" in args]
        self.assertTrue(event_calls, "Package/process events must cover installation and instrumentation boundaries")
        self.assertTrue(any("ActivityManager" in args and "InputMethodManagerService" in args and "InputMethodBindingController" in args
                            for name, args in calls if name == "adb" and "logcat" in args))

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
