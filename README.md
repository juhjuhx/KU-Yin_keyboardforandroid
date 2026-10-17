# KU-Yin 酷音｜台灣大千注音 Android 鍵盤

> **目前版本：實驗性 Alpha `0.2.0-aie.1`（Debug APK）**。已公開下載，但完整實機驗收、正式簽章與 Stable Release 尚未完成。請先閱讀下方「目前狀態與限制」。

**KU-Yin** 是以**台灣大千注音**為核心的離線 Android 開源輸入法。鍵盤介面使用 **Kotlin／Jetpack Compose**，中文解碼採 **libchewing（酷音）**，英文補全使用本機詞表。目標是讓注音組字、選字與候選翻頁更易用，同時保留清楚的資料權責、可維護的原生依賴及社群開發流程。

[📦 下載 Alpha 與原始碼](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1) · [🧭 架構](docs/ARCHITECTURE.md) · [📋 開發狀態](docs/CURRENT_STATE.md) · [🐛 問題回報](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues)

## 功能

| 已有功能 | 目前能做什麼 | 邊界 |
|---|---|---|
| **大千注音** | 固定 41 鍵大千配置、中文組字、無聲調候選 | P0 基本輸入有使用者實機回報；尚非完整裝置驗收 |
| **候選瀏覽** | 單列橫向捲動、展開網格、上下頁；候選身分／generation 防過期選取 | AIE-S1 已實作；完整手機瀏覽體驗待驗 |
| **英文字詞** | QWERTY、Shift／CapsLock、約 10,000 筆離線英文字首補全 | 部分跨 Editor 情境待補 |
| **基本符號與 Emoji** | 符號／Emoji 模式與基本標點 | 尚無完整符號搜尋、分類目錄與最近使用 |
| **設定** | 既有配色、字級、鍵盤高度、震動／音效等項目 | OEM、TalkBack、極端字級仍待驗 |
| **本機 Decoder** | JNI 與固定版本 libchewing；啟動失敗時的內建字典 fallback | 不等於所有裝置與生命週期已驗收 |

**目前未內建**：Whisper／語音輸入、AI 雲端候選、自適應觸控、AIE-S2 候選重排、完整個人學習清除。Windows 語音實驗分支也不是 Android 版功能。

## 安裝 Alpha

1. 前往 [`v0.2.0-aie.1` 發布頁](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1)，下載 **`KU-Yin-AIE-S1-candidate-browser-debug.apk`**。
2. 使用同頁 `SHA256SUMS` 核對，公開 APK SHA256：

   ```text
   acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634
   ```
3. 在 Android 系統輸入法設定啟用並選取 KU-Yin，先用非敏感測試文字練習候選／翻頁／選字。

**更新須知：** 此版本仍以既有 Debug 憑證簽章。自編 APK 可能用不同憑證，不能保證原地更新。若系統提示簽章不符或版本降級，請停止，**不要因測試而卸載或清除個人字典**。

## 目前狀態與已知限制

截至 **2026-10-17**，公開 Alpha 與後續尚未合併的測試分支必須分別看待：

- **公開 Alpha**：原 API 33 App 5 PASS；Host **15 PASS／9 FAIL／8 SKIP**（發布時基線）。
- **R2 測試分支**：曾在 API 33 取得 App **5 PASS**、Host **24 PASS／9 SKIP**；24 項含一項測試基礎設施檢查，實際產品案例為 **23 PASS／9 INCONCLUSIVE**。不是 `main` 新版本的完整驗收。
- **R2.1/R2.2 後續調查**：仍有 IME visibility/binding 阻擋。R2.2 A/B 都是 App **4 PASS／1 FAIL**，並在安裝 KU-Yin 前偵測到 **System UI ANR**；歷史 remote/session 根因仍 **UNRESOLVED**。
- **實機與正式版**：AIE-S1 完整真機候選瀏覽、不同 OEM、TalkBack、學習清除、正式 release key、商店與穩定版均 **OPEN／NOT_READY**。

請勿把 CI 編譯成功、模擬器局部成功或使用者基本打字回報當作整套功能通過。正式的 [Release readiness](docs/release/RELEASE_READINESS.md) 與 [AIE Roadmap](docs/roadmap/AIE_V1_ROADMAP.md) 仍是工作基礎。

## 隱私與安全

- Android 版目前沒有請求 **`INTERNET`** 權限；主要鍵盤輸入、中文組字與英文補全在裝置本機執行。
- 會依密碼、非文字欄位與 `IME_FLAG_NO_PERSONALIZED_LEARNING` 採用限制學習政策。
- 原生使用者字典在 App 私有的 `noBackupFilesDir`；**排除 Auto Backup 不等於額外加密**。其他偏好／Room 回饋資料的 Android 12+ 裝置轉移細節仍待驗。
- 「重設學習」因無法確保所有 Native／fallback 資料真正清除，現已停用並顯示不支援。
- 回報問題時請勿公開張貼密碼、個人輸入內容、使用者字典或未遮蔽系統日誌。

詳見 [Security Policy](SECURITY.md)、[資料儲存與隱私架構](docs/architecture/07-DATA-STORAGE-AND-PRIVACY.md)。

## 架構

```text
Android Editor / InputConnection
  ↕
KuYinInputMethodService (IME lifecycle / editor policy / text effects)
  ├─ ComposeDecoderSession (中文組字唯一 session owner)
  │   ├─ ChewingEngineSession → AndroidChewingEngine → JNI → libchewing
  │   └─ ZhuyinDictionarySession (native 啟動失敗時 fallback)
  ├─ EnglishWordSession → offline EnglishLexicon
  └─ Compose UI → CandidateStrip / ExpandedCandidateGrid
```

UI 只發送命令與呈現快照；文字提交由 Service 負責。候選預覽使用隔離的 scratch native context，避免破壞正在輸入的 live composition；Editor ACK 與 libchewing Native ACK 是不同的確認層。詳見 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)。

## 本機建置

需求：**JDK 21、Android SDK、NDK `28.2.13676358`、CMake `3.22.1`**。

```bash
bash scripts/bootstrap_native_deps.sh
python3 -m unittest discover -s scripts/tests -v
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

這是建置步驟，不代表本文編輯時已重新運行 Android 測試。新產生的 Debug APK 也不保證與公開 Alpha 的簽章相同；正式 CI、Host 與裝置測試請參考 [開發文件](docs/DEVELOPMENT.md)。

## 上游致謝與授權

KU-Yin 的核心技術建立在社群開源成果之上，特別感謝：

- [libchewing／酷音](https://codeberg.org/chewing/libchewing)：注音解碼、候選、使用者字典及學習。
- [Fcitx5 for Android／prebuilt](https://github.com/fcitx5-android/prebuilt)：固定版本的 Android Native archives、headers 和詞庫材料。
- [Android Open Source Project](https://source.android.com/)、AndroidX／Jetpack Compose：Android IME 平台與介面元件。
- AOSP LatinIME：離線英文詞表來源，詳見 `app/src/main/assets/english/SOURCE.md`。
- [Fcitx5 for Android](https://github.com/fcitx5-android/fcitx5-android)、[FlorisBoard](https://github.com/florisboard/florisboard)、[HeliBoard](https://github.com/HeliBorg/HeliBoard)：行為、操作與 UI 設計研究參考；**不表示直接複製其程式碼或素材**。

本專案倉庫提供 GNU **LGPL-2.1** 授權文字（`LICENSE`）；其他程式與詞庫各有適用授權。固定原生依賴、來源、NOTICE 與重鏈結材料見 [NOTICE](NOTICE)、[UPSTREAM](docs/UPSTREAM.md) 和 [Alpha 授權／建置紀錄](docs/release/AIE_ALPHA_LICENSE_AND_BUILD.md)。完整正式發行前仍需逐項審核授權與來源。

## 直接支持開源社群

開發者目前**不收取個人贊助**；若願意支持自由／開源軟體及數位權利，可以直接到各機構的官方捐款頁面：

- [Free Software Foundation（FSF）](https://my.fsf.org/donate)
- [Apache Software Foundation（ASF）](https://donate.apache.org/)
- [Open Source Initiative（OSI）](https://members.opensource.org/donate)
- [Electronic Frontier Foundation（EFF）](https://supporters.eff.org/donate/donate)

**重要：** 目前 Alpha 的 App 內 QR 圖樣不是標準可解碼 QR；在修正並通過手機掃描驗證前，請使用上述連結或 App 的「複製網址／前往官方捐款」功能。捐款在公益組織網站直接完成，與 KU-Yin 作者沒有收款關係；列名亦不代表組織背書本專案。

## 參與開發與後續計畫

歡迎回報注音鍵位、候選翻頁、跨 App 文字提交、英文補全、字級與裝置相容性。請提供 APK 版本、Android 版本、輸入欄位類型、合成文字的重現步驟、預期／實際行為；切勿附私人輸入或未清理的日誌。

下一階段優先穩定 **Emulator 健康檢查、真機驗收、可掃描捐款 QR、介面可讀性、正式簽章與 GitHub Release 管理**。之後才研究候選順位（S2）、個人學習（S3）、自適應觸控（S4）、符號目錄（S5）與可能的本機 Whisper ASR 擴充；這些並非現有版的功能承諾。

> KU-Yin 是獨立社群專案；上游和公益組織的列名表示致謝或直接支持管道，不代表其贊助、認證或背書 KU-Yin。
