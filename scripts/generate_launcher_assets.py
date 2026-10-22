#!/usr/bin/env python3
"""Regenerate legacy icons from the existing adaptive icon; never repair corrupt bytes.

Development-only requirement: Pillow 12.2.0 / libwebp 1.6.0 used for this revision.
The existing 108dp background and centered 66dp foreground are cropped to the
72dp adaptive viewport. Square and round outputs are legacy launcher fallbacks.
"""
import hashlib
import io
import json
from pathlib import Path
import xml.etree.ElementTree as ET
from PIL import Image, ImageDraw, __version__, features

ROOT = Path(__file__).resolve().parents[1]
ANDROID = '{http://schemas.android.com/apk/res/android}'
SOURCE = 'app/src/main/res/drawable/kuyin_zhuyin_icon_1789306106095.jpg'
SOURCE_SHA256 = '382aa73288b0c3a2f30131ab3aeaffee41b9eae71e17ff832635937794472d38'
DENSITIES = (('mdpi', 48), ('hdpi', 72), ('xhdpi', 96), ('xxhdpi', 144), ('xxxhdpi', 192))


def sha(data):
    return hashlib.sha256(data).hexdigest()


def generate():
    source_bytes = (ROOT / SOURCE).read_bytes()
    if sha(source_bytes) != SOURCE_SHA256:
        raise ValueError('Foreground source changed; review provenance before regeneration')
    foreground_path = ROOT / 'app/src/main/res/drawable/ic_launcher_foreground.xml'
    background_path = ROOT / 'app/src/main/res/drawable/ic_launcher_background.xml'
    item = ET.parse(foreground_path).getroot().find('item')
    if (item.get(ANDROID + 'width'), item.get(ANDROID + 'height'),
            item.get(ANDROID + 'gravity'), item.get(ANDROID + 'drawable')) != (
                '66dp', '66dp', 'center', '@drawable/kuyin_zhuyin_icon_1789306106095'):
        raise ValueError('Adaptive foreground geometry changed; review before generating')
    background = ET.parse(background_path).getroot()
    gradient = next(background.iter('gradient'))
    stops = list(gradient)
    if (background.get(ANDROID + 'viewportWidth'), background.get(ANDROID + 'viewportHeight')) != ('108', '108'):
        raise ValueError('Unsupported adaptive background viewport')
    expected_gradient = {'startX': '0', 'startY': '0', 'endX': '108', 'endY': '108', 'type': 'linear'}
    if (len(list(background)) != 1 or list(background)[0].get(ANDROID + 'pathData') != 'M0,0h108v108h-108z' or
            any(gradient.get(ANDROID + key) != value for key, value in expected_gradient.items()) or
            [(stop.get(ANDROID + 'offset'), stop.get(ANDROID + 'color')) for stop in stops] !=
            [('0.0', '#0F172A'), ('1.0', '#0284C7')]):
        raise ValueError('Adaptive background geometry/stops changed; review before generating')
    # 4x rasterization preserves the existing viewport while smoothing small sizes.
    scale = 4
    size = 108 * scale
    canvas = Image.new('RGBA', (size, size))
    start, end = (15, 23, 42), (2, 132, 199)
    pixels = canvas.load()
    for y in range(size):
        for x in range(size):
            t = (x + y) / (2 * (size - 1))
            pixels[x, y] = (*[round(a + (b - a) * t) for a, b in zip(start, end)], 255)
    with Image.open(io.BytesIO(source_bytes)) as image:
        image.load()
        foreground = image.convert('RGBA').resize((66 * scale, 66 * scale), Image.Resampling.LANCZOS)
    canvas.alpha_composite(foreground, (21 * scale, 21 * scale))
    square = canvas.crop((18 * scale, 18 * scale, 90 * scale, 90 * scale))
    mask = Image.new('L', square.size)
    ImageDraw.Draw(mask).ellipse((0, 0, square.width - 1, square.height - 1), fill=255)
    rounded = square.copy()
    rounded.putalpha(mask)
    outputs = []
    for density, count in DENSITIES:
        for name, image in [('ic_launcher', square), ('ic_launcher_round', rounded)]:
            path = ROOT / f'app/src/main/res/mipmap-{density}/{name}.webp'
            result = image.resize((count, count), Image.Resampling.LANCZOS)
            result.save(path, 'WEBP', lossless=True, quality=100, method=6, exact=True)
            outputs.append({'path': str(path.relative_to(ROOT)), 'pixels': [count, count],
                            'bytes': path.stat().st_size, 'sha256': sha(path.read_bytes()),
                            'rgba_sha256': sha(result.tobytes())})
    files = [SOURCE, str(foreground_path.relative_to(ROOT)), str(background_path.relative_to(ROOT))]
    manifest = {'schema': 1, 'classification': 'REGENERATED_LEGACY_LAUNCHER_NOT_RECOVERED_ORIGINAL',
                'authorized': '2026-10-21 user requested self-encoded generation because no original exists',
                'baseline_git_sha': '8369da25fa54ab762dfceed457e71a08767a642e',
                'generator': {'path': 'scripts/generate_launcher_assets.py', 'sha256': sha(Path(__file__).read_bytes()),
                              'Pillow': __version__, 'libwebp': features.version('webp')},
                'sources': [{'path': f, 'sha256': sha((ROOT / f).read_bytes())} for f in files],
                'geometry': 'existing 108dp background/66dp centered foreground, central72dp crop; circle mask for round fallback',
                'outputs': outputs, 'historical_corruption': 'ASSET_MANIFEST.json records original bytes; retained unchanged',
                'rights': 'Existing project foreground only; C2PA chain/complete rights review remains OPEN',
                'device_icon_display': 'NOT_TESTED', 'runtime_screenshots': False}
    path = ROOT / 'docs/community/media/GENERATED_LAUNCHER_MANIFEST.json'
    path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n')
    print('Generated 10 lossless WebP fallbacks from the existing adaptive icon; source unchanged')


if __name__ == '__main__':
    generate()
