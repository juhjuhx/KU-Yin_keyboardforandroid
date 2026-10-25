![KU-Yin 酷音｜台灣注音鍵盤開源專案原創 Banner（品牌插畫、非操作截圖）](docs/community/media/kuyin-readme-hero.svg)

# KU-Yin 酷音｜台灣大千注音 Android 鍵盤

> **目前版本：實驗性 Alpha `0.2.0-aie.1`（Debug APK）**。已公開下載，但完整實機驗收、正式簽章與 Stable Release 尚未完成。請先閱讀下方「目前狀態與限制」。

**KU-Yin** 是以**台灣大千注音**為核心的離線 Android 開源輸入法。鍵盤介面使用 **Kotlin／Jetpack Compose**，中文解碼採 **libchewing（酷音）**，英文補全使用本機詞表。目標是讓注音組字、選字與候選翻頁更易用，同時保留清楚的資料權責、可維護的原生依賴及社群開發流程。

[📦 下載 Alpha 與原始碼](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1) · [🧭 架構](docs/ARCHITECTURE.md) · [📋 開發狀態](docs/CURRENT_STATE.md) · [📣 社群分享／測試邀請素材](docs/community/README.md) · [🐛 問題回報](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues)

## 三十秒認識 KU-Yin

**對使用者：** 一套以台灣大千 41 鍵為中心的離線 Android 鍵盤，提供注音組字、可瀏覽的候選列與基本英文補全。現在公開的是可下載、但仍在實機相容性驗證中的 Experimental Alpha。

**對開發者：** 它是一個處理真實 Android IME lifecycle、JNI/libchewing、候選身分、防重複 Editor commit，以及本機資料界線的開源工程專案；不是只有 UI Preview 的鍵盤 Demo。

### 目前狀態（TUI 速覽）

~~~text
┌──────────────── KU-Yin / RELEASE BOARD ────────────────┐
│ CHANNEL    Experimental Alpha   v0.2.0-aie.1 / 20003  │
│ CHINESE    41-key Zhuyin        libchewing / JNI        │
│ UI         Compose              Candidate row / grid   │
│ ENGLISH    Offline prefix       ~10,000 words          │
│ MAIN CI    Build SUCCESS        App 5 PASS             │
│ HOST       15 PASS              9 FAIL / 8 SKIP        │
│ H0 / H1    INCONCLUSIVE / NOT_EXECUTED (latest health) │
│ H2         NOT_EXECUTED         Device NOT_TESTED      │
│ NEW RC     NO-GO                Stable NO-GO           │
└────────────────────────────────────────────────────────┘
~~~

上方是**來源對應的測試摘要**，不是宣傳用的通過率。Host 九個歷史 FAIL、八個 SKIP 仍待 Runtime 閉合；R3.7 observer 的 JVM 修正與離線 readiness 規格另有證據，不能當作裝置通過。詳見 [發布 Gate #19](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/19)。

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

以下保留各輪**歷史來源**；最新工程狀態以 [CURRENT_STATE](docs/CURRENT_STATE.md) 與 [維護交接](docs/maintenance/FINAL_AGENT_HANDOFF.md) 為準。公開 Alpha 與後續測試分支必須分別看待：

- **公開 Alpha**：原 API 33 App 5 PASS；Host **15 PASS／9 FAIL／8 SKIP**（發布時基線）。
- **R2 測試分支**：曾在 API 33 取得 App **5 PASS**、Host **24 PASS／9 SKIP**；24 項含一項測試基礎設施檢查，實際產品案例為 **23 PASS／9 INCONCLUSIVE**。不是 `main` 新版本的完整驗收。
- **R2.1/R2.2 後續調查**：仍有 IME visibility/binding 阻擋。R2.2 A/B 都是 App **4 PASS／1 FAIL**，並在安裝 KU-Yin 前偵測到 **System UI ANR**；歷史 remote/session 根因仍 **UNRESOLVED**。
- **R2.3.1 健康檢查分支**：已修復 frozen-display 解析缺陷；[health-only CI #39080190653](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39080190653) 為 H0 **HEALTHY**、Python **192 PASS**、contracts **10 PASS**。**未執行 H1／H2、App／Host 或新 APK 建置**；舊 H0 不可跨 Emulator boot 重用。
- **R3 候選／R3.1 隔離修正**：未發布的 `0.2.0-rc.1` 是 [Draft PR #20](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/pull/20) 中的 Debug 測試建置；QR 改為標準圖片並通過離線解碼，但手機掃碼尚未測。[R3.1 CI #39426302308](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39426302308) 的 H0 HEALTHY、H1 INCONCLUSIVE（缺 XML／逾時）；H2、App／Host 與實機未執行，`RC_READY=false`。
- **實機與正式版**：AIE-S1 完整真機候選瀏覽、不同 OEM、TalkBack、學習清除、正式 release key、商店與穩定版均 **OPEN／NOT_READY**。

請勿把 CI 編譯成功、模擬器局部成功或使用者基本打字回報當作整套功能通過。**實驗性 runtime-smoke 仍有失敗紀錄**；請查看每個 job，而非只讀整體 Workflow 狀態。正式的 [Release readiness](docs/release/RELEASE_READINESS.md)、[RC 發布驗收追蹤 #19](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/19) 與 [AIE Roadmap](docs/roadmap/AIE_V1_ROADMAP.md) 仍是工作基礎。

### R3.2～R3.3 歷史工程接續（非當前 Gate）

- **[圖片修復 PR #24](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/pull/24)：** 10 張 Launcher WebP 已由專案素材重新產生並通過限定範圍的完整性核驗；Roborazzi PNG 為測試 Render，不是實機截圖。公開舊 Alpha 不包含這次修復；main 整合狀態與 exact-source CI 以 PR24 為準。
- **[圖片 CI #39515675883](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39515675883)：** Blocking Build 成功，App Instrumentation 5 PASS，Host Instrumentation **15 PASS／9 FAIL／8 SKIP**。Workflow 總結 Success 是因 Experimental Runtime 允許失敗，不能當 Runtime PASS。
- **[H1 CI #39514852906](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39514852906)：** H0 有界 HEALTHY；H1 收到 XML 但 Editor focused=false，且擷取程序未自然結束，**INCONCLUSIVE**。H2 未執行。
- **R3.3 Workspace：** 當時本地已完成中央索引，原 dirty 工區保護；新增診斷提交 **9ac52b5699aad874a3daa6d6ee950dabc4ce22e9** 其後已完成限定診斷複審及推送；H1 並未因此取得 Runtime PASS。

**圖片聲明：** 頁首 SVG 是原創品牌示意，不是 Android 截圖；真正的鍵盤操作照片需要版本、裝置、截圖來源及匿名化審查，取得前不以 Robolectric／Roborazzi 畫面冒充。

## 實際畫面與最新證據

目前沒有完成來源／隱私核驗的鍵盤操作截圖，因此這裡不以品牌圖或 Preview 代替。截圖與圖卡待辦見 [media 清單](docs/community/media/README.md)。歷史 main [CI39442957936](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39442957936) XML 為 App5 PASS、Host15 PASS／9 FAIL／8 SKIP；不能套用成未合併 RC 的 Runtime 通過。[本輪驗證](docs/release/PUBLICATION_VALIDATION.md) 分開記錄來源。

## R3.8 工程維護模式

保留既有鍵盤、Banner、原創Icon、開源致謝與基金會支持入口。工程變更只處理測試observer、來源/授權完整性、CI權限與文件；沒有新增輸入法功能，也沒有新Release。

- [安全審查](docs/maintenance/FINAL_SECURITY_AUDIT.md)／[授權報告](docs/maintenance/FINAL_LICENSE_REPORT.md)／[Known Issues與維護清單](docs/maintenance/MAINTENANCE_BACKLOG.md)。
- 手動Alpha workflow僅產生**TEST_ONLY CI artifact**，不建立GitHub Release；CI Debug憑證不能保證更新公開Alpha。RC／Stable需要另外驗證及批准。
- 根LICENSE已校正為標準LGPL2.1文字；舊Alpha封包保留原bytes，已知授權文件異文另列更正事項，不宣稱全套散布義務已完成。

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

## 為什麼採用這套架構？

| 決策 | 理由 | 必須承擔的成本 |
|---|---|---|
| libchewing 而非從零開發注音引擎 | 重用成熟注音候選／組字能力 | 固定 Native 版本、JNI API 與來源授權維護 |
| Kotlin + Compose | UI／候選狀態與外觀可清楚分層 | IME Window／焦點／無障礙需要實際 Android 驗證 |
| 中文僅一個 Session owner | 避免多處狀態衝突或雙重提交 | 需要維持嚴格的 command／snapshot 邊界 |
| Candidate generation + identity | 翻頁或組字後阻止過期候選被選中 | 候選 projection 與測試較複雜 |
| Scratch Native Context | 只在隔離 context 做候選預覽，不修改 live composition | 可能帶來 Native replay／allocation 成本 |
| Service 控管 Editor effects | UI 不直接寫字到別的 App，方便做 Enter／ACK 策略 | Android 回傳的 Boolean ACK 仍不等於跨 IPC exactly-once |
| Offline-first | 不依賴 API 金鑰、遠端網路或雲端模型 | 本機字典、學習清除、備份／轉移責任仍需審查 |

### 專案用到哪些語言與資料格式？

| 範疇 | 技術／格式 |
|---|---|
| Android Service、輸入邏輯、UI | Kotlin、Android SDK、Jetpack Compose、Material 3 |
| 中文 Native 解碼 | libchewing C API、JNI、C++17、NDK／CMake |
| 編譯建置 | Gradle Wrapper 9.3.1、AGP 9.1.1、JDK 21、Kotlin 2.2.10 |
| 正式 App 的最低／目標 API | Android minSdk 24、targetSdk 36 |
| 資料／個人化 | Native userdict.dat、SharedPreferences、Room SQLite |
| 字庫與英文來源 | pinned tsi.dat／word.dat／symbols.dat、離線 TSV |
| 驗證 | Python contracts、JUnit、Robolectric／Roborazzi、UiAutomator、Android Instrumentation、JUnit XML |
| 發行 | APK、SHA256SUMS、source-and-relink archive、GitHub Pre-release |

Android 版本及 Native pin 可能隨正式 PR 更新，以 [DEVELOPMENT](docs/DEVELOPMENT.md)、[UPSTREAM](docs/UPSTREAM.md) 與最終發行的 Manifest 為準。

### 上游 → KU-Yin → 下游

~~~text
 upstream / source of truth                    KU-Yin                         downstream
 ──────────────────────────────                ─────────────────────────      ──────────────────────
 libchewing + pinned native ABI archives ─┐
 libchewing data + AOSP English TSV ───────┤→ JNI + Session + Compose IME ─┬→ Android APK / Tester
 Android InputMethodService / Editor ──────┘                               ├→ Test receipts / CI
                                                                          ├→ OSS source / relink
                                                                          └→ Issues / downstream users
~~~

- **實際用到的上游：** [libchewing](https://codeberg.org/chewing/libchewing)、[fcitx5-android/prebuilt](https://github.com/fcitx5-android/prebuilt)、[libchewing-data](https://github.com/chewing/libchewing-data)、AOSP LatinIME、AndroidX／Compose。Native 預編譯來源 pin 是 **3587ba3355711f0aca50136e787719f6562676b8**，對應 libchewing source **a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c**。
- **下游交付：** Android 使用者測試 APK、可追溯 CI 收據、授權與重鏈結檔案、GitHub Issue／PR 交流。尚未宣稱存在正式插件 SDK。
- **只用來研究：** Fcitx5 Android Framework、FlorisBoard、HeliBoard、OpenCC、Rime、Whisper。列為參考不代表已移植程式碼，更不代表上游背書本專案。
- **更新方式：** upstream-watch 只比對／通知上游更新，不能直接替換 libchewing pin 或自動合併主線。

## 我們從開發中學到了什麼？

KU-Yin 的主要工程難題並不只是「把按鍵畫出來」，而是讓組字與候選在真實 Android 輸入欄位中保持一致：

1. **先固定大千鍵位與 Decoder Session**：UI 的字元對應不能隨候選排序或畫面重組而改變；Native 開啟失敗才使用 fallback。
2. **候選不是單純列表**：連續輸入與翻頁可能使舊選字位置失效，因此採用 provider／generation／selection identity 追蹤。
3. **提交文字要區分多個確認邊界**：Android Editor 回報失敗不代表目標一定沒寫入；不可盲重送造成重複文字。Native ACK 不是 Editor ACK。
4. **畫面能顯示 ≠ 輸入法可用**：MainActivity 試打、Compose 截圖與 App 測試不能代替 Host Editor 的焦點、Binding、InputConnection 實驗。
5. **GitHub Release 不只是放 APK**：簽章、版本／更新相容、Native 來源、法律文件、用戶資料與可重現測試證據必須一致。

因此專案目前保留 **Host 9 FAIL／8 SKIP、R3.2 H1 INCONCLUSIVE** 的真實狀態，並把 Release Gate 獨立管理，而非靠忽略實驗性 Runtime 失敗換取 CI 全綠。

*關於最初的個人創作契機：這裡僅整理可由原始碼與工程記錄支持的動機與歷程；作者的生活經驗、使用偏好與故事可再由作者補上。*

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

**重要：** 已公開 Alpha 的 App 內 QR 圖樣不是標準可解碼 QR；未發布的 R3 隔離分支雖已完成標準 QR 靜態解碼，仍未經手機實掃，也尚未合併／發布。請使用上述官方連結或 Alpha 的「複製網址／前往官方捐款」功能。捐款在公益組織網站直接完成，與 KU-Yin 作者沒有收款關係；列名亦不代表組織背書本專案。

## 參與開發與後續計畫

歡迎回報注音鍵位、候選翻頁、跨 App 文字提交、英文補全、字級與裝置相容性。請提供 APK 版本、Android 版本、輸入欄位類型、合成文字的重現步驟、預期／實際行為；切勿附私人輸入或未清理的日誌。

下一階段優先穩定 **Emulator H1/H2、真機驗收、QR 手機實掃、介面可讀性、正式簽章與 GitHub Release 管理**。之後才研究候選順位（S2）、個人學習（S3）、自適應觸控（S4）、符號目錄（S5）與可能的本機 Whisper ASR 擴充；這些並非現有版的功能承諾。

> KU-Yin 是獨立社群專案；上游和公益組織的列名表示致謝或直接支持管道，不代表其贊助、認證或背書 KU-Yin。
