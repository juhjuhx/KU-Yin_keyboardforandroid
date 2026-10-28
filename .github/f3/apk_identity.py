"""Fail-closed parsers for public Android SDK identity reports."""
import re


def signer_certificate(text):
    counts = re.findall(r'^Number of signers: (\d+)\s*$', text, re.MULTILINE)
    if counts != ['1']:
        raise ValueError('Expected exactly one reported APK signer')
    labels = r'(?:#\d+|\(minSdkVersion=\d+, maxSdkVersion=\d+\))'
    lines = [line for line in text.splitlines() if line.startswith('Signer ') and 'certificate SHA-256 digest:' in line]
    matches = [re.fullmatch(r'Signer '+labels+r' certificate SHA-256 digest: ([^\r\n]+)', line) for line in lines]
    if any(match is None for match in matches):
        raise ValueError('Unknown APK signer label requires review')
    digests = [match[1] for match in matches]
    certificates = [digest.replace(':', '').lower() for digest in digests]
    if not certificates or any(not re.fullmatch(r'[0-9a-f]{64}', value) for value in certificates):
        raise ValueError('Missing or malformed APK signer SHA-256')
    if len(set(certificates)) != 1:
        raise ValueError('Different SDK-range certificates require separate rotation review')
    return certificates[0]


def package_header(text):
    headers = re.findall(r'^package:.*$', text, re.MULTILINE)
    if len(headers) != 1:
        raise ValueError('Expected exactly one aapt package header')
    match = re.match(r"package: name='([^']+)' versionCode='([^']*)' versionName='([^']*)'(?: |$)", headers[0])
    if not match:
        raise ValueError('Malformed aapt package header')
    return dict(zip(('package', 'code', 'name'), match.groups()))
