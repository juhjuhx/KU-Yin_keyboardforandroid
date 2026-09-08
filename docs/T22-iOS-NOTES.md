# T22: iOS Port Notes — App Group & Extension Constraints

> Wave 4 T22 | 2026-09-08 | Analysis & spec only — no code changes
> Status: DONE (documentation)

---

## 1. Core Constraint (App Group)

iOS IME must be published as a Keyboard Extension (com.apple.keyboard).
Cannot directly read main App UserDefaults. Shared data requires App Group.

## 2. libchewing on iOS

libchewing is a pure C library. Can be packaged via CocoaPods or SPM.
- Codeberg main repo: https://codeberg.org/chewing/libchewing
- iOS port reference: chewing-tui (Swift wrapper)
- Recommended: static link libchewing_capi.a to avoid dylib signing issues

## 3. OpenCC on iOS

OpenCC has official Swift wrapper or C API可直接包裝.
Default profile: s2tw.json / tw2s.json (Taiwan-specific vocabulary)
Fallback: s2t.json / t2s.json

## 4. User Dictionary on iOS

Storage: FileManager.default.containerURL(forApplicationGroupIdentifier:)
Filename: user_phrase.dat (same semantics as Linux libchewing)
Sync: Main App and Extension share same App Group directory

## 5. Known Limitations

| Limitation | Impact | Mitigation |
|---|---|---|
| Extension has no background execution | Dict backup cannot auto-run | Main App triggers backup; Extension only reads/writes |
| Extension has no network permission | Cannot download OpenCC profiles at runtime | Bundle profiles into Extension |
| Apple review is strict | IME may require manual enable | Guide user to Settings > Keyboard > Add |
| Dynamic code loading prohibited | Cannot hot-reload dictionary | Use App Group filesystem sync |

## 6. Mapping to Android engines/core

| Android | iOS |
|---|---|
| engines/core/ChewingEngine.kt | Engines/Core/ChewingEngine.swift (protocol) |
| engines/android/AndroidChewingEngine.kt | Engines/iOS/iOSChewingEngine.swift |
| engines/core/ChineseConverter.kt | Engines/Core/ChineseConverter.swift (protocol) |
| engines/opencc/OpenCCConverter.kt | Engines/iOS/iOSOpenCCConverter.swift |
| engines/core/IMEConfig.kt | Engines/Core/IMEConfig.swift |

_This document is T22 output. No code generated._