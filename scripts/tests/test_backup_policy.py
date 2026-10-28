"""Static Android XML policy contracts, not OEM transfer execution evidence."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
DOMAINS = {'root', 'file', 'database', 'sharedpref', 'external',
           'device_root', 'device_file', 'device_database', 'device_sharedpref'}


class BackupPolicyTest(unittest.TestCase):
    def test_d2d_whitelists_only_keyboard_preferences(self):
        policy = ET.parse(ROOT / 'app/src/main/res/xml/data_extraction_rules.xml').getroot()
        transfer = policy.find('device-transfer')
        self.assertIsNotNone(transfer, 'A commented sample does not restrict D2D')
        self.assertEqual([{'domain': 'sharedpref', 'path': 'kuyin_settings.xml'}],
                         [item.attrib for item in transfer])
        # Android switches this mode to an allowlist when an include is present.
        allowed = {(item.get('domain'), item.get('path')) for item in transfer.findall('include')}
        for domain, path, expected in [
            ('sharedpref', 'kuyin_settings.xml', True),
            ('sharedpref', 'kuyin_dict_prefs.xml', False),
            ('sharedpref', 'ime_config.xml', False),
            ('database', 'kuyin_app_database', False),
            ('database', 'kuyin_app_database-wal', False),
            ('file', 'synthetic-feedback.txt', False),
            ('device_sharedpref', 'kuyin_settings.xml', False),
        ]:
            self.assertEqual(expected, (domain, path) in allowed, (domain, path))

    def test_cloud_and_legacy_rules_explicitly_exclude_all_domains(self):
        modern = ET.parse(ROOT / 'app/src/main/res/xml/data_extraction_rules.xml').getroot()
        cloud = modern.find('cloud-backup')
        legacy = ET.parse(ROOT / 'app/src/main/res/xml/backup_rules.xml').getroot()
        for label, mode in [('cloud', cloud), ('legacy', legacy)]:
            self.assertEqual([], mode.findall('include'), label)
            self.assertEqual({(domain, '.') for domain in DOMAINS},
                             {(item.get('domain'), item.get('path')) for item in mode.findall('exclude')}, label)
            self.assertEqual(9, len(list(mode)), label)

    def test_manifest_keeps_no_backup_permission_and_both_rule_references(self):
        app = ET.parse(ROOT / 'app/src/main/AndroidManifest.xml').getroot().find('application')
        ns = '{http://schemas.android.com/apk/res/android}'
        self.assertEqual('false', app.get(ns + 'allowBackup'))
        self.assertEqual('@xml/data_extraction_rules', app.get(ns + 'dataExtractionRules'))
        self.assertEqual('@xml/backup_rules', app.get(ns + 'fullBackupContent'))


if __name__ == '__main__':
    unittest.main()
