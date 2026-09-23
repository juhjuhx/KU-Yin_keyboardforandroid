# Development and Build (derived from repo files, not memory)

| Component | Canonical source | Required version |
|---|---|---|
| JDK | CI `build.yml` + AGP requirement | 21 (Temurin) |
| Gradle wrapper | `gradle/wrapper/gradle-wrapper.properties` | 9.3.1 |
| AGP | `gradle/libs.versions.toml` | 9.1.1 |
| Kotlin | `gradle/libs.versions.toml` | 2.2.10 |
| compileSdk / targetSdk | `app/build.gradle.kts` | 36 / 36 (minorApiLevel 1) |
| minSdk | `app/build.gradle.kts` | 24 |
| NDK | `app/build.gradle.kts` | 28.2.13676358 |
| CMake | `app/build.gradle.kts` + `app/src/main/cpp/CMakeLists.txt` | 3.22.1, C++17 |
| Test | catalog + runners | JUnit4, Robolectric 4.16.1, Espresso, Roborazzi |

## Build

```bash
bash scripts/bootstrap_native_deps.sh
./gradlew testDebugUnitTest --stacktrace
./gradlew assembleDebug --stacktrace
```

Outputs: `app/build/outputs/apk/debug/app-debug.apk`; release stays unsigned
(no keystore — never present unsigned artifacts as signed).

## What bootstrap does

Fetches pinned `fcitx5-android/prebuilt` commit + libchewing source commit
(see script + `docs/UPSTREAM.md`), verifies per-ABI archives and dictionaries,
fails loudly on anything missing.

## Tests

Fast Python contracts catch Manifest/resource/mapping/architecture invariants;
JVM tests are the behavior source of truth; lifecycle/JNI need
instrumentation/runtime smoke. Contracts must stay textual invariants, never a
home-grown Kotlin parser — behavior belongs in JVM tests.

## CI

`.github/workflows/build.yml` is blocking (contracts → toolchain → bootstrap →
JVM → Debug APK → artifact). `runtime-smoke` is experimental
(`continue-on-error: true`) and never counts as PASS. `prerelease.yml` tags
prereleases on main; `upstream-watch.yml` only files review issues.

## Contribution workflow

Production behavior changes carry the narrowest regression test. Explain root
cause, expected behavior, tests, static build evidence, and runtime evidence
for lifecycle/native changes. Never mix SDK upgrades, UI redesigns, decoder
swaps, or refactors into one PR.
