"""Continue only a successful H0/H1 on the same owned CI boot; no Gradle reinstall."""
import hashlib
import json
import os
import re
import subprocess
from pathlib import Path


def run(serial, out, command, save, boot_id):
    assert os.environ.get('GITHUB_ACTIONS') == 'true' and serial.startswith('emulator-')
    assert json.loads((out/'H1_EFFECTIVE.json').read_text())['status'] == 'HEALTHY'
    assert json.loads((out/'H0_EFFECTIVE.json').read_text())['status'] == 'HEALTHY'
    sequence = 0
    def adb(name, args, timeout=20):
        nonlocal sequence
        sequence += 1
        r = command(['adb','-s',serial,*args], timeout=timeout)
        save(f'{sequence:03d}-'+name+'.json',r)
        assert r['exit']==0 and not r['timed_out'], name
        return r['stdout']
    def identity():
        assert adb('runtime-user',['shell','am','get-current-user']).strip()=='0'
        assert adb('runtime-boot',['shell','cat','/proc/sys/kernel/random/boot_id']).strip()==boot_id
    identity()
    manifest = json.loads(Path('.github/f4/p0_instrumentation_manifest.json').read_text())
    for name,digest in manifest['source_files'].items():
        assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==digest, name
    apks = [Path(p) for p in [
        'app/build/outputs/apk/debug/app-debug.apk',
        'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk',
        'acceptance-host/build/outputs/apk/debug/acceptance-host-debug.apk',
        'acceptance-host/build/outputs/apk/androidTest/debug/acceptance-host-debug-androidTest.apk']]
    save('FOUR_APKS.json',dict(source=os.environ['GITHUB_SHA'],apks=[dict(path=str(p),size=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest()) for p in apks]))
    tools=Path(os.environ['ANDROID_HOME'])/'build-tools/36.0.0'
    packages=['io.github.juhjuhx.kuyin','io.github.juhjuhx.kuyin.test',
              'io.github.juhjuhx.kuyin.acceptancehost','io.github.juhjuhx.kuyin.acceptancehost.test']
    for index,apk in enumerate(apks):
        badging=command([str(tools/'aapt'),'dump','badging',str(apk)])
        save('apk-badging-'+str(index)+'.json',badging)
        assert badging['exit']==0 and re.search(r"package: name='([^']+)'",badging['stdout']).group(1)==packages[index]
        signer=command([str(tools/'apksigner'),'verify','--verbose','--print-certs',str(apk)])
        save('apk-signer-'+str(index)+'.json',signer)
        assert signer['exit']==0 and not signer['timed_out']
    def screenshot(name):
        try:
            result=subprocess.run(['adb','-s',serial,'exec-out','screencap','-p'],capture_output=True,timeout=5)
        except subprocess.TimeoutExpired:
            save(name+'-capture.json',dict(exit=None,status='SCREENSHOT_TIMEOUT_NOT_TEST_VERDICT'))
            return
        save(name+'-capture.json',dict(exit=result.returncode,stderr=result.stderr.decode(errors='replace'),scope='SYNTHETIC_DISPOSABLE_RUNTIME_CAPTURE_NOT_MOCKUP'))
        if result.returncode==0 and result.stdout.startswith(b'\x89PNG\r\n\x1a\n'):
            (out/(name+'.png')).write_bytes(result.stdout)
    # Host target was installed before H1: verify it, never replace its running Activity.
    host_path=adb('host-package-path',['shell','pm','path','io.github.juhjuhx.kuyin.acceptancehost']).strip()
    assert host_path.startswith('package:') and '\n' not in host_path
    installed_hash=adb('host-installed-sha',['shell','sha256sum',host_path.removeprefix('package:')]).split()[0]
    assert installed_hash==hashlib.sha256(apks[2].read_bytes()).hexdigest()
    for i in [0,1,3]:
        identity()
        assert 'Success' in adb('install-'+str(i),['install','-r','-t',str(apks[i])])
    ime='io.github.juhjuhx.kuyin/com.example.ime.KuYinInputMethodService'
    def select(label):
        listed=adb(label+'-registered',['shell','ime','list','-s','-a'])
        assert ime in listed.splitlines()
        adb(label+'-enable',['shell','ime','enable','--user','0',ime])
        adb(label+'-select',['shell','ime','set','--user','0',ime])
        assert adb(label+'-selected',['shell','settings','--user','0','get','secure','default_input_method']).strip()==ime
        adb(label+'-imms',['shell','dumpsys','input_method'])
        adb(label+'-windows',['shell','dumpsys','window','windows'])
    select('H2')
    adb('runtime-memory-before',['shell','dumpsys','meminfo','io.github.juhjuhx.kuyin'])
    import r2_ci_protocol as protocol
    protocol.MANIFEST=Path('.github/f4/p0_instrumentation_manifest.json')
    suite={}
    for layer in ['app','host']:
        identity()
        if layer=='host': select('host-precondition')
        package='io.github.juhjuhx.kuyin'+('.acceptancehost' if layer=='host' else '')
        r=command(['adb','-s',serial,'shell','am','instrument','--user','0','-w','-r',package+'.test/androidx.test.runner.AndroidJUnitRunner'],timeout=600)
        save(layer+'-instrumentation-command.json',r)
        (out/(layer+'-instrumentation.txt')).write_text(r['stdout']+'\n'+r['stderr'])
        result=protocol.evaluate(r['stdout']+'\n'+r['stderr'],layer,returncode=r['exit'] if r['exit'] is not None else 124)
        save(layer+'-instrumentation-result.json',result)
        suite[layer]=result
        screenshot(layer+'-final-screen')
        if layer=='app' and result['result']!='PASS':
            save('PRODUCT_RUNTIME_DISPOSITION.json',dict(H2='UNVERIFIED_APP_SUITE_FAILED',App=result,Host='NOT_EXECUTED'))
            return 5
    identity()
    adb('runtime-memory',['shell','dumpsys','meminfo','io.github.juhjuhx.kuyin'])
    anr=adb('runtime-anr',['shell','dumpsys','activity','lastanr'])
    windows=adb('runtime-final-windows',['shell','dumpsys','window','windows'])
    displays=adb('runtime-final-displays',['shell','dumpsys','window','displays'])
    import emulator_health as health
    final_window=windows+'\n'+displays
    unknown,alerts=health.alert_state(final_window)
    if ('ACTIVITY MANAGER LAST ANR' not in anr or '<no ANR has occurred since boot>' not in anr or
            re.search(health.SERVICE_ERROR,anr,re.I) or not health.complete_window(final_window) or
            not health.window_unfrozen(final_window) or unknown or alerts):
        save('PRODUCT_RUNTIME_DISPOSITION.json',dict(H2='INCONCLUSIVE_POST_RUNTIME_HEALTH',suites=suite,
            reason='POST_ANR_OR_WINDOW_UNVERIFIED',unknown=unknown,alerts=alerts))
        return 6
    save('PRODUCT_RUNTIME_DISPOSITION.json',dict(H2='PASS_VIA_ORIGINAL_APP_VISIBILITY_AND_RECREATE_ASSERTIONS',suites=suite,scope='EMULATOR_ONLY_NOT_PHYSICAL_OR_UPGRADE'))
    return 0 if suite['host']['result']=='PASS' else 4
