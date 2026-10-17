"""Health-only dispatch must remain separate from APK/binding orchestration."""
from pathlib import Path
import re
import unittest


class HealthOnlyWorkflowTest(unittest.TestCase):
    def test_dispatch_has_an_isolated_health_only_job(self):
        text = (Path(__file__).resolve().parents[2] / '.github/workflows/r2-host-observation-probe.yml').read_text()
        self.assertRegex(text, r"options: \[.*\bhealth\b.*\]")
        match = re.search(r'(?ms)^  health-only:\n(.*?)(?=^  [a-z][a-z-]*:|\Z)', text)
        self.assertIsNotNone(match, 'Health-only must have its own job')
        job = match.group(1)
        self.assertIn("inputs.host_scope == 'health'", job)
        self.assertRegex(text, r"(?m)^    if: .*inputs.host_scope != 'health'")
        self.assertIn('timeout-minutes: 20', job)
        self.assertEqual(1, job.count('uses: reactivecircus/android-emulator-runner@'))
        for expected in ['api-level: 33', 'target: default', 'arch: x86_64', 'profile: pixel_7']:
            self.assertIn(expected, job)
        self.assertIn('scripts/emulator_health.py h0', job)
        for forbidden in ['adb install', './gradlew', 'bash scripts/bootstrap_native', ' h1', 'am instrument', 'ime set', 'bash scripts/runtime_smoke', 'continue-on-error']:
            self.assertNotIn(forbidden, job)
        self.assertIn('if: always()', job)
        self.assertEqual(1, job.count('uses: actions/upload-artifact@'))
