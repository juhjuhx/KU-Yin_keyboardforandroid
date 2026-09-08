# Security Policy

## Privacy-First Philosophy

android-keyboard treats user privacy as the highest guiding principle. As an Input Method Editor (IME), we commit to the following Privacy by Design principles:

1. **Zero Internet Permission**: This project does not request the INTERNET permission. No data leaves your device.
2. **Dictionary stays in-process**: All decoding, dictionary memory, and user dictionary are completed within the local process -- no cloud sync.
3. **Clipboard text-only**: When intercepting and processing the clipboard, only plain text is extracted to prevent rich text or malicious payloads from leaking.
4. **Supply-chain security**: All upstream dependencies use submodule SHA pinning to prevent dependency poisoning.
5. **Permission isolation**: Uses a standalone applicationId and FileProvider to avoid installation conflicts.

## Reporting a Vulnerability

If you discover a security vulnerability or privacy risk, do not discuss it in public GitHub Issues.

Please report via:
1. **GitHub Security Advisories**: Submit a private report at the repo Security tab
2. **Email**: Encrypted email (PGP key recommended)

## Response Commitment

- **Initial acknowledgement**: Within 48 hours of receiving the report.
- **Fix timeline**:
  - Critical/High: Emergency hotfix released within 7 days.
  - Medium/Low: Fixed in the next regular Wave iteration.
- **Public disclosure**: Coordinated with the reporter after the fix is released.

## Known Security Features

- Supports Direct Boot (follows upstream secure behaviour).
- Disables candidate display in password fields (prevents password character leakage).
- Disables keyboard rebuild on OTP multi-field input (prevents crash/flashing).
