"""Execute the actual H1-only workflow command with deterministic process doubles."""
import json
import os
from pathlib import Path
import shlex
import subprocess
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


class R31EditorHealthWorkflowTest(unittest.TestCase):
    def run_command(self, fail_stage):
        workflow = (ROOT / '.github/workflows/r2-host-observation-probe.yml').read_text()
        self.assertIn('  editor-health:', workflow, 'H1 must have a separate finite CI route')
        job = workflow.split('  editor-health:', 1)[1].split('\n  probe:', 1)[0]
        commands = [line.strip()[len('script: '):] for line in job.splitlines()
                    if line.strip().startswith('script: ')]
        self.assertEqual(1, len(commands))
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            child = '''import json, os, sys
from pathlib import Path
args = sys.argv[1:]
stage = args[1] if args and args[0] == 'scripts/emulator_health.py' else 'install'
with Path(os.environ['R31_CALLS']).open('a') as log:
    log.write(json.dumps({'stage': stage, 'args': args}) + '\\n')
raise SystemExit(3 if stage == os.environ['R31_FAIL_STAGE'] else 0)
'''
            for name in ['python3', 'adb']:
                path = directory / name
                path.write_text('#!' + sys.executable + '\n' + child)
                path.chmod(0o700)
            log = directory / 'calls.jsonl'
            env = {**os.environ, 'PATH': str(directory) + os.pathsep + os.environ['PATH'],
                   'R31_CALLS': str(log), 'R31_FAIL_STAGE': fail_stage}
            result = subprocess.run(shlex.split(commands[0]), cwd=directory, env=env,
                                    capture_output=True, text=True, timeout=10)
            return result, [json.loads(row) for row in log.read_text().splitlines()]

    def test_h0_refusal_prevents_any_install_or_h1(self):
        result, calls = self.run_command('h0')
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(['h0'], [row['stage'] for row in calls])

    def test_install_failure_prevents_h1(self):
        result, calls = self.run_command('install')
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(['h0', 'install'], [row['stage'] for row in calls])

    def test_h1_refusal_remains_failure(self):
        result, calls = self.run_command('h1')
        self.assertNotEqual(0, result.returncode)
        self.assertEqual(['h0', 'install', 'h1'], [row['stage'] for row in calls])

    def test_success_uses_same_serial_and_only_installs_host(self):
        result, calls = self.run_command('none')
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(['h0', 'install', 'h1'], [row['stage'] for row in calls])
        for row in calls:
            self.assertIn('emulator-5554', row['args'])
        self.assertEqual(['-s', 'emulator-5554', 'install', '-r', '-t', '--user', '0',
                          'build/r31-reference/acceptance-host/build/outputs/apk/debug/acceptance-host-debug.apk'],
                         calls[1]['args'])
        self.assertEqual('build/r31-editor-health/h0/result.json', calls[2]['args'][-1])
