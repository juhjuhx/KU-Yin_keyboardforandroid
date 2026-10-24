import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from check_repository_secrets import find_secrets, scan_repository

class RepositorySecretsTest(unittest.TestCase):
    def test_recognizes_private_key_and_provider_credentials_without_returning_values(self):
        values = [('ghp_' + 'A' * 36), ('AKIA' + 'A' * 16), ('AIza' + 'A' * 35),
                  ('-----BEGIN ' + 'PRIVATE KEY-----')]
        for value in values:
            with self.subTest(kind=value[:3]):
                found = find_secrets(value.encode())
                self.assertTrue(found)
                self.assertNotIn(value, json.dumps(found))

    def test_public_certificate_hashes_and_environment_references_are_not_credentials(self):
        text = 'signingPassword = System.getenv("PASSWORD")\nsha256=' + 'a' * 64
        self.assertEqual([], find_secrets(text.encode()))

    def test_deleted_secret_remains_visible_in_history_but_not_current_tree(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            def git(*args): return subprocess.check_output(['git', *args], cwd=root, stderr=subprocess.DEVNULL)
            git('init', '-q'); git('config', 'user.name', 'Synthetic Fixture'); git('config', 'user.email', 'fixture@example.invalid')
            token = 'ghp_' + 'B' * 36
            (root / 'config.txt').write_text(token)
            git('add', '.'); git('commit', '-qm', 'synthetic first')
            (root / 'config.txt').write_text('removed')
            git('commit', '-qam', 'synthetic removal')
            self.assertEqual([], scan_repository(root)['findings'])
            old = scan_repository(root, history=True)
            self.assertTrue(old['findings'])
            self.assertNotIn(token, json.dumps(old))

    def test_dotenv_variants_are_private_but_examples_are_permitted(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory);subprocess.run(['git', 'init', '-q'], cwd=root, check=True)
            for name in ['.env.production', '.env.example', '.env.template']:
                (root / name).write_text('synthetic_setting=value')
            subprocess.run(['git', 'add', '.'], cwd=root, check=True)
            found = scan_repository(root)['findings']
            self.assertEqual(['.env.production'], [x['path'] for x in found])

    def test_history_checks_every_private_path_even_when_blob_is_shared(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            def git(*args): return subprocess.check_output(['git', *args], cwd=root, stderr=subprocess.DEVNULL)
            git('init', '-q');git('config', 'user.name', 'Synthetic');git('config', 'user.email', 'fixture@example.invalid')
            for name in ['aaa-safe.txt', 'release.jks']: (root / name).write_text('same synthetic bytes')
            git('add', '.');git('commit', '-qm', 'shared blob')
            git('rm', 'release.jks');git('commit', '-qm', 'remove private alias')
            self.assertTrue(any(x['path'] == 'release.jks' for x in scan_repository(root, history=True)['findings']))

    def test_history_includes_private_paths_created_and_removed_only_in_merges(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            def git(*args): return subprocess.check_output(['git', *args], cwd=root, stderr=subprocess.DEVNULL)
            git('init', '-q');git('config', 'user.name', 'Synthetic');git('config', 'user.email', 'fixture@example.invalid')
            (root / 'aaa-safe.txt').write_text('same synthetic bytes')
            git('add', '.');git('commit', '-qm', 'root')
            main = git('branch', '--show-current').decode().strip()
            for index in range(2):
                branch = 'side' + str(index)
                git('switch', '-qc', branch)
                (root / (branch + '.txt')).write_text('side')
                git('add', '.');git('commit', '-qm', 'side')
                git('switch', '-q', main)
                (root / ('main' + str(index) + '.txt')).write_text('main')
                git('add', '.');git('commit', '-qm', 'main')
                git('merge', '--no-commit', '--no-ff', branch)
                if index == 0:
                    (root / 'release.jks').write_text('same synthetic bytes');git('add', 'release.jks')
                else: git('rm', 'release.jks')
                git('commit', '-qm', 'merge-only path change')
            self.assertFalse((root / 'release.jks').exists())
            self.assertTrue(any(x['path'] == 'release.jks' for x in scan_repository(root, history=True)['findings']))

    def test_tracked_keystore_is_blocked_without_reading_its_contents(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory);subprocess.run(['git', 'init', '-q'], cwd=root, check=True)
            (root / 'release.jks').write_bytes(b'\0synthetic nonsecret\0')
            subprocess.run(['git', 'add', '.'], cwd=root, check=True)
            self.assertTrue(scan_repository(root)['findings'])
