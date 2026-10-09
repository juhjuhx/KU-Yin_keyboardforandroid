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
