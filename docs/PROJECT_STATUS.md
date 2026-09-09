# KU-Yin Project Status

> Last verified branch: `fix/p0-p4-ime-recovery`
> Verified head: `7fab4442ba3f544acd12e8c12be6a033249af552`
> Verified GitHub Actions run: `#63` (`34359833239`)
> Status date: 2026-09-09

## Current state

KU-Yin is no longer in scaffold-only status. The current recovery branch contains a working Android IME project with Kotlin/View UI, JNI, pinned libchewing native dependencies, deterministic JVM tests, and Debug/Release APK generation in GitHub Actions.

Repository-side static verification is currently GREEN.

### Verified static gates

- Dachen source contract
- keyboard action contract
- editor synchronization contract
- P1 build/native contract
- P2 UI contract
- P3 architecture/security contract
- JDK 17 setup
- Android SDK setup
- Gradle 7.6.4 setup
- pinned native dependency bootstrap
- `testDebugUnitTest`
- `assembleDebug`
- `assembleRelease`
- Debug APK artifact upload
- Release APK artifact upload

### Latest artifacts

| Artifact | Size | Workflow digest |
|---|---:|---|
| `debug-apk` | 9,809,947 bytes | `sha256:5f96003360b62fc318fb2c2a3166908a88ec3da68f6aae51e9cdcea8f0ed068d` |
| `release-apk` | 8,632,454 bytes | `sha256:7db202a94f17012b2934ed069ea9475dc4bf4e3e16a41c4caef8edfa3699afda` |

These digests identify the GitHub Actions artifact archives. They are not a replacement for a future signed-release APK checksum manifest.

## Current architecture

```text
Android InputMethodService
        │
        ├── EditorPolicy
        │      └── privacy / ASCII / editor-action decisions
        │
        ├── ImeSessionController
        │      └── active keyboard/session policy
        │
        ├── KeyboardView / CandidateView
        │      └── View/Canvas rendering
        │
        └── ChewingEngine
               │
               └── AndroidChewingEngine
                        │
                        └── JNI / libchewing
```

Native dictionaries are bootstrapped from pinned upstream artifacts and installed into app-private storage before libchewing initialization.

## Completed recovery areas

### P0 / R1-R4 static recovery

- corrected Dachen mapping
- fixed special-key dispatch and touch hitboxes
- synchronized preedit / commit output with `InputConnection`
- candidate selection uses native candidate indices
- clean native dependency bootstrap
- Debug and Release APK generation
- removed the dangling round-icon resource
- OpenCC pass-through stub no longer presents itself as a working conversion backend
- added `EditorPolicy`
- added `ImeSessionController`
- added Dachen / ASCII session surfaces
- added ASCII digits, Shift, Space, Backspace, Enter
- wired editor actions
- wired `onUpdateSelection()` reconciliation
- wired libchewing personalized-learning policy control

## Not yet verified dynamically

Repository-side GREEN does **not** prove the IME is ready for release. The following still require Android runtime evidence:

- APK installation on an emulator or physical device
- Android IME registration / enable / switch
- rendered keyboard visibility
- Dachen composition and candidate commit
- ASCII / Shift / digits / Enter interaction
- password and `IME_FLAG_FORCE_ASCII` behavior
- `IME_FLAG_NO_PERSONALIZED_LEARNING` behavior at runtime
- cursor movement / composition reconciliation
- service restart / app-switch smoke
- crash-free repeated input

Until these are verified, the project should be described as **static-build verified, runtime verification pending**.

## Known limitations / deferred work

- `targetSdk` and `compileSdk` are currently API 33. Google Play requires API 36 for new apps and updates starting 2026-08-31; migration is intentionally isolated from the current recovery pass.
- OpenCC conversion is not a production feature yet.
- Hsu / Eten26 visual layouts are not currently exposed as completed user-facing layouts.
- per-key accessibility virtual nodes are deferred beyond R4.
- full symbol/emoji system, clipboard, gesture typing, prediction and Compose migration are out of the current recovery scope.

## Authoritative planning documents

Use these in order:

1. `docs/PROJECT_STATUS.md` — current factual state.
2. `docs/NEXT_STEPS.md` — immediate next work.
3. `docs/superpowers/plans/2026-09-09-r4-closing-pass.md` — R4 closeout checklist.
4. `docs/superpowers/specs/2026-09-09-recovery-v2-stable-kernel-design.md` — stable-kernel architecture rationale.

Older P0-P4 plans remain historical records and should not be used as the current status source.
