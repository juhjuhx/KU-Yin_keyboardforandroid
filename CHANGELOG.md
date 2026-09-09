# Changelog

All notable project changes should be recorded here. This file describes implemented repository behavior only; planned features belong in `docs/NEXT_STEPS.md` or roadmap/design documents.

## [Unreleased]

### Added

- Dachen (大千) Bopomofo keyboard surface aligned with the pinned libchewing standard layout.
- ASCII fallback keyboard surface with digits, Shift, Space, Backspace and Enter.
- `EditorPolicy` for input-type / IME-option decisions.
- `ImeSessionController` for keyboard/session policy.
- editor-action handling and selection reconciliation.
- app-private libchewing dictionary installation.
- deterministic GitHub Actions build with Debug and Release APK artifacts.
- regression tests for Dachen mapping, keyboard mode, OpenCC capability truthfulness, editor policy and session behavior.

### Changed

- native dependency handling now uses pinned upstream artifacts and fails the build when required ABI inputs are missing.
- composition/preedit and committed text are synchronized through `InputConnection`.
- candidate selection is routed through the native candidate index.
- OpenCC pass-through code no longer presents simplified/traditional conversion as an available production capability.
- Android backup is disabled in the recovery branch.
- README/build/security documentation now distinguishes static build success from Android runtime readiness.

### Fixed

- corrected incorrect Dachen key mappings and missing punctuation key mapping.
- corrected Space key ASCII handling.
- corrected Backspace/special-key dispatch semantics.
- rebuilt key hitboxes after view-size changes.
- removed a dangling `roundIcon` manifest resource that broke AAPT2 processing.
- prevented native-load failure from being described as a working fallback state.
- fixed an ASCII-layout enum initialization issue exposed by JVM compilation.

### Security / Privacy

- no Android `INTERNET` permission is requested.
- sensitive/no-learning editor policy can disable libchewing personalized learning.
- libchewing user data remains in app-private storage.

### Verification

The current recovery branch has passed repository-side contracts, JVM tests, native bootstrap, `assembleDebug`, `assembleRelease`, and artifact upload. Android runtime/device verification is still pending and is **not** claimed as completed here.

## [0.1.0] - 2026-09-08

Initial experimental/scaffold work. Historical documentation from this period may describe architecture or features that were later replaced during the recovery work.
