# C4 — Compose production switch to libchewing（計劃）

- Branch: `c4-compose-libchewing-switch`（基於 C3.5 尖端 `3f02976`，獨立 worktree）
- 目標：生產 UI 的中文解碼從 `ZhuyinDictionarySession` 切到 `ChewingEngineSession`，
  行為由測試＋實機 Gate 驗收。`ZhuyinDictionary` 本輪不刪（C9）。
- 前提（C3.5 已合入本分支）：policy 接線、lifecycle reconciliation、punctuation、
  code-point backspace、tone-aware ranking、smoke 正名、production 合約。

## D0. dispatch Result 升級（先行，已證必需）
- 證據：`AndroidChewingEngine.backspaceUpdate` 經 `snapshot(consumed=…)` 區分
  「decoder 消化」與「空緩衝無動作」；現 `dispatch(): String?` 把該位元丟掉，
  service 無法決定是否刪編輯器字。
- 改為 `data class DispatchResult(val commitText: String?, val consumed: Boolean)`；
  同步改 `ComposeDecoderSession` 介面、C1/C2/C3 測試與兩適配器。
- Backspace 語義：`consumed=true`→僅套用 update；`false`＋空 preedit→service 刪 surrounding。

## D1. CASE-4 RED 測試（fake 編碼延續語義）
- Fake `ChewingEngine`：`selectCandidateUpdate` 回傳 committedText 非空＋剩餘 preedit 非空；
  斷言：其後 `TapZhuyinKey` 直接進 `handleKeyUpdate`，中間 `reset()` 呼叫數為 0，
  終態 preedit 非空且 candidates 為新頁。
- 另測：空 select（index 越界）回傳 consumed=false 且狀態不變。

## D2. UI 接線（`KuYinKeyboardUi` 改吃 `ComposeDecoderSession`）
- `engine: KuYinEngine` 參數改為 session 介面；`onPasteClipboard` 等直通回調保留簽名；
  `switchMode`／`onEnglishKey` 等非解碼器行為保留 KuYinEngine（模式仍是 UI 層狀態，
  per STATE OWNERSHIP 決議，decoder 只擁有 composition／candidates／paging）。
- 風險：調用點含 MainActivity 預覽與既有測試；一次一處改，編譯即驗。

## D3. Service 接線（`KuYinInputMethodService`）
- `onCreate`：`LibChewingDataInstaller.ensureInstalled`＋`AndroidChewingEngine.init(DACHEN)`＋
  `setPersonalizedLearningEnabled(session policy)`，沿用 donor `onCreate` 順序；
  `onDestroy`：`close()`。
- `dispatch` 結果執行器：commitText 非空→`commitText`；consumed=false 的 Backspace→
  `performEditorBackspace()` 既有路徑；其餘沿用 C3.5 policy／lifecycle 接線。
- `ZhuyinDictionarySession` 降為 fallback（預設關），保留作比較 harness。

## D4. 驗證門
- JVM：全量 suite＋7 合約＋`assembleDebug`（native 照編）。
- 實機 Gate CASE 1–12（CASE 4／6 hard blocker 不可跳過）；QR 掃碼順手驗。
- 回滾：一行切回 `ZhuyinDictionarySession`（fallback 即開關）。

## 非目標
刪 `ZhuyinDictionary`（C9）、View shell 清理（C10）、emoji 資料集、簽章／AAB／F-Droid、
英文 stagger（visual baseline）、PR #4 處置。
