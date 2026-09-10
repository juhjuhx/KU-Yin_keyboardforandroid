# Development and Build

## Verified toolchain baseline

- JDK 17
- Android Gradle Plugin 7.4.2
- Kotlin Android plugin 1.9.22
- Gradle 7.6.4
- compileSdk / targetSdk 33
- minSdk 24
- Android NDK `27.3.13750724`
- CMake `3.22.1`
- C++17 JNI bridge

API 33 是当前可重复建置基线，不是长期目标；SDK/AGP 现代化列在 roadmap，并应与功能修复分开进行。

## Build

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest --stacktrace
gradle assembleDebug --stacktrace
gradle assembleRelease --stacktrace
```

产物：

```text
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release-unsigned.apk
```

Release APK 当前未配置正式 signing，因此只能视为 unsigned developer artifact。

## What bootstrap does

`scripts/bootstrap_native_deps.sh` 从固定的 `fcitx5-android/prebuilt` commit 取得四 ABI `libchewing_capi.a`、C headers 与 dictionary files，并写入 provenance。脚本在缺少任一 ABI / dictionary 时直接失败，避免静默生成不可用 APK。

## Tests

快速 Python contracts 用于发现 Manifest/resource/key mapping/architecture 等高价值不变量；Kotlin 行为以 JVM tests 为主要 source of truth。Android lifecycle 与 JNI 则由 instrumentation/runtime smoke 补充。

Contract script 不应逐渐变成自制 Kotlin parser。若行为能够在 JVM test 中表达，应优先写 JVM test。

## CI

`.github/workflows/build.yml` 是 blocking build baseline：contracts → JDK/SDK/Gradle → native bootstrap → JVM tests → Debug APK → unsigned Release APK → artifact upload。runtime-smoke 独立且目前为 experimental/non-blocking。

`.github/workflows/prerelease.yml` 在 `main` 上按 `versionName` 生成 prerelease，附 Debug APK、unsigned Release APK 与 SHA-256。

`.github/workflows/upstream-watch.yml` 只侦测上游变化并建立审查 Issue，不会未经验证自动更新 native pin。

## Contribution workflow

对 production 行为的修改应包含最窄可用 regression test。PR 需要说明问题根因、预期行为、测试、静态 build 证据，以及若涉及 IME lifecycle/native 输入时的 runtime 证据。

不要把 SDK 升级、UI 重设计、decoder 更换与 bug fix 混在同一个 PR。
