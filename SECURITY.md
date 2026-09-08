# Security Policy

## Zero-Network Policy

This keyboard operates with a **strict zero-network policy**. The IME does not
send any keystrokes, candidate data, or user dictionary contents over the
network. All processing happens entirely on-device.

- No analytics or telemetry
- No remote dictionary downloads at runtime
- No cloud-based prediction or suggestion
- User dictionaries are stored locally and never uploaded

If a future release needs network access for a specific, limited purpose (e.g.
optional dictionary update from a trusted source), the feature will be
opt-in with a clear disclosure, and the zero-network default will remain
unchanged for all core input functionality.

## Reporting a Vulnerability

If you discover a security vulnerability in this project, please report it
responsibly.

**Do not open a public GitHub issue for security vulnerabilities.**

Instead, please use
[GitHub Security Advisories](https://github.com/nickslin/android-keyboard/security/advisories/new)
to report the issue privately. This ensures the vulnerability can be triaged
and addressed before public disclosure.

### What to Include

- A description of the vulnerability and its potential impact
- Steps to reproduce or a proof of concept
- The component or area of the codebase affected
- Any suggested mitigation (if you have one)

### Response Commitment

- **Acknowledgement**: within 72 hours of report submission
- **Triage**: within 1 week — we will confirm the issue and assign a severity
- **Fix or mitigation**: critical issues targeted within 2 weeks; others in
  the next regular release
- **Disclosure**: we will coordinate with the reporter on timing before any
  public disclosure

## Scope

This security policy covers the Android keyboard IME application itself,
including the IME service, input processing pipeline, user dictionary
storage, and build/distribution configuration.

Third-party dependencies (libchewing, OpenCC, etc.) are out of scope for
direct reporting — please report vulnerabilities in those libraries to their
respective maintainers.

## Supported Versions

| Version | Supported |
|---------|-----------|
| 0.1.x   | Yes       |
| < 0.1   | No        |
