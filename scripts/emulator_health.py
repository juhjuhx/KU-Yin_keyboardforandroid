"""TEST_ONLY disposable-CI emulator health; no dictionary/settings access or repair."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import selectors
import subprocess
import shutil
import time
import xml.etree.ElementTree as ET
import zlib

from r2_ci_protocol import server_proto

HOST_PACKAGE = 'io.github.juhjuhx.kuyin.acceptancehost'
HOST_COMPONENT = HOST_PACKAGE + '/.AcceptanceEditorActivity'
PRODUCT = 'io.github.juhjuhx.kuyin'
EXIT = {'HEALTHY': 0, 'UNHEALTHY': 2, 'INCONCLUSIVE': 3}
SERVICE_ERROR = r'DUMP TIMEOUT|Permission Denial|Can.t find service|Error dumping'


def result(reasons, blockers=()):
    return {'status': 'UNHEALTHY' if blockers else 'INCONCLUSIVE' if reasons else 'HEALTHY',
            'reasons': list(blockers) + list(reasons)}


def alert_state(window):
    """Visibility is per Window block: an existing hidden ANR is not a visible one."""
    unknown, visible = [], []
    blocks = re.split(r'(?m)^\s*Window #\d+ ', window)[1:]
    for block in blocks:
        if not re.search(r'Application Not Responding:|Application Error:|ty=SYSTEM_ALERT|\bSYSTEM_ERROR\b', block):
            continue
        flags = re.findall(r'\bisVisible=(true|false)\b', block)
        if flags == ['true']: visible.append('BLOCKING_SYSTEM_ALERT')
        elif flags != ['false']: unknown.append('ALERT_VISIBILITY_UNVERIFIED')
    return unknown, visible


def window_unfrozen(window):
    """Only one explicit false is evidence; absent/unknown/conflicting is not."""
    return (len(re.findall(r'\bmDisplayFrozen=', window)) == 1 and
            re.findall(r'\bmDisplayFrozen=([^\s]+)', window) == ['false'])


def complete_window(window):
    """API33 WMS observation, not an atomic snapshot or continuous health proof."""
    if 'WINDOW MANAGER WINDOWS' not in window or re.search(SERVICE_ERROR, window, re.I): return False
    focus = re.findall(r'mCurrentFocus=(Window\{[^\n}]+\})', window)
    headers = re.findall(r'(?m)^\s*Window #(\d+) (Window\{[^\n}]+\}):', window)
    blocks = re.split(r'(?m)^\s*Window #\d+ ', window)[1:]
    return (window_unfrozen(window) and len(focus) == 1 and bool(headers) and
            [int(index) for index, _ in headers] == list(range(len(headers))) and
            focus[0] in [title for _, title in headers] and
            'mSystemBooted=true mDisplayEnabled=true' in window and
            all(len(re.findall(r'\bisVisible=(?:true|false)\b', block.split('\n  mGlobalConfiguration=', 1)[0])) == 1
                for block in blocks))


def complete_png(data):
    """Validate screenshot chunk framing/CRC/end; not an image content oracle."""
    if not data.startswith(b'\x89PNG\r\n\x1a\n'): return False
    offset, kinds = 8, []
    while offset + 12 <= len(data):
        size = int.from_bytes(data[offset:offset + 4], 'big')
        end = offset + 12 + size
        if end > len(data): return False
        chunk = data[offset + 4:end - 4]
        if zlib.crc32(chunk) != int.from_bytes(data[end - 4:end], 'big'): return False
        kinds.append(chunk[:4]); offset = end
        if chunk[:4] == b'IEND':
            return size == 0 and offset == len(data) and kinds[0] == b'IHDR' and b'IDAT' in kinds
    return False


def assess_h0(captures, serial):
    reasons, blockers = [], []
    required = ['devices', 'qemu', 'user', 'boot', 'bootanim', 'android_package', 'activity',
                'window', 'imms', 'system_ui', 'system_server', 'logcat', 'boot_id', 'installed_product', 'last-anr']
    values = {}
    for name in required:
        rec = captures.get(name, {})
        expected_exit = 1 if name == 'installed_product' else 0
        if rec.get('exit') != expected_exit or rec.get('timed_out'):
            reasons.append('COMMAND_UNVERIFIED:' + name)
        values[name] = rec.get('stdout', b'' if name == 'imms' else '')
    text = lambda k: values[k].decode(errors='replace') if isinstance(values[k], bytes) else values[k]
    targets = [s.split()[:2] for s in text('devices').splitlines() if s.strip() and not s.startswith('List of devices')]
    if not re.fullmatch(r'emulator-\d+', serial) or targets != [[serial, 'device']] or text('qemu').strip() != '1':
        reasons.append('TARGET_NOT_UNIQUE_DISPOSABLE_EMULATOR')
    if text('user').strip() != '0': reasons.append('ANDROID_USER_NOT_ZERO')
    if text('boot').strip() != '1' or text('bootanim').strip() != 'stopped': reasons.append('BOOT_INCOMPLETE')
    if not re.fullmatch(r'[0-9a-fA-F-]{36}', text('boot_id').strip()): reasons.append('BOOT_ID_UNVERIFIED')
    if text('installed_product').strip(): reasons.append('PRODUCT_ALREADY_PRESENT_BEFORE_H0')
    if captures.get('installed_product', {}).get('stderr', b'').strip(): reasons.append('PRODUCT_ABSENCE_UNVERIFIED')
    if not text('android_package').strip().startswith('package:/system/'): reasons.append('PACKAGE_MANAGER_UNVERIFIED')
    for name, header in [('activity', 'ACTIVITY MANAGER'), ('window', 'WINDOW MANAGER')]:
        if header not in text(name) or re.search(SERVICE_ERROR, text(name), re.I):
            reasons.append('SERVICE_RESPONSE_UNVERIFIED:' + name)
    if not re.search(r'(?:RootTask|Task\{)', text('activity')): reasons.append('ACTIVITY_TREE_INCOMPLETE')
    if not complete_window(text('window')): reasons.append('WINDOW_INVENTORY_UNVERIFIED')
    if not window_unfrozen(text('window')): reasons.append('DISPLAY_UNFROZEN_UNVERIFIED')
    for name in ['system_ui', 'system_server']:
        if not re.fullmatch(r'\d+(?:\s+\d+)*', text(name).strip()): reasons.append('PROCESS_UNVERIFIED:' + name)
    try:
        proto = server_proto(values['imms'])
        if proto['system_ready'] is not True: reasons.append('IMMS_NOT_READY')
    except (ValueError, TypeError, UnicodeError): reasons.append('IMMS_RESPONSE_UNVERIFIED')
    unknown, visible = alert_state(text('window')); reasons.extend(unknown); blockers.extend(visible)
    if re.search(r'ANR in |FATAL EXCEPTION|Timeout executing service|Watchdog.*(?:killing|blocked)|BOOT FAILURE', text('logcat'), re.I):
        reasons.append('CRITICAL_ERROR_HISTORY_WITHOUT_RECOVERY_PROOF')
    if ('ACTIVITY MANAGER LAST ANR' not in text('last-anr') or
            '<no ANR has occurred since boot>' not in text('last-anr') or
            re.search(SERVICE_ERROR, text('last-anr'), re.I)):
        reasons.append('LAST_ANR_WITHOUT_RECOVERY_PROOF')
    return {**result(reasons, blockers), 'identity': {'serial': serial, 'user': text('user').strip(),
            'boot_id': text('boot_id').strip()}, 'scope': 'H0_BOUNDED_OBSERVATION_NOT_UNIVERSAL_HEALTH'}


def assess_h1(window_before, hierarchy, window_after, selected_before, selected_after):
    reasons, blockers = [], []
    for window in [window_before, window_after]:
        if not complete_window(window):
            reasons.append('WINDOW_RESPONSE_UNVERIFIED')
        unknown, visible = alert_state(window); reasons.extend(unknown); blockers.extend(visible)
        focuses = re.findall(r'mCurrentFocus=Window\{[^\n}]*\s(\S+/\S+)\}', window)
        allowed = [HOST_COMPONENT, HOST_PACKAGE + '/' + HOST_PACKAGE + '.AcceptanceEditorActivity']
        if len(focuses) != 1 or focuses[0] not in allowed: reasons.append('EDITOR_WINDOW_FOCUS_UNVERIFIED')
    if selected_before.strip() in ['', 'null'] or selected_before.strip() != selected_after.strip(): blockers.append('DEFAULT_IME_CHANGED')
    try:
        start = hierarchy.index('<hierarchy')
        end = hierarchy.index('</hierarchy>') + len('</hierarchy>')
        if '<hierarchy' in hierarchy[end:]: raise ValueError('duplicate hierarchy')
        tree = ET.fromstring(hierarchy[start:end])
        nodes = lambda name: [n for n in tree.iter('node') if n.get('resource-id') == HOST_PACKAGE + ':id/' + name]
        for name, attr, expected in [('case_id', 'text', 'R23-H1'), ('editor', 'focused', 'true'),
                                     ('input_connection_present', 'text', 'true')]:
            found = nodes(name)
            if len(found) != 1 or found[0].get(attr) != expected: reasons.append('EDITOR_EVIDENCE_UNVERIFIED:' + name)
    except (ValueError, ET.ParseError): reasons.append('HIERARCHY_UNVERIFIED')
    return {**result(reasons, blockers), 'scope': 'H1_REAL_HOST_EDITOR_NOT_KUYIN_BINDING'}


def observe_output(command, timeout, observations, completion=None):
    """TEST_ONLY host receive times, not device event times or an atomic trace.

    Preserve stdout/stderr bytes and the existing timeout contract. Only the
    opt-in H1 UI dump uses this; verdict/oracle and command argv stay unchanged.
    """
    buffers = {'stdout': bytearray(), 'stderr': bytearray()}
    eof = {'stdout': False, 'stderr': False}
    completion = {} if completion is None else completion
    def retain(stream, data, *, after_timeout=False):
        observations.append({'stream': stream, 'offset': len(buffers[stream]), 'bytes': len(data),
                             'received_monotonic_ns': time.monotonic_ns(),
                             'received_at_utc': datetime.now(timezone.utc).isoformat(),
                             'drained_after_timeout': after_timeout})
        buffers[stream].extend(data)

    deadline = time.monotonic() + timeout
    with subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE) as proc:
        try:
            with selectors.DefaultSelector() as selector:
                for stream in buffers:
                    selector.register(getattr(proc, stream), selectors.EVENT_READ, stream)
                while selector.get_map():
                    remaining = deadline - time.monotonic()
                    if remaining <= 0:
                        raise subprocess.TimeoutExpired(command, timeout)
                    for key, _ in selector.select(remaining):
                        data = os.read(key.fd, 65536)
                        if not data:
                            eof[key.data] = True
                            selector.unregister(key.fileobj)
                            continue
                        stream = key.data
                        retain(stream, data)
                code = proc.wait(timeout=max(0, deadline - time.monotonic()))
        except subprocess.TimeoutExpired:
            # A direct child may already have exited while a descendant still
            # holds its pipes open. These host observations are not guest exit
            # evidence, and incomplete capture must remain a timeout.
            code_before = proc.poll()
            completion.update(child_pid=proc.pid, child_returncode_before_cleanup=code_before,
                              pipe_eof_observed_before_cleanup=dict(eof),
                              observed_monotonic_ns=time.monotonic_ns(),
                              observed_at_utc=datetime.now(timezone.utc).isoformat(),
                              cleanup_kill_attempted=code_before is None,
                              scope='HOST_DIRECT_CHILD_AND_PIPE_OBSERVATIONS_NOT_ATOMIC_OR_GUEST_EXIT')
            if code_before is None:
                proc.kill()
            proc.wait()
            completion['child_returncode_after_cleanup'] = proc.returncode
            # The reader may have been descheduled while the child wrote its
            # final bytes. Drain already-buffered bytes without waiting/retry;
            # timestamps after timeout remain explicitly distinguished.
            for stream in buffers:
                pipe = getattr(proc, stream)
                os.set_blocking(pipe.fileno(), False)
                while True:
                    try:
                        data = os.read(pipe.fileno(), 65536)
                    except BlockingIOError:
                        break
                    if not data:
                        break
                    retain(stream, data, after_timeout=True)
            raise subprocess.TimeoutExpired(command, timeout, output=bytes(buffers['stdout']),
                                            stderr=bytes(buffers['stderr'])) from None
        completion.update(child_pid=proc.pid, child_returncode_before_cleanup=code,
                          child_returncode_after_cleanup=code, pipe_eof_observed_before_cleanup=dict(eof),
                          observed_monotonic_ns=time.monotonic_ns(),
                          observed_at_utc=datetime.now(timezone.utc).isoformat(), cleanup_kill_attempted=False,
                          scope='HOST_DIRECT_CHILD_AND_PIPE_OBSERVATIONS_NOT_ATOMIC_OR_GUEST_EXIT')
    return subprocess.CompletedProcess(command, code, bytes(buffers['stdout']), bytes(buffers['stderr']))


class Capture:
    def __init__(self, serial, directory):
        self.serial, self.directory = serial, Path(directory)
        self.directory.mkdir(parents=True, exist_ok=False)
        self.records = {}
        self.output_timeline = False

    def run(self, name, args, *, binary=False, timeout=5):
        command = ['adb', '-s', self.serial, *args] if args[0] != 'devices' else ['adb', *args]
        (self.directory / (name + '-command.json')).write_text(json.dumps(command))
        started_at_utc = datetime.now(timezone.utc).isoformat()
        at = time.monotonic_ns()
        observations = []
        completion = {}
        try:
            proc = (observe_output(command, timeout, observations, completion)
                    if self.output_timeline and name.startswith('ui-') else
                    subprocess.run(command, capture_output=True, timeout=timeout))
            rec = {'exit': proc.returncode, 'timed_out': False, 'stdout': proc.stdout, 'stderr': proc.stderr}
        except subprocess.TimeoutExpired as exc:
            rec = {'exit': None, 'timed_out': True, 'stdout': exc.stdout or b'', 'stderr': exc.stderr or b''}
        end = time.monotonic_ns()
        rec.update(started_at_utc=started_at_utc, started_monotonic_ns=at,
                   finished_monotonic_ns=end, duration_ms=round((end - at) / 1_000_000))
        if self.output_timeline and name.startswith('ui-'):
            rec['output_observations'] = observations
            rec['output_time_basis'] = 'HOST_PIPE_RECEIVE_NOT_GUEST_EVENT'
            rec['process_completion'] = completion
        self.records[name] = rec
        for key in ['stdout', 'stderr']:
            (self.directory / (name + '-' + key + ('.bin' if binary and key == 'stdout' else '.txt'))).write_bytes(rec[key])
        if not binary: rec['stdout'] = rec['stdout'].decode(errors='replace')
        return rec

    def save(self, verdict):
        metadata = {name: {k: v for k, v in rec.items() if k not in ['stdout', 'stderr']}
                    for name, rec in self.records.items()}
        (self.directory / 'commands.json').write_text(json.dumps(metadata, indent=2))
        (self.directory / 'result.json').write_text(json.dumps(verdict, indent=2))
        return EXIT[verdict['status']]

    def identity(self):
        for name, args in [('devices', ['devices', '-l']), ('qemu', ['shell', 'getprop', 'ro.kernel.qemu']),
                           ('user', ['shell', 'am', 'get-current-user']),
                           ('boot_id', ['shell', 'cat', '/proc/sys/kernel/random/boot_id'])]:
            self.run(name, args)
        r = self.records
        targets = [s.split()[:2] for s in r['devices']['stdout'].splitlines() if s.strip() and not s.startswith('List of devices')]
        return (all(v['exit'] == 0 and not v['timed_out'] for v in r.values()) and
                re.fullmatch(r'emulator-\d+', self.serial) and targets == [[self.serial, 'device']] and
                r['qemu']['stdout'].strip() == '1' and r['user']['stdout'].strip() == '0')

    def window(self, name, *, timeout=6):
        # API33: windows explicitly dumps all current windows; displays owns focus.
        # A default WMS dump can include a historical last-ANR window snapshot.
        windows = self.run(name + '-windows', ['shell', 'dumpsys', '-t', '2', 'window', 'windows'], timeout=timeout / 2)
        displays = self.run(name + '-displays', ['shell', 'dumpsys', '-t', '2', 'window', 'displays'], timeout=timeout / 2)
        observation = {**windows, 'stdout': windows['stdout'] + '\n' + displays['stdout'],
                       'exit': 0 if windows['exit'] == 0 and displays['exit'] == 0 else 1,
                       'timed_out': windows['timed_out'] or displays['timed_out'],
                       'derived_from': [name + '-windows', name + '-displays'],
                       'atomic_snapshot': False,
                       'finished_monotonic_ns': displays['finished_monotonic_ns'],
                       'duration_ms': round((displays['finished_monotonic_ns'] - windows['started_monotonic_ns']) / 1_000_000)}
        (self.directory / (name + '-observation.txt')).write_text(observation['stdout'])
        self.records[name] = observation
        return observation

    def diagnostics(self):
        self.run('screen', ['exec-out', 'screencap', '-p'], binary=True)
        for key in ['ro.build.fingerprint', 'ro.build.version.sdk', 'ro.product.cpu.abi', 'ro.boot.qemu.avd_name']:
            self.run(key, ['shell', 'getprop', key])
        self.run('uptime', ['shell', 'cat', '/proc/uptime'])

    def environment(self):
        """Allowlisted infrastructure inventory; never dump environment or app data."""
        sdk = Path(os.environ.get('ANDROID_HOME', os.environ.get('ANDROID_SDK_ROOT', '/nonexistent')))
        emulator = sdk / 'emulator/emulator'
        observations = {'runner': {k: os.environ.get(k, 'UNKNOWN') for k in ['RUNNER_OS', 'RUNNER_ARCH', 'ImageOS', 'ImageVersion']},
                        'cpu_count': os.cpu_count(), 'kvm_read_write': os.access('/dev/kvm', os.R_OK | os.W_OK)}
        for name, command in [('os', ['uname', '-a']), ('cpu', ['lscpu']), ('ram', ['free', '-m']),
                              ('emulator_version', [str(emulator), '-version']),
                              ('acceleration', [str(emulator), '-accel-check'])]:
            try:
                p = subprocess.run(command, capture_output=True, timeout=5)
                observations[name] = {'command': command, 'exit': p.returncode,
                                      'stdout': p.stdout.decode(errors='replace'), 'stderr': p.stderr.decode(errors='replace')}
            except (OSError, subprocess.TimeoutExpired) as exc:
                observations[name] = {'command': command, 'error': type(exc).__name__}
        files = [sdk / 'emulator/source.properties', sdk / 'platform-tools/source.properties',
                 sdk / 'system-images/android-33/default/x86_64/source.properties',
                 sdk / 'system-images/android-33/default/x86_64/package.xml', Path('/etc/os-release')]
        avd = self.run('avd-name', ['shell', 'getprop', 'ro.boot.qemu.avd_name'])
        if re.fullmatch(r'[A-Za-z0-9_-]+', avd['stdout'].strip()):
            files.append(Path.home() / '.android/avd' / (avd['stdout'].strip() + '.avd') / 'config.ini')
        observations['files'] = []
        for path in files:
            if path.is_file():
                data = path.read_bytes()
                observations['files'].append({'path': str(path), 'sha256': hashlib.sha256(data).hexdigest(),
                                               'text': data.decode(errors='replace')})
            else: observations['files'].append({'path': str(path), 'status': 'MISSING'})
        if shutil.which('ps'):
            try:
                p = subprocess.run(['ps', '-eo', 'args='], capture_output=True, timeout=5)
                observations['qemu_command_lines'] = [line for line in p.stdout.decode(errors='replace').splitlines()
                                                      if re.search(r'(?:^|/)qemu-system-\S+\s', line.strip())]
                observations['process_inventory_exit'] = p.returncode
            except (OSError, subprocess.TimeoutExpired) as exc:
                observations['process_inventory_error'] = type(exc).__name__
        for name, args in [('guest-kernel', ['uname', '-a']), ('guest-ram', ['cat', '/proc/meminfo']),
                           ('guest-cpu', ['cat', '/proc/cpuinfo']),
                           ('guest-gpu', ['dumpsys', '-t', '3', 'SurfaceFlinger']),
                           ('last-anr', ['dumpsys', '-t', '3', 'activity', 'lastanr'])]:
            self.run(name, ['shell', *args])
        (self.directory / 'environment.json').write_text(json.dumps(observations, indent=2))

    def finish(self, verdict, *, editor=False):
        """Health is usable only with durable diagnostics and unchanged identity."""
        self.diagnostics()
        user = self.run('final-user', ['shell', 'am', 'get-current-user'])
        boot = self.run('final-boot-id', ['shell', 'cat', '/proc/sys/kernel/random/boot_id'])
        window = self.window('final-window')
        reasons = list(verdict['reasons'])
        blockers = ['BLOCKING_SYSTEM_ALERT'] if verdict['status'] == 'UNHEALTHY' else []
        required = ['screen', 'ro.build.fingerprint', 'ro.build.version.sdk', 'ro.product.cpu.abi',
                    'final-user', 'final-boot-id', 'final-window']
        if any(self.records[k]['exit'] != 0 or self.records[k]['timed_out'] for k in required):
            reasons.append('SECONDARY_EVIDENCE_INCOMPLETE')
        if not complete_png(self.records['screen']['stdout']):
            reasons.append('SCREENSHOT_UNVERIFIED')
        if (user['stdout'].strip() != self.records['user']['stdout'].strip() or
                boot['stdout'].strip() != self.records['boot_id']['stdout'].strip()):
            reasons.append('DEVICE_IDENTITY_CHANGED')
        unknown, visible = alert_state(window['stdout'])
        reasons.extend(unknown); blockers.extend(visible)
        if not complete_window(window['stdout']):
            reasons.append('FINAL_WINDOW_UNVERIFIED')
        if editor:
            focus = re.findall(r'mCurrentFocus=Window\{[^\n}]*\s(\S+/\S+)\}', window['stdout'])
            if focus not in [[HOST_COMPONENT], [HOST_PACKAGE + '/' + HOST_PACKAGE + '.AcceptanceEditorActivity']]:
                reasons.append('FINAL_EDITOR_FOCUS_UNVERIFIED')
        else:
            # Two observations only. Never replace an initial failure with recovery.
            verdict = {**verdict, 'readiness': {
                'policy': 'INITIAL_AND_FINAL_UNFROZEN_NO_RETRY',
                'atomic_snapshot': False, 'observations': ['window', 'final-window'],
                'first_assessment': {'status': verdict['status'], 'reasons': list(verdict['reasons'])},
                'time_bounds': {name: {key: self.records.get(name, {}).get(key)
                                     for key in ['started_at_utc', 'started_monotonic_ns', 'finished_monotonic_ns']}
                                for name in ['window', 'final-window']}}}
        return self.save({**verdict, **result(reasons, blockers)})


def collect_h0(serial, directory):
    capture = Capture(serial, directory)
    if not capture.identity(): return capture.save(result(['IDENTITY_UNVERIFIED']))
    capture.environment()
    commands = {'boot': ['getprop', 'sys.boot_completed'], 'bootanim': ['getprop', 'init.svc.bootanim'],
                'android_package': ['pm', 'path', '--user', '0', 'android'],
                'installed_product': ['pm', 'path', '--user', '0', PRODUCT],
                'activity': ['dumpsys', '-t', '3', 'activity', 'activities'],
                'window': ['dumpsys', '-t', '3', 'window'],
                'imms': ['dumpsys', '-t', '3', 'input_method', '--proto'],
                'system_ui': ['pidof', 'com.android.systemui'], 'system_server': ['pidof', 'system_server']}
    for name, args in commands.items():
        if name == 'window': capture.window(name)
        else: capture.run(name, ['shell', *args], binary=name == 'imms')
    capture.run('logcat', ['logcat', '-d', '-v', 'epoch', '-b', 'main', '-b', 'system', '-b', 'crash',
                           '-s', 'ActivityManager', 'AndroidRuntime', 'Watchdog', 'SystemServer', 'SystemUI'])
    verdict = assess_h0(capture.records, serial)
    return capture.finish(verdict)


def collect_h1(serial, directory, h0, *, output_timeline=False):
    capture = Capture(serial, directory)
    capture.output_timeline = output_timeline
    prior = json.loads(Path(h0).read_text())
    if (prior.get('status') != 'HEALTHY' or not capture.identity() or
            prior.get('identity') != {'serial': serial, 'user': '0', 'boot_id': capture.records['boot_id']['stdout'].strip()}):
        return capture.save(result(['H0_OR_DEVICE_IDENTITY_UNVERIFIED']))
    if output_timeline:
        capture.run('clock-before', ['shell', 'date', '+%s.%N'])
        capture.run('uptime-before', ['shell', 'cat', '/proc/uptime'])
        capture.run('overlays-before', ['shell', 'cmd', 'overlay', 'list', '--user', '0'])
    before = capture.run('default-before', ['shell', 'settings', '--user', '0', 'get', 'secure', 'default_input_method'])
    launched = capture.run('launch', ['shell', 'am', 'start', '--user', '0', '-W', '-n', HOST_COMPONENT,
                                     '--es', 'caseId', 'R23-H1', '--ei', 'inputType', '1', '--ei', 'imeOptions', '6'])
    verdict = result(['EDITOR_LAUNCH_UNVERIFIED'])
    if launched['exit'] == 0 and not re.search(r'Error:|Permission Denial|Exception', launched['stdout']):
        deadline = time.monotonic() + 10
        for n in range(40):
            remaining = deadline - time.monotonic()
            if remaining <= 0: break
            w1 = capture.window(f'window-before-{n}', timeout=min(3, remaining))
            remaining = deadline - time.monotonic()
            if remaining <= 0: break
            # DumpCommand writes XML to FileWriter(/dev/tty), not System.out.
            # Noninteractive adb shell defaults to no controlling terminal.
            # Force a PTY for this read only; retain the same timeout/strict oracle.
            xml = capture.run(f'ui-{n}', ['shell', '-tt', 'uiautomator', 'dump', '/dev/tty'], timeout=min(3, remaining))
            remaining = deadline - time.monotonic()
            if remaining <= 0: break
            w2 = capture.window(f'window-after-{n}', timeout=min(3, remaining))
            remaining = deadline - time.monotonic()
            if remaining <= 0: break
            after = capture.run(f'default-after-{n}', ['shell', 'settings', '--user', '0', 'get', 'secure', 'default_input_method'], timeout=min(3, remaining))
            if any(r['exit'] != 0 or r['timed_out'] for r in [before, w1, xml, w2, after]):
                verdict = result(['EDITOR_CAPTURE_UNVERIFIED']); break
            verdict = assess_h1(w1['stdout'], xml['stdout'], w2['stdout'], before['stdout'], after['stdout'])
            if verdict['status'] != 'INCONCLUSIVE': break
            time.sleep(0.25)
    if output_timeline:
        capture.run('clock-after', ['shell', 'date', '+%s.%N'])
        capture.run('uptime-after', ['shell', 'cat', '/proc/uptime'])
        capture.run('overlays-after', ['shell', 'cmd', 'overlay', 'list', '--user', '0'])
        capture.run('framework-log', ['logcat', '-d', '-t', '500', '-v', 'epoch',
                    '-b', 'main', '-b', 'system', '-b', 'crash', '-s', 'WindowManager',
                    'ActivityTaskManager', 'OverlayManager', 'AccessibilityManagerService',
                    'UiAutomation', 'UiAutomationConnection', 'AndroidRuntime', 'SystemUI'])
        capture.run('uiautomator-pid-after', ['shell', 'pidof', 'uiautomator'])
    return capture.finish(verdict, editor=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('stage', choices=['h0', 'h1'])
    parser.add_argument('--serial', required=True)
    parser.add_argument('--out', required=True, type=Path)
    parser.add_argument('--h0', type=Path)
    parser.add_argument('--output-timeline', action='store_true',
                        help='TEST_ONLY timestamp H1 UI-dump bytes as received; same timeout/oracle')
    args = parser.parse_args()
    if os.environ.get('CI') != 'true': parser.error('only disposable CI emulator is authorized')
    if args.stage == 'h1' and args.h0 is None: parser.error('H1 requires durable H0 result')
    return collect_h0(args.serial, args.out) if args.stage == 'h0' else collect_h1(args.serial, args.out, args.h0, output_timeline=args.output_timeline)


if __name__ == '__main__':
    raise SystemExit(main())
