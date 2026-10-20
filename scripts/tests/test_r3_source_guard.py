"""Execute the actual RC workflow guard against isolated Git history; no Android target."""
from pathlib import Path
import subprocess
import sys
import tempfile
import textwrap
import unittest

ROOT = Path(__file__).resolve().parents[2]
WORKFLOW = ROOT / '.github/workflows/r2-host-observation-probe.yml'
QR = 'de2ac7d94e0a79f492d11ee165173e65b4562bd7'
INFRA = 'e137e37594c04f8c0fd88c01ef911d1df9bcbc49'
COMBINED = '185a4e545bd2574ad773e255ea558e6be5f3ab0e'
RUNTIME = 'b98206267b3a65c452db1661bb3bae910ec61f6a'
HEALTH = '97645c238dcecc43e677de625bf7049bd487d700'


class R3SourceGuardTest(unittest.TestCase):
    def test_rc_uses_only_explicit_health_gated_runtime_not_duplicate_legacy_job(self):
        build = (ROOT / '.github/workflows/build.yml').read_text()
        job = build.split('  runtime-smoke:', 1)[1]
        self.assertIn("if: github.head_ref != 'release/rc-closure-r3'", job)
        workflow = WORKFLOW.read_text()
        self.assertIn("&& inputs.host_scope != 'release-candidate'", workflow)
        rc = workflow.split('  release-candidate:', 1)[1]
        self.assertIn("if: github.event_name == 'workflow_dispatch' && inputs.host_scope == 'release-candidate'", rc)
        self.assertIn("KUYIN_R23_HEALTH: 'true'", rc)
        self.assertIn('KUYIN_R22_BINDING_ARM: preinstalled', rc)
        self.assertIn('script: bash scripts/runtime_smoke.sh', rc)

    def run_guard(self, changed=None, *, commit=True):
        lines = WORKFLOW.read_text().splitlines()
        title = '      - name: Verify approved RC source groups'
        if title in lines:
            start = lines.index('        run: |', lines.index(title)) + 1
            end = next(i for i in range(start, len(lines)) if lines[i].startswith('      - '))
            program = textwrap.dedent('\n'.join(lines[start:end]))
            executable = [sys.executable, '-c']
        else:
            # Meaningful pre-fix RED: the existing binding guard rejects even the approved QR slice.
            title = '      - name: Verify unchanged Android sources and build shared four APKs once'
            start = lines.index('        run: |', lines.index(title)) + 1
            program = lines[start].strip()
            executable = ['bash', '-c']
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            files = {
                'app/src/main/Product.kt': 'typing unchanged',
                'app/src/main/java/com/example/ime/ui/OpenSourceDonationCard.kt': 'old pseudo QR',
                'app/build.gradle.kts': 'version20003; signing unchanged',
                'app/src/androidTest/Visibility.kt': 'original visibility assertion',
                'app/src/debug/Editor.kt': 'original real editor',
                'app/src/test/TypingTest.kt': 'strict typing regression',
                'acceptance-host/src/main/Host.kt': 'real EditText',
                'acceptance-host/src/androidTest/HostTest.kt': 'strict commit assertion',
                'acceptance-host/src/test/ProtocolTest.kt': 'strict unit regression',
                'build.gradle.kts': 'approved root plugins',
                'settings.gradle.kts': 'approved modules and repositories',
                'gradle.properties': 'approved build settings',
                'gradlew': 'approved wrapper', 'gradlew.bat': 'approved Windows wrapper',
                'gradle/libs.versions.toml': 'pinned test dependency 2.4.0',
                'scripts/bootstrap_native_deps.sh': 'native pin unchanged',
                'scripts/runtime_smoke.sh': 'approved H0 H1 and exact instrumentation sequence',
                'scripts/emulator_health.py': 'frozen and unknown fail closed',
                'scripts/r2_ci_protocol.py': 'strict protocol',
                'scripts/p1_instrumentation_manifest.json': 'frozen manifest',
                'scripts/r2_current_instrumentation_manifest.json': 'exact 5/33 test identities',
                'scripts/p1_acceptance_device.py': 'frozen physical safety runner',
                'LICENSE': 'unchanged license', 'NOTICE': 'unchanged upstream notices',
            }
            for name, content in files.items():
                path = root / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(content)
            def git(*args):
                return subprocess.check_output(['git', *args], cwd=root, text=True, stderr=subprocess.PIPE).strip()
            def save(message, *, allow_empty=False):
                git('add', '.')
                git('-c', 'user.name=Fixture', '-c', 'user.email=fixture@example.invalid', 'commit',
                    *(['--allow-empty'] if allow_empty else []), '-qm', message)
                return git('rev-parse', 'HEAD')
            git('init', '-q')
            infrastructure = save('infrastructure baseline')
            (root / 'app/src/main/java/com/example/ime/ui/OpenSourceDonationCard.kt').write_text('approved real QR')
            (root / 'app/build.gradle.kts').write_text('version20004; same signing; test-only ZXing')
            (root / 'NOTICE').write_text('approved notices and QR provenance')
            qr_test = root / 'app/src/test/java/com/example/ime/DonationQrUiTest.kt'
            qr_test.parent.mkdir(parents=True, exist_ok=True)
            qr_test.write_text('strict QR assertions')
            product = save('approved QR product')
            combined = save('approved combined test source', allow_empty=True)
            (root / 'scripts/emulator_health.py').write_text('approved controlling terminal; strict oracle unchanged')
            health = save('approved H1 capture correction')
            runtime = save('approved runtime orchestration', allow_empty=True)
            if changed:
                name, content = changed
                path = root / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(content)
                if commit: save('unreviewed change')
            program = program.replace(QR, product).replace(INFRA, infrastructure).replace(COMBINED, combined).replace(RUNTIME, runtime).replace(HEALTH, health).replace(
                '2b8e8e4efff73741319fb00daf4cb4e58101e4e8', infrastructure)
            return subprocess.run([*executable, program], cwd=root, capture_output=True, text=True, timeout=10)

    def test_reviewed_qr_and_h1_amendment_preserve_other_infrastructure(self):
        result = self.run_guard()
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

    def test_unreviewed_typing_source_is_rejected(self):
        self.assertNotEqual(0, self.run_guard(('app/src/main/Product.kt', 'modified decoder')).returncode)

    def test_unreviewed_permission_is_rejected(self):
        self.assertNotEqual(0, self.run_guard(('app/src/main/AndroidManifest.xml', 'new INTERNET permission')).returncode)

    def test_wrong_version_or_signing_is_rejected(self):
        self.assertNotEqual(0, self.run_guard(('app/build.gradle.kts', 'new unapproved signing')).returncode)

    def test_native_pin_and_host_editor_changes_are_rejected(self):
        for name in ['scripts/bootstrap_native_deps.sh', 'gradle/libs.versions.toml', 'acceptance-host/src/main/Host.kt']:
            with self.subTest(name=name):
                self.assertNotEqual(0, self.run_guard((name, 'unreviewed')).returncode)

    def test_original_visibility_and_host_assertions_are_preserved(self):
        for name in ['app/src/androidTest/Visibility.kt', 'acceptance-host/src/androidTest/HostTest.kt']:
            with self.subTest(name=name):
                self.assertNotEqual(0, self.run_guard((name, 'weakened')).returncode)

    def test_health_and_protocol_and_frozen_runner_are_preserved(self):
        for name in ['scripts/emulator_health.py', 'scripts/r2_ci_protocol.py',
                     'scripts/p1_acceptance_device.py', 'scripts/p1_instrumentation_manifest.json',
                     'scripts/r2_current_instrumentation_manifest.json']:
            with self.subTest(name=name):
                self.assertNotEqual(0, self.run_guard((name, 'unreviewed')).returncode)

    def test_dirty_protected_source_is_rejected(self):
        self.assertNotEqual(0, self.run_guard(('app/src/main/Product.kt', 'dirty'), commit=False).returncode)

    def test_untracked_product_source_is_rejected(self):
        self.assertNotEqual(0, self.run_guard(('app/src/main/New.kt', 'untracked'), commit=False).returncode)

    def test_unreviewed_root_build_wrapper_and_jvm_test_inputs_are_rejected(self):
        for name in ['build.gradle.kts', 'settings.gradle.kts', 'gradle.properties', 'gradlew', 'gradlew.bat',
                     'app/src/test/java/com/example/ime/DonationQrUiTest.kt',
                     'acceptance-host/src/test/ProtocolTest.kt']:
            with self.subTest(name=name):
                self.assertNotEqual(0, self.run_guard((name, 'unreviewed build or weakened assertions')).returncode)

    def test_unreviewed_runtime_orchestration_is_rejected(self):
        self.assertNotEqual(0, self.run_guard(('scripts/runtime_smoke.sh', 'skip H1 and report PASS')).returncode)

    def test_dirty_runtime_orchestration_is_rejected(self):
        self.assertNotEqual(0, self.run_guard(('scripts/runtime_smoke.sh', 'dirty ordering'), commit=False).returncode)


if __name__ == '__main__':
    unittest.main()
