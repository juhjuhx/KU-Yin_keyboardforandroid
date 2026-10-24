import importlib.util
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import sys
ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location("winvoice", ROOT / "winvoice.py")
winvoice = importlib.util.module_from_spec(spec)
sys.modules["winvoice"] = winvoice
spec.loader.exec_module(winvoice)


class VoiceContractTest(unittest.TestCase):
    def test_unicode_bmp_and_surrogate_pair(self):
        events = winvoice.unicode_inputs("注😀")
        self.assertEqual(len(events), 6)
        self.assertEqual([x.data.ki.wScan for x in events[::2]], [0x6CE8, 0xD83D, 0xDE00])
        self.assertTrue(all(x.data.ki.dwFlags == winvoice.KEYEVENTF_UNICODE
                            for x in events[::2]))

    def test_empty_input(self):
        self.assertEqual(winvoice.unicode_inputs(""), [])

    def test_config_missing_binary(self):
        config = winvoice.Config(cli=Path("/nonexistent/whisper"), model=Path("/none/model"))
        with self.assertRaises(FileNotFoundError):
            config.validate()

    def test_invalid_language(self):
        with tempfile.TemporaryDirectory() as d:
            executable = Path(d) / "exe"
            executable.touch()
            config = winvoice.Config(executable, executable, language="garbage")
            with self.assertRaises(ValueError):
                config.validate()

    def test_transcribe_invokes_shell_false_and_reads_temp_output(self):
        with tempfile.TemporaryDirectory() as d:
            binary, model, wav = [Path(d) / name for name in ("whisper.exe", "model.bin", "test.wav")]
            for p in (binary, model, wav):
                p.touch()
            def fake_run(argv, *, shell, capture_output, timeout, check):
                self.assertFalse(shell)
                self.assertTrue(capture_output)
                self.assertEqual(timeout, 180)
                self.assertEqual(argv[-1], "-np")
                Path(argv[argv.index("-of") + 1] + ".txt").write_text("你好台灣\n", encoding="utf8")
                return type("Completed", (), {"returncode": 0})()
            self.assertEqual(winvoice.transcribe(wav, winvoice.Config(binary, model), fake_run), "你好台灣")

    def test_transcribe_failure_no_raw_audio_leak(self):
        with tempfile.TemporaryDirectory() as d:
            exe = Path(d) / "exe"
            exe.touch()
            def fail(*_args, **_kwargs):
                return type("Completed", (), {"returncode": 3})()
            with self.assertRaisesRegex(RuntimeError, "退出碼 3"):
                winvoice.transcribe(exe, winvoice.Config(exe, exe), fail)

    def test_traditional_disabled_without_optional_dep(self):
        self.assertEqual(winvoice.to_traditional("简体字", False), "简体字")


if __name__ == "__main__":
    unittest.main()
