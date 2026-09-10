# Security Policy

KU-Yin is an Android input method. Security and privacy claims in this document are limited to behavior that is implemented or directly observable in the current recovery branch.

## Verified / implemented properties

- The application does not request the Android `INTERNET` permission.
- libchewing dictionaries and user data are handled locally.
- user dictionary data is stored under app-private storage.
- Android application backup is disabled in the current recovery branch.
- editor policy distinguishes sensitive/password input and no-personalized-learning sessions.
- the native libchewing adapter exposes personalized-learning enable/disable control for session policy.
- the current build uses pinned upstream native/prebuilt revisions rather than floating branch names.

These properties reduce data-exfiltration and accidental-learning risk, but they do not substitute for runtime testing on Android devices.

## Current limitations

The following should **not** be interpreted as completed security guarantees yet:

- runtime verification of password/FORCE_ASCII behavior is still pending
- runtime verification of `IME_FLAG_NO_PERSONALIZED_LEARNING` behavior is still pending
- physical-device and OEM-specific IME lifecycle testing is still pending
- native staged artifacts do not yet have a repository-maintained per-file SHA-256 manifest/rebuild-comparison gate
- OpenCC is not a production backend
- there is no clipboard feature in the current recovery scope
- Direct Boot support has not been verified as a release guarantee
- OTP-specific behavior is not currently claimed as a verified feature

## Sensitive input policy

The current architecture routes `EditorInfo` through `EditorPolicy` / `ImeSessionController` so sensitive fields can use an ASCII-oriented session and disable personalized learning.

This policy is considered **statically implemented**. It becomes a release-level guarantee only after the runtime checklist in `docs/NEXT_STEPS.md` is completed.

## Dependency / supply-chain model

Native dependencies are bootstrapped from pinned upstream revisions by `scripts/bootstrap_native_deps.sh`. Missing native ABI artifacts fail the build instead of silently creating a potentially unusable APK.

A later hardening pass should add explicit per-file hashes and provenance/reproducibility documentation.

## Reporting a vulnerability

If you discover a security or privacy issue, prefer a private GitHub Security Advisory when the repository Security tab supports private reporting. Avoid posting sensitive exploit details in a public issue before maintainers have had a chance to review them.

No guaranteed response-time SLA is currently published by this project.
