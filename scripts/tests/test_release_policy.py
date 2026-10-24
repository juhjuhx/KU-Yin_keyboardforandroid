"""Release-policy boundaries, distinct from product Runtime acceptance."""
import hashlib
from pathlib import Path
import re
import unittest
import yaml

ROOT = Path(__file__).resolve().parents[2]

def workflow(name):
    return yaml.load((ROOT / '.github/workflows' / name).read_text(), Loader=yaml.BaseLoader)

class ReleasePolicyTest(unittest.TestCase):
    def test_manual_alpha_workflow_has_no_repository_write_or_publication_capability(self):
        w = workflow('prerelease.yml')
        self.assertEqual({'contents': 'read'}, w['permissions'])
        self.assertEqual(['workflow_dispatch'], list(w['on']))
        for job in w['jobs'].values():
            job_permissions = job.get('permissions', {})
            self.assertNotEqual('write-all', job_permissions)
            if isinstance(job_permissions, dict): self.assertNotEqual('write', job_permissions.get('contents'))
            for step in job['steps']:
                # gh release publication was the confirmed unsafe boundary.
                self.assertNotRegex(step.get('run', ''), r'\bgh\s+release\s+(create|upload|edit|delete)\b')
        self.assertTrue(any('/upload-artifact@' in step.get('uses', '') for job in w['jobs'].values() for step in job['steps']))

    def test_runtime_is_explicit_opt_in_and_failure_cannot_be_masked(self):
        w = workflow('build.yml')
        self.assertIn('workflow_dispatch', w['on'], 'Runtime must require explicit manual opt-in')
        self.assertEqual('false', w['on']['workflow_dispatch']['inputs']['run_runtime']['default'])
        runtime = w['jobs']['runtime-smoke']
        self.assertEqual("${{ github.event_name == 'workflow_dispatch' && inputs.run_runtime == true }}", runtime['if'])
        self.assertNotEqual('true', runtime.get('continue-on-error', 'false'))
        self.assertEqual({'contents': 'read'}, w['permissions'])

    def test_android_lint_is_an_explicit_blocking_ci_job(self):
        jobs = workflow('build.yml')['jobs']
        self.assertIn('lint', jobs)
        self.assertNotEqual('true', jobs['lint'].get('continue-on-error', 'false'))
        commands = '\n'.join(step.get('run', '') for step in jobs['lint']['steps'])
        self.assertIn(':app:lintDebug', commands)
        self.assertIn(':acceptance-host:lintDebug', commands)

    def test_all_external_actions_are_immutable_commits(self):
        for file in (ROOT / '.github/workflows').glob('*.yml'):
            w = workflow(file.name)
            for job in w['jobs'].values():
                for step in job['steps']:
                    if 'uses' in step:
                        self.assertRegex(step['uses'], r'^[\w./-]+@[0-9a-f]{40}$', file.name)

    def test_wrapper_archive_is_authenticated_by_official_checksum(self):
        text = (ROOT / 'gradle/wrapper/gradle-wrapper.properties').read_text()
        self.assertIn('distributionSha256Sum=b266d5ff6b90eada6dc3b20cb090e3731302e553a27c5d3e4df1f0d76beaff06', text)

    def test_lgpl_document_is_the_verbatim_pinned_standard(self):
        # SPDX license-list-data 31ba1a50e5397e00a304dbadc76531740e89ee48.
        self.assertEqual('5749785c8bdefafcb5d798270ed0a967036fe2ca63dcedade1627565dfef81d2',
                         hashlib.sha256((ROOT / 'LICENSE').read_bytes()).hexdigest())
