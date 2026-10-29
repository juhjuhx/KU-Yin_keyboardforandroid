"""OFFLINE PREPARATORY SPECIFICATION ONLY. No ADB collector or runtime wiring.

Android 13 evidence currently has no verified finite initializer completion ACK.
The sole accepted protocol below is a synthetic model for reviewing that missing
boundary. It MUST NOT authorize a device, CI stage, or replace the original H0.
"""
import hashlib
import json
import re

from h0_window_observation import SERVICE_ERROR, alert_state, complete_window


def digest(raw):
    return hashlib.sha256(raw.encode("utf-8")).hexdigest()


def assess_readiness(bundle, completion_contract=None):
    """Evaluate immutable sequential observations, never an atomic snapshot."""
    bundle = bundle if isinstance(bundle, dict) else {}
    reasons, blockers = [], []
    original = {}
    original_sha = bundle.get('original_h0_sha256')
    try:
        raw = bundle['original_h0_json']
        if digest(raw) != original_sha: raise ValueError('hash')
        original = json.loads(raw)
        if not isinstance(original, dict): raise ValueError('original schema')
        if original['status'] not in ['HEALTHY', 'INCONCLUSIVE', 'UNHEALTHY']:
            raise ValueError('status')
        original_reasons = original['reasons']
        if (not isinstance(original_reasons, list) or any(not isinstance(r, str) or not r for r in original_reasons) or
                (original['status'] == 'HEALTHY') != (not original_reasons)):
            raise ValueError('status/reasons contradiction')
        identity = original['identity']
        if (not re.fullmatch(r'emulator-\d+', identity['serial']) or identity['user'] != '0' or
                not re.fullmatch(r'[0-9a-f-]{36}', identity['boot_id'])):
            raise ValueError('identity')
    except (KeyError, ValueError, TypeError, AttributeError):
        reasons.append('ORIGINAL_H0_UNVERIFIED')
        original = {}
        identity = None
    if original.get('status') == 'UNHEALTHY': blockers.append('ORIGINAL_H0_UNHEALTHY')
    elif original.get('status') != 'HEALTHY': reasons.append('ORIGINAL_H0_NOT_HEALTHY')

    def finish():
        return {'scope': 'OFFLINE_SPECIFICATION_ONLY',
                'status': 'UNHEALTHY' if blockers else 'INCONCLUSIVE' if reasons else 'HEALTHY',
                'reasons': blockers + reasons, 'first_assessment': original,
                'original_h0_sha256': original_sha, 'atomic_snapshot': False,
                'runtime_authorization': False}

    def capture(rec, label):
        try:
            if not isinstance(rec, dict): raise ValueError('capture schema')
            start, end = rec['host_start_ns'], rec['host_end_ns']
            raw = rec['raw']
            if (not isinstance(rec['argv'], list) or not rec['argv'] or
                    any(not isinstance(arg, str) or not arg for arg in rec['argv']) or
                    type(rec['exit']) is not int or rec['exit'] != 0 or rec['timed_out'] is not False or
                    rec['clock_domain'] != 'HOST_MONOTONIC' or
                    type(start) is not int or type(end) is not int or start < 0 or end < start or
                    not isinstance(raw, str) or digest(raw) != rec['raw_sha256']):
                raise ValueError(label)
            return raw, start, end
        except (KeyError, ValueError, TypeError, AttributeError):
            reasons.append('CAPTURE_UNVERIFIED:' + label)
            return None

    # This deliberate synthetic-only adapter cannot accept Android runtime data.
    contract = completion_contract if isinstance(completion_contract, dict) else {}
    packages = contract.get('expected_packages')
    image_id = bundle.get('image_id')
    if (type(bundle.get('schema')) is not int or bundle.get('schema') != 1 or bundle.get('provenance') != 'SYNTHETIC' or
            contract.get('fixture_only') is not True or
            contract.get('protocol_id') != 'SYNTHETIC-FINITE-INIT-V1' or
            not isinstance(image_id, str) or not image_id.startswith('SYNTHETIC-') or
            image_id != contract.get('image_id') or
            not isinstance(packages, list) or not packages or
            any(not isinstance(p, str) or not p for p in packages) or len(set(packages)) != len(packages)):
        reasons.append('NO_APPROVED_COMPLETION_OBSERVER')
        return finish()
    samples = bundle.get('samples', [])
    if (not isinstance(samples, list) or any(not isinstance(s, dict) for s in samples) or
            [s.get('label') for s in samples] != ['initial', 'final', 'post']):
        reasons.append('SEQUENTIAL_SAMPLE_SET_UNVERIFIED')
        return finish()
    configurations, overlays_seen, spans = [], [], []
    required = ['window', 'activity', 'last_anr', 'logcat', 'overlays', 'boot']
    for sample in samples:
        label = sample['label']
        if identity is None or sample.get('identity') != identity: reasons.append('IDENTITY_CHANGED:' + label)
        values = {}
        captures = sample.get('captures')
        if not isinstance(captures, dict): captures = {}
        for name in required:
            item = capture(captures.get(name, {}), label + ':' + name)
            if item:
                values[name] = item[0]; spans.append((item[1], item[2]))
        window = values.get('window', '')
        if not complete_window(window): reasons.append('WINDOW_UNVERIFIED:' + label)
        unknown, visible = alert_state(window); reasons.extend(unknown); blockers.extend(visible)
        configs = re.findall(r'(?m)^\s*mGlobalConfiguration=(.+)$', window)
        if len(configs) != 1 or not re.fullmatch(r'\{[^{}]+\}', configs[0]): reasons.append('CONFIGURATION_UNVERIFIED:' + label)
        else: configurations.append(digest(configs[0]))
        activity = values.get('activity', '')
        if ('ACTIVITY MANAGER' not in activity or not re.search(r'RootTask|Task\{', activity) or
                re.search(SERVICE_ERROR, activity, re.I)):
            reasons.append('ACTIVITY_UNVERIFIED:' + label)
        anr = values.get('last_anr', '')
        if ('ACTIVITY MANAGER LAST ANR' not in anr or '<no ANR has occurred since boot>' not in anr or
                re.search(SERVICE_ERROR, anr, re.I)):
            reasons.append('ANR_HISTORY_UNVERIFIED:' + label)
        if re.search(r'ANR in |FATAL EXCEPTION|Timeout executing service|Watchdog.*(?:killing|blocked)|BOOT FAILURE',
                     values.get('logcat', ''), re.I): reasons.append('CRITICAL_ERROR_HISTORY:' + label)
        if values.get('boot', '').strip() != '1': reasons.append('BOOT_INCOMPLETE:' + label)
        overlay = values.get('overlays', '')
        overlays_seen.append(overlay)
        if re.search(SERVICE_ERROR, overlay, re.I): reasons.append('OVERLAY_SERVICE_UNVERIFIED:' + label)
        for package in contract.get('expected_packages', []):
            if re.findall(r'(?m)^\[([^]]*)\] ' + re.escape(package) + r'$', overlay) != ['x']:
                reasons.append('PROFILE_OVERLAY_UNVERIFIED:' + package)
    if len(configurations) != 3 or len(set(configurations)) != 1: reasons.append('CONFIGURATION_CHANGED')
    if len(set(overlays_seen)) != 1: reasons.append('OVERLAY_INVENTORY_CHANGED')
    if any(a[1] > b[0] for a, b in zip(spans, spans[1:])): reasons.append('CAPTURE_ORDER_UNVERIFIED')
    budget = contract.get('max_host_span_ns')
    if type(budget) is not int or budget <= 0 or not spans or spans[-1][1] - spans[0][0] > budget:
        reasons.append('OBSERVATION_BOUND_UNVERIFIED')

    completion = capture(bundle.get('completion') or {}, 'completion')
    try:
        if completion is None: raise ValueError('missing')
        events = json.loads(completion[0])
        packages = contract['expected_packages']
        if (not isinstance(packages, list) or not packages or
                any(not isinstance(p, str) or not p for p in packages) or len(set(packages)) != len(packages)): raise ValueError('packages')
        if (not isinstance(events, list) or any(not isinstance(e, dict) for e in events) or
                len(events) != len(packages) + 3): raise ValueError('count')
        begin, propagation, close = events[0], events[-2], events[-1]
        generation = begin['generation']
        if (type(generation) is not int or begin['kind'] != 'begin' or
                begin['protocol_id'] != contract['protocol_id'] or begin['source_sha'] != contract['source_sha'] or
                begin['image_id'] != bundle['image_id'] or begin.get('identity') != identity or
                any(type(e.get('generation')) is not int or e.get('generation') != generation for e in events)):
            raise ValueError('generation/protocol')
        acks = events[1:-2]
        if (any(e.get('kind') != 'overlay_ack' or e.get('success') is not True for e in acks) or
                sorted(e.get('package', '') for e in acks) != sorted(packages) or
                propagation['kind'] != 'propagation_ack' or propagation['pending'] is not False or
                configurations != [propagation['configuration_sha256']] * 3 or
                close['kind'] != 'close' or type(close['operation_count']) is not int or close['operation_count'] != len(packages) or
                not spans or completion[2] > spans[0][0]):
            raise ValueError('unclosed/unpropagated')
    except (KeyError, ValueError, TypeError, IndexError):
        reasons.append('INITIALIZER_COMPLETION_UNVERIFIED')
    return finish()
