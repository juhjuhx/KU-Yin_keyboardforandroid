# KU-Yin Keyboard for Android

> 一個以臺灣注音輸入為核心、local-first 的開源 Android 輸入法。Android 端使用 Kotlin/View 與 `InputMethodService`，透過 JNI/C++ 呼叫 libchewing C API。

[![Build](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/workflows/build.yml/badge.svg)](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/workflows/build.yml)
[![License: LGPL-2.1](https://img.shields.io/badge/License-LGPL--2.1-blue.svg)](LICENSE)

**繁體中文** · [简体中文](docs/i18n/README.zh-CN.md) · [English](docs/i18n/README.en.md)

## 上游與技術基礎

KU-Yin 的核心不是自造中文解碼器，而是在 Android IME 層把成熟的上游能力以可測試、可追溯的方式接入：

| 上游 / 基礎 | KU-Yin 中的角色 | 目前邊界 |
|---|---|---|
| [libchewing](https://codeberg.org/chewing/libchewing) | 注音解碼、候選與使用者詞典核心 | 透過 C API / JNI 使用；正式開發已遷移至 Codeberg |
| [fcitx5-android/prebuilt](https://github.com/fcitx5-android/prebuilt) | Android 四種 ABI 的 libchewing 預建來源 | 固定 commit `3587ba3355711f0aca50136e787719f6562676b8` |
| libchewing source pin | 上述 prebuilt 對應的原始碼來源 | `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c` |
| Android `InputMethodService` / `InputConnection` | 系統 IME 生命週期與編輯器介面 | KU-Yin Android adapter 的平台契約 |
| Kotlin / Android View | 鍵盤、候選列、session 與 editor policy | KU-Yin 自有 Android 層 |
| C++ JNI bridge | Kotlin 與 libchewing C API 的邊界 | KU-Yin 自有 native adapter |

精確的 provenance、參考專案與授權邊界請見 [`docs/UPSTREAM.md`](docs/UPSTREAM.md) 與 [`NOTICE`](NOTICE)。KU-Yin **沒有自有 Rust 層**；現代 libchewing 上游大量採用 Rust，不代表本專案需要為了技術棧外觀額外引入 Rust。

## 目前狀態

目前原始碼版本為 **`0.1.1-alpha`**。這是可建置、可安裝測試的 alpha，並非穩定版。

目前已建立的驗證鏈包含 JVM 單元測試、輸入映射／架構 contracts、固定 native dependency bootstrap、Debug APK、unsigned Release APK，以及 Android 13 emulator 的安裝、IME 註冊、啟用與選擇。headless emulator 的 IME window-visible assertion 仍屬實驗性、非阻塞檢查；實際 OEM／實機相容性仍需持續擴充。

下載請使用 [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases)。Debug APK 為 debug-signed，可直接安裝測試；Release APK 目前尚未配置正式簽章，因此只作為 release-build developer artifact。

## 核心能力

- 大千（Dachen）注音鍵位與 libchewing 解碼
- 組字、候選與 Android `InputConnection` 同步
- 候選透過 native candidate index 提交
- ASCII／密碼欄位輸入介面
- Shift、數字、Space、Backspace、Enter 與 editor action
- 游標／selection 改變後的 composition reconciliation
- `IME_FLAG_NO_PERSONALIZED_LEARNING` 對應的學習控制
- `armeabi-v7a`、`arm64-v8a`、`x86`、`x86_64` 四種 ABI
- app-private、no-backup 的 libchewing 資料目錄
- 上游 native 輸入固定 revision，缺少必要檔案時 build 會明確失敗

## 安裝與使用指南

1. 從 Releases 下載最新 **Debug APK**。
2. 安裝 APK。Android 會提示輸入法屬於可讀取輸入內容的高敏感系統元件，這是 IME 正常的安全提示。
3. 到「設定 → 系統／一般管理 → 語言與輸入／鍵盤 → 螢幕鍵盤／管理鍵盤」啟用 **KU-Yin**。不同 OEM 的選單名稱可能不同。
4. 開啟任意文字輸入框，透過系統輸入法切換器選擇 KU-Yin。
5. 一般文字欄位使用大千注音；密碼或強制 ASCII 欄位會進入 ASCII-oriented session。
6. 若遇到鍵盤不出現、候選異常或 OEM 特定問題，請在 Issue 附上 Android 版本、裝置型號、可重現步驟，以及 Logcat 中**不含敏感輸入內容**的錯誤資訊。

## 架構

```text
Android Framework
EditorInfo / InputConnection / IME lifecycle
                    │
                    ▼
        ChewingInputMethodService
                    │
        ┌───────────┼───────────────┐
        ▼           ▼               ▼
  EditorPolicy  ImeSession      KeyboardView /
                 Controller      CandidateView
        │           │               │
        └───────────┴───────┬───────┘
                            ▼
                    ChewingEngine contract
                            │
                            ▼
                  AndroidChewingEngine
                            │
                            ▼
                       JNI / C++
                            │
                            ▼
                    libchewing C API
```

Android 生命週期、隱私策略、session 狀態、UI 與 native decoder 盡量分層，避免 View 直接依賴 JNI。詳細說明請見 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)。

## 隱私、安全與資料外洩邊界

目前原始碼沒有申請 Android `INTERNET` permission，也沒有發現網路輸入路徑、Clipboard 功能，或將按鍵／候選明文寫入日誌的程式碼。libchewing system/user data 位於 `noBackupFilesDir`，Manifest 關閉 Android backup。敏感 editor 會停用候選／composition，並可關閉 personalized learning。

這些控制不代表「絕對安全」。使用者詞典本身可能包含敏感詞彙，目前依賴 Android app-private storage，而非應用層額外加密；root／已遭入侵的裝置不在此威脅模型內。native prebuilt 雖固定 commit，但尚未建立原始碼重編與逐檔 hash 等價驗證。完整審計請見 [`docs/SECURITY_AUDIT.md`](docs/SECURITY_AUDIT.md) 與 [`SECURITY.md`](SECURITY.md)。

## 自行建置

CI 參考工具鏈：JDK 17、Gradle 7.6.4、AGP 7.4.2、Kotlin 1.9.22、Android API 33、NDK `27.3.13750724`、CMake `3.22.1`。

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest --stacktrace
gradle assembleDebug --stacktrace
gradle assembleRelease --stacktrace
```

Debug APK：`app/build/outputs/apk/debug/app-debug.apk`

Unsigned Release APK：`app/build/outputs/apk/release/app-release-unsigned.apk`

環境、CI 與 release 說明請見 [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md)。

## 上游更新策略

`.github/workflows/upstream-watch.yml` 定期檢查 `fcitx5-android/prebuilt` 與官方 libchewing release。發現變化時只建立／更新審查 Issue，不會直接修改 `main`。任何 native pin 更新都必須重新通過 bootstrap、JVM tests、Debug/Release APK build 後才能合併。

## Roadmap

後續優先級請見 [`docs/ROADMAP.md`](docs/ROADMAP.md)。近期重點包括實機／OEM matrix、正式 release signing、API/target SDK 現代化、per-key accessibility、符號／Emoji 與後續布局；OpenCC、prediction、gesture typing 等功能在真正接通前不會列為已完成能力。

## 專案迭代記錄

KU-Yin 曾經歷「可產生 APK，但實際不可用」的早期階段，之後透過 P0–R4 recovery，分階段恢復 Manifest、libchewing/JNI、輸入語義、UI lifecycle、privacy policy、CI、runtime smoke 與 release flow。完整迭代報告與設計記憶請見 [`docs/HISTORY.md`](docs/HISTORY.md)。

## 致謝與貢獻記錄

專案維護者：**juhjuhx**。

開發過程曾使用 OpenCode／Muse Spark、Hermes／Agnes，以及 ChatGPT／GPT-5.6 Sol。這裡的 AI attribution 只表示這些系統的輸出曾實質影響 repository；不將 AI 系統視為人類 GitHub 身分、法律作者或著作權主體。即使部分早期實作後來被替換，相關參與脈絡仍保留在迭代記錄中。

更重要的技術基礎來自 libchewing、fcitx5-android prebuilt、Android Open Source Project／Android Developers，以及社群鍵盤專案帶來的架構與 UX 參考。生產依賴與 reference-only 專案會在 [`docs/UPSTREAM.md`](docs/UPSTREAM.md) 明確區分。

## 參與開發

請先閱讀 [`CONTRIBUTING.md`](CONTRIBUTING.md)。提交應優先附上可重現問題、最小修改、對應測試與驗證證據。輸入法屬於敏感元件，不接受未經說明的網路輸入、按鍵內容遙測，或直接複製第三方實作的 PR。

## License

KU-Yin repository 使用 [`GNU LGPL 2.1`](LICENSE)。第三方來源、固定 revision 與歸屬請見 [`NOTICE`](NOTICE)／[`docs/UPSTREAM.md`](docs/UPSTREAM.md)。對於靜態連結 libchewing 的再發布義務，發行者應自行確認 LGPL 對原始碼提供與可重新連結能力的具體要求；本專案文件不構成法律意見。
