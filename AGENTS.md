# Repository Guidelines

## Overview

**android-keyboard** - Android Bopomofo (注音) IME built on libchewing (C) with OpenCC for Traditional/Simplified toggle. Self-drawn Kotlin UI, no Jetpack compose. Forked from fcitx5-android (LGPL-2.1).

**Current version**: 0.1.0-alpha | **License**: LGPL-2.1

---

## Project Structure

```
android-keyboard/
+-- app/src/main/
|   +-- java/com/example/androidkeyboard/
|   |   +-- input/                 # IMS, KeyboardView, KeyboardLayout, SymbolPicker, KeyMapping
|   |   +-- ui/                    # CandidateView (candidate popup)
|   |   +-- engines/
|   |   |   +-- core/              # ChewingEngine.kt, ChineseConverter.kt, IMEConfig.kt
|   |   |   +-- android/           # AndroidChewingEngine.kt (JNI external funs)
|   |   |   +-- opencc/            # OpenCCConverter.kt (stub)
|   +-- cpp/
|   |   +-- CMakeLists.txt         # Builds libchewing-jni.so from legacy C sources
|   |   +-- chewing_jni.cpp        # JNI bridge - 14 external funs
|   |   +-- include/chewing/       # Header stubs (filled by CMake configure)
|   |   +-- libchewing-src/        # Cloned libchewing C source tree (gitignored)
|   +-- res/                       # Layouts, colors, themes, input_method.xml
+-- docs/
|   +-- CHECKS/                    # Per-wave audit logs (wave0-wave4)
|   +-- diagram/                   # architecture.svg, landing.html
|   +-- JNI-INTEGRATION.md         # How to activate real JNI (stub to real)
|   +-- ROADMAP.md                 # Feature roadmap
|   +-- T*.md                      # Per-task documentation
+-- gradle/wrapper/                # Gradle 8.5 wrapper
+-- build.gradle.kts               # Top-level: AGP 7.4.2, Kotlin 1.9.22
+-- settings.gradle.kts
```

---

## Build and Run

```
# Prerequisites: Java 8+, Android SDK, NDK r25c+, CMake 3.18+
# Do NOT set JAVA_HOME to a non-existent path (causes build failure)

.\gradlew :app:externalNativeBuildDebug   # Compile native lib only (.so)
.\gradlew :app:assembleDebug              # Build full debug APK
.\gradlew :app:installDebug               # Install to connected device/emulator
.\gradlew :app:assembleRelease            # Release build (requires signing config)
```

**Java note**: This project targets Java 8 (sourceCompatibility = VERSION_1_8). Ensure your JDK is Java 8 or 11+. If JAVA_HOME points nowhere, unset it:

```
$env:JAVA_HOME = $null
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", $null, "User")
```

## Coding Style

| Aspect | Rule |
|---|---|
| Language | Kotlin (primary), C/C++17 (native/JNI) |
| Indentation | 4 spaces |
| Kotlin style | kotlinlang.org/styleguide |
| C/C++ style | Follow libchewing conventions; extern C in .cpp for JNI symbols |
| Package | com.example.androidkeyboard |
| Class names | PascalCase (ChewingEngine, CandidateView) |
| Function/variable | camelCase (handleDefaultKey, composingText) |
| Constants | SCREAMING_SNAKE (MAX_CANDIDATES, NGRAM_MAX) |

---

## Testing

No unit tests are currently implemented. The project is in **stub mode** - JNI functions return empty strings until activated per docs/JNI-INTEGRATION.md.

To add tests:
- Unit tests -> app/src/test/java/...
- Instrumented tests -> app/src/androidTest/java/...
- Framework: androidx.test (already in dependencies)

---

## Commit and PR Guidelines

Commit messages follow this convention (seen in git history):

```
fix(waveN): <description>
feat(waveN): <description>
chore: <description>
docs: <description>
refactor: <description>
```

**Scope examples**: (wave4), (wave3), or omit for non-wave changes.

PR requirements:
- Link the task or tracking doc (e.g., docs/CHECKS/wave4-t21.md)
- Include screenshots for UI changes
- Confirm build passes: .\gradlew :app:assembleDebug
- For native changes, verify: .\gradlew :app:externalNativeBuildDebug

---

## Architecture Quick Reference

- **Engine layer**: ChewingEngine (core interface) -> AndroidChewingEngine (JNI implementation)
- **Native**: CMake compiles libchewing C sources + chewing_jni.cpp -> libchewing-jni.so
- **Converter**: OpenCC handles Trad/Simp toggle (currently a stub, not wired)
- **Config**: IMEConfig.kt wraps SharedPreferences for 8 keys (autoPhrase, maxChiLen, etc.)
- **IME entry point**: ChewingInputMethodService extends InputMethodService

---

## Agent Workflow

Development is organized into **waves** (T1-Tn tasks):

1. Check the current wave in docs/CHECKS/waveN.md
2. Complete tasks sequentially; update the wave doc as you go
3. After each wave, run the self-check checklist in that wave's doc
4. Commit with the wave scope prefix: feat(wave4): T21 engine split
5. Update docs/ROADMAP.md if scope changes

Key docs to read before touching code:
- docs/ARCHITECTURE.md - system overview
- docs/JNI-INTEGRATION.md - native layer activation
- docs/KEYMAP.md - Bopomofo key mapping
