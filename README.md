# KU-Yin Keyboard for Android

> 一個開源、local-first 的 Android 注音輸入法實驗專案，使用 Android `InputMethodService`、Kotlin/View UI、JNI 與 libchewing。

[![Build](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/workflows/build.yml/badge.svg)](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/workflows/build.yml)
[![License: LGPL-2.1](https://img.shields.io/badge/License-LGPL--2.1-blue.svg)](LICENSE)

**繁體中文** · [简体中文](README.zh-CN.md) · [English](README.en.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [Español](README.es.md) · [Português](README.pt-BR.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md)

## 專案狀態

目前版本為 **`0.1.0-alpha`**。KU-Yin 已從不可用的初始 APK 恢復為可建置的 Android IME 專案：JVM 測試、Debug APK、Release APK 與 native dependency bootstrap 已能在 GitHub Actions 通過。

Android 13 emulator 已實際確認：APK 可安裝、系統可辨識 KU-Yin 為 IME、可 `enable` 並切換為預設輸入法。現階段自動化 runtime smoke 仍卡在 headless emulator 的「IME 視窗可見」斷言，因此本版本應視為 **alpha preview**，不是穩定發行版。

## 目前具備的核心能力

- 大千注音輸入與 libchewing 解碼核心
- 組字、候選字與 `InputConnection` 同步
- 候選字以 native candidate index 提交
- ASCII / 密碼欄位輸入表面
- Shift、數字、Space、Backspace、Enter 與 editor action
- 游標／selection 變更後的 composition reconciliation
- `IME_FLAG_NO_PERSONALIZED_LEARNING` 對應的個人化學習控制
- `armeabi-v7a`、`arm64-v8a`、`x86`、`x86_64` 四種 ABI
- 依賴固定版本的 libchewing native bootstrap

## 下載與安裝

請從 [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases) 下載最新 alpha 預覽版。`APK/README.md` 會列出目前 APK 類型與 SHA-256。

目前會提供兩種產物：

- **Debug APK**：debug-signed，可直接安裝測試。
- **Release unsigned APK**：未簽章的開發者產物，用於驗證 release build；它不是正式可發布安裝包。

安裝後，在 Android 的「設定 → 系統／一般管理 → 鍵盤／語言與輸入 → 螢幕鍵盤／管理鍵盤」中啟用 KU-Yin，再透過輸入法切換器選擇 KU-Yin。各品牌 Android 的選單名稱可能不同。

## 隱私與安全

輸入法可以接觸高度敏感的文字內容，因此 KU-Yin 以 local-first 為基本原則。現有輸入流程不需要雲端服務才能完成注音解碼；密碼欄位、ASCII 強制模式與禁止個人化學習等 editor policy 會在 IME 層處理。

在正式處理敏感資料前，請先閱讀 [SECURITY.md](SECURITY.md)、原始碼與已知限制。本專案仍處於 alpha 階段，不應把目前的 CI 綠燈等同於所有 OEM／Android 版本都已完成安全與相容性驗證。

## 自行建置

主要工具鏈：

- JDK 17
- Android SDK / compileSdk 33
- Gradle 7.6.4
- NDK `27.3.13750724`
- CMake `3.22.1`

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest
gradle assembleDebug
gradle assembleRelease
```

更完整的環境與驗證流程請看 [BUILD.md](BUILD.md)。

## 架構概覽

```text
Android InputMethodService
        │
        ├── EditorPolicy
        ├── ImeSessionController
        ├── KeyboardView / CandidateView
        └── ChewingEngine
               └── AndroidChewingEngine
                      └── JNI / libchewing
```

輸入法 service、editor policy、session state、UI 與 native decoding 盡量分離，讓核心行為可以被 JVM／instrumentation 測試覆蓋。

## 已知限制

- Android 13 headless emulator 的 IME-window-visible smoke 目前仍未通過；install/register/enable/select 已確認成功。
- `compileSdk` / `targetSdk` 目前仍為 API 33，之後需要獨立升級。
- OpenCC 轉換目前不是完整 production feature。
- Hsu / Eten26 尚未作為完整使用者布局提供。
- per-key accessibility virtual nodes、完整符號／Emoji、clipboard、gesture typing、prediction 等仍在後續範圍。
- 正式 release signing 尚未建立，因此目前 Release APK 仍為 unsigned developer artifact。

## 參與開發

請先閱讀 [CONTRIBUTING.md](CONTRIBUTING.md)、[docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) 與 [docs/NEXT_STEPS.md](docs/NEXT_STEPS.md)。Bug、相容性結果與可重現的裝置測試都很有價值。

## 授權與第三方元件

本 repository 根授權文件為 [GNU LGPL 2.1](LICENSE)。第三方元件、libchewing 與 native dependency 的來源與授權資訊請一併參考 [NOTICE](NOTICE)。
