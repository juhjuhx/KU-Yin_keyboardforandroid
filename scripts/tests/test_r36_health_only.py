"""Controller contract with real capture files and deterministic subprocess doubles."""
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import r36_health_only as probe
from test_emulator_health import WINDOW, VISIBLE


class OneHealthObservationTest(unittest.TestCase):
    def execute(self, *, h0_status='INCONCLUSIVE', final_user='0', post_window=WINDOW):
        calls, runs = [], []
        identity = {'serial': 'emulator-5554', 'user': '0', 'boot_id': 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'}
        def command(argv, **kwargs):
            calls.append(argv)
            self.assertFalse(any(x in argv for x in ['install', 'enable', 'set', 'put', 'instrument', 'start', 'uninstall', 'clear']))
            if argv[1:] == ['devices', '-l']: data = 'List of devices attached\nemulator-5554 device\n'
            elif 'get-current-user' in argv: data = ('0' if sum('get-current-user' in c for c in calls) == 1 else final_user) + '\n'
            elif 'ro.kernel.qemu' in argv: data = '1\n'
            elif '/proc/sys/kernel/random/boot_id' in argv: data = identity['boot_id'] + '\n'
            elif 'window' in argv:
                # Match the real separate WMS windows/displays command interface.
                data = '\n'.join(line for line in post_window.splitlines()
                                 if ('mCurrentFocus=' in line) == ('displays' in argv))
            else: data = 'synthetic\n'
            return subprocess.CompletedProcess(argv, 0, data.encode(), b'')
        def h0(serial, directory):
            runs.append(serial); Path(directory).mkdir()
            data = {'status': h0_status, 'reasons': [] if h0_status == 'HEALTHY' else ['FINAL_WINDOW_UNVERIFIED'], 'identity': identity}
            (Path(directory) / 'result.json').write_text(json.dumps(data))
            return probe.health.EXIT[h0_status]
        with tempfile.TemporaryDirectory() as tmp, patch.object(probe.health.subprocess, 'run', side_effect=command), patch.object(probe.health, 'collect_h0', side_effect=h0):
            code = probe.run_health('emulator-5554', tmp)
            return code, json.loads((Path(tmp) / 'observer/result.json').read_text()), calls, runs

    def test_later_unfrozen_window_never_replaces_the_original_h0_failure(self):
        code, result, calls, runs = self.execute()
        self.assertEqual(3, code)
        self.assertEqual(['emulator-5554'], runs)
        self.assertEqual('INCONCLUSIVE', result['original_h0_status'])
        self.assertIn('FINAL_WINDOW_UNVERIFIED', result['reasons'])
        self.assertEqual(2, sum('logcat' in c for c in calls))

    def test_same_boot_healthy_h0_is_preserved_without_running_h1_or_h2(self):
        code, result, calls, runs = self.execute(h0_status='HEALTHY')
        self.assertEqual(0, code)
        self.assertEqual('HEALTHY', result['status'])
        self.assertEqual(1, len(runs))
        self.assertEqual('UNRESOLVED', result['causal_verdict'])

    def test_user_switch_invalidates_even_healthy_h0(self):
        code, result, _, _ = self.execute(h0_status='HEALTHY', final_user='10')
        self.assertEqual(3, code)
        self.assertIn('DEVICE_IDENTITY_CHANGED', result['reasons'])

    def test_frozen_or_missing_post_window_does_not_label_the_whole_experiment_healthy(self):
        for post in [WINDOW.replace('mDisplayFrozen=false', 'mDisplayFrozen=true'), '']:
            with self.subTest(post=post):
                code, result, _, runs = self.execute(h0_status='HEALTHY', post_window=post)
                self.assertEqual(3, code, 'Later contradictory observation must remain visible')
                self.assertEqual('HEALTHY', result['original_h0_status'])
                self.assertIn('POST_HEALTH_WINDOW_UNVERIFIED', result['reasons'])
                self.assertEqual(1, len(runs))

    def test_later_visible_system_anr_is_not_a_healthy_experiment(self):
        code, result, _, _ = self.execute(h0_status='HEALTHY', post_window=WINDOW + VISIBLE)
        self.assertEqual(2, code)
        self.assertEqual('HEALTHY', result['original_h0_status'])
        self.assertIn('BLOCKING_SYSTEM_ALERT', result['reasons'])
