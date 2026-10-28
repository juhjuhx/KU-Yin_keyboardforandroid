"""TEST_ONLY F3 disposable-GitHub-runner H0 -> real Host H1. No product install."""
from pathlib import Path
import datetime
import hashlib
import json
import os
import re
import subprocess
import sys
import time

assert os.environ.get('CI') == 'true' and os.environ.get('GITHUB_ACTIONS') == 'true'
assert os.environ.get('GITHUB_REPOSITORY') == 'juhjuhx/KU-Yin_keyboardforandroid'
root = Path.cwd()
out = root / 'build/f3-health'
out.mkdir(parents=True, exist_ok=False)

def command(argv, timeout=15):
    started = time.monotonic_ns()
    utc = datetime.datetime.now(datetime.timezone.utc).isoformat()
    try:
        p = subprocess.run(argv, capture_output=True, text=True, timeout=timeout)
        return dict(argv=argv, started_utc=utc, started_monotonic_ns=started,
                    elapsed_ns=time.monotonic_ns()-started, exit=p.returncode,
                    stdout=p.stdout, stderr=p.stderr, timed_out=False)
    except subprocess.TimeoutExpired as exc:
        def text(value):
            return value.decode(errors='replace') if isinstance(value, bytes) else value or ''
        return dict(argv=argv, started_utc=utc, started_monotonic_ns=started,
                    elapsed_ns=time.monotonic_ns()-started, exit=None,
                    stdout=text(exc.stdout), stderr=text(exc.stderr), timed_out=True)

def save(name, value):
    (out / name).write_text(json.dumps(value, indent=2)+'\n')

devices = command(['adb', 'devices'])
targets = [line.split() for line in devices['stdout'].splitlines()[1:] if line.strip()]
assert devices['exit'] == 0 and len(targets) == 1 and targets[0][1] == 'device'
serial = targets[0][0]
assert serial.startswith('emulator-')

def adb(*args):
    return command(['adb', '-s', serial, *args])

# A fresh AVD is owned by this one job; never a personal device or an old local AVD.
identity = {key: adb(*args) for key, args in {
    'name': ['emu', 'avd', 'name'], 'qemu': ['shell', 'getprop', 'ro.kernel.qemu'],
    'user': ['shell', 'am', 'get-current-user'],
    'boot_id': ['shell', 'cat', '/proc/sys/kernel/random/boot_id'],
    'guest_uptime': ['shell', 'cat', '/proc/uptime'],
    'overlays': ['shell', 'cmd', 'overlay', 'list', '--user', '0'],
}.items()}
configs = list((Path.home()/'.android/avd').glob('*.avd/config.ini'))
save('AVD_CREATION_RECEIPT.json', dict(source=os.environ['GITHUB_SHA'], run_id=os.environ['GITHUB_RUN_ID'],
    authority='single fresh GitHub disposable AVD; force creation, no snapshots',
    serial=serial, identity=identity, avd_configs={str(p):p.read_text() for p in configs},
    expected_image='API33 default x86_64', profile='pixel_2',
    clock_note='Host receipt times and guest uptime are different clock domains; no atomic snapshot'))
assert len(configs) == 1 and identity['name']['stdout'].splitlines()[0] == 'test'
assert all(v['exit'] == 0 and not v['timed_out'] for v in identity.values())
assert identity['qemu']['stdout'].strip() == '1' and identity['user']['stdout'].strip() == '0'

sys.path.insert(0, str(root/'build/f3-health-tools'))
import emulator_health as h
save('COLLECTOR_SOURCE.json', dict(pin='aa1bf8753a9e66ab249ba6921cd4ea22a3810a06',
    source_hashes={str(p.name):hashlib.sha256(p.read_bytes()).hexdigest()
                   for p in (root/'build/f3-health-tools').iterdir() if p.is_file()}))
h.collect_h0(serial, out/'H0')
raw = (out/'H0/result.json').read_bytes()
initial = json.loads(raw)
post = {key: adb(*args) for key, args in {
    'windows': ['shell', 'dumpsys', '-t', '2', 'window', 'windows'],
    'displays': ['shell', 'dumpsys', '-t', '2', 'window', 'displays'],
    'user': ['shell', 'am', 'get-current-user'],
    'boot': ['shell', 'cat', '/proc/sys/kernel/random/boot_id'],
    'anr': ['shell', 'dumpsys', '-t', '2', 'activity', 'lastanr'],
    'overlays': ['shell', 'cmd', 'overlay', 'list', '--user', '0'],
}.items()}
window = post['windows']['stdout']+'\n'+post['displays']['stdout']
reasons = []
if any(p['exit'] != 0 or p['timed_out'] for p in post.values()): reasons.append('POST_COMMAND_UNVERIFIED')
if not h.complete_window(window): reasons.append('POST_WINDOW_UNVERIFIED')
if not h.window_unfrozen(window): reasons.append('POST_DISPLAY_UNFROZEN_UNVERIFIED')
unknown, visible = h.alert_state(window)
reasons += unknown + visible
if ('ACTIVITY MANAGER LAST ANR' not in post['anr']['stdout'] or
        '<no ANR has occurred since boot>' not in post['anr']['stdout'] or
        re.search(h.SERVICE_ERROR, post['anr']['stdout'], re.I)):
    reasons.append('POST_ANR_UNVERIFIED')
if post['user']['stdout'].strip() != initial.get('identity', {}).get('user') or post['boot']['stdout'].strip() != initial.get('identity', {}).get('boot_id'): reasons.append('POST_IDENTITY_CHANGED')
if not identity['overlays']['stdout'].strip() or identity['overlays']['exit'] != 0 or identity['overlays']['stdout'] != post['overlays']['stdout']: reasons.append('OVERLAY_STATE_CHANGED_OR_UNKNOWN')
status = initial['status']
if status == 'HEALTHY' and reasons: status = 'UNHEALTHY' if visible else 'INCONCLUSIVE'
save('H0_EFFECTIVE.json', dict(status=status, first_result_sha256=hashlib.sha256(raw).hexdigest(),
    original_result=initial, post=post, reasons=reasons, atomic_snapshot=False,
    scope='BOUNDED_ENVIRONMENT_ONLY; RRO caller and universal initializer completion unresolved'))
if status != 'HEALTHY':
    save('STAGE_DISPOSITION.json', dict(H0=status, H1='NOT_EXECUTED', H2='NOT_EXECUTED', product_installed=False))
    raise SystemExit(2)

host = root/'acceptance-host/build/outputs/apk/debug/acceptance-host-debug.apk'
save('HOST_APK_IDENTITY.json', dict(source=os.environ['GITHUB_SHA'], path=str(host.relative_to(root)),
    sha256=hashlib.sha256(host.read_bytes()).hexdigest(), size=host.stat().st_size))
host_absent = adb('shell', 'pm', 'path', '--user', '0', h.HOST_PACKAGE)
save('HOST_ABSENCE.json', host_absent)
assert host_absent['exit'] == 1 and not host_absent['timed_out']
assert not host_absent['stdout'].strip() and not host_absent['stderr'].strip()
installed = adb('install', '-r', '-t', str(host))
save('HOST_INSTALL.json', installed)
assert installed['exit'] == 0 and 'Success' in installed['stdout']
h.collect_h1(serial, out/'H1', out/'H0/result.json', output_timeline=True)
h1_raw = (out/'H1/result.json').read_bytes()
h1 = json.loads(h1_raw)
h1_post = {key: adb(*args) for key, args in {
    'windows': ['shell', 'dumpsys', '-t', '2', 'window', 'windows'],
    'displays': ['shell', 'dumpsys', '-t', '2', 'window', 'displays'],
    'user': ['shell', 'am', 'get-current-user'],
    'boot': ['shell', 'cat', '/proc/sys/kernel/random/boot_id'],
    'anr': ['shell', 'dumpsys', '-t', '2', 'activity', 'lastanr'],
    'overlays': ['shell', 'cmd', 'overlay', 'list', '--user', '0'],
}.items()}
h1_reasons = []
if any(v['exit'] != 0 or v['timed_out'] for v in h1_post.values()): h1_reasons.append('H1_POST_COMMAND_UNVERIFIED')
h1_window = h1_post['windows']['stdout']+'\n'+h1_post['displays']['stdout']
if not h.complete_window(h1_window): h1_reasons.append('H1_POST_WINDOW_OR_UNFROZEN_UNVERIFIED')
h1_unknown, h1_visible = h.alert_state(h1_window)
h1_reasons += h1_unknown + h1_visible
if (h1_post['user']['stdout'].strip() != initial['identity']['user'] or
        h1_post['boot']['stdout'].strip() != initial['identity']['boot_id']):
    h1_reasons.append('H1_POST_IDENTITY_CHANGED')
anr = h1_post['anr']['stdout']
if ('ACTIVITY MANAGER LAST ANR' not in anr or '<no ANR has occurred since boot>' not in anr or
        re.search(h.SERVICE_ERROR, anr, re.I)): h1_reasons.append('H1_POST_ANR_UNVERIFIED')
commands = json.loads((out/'H1/commands.json').read_text())
timeline = {}
for name in ['clock-before', 'clock-after', 'uptime-before', 'uptime-after', 'overlays-before', 'overlays-after']:
    rec = commands.get(name, {})
    path = out/'H1'/f'{name}-stdout.txt'
    text = path.read_text(errors='replace').strip() if path.is_file() else ''
    timeline[name] = text
    if rec.get('exit') != 0 or rec.get('timed_out') or not text:
        h1_reasons.append('H1_TIMELINE_UNVERIFIED:' + name)
for name in ['clock-before', 'clock-after']:
    if not re.fullmatch(r'\d+\.\d{1,9}', timeline[name]): h1_reasons.append('H1_GUEST_CLOCK_UNVERIFIED:' + name)
for name in ['uptime-before', 'uptime-after']:
    if not re.fullmatch(r'\d+(?:\.\d+)?\s+\d+(?:\.\d+)?', timeline[name]): h1_reasons.append('H1_GUEST_UPTIME_UNVERIFIED:' + name)
if (timeline['overlays-before'] != timeline['overlays-after'] or
        timeline['overlays-after'] != h1_post['overlays']['stdout'].strip()):
    h1_reasons.append('H1_OVERLAY_STATE_CHANGED_OR_UNKNOWN')
h1_status = h1['status']
if h1_status == 'HEALTHY' and h1_reasons: h1_status = 'UNHEALTHY' if h1_visible else 'INCONCLUSIVE'
save('H1_EFFECTIVE.json', dict(status=h1_status, original_result=h1,
    original_result_sha256=hashlib.sha256(h1_raw).hexdigest(), post=h1_post,
    reasons=h1_reasons, timeline=timeline, atomic_snapshot=False))
save('STAGE_DISPOSITION.json', dict(H0=status, H1=h1_status, H2='NOT_EXECUTED',
    product_installed=False, scope='H0/H1 only; no KU-Yin install/select/typing/update'))
raise SystemExit(0 if h1_status == 'HEALTHY' else 3)
