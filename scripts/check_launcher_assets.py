#!/usr/bin/env python3
"""Validate all legacy launcher resources using independent full WebP decoders."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import shutil
import struct
import subprocess

ROOT = Path(__file__).resolve().parents[1]
DENSITIES = (('mdpi', 48), ('hdpi', 72), ('xhdpi', 96), ('xxhdpi', 144), ('xxxhdpi', 192))


def inspect(path, pixels):
    data = path.read_bytes()
    problems = []
    if len(data) < 12 or data[:4] != b'RIFF' or data[8:12] != b'WEBP':
        problems.append('invalid RIFF/WEBP signature')
    elif struct.unpack('<I', data[4:8])[0] + 8 != len(data):
        problems.append('RIFF declared length differs from actual bytes')
    else:
        offset = 12
        chunks = []
        while offset + 8 <= len(data):
            kind = data[offset:offset + 4]
            size = struct.unpack('<I', data[offset + 4:offset + 8])[0]
            chunks.append(kind)
            offset += 8 + size + size % 2
        if offset != len(data) or not any(k in chunks for k in (b'VP8 ', b'VP8L')):
            problems.append('incomplete WebP chunks or absent image payload')
    commands = [
        ['dwebp', str(path), '-o', '/dev/null'],
        ['ffmpeg', '-v', 'error', '-xerror', '-err_detect', 'explode', '-i', str(path),
         '-frames:v', '1', '-f', 'rawvideo', '-pix_fmt', 'rgba', '-'],
    ]
    decoders = []
    for command in commands:
        if not shutil.which(command[0]):
            problems.append('required decoder unavailable: ' + command[0])
            continue
        try:
            proc = subprocess.run(command, capture_output=True, timeout=10)
        except subprocess.TimeoutExpired:
            problems.append('decoder timed out: ' + command[0])
            continue
        stderr = proc.stderr.decode(errors='replace')
        decoders.append({'tool': command[0], 'exit': proc.returncode, 'stderr': stderr[:700]})
        if proc.returncode != 0:
            problems.append('full decode failed: ' + command[0])
        elif command[0] == 'dwebp':
            match = re.search(r'Dimensions: (\d+) x (\d+)', stderr)
            if not match or tuple(map(int, match.groups())) != (pixels, pixels):
                problems.append('wrong launcher dimensions')
        elif len(proc.stdout) != pixels * pixels * 4:
            problems.append('FFmpeg did not decode one complete expected RGBA frame')
    return {'path': str(path.relative_to(ROOT)), 'bytes': len(data),
            'sha256': hashlib.sha256(data).hexdigest(), 'expected_pixels': pixels,
            'problems': problems, 'decoders': decoders}


def provenance_problems():
    # Fixed allowlists: a malicious manifest cannot make CI read arbitrary paths.
    sources = {'app/src/main/res/drawable/kuyin_zhuyin_icon_1789306106095.jpg',
               'app/src/main/res/drawable/ic_launcher_foreground.xml',
               'app/src/main/res/drawable/ic_launcher_background.xml'}
    outputs = {f'app/src/main/res/mipmap-{density}/{name}.webp'
               for density, _ in DENSITIES for name in ('ic_launcher', 'ic_launcher_round')}
    problems = []
    try:
        manifest = json.loads((ROOT / 'docs/community/media/GENERATED_LAUNCHER_MANIFEST.json').read_text())
        for field, expected in [('sources', sources), ('outputs', outputs)]:
            records = manifest[field]
            names = [record['path'] for record in records]
            if len(names) != len(expected) or set(names) != expected:
                problems.append('unexpected/missing/duplicate manifest ' + field)
                continue
            for record in records:
                actual = hashlib.sha256((ROOT / record['path']).read_bytes()).hexdigest()
                if actual != record['sha256']:
                    problems.append('manifest hash mismatch: ' + record['path'])
        generator = manifest['generator']
        expected_generator = 'scripts/generate_launcher_assets.py'
        if (generator['path'] != expected_generator or generator['sha256'] !=
                hashlib.sha256((ROOT / expected_generator).read_bytes()).hexdigest()):
            problems.append('generator provenance mismatch')
    except (OSError, ValueError, KeyError, TypeError) as error:
        problems.append('manifest unavailable/malformed: ' + type(error).__name__)
    return problems


def inspect_all():
    provenance = provenance_problems()
    records = [inspect(ROOT / f'app/src/main/res/mipmap-{density}/{name}.webp', pixels)
               for density, pixels in DENSITIES for name in ('ic_launcher', 'ic_launcher_round')]
    for record in records:
        record['problems'].extend(provenance)
    return records


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--json', type=Path)
    args = parser.parse_args()
    records = inspect_all()
    if args.json:
        args.json.write_text(json.dumps(records, ensure_ascii=False, indent=2) + '\n')
    for record in records:
        print(('FAIL ' if record['problems'] else 'PASS ') + record['path'])
        for problem in record['problems']:
            print('  ' + problem)
    return int(any(record['problems'] for record in records))


if __name__ == '__main__':
    raise SystemExit(main())
