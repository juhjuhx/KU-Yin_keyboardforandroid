"""TEST_ONLY: distinguish direct child state from inherited pipe lifetime."""
import json
import os
from pathlib import Path
import signal
import subprocess
import sys
import tempfile
import time
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import emulator_health as health


class H1ProcessCompletionTest(unittest.TestCase):
    def capture_child(self, directory, child, *, wait_for_exit=False, ready_file=None):
        capture = health.Capture('emulator-5554', directory / 'capture')
        capture.output_timeline = True
        real_popen = subprocess.Popen
        # Setup is not the behavior under test: start the real child and wait
        # for its explicit state before measuring pipe/exit observation. This
        # does not change the Android command or H1 capture deadline.
        process = real_popen(child, stdout=subprocess.PIPE, stderr=subprocess.PIPE, start_new_session=True)
        try:
            if wait_for_exit:
                self.assertEqual(0, process.wait(timeout=5))
            if ready_file is not None:
                deadline = time.monotonic() + 5
                while not ready_file.exists() and time.monotonic() < deadline:
                    self.assertIsNone(process.poll(), 'Fixture exited before readiness')
                    time.sleep(.005)
                self.assertTrue(ready_file.exists(), 'Fixture did not reach its declared state')
            def launch(command, **kwargs):
                self.assertEqual('adb', command[0])
                return process
            with patch.object(health.subprocess, 'Popen', side_effect=launch):
                record = capture.run('ui-0', ['shell', '-tt', 'uiautomator', 'dump', '/dev/tty'], timeout=.4)
        finally:
            # Kill the isolated fixture group even if the parent exited before
            # writing its descendant PID. No child is left holding a pipe.
            try: os.killpg(process.pid, signal.SIGKILL)
            except ProcessLookupError: pass
            process.wait(timeout=5)
            for pipe in (process.stdout, process.stderr):
                if not pipe.closed: pipe.close()
        capture.save(health.result(['EDITOR_CAPTURE_UNVERIFIED']) if record['timed_out'] else health.result([]))
        saved = json.loads((directory / 'capture/commands.json').read_text())['ui-0']
        return record, saved

    def test_exited_child_with_inherited_open_pipes_does_not_prove_adb_is_running(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            pidfile = directory / 'inheritor.pid'
            code = ('import os,time,pathlib\n'
                    'child=os.fork()\n'
                    'if child==0:\n time.sleep(30)\n os._exit(0)\n'
                    'pathlib.Path(' + repr(str(pidfile)) + ').write_text(str(child))\n'
                    'os.write(1,b"<hierarchy></hierarchy>\\n")\n'
                    'os._exit(0)\n')
            try:
                record, saved = self.capture_child(directory, [sys.executable, '-c', code], wait_for_exit=True)
                self.assertTrue(record['timed_out'])
                self.assertIsNone(record['exit'], 'Incomplete capture must remain a timeout')
                self.assertIn('<hierarchy>', record['stdout'])
                state = record.get('process_completion', {})
                self.assertIn('child_returncode_before_cleanup', state,
                              'Missing child/pipe evidence hides an already exited direct child')
                self.assertEqual(0, state['child_returncode_before_cleanup'])
                self.assertEqual({'stdout': False, 'stderr': False}, state['pipe_eof_observed_before_cleanup'])
                self.assertFalse(state['cleanup_kill_attempted'])
                self.assertEqual(state, saved['process_completion'])
                self.assertEqual('INCONCLUSIVE', json.loads((directory / 'capture/result.json').read_text())['status'])
            finally:
                if pidfile.exists():
                    try: os.kill(int(pidfile.read_text()), signal.SIGKILL)
                    except ProcessLookupError: pass

    def test_live_child_at_deadline_retains_primary_timeout_and_records_cleanup(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            ready = directory / 'ready'
            code = ('import os,time,pathlib; os.write(1,b"<hierarchy></hierarchy>\\n"); '
                    'pathlib.Path(' + repr(str(ready)) + ').touch(); time.sleep(30)')
            record, saved = self.capture_child(directory, [sys.executable, '-c', code], ready_file=ready)
            self.assertTrue(record['timed_out'])
            self.assertIsNone(record['exit'])
            state = record.get('process_completion', {})
            self.assertIn('child_returncode_before_cleanup', state,
                          'Timeout alone cannot show whether the direct child was still alive')
            self.assertIsNone(state['child_returncode_before_cleanup'])
            self.assertEqual({'stdout': False, 'stderr': False}, state['pipe_eof_observed_before_cleanup'])
            self.assertTrue(state['cleanup_kill_attempted'])
            self.assertEqual(-signal.SIGKILL, state['child_returncode_after_cleanup'])
            self.assertEqual(state, saved['process_completion'])

    def test_normal_exit_requires_completed_streams_and_keeps_real_return_code(self):
        with tempfile.TemporaryDirectory() as tmp:
            record, saved = self.capture_child(Path(tmp), [sys.executable, '-c', 'pass'], wait_for_exit=True)
            self.assertFalse(record['timed_out'])
            self.assertEqual(0, record['exit'])
            state = record.get('process_completion', {})
            self.assertEqual({'stdout': True, 'stderr': True}, state.get('pipe_eof_observed_before_cleanup'))
            self.assertEqual(0, state['child_returncode_before_cleanup'])
            self.assertFalse(state['cleanup_kill_attempted'])
            self.assertEqual(state, saved['process_completion'])
