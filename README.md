# KU-Yin 酷音注音鍵盤 for Android

> 狀態：alpha（`0.2.0-alpha.1`，versionCode 20001）。Compose 產品主線開發中，所有行為聲明以實機驗證為準。
>
> 其他語言：[English](docs/i18n/README.en.md) · [简体中文](docs/i18n/README.zh-CN.md) · [日本語](docs/i18n/README.ja.md) · [한국어](docs/i18n/README.ko.md) · [Русский](docs/i18n/README.ru.md) · [Deutsch](docs/i18n/README.de.md) · [Français](docs/i18n/README.fr.md) · [Español](docs/i18n/README.es.md) · [Português](docs/i18n/README.pt.md)

臺灣大千注音 Android 輸入法。Jetpack Compose 產品 UI，中文解碼正由靜態詞表（`ZhuyinDictionary`，過渡）遷往 libchewing 權威解碼（`ChewingEngine`／JNI 已編入 APK，尚未接線）。

## 功能（已實作，未實機驗收前皆視為 UNVERIFIED）

- 大千注音輸入、候選橫條、選字、空白／Backspace／Enter
- 英文 QWERTY（含 Shift／CapsLock）、符號頁、emoji 頁
- 深色模式、多套鍵盤配色、候選字級、震動／音效開關、鄰鍵容錯
- 候選排序：精確匹配 ＞ 有效延續 ＞ 聲調回退；學習只在同層內調整順序
- 按鍵觸控格全覆蓋（視覺鍵帽維持 44dp）；邊鍵觸控貼屏緣
- 使用者回饋（本機 Room 儲存）、開源捐贈卡（見下）

## 隱私與安全（程式實測）

- 零權限：無 `INTERNET`、無 `VIBRATE`；`allowBackup=false`
- 全本機：輸入不傳輸、不記錄按鍵明文；詞庫與 userdict 在 app-private `noBackupFilesDir`
- 原生依賴固定 revision（`scripts/bootstrap_native_deps.sh`：fcitx5-android/prebuilt `3587ba33`、libchewing `a6a8fa4`），非浮動分支

## 建置

需求：JDK 21、Android SDK（含 NDK 28.2、CMake 3.22.1）。

```bash
bash scripts/bootstrap_native_deps.sh   # 一次性抓取 pinned 原生依賴＋詞庫
./gradlew :app:testDebugUnitTest        # 61 測試
./gradlew :app:assembleDebug            # app/build/outputs/apk/debug/app-debug.apk
```

合約檢查：`scripts/check_*.py`（dachen／P0／editor_sync／P1／P2／P3）。Application ID：`io.github.juhjuhx.kuyin`。

## 架構（收斂中）

```
KuYinInputMethodService → KuYinEngine → ZhuyinDictionary（過渡）→ Compose UI → InputConnection
                                     ↘（C1–C3 已備）ComposeDecoderSession ← ChewingEngine ← AndroidChewingEngine ← JNI ← libchewing
```

C1–C3（`ime/session/`：`ComposeImeState`／`ImeCommand`／雙適配器，RED-first 測試）已合入；C4 生產切換需實機 Gate（CASE 4 選字後續打、C6 轉屏）通過才走。舊 View shell（PR #4）僅為 donor，不再是產品 UI。

## 安裝與啟用（實機）

1. 安裝 Debug APK（debug 簽章，僅供測試）。
2. 系統設定 → 語言與輸入法 → 啟用「酷音注音輸入法」。
3. 切換為目前輸入法；App 內主畫面可即時試打、調主題／字級／震動。
4. 回報請用 App 內回饋卡或 GitHub Issues（皆為本機／瀏覽器 Intent，無遙測）。

## 開源捐贈

作者不收個人贊助。App 內開源捐贈卡提供 FSF／ASF／Open Source Initiative／EFF 捐款連結＋QR 風格圖（`OpenSourceDonationCard.kt`，Canvas 手繪 21×21 模組圖；**是否真可掃描待實機驗證**，驗過前請視為裝飾）。

## 授權

- 本專案：LGPL-2.1（見 `LICENSE`）
- libchewing：上游 https://codeberg.org/chewing/libchewing（LGPL-2.1 家族），出處與 pin 見 `NOTICE`＋`docs/UPSTREAM.md`
- 第三方：AndroidX／Compose／Room（Apache-2.0）、JUnit／Robolectric／Roborazzi（僅測試）
- 研究參考（fcitx5／FlorisBoard／HeliBoard）未拷貝，禁未審援引

## 文件

- `docs/COMPOSE-MAINLINE.md`：主線檔案審計（保留／刪除收錄／新增）、決策與路線圖
- `docs/ARCHITECTURE.md`：描述舊 View 架構（待 C12 重寫，現為歷史）
- `SECURITY.md`：安全聲明（與程式一致）
- `docs/ROADMAP.md`、`docs/HISTORY.md`：歷史路線與沿革（部分條目已過時，見審計文檔）
- 注意：尚無 `PRIVACY.md`；`docs/ARCHITECTURE.md`、`docs/DEVELOPMENT.md` 部分內容已過時（分支錯位／舊工具鏈），以本 README＋程式為準

## 已知限制（誠實）

- 未實機驗收前所有行為皆 UNVERIFIED；CASE 4（選字後續打）、CASE 6（轉屏自愈）為 hard blocker
- Release unsigned；無 AAB；F-Droid source lane 缺（預編譯＋網路構建）；Play Data Safety 不得推斷
- 英文第二排 16dp stagger 側死區保留（等視覺基線決策）
