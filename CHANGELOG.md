# Changelog

## [0.1.2-alpha] - 2026-09-13

### P0 real-device fixes

- Restored visible Dachen/Zhuyin preedit on real devices by surfacing libchewing's current bopomofo buffer and merging it with the composition buffer at the native cursor position.
- Added regression coverage for visible phonetic preedit so `consumed=true` with an empty preedit no longer counts as a successful input transition.
- Fixed the Settings screen dark-mode contrast by adding an explicit night-qualified app theme.
- Added a static UI contract guarding the night theme against accidental fallback to the light window background.

### Verification

- Core CI contracts, native bootstrap, JVM tests, Debug APK, and unsigned Release APK build successfully.
- Android instrumentation confirms Dachen key `1` surfaces `ㄅ` in preedit.
- Install/register/enable/select succeed on Android 13 emulator.
- The headless emulator IME-window-visible assertion remains experimental/non-blocking and is not used as evidence that physical-device UI has been verified.

### Release status

- Bumped application version to `0.1.2-alpha` / versionCode 3.
- Debug APK is intended for direct device testing.
- Release APK remains unsigned and is a developer artifact, not a production-signed package.

## [0.1.1-alpha] - 2026-09-10

### Repository / release

- Consolidated public documentation into a small authoritative docs tree.
- Rewrote README around upstream provenance, usage, architecture, security, roadmap and acknowledgements.
- Added a single iteration report covering the scaffold → P0–R4 recovery → mainline path.
- Added security/privacy/performance/supply-chain audit documentation.
- Added safe upstream monitoring for `fcitx5-android/prebuilt` and official Codeberg libchewing releases.
- Bumped application version to `0.1.1-alpha` / versionCode 2.
- Prerelease workflow now derives the GitHub tag and asset names from `versionName`.

### Existing recovered functionality carried forward

- Dachen Zhuyin + libchewing decoding.
- composition/candidate synchronization via `InputConnection`.
- ASCII/password sessions, Shift/digits/Space/Backspace/Enter/editor actions.
- selection reconciliation and no-personalized-learning policy.
- deterministic four-ABI native bootstrap and Debug/unsigned Release builds.

### Known alpha limitations

- production release signing not configured.
- headless IME-window-visible smoke remains experimental/non-blocking.
- target/compile SDK remain API 33 pending a separate modernization pass.
- OpenCC, Hsu/Eten26, full accessibility, symbols/Emoji, clipboard, gesture typing and prediction are not complete production features.

## [0.1.0-alpha] - 2026-09-10

First recoverable alpha after the P0–R4 repair pass. Build/install/IME register/enable/select flow was established and published with Debug and unsigned Release APK artifacts.

## Historical scaffold

Earlier experimental work prior to the recovery is preserved in Git history. Some historical documents described planned features that were never shipped; current README/docs are authoritative.
