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
