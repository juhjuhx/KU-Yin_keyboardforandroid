# KU-Yin Project Status

> Recovery branch: `fix/p0-p4-ime-recovery`
> Latest completed runtime evidence: GitHub Actions run `#85` (`34375498331`)
> Status date: 2026-09-10

## Current state

KU-Yin is an Android IME alpha project with Kotlin/View UI, JNI, pinned libchewing native dependencies, deterministic JVM tests, and Debug/Release APK generation in GitHub Actions.

The repository-side build chain is GREEN. Android 13 emulator evidence now confirms that the Debug APK installs successfully, Android registers KU-Yin as an IME, `ime enable` succeeds, and KU-Yin can be selected as the default input method.

The remaining automated runtime failure is narrower: the headless emulator test does not observe the KU-Yin input window becoming visible after launching the test editor. For that reason, runtime-smoke is tracked as an experimental non-blocking job while the failure remains visible in Actions.

## Verified gates

- Dachen source contract
- keyboard action contract
- editor synchronization contract
- P1 build/native contract
- P2 UI contract
- P3 architecture/security contract
- JDK 17 / Android SDK / Gradle 7.6.4 setup
- pinned native dependency bootstrap
- `testDebugUnitTest`
- `assembleDebug`
- `assembleRelease`
- Debug and Release artifact upload
- Debug APK installation on Android 13 emulator
- Android IME registration
- IME enable
- IME selection as default input method
- libchewing Android-runtime initialization test

## Runtime item still open

- Headless emulator assertion: `KU-Yin input window should become visible`
- Full rendered-keyboard interaction on physical/OEM devices
- Repeated real-world app switching, rotation, and long-running input sessions across OEM builds

The current alpha must not be described as fully device-certified or production-ready.

## Current architecture

```text
Android InputMethodService
        │
        ├── EditorPolicy
        ├── ImeSessionController
        ├── KeyboardView / CandidateView
        └── ChewingEngine
               └── AndroidChewingEngine
                      └── JNI / libchewing
```

## Completed recovery areas

- corrected Dachen mapping
- fixed special-key dispatch and touch hitboxes
- synchronized preedit / commit output with `InputConnection`
- candidate selection through native candidate indices
- pinned native dependency bootstrap for four ABIs
- Debug and Release APK generation
- truthful OpenCC capability state
- `EditorPolicy` and `ImeSessionController`
- ASCII/password input surface with digits and Shift
- editor-action routing
- selection reconciliation
- libchewing personalized-learning policy control
- runtime harness for install/register/enable/select and Android instrumentation

## Known limitations / deferred work

- `targetSdk` and `compileSdk` remain API 33 and need a separate modernization pass.
- Production release signing is not configured; the release APK produced by CI is unsigned.
- OpenCC conversion is not a completed production feature.
- Hsu / Eten26 visual layouts are not exposed as completed user-facing layouts.
- per-key accessibility virtual nodes remain deferred.
- full symbol/emoji system, clipboard, gesture typing, prediction, and Compose migration remain outside this recovery pass.

## Release policy

`v0.1.0-alpha` is an early preview. The GitHub prerelease publishes a debug-signed APK for direct testing, an unsigned release-build artifact for developers, and `SHA256SUMS.txt`. Release signing will be introduced separately before any stable release claim.

## Authoritative documents

1. `README.md` — public project landing page.
2. `APK/README.md` — current package/release semantics.
3. `docs/PROJECT_STATUS.md` — current factual engineering state.
4. `docs/NEXT_STEPS.md` — immediate engineering follow-up.
5. `SECURITY.md` — security and privacy boundaries.
