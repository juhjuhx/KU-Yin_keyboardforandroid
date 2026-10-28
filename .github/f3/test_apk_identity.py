"""Public SDK output fixtures only; no APK install or private signing key access."""
import unittest
from apk_identity import signer_certificate, package_header

CERT = '4' * 64
OTHER = '5' * 64
LEGACY = 'Number of signers: 1\nSigner #1 certificate SHA-256 digest: '+CERT+'\n'
# AOSP ApkSignerTool V3.1 emits SDK-range labels instead of numbered labels:
# https://android.googlesource.com/platform/tools/apksig/+/refs/heads/main/src/apksigner/java/com/android/apksigner/ApkSignerTool.java
RANGED = ('Number of signers: 1\nSigner (minSdkVersion=33, maxSdkVersion=2147483647) certificate SHA-256 digest: '+CERT+'\n'
          'Signer (minSdkVersion=28, maxSdkVersion=32) certificate SHA-256 digest: '+CERT+'\n')


class ApkIdentityTest(unittest.TestCase):
    def test_unknown_signer_label_cannot_hide_different_certificate(self):
        text = RANGED + 'Signer (minSdkVersion=33 (dev release=true), maxSdkVersion=2147483647) certificate SHA-256 digest: '+OTHER+'\n'
        with self.assertRaises(ValueError): signer_certificate(text)
    def test_numbered_single_signer(self): self.assertEqual(CERT, signer_certificate(LEGACY))
    def test_one_certificate_across_sdk_ranges_is_not_multiple_signers(self):
        self.assertEqual(CERT, signer_certificate(RANGED))
    def test_distinct_rotation_certificates_are_not_silently_accepted(self):
        with self.assertRaises(ValueError): signer_certificate(RANGED.replace(CERT, OTHER, 1))
    def test_multiple_signer_report_cannot_pass_even_with_one_printed_cert(self):
        with self.assertRaises(ValueError): signer_certificate(LEGACY.replace('signers: 1','signers: 2'))
    def test_source_stamp_certificate_is_not_apk_signer(self):
        with self.assertRaises(ValueError): signer_certificate('Number of signers: 1\nSource Stamp Signer certificate SHA-256 digest: '+CERT+'\n')
    def test_truncated_digest_cannot_pass(self):
        with self.assertRaises(ValueError): signer_certificate(LEGACY.replace(CERT,'aabb'))
    def test_real_instrumentation_header_may_have_no_version(self):
        # Observed with aapt36 on this project's built App/Host instrumentation APKs.
        self.assertEqual(dict(package='io.github.juhjuhx.kuyin.test',code='',name=''),
            package_header("package: name='io.github.juhjuhx.kuyin.test' versionCode='' versionName='' platformBuildVersionName='16'\n"))
    def test_app_version_remains_explicit(self):
        self.assertEqual(dict(package='io.github.juhjuhx.kuyin',code='20006',name='0.2.0-f3.1-test'),
            package_header("package: name='io.github.juhjuhx.kuyin' versionCode='20006' versionName='0.2.0-f3.1-test'\n"))
    def test_duplicate_or_missing_package_headers_cannot_pass(self):
        for text in ['', "package: name='a' versionCode='1' versionName='v'\n"*2]:
            with self.assertRaises(ValueError): package_header(text)

if __name__ == '__main__': unittest.main()
