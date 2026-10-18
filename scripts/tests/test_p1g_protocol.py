"""Explicit canonical identities and malformed Android instrumentation transcripts."""
import copy
import inspect
import json
import re
import shutil
import tempfile
from pathlib import Path
import unittest
from test_p1_acceptance_device import device

MANIFEST_PATH = Path(__file__).resolve().parents[1] / 'p1_instrumentation_manifest.json'
MANIFEST = json.loads(MANIFEST_PATH.read_text())
HOST = MANIFEST['host']['tests']
FIXTURES = Path(__file__).resolve().parent / 'fixtures'
FROZEN_SOURCE_ROOT = FIXTURES / 'p1e-frozen-instrumentation'


def assert_frozen_sources(test, root):
    """Verify the exact historical bytes, never the evolving working-tree sources."""
    for relative, digest in MANIFEST['source_sha256'].items():
        source = root / relative
        test.assertTrue(source.is_file(), f'missing frozen source: {relative}')
        test.assertEqual(digest, device.sha256(source), relative)


def current_test_identities(root):
    """Fail closed if the explicitly inventoried Kotlin test syntax changes."""
    identities = []
    for source in sorted(root.rglob('*.kt')):
        text = source.read_text()
        annotations = re.findall(r'@Test\b', text)
        if not annotations:
            continue
        packages = re.findall(r'^package\s+(\S+)\s*$', text, re.M)
        owners = re.findall(r'^class\s+(\w+)\b', text, re.M)
        methods = re.findall(r'@Test\s+fun\s+(\w+)\s*\(', text)
        if len(packages) != 1 or len(owners) != 1 or len(methods) != len(annotations):
            raise AssertionError(f'unrecognized instrumentation source structure: {source}')
        identities.extend(f'{packages[0]}.{owners[0]}#{method}' for method in methods)
    return identities

def event(case, **extra):
    return {'case_id':case, 'result':'PASS', 'scope':'synthetic protocol only',
            'evidence':{'fixed':'fixture'}, **extra}

def transcript(tests=None, *, statuses=None, emits=None, count=None, terminal=True):
    tests = HOST if tests is None else tests
    lines=[]
    count=len(tests) if count is None else count
    statuses=[0]*len(tests) if statuses is None else statuses
    for index, test in enumerate(tests):
        klass, method=test['identity'].split('#')
        header=[f'INSTRUMENTATION_STATUS: class={klass}', f'INSTRUMENTATION_STATUS: test={method}',
                f'INSTRUMENTATION_STATUS: numtests={count}']
        lines += header+['INSTRUMENTATION_STATUS_CODE: 1']
        observations=[event(item['case_id']) for item in test['cases']] if emits is None else emits[index]
        for value in observations:
            lines += ['INSTRUMENTATION_STATUS: stream='+device.CASE_PREFIX+json.dumps(value),
                      'INSTRUMENTATION_STATUS_CODE: 2']
        lines += header+[f'INSTRUMENTATION_STATUS_CODE: {statuses[index]}']
    if terminal:
        lines += [f'INSTRUMENTATION_RESULT: stream=OK ({len(tests)-statuses.count(-3)} tests)',
                  'INSTRUMENTATION_CODE: -1']
    return '\n'.join(lines)

def parsed(output, tests=None, *, count=None):
    tests=HOST if tests is None else tests
    # The old runner lacked a contract parameter; bypassing it here demonstrates
    # semantic RED rather than failing because an API/import does not exist.
    kwargs={'expected_tests':len(tests) if count is None else count}
    parameters=inspect.signature(device.parse_instrumentation).parameters
    if 'expected_identities' in parameters:
        kwargs['expected_identities']=[item['identity'] for item in tests]
        kwargs['allow_skips']=True
    if 'case_contract' in parameters:
        kwargs['case_contract']={item['identity']:item['cases'] for item in tests}
    return device.parse_instrumentation(output, **kwargs)

class CountTests(unittest.TestCase):
    def test_COUNT_01_all_26_expected_identities(self):
        self.assertEqual('PASS',parsed(transcript())['result'])
    def test_COUNT_02_25_completed_is_not_26(self):
        self.assertEqual('FAIL',parsed(transcript(HOST[:-1]))['result'])
    def test_COUNT_03_27_with_unexpected_test(self):
        other={'identity':'unexpected.Test#method','cases':[]}
        self.assertEqual('FAIL',parsed(transcript(HOST+[other]))['result'])
    def test_COUNT_04_substitution_cannot_hide_missing_identity(self):
        tests=copy.deepcopy(HOST);tests[-1]['identity']='unexpected.Test#method'
        self.assertEqual('FAIL',parsed(transcript(tests))['result'])
    def test_COUNT_05_duplicate_hides_missing_identity(self):
        tests=copy.deepcopy(HOST);tests[-1]=tests[0]
        self.assertEqual('FAIL',parsed(transcript(tests))['result'])
    def test_COUNT_06_skip_is_not_pass(self):
        self.assertNotEqual('PASS',parsed(transcript(statuses=[0]*25+[-4]))['result'])
    def test_COUNT_07_failure_is_not_pass(self):
        self.assertEqual('FAIL',parsed(transcript(statuses=[0]*25+[-2]))['result'])
    def test_COUNT_08_inconsistent_terminal_count(self):
        self.assertEqual('FAIL',parsed(transcript().replace('OK (26 tests)','OK (25 tests)'))['result'])

class ProtocolTests(unittest.TestCase):
    def test_PROTO_01_complete_sequence(self):
        self.assertEqual('PASS',parsed(transcript())['result'])
    def test_PROTO_02_finish_without_start(self):
        lines=transcript().splitlines(); del lines[:4]
        self.assertEqual('FAIL',parsed('\n'.join(lines))['result'])
    def test_PROTO_03_start_without_finish(self):
        orphan='INSTRUMENTATION_STATUS: class=orphan.Test\nINSTRUMENTATION_STATUS: test=orphan\nINSTRUMENTATION_STATUS_CODE: 1\n'
        self.assertEqual('FAIL',parsed(orphan+transcript())['result'])
    def test_PROTO_04_case_wrong_owner(self):
        emits=[[event(item['cases'][0]['case_id'])] for item in HOST]; emits[0]=[event('D01')]
        self.assertEqual('FAIL',parsed(transcript(emits=emits))['result'])
    def test_PROTO_05_duplicate_case_or_subcase(self):
        emits=[[event(item['cases'][0]['case_id'])] for item in HOST]; emits[0]*=2
        self.assertEqual('FAIL',parsed(transcript(emits=emits))['result'])
    def test_PROTO_06_missing_required_case_is_not_tested(self):
        emits=[[event(item['cases'][0]['case_id'])] for item in HOST]; emits[0]=[]
        result=parsed(transcript(emits=emits))
        self.assertNotEqual('PASS',result['result'])
        self.assertEqual('NOT_TESTED',device.summarize_cases(result['case_emissions'])['B01']['observable_result'])
    def test_PROTO_07_unknown_case(self):
        emits=[[event(item['cases'][0]['case_id'])] for item in HOST]; emits[0]=[event('X99')]
        self.assertEqual('FAIL',parsed(transcript(emits=emits))['result'])
    def test_PROTO_08_no_terminal_result(self):
        self.assertEqual('FAIL',parsed(transcript(terminal=False))['result'])
    def test_PROTO_09_junit_failure_overrides_case_pass(self):
        result=parsed(transcript(statuses=[-2]+[0]*25))
        self.assertEqual('FAIL',result['result'])
        self.assertEqual('FAIL',device.summarize_cases(result['case_emissions'])['B01']['observable_result'])
    def test_PROTO_10_multiple_declared_subcases_only(self):
        tests=[{'identity':'example.Multi#observations','cases':[{'case_id':'D18','subcase_id':'one'}, {'case_id':'D18','subcase_id':'two'}]}]
        output=transcript(tests,emits=[[event('D18',subcase_id='one'), event('D18',subcase_id='two')]])
        self.assertEqual('PASS',parsed(output,tests)['result'])
        extra=output.replace('subcase_id": "two"','subcase_id": "three"')
        self.assertEqual('FAIL',parsed(extra,tests)['result'])
    def test_PROTO_11_skip_never_case_pass(self):
        result=parsed(transcript(statuses=[-4]+[0]*25))
        self.assertNotEqual('PASS',result['result'])
        self.assertNotEqual('PASS',device.summarize_cases(result['case_emissions'])['B01']['observable_result'])
    def test_PROTO_12_observable_does_not_waive_full_scope(self):
        summary=device.summarize_cases(parsed(transcript())['case_emissions'])
        for case in ('C01','D17','B01'):
            self.assertEqual('PASS',summary[case]['observable_result'])
            self.assertEqual('INCONCLUSIVE',summary[case]['full_acceptance_result'])
    def test_emit_outside_active_test_is_rejected(self):
        extra='\n'+device.CASE_PREFIX+json.dumps(event('D01'))
        self.assertEqual('FAIL',parsed(transcript()+extra)['result'])
    def test_duplicate_finish_is_rejected(self):
        text=transcript(); first=text.split('INSTRUMENTATION_STATUS_CODE: 0',1)[0]
        finish='\n'.join(first.splitlines()[-3:])+'\nINSTRUMENTATION_STATUS_CODE: 0\n'
        self.assertEqual('FAIL',parsed(text.replace('INSTRUMENTATION_STATUS_CODE: 0','INSTRUMENTATION_STATUS_CODE: 0\n'+finish,1))['result'])

class RuntimeWiringTests(unittest.TestCase):
    def test_exact_app5_then_host26_is_partial_and_restored(self):
        from p1g_fakes import Session
        with Session() as s:
            s.fake.instrumentation_outputs={device.APP_RUNNER:transcript(MANIFEST['app']['tests']),device.HOST_RUNNER:transcript()}
            s.execute('prepare'); s.execute('select')
            self.assertEqual(3,s.execute('run'))
            summary=json.loads(next(s.evidence.glob('run-*/run-summary.json')).read_text())
            self.assertEqual([5,26],[suite['completed_tests'] for suite in summary['suites']])
            self.assertEqual('PASS',summary['restore']['result'])
            self.assertFalse(summary['full_acceptance_complete'])
            self.assertEqual('NOT_TESTED',summary['cases']['A04']['full_acceptance_result'])
    def test_substituted_app_identity_stops_before_host(self):
        from p1g_fakes import Session
        tests=copy.deepcopy(MANIFEST['app']['tests']);tests[0]['identity']='wrong.Test#method'
        with Session() as s:
            s.fake.instrumentation_outputs={device.APP_RUNNER:transcript(tests),device.HOST_RUNNER:transcript()}
            s.execute('prepare'); s.execute('select')
            self.assertEqual(2,s.execute('run'))
            instruments=[a for a in s.fake.mutations if a[3:6]==['shell','am','instrument']]
            self.assertEqual([device.APP_RUNNER],[a[-1] for a in instruments])
            self.assertEqual('original.ime/.Service',s.fake.states[0][0].strip())

class ManifestIntegrityTests(unittest.TestCase):
    def test_native_case4_owner_is_test_class_not_private_delegate(self):
        identities={item['identity'] for item in device.instrumentation_manifest()['app']['tests']}
        self.assertIn('com.example.androidkeyboard.runtime.NativeCase4Test#selectionCommitsSegmentAndNextKeyContinuesSameContext',identities)
        self.assertNotIn('com.example.androidkeyboard.runtime.ResetCountingEngine#selectionCommitsSegmentAndNextKeyContinuesSameContext',identities)
    def test_manifest_change_after_prepare_blocks_install(self):
        import tempfile
        from unittest.mock import patch
        from p1g_fakes import Session
        with tempfile.TemporaryDirectory() as tmp:
            path=Path(tmp)/'manifest.json';path.write_text(MANIFEST_PATH.read_text())
            with patch.object(device,'PROTOCOL_MANIFEST',path), Session() as s:
                s.execute('prepare')
                data=json.loads(path.read_text());data['app']['tests'][0]['identity']='changed.Test#method';path.write_text(json.dumps(data))
                with self.assertRaises(device.RunnerError):
                    s.execute('install')
                self.assertEqual([],s.fake.mutations)
    def test_source_hashes_match_frozen_instrumentation_sources(self):
        assert_frozen_sources(self, FROZEN_SOURCE_ROOT)

    def test_missing_frozen_source_is_failure_not_skip(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp) / 'frozen'
            shutil.copytree(FROZEN_SOURCE_ROOT, root)
            (root / next(iter(MANIFEST['source_sha256']))).unlink()
            with self.assertRaisesRegex(AssertionError, 'missing frozen source'):
                assert_frozen_sources(self, root)

    def test_tampered_frozen_source_is_failure(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp) / 'frozen'
            shutil.copytree(FROZEN_SOURCE_ROOT, root)
            source = root / next(iter(MANIFEST['source_sha256']))
            source.write_bytes(source.read_bytes() + b'\n// tampered\n')
            with self.assertRaises(AssertionError):
                assert_frozen_sources(self, root)

    def test_current_instrumentation_has_exact_five_and_thirty_three_identities(self):
        contract = json.loads((FIXTURES / 'current_instrumentation_identities.json').read_text())
        for module, count in (('app', 5), ('acceptance-host', 33)):
            with self.subTest(module=module):
                expected = contract[module]['identities']
                actual = current_test_identities(MANIFEST_PATH.parents[1] / module / 'src/androidTest')
                self.assertEqual(count, contract[module]['expected_count'])
                self.assertEqual(count, len(expected))
                self.assertEqual(count, len(set(expected)))
                self.assertEqual(count, len(actual))
                self.assertEqual(count, len(set(actual)))
                self.assertEqual(set(expected), set(actual))
        self.assertTrue({t['identity'] for t in HOST}.issubset(contract['acceptance-host']['identities']))

    def test_current_identity_reader_rejects_unsupported_annotation_shape(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / 'Test.kt').write_text('package example\nclass SampleTest {\n@Test(timeout=100) fun method() {}\n}\n')
            with self.assertRaisesRegex(AssertionError, 'unrecognized'):
                current_test_identities(root)

class MalformedWireTests(unittest.TestCase):
    def test_malformed_control_lines_are_not_ignored(self):
        for line in ('INSTRUMENTATION_STATUS_CODE: START','INSTRUMENTATION_STATUS_CODE: -2garbage','INSTRUMENTATION_CODE: broken'):
            with self.subTest(line=line):
                self.assertEqual('FAIL',parsed(line+'\n'+transcript())['result'])
    def test_duplicate_identity_field_is_not_overwritten(self):
        output=transcript().replace('INSTRUMENTATION_STATUS: class=', 'INSTRUMENTATION_STATUS: class=wrong.Class\nINSTRUMENTATION_STATUS: class=',1)
        self.assertEqual('FAIL',parsed(output)['result'])

    def test_missing_colon_and_empty_key_are_rejected(self):
        for line in ('INSTRUMENTATION_STATUS_CODE -2','INSTRUMENTATION_CODE broken','INSTRUMENTATION_STATUS: =emptykey'):
            with self.subTest(line=line):
                self.assertEqual('FAIL',parsed(line+'\n'+transcript())['result'])

class CaseBundleTests(unittest.TestCase):
    def test_case_requires_send_status_two_bundle(self):
        output=transcript().replace('INSTRUMENTATION_STATUS_CODE: 2\n','')
        self.assertEqual('FAIL',parsed(output)['result'])
    def test_arbitrary_stdout_is_not_case_protocol(self):
        output=transcript().replace('INSTRUMENTATION_STATUS: stream='+device.CASE_PREFIX,'ARBITRARY_STDOUT '+device.CASE_PREFIX)
        self.assertEqual('FAIL',parsed(output)['result'])

class CanonicalManifestTests(unittest.TestCase):
    def test_pre_prepare_manifest_substitution_is_rejected(self):
        import tempfile
        from unittest.mock import patch
        bad=copy.deepcopy(MANIFEST)
        bad['fixture_source_sha']='arbitrary';bad['source_sha256']={}
        for layer in ('app','host'):
            for index,item in enumerate(bad[layer]['tests']):
                item['identity']=f'wrong.{layer}Test#method{index}'
        with tempfile.TemporaryDirectory() as tmp:
            path=Path(tmp)/'substitution.json';path.write_text(json.dumps(bad))
            with patch.object(device,'PROTOCOL_MANIFEST',path), self.assertRaises(device.RunnerError):
                device.instrumentation_manifest()

class PayloadSchemaTests(unittest.TestCase):
    def test_duplicate_json_cannot_hide_fail_or_wrong_owner(self):
        source=transcript()
        for original,replacement in (
            ('"result": "PASS"','"result":"FAIL","result":"PASS"'),
            ('"case_id": "B01"','"case_id":"D01","case_id":"B01"'),
        ):
            with self.subTest(replacement=replacement):
                self.assertEqual('FAIL',parsed(source.replace(original,replacement,1))['result'])
    def test_empty_terminal_result_key_is_rejected(self):
        self.assertEqual('FAIL',parsed('INSTRUMENTATION_RESULT: =emptykey\n'+transcript())['result'])
    def test_wrong_status_bundle_identity_cannot_leave_case_pass(self):
        text=transcript().replace('INSTRUMENTATION_STATUS_CODE: 2','INSTRUMENTATION_STATUS: class=wrong.Class\nINSTRUMENTATION_STATUS: test=wrong\nINSTRUMENTATION_STATUS_CODE: 2',1)
        result=parsed(text)
        self.assertEqual('FAIL',result['result'])
        self.assertEqual('FAIL',device.summarize_cases(result['case_emissions'])['B01']['observable_result'])
