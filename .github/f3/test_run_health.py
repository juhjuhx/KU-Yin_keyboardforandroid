"""Deterministic wrapper fault injection only. No ADB/process/network is executed."""
from pathlib import Path
import json
import os
import runpy
import subprocess
import sys
import tempfile
import types
import unittest
from unittest.mock import patch

COLLECTOR = Path(os.environ.get('F3_TEST_COLLECTOR_ROOT', str(Path(__file__).resolve().parents[2]/'build/f3-health-tools')))
sys.path.insert(0, str(COLLECTOR))
import emulator_health as real
SCRIPT = Path(__file__).with_name('run_health.py')
BOOT = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'
WINDOW = ('WINDOW MANAGER WINDOWS (dumpsys window)\n  mCurrentFocus=Window{abc u0 ' + real.HOST_COMPONENT + '}\n'
          '  Window #0 Window{abc u0 ' + real.HOST_COMPONENT + '}:\n    isVisible=true\n'
          '  mSystemBooted=true mDisplayEnabled=true\n  mDisplayFrozen=false windows=0\n')
OVERLAY = 'android\n[x] synthetic.profile.overlay\n'

class HealthWrapperTest(unittest.TestCase):
    def exercise(self, scenario):
        original_cwd = Path.cwd()
        with tempfile.TemporaryDirectory(prefix='f3-health-fixture-') as name:
            root = Path(name); home = root/'home'; avd = home/'.android/avd/test.avd'
            avd.mkdir(parents=True); (avd/'config.ini').write_text('AvdId=test\n')
            apk = root/'acceptance-host/build/outputs/apk/debug/acceptance-host-debug.apk'
            apk.parent.mkdir(parents=True); apk.write_bytes(b'synthetic-apk-not-installable')
            tools = root/'build/f3-health-tools'; tools.mkdir(parents=True)
            (tools/'emulator_health.py').write_text('# synthetic source marker\n')
            stage = {'h1': False, 'installs': 0}
            def collect0(serial, out):
                out.mkdir(); (out/'result.json').write_text(json.dumps({'status':'INCONCLUSIVE' if scenario=='initial_failure' else 'HEALTHY', 'identity':{'serial':serial,'user':'0','boot_id':BOOT},'reasons':[]}))
            def collect1(serial, out, prior, **kwargs):
                stage['h1'] = True; out.mkdir(); (out/'result.json').write_text('{"status":"HEALTHY"}')
                records = {}
                for key, text in [('clock-before','1000.123'),('clock-after','1001.123'),('uptime-before','10.1 9.0'),('uptime-after','11.1 10.0'),('overlays-before',OVERLAY),('overlays-after',OVERLAY)]:
                    records[key] = {'exit': 0, 'timed_out': False}
                    (out/(key+'-stdout.txt')).write_text(text)
                if scenario == 'missing_timeline': del records['clock-after']
                (out/'commands.json').write_text(json.dumps(records))
            facade = types.SimpleNamespace(HOST_PACKAGE=real.HOST_PACKAGE, HOST_COMPONENT=real.HOST_COMPONENT,
                SERVICE_ERROR=real.SERVICE_ERROR, complete_window=real.complete_window,
                window_unfrozen=real.window_unfrozen, alert_state=real.alert_state,
                collect_h0=collect0, collect_h1=collect1)
            def fake_run(argv, **kwargs):
                assert argv[0] == 'adb', 'Fixture refuses any real or unexpected subprocess'
                args = argv[3:] if len(argv)>2 and argv[1]=='-s' else argv[1:]
                text=''; err=''; rc=0
                if args == ['devices']: text='List of devices attached\nemulator-5554 device\n'
                elif args[:2] == ['emu','avd']: text='test\nOK\n'
                elif 'ro.kernel.qemu' in args: text='1\n'
                elif 'get-current-user' in args: text='0\n'
                elif '/proc/sys/kernel/random/boot_id' in args: text=BOOT+'\n'
                elif '/proc/uptime' in args: text='10.1 9.0\n'
                elif 'overlay' in args: text=OVERLAY
                elif 'windows' in args: text=WINDOW.replace('mDisplayFrozen=false','mDisplayFrozen=true') if stage['h1'] and scenario=='h1_frozen' else WINDOW
                elif 'displays' in args: text=''
                elif 'lastanr' in args:
                    text='ACTIVITY MANAGER LAST ANR\n<no ANR has occurred since boot>\n'
                    if stage['h1'] and scenario=='h1_anr': text='ACTIVITY MANAGER LAST ANR\nANR in synthetic.systemui\n'
                    if scenario=='bad_anr_header': text='<no ANR has occurred since boot>\n'
                elif 'pm' in args:
                    rc=1
                    if scenario=='pm_error': err='Permission Denial\n'
                elif args[0]=='install': stage['installs']+=1; text='Success\n'
                else: raise AssertionError(('Unmodelled argv, no subprocess allowed',argv))
                return types.SimpleNamespace(returncode=rc,stdout=text,stderr=err)
            env={'CI':'true','GITHUB_ACTIONS':'true','GITHUB_REPOSITORY':'juhjuhx/KU-Yin_keyboardforandroid','GITHUB_SHA':'0'*40,'GITHUB_RUN_ID':'synthetic-fixture'}
            os.chdir(root)
            try:
                with patch.dict(os.environ,env), patch.object(Path,'home',return_value=home), patch.dict(sys.modules,{'emulator_health':facade}), patch.object(subprocess,'run',side_effect=fake_run):
                    try: runpy.run_path(str(SCRIPT),run_name='__main__')
                    except SystemExit as e: code=e.code
                    except AssertionError: code='REFUSED_ASSERTION'
                    else: code=0
                raw=root/'build/f3-health/H0/result.json'
                self.assertTrue(raw.exists()); self.assertIn('status',json.loads(raw.read_text()))
                return code,stage['installs']
            finally: os.chdir(original_cwd)

    def test_complete_healthy_observations_pass(self): self.assertEqual(0,self.exercise('healthy')[0])
    def test_initial_failure_is_not_rescued_by_post(self): self.assertNotEqual(0,self.exercise('initial_failure')[0])
    def test_h1_anr_cannot_report_healthy(self): self.assertNotEqual(0,self.exercise('h1_anr')[0])
    def test_h1_frozen_cannot_report_healthy(self): self.assertNotEqual(0,self.exercise('h1_frozen')[0])
    def test_missing_h1_timeline_cannot_report_healthy(self): self.assertNotEqual(0,self.exercise('missing_timeline')[0])
    def test_pm_error_is_not_host_absence(self):
        code,installs=self.exercise('pm_error');self.assertNotEqual(0,code);self.assertEqual(0,installs)
    def test_no_anr_marker_without_header_is_not_credible(self): self.assertNotEqual(0,self.exercise('bad_anr_header')[0])

if __name__=='__main__':unittest.main()
