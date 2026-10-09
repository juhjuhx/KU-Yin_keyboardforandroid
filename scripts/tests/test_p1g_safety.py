"""P1-G user/subtype/recovery regressions; synthetic subprocesses only."""
import contextlib
import io
import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

from p1g_fakes import FakeAndroid, Session, device


class UserIdentityTests(unittest.TestCase):
    def test_USER_01_prepare_captures_user_zero(self):
        with Session() as s:
            s.execute("prepare")
            self.assertEqual(0, s.state.get("android_user_id"))

    def test_USER_02_prepare_captures_secondary_user(self):
        with Session(FakeAndroid(10)) as s:
            s.execute("prepare")
            self.assertEqual(10, s.state.get("android_user_id"))

    def assert_change_blocks(self, command):
        with Session() as s:
            s.execute("prepare")
            if command == "run":
                selected = [device.IME + "\n", "original.ime/.Service:" + device.IME + "\n", "-1\n"]
                s.fake.states[0] = list(selected)
                s.fake.states[10] = list(selected)
            s.fake.user = 10
            try:
                s.execute(command)
            except device.RunnerError:
                pass
            self.assertEqual([], s.fake.mutations, "user drift must block all mutation including recovery")

    def test_USER_03_change_before_install(self):
        self.assert_change_blocks("install")

    def test_USER_04_change_before_select(self):
        self.assert_change_blocks("select")

    def test_USER_05_change_before_run(self):
        self.assert_change_blocks("run")

    def test_USER_06_change_during_install_stops_remaining(self):
        with Session() as s:
            s.execute("prepare")
            s.fake.after_mutation = lambda argv: setattr(s.fake, "user", 10)
            with self.assertRaises(device.RunnerError):
                s.execute("install")
            self.assertEqual(1, len(s.fake.mutations))

    def test_USER_07_change_before_restore(self):
        with Session() as s:
            s.execute("prepare")
            s.fake.states[0][0] = device.IME + "\n"
            s.fake.states[10] = [device.IME + "\n", "original.ime/.Service:" + device.IME + "\n", "-1\n"]
            s.fake.user = 10
            with self.assertRaises(device.RunnerError):
                s.execute("restore")
            self.assertEqual([], s.fake.mutations)

    def test_USER_08_same_properties_different_user_is_different_identity(self):
        with Session() as s:
            adb = device.Adb("FAKE", s.directory / "commands.jsonl", 30)
            first = device.read_identity(adb)
            s.fake.user = 10
            self.assertNotEqual(first, device.read_identity(adb))

    def test_USER_09_unsupported_secondary_user_commands_fail_closed(self):
        with Session(FakeAndroid(10)) as s:
            s.fake.unsupported = "ime"
            with self.assertRaises(device.RunnerError):
                s.execute("prepare")
            self.assertEqual([], s.fake.mutations)

    def test_USER_10_supported_user_zero_scopes_install(self):
        with Session() as s:
            s.execute("prepare")
            self.assertEqual(0, s.execute("install"))
            self.assertEqual(4, len(s.fake.mutations))
            for argv in s.fake.mutations:
                self.assertIn("--user", argv)
                self.assertEqual("0", argv[argv.index("--user") + 1])


class SubtypeTests(unittest.TestCase):
    def test_SUBTYPE_01_original_selected_value_is_captured(self):
        with Session() as s:
            s.execute("prepare")
            self.assertEqual("-1\n", s.state.get("original_selected_input_method_subtype_raw"))

    def test_SUBTYPE_02_unset_with_zero_declared_subtypes_is_supported(self):
        with Session() as s:
            self.assertEqual(0, s.execute("prepare"))
            self.assertEqual("SENTINEL_WITH_ZERO_SUBTYPES", s.state.get("subtype_restore", {}).get("method"))

    def test_SUBTYPE_03_specific_subtype_is_captured_but_blocked(self):
        with Session() as s:
            s.fake.states[0][2] = "73\n"
            with self.assertRaisesRegex(device.RunnerError, "BLOCKED_SUBTYPE_RESTORE_UNSUPPORTED"):
                s.execute("prepare")
            self.assertEqual("73\n", s.state["original_selected_input_method_subtype_raw"])
            self.assertEqual([], s.fake.mutations)

    def test_SUBTYPE_04_matching_default_with_changed_subtype_is_not_restore_pass(self):
        with Session() as s:
            s.execute("prepare")
            s.fake.states[0][2] = "73\n"
            with self.assertRaises(device.RunnerError):
                s.execute("restore")

    def test_SUBTYPE_05_all_three_states_match_restore_pass(self):
        with Session() as s:
            s.execute("prepare")
            self.assertEqual(0, s.execute("restore"))
            result = json.loads(next(s.evidence.glob("restore-*/restore-result.json")).read_text())
            self.assertTrue(result.get("selected_subtype_matched"))
            self.assertEqual(0, result.get("android_user_id"))

    def test_SUBTYPE_06_unreadable_subtype_blocks_mutation(self):
        with Session() as s:
            s.fake.states[0][2] = "null\n"
            with self.assertRaises(device.RunnerError):
                s.execute("prepare")
            self.assertEqual([], s.fake.mutations)

    def test_SUBTYPE_07_implicit_or_single_subtype_cannot_be_restored(self):
        with Session() as s:
            s.fake.subtype_count = 1
            with self.assertRaises(device.RunnerError):
                s.execute("prepare")
            with self.assertRaises(device.RunnerError):
                s.execute("install")
            self.assertEqual([], s.fake.mutations)

    def test_SUBTYPE_08_secondary_user_subtype_never_reads_user_zero(self):
        with Session(FakeAndroid(10)) as s:
            s.fake.states[0] = ["original.ime/.Service\n", "original.ime/.Service\n", "73\n"]
            s.execute("prepare")
            self.assertEqual("-1\n", s.state.get("original_selected_input_method_subtype_raw"))
            reads = [a for a in s.fake.calls if a[-1] == "selected_input_method_subtype"]
            self.assertTrue(reads)
            self.assertTrue(all(a[a.index("--user") + 1] == "10" for a in reads))

    def test_SUBTYPE_09_unknown_oem_format_and_ambiguous_counts_block(self):
        bad = ["original.ime/.Service:\n  mSubtypeCount=0\n",
               "original.ime/.Service:\n  mId=original.ime/.Service\n  InputMethodSubtype array: count=0\n  InputMethodSubtype array: count=1\n",
               "original.ime/.Service:\n  mId=wrong.ime/.Service\n  InputMethodSubtype array: count=0\n"]
        for listing in bad:
            with self.subTest(listing=listing), Session() as s:
                s.fake.list_override = listing
                with self.assertRaises(device.RunnerError):
                    s.execute("prepare")
                self.assertEqual([], s.fake.mutations)

    def test_SUBTYPE_10_restore_failure_does_not_overwrite_original_evidence(self):
        with Session() as s:
            s.execute("prepare")
            before = (s.evidence / device.STATE_NAME).read_bytes()
            s.fake.states[0][2] = "unexpected\n"
            with self.assertRaises(device.RunnerError):
                s.execute("restore")
            self.assertEqual(before, (s.evidence / device.STATE_NAME).read_bytes())


class UserCapabilityTests(unittest.TestCase):
    def test_official_settings_get_and_multiline_am_synopsis_are_supported(self):
        with Session() as s:
            adb = device.Adb("FAKE", s.directory / "probe.jsonl", 30)
            try:
                result = device.user_capabilities(adb)
            except device.RunnerError:
                result = None
            self.assertIsNotNone(result, "official get --user and multiline instrument help must be recognized")

    def test_install_existing_help_does_not_prove_install_scoping(self):
        fake = FakeAndroid()
        def executor(argv, **kwargs):
            if argv[3:] == ["shell", "pm", "help"]:
                return subprocess.CompletedProcess(argv, 0,
                    "  install [-r] PATH\n  install-existing [--user USER_ID] PACKAGE\n", "")
            return fake(argv, **kwargs)
        with tempfile.TemporaryDirectory() as tmp:
            adb = device.Adb("FAKE", Path(tmp) / "commands.jsonl", executor=executor)
            with self.assertRaises(device.RunnerError):
                device.user_capabilities(adb)


class IoRecoveryTests(unittest.TestCase):
    @contextlib.contextmanager
    def broken_log(self, should_fail):
        original = Path.open
        def opened(path, mode="r", *args, **kwargs):
            if path.name == "commands.jsonl" and mode == "a" and should_fail():
                raise OSError("PRIMARY_DISK_FULL")
            return original(path, mode, *args, **kwargs)
        with patch.object(Path, "open", opened):
            yield

    def assert_recovery_set(self, s):
        self.assertTrue(any(a[3:6] == ["shell", "ime", "set"] and a[-1] == "original.ime/.Service"
                            for a in s.fake.mutations), "cleanup must attempt original IME after log failure")

    def test_IO_01_unwritable_log_before_mutation_blocks_mutation(self):
        with Session() as s:
            s.execute("prepare")
            with self.broken_log(lambda: True), contextlib.redirect_stderr(io.StringIO()):
                with self.assertRaises((OSError, device.RunnerError)):
                    s.execute("select")
            self.assertEqual([], s.fake.mutations)

    def test_IO_02_post_selection_log_failure_attempts_restore(self):
        with Session() as s:
            s.execute("prepare")
            with self.broken_log(lambda: any(a[3:6] == ["shell", "ime", "set"] and a[-1] == device.IME for a in s.fake.mutations)), \
                 contextlib.redirect_stderr(io.StringIO()):
                with self.assertRaises((OSError, device.RunnerError)):
                    s.execute("select")
            self.assert_recovery_set(s)

    def test_IO_03_test_execution_log_failure_attempts_restore(self):
        with Session() as s:
            s.execute("prepare")
            s.execute("select")
            with self.broken_log(lambda: any(a[3:6] == ["shell", "am", "instrument"] for a in s.fake.mutations)), \
                 contextlib.redirect_stderr(io.StringIO()):
                try:
                    result = s.execute("run")
                except (OSError, device.RunnerError):
                    result = 2
            self.assertEqual(2, result)
            self.assert_recovery_set(s)

    def test_IO_04_successful_restore_does_not_hide_primary(self):
        with Session() as s:
            s.execute("prepare")
            error = io.StringIO()
            with self.broken_log(lambda: any(a[3:6] == ["shell", "ime", "set"] and a[-1] == device.IME for a in s.fake.mutations)), \
                 contextlib.redirect_stderr(error):
                with self.assertRaises((OSError, device.RunnerError)):
                    s.execute("select")
            self.assertIn("PRIMARY_DISK_FULL", error.getvalue())
            self.assert_recovery_set(s)

    def test_IO_05_primary_and_restore_failure_remain_distinct(self):
        fake = FakeAndroid()
        original = fake.__call__
        def executor(argv, **kwargs):
            if argv[3:6] == ["shell", "ime", "set"] and argv[-1] == "original.ime/.Service":
                fake.mutations.append(argv)
                return subprocess.CompletedProcess(argv, 1, "", "RESTORE_TRANSPORT_UNAVAILABLE")
            return original(argv, **kwargs)
        with Session() as s:
            s.fake = fake
            # Constructor's closure reads s.fake; substitute the executor explicitly.
            real = device.Adb.side_effect("FAKE", s.directory / "unused", 30).__class__
            with patch.object(device, "Adb", side_effect=lambda serial, log, timeout: real(serial, log, timeout, executor=executor)):
                s.execute("prepare")
                error = io.StringIO()
                with self.broken_log(lambda: any(a[3:6] == ["shell", "ime", "set"] and a[-1] == device.IME for a in fake.mutations)), \
                     contextlib.redirect_stderr(error):
                    with self.assertRaises((OSError, device.RunnerError)):
                        s.execute("select")
                self.assertIn("PRIMARY_DISK_FULL", error.getvalue())
                self.assertIn("RESTORE_TRANSPORT_UNAVAILABLE", error.getvalue())

    def test_IO_06_result_directory_failure_uses_private_fallback_and_no_pass(self):
        with Session() as s:
            s.execute("prepare")
            error = io.StringIO()
            with patch.object(device, "write_json_new", side_effect=OSError("RESULT_DISK_FULL")), \
                 contextlib.redirect_stderr(error):
                with self.assertRaises((OSError, device.RunnerError)):
                    s.execute("select")
            self.assert_recovery_set(s)
            import re
            match = re.search(r"FALLBACK_EVIDENCE=(.+)", error.getvalue())
            self.assertIsNotNone(match, error.getvalue())
            fallback = Path(match.group(1).strip())
            try:
                self.assertEqual(0o600, fallback.stat().st_mode & 0o777)
                self.assertEqual("FAIL", json.loads(fallback.read_text())["result"])
            finally:
                fallback.unlink()

    def test_IO_07_timeout_restores(self):
        with Session() as s:
            s.execute("prepare"); s.execute("select")
            s.fake.after_mutation = lambda argv: (_ for _ in ()).throw(subprocess.TimeoutExpired(argv, 1)) \
                if argv[3:6] == ["shell", "am", "instrument"] else None
            self.assertEqual(2, s.execute("run"))
            self.assert_recovery_set(s)

    def test_IO_08_interrupt_restores(self):
        with Session() as s:
            s.execute("prepare"); s.execute("select")
            s.fake.after_mutation = lambda argv: (_ for _ in ()).throw(KeyboardInterrupt()) \
                if argv[3:6] == ["shell", "am", "instrument"] else None
            self.assertEqual(2, s.execute("run"))
            self.assert_recovery_set(s)

    def test_IO_09_wrong_user_cleanup_has_no_mutation(self):
        with Session() as s:
            s.execute("prepare"); s.execute("select")
            before = len(s.fake.mutations)
            s.fake.user = 10
            self.assertEqual(2, s.execute("run"))
            self.assertEqual(before, len(s.fake.mutations))

    def test_IO_10_normal_command_log_keeps_stdout_exit_and_serial(self):
        with Session() as s:
            s.execute("prepare")
            records = [json.loads(line) for line in next(s.evidence.glob("prepare-*/commands.jsonl")).read_text().splitlines()]
            completed = [r for r in records if "returncode" in r]
            self.assertTrue(completed)
            self.assertTrue(all(r["argv"][:3] == ["adb", "-s", "FAKE"] and r["returncode"] == 0 for r in completed))


class InstallRecoveryTests(unittest.TestCase):
    def test_post_install_logging_failure_retains_partial_install_and_cleanup(self):
        with Session() as s:
            s.execute("prepare")
            with IoRecoveryTests().broken_log(lambda: any(a[3] == "install" for a in s.fake.mutations)), contextlib.redirect_stderr(io.StringIO()):
                with self.assertRaises((OSError, device.RunnerError)):
                    s.execute("install")
            reports = list(s.evidence.glob("install-*/install-failure.json"))
            self.assertEqual(1, len(reports), "partial install must preserve primary + restoration evidence")
            report = json.loads(reports[0].read_text())
            self.assertIn("PRIMARY_DISK_FULL", report["failure"])
            self.assertIn("restore", report)
            self.assertEqual(1, len(s.fake.mutations))


class FailClosedParsingTests(unittest.TestCase):
    def test_user_profile_flag_is_not_user_scope(self):
        fake = FakeAndroid()
        def executor(argv, **kwargs):
            result = fake(argv, **kwargs)
            if argv[-1:] == ["help"]:
                result.stdout = result.stdout.replace("--user", "--user-profile")
            return result
        with tempfile.TemporaryDirectory() as tmp:
            adb = device.Adb("FAKE", Path(tmp)/"commands.jsonl", executor=executor)
            with self.assertRaises(device.RunnerError):
                device.user_capabilities(adb)

    def test_duplicate_malformed_subtype_count_is_never_ready(self):
        for bad in ("01", "unknown", "-1"):
            with self.subTest(bad=bad), Session() as s:
                s.fake.list_override = "original.ime/.Service:\n  mId=original.ime/.Service\n  InputMethodSubtype array: count=0\n  InputMethodSubtype array: count=" + bad + "\n"
                with self.assertRaises(device.RunnerError):
                    s.execute("prepare")
                self.assertEqual([], s.fake.mutations)


class SubtypeIdentityFieldTests(unittest.TestCase):
    def test_duplicate_empty_ime_id_cannot_hide_behind_valid_id(self):
        with Session() as s:
            s.fake.list_override = "original.ime/.Service:\n  mId=original.ime/.Service\n  mId=\n  InputMethodSubtype array: count=0\n"
            with self.assertRaises(device.RunnerError):
                s.execute("prepare")


class ConsoleFailureTests(unittest.TestCase):
    def test_broken_stderr_never_blocks_restore_or_primary_evidence(self):
        class BrokenConsole:
            def write(self, value):
                raise OSError("STDERR_CLOSED")
        with Session() as s:
            s.execute("prepare")
            with IoRecoveryTests().broken_log(lambda: any(a[3:6] == ["shell", "ime", "set"] and a[-1] == device.IME for a in s.fake.mutations)), contextlib.redirect_stderr(BrokenConsole()):
                with self.assertRaises((OSError, device.RunnerError)):
                    s.execute("select")
            self.assertEqual("original.ime/.Service", s.fake.states[0][0].strip())
            reports = list(s.evidence.glob("select-*/select-failure.json"))
            self.assertEqual(1, len(reports))
            self.assertIn("PRIMARY_DISK_FULL", json.loads(reports[0].read_text())["failure"])

    def test_closed_stderr_value_error_does_not_block_cleanup(self):
        console = io.StringIO(); console.close()
        with Session() as s:
            s.execute("prepare")
            with IoRecoveryTests().broken_log(lambda: any(a[3:6] == ["shell", "ime", "set"] and a[-1] == device.IME for a in s.fake.mutations)), contextlib.redirect_stderr(console):
                with self.assertRaises((OSError, ValueError, device.RunnerError)):
                    s.execute("select")
            self.assertEqual("original.ime/.Service", s.fake.states[0][0].strip())


class OperationDirectoryFailureTests(unittest.TestCase):
    def test_run_and_restore_recover_when_operation_directory_cannot_be_created(self):
        for command in ("run", "restore"):
            with self.subTest(command=command), Session() as s:
                s.execute("prepare"); s.execute("select")
                original = Path.mkdir
                def mkdir(path, *args, **kwargs):
                    if path.name.startswith(command + "-"):
                        raise OSError("OPERATION_DIR_DISK_FULL")
                    return original(path, *args, **kwargs)
                error = io.StringIO()
                with patch.object(Path, "mkdir", mkdir), contextlib.redirect_stderr(error):
                    with self.assertRaises((OSError, device.RunnerError)):
                        s.execute(command)
                self.assertEqual("original.ime/.Service", s.fake.states[0][0].strip())
                import re
                for name in re.findall(r"FALLBACK_EVIDENCE=(.+)", error.getvalue()):
                    path = Path(name.strip())
                    self.assertEqual("FAIL", json.loads(path.read_text())["result"])
                    path.unlink()


class ProgressConsoleFailureTests(unittest.TestCase):
    def test_broken_stdout_cannot_block_run_or_explicit_restore(self):
        class BrokenConsole:
            def write(self, value):
                raise BrokenPipeError("STDOUT_CLOSED")
        for command in ("run", "restore"):
            with self.subTest(command=command), Session() as s:
                s.execute("prepare"); s.execute("select")
                with contextlib.redirect_stdout(BrokenConsole()), contextlib.redirect_stderr(io.StringIO()):
                    try:
                        s.execute(command)
                    except (OSError, device.RunnerError):
                        pass
                self.assertEqual("original.ime/.Service", s.fake.states[0][0].strip())

class FrozenHostArtifactTests(unittest.TestCase):
    def test_host_bytes_cannot_be_replaced_by_rewriting_local_manifest(self):
        with Session() as s:
            name='acceptance-host-debug.apk'
            path=s.package/name;path.write_bytes(b'changed host')
            manifest_path=s.package/'artifact-manifest.json'; manifest=json.loads(manifest_path.read_text())
            for item in manifest['artifacts']:
                if item['file']==name:
                    item['bytes']=path.stat().st_size;item['sha256']=device.sha256(path)
            manifest_path.write_text(json.dumps(manifest))
            with self.assertRaises(device.RunnerError):
                s.execute('prepare')
            self.assertEqual([],s.fake.mutations)
