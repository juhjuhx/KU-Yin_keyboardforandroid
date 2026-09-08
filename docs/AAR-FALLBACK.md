# AAR Fallback 路徑：libchewing Android App Module

> **狀態**：文件記錄（read-only inspection），非實作交付。  
> **檢視日期**：2026-09-08  
> **上游 repo SHA**：`a3511a8cca1ece80f78cf7dfa02b89fa225a6acc`（`2025-06-06`，`Release 0.9.1.8`）  
> **上游 clone 方式**：`git clone --depth=1`（shallow），未抓 history。

---

## 1. 觸發條件（何時啟用 fallback）

本專案（android-keyboard）預設路徑為 fork-mirror `fcitx5-android`，其透過 `prebuilt` submodule 以**預編譯靜態庫**（`libchewing_capi.a`）提供 libchewing。

**AAR fallback 適用於**：當以下任一條件成立時：

1. `fcitx5-android` 上游 prebuilt submodule 的 libchewing 靜態庫無法使用（submodule 鎖定失敗、git 伺服器不可達、或 prebuilt 內容與目標 ABI 不符）。
2. 需要**自行從源碼編譯 libchewing** 並以 Android Library（AAR）格式封裝，供 `fcitx5-chewing` 橋接層引用。
3. 需要**驗證 libchewing 功能**或**自訂 patch**，而 prebuilt 二進位不允許修改。

**預設行為 = 不 vendoring**：AAR 產物不進本倉；僅在 fallback 路徑啟動時，於臨時目錄構建後手動複製到 `prebuilt/` 對應位置。

---

## 2. 上游 repo 結構摘要

```
libchewingAndroidAppModule/
├── build.gradle.kts            # root build：宣告 AGP + Kotlin + Android Library plugin
├── settings.gradle.kts         # include(":app")，repositories 設定
├── gradle.properties           # JVM args、AndroidX、nonTransitiveRClass
├── gradle/
│   ├── libs.versions.toml      # version catalog
│   └── wrapper/                # Gradle 8.11.1 wrapper
├── .gitmodules                 # submodule: app/src/main/cpp/libs/libchewing → hiroshiyui/libchewing (android branch)
├── app/
│   ├── build.gradle.kts        # 核心：library plugin、CMake、NDK、Rust targets、tasks
│   └── src/main/
│       ├── cpp/
│       │   ├── CMakeLists.txt  # cmake 3.24.0+、add_subdirectory(libchewing)、SHARED lib
│       │   ├── app.cpp         # JNI bridge（795 行，完整 chewing API 包裝）
│       │   └── libs/libchewing # git submodule（android branch）
│       ├── java/com/miyabi_hiroshi/  # Kotlin Chewing wrapper class
│       └── AndroidManifest.xml
└── GPL-3.0.txt                 # 授權（GPL-3.0）
```

---

## 3. 版本需求與前置條件

### 3.1 Gradle / AGP / Kotlin

| 項目 | 版本 | 來源 |
|------|------|------|
| Gradle wrapper | **8.11.1** | `gradle/wrapper/gradle-wrapper.properties` → `gradle-8.11.1-bin.zip` |
| Android Gradle Plugin (AGP) | **8.9.0** | `gradle/libs.versions.toml` → `agp = "8.9.0"` |
| Kotlin | **1.9.0** | `gradle/libs.versions.toml` → `kotlin = "1.9.0"` |
| compileSdk | **35** | `app/build.gradle.kts` → `compileSdk = 35` |
| minSdk | **23** | `app/build.gradle.kts` → `minSdk = 23` |
| buildToolsVersion | **35.0.0** | `app/build.gradle.kts` → `buildToolsVersion = "35.0.0"` |
| Java compatibility | **17** | `app/build.gradle.kts` → `sourceCompatibility = JavaVersion.VERSION_17` |
| CMake | **3.24.0+** | `app/build.gradle.kts` → `version = "3.24.0+"` |

### 3.2 NDK

| 項目 | 版本 |
|------|------|
| NDK | **28.1.13356709**（`app/build.gradle.kts` → `ndkVersion = "28.1.13356709"`） |

### 3.3 Rust（可選但建議）

建置 script 包含四個 `rustup target add` task：

| Target triple | task name |
|---------------|-----------|
| `aarch64-linux-android` | `rustupTargetAddAarch64LinuxAndroid` |
| `armv7-linux-androideabi` | `rustupTargetAddArmv7LinuxAndroideabi` |
| `i686-linux-android` | `rustupTargetAddI686LinuxAndroid` |
| `x86_64-linux-android` | `rustupTargetAddX64LinuxAndroid` |

> **注意**：這些 task 在 `preBuild` 時自動執行；若 `rustup` 不可用，task 會因 `isIgnoreExitValue` 而靜默跳過（不阻斷建置）。但若 libchewing 上游（android branch）使用 Rust 重寫版本，**Rust 工具鏈為必需**。

### 3.4 MSRV（Minimum Supported Rust Version）

本文件產生時**未找到明確的 MSRV 標記**。建議：
- 若 libchewing android branch 為 Rust 重寫版，參考上游 `Cargo.toml` 中的 `rust-version` 欄位。
- 若無明確標記，保守使用 **1.88**（當前穩定版附近），並在首次建置前以 `rustc --version` 確認。

---

## 4. 建置指令（可重複步驟）

```bash
# 0. 前置條件確認
java -version          # 需 JDK 17+（⚠️ 本機為 1.8.0_401，不符）
cmake --version        # 需 3.24.0+
ndk-build --version    # 或透過 Android SDK Manager 安裝 NDK 28.1.13356709
rustc --version        # 可選，建議 1.88+

# 1. Clone（shallow + recursive）
git clone --depth=1 --recursive https://github.com/hiroshiyui/libchewingAndroidAppModule.git
cd libchewingAndroidAppModule

# 2. 建置 AAR
./gradlew bundleReleaseAar

# 3. 產物位置
ls app/build/outputs/aar/
# 預期檔案：libchewing_android_app_module_0.9.1.8-release.aar
```

---

## 5. 產物路徑與檔名規則

| 項目 | 值 |
|------|----|
| AAR 輸出目錄 | `app/build/outputs/aar/` |
| 檔名格式 | `${projectName}-${versionName}.aar` |
| projectName | `libchewing_android_app_module` |
| versionName | `0.9.1.8` |
| 預期 AAR 檔名 | `libchewing_android_app_module_0.9.1.8-release.aar` |
| archivesBaseName 設定 | `app/build.gradle.kts` → `setProperty("archivesBaseName", "${projectName}_${versionName}")` |

> ⚠️ Gradle 的 `bundleReleaseAar` task 產出的 AAR 檔名可能包含 build type 後綴（如 `-release.aar`），實際檔名以 `app/build/outputs/aar/` 內容為準。

---

## 6. CMake 建置細節

`app/src/main/cpp/CMakeLists.txt` 關鍵設定：

```cmake
cmake_minimum_required(VERSION 3.24.0)
project("libchewing_android_app_module" LANGUAGES C CXX)

# libchewing submodule 引入（android branch）
SET(BUILD_INFO false)
SET(BUILD_TESTING false)
SET(WITH_SQLITE3 false)
SET(CMAKE_BUILD_TYPE Release)
add_subdirectory(${CMAKE_SOURCE_DIR}/libs/libchewing EXCLUDE_FROM_ALL)

# JNI shared library
add_library(${CMAKE_PROJECT_NAME} SHARED app.cpp)
target_link_libraries(${CMAKE_PROJECT_NAME} libchewing android log)
```

**重要**：`libs/libchewing` 為 git submodule（`.gitmodules` 指向 `hiroshiyui/libchewing` android branch），需以 `--recursive` clone 或手動 `git submodule update --init`。

---

## 7. libchewing Data Files（字典檔）

建置流程會先編譯 libchewing 的 data files，再複製到 AAR assets：

| Data file | 用途 |
|-----------|------|
| `tsi.dat` | 系統詞庫（詞頻） |
| `word.dat` | 使用者詞庫模板 |
| `swkb.dat` | 符號對照 |
| `symbols.dat` | 符號表 |

Gradle tasks：`prepareChewing` → `buildChewingData` → `copyChewingDataFiles`（自動在 `preBuild` 時執行）。

---

## 8. 本機建置阻斷器（實際探查結果）

| 項目 | 本機狀態 | 需求 | 結果 |
|------|----------|------|------|
| Java | **1.8.0_401** | JDK 17+ | **BLOCKED**（版本過低） |
| CMake | **未安裝** | 3.24.0+ | **BLOCKED** |
| Rust | **未安裝** | 可選（建議 1.88+） | WARN |
| Android SDK | **未設定**（`ANDROID_HOME` / `ANDROID_SDK_ROOT` 無） | SDK + NDK 28.1.13356709 | **BLOCKED** |
| NDK | **未安裝** | 28.1.13356709 | **BLOCKED** |

> **結論**：本機環境缺少 JDK 17、CMake、Android SDK/NDK，無法執行完整建置。以上文件中所有路徑與版本號均來自**靜態讀取 Gradle 設定檔**，非建置產出。標記為 `NOT-RUN` 的步驟已明確註記。

---

## 9. Gradle Task 依賴圖

```
preBuild
├── copyChewingDataFiles
│   └── buildChewingData
│       └── prepareChewing  (cmake + make data all_static_data)
├── rustupTargetAddAarch64LinuxAndroid
├── rustupTargetAddArmv7LinuxAndroideabi
├── rustupTargetAddI686LinuxAndroid
└── rustupTargetAddX64LinuxAndroid

bundleReleaseAar  (Android Library plugin 提供)
    └── preBuild (上述全部)

clean
├── cleanChewingDataFiles
├── execMakeClean
├── deleteChewingBuildDirectory
└── deleteBuiltAarFile
```

---

## 10. 授權注意事項

- `libchewingAndroidAppModule` 本身為 **GPL-3.0**（見 `GPL-3.0.txt`）。
- `libchewing` 上游為 **LGPL-2.1**。
- 本專案（android-keyboard）底座 `fcitx5-android` 為 **LGPL-2.1**。
- 若將 GPL-3.0 封裝的 AAR 整合進 LGPL-2.1 專案，需確認 LGPL-2.1 的「動態連結」例外是否適用。建議僅使用 libchewing 本身（LGPL-2.1），而不包含 `app.cpp` 等 GPL-3.0 封裝層。

---

## 11. 與本專案的整合路徑

若需使用此 fallback AAR：

1. 在臨時目錄完成建置，取得 `libchewing_android_app_module_0.9.1.8-release.aar`。
2. 將 AAR 內的 `lib/` 目錄（含 `.so`）與 `assets/` 目錄（含 `.dat` 字典檔）提取。
3. 放置到 `fcitx5-android` 的 `prebuilt/libchewing/<ABI>/` 對應位置，替換預編譯靜態庫。
4. 調整 `fcitx5-chewing` 的 `CMakeLists.txt` 從 `find_package(Chewing)` 改為直接連結 `.so`（或維持 static linking）。

> ⚠️ 此為**概念路徑**，尚未實作。具體整合需考量 ABI 相容性、libchewing 版本（rust 重寫 vs C 舊版）差異、以及 `chewing_Reset` 語意變更風險（見 `docs/UPSTREAM.md` §4）。

---

## 12. 風險與限制

| 風險 | 說明 |
|------|------|
| 版本過時 | 上游 repo 最後更新 2025-06-06（`0.9.1.8`），libchewing 上游已遷至 Codeberg 且發佈至 0.13.1 |
| GPL-3.0 封裝 | AAR 整體為 GPL-3.0，與本專案 LGPL-2.1 基座有授權張力 |
| Rust 工具鏈 | android branch 可能依賴 Rust，MSRV 未明確標記 |
| `chewing_Reset` 語意 | rust 重寫版與 C 舊版行為不同（見 `UPSTREAM.md` §4），直接替換有風險 |
| NDK 版本耦合 | 鎖定 NDK 28.1.13356709，升級需同步測試 |

---

## 13. 本文件的Inspectable Proof

以下為本文件撰寫過程中實際讀取的檔案與對應行號，可作為 adversarial probe 的 evidence：

| 聲稱 | 檔案 | 行號 | 內容 |
|------|------|------|------|
| versionName = 0.9.1.8 | `app/build.gradle.kts` | 27 | `val versionName: String = "0.9.1.8"` |
| compileSdk = 35 | `app/build.gradle.kts` | 31 | `compileSdk = 35` |
| minSdk = 23 | `app/build.gradle.kts` | 34 | `minSdk = 23` |
| ndkVersion = 28.1.13356709 | `app/build.gradle.kts` | 86 | `ndkVersion = "28.1.13356709"` |
| buildToolsVersion = 35.0.0 | `app/build.gradle.kts` | 85 | `buildToolsVersion = "35.0.0"` |
| Java 17 | `app/build.gradle.kts` | 70-71 | `sourceCompatibility = JavaVersion.VERSION_17` |
| CMake 3.24.0+ | `app/build.gradle.kts` | 65 | `version = "3.24.0+"` |
| Gradle 8.11.1 | `gradle/wrapper/gradle-wrapper.properties` | 4 | `distributionUrl=…gradle-8.11.1-bin.zip` |
| AGP 8.9.0 | `gradle/libs.versions.toml` | 2 | `agp = "8.9.0"` |
| Kotlin 1.9.0 | `gradle/libs.versions.toml` | 3 | `kotlin = "1.9.0"` |
| Repo SHA | `git log -1` | — | `a3511a8… 2025-06-06 Release 0.9.1.8` |
| submodule URL | `.gitmodules` | 3 | `url = https://github.com/hiroshiyui/libchewing.git` |
| AAR 輸出路徑 | `app/build.gradle.kts` | 210 | `delete("$rootDir/app/build/outputs/aar/…")` |
| Rust targets | `app/build.gradle.kts` | 121-179 | 四組 `rustup target add` task |
| buildChewingData | `app/build.gradle.kts` | 107-111 | `commandLine("make", "data", "all_static_data")` |
| CMakeLists 鏈接 | `app/src/main/cpp/CMakeLists.txt` | 62-65 | `target_link_libraries(… libchewing android log)` |
