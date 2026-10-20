"""Execute the actual probe workflow's guard in an isolated Git fixture."""
from pathlib import Path
import subprocess
import sys
import tempfile
import textwrap
import unittest

WORKFLOW = Path(__file__).resolve().parents[2] / ".github/workflows/r2-host-observation-probe.yml"
BASELINE = "63c9e4ab8ad7e277013acb5d7cce99228ec1f827"


class R2SourceGuardTest(unittest.TestCase):
    def run_guard(self, change=None):
        lines = WORKFLOW.read_text().splitlines()
        start = lines.index("      - name: Verify protected source and record diagnostic provenance")
        start = lines.index("        run: |", start) + 1
        end = next(i for i in range(start, len(lines)) if lines[i].startswith("      - name:"))
        program = textwrap.dedent("\n".join(lines[start:end]))
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            files = {
                "app/src/main/Product.kt": "unchanged product",
                "acceptance-host/src/main/Host.kt": "unchanged editor",
                "gradle/native-pin.txt": "unchanged native pin",
                "scripts/bootstrap_native_deps.sh": "actual bootstrap dependency pin",
                "gradle/libs.versions.toml": '[versions]\nuiautomator = "2.3.0"\ncore = "1.6.1"\n',
            }
            for path, content in files.items():
                target = root / path
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text(content)
            def git(*args):
                return subprocess.check_output(["git", *args], cwd=root, stderr=subprocess.PIPE, text=True)
            git("init", "-q")
            git("add", ".")
            git("-c", "user.name=Test", "-c", "user.email=test@example.invalid", "commit", "-qm", "baseline")
            baseline = git("rev-parse", "HEAD").strip()
            if change:
                path, content = change
                (root / path).write_text(content)
                git("add", ".")
                git("-c", "user.name=Test", "-c", "user.email=test@example.invalid", "commit", "-qm", "fixture change")
            return subprocess.run([sys.executable, "-c", program.replace(BASELINE, baseline)],
                                  cwd=root, capture_output=True, text=True, timeout=10)

    def test_binding_comparison_requires_both_a_health_gates(self):
        import json
        lines = WORKFLOW.read_text().splitlines()
        start = lines.index("          python3 - <<'HEALTH'") + 1
        end = lines.index("          HEALTH", start)
        program = textwrap.dedent("\n".join(lines[start:end]))
        for h0, h1, success in [('HEALTHY', 'HEALTHY', True), ('UNHEALTHY', 'HEALTHY', False),
                                ('HEALTHY', 'INCONCLUSIVE', False), ('MISSING', 'HEALTHY', False)]:
            with self.subTest(h0=h0, h1=h1), tempfile.TemporaryDirectory() as tmp:
                for stage, status in [('h0', h0), ('h1', h1)]:
                    if status == 'MISSING': continue
                    path = Path(tmp, 'build/r22-legacy', stage, 'result.json')
                    path.parent.mkdir(parents=True, exist_ok=True)
                    path.write_text(json.dumps({'status': status}))
                proc = subprocess.run([sys.executable, '-c', program], cwd=tmp, capture_output=True)
                self.assertEqual(success, proc.returncode == 0)

    def test_unchanged_source_is_accepted(self):
        result = self.run_guard()
        self.assertEqual(0, result.returncode, result.stderr)

    def test_only_supported_host_test_dependency_upgrade_is_accepted(self):
        result = self.run_guard(("gradle/libs.versions.toml", '[versions]\nuiautomator = "2.4.0"\ncore = "1.6.1"\n'))
        self.assertEqual(0, result.returncode, result.stderr)

    def test_other_dependency_change_is_rejected(self):
        result = self.run_guard(("gradle/libs.versions.toml", '[versions]\nuiautomator = "2.4.0"\ncore = "1.7.0"\n'))
        self.assertNotEqual(0, result.returncode)

    def test_unreviewed_uiautomator_version_is_rejected(self):
        result = self.run_guard(("gradle/libs.versions.toml", '[versions]\nuiautomator = "9.0.0"\ncore = "1.6.1"\n'))
        self.assertNotEqual(0, result.returncode)

    def test_other_gradle_file_change_is_rejected(self):
        self.assertNotEqual(0, self.run_guard(("gradle/native-pin.txt", "different pin")).returncode)

    def test_actual_native_bootstrap_pin_change_is_rejected(self):
        self.assertNotEqual(0, self.run_guard(("scripts/bootstrap_native_deps.sh", "changed actual bootstrap pin")).returncode)

    def test_product_or_host_source_change_is_rejected(self):
        for path in ("app/src/main/Product.kt", "acceptance-host/src/main/Host.kt"):
            with self.subTest(path=path):
                self.assertNotEqual(0, self.run_guard((path, "changed source")).returncode)


if __name__ == "__main__":
    unittest.main()
