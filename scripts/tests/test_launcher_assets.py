"""Real asset regression: malformed, truncated or wrong-size icons cannot pass."""
from pathlib import Path
import sys
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import check_launcher_assets as assets


class LauncherBinaryIntegrityTest(unittest.TestCase):
    def test_every_density_and_round_variant_fully_decodes_at_expected_size(self):
        records = assets.inspect_all()
        self.assertEqual(10, len(records))
        for record in records:
            with self.subTest(path=record['path']):
                self.assertFalse(record['problems'], record['problems'])
                self.assertEqual(['dwebp', 'ffmpeg'], [d['tool'] for d in record['decoders']])


class LauncherProvenanceRegressionTest(unittest.TestCase):
    def sandbox(self):
        import tempfile
        import shutil
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        target = Path(self.directory.name)
        originals = assets.ROOT
        files = ['scripts/generate_launcher_assets.py',
                 'app/src/main/res/drawable/ic_launcher_foreground.xml',
                 'app/src/main/res/drawable/ic_launcher_background.xml',
                 'app/src/main/res/drawable/kuyin_zhuyin_icon_1789306106095.jpg',
                 'docs/community/media/GENERATED_LAUNCHER_MANIFEST.json']
        files += [f'app/src/main/res/mipmap-{d}/{n}.webp'
                  for d, _ in assets.DENSITIES for n in ('ic_launcher', 'ic_launcher_round')]
        for name in files:
            out = target / name
            out.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(originals / name, out)
        return target

    def test_drifted_gradient_direction_is_rejected_before_generating_icons(self):
        from unittest.mock import patch
        import generate_launcher_assets as generator
        target = self.sandbox()
        path = target / 'app/src/main/res/drawable/ic_launcher_background.xml'
        path.write_text(path.read_text().replace('android:endX="108"', 'android:endX="0"'))
        with patch.object(generator, 'ROOT', target), self.assertRaises(ValueError):
            generator.generate()

    def test_wrong_output_provenance_cannot_pass_with_decodable_icons(self):
        import json
        from unittest.mock import patch
        target = self.sandbox()
        path = target / 'docs/community/media/GENERATED_LAUNCHER_MANIFEST.json'
        manifest = json.loads(path.read_text())
        manifest['outputs'][0]['sha256'] = '0' * 64
        path.write_text(json.dumps(manifest))
        with patch.object(assets, 'ROOT', target):
            self.assertTrue(any(r['problems'] for r in assets.inspect_all()),
                            'Full decoding alone cannot authenticate a mismatching provenance manifest')


class RoborazziPngIntegrityTest(unittest.TestCase):
    def test_greeting_unit_render_fully_decodes_with_pillow_and_ffmpeg(self):
        import subprocess
        import json
        import hashlib
        from PIL import Image
        path = assets.ROOT / 'app/src/test/screenshots/greeting.png'
        try:
            with Image.open(path) as image:
                image.load()
                self.assertEqual('PNG', image.format)
                width, height = image.size
                metadata = list(image.info)
        except OSError as error:
            self.fail('Complete PNG decode failed: ' + str(error))
        result = subprocess.run(['ffmpeg', '-v', 'error', '-xerror', '-err_detect', 'explode',
                                 '-i', str(path), '-frames:v', '1', '-f', 'rawvideo',
                                 '-pix_fmt', 'rgba', '-'], capture_output=True, timeout=10)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(width * height * 4, len(result.stdout))

        manifest = json.loads((assets.ROOT / 'docs/community/media/ROBORAZZI_RENDER_MANIFEST.json').read_text())
        self.assertEqual('ROBORAZZI_UNIT_TEST_RENDER_NOT_DEVICE_SCREENSHOT', manifest['classification'])
        self.assertFalse(manifest['phone_runtime_evidence'])
        self.assertEqual('app/src/test/screenshots/greeting.png', manifest['output']['path'])
        self.assertEqual(hashlib.sha256(path.read_bytes()).hexdigest(), manifest['output']['sha256'])
        self.assertEqual([width, height], manifest['output']['dimensions'])
        self.assertEqual([], metadata)
        self.assertEqual(metadata, manifest['output']['metadata_keys'])
        expected_sources = {'app/src/test/java/com/example/GreetingScreenshotTest.kt',
                            'app/src/main/java/com/example/MainActivity.kt',
                            'app/build.gradle.kts', 'gradle/libs.versions.toml'}
        names = [record['path'] for record in manifest['source_files']]
        self.assertEqual(len(expected_sources), len(names))
        self.assertEqual(expected_sources, set(names))  # No manifest path traversal.
        for record in manifest['source_files']:
            self.assertEqual(hashlib.sha256((assets.ROOT / record['path']).read_bytes()).hexdigest(),
                             record['sha256'])
