"""Current R2 CI adapter; reuses the frozen runner's strict protocol state machine.

No ADB, installation, dictionary access or mutation of the frozen manifest.
The current source-owned case vocabulary differs from frozen P1-E.
"""
import argparse
import json
import importlib.util
import sys
from pathlib import Path
# Private module instance: current case IDs cannot alter the frozen P1 runner.
_spec = importlib.util.spec_from_file_location('_kuyin_r2_protocol_engine', Path(__file__).with_name('p1_acceptance_device.py'))
protocol = importlib.util.module_from_spec(_spec)
sys.modules[_spec.name] = protocol
_spec.loader.exec_module(protocol)

MANIFEST = Path(__file__).with_name('r2_current_instrumentation_manifest.json')
_current = json.loads(MANIFEST.read_text())
protocol.CASE_IDS = tuple(sorted({case['case_id'] for layer in ('app', 'host')
    for test in _current[layer]['tests'] for case in test['cases']}))


def server_proto(data):
    """API33 server-only trace envelope; no client dump, no remote/session field."""
    def fields(raw):
        offset = 0
        result = {}
        def varint():
            nonlocal offset
            value = 0
            for shift in range(0, 70, 7):
                if offset >= len(raw): raise ValueError('truncated varint')
                byte = raw[offset]; offset += 1
                value |= (byte & 127) << shift
                if byte < 128: return value
            raise ValueError('oversized varint')
        while offset < len(raw):
            tag = varint(); number, wire = tag >> 3, tag & 7
            if number == 0 or number in result: raise ValueError('invalid/duplicate field')
            if wire == 0:
                value = varint()
            elif wire in (1, 2, 5):
                size = varint() if wire == 2 else 8 if wire == 1 else 4
                if offset + size > len(raw): raise ValueError('truncated field')
                value = raw[offset:offset+size]; offset += size
            else:
                raise ValueError('unsupported wire type')
            result[number] = (wire, value)
        return result
    outer = fields(data)
    if outer.get(3, (None, None))[0] != 2: raise ValueError('missing service trace envelope')
    inner = fields(outer[3][1])
    if inner.get(1, (None, None))[0] != 2: raise ValueError('missing selected IME ID field')
    result = {'cur_method_id': inner[1][1].decode('utf-8'),
              'remote_method_and_session': 'NOT_EXPOSED_BY_API33_SERVER_PROTO'}
    for number, name in [(8, 'binding_id'), (9, 'show_requested'), (12, 'input_shown'),
                         (16, 'system_ready'), (18, 'have_connection'), (19, 'bound_to_method')]:
        field = inner.get(number)
        if field is None:
            result[name] = None
        elif number == 8 and field[0] == 2:
            result[name] = field[1].decode('utf-8')
        elif number != 8 and field[0] == 0 and field[1] in (0, 1):
            result[name] = bool(field[1])
        else:
            raise ValueError('invalid field type/value: ' + name)
    return result


def evaluate(output, layer, *, returncode=0, only_b01=False):
    contract = json.loads(MANIFEST.read_text())[layer]
    tests = contract['tests']
    if only_b01:
        tests = [test for test in tests if test['cases'] == [{'case_id': 'B01'}]]
    return protocol.parse_instrumentation(
        output, returncode=returncode, expected_tests=len(tests),
        expected_identities=[test['identity'] for test in tests],
        allow_skips=contract['allow_skips'],
        case_contract={test['identity']: test['cases'] for test in tests})


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('layer', choices=['app', 'host', 'server-proto'])
    parser.add_argument('raw', type=Path)
    parser.add_argument('result', type=Path)
    parser.add_argument('--adb-exit', type=int, default=0)
    parser.add_argument('--b01', action='store_true')
    args = parser.parse_args()
    result = server_proto(args.raw.read_bytes()) if args.layer == 'server-proto' else evaluate(
        args.raw.read_text(), args.layer, returncode=args.adb_exit, only_b01=args.b01)
    args.result.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
    print(json.dumps({key: value for key, value in result.items() if key not in ('case_emissions', 'reported_counts')}, ensure_ascii=False))
    # INCONCLUSIVE stays in durable evidence, and is never full acceptance PASS.
    return 2 if result.get('result') == 'FAIL' else 0


if __name__ == '__main__':
    raise SystemExit(main())
