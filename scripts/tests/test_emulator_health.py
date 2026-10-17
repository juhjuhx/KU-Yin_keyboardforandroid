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
          '  mSystemBooted=true mDisplayEnabled=true\n')
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


if __name__ == '__main__':
    unittest.main()

class EmulatorHealthCollectorTest(unittest.TestCase):
    def collect(self, *, bad_screen=False, switched_user=False, broken_evidence=False, final_window=None):
        import json
        import subprocess
        import tempfile
        from unittest.mock import patch
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
            elif 'uiautomator' in args: data = XML.encode()
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

    def test_evidence_write_failure_stops_before_host_launch(self):
        self.collect(broken_evidence=True)
        self.assertFalse(any('start' in c for c in self.calls))
