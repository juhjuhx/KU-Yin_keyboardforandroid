# Build Guide

KU-Yin currently uses a deterministic GitHub Actions build as the reference build path. Local builds should reproduce the same major steps rather than rely on machine-specific absolute paths.

## Verified CI toolchain

The latest verified recovery head uses:

- JDK 17
- Android SDK via `android-actions/setup-android@v3`
- Gradle 7.6.4 via `gradle/actions/setup-gradle@v6`
- NDK `27.3.13750724`
- CMake 3.22.1 for the Android app native build
- pinned libchewing/prebuilt inputs staged by `scripts/bootstrap_native_deps.sh`
- Kotlin/Android application module under `app/`

Current Android application settings:

- `minSdk = 24`
- `compileSdk = 33`
- `targetSdk = 33`
- four native ABIs: `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`

API 36 migration is intentionally deferred to a dedicated compatibility pass after runtime IME recovery.

## GitHub Actions reference build

The CI sequence is defined in `.github/workflows/build.yml`:

```text
contracts
  ↓
JDK 17
  ↓
Android SDK
  ↓
Gradle 7.6.4
  ↓
bootstrap pinned native dependencies
  ↓
testDebugUnitTest
  ↓
assembleDebug
  ↓
assembleRelease
  ↓
upload Debug / Release APK artifacts
```

The current recovery branch has passed this full sequence.

## Local build

Prerequisites:

- JDK 17
- Android SDK with the required platform/build tools
- Android NDK `27.3.13750724`
- CMake compatible with the project configuration
- Bash, Python 3 and network access for the dependency bootstrap step

From the repository root:

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest --stacktrace
gradle assembleDebug --stacktrace
gradle assembleRelease --stacktrace
```

If a compatible Gradle wrapper is restored later, `./gradlew` may replace the system `gradle` command. The current CI intentionally pins Gradle 7.6.4 through the GitHub Action.

## Outputs

```text
app/build/outputs/apk/debug/
app/build/outputs/apk/release/
```

GitHub Actions also uploads artifacts named:

- `debug-apk`
- `release-apk`

## Native dependency model

`scripts/bootstrap_native_deps.sh` stages pinned libchewing native archives, headers and dictionary data. CMake treats a missing ABI archive as a build failure rather than producing an APK that may fail at runtime.

libchewing dictionary data is copied into app-private storage before engine initialization.

## What a successful build proves

A successful build proves that:

- source/build contracts pass
- JVM tests pass
- native archives link for all configured ABIs
- Debug and Release APKs are produced

A successful build does **not** prove that Android can enable/switch/render/use the IME correctly. Runtime verification is tracked separately in `docs/NEXT_STEPS.md`.

## Troubleshooting principle

When CI fails, use the first failing step as the root-cause boundary. Avoid adding workaround patches before the current failure is reproduced and understood.
