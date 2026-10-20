"""TEST_ONLY: receiving null-root before timeout must retain timed raw evidence."""
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import emulator_health as health


class H1OutputTimelineRegressionTest(unittest.TestCase):
    def test_null_root_received_before_timeout_retains_bytes_and_receive_time(self):
        # This actual child emits the original failure then remains alive.
        # It tests evidence collection, not Android root acquisition or its cause.
        message = b'ERROR: null root node returned by UiTestAutomationBridge.\n'
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            # Invoke Python directly: temporary shebang files undergo additional
            # macOS executable assessment (~378ms vs 17ms observed), unrelated to
            # the 400ms process IO budget under test. Replace only the ADB process.
            import subprocess
            real_popen = subprocess.Popen
            child = [sys.executable, '-c', 'import os, time; os.write(1, ' + repr(message) + '); time.sleep(10)']
            capture = health.Capture('emulator-5554', directory / 'capture')
            capture.output_timeline = True
            def launch(command, **kwargs):
                self.assertEqual('adb', command[0])
                return real_popen(child, **kwargs)
            with patch.object(health.subprocess, 'Popen', side_effect=launch):
                record = capture.run('ui-0', ['shell', '-tt', 'uiautomator', 'dump', '/dev/tty'], timeout=0.4)
            self.assertTrue(record['timed_out'])
            self.assertIsNone(record['exit'])
            self.assertEqual(message.decode(), record['stdout'], repr(record))
            self.assertEqual(message, (directory / 'capture/ui-0-stdout.txt').read_bytes())
            events = record.get('output_observations', [])
            self.assertTrue(events, 'Partial null-root bytes need a receive timestamp before timeout')
            self.assertEqual('stdout', events[0]['stream'])
            self.assertEqual(0, events[0]['offset'])
            self.assertEqual(len(message), events[0]['bytes'])
            self.assertLess(events[0]['received_monotonic_ns'], record['finished_monotonic_ns'])
            self.assertIn('+00:00', events[0]['received_at_utc'])
            capture.save(health.result(['EDITOR_CAPTURE_UNVERIFIED']))
            saved = json.loads((directory / 'capture/commands.json').read_text())
            self.assertEqual(events, saved['ui-0']['output_observations'])
            self.assertEqual('INCONCLUSIVE', json.loads((directory / 'capture/result.json').read_text())['status'])

    def test_timeout_retains_tail_written_while_reader_is_descheduled(self):
        import os
        import subprocess
        import time
        with tempfile.TemporaryDirectory() as tmp:
            marker = Path(tmp) / 'tail-written'
            code = ('import os,time,pathlib\n'
                    'os.write(1,b"FIRST\\n")\n'
                    'time.sleep(.02)\n'
                    'os.write(1,b"\\x00\\xffTAIL\\r\\n")\n'
                    'os.write(2,b"\\xfeERR\\x00")\n'
                    'pathlib.Path(' + repr(str(marker)) + ').touch()\n'
                    'time.sleep(10)\n')
            real_read = os.read
            def descheduled(fd, size):
                data = real_read(fd, size)
                if data == b'FIRST\n':
                    time.sleep(1.2)  # Actual pipe bytes arrive before the 1s deadline.
                return data
            with patch.object(health.os, 'read', side_effect=descheduled):
                with self.assertRaises(subprocess.TimeoutExpired) as caught:
                    health.observe_output([sys.executable, '-c', code], 1, [])
            self.assertTrue(marker.exists(), 'Child must actually have written its tail')
            self.assertEqual(b'FIRST\n\x00\xffTAIL\r\n', caught.exception.stdout)
            self.assertEqual(b'\xfeERR\x00', caught.exception.stderr)
