# Contributing to KU-Yin

KU-Yin is an Android Bopomofo IME using Kotlin/View, JNI/C++ and libchewing. The current project is in recovery/validation rather than broad feature expansion, so contributions should keep changes focused and preserve diagnosable build/test boundaries.

## Before contributing

Read, in order:

1. `docs/PROJECT_STATUS.md`
2. `docs/NEXT_STEPS.md`
3. `docs/superpowers/plans/2026-09-09-r4-closing-pass.md`

Older scaffold/recovery plans are historical context and may no longer represent the current implementation.

## Current scope

Good contribution targets include:

- reproducible Android runtime/IME bug reports
- Dachen input correctness
- session/editor-policy correctness
- JNI/libchewing integration fixes
- focused tests for existing behavior
- documentation corrections
- build/reproducibility hardening

Please avoid bundling unrelated toolchain upgrades, UI redesigns and new feature families into bug-fix PRs.

## Toolchain

The verified CI build currently uses:

- JDK 17
- Gradle 7.6.4
- Android SDK / API 33 project configuration
- Android NDK `27.3.13750724`
- CMake 3.22.1 for the app native build
- pinned native inputs staged by `scripts/bootstrap_native_deps.sh`

See `BUILD.md` for the reference build sequence.

## Testing

For behavior changes, add or update the narrowest useful regression test first.

Reference checks:

```bash
python3 scripts/check_dachen_contract.py
python3 scripts/check_p0_keyboard_contract.py
python3 scripts/check_editor_sync_contract.py
python3 scripts/check_p1_build_contract.py
python3 scripts/check_p2_ui_contract.py
python3 scripts/check_p3_architecture_security_contract.py
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest --stacktrace
gradle assembleDebug --stacktrace
gradle assembleRelease --stacktrace
```

The lightweight Python checks are preflight/integration contracts. JVM tests are the behavioral source of truth for Kotlin logic.

Android runtime/device verification is tracked separately; do not describe an APK as runtime-ready merely because `assembleDebug` succeeds.

## Pull requests

Keep PRs small enough that one failure can be traced to one logical change. A good PR description should include:

- problem/root cause
- intended behavior
- tests added or updated
- static build evidence
- runtime evidence when the change affects IME lifecycle/input behavior
- known limitations or follow-up work

## Commit style

Conventional-commit style is preferred:

```text
fix(input): correct selection reconciliation
test(session): cover password editor policy
docs: reconcile runtime verification status
chore(ci): pin a build dependency
```

## Third-party code and licenses

Do not copy implementation code from another keyboard/project without checking the exact file/module license and recording provenance. Architecture and behavior patterns may be studied without copying source.

The current production decoder dependency is libchewing; fcitx5-android, FlorisBoard, HeliBoard and other keyboards are primarily reference/donor research sources unless explicit license/provenance review approves source reuse.

## Security-sensitive changes

IME code handles sensitive text. Avoid:

- network-backed typing paths
- keystroke/content logging
- storing plaintext user input outside the intended private dictionary/session path
- claiming password/no-learning protection without runtime evidence

See `SECURITY.md`.

## License

By contributing, you agree that your contribution is licensed under the repository's project license and any applicable upstream obligations.
