import importlib.util
import json
import hashlib
from pathlib import Path
import re
import sys
import unittest

SCRIPTS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SCRIPTS))
import r2_ci_protocol as adapter


def stream(layer='host'):
    tests = json.loads(adapter.MANIFEST.read_text())[layer]['tests']
    lines = []
    for index, test in enumerate(tests, 1):
        cls, name = test['identity'].split('#')
        identity = [f'INSTRUMENTATION_STATUS: class={cls}', f'INSTRUMENTATION_STATUS: test={name}',
                    f'INSTRUMENTATION_STATUS: numtests={len(tests)}', f'INSTRUMENTATION_STATUS: current={index}']
        lines += identity + ['INSTRUMENTATION_STATUS_CODE: 1']
        for case in test['cases']:
            event = dict(case, result='INCONCLUSIVE' if case['case_id'] == 'B02' else 'PASS',
                         scope='SYNTHETIC_PROTOCOL_UNIT_ONLY', evidence={'synthetic': True})
            lines += ['INSTRUMENTATION_STATUS: stream=P1_ACCEPTANCE_CASE=' + json.dumps(event),
                      'INSTRUMENTATION_STATUS_CODE: 2']
        lines += identity + ['INSTRUMENTATION_STATUS_CODE: 0']
    lines += [f'INSTRUMENTATION_RESULT: stream=OK ({len(tests)} tests)', 'INSTRUMENTATION_CODE: -1']
    return '\n'.join(lines)


class CurrentProtocolTest(unittest.TestCase):
    def test_current_33_test_32_case_contract_is_not_frozen_26(self):
        result = adapter.evaluate(stream(), 'host')
        self.assertEqual([], result['errors'])
        self.assertEqual(33, result['completed_tests'])
        self.assertEqual(32, len(result['case_emissions']))
        self.assertEqual('INCONCLUSIVE', result['result'])
        self.assertEqual([], result['missing_required_cases'])

    def test_proto_rejects_empty_text_corrupt_and_rootless(self):
        for raw in [b'', b'Permission Denial', b'\x1a\xff', b'\x0a\x03ime']:
            with self.subTest(raw=raw):
                with self.assertRaises(ValueError): adapter.server_proto(raw)

    def test_proto_distinguishes_missing_bool_from_observed_false(self):
        snapshot = adapter.server_proto(b'\x1a\x05\x0a\x03ime')
        self.assertEqual('ime', snapshot['cur_method_id'])
        self.assertIsNone(snapshot['bound_to_method'])
        self.assertNotIn('remote_method_present', snapshot)
        self.assertNotIn('enabled_session', snapshot)
        snapshot = adapter.server_proto(b'\x1a\x08\x0a\x03ime\x98\x01\x00')
        self.assertIs(False, snapshot['bound_to_method'])

    def test_frozen_parser_case_vocabulary_is_unchanged(self):
        import p1_acceptance_device as frozen
        self.assertNotIn('P0-ZH01', frozen.CASE_IDS)
        self.assertEqual(26, frozen.EXPECTED_HOST_TESTS)

    def test_app_requires_all_five_and_no_skips(self):
        text = stream('app')
        self.assertEqual('PASS', adapter.evaluate(text, 'app')['result'])
        self.assertEqual('FAIL', adapter.evaluate(text.replace('STATUS_CODE: 0', 'STATUS_CODE: -4', 1), 'app')['result'])
        self.assertEqual('FAIL', adapter.evaluate(text.replace('numtests=5', 'numtests=4'), 'app')['result'])

    def test_truncated_terminal_and_failed_junit_never_pass(self):
        text = stream()
        self.assertEqual('FAIL', adapter.evaluate(text.rsplit('INSTRUMENTATION_CODE:', 1)[0], 'host')['result'])
        self.assertEqual('FAIL', adapter.evaluate(text.replace('STATUS_CODE: 0', 'STATUS_CODE: -2', 1), 'host')['result'])

    def test_wrong_owner_duplicate_and_missing_case_never_pass(self):
        text = stream()
        self.assertEqual('FAIL', adapter.evaluate(text.replace('P0-EN02', 'B01'), 'host')['result'])
        case = next(line for line in text.splitlines() if 'P1_ACCEPTANCE_CASE=' in line)
        self.assertEqual('FAIL', adapter.evaluate(text.replace(case, case+'\nINSTRUMENTATION_STATUS_CODE: 2\n'+case, 1), 'host')['result'])
        self.assertEqual('INCONCLUSIVE', adapter.evaluate(text.replace(case+'\nINSTRUMENTATION_STATUS_CODE: 2\n', '', 1), 'host')['result'])

    def test_canonical_identities_and_source_hashes_match_actual_tests(self):
        manifest = json.loads(adapter.MANIFEST.read_text())
        for path, expected in manifest['source_files'].items():
            self.assertEqual(expected, hashlib.sha256((SCRIPTS.parent/path).read_bytes()).hexdigest())
        for layer, directory in [('app', 'app/src/androidTest'), ('host', 'acceptance-host/src/androidTest')]:
            identities = set()
            for path in (SCRIPTS.parent/directory).rglob('*.kt'):
                text = path.read_text()
                package = re.search(r'^package (\S+)', text, re.M).group(1)
                identities.update(package+'.'+path.stem+'#'+name for name in re.findall(r'@Test\s+fun\s+(\w+)\s*\(', text))
            self.assertEqual(identities, {test['identity'] for test in manifest[layer]['tests']})
            self.assertEqual(manifest[layer]['expected_count'], len(identities))


if __name__ == '__main__':
    unittest.main()
