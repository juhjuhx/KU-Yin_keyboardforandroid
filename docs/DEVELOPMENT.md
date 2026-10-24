# Development and Build (derived from repo files, not memory)

| Component | Canonical source | Required version |
|---|---|---|
| JDK | CI `build.yml` + AGP requirement | 21 (Temurin) |
| Gradle wrapper | `gradle/wrapper/gradle-wrapper.properties` | 9.3.1 |
| AGP | `gradle/libs.versions.toml` | 9.1.1 |
| Kotlin | `gradle/libs.versions.toml` | 2.2.10 |
| compileSdk / targetSdk | `app/build.gradle.kts` | 36 / 36 (minorApiLevel 1) |
| minSdk | `app/build.gradle.kts` | 24 |
| applicationId | `app/build.gradle.kts` | io.github.juhjuhx.kuyin |
| NDK | `app/build.gradle.kts` | 28.2.13676358 |
| CMake | `app/build.gradle.kts` + `app/src/main/cpp/CMakeLists.txt` | 3.22.1, C++17 |
| Test | catalog + runners | JUnit4, Robolectric 4.16.1, Espresso, Roborazzi |

## Build

```bash
bash scripts/bootstrap_native_deps.sh
./gradlew testDebugUnitTest --stacktrace
./gradlew assembleDebug --stacktrace
```

Outputs: `app/build/outputs/apk/debug/app-debug.apk` (Debug signed). Release configuration requires a keystore; if absent, packaging is BLOCKED_BY_SIGNING, not a successful unsigned artifact.

P1-E 驗收工具建置 gates（不需要裝置）：

```bash
./gradlew :acceptance-host:testDebugUnitTest :acceptance-host:assembleDebug :acceptance-host:assembleDebugAndroidTest
./gradlew :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:assembleDebug
python3 -m unittest discover -s scripts/tests -v
```

Host APK 是 test-only helper；編譯成功不能代替實機驗收。依
`docs/review/2026-10-09-p1g/09-device-runbook.md` 與
`scripts/p1_acceptance_device.py` 核對凍結檔案、固定 serial、保存與還原原 IME 狀態。
舊 runtime_smoke.sh 不作為凍結驗收包的執行入口。

## What bootstrap does

Fetches the pinned `fcitx5-android/prebuilt` commit; records its associated libchewing source commit as provenance (does not rebuild Rust). Checks checkout SHA and nonempty per-ABI archives/dictionaries. Stages to `app/src/main/cpp/.deps/libchewing` and `assets/libchewing`. Use a fresh `KUYIN_DEPS_CACHE` because bootstrap replaces its checkout/output directories.

## Tests

Fast Python contracts catch Manifest/resource/mapping/architecture invariants;
JVM tests are the behavior source of truth; lifecycle/JNI need
instrumentation/runtime smoke. Contracts must stay textual invariants, never a
home-grown Kotlin parser — behavior belongs in JVM tests.

## CI

`.github/workflows/build.yml` 的unit/build為blocking：全部Python、contracts（既有10＋Launcher與secrets）、固定native bootstrap、JVM與Debug。驗證Python依賴：`python3 -m pip install -r requirements-verification.txt`。安全tripwire：`python3 scripts/check_repository_secrets.py`；維護者可用`--history`唯讀檢查reachable Git blobs，輸出不含憑證值；不是完整credential certification。

Runtime只在workflow_dispatch明確run_runtime=true時執行；一旦執行，失敗不忽略。PR的Runtime SKIPPED是未執行，不是PASS。prerelease.yml雖保留檔名，現在只上傳TEST_ONLY Alpha artifact，contentsread，沒有gh release/create/tag寫入。公開Alpha及其source/relink不變。

checkout完整history用於凍結6595b684來源ledger。scripts/p1_instrumentation_manifest.json仍描述原5/26 frozen suite；當前Host測試來源另見docs/testing/R3_8_INSTRUMENTATION_SOURCES.json，不能把current helper編譯結果冒稱原四APK。

Actions commit與wrapper SHA來源見docs/security/BUILD_INPUT_PINS.json。依賴驗證metadata、完整新二進位SBOM/重鏈結重建等價仍需後續審查；固定版本不是漏洞保證。

## Contribution workflow

Production behavior changes carry the narrowest regression test. Explain root
cause, expected behavior, tests, static build evidence, and runtime evidence
for lifecycle/native changes. Never mix SDK upgrades, UI redesigns, decoder
swaps, or refactors into one PR.

## Launcher 資產生成與驗證

Launcher fallback 現由既有 adaptive foreground／background 程式生成。開發用 generator 需要 Pillow12.2.0／libwebp1.6.0；`python3 scripts/generate_launcher_assets.py` 會更新十個 density/round WebP 與來源 manifest，不改 adaptive XML／原圖。

`python3 scripts/check_launcher_assets.py` 使用 `dwebp` 與 `ffmpeg` 完整解碼、RIFF framing及各DPI尺寸檢查；本機需先安裝這兩工具與 `Pillow==12.2.0`（geometry regression載入generator）；CI在Python regressions前明確安裝三者。全部 `check_*.py` 現含這項新gate，不再只有原十項。詳細限制見 `docs/testing/R3_2_IMAGE_INTEGRITY.md`，這不是手機icon顯示或Runtime截圖驗收。
