"""Validate checked-in social vectors as accessible, attributable brand assets."""
import hashlib
import json
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
MEDIA = ROOT / 'docs/community/media/social'


class PublicationMediaTest(unittest.TestCase):
    def test_svg_accessibility_references_are_unique_and_resolvable(self):
        for path in sorted(MEDIA.glob('*.svg')):
            with self.subTest(asset=path.name):
                root = ET.parse(path).getroot()
                ids = [e.attrib['id'] for e in root.iter() if 'id' in e.attrib]
                self.assertEqual(len(ids), len(set(ids)), 'ambiguous SVG accessibility reference')
                for element in root.iter():
                    for reference in element.get('aria-labelledby', '').split():
                        self.assertIn(reference, ids)

    def test_asset_bytes_match_manifest_and_declared_dimensions(self):
        manifest = json.loads((MEDIA / 'ASSET_LICENSE_MANIFEST.json').read_text())
        self.assertEqual(manifest['screenshot_count'], 0)
        self.assertTrue(manifest['assets'])
        for asset in manifest['assets']:
            with self.subTest(asset=asset['id']):
                self.assertEqual(asset['category'], 'BRAND_RENDER_NOT_RUNTIME')
                for kind in ['svg', 'png']:
                    path = MEDIA / asset[kind]
                    self.assertEqual(hashlib.sha256(path.read_bytes()).hexdigest(), asset[kind + '_sha256'])
                with Image.open(MEDIA / asset['png']) as image:
                    image.load()
                    self.assertEqual(image.size, (asset['width'], asset['height']))
