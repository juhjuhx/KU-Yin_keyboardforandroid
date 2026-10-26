# Changelog

## 未發行：公開文件與素材收束

README重新區分公開Alpha／main／Draft；原Hero僅修標籤底框溢出。新增同一視覺系統的七個唯一社群版型與來源／Alt／SHA，更新維護文案與Gallery證據限制；沒有產品功能、版本、簽章或舊Release變更。

## Unreleased — R3.8 engineering closure

- Bound Host accessibility observations and preserve primary/cleanup errors; no click retries or weakened product assertions.
- Keep offline H0 readiness separate from Android health evidence.
- Make manual Alpha CI artifact-only; separate opt-in Runtime failure from build success. Pin Actions and wrapper checksum, add redacted credential tripwire.
- Restore standard LGPL2.1 document without changing the existing license choice. Preserve all frozen Alpha assets.
- Consolidate current docs, provenance and maintenance backlog. No new RC/Stable or typing feature.


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
