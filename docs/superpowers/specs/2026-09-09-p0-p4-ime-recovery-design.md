# KU-Yin P0–P4 IME Recovery Design

> Recovery branch: `fix/p0-p4-ime-recovery`
> Baseline: `74ef9039c4fd51e976a6f9dfbedbe51895f59d5c`
> Date: 2026-09-09

## Goal

Turn KU-Yin from a buildable but unusable Android IME prototype into a reproducible, testable open-source input-method baseline whose core keyboard, Bopomofo composition, candidate selection, lifecycle, native dependency handling, CI and documentation agree with each other.

## Non-goals for this recovery pass

- No Compose migration.
- No broad AGP/Kotlin modernization while core IME behavior is being repaired.
- No cloud typing, telemetry or network-backed prediction.
- No claim that OpenCC conversion is implemented until a real converter is wired and tested.
- No claim that a release is production-ready without device/emulator smoke evidence.

## Architecture

```text
Android InputMethodService
        |
        v
KeyAction / KeyboardLayout
        |
        v
ChewingEngine (platform-neutral contract)
        |
        v
AndroidChewingEngine
        |
        v
JNI bridge -> pinned libchewing
        |
        +--> preedit / candidates / commit buffer
        |
        v
ImeEditorBridge -> InputConnection
        |
        +--> setComposingText
        +--> commitText
        +--> finishComposingText
```

UI rendering stays in the existing Kotlin `View`/`Canvas` stack. Logical key behavior is separated from labels so that glyph changes cannot silently change input semantics.

## P0 — Make typing real

1. Correct the Dachen/Standard physical-key mapping against pinned libchewing.
2. Introduce explicit `KeyAction` values for Backspace, Space, Dismiss and input keys.
3. Fix keyboard hit boxes by rebuilding slots after size changes.
4. Make CandidateView report the candidate index, not only the rendered string.
5. Synchronize native preedit to `InputConnection.setComposingText()`.
6. Read and commit libchewing's commit buffer when the native keystroke result indicates commit.
7. Finish composing state when preedit becomes empty.
8. Make native library absence explicit and non-crashing.
9. Add regression tests for mapping and pure input-state decisions before production fixes.

Acceptance: key geometry is non-zero after measurement; Backspace does not insert `⌫`; Space is ASCII 32; Dachen physical mapping matches upstream; editor composition/commit decisions are unit-testable.

## P1 — Native/libchewing reproducibility

1. Pin libchewing source revision in a machine-readable manifest.
2. Replace the current warning-only CMake behavior with fail-fast checks.
3. Provide deterministic bootstrap scripts for Linux/macOS shell and Windows PowerShell.
4. Build four Android ABIs (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`) from the pinned source.
5. Stage headers/native archives under a generated, gitignored directory.
6. Stage required system dictionary data and initialize libchewing with explicit app-private paths.
7. Keep generated binaries out of source control.

Acceptance: a missing native dependency fails before APK packaging; CI reconstructs native inputs from the pin instead of relying on a developer workstation cache.

## P2 — Lifecycle, privacy and failure containment

1. Reset decoder/editor composition coherently on input finish/restart.
2. Re-apply configured layout after reset instead of silently falling back.
3. Remove per-keystroke logging and avoid logging text/candidates.
4. Disable Android application backup for the IME baseline to reduce accidental persistence of sensitive state.
5. Handle null `InputConnection` and native-not-ready states without crashing.
6. Keep converter behavior explicit: pass-through is allowed, but the UI/docs must not claim OpenCC conversion is already functional.

Acceptance: lifecycle paths do not leak typed content to logs; failure paths are explicit; reset does not mutate the saved layout policy.

## P3 — Tests, CI and release gates

CI must run from a clean checkout and include:

- JDK 17 / Android SDK / NDK setup.
- Rust toolchain plus Android Rust targets.
- libchewing bootstrap.
- JVM unit tests.
- native build.
- debug APK build.
- release APK build only when signing-independent configuration allows it.
- artifact upload with failure on missing APK.

Repository tests cover at minimum:

- Standard/Dachen physical mapping.
- Space/Backspace/Dismiss action semantics.
- editor synchronization decision logic.
- native keystroke bitmask interpretation.

Device/emulator smoke remains an explicit release gate; CI build success alone is not represented as runtime proof.

## P4 — Open-source project surface

1. Rewrite README around actual project state, build path, limitations and install/enable workflow.
2. Rewrite/update architecture and roadmap documents.
3. Add `CONTRIBUTORS.md` with human ownership, AI-assisted contribution ledger and upstream acknowledgements.
4. Record this recovery in `CHANGELOG.md` and a dedicated recovery report.
5. Keep `NOTICE` / `UPSTREAM` aligned with fcitx5-android, fcitx5-chewing, libchewing, OpenCC and other borrowed references actually used.
6. Replace the stale architecture SVG with a readable current architecture diagram.
7. Add a project visual mark suitable for README/social preview. If the ChatGPT image-generation runtime is unavailable in the execution environment, a repository-native SVG mark is used as the deterministic fallback and is labeled accordingly.

## Contribution attribution policy

Authorship is not inferred from code quality. Material research, scaffolding, documentation or implementation work is credited even when later corrected.

The project owner requested the following AI-assisted contributors to be recorded:

- OpenCode using **Mules Spark 1.3** — early project scaffolding/documentation work.
- Hermes using **Agnes 2.0 Dash Flash** — early architecture, implementation and documentation work.
- ChatGPT / GPT-5.6 Sol — P0–P4 forensic review, recovery design, implementation, tests, CI and documentation pass.

These entries are recorded as AI-assisted tooling/model contributions, not as human GitHub identities and not as copyright ownership claims.

## Security boundary

KU-Yin is an IME and therefore handles high-sensitivity text. The recovery baseline follows: no telemetry, no network typing service, no plaintext keystroke logs, generated/native artifacts from pinned upstreams, explicit failure on missing native dependencies, and conservative `InputConnection` interactions.
