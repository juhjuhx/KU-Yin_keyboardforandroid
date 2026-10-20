# KU-Yin 酷音｜技術作品集與履歷材料

> 發布資料基準：2026-10-20。**公開產品為 `v0.2.0-aie.1` Experimental Alpha；未合併的 R3 `0.2.0-rc.1` 尚未公開。** 本文描述的是專案架構與已有程式實作，不代表最新 RC 已完成實機驗收。

## 可公開的專案卡

**名稱**：KU-Yin（酷音）｜Android 大千注音開源鍵盤

**領域**：Android IME／Native integration／UX／離線工具／工程品質

**技術**：Kotlin、Jetpack Compose、Android InputMethodService、JNI/C++、libchewing、StateFlow、Room、Gradle、Android NDK/CMake、JUnit、Android Instrumentation、GitHub Actions。

**一句話介紹**：以 Android InputMethodService、Jetpack Compose 與 libchewing 製作離線大千注音鍵盤，將輸入法生命週期、中文解碼、候選快照及文字提交獨立管理，並建立測試／原生來源追溯流程。

**連結**：
- 原始碼：https://github.com/juhjuhx/KU-Yin_keyboardforandroid
- 目前可下載版本：https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1
- 開發中候選 PR：https://github.com/juhjuhx/KU-Yin_keyboardforandroid/pull/20
- 上游 libchewing：https://codeberg.org/chewing/libchewing

## 專案要解決什麼問題？

對習慣台灣大千注音的使用者而言，鍵盤不只是字元排列，而是中文組字、候選瀏覽、選字與 Android Editor 寫入的協調問題。當切換不同 App、轉換輸入欄位或快速翻頁時，候選來源和文字提交的一致性都會影響可靠性。

KU-Yin 採用：
1. **UI 與解碼分離**：Compose 顯示輸入狀態，正式中文組字由 ComposeDecoderSession 管理。
2. **Native 依賴明確化**：固定版本的 libchewing，透過自有 JNI Adapter 接入 Android。
3. **候選身分與過期保護**：CandidateRef／Snapshot generation／Provider identity，拒絕不一致的候選操作。
4. **Editor 寫入保護**：由 InputMethodService 管理 InputConnection、composing range 與 ACK，避免收到 ambiguous false 回傳時盲目重送。
5. **離線優先**：中文與英文補全在本地執行，產品 APK 不要求 INTERNET 權限。

## 架構概要

```text
Android Editor / InputConnection
          ^
          | Editor effects / ACK
          |
KuYinInputMethodService  <-- EditorPolicy
          |
   ComposeDecoderSession ---------------- EnglishWordSession
          |                                   |
    ChewingEngineSession                EnglishLexicon (TSV)
          |
    AndroidChewingEngine
          |
         JNI
          |
      libchewing
          |
     tsi.dat / word.dat / userdict.dat
```

CandidatePageSnapshot 回到 Compose 候選列與展開網格。Native startup failed 時才啟用 ZhuyinDictionarySession 備援。詳細設計見倉庫 `docs/ARCHITECTURE.md`。

## 可以如實量化的工程成果

| 面向 | 可引用的數據 | 不可延伸成的說法 |
|---|---|---|
| 已公開 Alpha | `v0.2.0-aie.1`、Debug-signed | 正式 Stable |
| 大千配置 | 固定 41 鍵，靜態映射測試 | 已在所有手機驗證 |
| R3 JVM 測試（PR #20 lineage） | App **280 PASS／10 SKIP**，Host JVM **123 PASS**；Python **205 PASS**，contracts **10 PASS** | 代表最新 App/Host instrumentation 已通過 |
| R3.1 H1 capture lineage | Python **212 PASS**、contracts **10 PASS**；H0 HEALTHY，H1 INCONCLUSIVE | H2／App／Host／實機已通過 |
| R3 候選 APK | `0.2.0-rc.1`，versionCode `20004`，Debug 測試檔 | 已在 GitHub Release 上線 |
| R3 捐款 QR 修補 | 分支上的四個 URL PNG 解碼與 Compose 渲染驗證 | 現有 main／公開 Alpha 已擁有該修補或手機相機掃描已驗證 |

若引用測試數字，**必須連同分支、日期和測試類型顯示**。R3.1 的 Build 流程與 R3 正式產品 Runtime 是不同範圍。

## 技術問題與解法／限制

**候選在翻頁後可能過期**：使用 candidate snapshot identity 與 generation 檢查。尚需在最新 RC 的真實 Android Editor 做完整驗收。

**Native Preview 可能破壞目前組字**：預覽使用獨立 scratch context 與 replay，使 Live context 不被 UI 預覽直接篡改；這也可能造成效能成本，尚無真機 latency benchmark。

**InputConnection commit ACK 可能不可靠**：失敗時保留未確認文字並停止盲目重送，不宣稱端到端 exactly-once 語義。

**Android Emulator 無法取得可靠 Editor UI XML**：R3.1 已採用有界 H0/H1 健康檢查、保存原始證據與 fail-closed，H1 根因仍未消除。不能把 CI 不穩定直接推定產品一定有錯，也不能把 JVM PASS 冒稱真機 PASS。

**App 資料安全**：Native 使用者資料與詞庫區分、備援字典另有偏好儲存；完整學習紀錄刪除、裝置轉移與正式簽章策略尚在 Roadmap。

## 履歷條目範例

**Android／Native Software Engineering**

- 開發 Android 大千注音開源鍵盤，整合 Jetpack Compose、Kotlin session 狀態管理與 JNI/libchewing 中文解碼。
- 設計候選頁面 Snapshot／Identity 防過期機制，以及 InputMethodService → InputConnection 的 Editor 效果追蹤。
- 建立 JVM／合約測試、Android Instrumentation、受控 Emulator 健康探針與 GitHub Actions 分層驗證流程。
- 維護第三方 Native 固定版本、四 ABI 打包及來源追溯材料，將核心產品開發與實驗性 RC 驗收分離。

**AI 輔助研發／Agent 協作說明**

本專案部分工程與文件由開發者協調 Codex、DSH 等 AI Agent 協作，工程成果須由原始碼、Git Commit、測試收據及審查結果支持。作品集敘述應具體呈現自己的需求定義、架構決策、驗收與風險管理；不要聲稱每一行程式碼都親手撰寫，也不要聲稱 Agent 自報的結果等同獨立驗證。

## Demo 前準備

使用合成文字（例如「我現在正在測試」）拍攝鍵盤、候選展開、英文輸入與跨 App 切換。如果沒有最新實機測試，先公開 GitHub 圖解／程式碼導覽；等驗收完成再補上真機演示。示範中應遮蓋所有私人通知、通訊錄、鍵盤個人詞庫或地址。

**不能宣稱**：Stable 已發行、官方上游背書、支援 Whisper ASR 或 OpenCC 生產轉換、所有 OEM／Android 版本驗收通過、提供端到端絕對安全的輸入。

## 下一個工程里程碑

1. 確認 R3.1 本機 ENOSPC 的十份文件安全保存、提交與追蹤。
2. 根因定位 H1 UI XML capture，於可信新 boot 完成 H0→H1→H2。
3. App／Host instrumentation 與 32-case matrix；B01 未完成案例獨立標示。
4. 實機 30-case／QR 手機掃描／TalkBack／保資料更新。
5. 長期正式簽章、裝置轉移政策、LGPL Native 重鏈結與授權資料收束。
6. 全部 Critical Gate 完成後才成立新的 GitHub RC/Stable 決策。

詳細阻擋請查看 Issue #19；本文件不是發布核准書。
