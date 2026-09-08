# 上游追蹤（UPSTREAM）

> 狀態：**文件記錄（docs-only，Todo 2 / tier LIGHT）**。
> 本檔記錄上游 `fcitx5-android` 的 remote / branch / HEAD 快照、完整 submodule 釘選表，以及 **libchewing `chewing_Reset` 行為變更風險**。所有 SHA 與日期皆為「ⓘ 擷取當下」的唯讀快照，非本專案實際 clone 內容（本目錄仍為 fork-mirror 骨架，尚未實抓）。

---

## 1. 上游 remote / branch / HEAD 快照

| 項目 | 值 |
|------|----|
| remote 名稱 | `upstream` |
| remote URL | `https://github.com/fcitx5-android/fcitx5-android.git` |
| 追蹤分支 | `master` |
| HEAD SHA | `6998502528cd26efb6079556da92003638e4179c` |
| 擷取日期 | 2026-09-08 |
| HEAD 提交日期 | 2026-08-21（`Rocka`；訊息：`Update fcitx5 submodules`） |
| 上游授權 | LGPL-2.1 |

> ⚠️ **staleness 提醒**：以上 SHA 為 2026-09-08 擷取的**靜態快照**。上游 `master` 變動快速（最後一筆即為「更新 submodules」），任何後續作業前請重新 `git ls-remote` 校驗，勿以本檔快照當作永遠有效。

---

## 2. 完整 submodule 狀態表

以下為上游 `6998502528…` 的 `git ls-tree -r HEAD` 中所有 `160000 commit`（gitlink）釘選。`fcitx5-android` 的 submodule 透過 `.gitmodules` 定義；**libchewing / OpenCC 並非頂層 submodule**，而是經由 `prebuilt` submodule 內的**預編譯靜態庫**提供（見 §3）。

| 路徑 | 釘選 SHA | 上游 URL |
|------|----------|----------|
| `lib/fcitx5/src/main/cpp/fcitx5` | `442edbc9b1880c766ff9cc9a9ac67e864b200595` | https://github.com/fcitx/fcitx5.git |
| `lib/fcitx5/src/main/cpp/prebuilt` | `86ce2c95d42f1132746fbf60c278193aa1f4b758` | https://github.com/fcitx5-android/prebuilt.git |
| `lib/fcitx5-lua/src/main/cpp/fcitx5-lua` | `05db9ee519d448a64ccbe216044e8e0342e8c536` | https://github.com/fcitx/fcitx5-lua.git |
| `lib/libime/src/main/cpp/libime` | `65003b6020623affcdbda14ce9292f408fb7a3ad` | https://github.com/fcitx/libime.git |
| `lib/fcitx5-chinese-addons/src/main/cpp/fcitx5-chinese-addons` | `0d3fd0408d03abc63cdb700559f85a387b98735f` | https://github.com/fcitx/fcitx5-chinese-addons.git |
| `plugin/anthy/src/main/cpp/anthy-cmake` | `627b94e60320b6ef3ca5cc404c22c84649b76f73` | https://github.com/fcitx5-android/anthy-cmake.git |
| `plugin/anthy/src/main/cpp/fcitx5-anthy` | `84bf0376cdb924a89c41bc7fadeb49d78bad385c` | https://github.com/fcitx/fcitx5-anthy.git |
| `plugin/unikey/src/main/cpp/fcitx5-unikey` | `366b858db1c0f464102857021d7af9b75e14ebd8` | https://github.com/fcitx/fcitx5-unikey.git |
| `plugin/rime/src/main/cpp/fcitx5-rime` | `4e996319edea790495edc2c91893e9af4c4e6d6a` | https://github.com/fcitx/fcitx5-rime.git |
| `plugin/rime/src/main/cpp/rime-prelude` | `082425ea0684bca36474415d4a0e8db9b016487e` | https://github.com/rime/rime-prelude.git |
| `plugin/rime/src/main/cpp/rime-essay` | `e9b1a374a6ea015fca5bdd04318924b4483ac35a` | https://github.com/rime/rime-essay.git |
| `plugin/rime/src/main/cpp/rime-luna-pinyin` | `56b934b099dfbeab842320f13aa8b461a6ab3e42` | https://github.com/rime/rime-luna-pinyin.git |
| `plugin/rime/src/main/cpp/rime-stroke` | `3a4b0f4013e2b4c14b1e80c92b1d4723eb65f39c` | https://github.com/rime/rime-stroke.git |
| `plugin/hangul/src/main/cpp/fcitx5-hangul` | `9357892335b7f4a38a885f7c09113295f23c5d4a` | https://github.com/fcitx/fcitx5-hangul.git |
| `plugin/chewing/src/main/cpp/fcitx5-chewing` | `07eddb16961b18765e67cec538708b6964baa57c` | https://github.com/fcitx/fcitx5-chewing.git |
| `plugin/sayura/src/main/cpp/fcitx5-sayura` | `43ea084170dfe0496fcd9f2dbfc712cc7d7eef64` | https://github.com/fcitx/fcitx5-sayura.git |
| `plugin/jyutping/src/main/cpp/libime-jyutping` | `aeb0d010e0b945d894f60b48a952a59820d85f46` | https://github.com/fcitx/libime-jyutping.git |
| `plugin/clipboard-filter/ClearURLsRules` | `11086f40512774dcadef54079f1ba023bfacf940` | https://github.com/ClearURLs/Rules.git |
| `plugin/thai/src/main/cpp/fcitx5-libthai` | `8bfa27d7ae675fda3257f21e691cd5c285663e6f` | https://github.com/fcitx/fcitx5-libthai |

### 2.1 關鍵 submodule（本專案關注重點）釘選日期

| 模組 | 釘選 SHA | 提交日期（UTC） |
|------|----------|-----------------|
| `fcitx5-chinese-addons`（簡繁/OpenCC 消費端） | `0d3fd040…b98735f` | 2026-08-17 |
| `fcitx5-chewing`（libchewing 橋接） | `07eddb16…baa57c` | 2026-06-20 |
| `fcitx5` | `442edbc9…200595` | 2026-08-20 |
| `fcitx5-lua` | `05db9ee5…8c536` | 2026-06-20 |
| `libime` | `65003b60…a3ad` | 2026-08-20 |

> 追蹤建議：因 fcitx5-android 會週期性「Update fcitx5 submodules」，若要維持可重現建置，**務必鎖定上述 gitlink（或本快照）**，勿在 feature 分支直接追蹤子模組的 remote head。

---

## 3. libchewing 與 OpenCC 的釘選方式（重要）

`fcitx5-android` **不以頂層 submodule 釘選 libchewing / OpenCC**，而是透過 `prebuilt` submodule（`86ce2c95…`）提供**跨 ABI 的預編譯靜態庫**，再由 plugin/lib 的 `CMakeLists.txt` 用 `find_package` 消費：

- **libchewing**：`plugin/chewing/src/main/cpp/CMakeLists.txt` 以 `Chewing_static STATIC IMPORTED` 引入 `${PREBUILT_DIR}/libchewing/${ANDROID_ABI}/lib/libchewing_capi.a`；字典檔亦由 `prebuilt/chewing-dict/` 安裝。
- **OpenCC**：`lib/fcitx5-chinese-addons/src/main/cpp/CMakeLists.txt` 以 `find_package(OpenCC)`（`OpenCC_DIR = ${PREBUILT_DIR}/opencc/…`，並設 `CMAKE_FIND_PACKAGE_PREFER_CONFIG ON`）引入預編譯 `libopencc`；其所需 marisa 亦 `find_package(Marisa)` 自 prebuilt。
- **libchewing 最低要求**：`fcitx5-chewing` 的 `CMakeLists.txt` 宣告 `chewing>=0.5.0`（pkg-config），但在 Android 建置中其實際由**預編譯 libchewing 版本決定**。

### 3.1 prebuilt 內實際釘選版本（自 prebuilt `86ce2c95…` 解讀）

| 元件 | prebuilt 內版本 | 來源 |
|------|-----------------|------|
| **libchewing** | `a6a8fa4-dirty`（git commit `a6a8fa4`，dirty build） | `libchewing/…/lib/cmake/Chewing/ChewingConfigVersion.cmake` → `set(PACKAGE_VERSION "a6a8fa4-dirty")` |
| **OpenCC** | `1.2.0` | `opencc/…/lib/cmake/opencc/OpenCCConfigVersion.cmake` → `set(PACKAGE_VERSION "1.2.0")` |
| prebuilder 工具鏈 | NDK `28.0.13004108` / CMake `3.31.6` / rust `1.92.0` / prebuilder `eb156443…` | `toolchain-versions.json` |

> 本專案沿用這些版本時，請以「**維持與 upstream prebuilt 相同**」為原則，避免自行重編 libchewing / OpenCC（改動 `chewing_Reset` 語意風險見 §4）。

---

## 4. ⚠️ 行為變更風險：libchewing `chewing_Reset`

> 摘自上游 issue：**fcitx5-android#836**（`chewing_Reset` 語意差異）。

| 版本 | `chewing_Reset` 語意 |
|------|------------------------|
| libchewing **0.8.5**（C 舊版） | **reset-all-settings**：重置所有設定狀態 |
| libchewing **rust 0.11.0**（重寫版） | **reset-context-keep-settings**：僅重置輸入上下文，**保留設定** |

在 fcitx5-android 生態中，`chewing_Reset` 被呼叫以在切換/中斷輸入時清理狀態。若底層 libchewing 從舊 C 版升級到 rust 重寫版，`Reset` 的副作用會從「連設定一起清掉」變成「保留設定」——兩者**不可互通、不可互換解讀**。

### 4.1 本專案緩解策略（已決定）

1. **維持釘選的 plugin 版本**：鎖定 `fcitx5-chewing` 於 `07eddb16…`（與其對應的 prebuilt libchewing `a6a8fa4-dirty`），**不主動升級 libchewing**。
2. **永不「重新解讀」語意**：不解釋成「舊版=對/新版=對」，也不在程式碼層把兩種語意互相轉換/隱藏。只允許「整包升級（plugin + libchewing + 相關測試）+ 語意重新評估」的整體變更。
3. 若未來要升級 rust 版，需**先擴充測試**覆蓋 `chewing_Reset` 後的「設定是否保留」行為，再切換版本。

---

## 5. 快照取得命令（可供重現 / Manual-QA）

```powershell
# HEAD SHA + 分支（ls-remote）
git ls-remote https://github.com/fcitx5-android/fcitx5-android.git HEAD refs/heads/master
#   → 6998502528cd26efb6079556da92003638e4179c  HEAD
#   → 6998502528cd26efb6079556da92003638e4179c  refs/heads/master

# 淺克隆到唯讀暫存目錄（D:\Temp\opencode，事後刪除）
git clone --depth 1 --branch master --single-branch `
  https://github.com/fcitx5-android/fcitx5-android.git D:\Temp\opencode\android-keyboard-upstream

# HEAD 提交日期
git -C D:\Temp\opencode\android-keyboard-upstream log -1 --format="%H%n%ad%n%an%n%s" --date=iso-strict
#   → 6998502528cd26efb6079556da92003638e4179c / 2026-08-21T23:27:37+08:00 / Rocka / Update fcitx5 submodules

# 完整 submodule gitlink SHA
git -C D:\Temp\opencode\android-keyboard-upstream ls-tree -r HEAD | Select-String "160000"

# prebuilt 內的 libchewing / OpenCC 版本（由 pinned prebuilt SHA 讀）
#   libchewing/…/ChewingConfigVersion.cmake  → PACKAGE_VERSION "a6a8fa4-dirty"
#   opencc/…/OpenCCConfigVersion.cmake        → PACKAGE_VERSION "1.2.0"
```

> **清理憑據**：上述暫存目錄 `D:\Temp\opencode\android-keyboard-upstream` 於本檔產生後已 `Remove-Item -Recurse -Force` 刪除（見 DoneClaim cleanup receipt），未於任何目標目錄留下 clone / fetch / 上游原始碼。

---

## 6. 引用

- 上游：https://github.com/fcitx5-android/fcitx5-android（`master`，LGPL-2.1）
- 上游相關 issue：fcitx5-android#836（`chewing_Reset` 語意變更）
- 依賴 prebuilt：https://github.com/fcitx5-android/prebuilt
- 專案內相依文件：`docs/ARCHITECTURE.md`、README.md（§ 授權與上游聲明）
