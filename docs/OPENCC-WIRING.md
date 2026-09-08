# OpenCC 一鍵簡繁切換接線規格（OPENCC-WIRING）

> 狀態：**分析與規格文件（docs-only，Todo 6 / tier MEDIUM，無任何程式碼變更）**。
> 本檔以唯讀方式檢視上游 `fcitx5-chinese-addons`（pin `0d3fd0408d03abc63cdb700559f85a387b98735f`）與 `fcitx5-android`（HEAD `6998502528cd26efb6079556da92003638e4179c`）的 **OpenCC 一鍵簡繁切換**完整接線（upstream 消費路徑、addon 設定表面、toolbar 一鍵切換與 per-profile 持久化），供本專案（`android-keyboard` fork-mirror）後續落地參考。
> 所有 SHA、檔案路徑、grep 皆為「ⓘ 擷取當下」的唯讀快照；**本檔不修改任何既有檔案，亦不改寫上游**。

---

## 0. 快照與權威（stale_state 記錄）

| 項目 | 值 |
|------|----|
| 檢視日期 | 2026-09-08 |
| `fcitx5-chinese-addons` pin（上游 gitlink） | `0d3fd0408d03abc63cdb700559f85a387b98735f`（2026-08-16，「Add cache for bestMatchPinyin」） |
| `fcitx5-android` HEAD（本次 clone） | `6998502528cd26efb6079556da92003638e4179c`（2026-08-21，「Update fcitx5 submodules」） |
| upstream remote URL | `https://github.com/fcitx/fcitx5-chinese-addons.git` |
| android remote URL | `https://github.com/fcitx5-android/fcitx5-android.git` |
| OpenCC 預編譯版本 | `1.2.0`（prebuilt `opencc/…/OpenCCConfigVersion.cmake`） |
| marisa（OpenCC 依賴） | prebuilt 提供（`PREBUILT_DIR/marisa/<abi>/lib/cmake/Marisa`） |

> ⚠️ **staleness 提醒**：以上 SHA 為靜態快照。任何落地作業前請重新 `git ls-remote` 校驗，勿以本檔當作永遠有效。上游 `master` 會週期性「Update fcitx5 submodules」改動 gitlink。

---

## 1. 上游 OpenCC 消費路徑（fcitx5-chinese-addons side）

### 1.1 pin 與 CMake 開關

`fcitx5-chinese-addons` 頂層 `CMakeLists.txt`：

```
# D:\Temp\opencode\opencc-upstream\CMakeLists.txt
option(ENABLE_OPENCC "Enable OpenCC for chttrans" On)
...
if (ENABLE_OPENCC)
    find_package(OpenCC 1.0.1 REQUIRED)          # 要求 OpenCC >= 1.0.1
endif()
if (ENABLE_OPENCC OR ENABLE_CLOUDPINYIN)
    find_package(nlohmann_json 3.2 REQUIRED)
endif()
```

- OpenCC 僅用於 `chttrans`（簡繁轉換）addon，由 `ENABLE_OPENCC` 控制（預設 `On`）。
- 需要 `nlohmann_json`（用於解析 OpenCC profile `.json` 檔，見 §2）。

### 1.2 chttrans 模組的 OpenCC 後端

消費路徑核心在 `modules/chttrans/`：

| 檔案 | 角色 |
|------|------|
| `modules/chttrans/chttrans.cpp` | addon 主體、OutputFilter/CommitFilter、toggle、設定讀寫 |
| `modules/chttrans/chttrans.h` | 設定 schema（`Engine` / `OpenCCS2TProfile` / `OpenCCT2SProfile` / `EnabledIM` / `Hotkey`）與 `ToggleAction` |
| `modules/chttrans/chttrans-opencc.cpp` / `.h` | `OpenCCBackend`：`opencc::SimpleConverter` 載入 s2t / t2s profile |
| `modules/chttrans/chttrans-native.cpp` / `.h` | 內建 native（GBK→Big5 table）fallback 後端 |
| `modules/chttrans/opencc-profile-name.cpp` | OpenCC profile 名稱 i18n（`NC_("OpenCC Profile", …)`） |
| `modules/chttrans/CMakeLists.txt` | addon 建置、連結 `OpenCC::OpenCC` |
| `modules/chttrans/gbks2t.tab` | native 後端用的簡繁對照表 |

**CMake 連結**（`modules/chttrans/CMakeLists.txt`）：

```cmake
if (ENABLE_OPENCC)
    set(CHTTRANS_SOURCES ${CHTTRANS_SOURCES} chtrans-opencc.cpp)
endif()
add_fcitx5_addon(chttrans ${CHTTRANS_SOURCES})
target_link_libraries(chttrans Fcitx5::Core Fcitx5::Config Fcitx5::Module::Notifications)
if (ENABLE_OPENCC)
    target_link_libraries(chttrans OpenCC::OpenCC nlohmann_json::nlohmann_json)
endif()
```

### 1.3 OpenCC 資料檔（profile `.json`）解析路徑

`modules/chttrans/chttrans.cpp` 定義 OpenCC StandardPaths：

```cpp
std::unordered_map<std::string, std::vector<std::filesystem::path>>
openCCBuiltInPath() {
    ...
    result["datadir"] = {prefix / "share"};
    result["pkgdatadir"] = {prefix / "share/opencc"};
    ...
}
const StandardPaths &openCCStandardPath() { ... }
```

而 `chttrans-opencc.cpp` 的 `locateProfile` 依 `StandardPathsType::PkgData` 找 profile：

```cpp
std::string OpenCCBackend::locateProfile(const std::string &profile) {
    auto profilePath =
        openCCStandardPath().locate(StandardPathsType::PkgData, profile);
    return profilePath.empty() ? profile : profilePath.string();
}
```

**Key 決策（`chttrans-opencc.cpp::updateConfig`）——預設台灣 profile：**

```cpp
auto s2tProfile = *config.openCCS2TProfile;
if (s2tProfile.empty() || s2tProfile == "default") {
    const std::string preferredS2TProfile = "s2tw.json";   // ⭐ 簡→繁台灣
    if (locateProfile(preferredS2TProfile) != preferredS2TProfile) {
        s2tProfile = preferredS2TProfile;
    } else {
        s2tProfile = OPENCC_DEFAULT_CONFIG_SIMP_TO_TRAD;    // fallback s2t.json
    }
}
...
auto preferredT2SProfile = "tw2s.json";                      // ⭐ 繁台灣→簡
... fallback：OPENCC_DEFAULT_CONFIG_TRAD_TO_SIMP（t2s.json）
```

即：**s2t 預設 `s2tw.json`（台灣），找不到才 fallback 到 `s2t.json`；t2s 預設 `tw2s.json`，fallback `t2s.json`**。此為本專案「預設台灣 s2tw/tw2s」的來源。

轉換為 lazy 建立 `opencc::SimpleConverter`，轉換失敗（例外）時原樣回傳字串（graceful degradation）。

---

## 2. Android 端 prebuilt 消費路徑（fcitx5-android side）

### 2.1 fcitx5-chinese-addons 的 prebuilt OpenCC/marisa（CMake）

`lib/fcitx5-chinese-addons/src/main/cpp/CMakeLists.txt`（fcitx5-android 內嵌 wrapper）：

```cmake
# prebuilt marisa-tire, OpenCC needs it
set(Marisa_DIR "${PREBUILT_DIR}/marisa/${ANDROID_ABI}/lib/cmake/Marisa")
find_package(Marisa)

# prebuilt opencc
set(OpenCC_DIR "${PREBUILT_DIR}/opencc/${ANDROID_ABI}/lib/cmake/opencc")
find_package(OpenCC)

...
# prefer OpenCC_DIR rather than fcitx5-chinese-addons/cmake/FindOpenCC.cmake
set(CMAKE_FIND_PACKAGE_PREFER_CONFIG ON)
add_subdirectory(fcitx5-chinese-addons)
```

**Key 決策（本專案沿用）：**
- OpenCC 以**預編譯**方式引入，不走 Linux 桌面的 `pkg-config` / `FindOpenCC.cmake`（那會設 `OPENCC_PREFIX`）。
- `CMAKE_FIND_PACKAGE_PREFER_CONFIG ON`（在 `find_package(OpenCC)` **之前**設定）→ 強制優先吃 prebuilt 的 `OpenCCConfig.cmake`，而非 upstream 自帶的 `cmake/FindOpenCC.cmake` module。
- marisa（OpenCC 的字典後端資料結構庫）同樣以 prebuilt `find_package(Marisa)` 引入。
- `PREBUILT_DIR` 由 `NativeBaseConventionPlugin.kt` 傳入（CMake argument）：

```
build-logic/convention/src/main/kotlin/NativeBaseConventionPlugin.kt
  → -DPREBUILT_DIR=${prebuiltDir.absolutePath}
  → prebuiltDir = <root>/lib/fcitx5/src/main/cpp/prebuilt   （prebuilt submodule，pin 86ce2c95…）
```

### 2.2 prebuilt 內實際路徑

| 元件 | prebuilt 內路徑（依 `<abi>`） |
|------|------|
| OpenCC 程式庫 CMake config | `${PREBUILT_DIR}/opencc/<abi>/lib/cmake/opencc/`（`OpenCCConfig.cmake`、`OpenCCConfigVersion.cmake` → `PACKAGE_VERSION "1.2.0"`） |
| OpenCC 資料（`.json` profile） | `${PREBUILT_DIR}/opencc/data/`（`install(DIRECTORY …)` 來源） |
| marisa CMake config | `${PREBUILT_DIR}/marisa/<abi>/lib/cmake/Marisa/` |
| 其它 chinese-addons 資料 | `${PREBUILT_DIR}/chinese-addons-data/…`、`${PREBUILT_DIR}/libime/…` |
| `<abi>` | `arm64-v8a` / `armeabi-v7a` / `x86_64`（`Versions.supportedABIs`） |

### 2.3 OpenCC 資料檔打包進 APK（assets → /usr/share/opencc）

`app/src/main/cpp/CMakeLists.txt`（fcitx5-android root native）：

```cmake
install(DIRECTORY "${PREBUILT_DIR}/opencc/data/" DESTINATION "${FCITX_INSTALL_DATADIR}/opencc" COMPONENT prebuilt-assets)
```

- `${FCITX_INSTALL_DATADIR}` = `/usr/share`（`lib/fcitx5/src/main/cpp/cmake/Fcitx5AndroidInstallDirs.cmake:9`）。
- `COMPONENT prebuilt-assets` 由 Gradle `FcitxComponentPlugin` 的 `installPrebuiltAssets=true` 觸發 `cmake --install --component prebuilt-assets`，把 `/usr/share/opencc/*.json` 安裝到 `app/src/main/assets/usr/share/opencc/`。
- 執行期 fcitx/opencc 的 `StandardPaths` 以 `pkdatadir` = `share/opencc` 定位 → profile 查得到 `.json`（含 `s2tw.json` / `tw2s.json` 等）。

**因此本專案若要一鍵切換，無需自行重編 OpenCC**：只要維持「沿用上游 prebuilt OpenCC 1.2.0 + marisa」即可，資料檔已自動隨 `prebuilt-assets` 打進 APK。

---

## 3. Addon 設定表面（Settings 頁面）

### 3.1 Addons / Simplified-Traditional 頁面（`AddonListFragment`）

- 入口：`Settings → Addons`（`app/src/main/java/…/ui/main/settings/addon/AddonListFragment.kt`）。
- 每個 addon 若 `isConfigurable && enabled`（且非 `clipboard`）顯示設定按鈕，點擊導向 `SettingsRoute.AddonConfig(displayName, uniqueName)`（`AddonListFragment.kt:113-124`）。
- chttrans addon 的 `uniqueName` 為 `"chttrans"`。

### 3.2 走法（`ConfigExternal.ETy.Chttrans` → `addonConfigPreference("chttrans")`）

`app/src/main/java/…/ui/main/settings/PreferenceScreenFactory.kt`：

```kotlin
ConfigExternal.ETy.Chttrans -> addonConfigPreference("chttrans")   // line 216
```

- `ConfigDescriptor.kt:324`：`"Chttrans" -> ConfigExternal.ETy.Chttrans`（by `Type` 字串）。
- `addonConfigPreference("chttrans")` 導向 `SettingsRoute.AddonConfig`（`SettingsRoute.kt:64`）→ `AddonConfigFragment`（`addon/AddonConfigFragment.kt`）：

### 3.3 `AddonConfigFragment`（實際讀寫 chtrans 設定）

`app/src/main/java/…/ui/main/settings/addon/AddonConfigFragment.kt`：

```kotlin
override suspend fun obtainConfig(fcitx: FcitxAPI): RawConfig {
    val addon = args.uniqueName            // = "chttrans"
    val raw = fcitx.getAddonConfig(addon)  // getFcitxAddonConfig("chttrans")
    ...
}
override suspend fun saveConfig(fcitx: FcitxAPI, newConfig: RawConfig) {
    fcitx.setAddonConfig(args.uniqueName, newConfig)  // setFcitxAddonConfig
}
```

底層 JNI/daemon：(`Fcitx.kt:134-140`) `getAddonConfig`/`setAddonConfig`（`withFcitxContext { getFcitxAddonConfig / setFcitxAddonConfig }`）。

### 3.4 設定鍵（profile 鍵）與 i18n

`modules/chttrans/chttrans.h` 的 `FCITX_CONFIGURATION(ChttransConfig, …)`：

| 鍵 | 型別 | 說明 |
|----|------|------|
| `Engine` | enum `ChttransEngine{Native,OpenCC}` | 預設 `ChttransEngine::OpenCC` |
| `Hotkey` | keylist | 預設 `Control+Shift+F`（桌面版 toggle 快鍵） |
| `EnabledIM` | hidden list<string> | 已啟用簡繁轉換的 IME uniqueName（per-IME toggle 持久化） |
| `OpenCCS2TProfile` | string + `OpenCCAnnotation` | S2T profile 名稱，預設 `"default"`（↑ 解析為 `s2tw.json`） |
| `OpenCCT2SProfile` | string + `OpenCCAnnotation` | T2S profile 名稱，預設 `"default"`（↑ 解析為 `tw2s.json`） |

**Android 端顯示為下拉選單（ListPreference）：** `chttrans.cpp::getConfig()` 列舉 opencc `PkgData` 下所有 `.json` 檔、解析其 JSON `name`、以 `OpenCCAnnotation::setProfiles` 餵給 `EnumI18n`（`chttrans.h:24-41`）。profile 顯示名取自 `opencc-profile-name.cpp` 的 `NC_("OpenCC Profile", …)` i18n。

**可用 profile（OpenCC 1.2.0 內建，`share/opencc/*.json`）：**

| profile 檔 | 用途（本專案分類） |
|-----------|------------------|
| `s2tw.json` | 簡 → 繁**台灣**（**預設 s2t**） |
| `tw2s.json` | 繁台灣 → 簡（**預設 t2s**） |
| `s2t.json` | 簡 → 繁（通用）fallback |
| `t2s.json` | 繁 → 簡（通用）fallback |
| `s2twp.json` | 簡 → 繁台灣**含詞組**（`tw2s` 的對應 s2t 詞組版，註：schema 名 `s2twp`，見下方註） |
| `tw2sp.json` | 繁台灣 → 簡**含詞組** |
| `s2hk.json` / `hk2s.json` / `t2hk.json` | 香港變體 / 通用↔繁香港 |
| `t2tw.json` / `tw2t.json` | 通用繁 ↔ 繁台灣 |
| `t2jp.json` / `jp2t.json` | 日文新字體（Shinjitai）↔ 繁體（Kyūjitai） |

> 註：Task 描述提到 `s2tw/tw2s default + s2t/t2s fallback (+ s2hk/tw2sp/hk2s/t2hk available)`。上游 `getConfig()` 會列出**所有**內建 `.json`，因此在 Android 下拉中 `s2hk/tw2sp/hk2s/t2hk` 均可選（available）。`tw2sp.json` 是「繁台灣→簡含詞組」；其反向對應 `s2twp.json` 亦內建。本專案預設採 `s2tw/tw2s`、fallback `s2t/t2s`，與上游 `updateConfig` 一致。

---

## 4. Toolbar 一鍵切換 與 per-profile 持久化接線點

### 4.1 一鍵切換（toolbar / status area）

- chtrans addon 註冊一個 `fcitx::Action`（`chttrans.h` 的 `ToggleAction`）於 `userInterfaceManager().registerAction("chttrans", &toggleAction_)`（`chttrans.cpp:83-84`）。
- `ToggleAction`：`shortText` =「繁體中文/簡體中文」、`icon` = `fcitx-chttrans-active` / `fcitx-chttrans-inactive`、`activate()` → `parent_->toggle(ic)`（`chttrans.h:97-118`）。
- toggle 邏輯（`chttrans.cpp:194-212`）：針對該 IME 的 `EnabledIM` 集合增/刪，`syncToConfig()`，再 `toggleAction_.update(ic)`。
- 轉換掛鉤：`Instance::OutputFilter`、`Instance::CommitFilter`（`chttrans.cpp:129-191`）——命中 `EnabledIM` 且 IME 語系為 zh 時，於 commit / output filter 階段跑 `convert(type, str)`。

**Android 端 toolbar 渲染（status area）：**
- `app/src/main/java/…/input/status/StatusAreaEntry.kt:35-36` 把 icon 名字對到 drawable：
  ```kotlin
  "fcitx-chttrans-active" -> R.drawable.ic_fcitx_status_chttrans_trad
  "fcitx-chttrans-inactive" -> R.drawable.ic_fcitx_status_chttrans_simp
  ```
- `StatusAreaWindow.kt:175-180`：`onStatusAreaUpdate(actions)` 把每個 `Action` 轉成 `StatusAreaEntry.fromAction`；點擊 → `activateAction(action.id)`（`StatusAreaWindow.kt:83-87`）→ `Fcitx.kt:178 activateAction(id)` → JNI `activateUserInterfaceAction` → 觸發 `ToggleAction.activate` → toggle。
- 一鍵切換即：**toolbar/status-area 上的 chttrans 動作按鈕**（切換該 IME 的 EnabledIM 位元，決定輸出的繁/簡）。

### 4.2 per-profile 持久化接線（本專案「per-profile persistence」）

per-profile 指 `OpenCCS2TProfile` / `OpenCCT2SProfile` 兩個設定值，其持久化路徑：

1. **設定寫入**：Android `AddonConfigFragment.saveConfig` → `setFcitxAddonConfig("chttrans", …)` → 桌面 fcitx 側 `Chttrans::setConfig(raw)`（`chttrans.h:126-130`）→ `config_.load(config, true)` + `fcitx::safeSaveAsIni(config_, "conf/chttrans.conf")`。
2. **持久化檔案**：`save()`（`chttrans.cpp:255-258`）`safeSaveAsIni(config_, "conf/chttrans.conf")`；`reloadConfig()`（`chttrans.cpp:214-217`）`readAsIni(config_, "conf/chttrans.conf")` → `populateConfig()`（`chttrans.cpp:219-244`）把 `engine`/`enabledIM`/hotkey 同步到後端與 toggle。
3. **per-IME（EnabledIM）持久化**：`EnabledIM` 為 hidden option，`toggle()` 時增刪、`syncToConfig()`（`chttrans.cpp:246-253`）寫回 config、`save()` 落盤。
4. **profile 下拉**：`getConfig()`（`chttrans.cpp:260-291`）每次列舉 `.json` 重設 `OpenCCS2TProfile`/`OpenCCT2SProfile` 的 `Enum` 清單。

> fcitx 的設定檔案根目錄由 fcitx5 StandardPaths 決定（Android 在 `/data/data/<pkg>/…`）。本專案 fork 時**沿用此持久化機制**（`chttrans.conf` + `EnabledIM`），即可讓「簡繁切換 + 兩個 profile 選擇」跨重開機保留。

### 4.3 接線點總表（供 Manual-QA grep）

| 接線點 | 檔案（upstream tree） | 關鍵符號/證明 |
|--------|------------------------|----------------|
| 一鍵 toggle（toolbar 動作） | `modules/chttrans/chttrans.cpp` | `registerAction("chttrans", &toggleAction_)`、`ToggleAction::activate`、`toggle(ic)` |
| 轉換掛鉤 | `modules/chttrans/chttrans.cpp` | `Instance::OutputFilter`、`Instance::CommitFilter` |
| 設定 schema（profile 鍵） | `modules/chttrans/chttrans.h` | `openCCS2TProfile` / `openCCT2SProfile` / `engine` / `EnabledIM` |
| OpenCC 後端載入 | `modules/chttrans/chttrans-opencc.cpp` | `SimpleConverter`、`s2tw.json`/`tw2s.json` 預設、`s2t/t2s` fallback |
| profile 列舉 i18n | `modules/chttrans/opencc-profile-name.cpp` | `NC_("OpenCC Profile", …)` 14 items |
| prebuilt OpenCC 消費 | `lib/fcitx5-chinese-addons/src/main/cpp/CMakeLists.txt` | `find_package(OpenCC)`、`OpenCC_DIR`、`CMAKE_FIND_PACKAGE_PREFER_CONFIG ON`、`find_package(Marisa)` |
| prebuilt 資料打包 | `app/src/main/cpp/CMakeLists.txt` | `install(DIRECTORY "${PREBUILT_DIR}/opencc/data/" DESTINATION …/opencc COMPONENT prebuilt-assets)` |
| Android 設定走法 | `app/src/main/java/…/ui/main/settings/PreferenceScreenFactory.kt:216` | `ConfigExternal.ETy.Chttrans -> addonConfigPreference("chttrans")` |
| Android AddonConfig 頁 | `app/src/main/java/…/ui/main/settings/addon/AddonConfigFragment.kt` | `getAddonConfig("chttrans")` / `setAddonConfig` |
| Android toolbar 圖示 | `app/src/main/java/…/input/status/StatusAreaEntry.kt:35-36` | `fcitx-chttrans-active/inactive` → drawable |
| Android toolbar 動作 | `app/src/main/java/…/input/status/StatusAreaWindow.kt:83-87,175-180` | `activateAction` / `onStatusAreaUpdate` |

---

## 5. 20 詞簡繁 roundtrip 測試計畫（含 tw2s/tw2sp 成語案例）

> 目的：驗證「預設 s2tw/tw2s + fallback s2t/t2s」以及可選 profile（tw2sp…）下，簡繁轉換的正確性與 roundtrip 穩定性。為**手動測試計畫**（不產生程式碼）。

### 5.1 測試標的 profile 組合

| Case | S2T profile | T2S profile | 驗證重點 |
|------|-------------|-------------|----------|
| A（預設） | `s2tw` | `tw2s` | 台灣詞彙、字對應 |
| B（fallback） | `s2t` | `t2s` | 通用轉換，非台灣地區化 |
| C（含詞組） | `s2twp` | `tw2sp` | 詞組級轉換（臺灣→裏/裡、軟件→軟體 等成語/對應） |
| D（香港選項可用） | `s2hk` | `hk2s` | 香港變體（僅驗證「available」+ 基本行為） |

### 5.2 20 詞清單（台灣/大陆混合、含 tw2s/tw2sp 成語案例）

| # | 輸入（原字） | 預期（s2tw / tw2s 方向） | 註 |
|---|--------------|--------------------------|----|
| 1 | 台湾 | 臺灣 | 地名地區化（s2tw） |
| 2 | 用户 / 用户 | 用戶 | 兩岸用語差異 |
| 3 | 软体（大陆：软件） | 軟體 | 台灣用語（s2twp 亦同） |
| 4 | 屏幕 | 螢幕 | 台灣用語（vs 大陸「屏幕/螢幕」） |
| 5 | 联网 | 連線 / 聯網 | 用語 |
| 6 | 显示器 | 顯示器 | 名詞 |
| 7 | 操作系统 | 作業系統 | 複合名詞 |
| 8 | 激光 | 雷射 | 台灣譯名 |
| 9 | 导弹 | 飛彈 | 台灣譯名 |
| 10 | 软件包 | 軟體套件 | 名詞 |
| 11 | 打印 | 列印 | 動詞用語 |
| 12 | 网络 | 網路 | 名詞 |
| 13 | 微信 | 微信 | 專名（不該被地區化誤改） |
| 14 | 台湾当局 | 臺灣當局 | 詞組 |
| 15 | 一国两制 | 一國兩制（tw2s 方向：一國兩制→一國兩制） | 政治術語 |
| 16 | 後臺 / 后臺 | 後臺 / 後台 | 後/后 區分（s2tw 常用字對應） |
| 17 | 裏 / 裡 | （tw2sp：裡） | 裏/裡 異體 |
| 18 | 位元组 | 位元組 | 術語 |
| 19 | 数据 | 資料 / 數據 | 用語（台灣常用「資料」） |
| 20 | 文件夹 | 資料夾 | 台灣用語 |

> 方向說明：簡字→繁字以 S2T profile；繁字→簡字以 T2S profile。Roundtrip 穩定性 = **case X 用第 1 個 profile 轉出後，再用對應反向 profile 轉回**，應回原（或語意等價）字。例如「台湾 →(s2tw) 臺灣 →(tw2s) 台湾」應回原。

### 5.3 測試執行方式（手動）

1. 於 fork 落地後，切換前述 profile 組合（Case A–D），在：(a) 候選詞輸出、(b) commit 後上屏 兩階段觀察。
2. 對每一詞逐一驗證「正向轉換結果」與「roundtrip 結果」。
3. 驗證 `EnabledIM` 的 per-IME 切換：切到非 zh IME 不轉；切到 zh IME 且該 IME 在 EnabledIM 中才轉。
4. 驗證 fallback：臨時移除 `s2tw.json`/`tw2s.json`（僅測試環境）→ 應自動 fallback 至 `s2t.json`/`t2s.json` 而不 crash。

### 5.4 測試計畫落點建議

- 此為 Todo 6（spec）→ 後續實作 Todo 落地後，建立對應的儀器測試 / 手動 QA 清單於專案 `docs/CHECKS/` 或測試檔。
- 上游自帶 `test/` 目錄但有成熟 IME 測試框架；本專案 fork 時可視需要對照 `fcitx5-chinese-addons/test/` 的 chttrans 測試。

---

## 6. 與本專案既有文件對應

- `docs/ARCHITECTURE.md` §2：三大支柱中「簡繁 = OpenCC，一鍵切換，預設台灣 s2tw/tw2s，fallback s2t/t2s」→ 本檔 §1.3 / §2 正是其工程依據。
- `docs/UPSTREAM.md` §3：已記錄「OpenCC 以 prebuilt `OpenCC_DIR` + `CMAKE_FIND_PACKAGE_PREFER_CONFIG ON` 消費、marisa 亦 prebuilt、版本 1.2.0」→ 本檔 §2 補足具體檔案路徑與資料打包。
- 本檔僅新增，未修改上述任何既有檔案。

---

## 7. 引用與取得命令（Manual-QA 可重現）

```powershell
# fcitx5-chinese-addons（pin）OpenCC 消費檔案
git -C <clone> show 0d3fd040:modules/chttrans/chttrans-opencc.cpp    # 預設 s2tw/tw2s
git -C <clone> show 0d3fd040:modules/chttrans/chttrans.h             # 設定 schema
git -C <clone> show 0d3fd040:modules/chttrans/CMakeLists.txt         # link OpenCC::OpenCC
git -C <clone> grep -n "CMAKE_FIND_PACKAGE_PREFER_CONFIG\|OpenCC_DIR\|find_package(Marisa)" \
    lib/fcitx5-chinese-addons/src/main/cpp/CMakeLists.txt            # prebuilt 消費

# fcitx5-android 資料打包 + Android 設定表面
git -C <clone> grep -n 'opencc/data\|prebuilt-assets' \
    6998502 -- app/src/main/cpp/CMakeLists.txt
git -C <clone> grep -n "Chttrans" \
    6998502 -- app/src/main/java/.../PreferenceScreenFactory.kt    # addonConfigPreference("chttrans")
git -C <clone> grep -n "chttrans-active\|chttrans-inactive" \
    6998502 -- app/src/main/java/.../StatusAreaEntry.kt
```

> 上述 grep 結果已於本檔 §4.3 總表與 §2.3 內文以實際輸出呈現（見 DoneClaim manual_qa artifacts）。

---

_本檔為 Todo 6 / tier MEDIUM 產物（分析 + spec，無 code）。生成於 2026-09-08，基於唯讀上游檢視；臨時 clone 已於生成後刪除（見 DoneClaim cleanup receipt）。_
