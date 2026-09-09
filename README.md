# KU-Yin Keyboard for Android

KU-Yin is an open-source Android Bopomofo IME focused on a local-first, privacy-conscious input path. The current implementation uses Kotlin/View for the Android UI, JNI/C++ for the native bridge, and libchewing for Bopomofo decoding.

> Current project state: **static build verified; Android runtime verification pending**.

See [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md) for the factual current state and [`docs/NEXT_STEPS.md`](docs/NEXT_STEPS.md) for the immediate plan.

## What currently exists

- Android `InputMethodService`
- Dachen (大千) Bopomofo keyboard surface
- ASCII fallback surface with digits, Shift, Space, Backspace and Enter
- candidate UI and composition synchronization through `InputConnection`
- `EditorPolicy` and `ImeSessionController` boundaries for editor/session behavior
- JNI bridge to pinned libchewing native artifacts
- app-private libchewing dictionary installation
- Debug and Release APK builds in GitHub Actions
- deterministic JVM tests and lightweight source/build contracts

## Current verification status

The latest verified recovery head is documented in `docs/PROJECT_STATUS.md`. The current CI path verifies:

```text
source/build contracts
        ↓
JDK 17 + Android SDK + Gradle 7.6.4
        ↓
pinned native dependency bootstrap
        ↓
JVM tests
        ↓
assembleDebug
        ↓
assembleRelease
        ↓
Debug / Release APK artifacts
```

This proves the repository builds cleanly in GitHub Actions. It does **not** yet prove that the IME is release-ready on a real Android runtime.

## Architecture

```text
Android framework
    │
    ▼
ChewingInputMethodService
    │
    ├── EditorPolicy
    ├── ImeSessionController
    ├── KeyboardView / CandidateView
    └── ChewingEngine
            │
            ▼
    AndroidChewingEngine
            │
            ▼
       JNI / libchewing
```

The Android service is intended to remain an adapter: editor/session decisions live in pure Kotlin policy/controller code, and native decoding stays behind the `ChewingEngine` boundary.

## Privacy / capability notes

- The application does not request the Android `INTERNET` permission.
- libchewing user data is stored under app-private storage.
- personalized-learning policy can be disabled for sensitive/no-learning editor sessions.
- Android backup is disabled in the current recovery branch.
- OpenCC conversion is **not** currently a production feature; the pass-through stub no longer presents itself as a working backend.

See [`SECURITY.md`](SECURITY.md) for the supported guarantees and known limitations.

## Build

The authoritative build path is GitHub Actions. For local build notes and exact commands, see [`BUILD.md`](BUILD.md).

## Current limitations

The following are intentionally outside the current static-recovery completion claim:

- Android runtime / physical-device IME verification
- API 36 toolchain migration
- OpenCC production integration
- Hsu / Eten26 completed visual keyboard layouts
- per-key accessibility virtual nodes
- full symbol/emoji system
- clipboard, gesture typing and prediction features

Google Play requires new apps and app updates to target Android 16 (API 36) starting 2026-08-31. The current project still uses API 33 and will migrate in a dedicated compatibility pass after runtime recovery is closed.

## Development documents

Current documents, in order of authority:

1. [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md)
2. [`docs/NEXT_STEPS.md`](docs/NEXT_STEPS.md)
3. [`docs/superpowers/plans/2026-09-09-r4-closing-pass.md`](docs/superpowers/plans/2026-09-09-r4-closing-pass.md)
4. [`docs/superpowers/specs/2026-09-09-recovery-v2-stable-kernel-design.md`](docs/superpowers/specs/2026-09-09-recovery-v2-stable-kernel-design.md)

Older P0-P4 recovery files are retained as historical implementation records.

## Attribution

Project and AI-assisted contribution history is recorded in [`CONTRIBUTORS.md`](CONTRIBUTORS.md). Upstream/reference projects and code must retain their original license/provenance requirements when reused.

## License

See [`LICENSE`](LICENSE) and repository notices for licensing details.
