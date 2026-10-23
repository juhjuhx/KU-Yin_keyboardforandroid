"""Health policy tests: synthetic fixtures, no Android target or user data."""
import base64
import copy
from pathlib import Path
import sys
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import emulator_health as health

PKG = 'io.github.juhjuhx.kuyin.acceptancehost'
HOST = PKG + '/.AcceptanceEditorActivity'
WINDOW = ('WINDOW MANAGER WINDOWS (dumpsys window)\n  mCurrentFocus=Window{abc u0 ' + HOST + '}\n'
          '  Window #0 Window{abc u0 ' + HOST + '}:\n    isVisible=true\n'
          '  mSystemBooted=true mDisplayEnabled=true\n  mDisplayFrozen=false windows=0\n')
VISIBLE = '\n  Window #1 Window{def u0 Application Not Responding: com.android.systemui}:\n    mPolicyVisibility=true\n    isVisible=true\n'
HIDDEN = VISIBLE.replace('=true', '=false')
XML = ('<hierarchy><node resource-id="' + PKG + ':id/editor" focused="true" />'
       '<node resource-id="' + PKG + ':id/case_id" text="R23-H1" />'
       '<node resource-id="' + PKG + ':id/input_connection_present" text="true" /></hierarchy>')


def fixture():
    # Independent literals from platform command contracts; not built by the probe.
    inner = b'\x0a\x03ime\x80\x01\x01'
    values = {'devices': 'List of devices attached\nemulator-5554 device product:sdk\n',
              'qemu': '1\n', 'user': '0\n', 'boot': '1\n', 'bootanim': 'stopped\n',
              'android_package': 'package:/system/framework/framework-res.apk\n',
              'activity': 'ACTIVITY MANAGER ACTIVITIES (dumpsys activity activities)\n RootTask #1\n',
              'window': WINDOW, 'imms': b'\x1a\x08' + inner,
              'last-anr': 'ACTIVITY MANAGER LAST ANR\n <no ANR has occurred since boot>\n', 'system_ui': '555\n', 'system_server': '222\n', 'logcat': '',
              'boot_id': 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee\n', 'installed_product': ''}
    result = {k: {'exit': 0, 'stdout': v} for k, v in values.items()}
    result['installed_product']['exit'] = 1
    return result


class EmulatorHealthPolicyTest(unittest.TestCase):
    def test_boot_completed_does_not_override_visible_system_anr(self):
        f = fixture(); f['window']['stdout'] += VISIBLE
        self.assertEqual('UNHEALTHY', health.assess_h0(f, 'emulator-5554')['status'])

    def test_healthy_complete_fixture_passes(self):
        self.assertEqual('HEALTHY', health.assess_h0(fixture(), 'emulator-5554')['status'])

    def test_hidden_anr_window_is_not_falsely_called_visible(self):
        f = fixture(); f['window']['stdout'] += HIDDEN
        self.assertEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'])

    def test_hidden_window_with_anr_history_needs_recovery_evidence(self):
        f = fixture(); f['window']['stdout'] += HIDDEN
        f['logcat']['stdout'] = 'E ActivityManager: ANR in com.android.systemui\n'
        self.assertEqual('INCONCLUSIVE', health.assess_h0(f, 'emulator-5554')['status'])

    def test_unknown_alert_visibility_fails_closed(self):
        f = fixture(); f['window']['stdout'] += VISIBLE.replace('isVisible=true', '')
        self.assertEqual('INCONCLUSIVE', health.assess_h0(f, 'emulator-5554')['status'])

    def test_empty_or_unknown_last_anr_is_not_recovery_evidence(self):
        for value in ['', 'ACTIVITY MANAGER LAST ANR\nunknown history', 'DUMP TIMEOUT']:
            f = fixture(); f['last-anr']['stdout'] = value
            self.assertNotEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'])

    def test_critical_boot_error_is_not_silently_ignored(self):
        f = fixture(); f['logcat']['stdout'] = 'W ActivityManager: Timeout executing service: SystemUI KeyguardService\n'
        self.assertEqual('INCONCLUSIVE', health.assess_h0(f, 'emulator-5554')['status'])

    def test_each_missing_or_failed_required_command_cannot_pass(self):
        for key in fixture():
            for mode in ['missing', 'failure', 'timeout']:
                with self.subTest(key=key, mode=mode):
                    f = fixture()
                    if mode == 'missing': del f[key]
                    elif mode == 'failure': f[key]['exit'] = 17
                    else: f[key]['timed_out'] = True
                    self.assertNotEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'])

    def test_wrong_user_physical_and_multiple_targets_cannot_pass(self):
        for key, value in [('user', '10'), ('qemu', '0'), ('devices', 'List of devices attached\nemulator-5554 device\nphysical device\n')]:
            f = fixture(); f[key]['stdout'] = value
            self.assertNotEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'])

    def test_service_error_text_with_zero_exit_fails_closed(self):
        for key in ['android_package', 'activity', 'window', 'imms']:
            f = fixture(); f[key]['stdout'] = "Can't find service: " + key
            self.assertNotEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'])

    def test_truncated_service_headers_are_not_health_evidence(self):
        for key, value in [('activity', 'ACTIVITY MANAGER ACTIVITIES\n'), ('window', 'WINDOW MANAGER WINDOWS\n')]:
            f = fixture(); f[key]['stdout'] = value
            self.assertNotEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'])

    def test_focus_summary_without_window_inventory_is_incomplete(self):
        partial = 'WINDOW MANAGER WINDOWS\n mCurrentFocus=Window{abc u0 ' + HOST + '}\n'
        f = fixture(); f['window']['stdout'] = partial
        self.assertNotEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'])
        self.assertNotEqual('HEALTHY', health.assess_h1(partial, XML, partial, 'latin', 'latin')['status'])

    def test_package_lookup_error_does_not_prove_product_absence(self):
        f = fixture(); f['installed_product']['stderr'] = b'Error: package manager unavailable'
        self.assertNotEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'])

    def test_h1_timeout_with_old_focus_text_cannot_pass(self):
        self.assertNotEqual('HEALTHY', health.assess_h1(WINDOW + '\nDUMP TIMEOUT\n', XML, WINDOW, 'latin', 'latin')['status'])

    def test_incomplete_boot_and_already_installed_product_do_not_pass(self):
        for key, value in [('boot', '0'), ('bootanim', 'running'), ('system_ui', ''), ('system_server', ''), ('boot_id', '')]:
            f = fixture(); f[key]['stdout'] = value
            self.assertNotEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'])
        f = fixture(); f['installed_product'] = {'exit': 0, 'stdout': 'package:/data/app/base.apk'}
        self.assertNotEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'])

    def test_h1_proves_focus_real_connection_and_unchanged_default(self):
        self.assertEqual('HEALTHY', health.assess_h1(WINDOW, XML, WINDOW, 'latin', 'latin')['status'])

    def test_h1_wrong_window_stale_case_null_connection_or_lost_view_focus_never_pass(self):
        for w, x in [(WINDOW.replace(HOST, 'other/.Activity'), XML), (WINDOW, XML.replace('R23-H1', 'old')),
                     (WINDOW, XML.replace('text="true"', 'text="false"')), (WINDOW, XML.replace('focused="true"', 'focused="false"'))]:
            self.assertNotEqual('HEALTHY', health.assess_h1(w, x, WINDOW, 'latin', 'latin')['status'])

    def test_h1_alert_after_xml_or_changed_ime_blocks(self):
        self.assertEqual('UNHEALTHY', health.assess_h1(WINDOW, XML, WINDOW + VISIBLE, 'latin', 'latin')['status'])
        self.assertEqual('UNHEALTHY', health.assess_h1(WINDOW, XML, WINDOW, 'latin', 'changed')['status'])

    def test_h1_malformed_or_duplicate_hierarchy_cannot_pass(self):
        for x in ['', '<hierarchy>', XML + XML, XML.replace('</hierarchy>', '<node resource-id="' + PKG + ':id/case_id" text="R23-H1" /></hierarchy>')]:
            self.assertNotEqual('HEALTHY', health.assess_h1(WINDOW, x, WINDOW, 'latin', 'latin')['status'])


class CapturedWmsReadinessTest(unittest.TestCase):
    """Exact CI raw; variations below are explicitly synthetic counterexamples."""
    directory = Path(__file__).parent / 'fixtures/r23_wms'

    def observation(self, prefix=''):
        return ((self.directory / (prefix + 'window-windows-stdout.txt')).read_text() + '\n' +
                (self.directory / (prefix + 'window-displays-stdout.txt')).read_text())

    def test_captured_frozen_first_observation_is_rejected(self):
        raw = self.observation()
        self.assertIn('mDisplayFrozen=true', raw)
        self.assertFalse(health.complete_window(raw))

    def test_captured_final_unfrozen_inventory_is_accepted(self):
        self.assertTrue(health.complete_window(self.observation('final-')))

    def test_f1_real_final_inventory_with_only_frozen_boolean_changed_never_passes(self):
        raw = self.observation('final-')
        self.assertEqual(1, raw.count('mDisplayFrozen=false'))
        counterexample = raw.replace('mDisplayFrozen=false', 'mDisplayFrozen=true')
        f = fixture(); f['window']['stdout'] = counterexample
        self.assertNotEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'],
                            'F1: valid focus must not hide a frozen display')

    def test_initial_frozen_raw_with_focus_restored_still_cannot_pass(self):
        import re
        raw = self.observation()
        title = re.findall(r'Window #\d+ (Window\{[^\n}]+\}):', raw)[0]
        counterexample = raw.replace('mCurrentFocus=null', 'mCurrentFocus=' + title)
        self.assertIn('mDisplayFrozen=true', counterexample)
        self.assertFalse(health.complete_window(counterexample))

    def test_missing_unknown_conflicting_or_duplicate_state_fails_closed(self):
        raw = self.observation('final-')
        for state in ['', 'mDisplayFrozen=unknown', 'mDisplayFrozen=false\n mDisplayFrozen=true',
                      'mDisplayFrozen=false\n mDisplayFrozen=false', 'mDisplayFrozen=falseish',
                      'mDisplayFrozen=false\n mDisplayFrozen=', 'mDisplayFrozen=false\n mDisplayFrozen= true']:
            with self.subTest(state=state):
                f = fixture(); f['window']['stdout'] = raw.replace('mDisplayFrozen=false', state)
                self.assertNotEqual('HEALTHY', health.assess_h0(f, 'emulator-5554')['status'])

    def test_h1_also_rejects_a_frozen_bracketing_observation(self):
        frozen = WINDOW.replace('mDisplayFrozen=false', 'mDisplayFrozen=true')
        for before, after in [(frozen, WINDOW), (WINDOW, frozen)]:
            self.assertNotEqual('HEALTHY', health.assess_h1(before, XML, after, 'latin', 'latin')['status'])

    def test_raw_fixture_hashes_match_ci_provenance(self):
        import hashlib
        import json
        manifest = json.loads((self.directory / 'provenance.json').read_text())
        for name, entry in manifest['files'].items():
            data = (self.directory / name).read_bytes()
            self.assertEqual(entry['sha256'], hashlib.sha256(data).hexdigest())
            self.assertEqual(entry['bytes'], len(data))

    def finish_h0(self, initial, final):
        import json
        import subprocess
        import tempfile
        from unittest.mock import patch
        def run(command, **kwargs):
            args = command[3:]
            if 'screencap' in args:
                data = base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=')
            elif 'get-current-user' in args: data = b'0\n'
            elif '/proc/sys/kernel/random/boot_id' in args: data = b'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee\n'
            elif 'window' in args:
                data = '\n'.join(line for line in final.splitlines()
                                 if ('mCurrentFocus=' in line) == ('displays' in args)).encode()
            else: data = b'synthetic\n'
            return subprocess.CompletedProcess(command, 0, data, b'')
        with tempfile.TemporaryDirectory() as tmp, patch.object(health.subprocess, 'run', side_effect=run):
            capture = health.Capture('emulator-5554', Path(tmp) / 'h0')
            capture.records = fixture()
            capture.records['window']['stdout'] = initial
            code = capture.finish(health.assess_h0(capture.records, 'emulator-5554'))
            return code, json.loads((Path(tmp) / 'h0/result.json').read_text())

    def test_r36_raw_config_delta_is_retained_without_a_causal_or_rotation_claim(self):
        directory = Path(__file__).parent / 'fixtures/r36_wms'
        def raw(prefix):
            return ((directory / (prefix + 'window-windows-stdout.txt')).read_text() + '\n' +
                    (directory / (prefix + 'window-displays-stdout.txt')).read_text())
        code, value = self.finish_h0(raw(''), raw('final-'))
        self.assertEqual(3, code)
        self.assertEqual('INCONCLUSIVE', value['status'])
        facts = value.get('display_evidence')
        self.assertIsInstance(facts, dict, 'Missing durable diagnostic evidence for the real frozen transition')
        self.assertEqual('false', facts['initial']['frozen'])
        self.assertEqual('true', facts['final']['frozen'])
        self.assertEqual('0', facts['initial']['rotation'])
        self.assertEqual('0', facts['final']['rotation'])
        self.assertFalse(facts['rotation_changed'])
        self.assertTrue(facts['app_bounds_changed'])
        self.assertEqual(['NavigationBar0', 'StatusBar'], facts['final']['orientation_changing_windows'])
        self.assertEqual('UNRESOLVED', facts['causal_verdict'])
        self.assertEqual('MISSING', facts['guest_event_time'])
        self.assertFalse(facts['atomic_snapshot'])

    def test_r36_missing_or_conflicting_fields_are_unknown_not_false(self):
        for state in ['', 'mDisplayFrozen=false\n mDisplayFrozen=true']:
            with self.subTest(state=state):
                _, value = self.finish_h0(WINDOW, WINDOW.replace('mDisplayFrozen=false', state))
                facts = value.get('display_evidence')
                self.assertIsInstance(facts, dict)
                self.assertEqual('unknown', facts['final']['frozen'])
                self.assertIsNone(facts['app_bounds_changed'])
                self.assertIsNone(facts['rotation_changed'])
                self.assertNotEqual('HEALTHY', value['status'])

    def test_final_recovery_cannot_erase_first_frozen_failure(self):
        code, value = self.finish_h0(WINDOW.replace('mDisplayFrozen=false', 'mDisplayFrozen=true'), WINDOW)
        self.assertNotEqual(0, code)
        self.assertIn('DISPLAY_UNFROZEN_UNVERIFIED', value['reasons'])
        self.assertIn('readiness', value, 'The durable result must identify the original assessment')
        self.assertEqual('INCONCLUSIVE', value['readiness']['first_assessment']['status'])
        self.assertFalse(value['readiness']['atomic_snapshot'])
        self.assertEqual(['window', 'final-window'], value['readiness']['observations'])

    def test_final_frozen_observation_invalidates_initial_health(self):
        code, value = self.finish_h0(WINDOW, WINDOW.replace('mDisplayFrozen=false', 'mDisplayFrozen=true'))
        self.assertNotEqual(0, code)
        self.assertEqual('INCONCLUSIVE', value['status'])
        self.assertIn('FINAL_WINDOW_UNVERIFIED', value['reasons'])


if __name__ == '__main__':
    unittest.main()

class EmulatorHealthCollectorTest(unittest.TestCase):
    def collect(self, *, bad_screen=False, switched_user=False, broken_evidence=False, final_window=None,
                real_terminal_dump=False):
        import json
        import subprocess
        import tempfile
        from unittest.mock import patch
        real_run = subprocess.run
        self.calls = []
        def run(command, **kwargs):
            self.calls.append(command)
            args = command[3:] if command[1:3] == ['-s', 'emulator-5554'] else command[1:]
            data, code = b'', 0
            if args == ['devices', '-l']: data = b'List of devices attached\nemulator-5554 device\n'
            elif args == ['shell', 'getprop', 'ro.kernel.qemu']: data = b'1\n'
            elif args == ['shell', 'am', 'get-current-user']:
                launched = any('start' in c for c in self.calls)
                data = b'10\n' if switched_user and launched else b'0\n'
            elif args == ['shell', 'cat', '/proc/sys/kernel/random/boot_id']: data = b'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee\n'
            elif 'default_input_method' in args: data = b'latin/.IME\n'
            elif 'window' in args:
                # Final snapshot follows the two H1 bracketing window/display pairs.
                count = sum('window' in c for c in self.calls)
                observation = final_window if final_window is not None and count > 4 else WINDOW
                lines = observation.splitlines()
                data = ('\n'.join(line for line in lines if ('mCurrentFocus=' in line) == ('displays' in args))).encode()
            elif 'uiautomator' in args:
                if not real_terminal_dump:
                    data = XML.encode()
                else:
                    # AOSP raw shell has no controlling terminal. Exercise actual
                    # FileWriter-like IO, rather than fabricate XML for every argv.
                    import os
                    import pty
                    import select
                    import signal
                    import time
                    child = [sys.executable, '-c', '''
import sys
try:
    with open(sys.argv[1], 'w') as output:
        output.write(sys.argv[2])
except OSError:
    pass  # AOSP dumper logs IOException but DumpCommand can still print its marker.
print('UI hierchary dumped to: ' + sys.argv[1])
''', args[-1], XML]
                    if '-tt' not in args:
                        return real_run(child, capture_output=True, start_new_session=True,
                                        timeout=kwargs['timeout'])
                    pid, master = pty.fork()
                    if pid == 0:
                        os.execv(child[0], child)
                    status = None
                    try:
                        chunks = []
                        deadline = time.monotonic() + kwargs['timeout']
                        while time.monotonic() < deadline:
                            if not select.select([master], [], [], 0.05)[0]:
                                continue
                            try:
                                chunk = os.read(master, 4096)
                            except OSError:
                                break  # PTY EOF is EIO on Linux.
                            if not chunk:
                                break
                            chunks.append(chunk)
                        waited, status = os.waitpid(pid, os.WNOHANG)
                        while waited != pid and time.monotonic() < deadline:
                            time.sleep(0.005)
                            waited, status = os.waitpid(pid, os.WNOHANG)
                        if waited != pid:
                            status = None
                            raise subprocess.TimeoutExpired(child, kwargs['timeout'])
                        data, code = b''.join(chunks), os.waitstatus_to_exitcode(status)
                    finally:
                        os.close(master)
                        if status is None:
                            os.kill(pid, signal.SIGKILL)
                            os.waitpid(pid, 0)
            elif 'screencap' in args:
                code = 13 if bad_screen else 0
                data = base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=')
            elif 'getprop' in args: data = b'synthetic\n'
            return subprocess.CompletedProcess(command, code, data, b'')
        with tempfile.TemporaryDirectory() as tmp:
            prior = Path(tmp) / 'h0.json'
            prior.write_text(json.dumps({'status': 'HEALTHY', 'identity': {'serial': 'emulator-5554', 'user': '0', 'boot_id': 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'}}))
            with patch.object(health.subprocess, 'run', side_effect=run):
                if broken_evidence:
                    with patch.object(Path, 'write_bytes', side_effect=OSError('evidence disk unavailable')):
                        with self.assertRaises(OSError): health.collect_h1('emulator-5554', Path(tmp) / 'h1', prior)
                        return None
                code = health.collect_h1('emulator-5554', Path(tmp) / 'h1', prior)
                return code, json.loads((Path(tmp) / 'h1/result.json').read_text())

    def test_h1_diagnostic_failure_cannot_return_healthy(self):
        code, result = self.collect(bad_screen=True)
        self.assertNotEqual(0, code)
        self.assertNotEqual('HEALTHY', result['status'])

    def test_window_pair_preserves_each_raw_exit_and_combined_failure(self):
        import subprocess
        import tempfile
        from unittest.mock import patch
        def run(command, **kwargs):
            return subprocess.CompletedProcess(command, 17 if command[-1] == 'displays' else 0, b'raw output', b'')
        with tempfile.TemporaryDirectory() as tmp, patch.object(health.subprocess, 'run', side_effect=run):
            capture = health.Capture('emulator-5554', Path(tmp) / 'capture')
            observation = capture.window('sample')
            self.assertIn('sample-windows', capture.records, 'Each command must retain its own raw result')
            self.assertEqual(0, capture.records['sample-windows']['exit'])
            self.assertEqual(17, capture.records['sample-displays']['exit'])
            self.assertNotEqual(0, observation['exit'])

    def test_window_pair_records_nonatomic_time_bounds_and_raw_commands(self):
        import datetime
        import json
        import subprocess
        import tempfile
        from unittest.mock import patch
        with tempfile.TemporaryDirectory() as tmp, patch.object(health.subprocess, 'run',
                return_value=subprocess.CompletedProcess([], 0, WINDOW.encode(), b'')):
            capture = health.Capture('emulator-5554', Path(tmp) / 'capture')
            observation = capture.window('sample')
            capture.save(health.result([]))
            records = json.loads((Path(tmp) / 'capture/commands.json').read_text())
            for name in ['sample-windows', 'sample-displays']:
                rec = records[name]
                self.assertIsNotNone(datetime.datetime.fromisoformat(rec['started_at_utc']).tzinfo)
                self.assertLessEqual(rec['started_monotonic_ns'], rec['finished_monotonic_ns'])
            self.assertLessEqual(records['sample-windows']['finished_monotonic_ns'],
                                 records['sample-displays']['started_monotonic_ns'])
            self.assertFalse(observation['atomic_snapshot'])
            self.assertEqual(records['sample-windows']['started_monotonic_ns'], observation['started_monotonic_ns'])
            self.assertEqual(records['sample-displays']['finished_monotonic_ns'], observation['finished_monotonic_ns'])

    def test_magic_only_png_is_not_durable_screenshot_evidence(self):
        self.assertFalse(health.complete_png(b'\x89PNG\r\n\x1a\nsynthetic'))

    def test_final_window_must_still_be_complete_and_focused_on_editor(self):
        for window in ['', 'DUMP TIMEOUT\n', WINDOW.replace(HOST, 'other/.Activity')]:
            with self.subTest(window=window):
                code, result = self.collect(final_window=window)
                self.assertNotEqual(0, code)
                self.assertNotEqual('HEALTHY', result['status'])

    def test_user_switch_after_editor_launch_invalidates_health(self):
        code, result = self.collect(switched_user=True)
        self.assertNotEqual(0, code)
        self.assertNotEqual('HEALTHY', result['status'])

    def test_successful_h1_launches_editor_once_without_secure_settings_mutation(self):
        code, result = self.collect()
        self.assertEqual(0, code, result)
        self.assertEqual(1, sum('start' in c for c in self.calls))
        self.assertFalse(any('install' in c or 'put' in c or 'enable' in c for c in self.calls))
        self.assertTrue(all(c[:3] == ['adb', '-s', 'emulator-5554'] or c == ['adb', 'devices', '-l'] for c in self.calls))

    def test_h1_receives_real_xml_from_dump_terminal_not_just_success_marker(self):
        code, result = self.collect(real_terminal_dump=True)
        self.assertEqual(0, code, result)
        self.assertEqual('HEALTHY', result['status'])
        self.assertEqual(1, sum('uiautomator' in c for c in self.calls),
                         'A ready synthetic editor must not need blind dump retries')

    def test_evidence_write_failure_stops_before_host_launch(self):
        self.collect(broken_evidence=True)
        self.assertFalse(any('start' in c for c in self.calls))
