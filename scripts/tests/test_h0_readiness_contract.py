"""A-F are explicitly synthetic protocol fixtures; none proves Android completion."""
import copy
import hashlib
import json
from pathlib import Path
import sys
import unittest
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from h0_readiness_contract import assess_readiness
from test_emulator_health import fixture
from emulator_health import assess_h0

PACKAGES = ['com.android.internal.emulation.pixel_7', 'com.android.systemui.emulation.pixel_7']
IDENTITY = {'serial': 'emulator-5554', 'user': '0', 'boot_id': 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'}
WINDOW = ('WINDOW MANAGER WINDOWS\n Window #0 Window{abc u0 launcher/.Main}:\n  isVisible=true\n'
          ' mCurrentFocus=Window{abc u0 launcher/.Main}\n mSystemBooted=true mDisplayEnabled=true\n'
          ' mDisplayFrozen=false windows=0 mRotation=0\n mGlobalConfiguration={fixture-stable-configuration}\n')
CONTRACT = {'protocol_id': 'SYNTHETIC-FINITE-INIT-V1', 'source_sha': '1' * 40,
            'image_id': 'SYNTHETIC-ANDROID13-PIXEL7', 'expected_packages': PACKAGES,
            'max_host_span_ns': 1000, 'fixture_only': True}


def record(name, raw, at):
    return {'argv': ['SYNTHETIC', name], 'exit': 0, 'timed_out': False,
            'clock_domain': 'HOST_MONOTONIC', 'host_start_ns': at, 'host_end_ns': at + 1,
            'raw': raw, 'raw_sha256': hashlib.sha256(raw.encode()).hexdigest()}


def complete_bundle():
    original = json.dumps({'status': 'HEALTHY', 'reasons': [], 'identity': IDENTITY})
    overlays = 'android\n[x] ' + PACKAGES[0] + '\ncom.android.systemui\n[x] ' + PACKAGES[1] + '\n'
    configuration = hashlib.sha256('{fixture-stable-configuration}'.encode()).hexdigest()
    events = [{'kind': 'begin', 'generation': 7, 'protocol_id': CONTRACT['protocol_id'],
               'source_sha': CONTRACT['source_sha'], 'image_id': CONTRACT['image_id'], 'identity': copy.deepcopy(IDENTITY)},
              *[{'kind': 'overlay_ack', 'generation': 7, 'package': p, 'success': True} for p in PACKAGES],
              {'kind': 'propagation_ack', 'generation': 7, 'pending': False, 'configuration_sha256': configuration},
              {'kind': 'close', 'generation': 7, 'operation_count': 2}]
    samples = []
    for i, label in enumerate(['initial', 'final', 'post']):
        raw = {'window': WINDOW, 'activity': 'ACTIVITY MANAGER ACTIVITIES\n RootTask #1\n',
               'last_anr': 'ACTIVITY MANAGER LAST ANR\n <no ANR has occurred since boot>\n',
               'logcat': '', 'overlays': overlays, 'boot': '1\n'}
        samples.append({'label': label, 'identity': copy.deepcopy(IDENTITY),
                        'captures': {k: record(k, v, 100 + i * 100 + n * 3) for n, (k, v) in enumerate(raw.items())}})
    return {'schema': 1, 'provenance': 'SYNTHETIC', 'image_id': CONTRACT['image_id'],
            'original_h0_json': original, 'original_h0_sha256': hashlib.sha256(original.encode()).hexdigest(),
            'completion': record('finite_initializer_ack', json.dumps(events), 80), 'samples': samples}


def change_raw(bundle, index, key, value):
    rec = bundle['samples'][index]['captures'][key]
    rec.update(raw=value, raw_sha256=hashlib.sha256(value.encode()).hexdigest())


class H0ReadinessContractTest(unittest.TestCase):
    def check(self, bundle, expected, contract=CONTRACT):
        before = copy.deepcopy(bundle)
        result = assess_readiness(bundle, contract)
        self.assertIsInstance(result, dict, 'Candidate readiness contract not implemented')
        self.assertEqual(expected, result['status'])
        self.assertEqual('OFFLINE_SPECIFICATION_ONLY', result['scope'])
        self.assertEqual(before, bundle, 'Original evidence must remain unchanged')
        self.assertEqual(bundle['original_h0_sha256'], result['original_h0_sha256'])
        return result

    def test_A_boot_completed_before_profile_initializer_completion_is_inconclusive(self):
        self.assertEqual('HEALTHY', assess_h0(fixture(), 'emulator-5554')['status'])
        b = complete_bundle(); b['completion'] = None
        change_raw(b, 0, 'overlays', b['samples'][0]['captures']['overlays']['raw'].replace('[x]', '[ ]'))
        self.check(b, 'INCONCLUSIVE')

    def test_B_overlay_transition_frozen_and_initial_h0_failure_is_never_erased(self):
        b = complete_bundle(); h = json.loads(b['original_h0_json']); h.update(status='INCONCLUSIVE', reasons=['FINAL_WINDOW_UNVERIFIED'])
        b['original_h0_json'] = json.dumps(h); b['original_h0_sha256'] = hashlib.sha256(b['original_h0_json'].encode()).hexdigest()
        change_raw(b, 0, 'overlays', b['samples'][0]['captures']['overlays']['raw'].replace('[x]', '[ ]'))
        change_raw(b, 1, 'window', WINDOW.replace('mDisplayFrozen=false', 'mDisplayFrozen=true'))
        r = self.check(b, 'INCONCLUSIVE'); self.assertEqual('INCONCLUSIVE', r['first_assessment']['status'])

    def test_original_failure_survives_fully_recovered_subsequent_observations(self):
        for status in ['INCONCLUSIVE', 'UNHEALTHY']:
            with self.subTest(status=status):
                b = complete_bundle()
                h = json.loads(b['original_h0_json'])
                h.update(status=status, reasons=['FIRST_FAILURE_MUST_REMAIN'])
                b['original_h0_json'] = json.dumps(h)
                b['original_h0_sha256'] = hashlib.sha256(b['original_h0_json'].encode()).hexdigest()
                r = self.check(b, status)
                self.assertEqual(h, r['first_assessment'])

    def test_C_original_healthy_post_frozen_is_inconclusive(self):
        b = complete_bundle(); change_raw(b, 2, 'window', WINDOW.replace('mDisplayFrozen=false', 'mDisplayFrozen=true'))
        self.check(b, 'INCONCLUSIVE')

    def test_D_original_healthy_post_missing_is_inconclusive(self):
        b = complete_bundle(); del b['samples'][2]['captures']['window']
        self.check(b, 'INCONCLUSIVE')

    def test_E_original_healthy_post_visible_anr_is_unhealthy(self):
        b = complete_bundle(); change_raw(b, 2, 'window', WINDOW + ' Window #1 Window{def u0 Application Not Responding: com.android.systemui}:\n isVisible=true\n')
        self.check(b, 'UNHEALTHY')

    def test_F_finite_initializer_closed_propagated_and_all_observations_valid(self):
        self.check(complete_bundle(), 'HEALTHY')

    def test_two_enabled_snapshots_and_bootcomplete_are_not_completion_proof(self):
        self.check(complete_bundle(), 'INCONCLUSIVE', None)

    def test_missing_failed_duplicate_or_post_close_initializer_operations_refuse(self):
        for variant in ['missing_ack', 'failed_ack', 'duplicate_ack', 'pending', 'no_close', 'post_close_mutation']:
            with self.subTest(variant=variant):
                b = complete_bundle(); events = json.loads(b['completion']['raw'])
                if variant == 'missing_ack': del events[1]
                elif variant == 'failed_ack': events[1]['success'] = False
                elif variant == 'duplicate_ack': events.insert(2, events[1])
                elif variant == 'pending': events[-2]['pending'] = True
                elif variant == 'no_close': events.pop()
                else: events.append({'kind': 'overlay_ack', 'generation': 7, 'package': PACKAGES[0], 'success': True})
                b['completion'] = record('finite_initializer_ack', json.dumps(events), 80)
                self.check(b, 'INCONCLUSIVE')

    def test_identity_clock_hash_and_failed_commands_fail_closed(self):
        for variant in ['user', 'boot', 'serial', 'clock', 'hash', 'timeout', 'exit', 'interval']:
            with self.subTest(variant=variant):
                b = complete_bundle(); rec = b['samples'][2]['captures']['window']
                if variant == 'user': b['samples'][2]['identity']['user'] = '10'
                elif variant == 'boot': b['samples'][2]['identity']['boot_id'] = 'other'
                elif variant == 'serial': b['samples'][2]['identity']['serial'] = 'emulator-5556'
                elif variant == 'clock': rec['clock_domain'] = 'GUEST_EPOCH'
                elif variant == 'hash': rec['raw_sha256'] = '0' * 64
                elif variant == 'timeout': rec['timed_out'] = True
                elif variant == 'exit': rec['exit'] = 1
                else: rec['host_end_ns'] = rec['host_start_ns'] - 1
                self.check(b, 'INCONCLUSIVE')

    def test_initializer_ack_identity_generation_protocol_and_time_are_bound(self):
        for variant in ['ack_user', 'ack_boot', 'ack_serial', 'late_ack', 'generation',
                        'source', 'image', 'config_ack', 'missing_propagation', 'order', 'span']:
            with self.subTest(variant=variant):
                b = complete_bundle(); events = json.loads(b['completion']['raw'])
                if variant.startswith('ack_'): events[0]['identity'][variant[4:] if variant != 'ack_boot' else 'boot_id'] = 'other'
                elif variant == 'generation': events[1]['generation'] = 8
                elif variant == 'source': events[0]['source_sha'] = '2' * 40
                elif variant == 'image': events[0]['image_id'] = 'other'
                elif variant == 'config_ack': events[-2]['configuration_sha256'] = '0' * 64
                elif variant == 'missing_propagation': del events[-2]
                elif variant == 'order': b['samples'][1]['captures']['window']['host_start_ns'] = 1
                elif variant == 'span': b['samples'][2]['captures']['boot']['host_end_ns'] = 2000
                b['completion'] = record('finite_initializer_ack', json.dumps(events), 900 if variant == 'late_ack' else 80)
                self.check(b, 'INCONCLUSIVE')

    def test_synthetic_protocol_is_never_accepted_as_real_android_evidence(self):
        b = complete_bundle(); b['provenance'] = 'ANDROID_RUNTIME'
        self.check(b, 'INCONCLUSIVE')

    def test_contract_container_types_and_integer_protocol_fields_are_strict(self):
        for variant in ['image_number', 'packages_number', 'packages_null', 'package_null', 'ack_float', 'count_float']:
            b = complete_bundle(); contract = copy.deepcopy(CONTRACT)
            if variant == 'image_number': b['image_id'] = 7
            elif variant == 'packages_number': contract['expected_packages'] = 7
            elif variant == 'packages_null': contract['expected_packages'] = None
            elif variant == 'package_null': contract['expected_packages'] = [None]
            else:
                events = json.loads(b['completion']['raw'])
                if variant == 'ack_float': events[1]['generation'] = 7.0
                else: events[-1]['operation_count'] = 2.0
                b['completion'] = record('finite_initializer_ack', json.dumps(events), 80)
            self.check(b, 'INCONCLUSIVE', contract)

    def test_original_h0_status_reasons_must_be_consistent(self):
        for status, reasons in [('HEALTHY', ['FINAL_WINDOW_UNVERIFIED']), ('HEALTHY', None),
                                ('INCONCLUSIVE', []), ('UNHEALTHY', [])]:
            b = complete_bundle(); h = json.loads(b['original_h0_json'])
            h.update(status=status, reasons=reasons)
            b['original_h0_json'] = json.dumps(h)
            b['original_h0_sha256'] = hashlib.sha256(b['original_h0_json'].encode()).hexdigest()
            self.check(b, 'INCONCLUSIVE')

    def test_malformed_or_unknown_evidence_is_structured_inconclusive(self):
        for variant in ['argv', 'exit_bool', 'exit_float', 'schema_bool', 'overlay_service_error',
                        'unknown_config', 'sample_null', 'captures_null', 'original_null',
                        'original_list', 'event_null']:
            with self.subTest(variant=variant):
                b = complete_bundle()
                rec = b['samples'][0]['captures']['window']
                if variant == 'argv': rec['argv'] = 'not-an-argv-list'
                elif variant == 'exit_bool': rec['exit'] = False
                elif variant == 'exit_float': rec['exit'] = 0.0
                elif variant == 'schema_bool': b['schema'] = True
                elif variant == 'overlay_service_error':
                    for i in range(3): change_raw(b, i, 'overlays', b['samples'][i]['captures']['overlays']['raw'] + 'DUMP TIMEOUT')
                elif variant == 'unknown_config':
                    for i in range(3): change_raw(b, i, 'window', WINDOW.replace('{fixture-stable-configuration}', 'unknown'))
                    events = json.loads(b['completion']['raw']); events[-2]['configuration_sha256'] = hashlib.sha256(b'unknown').hexdigest()
                    b['completion'] = record('finite_initializer_ack', json.dumps(events), 80)
                elif variant == 'sample_null': b['samples'][0] = None
                elif variant == 'captures_null': b['samples'][0]['captures'] = None
                elif variant.startswith('original_'):
                    b['original_h0_json'] = 'null' if variant == 'original_null' else '[]'
                    b['original_h0_sha256'] = hashlib.sha256(b['original_h0_json'].encode()).hexdigest()
                else:
                    events = json.loads(b['completion']['raw']); events[1] = None
                    b['completion'] = record('finite_initializer_ack', json.dumps(events), 80)
                self.check(b, 'INCONCLUSIVE')

    def test_configuration_or_overlay_mutation_after_ack_is_not_stable(self):
        for key, raw in [('window', WINDOW.replace('fixture-stable-configuration', 'new-configuration')),
                         ('overlays', 'android\n[x] ' + PACKAGES[0] + '\ncom.android.systemui\n[ ] ' + PACKAGES[1] + '\n')]:
            b = complete_bundle(); change_raw(b, 2, key, raw)
            self.check(b, 'INCONCLUSIVE')
