"""TEST_ONLY one disposable health observation; no install, selection or H1/H2."""
import argparse
import hashlib
import json
from pathlib import Path
import emulator_health as health


def run_health(serial, directory):
    directory = Path(directory)
    observer = health.Capture(serial, directory / 'observer')
    if not observer.identity():
        return observer.save(health.result(['IDENTITY_UNVERIFIED']))
    identity = {'serial': serial, 'user': observer.records['user']['stdout'].strip(),
                'boot_id': observer.records['boot_id']['stdout'].strip()}
    for label, args in [('clock-before', ['date', '+%s.%N']),
                        ('uptime-before', ['cat', '/proc/uptime']),
                        ('overlays-before', ['cmd', 'overlay', 'list', '--user', '0'])]:
        observer.run(label, ['shell', *args])
    # Exactly one unchanged H0 oracle; no retry or replacement with later recovery.
    code = health.collect_h0(serial, directory / 'h0')
    first = json.loads((directory / 'h0/result.json').read_text())
    for label, args in [('clock-after', ['date', '+%s.%N']),
                        ('uptime-after', ['cat', '/proc/uptime']),
                        ('overlays-after', ['cmd', 'overlay', 'list', '--user', '0']),
                        ('final-user', ['am', 'get-current-user']),
                        ('final-boot-id', ['cat', '/proc/sys/kernel/random/boot_id'])]:
        observer.run(label, ['shell', *args])
    observer.run('guest-events', ['logcat', '-d', '-t', '4000', '-v', 'epoch', '-b', 'events'])
    observer.run('guest-framework', ['logcat', '-d', '-t', '4000', '-v', 'epoch',
                 '-b', 'main', '-b', 'system', '-b', 'crash', '-s', 'WindowManager',
                 'ActivityTaskManager', 'ActivityManager', 'DisplayManagerService',
                 'OverlayManager', 'ScreenDecorations', 'SystemUI', 'AndroidRuntime', 'Watchdog'])
    observer.window('post-health-window')
    reasons = list(first['reasons'])
    if first['status'] != 'HEALTHY' and not reasons:
        reasons.append('ORIGINAL_H0_NOT_HEALTHY')
    if any(v['exit'] != 0 or v['timed_out'] for v in observer.records.values()):
        reasons.append('SUPPLEMENTAL_OBSERVATION_INCOMPLETE')
    if (observer.records['final-user']['stdout'].strip() != identity['user'] or
            observer.records['final-boot-id']['stdout'].strip() != identity['boot_id'] or
            first.get('identity') != identity):
        reasons.append('DEVICE_IDENTITY_CHANGED')
    verdict = {**health.result(reasons, ['ORIGINAL_H0_UNHEALTHY'] if first['status'] == 'UNHEALTHY' else []),
               'identity': identity, 'original_h0_status': first['status'], 'original_h0_exit': code,
               'original_result_sha256': hashlib.sha256((directory / 'h0/result.json').read_bytes()).hexdigest(),
               'scope': 'ONE_HEALTH_ONLY_EXPERIMENT_NO_INSTALL_NO_H1_H2',
               'atomic_snapshot': False, 'causal_verdict': 'UNRESOLVED',
               'post_observation_policy': 'SUPPLEMENTAL_ONLY_NEVER_REPLACE_FIRST_FAILURE',
               'clock_contract': 'HOST_COMMAND_BOUNDS; guest date/uptime and log epochs separate; no atomic clock alignment'}
    return observer.save(verdict)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--serial', required=True)
    parser.add_argument('--out', required=True)
    args = parser.parse_args()
    raise SystemExit(run_health(args.serial, args.out))
