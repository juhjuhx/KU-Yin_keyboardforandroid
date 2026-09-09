# Emoji / 符號 / 剪貼簿 / 觸覺回饋 差距分析（Todo 13，LIGHT 層級）

- **任務**：Todo 13 — 唯讀差距分析（PARITY-OK / GAP）
- **範圍**：emoji / 符號（SymbolPicker）/ 剪貼簿（clipboard）/ 觸覺回饋（haptics）
- **參考樹**：`D:\Temp\opencode\upstream-ro`（HEAD `6998502`，**唯讀，勿改動**）
- **標的樹**：`D:\666\opencode\android-keyboard`
- **狀態**：本文件為**唯讀分析**，未改動任何產品程式碼、未 merge、未 push。

> 對照上游功能：
> - emoji picker + 不支援 emoji 過濾（#747，`hide_unsupported_emojis`）+ 膚色（#745，skin tones）
> - 剪貼簿純文字限定（plain-text-only）
> - 觸覺回饋 10–20ms + key-repeat 觸覺（#626，`haptic_on_repeat`）
> - 候選標籤 / 文字 / 註解顏色（#647，candidate label/text/comment colors）

---

## 0. 基準（Baseline）

執行於標的樹（工作目錄 `D:\666\opencode\android-keyboard`）：

```
$ git status --short
```

結果摘要（**報告用，未做任何變更**）：基線 git 狀態由撰寫 worker 誤判（其 `git status` 實際跑在錯誤目錄）。
更正：標的樹**是 git repo**（`.git/` 含 HEAD/index/refs/shallow/FETCH_HEAD，commits `2a523c8`/`5a1be7d`＋fetched upstream，
見 `docs/UPSTREAM.md` 與 `docs/RECONCILE-agnes.md`）；本次任務僅新增本規格檔一份，未 merge、未 push。

> 註：`git status --short` 於工作目錄執行無輸出（非 repo），符合 AGENTS.md「本 checkout 不做 git 操作」之約束。已確認僅新增 `docs/EMOJI-HAPTICS-SPEC.md` 一份檔案。

---

## 1. 標的（Target）現況盤點

### 1.1 Emoji — 完全不存在
- 標的 `app/src/main/java` 中 **任何含 `emoji|Emoji|EMOJI` 的符號（symbol）皆為 0**（`grep` 無命中）。
- 標的 `app/src/main/res` 中 **無 emoji（無 `😀`、無 `U+1Fxxx`、無 `emoji` 字串/字元）**。
- 標的 `SymbolPicker`（`input/SymbolPicker.kt`）只接受 `List<String>` 符號，**沒有 emoji 資料、沒有膚色、沒有不支援過濾**（檔內無 `emoji` 命中）。

### 1.2 符號（SymbolPicker）
- `app/src/main/java/com/example/androidkeyboard/input/SymbolPicker.kt`（全文 64 行）：
  - `setSymbols(items: List<String>, anchorX: Float)`（L24–30）——被動餵資料，**無任何來源呼叫 `setSymbols`**。
  - `onDraw`（L32–40）：單色 `0xFF212121`，選中 `0xFF38BDF8`（L37）。
  - `onTouchEvent`（L42–63）：`ACTION_DOWN` 選中、`ACTION_UP` 回呼、`ACTION_CANCEL` 關閉。
- `res/layout/activity_input.xml` L17–22：宣告 `SymbolPicker`（`visibility="gone"`），但 **`ChewingInputMethodService` 使用自建 `FrameLayout`（L64–72），並未 inflated 此 layout**；`symbolPicker` 以 `addView` 加入容器（L71），卻**從未呼叫 `setSymbols`** 餵入任何符號 → 實務上是永遠空白、不可見的空殼。

### 1.3 剪貼簿（Clipboard）
- `app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt`：
  - L22 `private lateinit var clipboard: ClipboardManager`
  - L61 `clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager`
  - **之後整個檔案不再使用 `clipboard`**（`grep` 僅兩處：宣告 + 初始化）。
  - → **沒有歷史、沒有 Room 資料庫、沒有 UI、沒有監聽 `OnPrimaryClipChangedListener`、沒有貼上行為**。剪貼簿近乎「可移除的未用欄位」。

### 1.4 觸覺回饋（Haptics）
- `app/src/main/java/com/example/androidkeyboard/input/KeyboardView.kt`：
  - `vibrate()`（L85–92）：`hapticEnabled` gate（L86）→ API ≥ Q 走 `performHapticFeedback(KEYBOARD_TAP, FLAG_IGNORE_GLOBAL_SETTING)`（L87–88）；< Q 走 `Vibrator.vibrate(10)`（L90，**固定 10ms**）。
  - `onTouchEvent` `ACTION_DOWN`（L119）呼叫 `vibrate()`。
  - **僅按下（press）有觸覺；無 key-up、無 long-press、無 key-repeat、無振幅控制、無 10–20ms 配置**。
- `IMEConfig`（`engines/core/IMEConfig.kt`）：`hapticEnabled`（L42–44，key `haptic_enabled`，預設 true）。
- `KeyboardView.setHaptic`（L58）；`ChewingInputMethodService.onCreateInputView`（L43）帶入 `config.hapticEnabled`。
- `SettingsActivity.kt`：L76–79 `haptic_enabled` 開關（「按鍵震動回饋」）。

---

## 2. 對照表（Coverage Map）

| 主題 | 上游（upstream-ro，HEAD 6998502） | 標的（android-keyboard） | 判定 |
|------|----------------------------------|--------------------------|------|
| Emoji picker 視窗 | `input/picker/PickerWindowPreset.kt:20-28` `emojiPicker()` + `PickerWindow` | **不存在**；SymbolPicker 為空殼 | **GAP** |
| 不支援 emoji 過濾（#747） | `PickerPolicy.kt:49-74` `EmojiPickerPolicy.filter()` → `EmojiModifier.isValidEmoji()`（`EmojiModifier.kt:99-105`，`RGI_Emoji` + `hasGlyph`）；偏好 `hide_unsupported_emojis`（`AppPrefs.kt:362-363`） | **不存在** | **GAP** |
| 膚色（#745） | `EmojiModifier.kt:20-27` `SkinTone` 列舉；`getPreferredTone`/`produceSkinTones`（L107-124）；`PickerPolicy.transform/popup`（L65-72） | **不存在** | **GAP** |
| 剪貼簿純文字限定 | `data/clipboard/ClipboardManager.kt`（Room `clbdb`、`ClipboardEntry.fromClipData` 只取 `item.text`）；`ClipboardEntryTransformer.kt`（IPC 純文字變換） | **無任何剪貼簿資料層** | **GAP** |
| 觸覺 10–20ms | `data/InputFeedbacks.kt:56-105`（`buttonPressVibrationMilliseconds` 可配置、振幅控制） | **固定 10ms（<Q）或系統 KEYBOARD_TAP** | **PARTIAL-GAP** |
| key-repeat 觸覺（#626） | `rept` 於 `BaseKeyboard.kt:170,189,218`、`TextEditingWindow.kt:50` 以 `hapticOnRepeat` 觸發 `InputFeedbacks.hapticFeedback`（`AppPrefs.kt:73`） | **無 key-repeat 觸覺** | **GAP** |
| 候選 標籤/文字/註解 顏色（#647） | `data/theme/Theme.kt:35-37`（`candidateLabelColor`/`candidateTextColor`/`candidateCommentColor`），套用於 `CandidateItemUi.kt:31,48-49`、`LabeledCandidateItemUi.kt:30-32` | **單一顏色** `0xFF212121`（`CandidateView.kt:47`） | **GAP** |

---

## 3. PARITY-OK（無差距／已對齊）

以「既有功能」語意對齊，標的目前**幾乎無對齊項目**；以下為「部分對齊」清單：

1. **觸覺開關（boolean toggle）**：標的有 `haptic_enabled`（`IMEConfig.kt:42-44`）+ 設定頁開關（`SettingsActivity.kt:76-79`）+ `KeyboardView.setHaptic`（`KeyboardView.kt:58`）→ 與上游「開/關」層級對齊（但上游是 3 態 `InputFeedbackMode`，非純 boolean）。判定：**PARTIAL-OK**。

---

## 4. GAP 詳表（含精確路徑／行號／修復提示）

> 修復提示僅為「未來 Todo」的可行性建議；**本次不實作**。

### GAP-A：缺少完整 emoji 系統（picker + 過濾 + 膚色）
- **現況**：標的無 emoji 資料、無 emoji picker；`SymbolPicker` 是未餵資料的空殼。
  - `input/SymbolPicker.kt:24` `setSymbols` 無呼叫端；`ChewingInputMethodService.kt:55-59,71` 建立並 `addView` 但從未 `setSymbols`。
- **上游對照**：
  - Emoji picker：`upstream-ro/app/src/main/java/org/fcitx/fcitx5/android/input/picker/PickerWindowPreset.kt:20-28`、`InputView.kt:111,218`。
  - 過濾（#747）：`input/picker/PickerPolicy.kt:49-74`、`input/popup/EmojiModifier.kt:99-105`、`data/prefs/AppPrefs.kt:362-363`。
  - 膚色（#745）：`input/popup/EmojiModifier.kt:20-27,107-124`。
- **修復提示（未來）**：
  1. 新增 emoji 資料集 + `EmojiPickerPolicy`（filter/transform/popup）。
  2. 移植 `EmojiModifier`（`isValidEmoji` 用 `RGI_Emoji` 或 `TextPaint.hasGlyph`，API 門檻如 `Build.VERSION_CODES.P`）。
  3. 在 `ChewingInputMethodService` 中真接線 `SymbolPicker.setSymbols`（餵入過濾後 emoji/符號）。

### GAP-B：缺少不支援 emoji 過濾（#747）與膚色（#745）
- **現況**：標的無任何過濾邏輯、無膚色。
- **上游**：`EmojiModifier.kt`（全文，見 §2）、`PickerPolicy.kt:61-72`。
- **修復提示**：於 commit 前以 `isValidEmoji` 過濾；膚色經 `produceSkinTones` 產生長壓選單。**注意**：需確保**單一 code point 的 emoji 不會在「字串層次」被誤判為多重碼位而崩潰**（跨語言一致性 guard，見 §6）。

### GAP-C：剪貼簿純文字資料層完全缺失
- **現況**：`ChewingInputMethodService.kt:22,61` 僅宣告+初始化 `ClipboardManager`，從未使用。
- **上游**：`data/clipboard/ClipboardManager.kt`（Room `clbdb`、`onPrimaryClipChanged` 監聽、純文字 `ClipboardEntry.fromClipData` 只取 `item.text`）、`data/clipboard/db/ClipboardEntry.kt`（`MIMETYPE_TEXT_PLAIN` 預設）、`ClipboardEntryTransformer.kt`（純文字 IPC 變換）。
- **修復提示**：若要剪貼簿歷史，需自建 Room DB + `OnPrimaryClipChangedListener`；**先確認產品是否要剪貼簿功能**，否則可移除未用欄位（L22/L61）以免誤導。

### GAP-D：觸覺回饋過於簡化（無配置、無 key-up/long-press/repeat、無振幅）
- **現況**：`KeyboardView.kt:85-92` 固定行為（<Q 10ms；≥Q 系統 `KEYBOARD_TAP`），僅 `ACTION_DOWN`（L119）。
- **上游**：`data/InputFeedbacks.kt:56-105`（`buttonPressVibrationMilliseconds`/`buttonLongPressVibrationMilliseconds`/振幅；`hapticOnKeyUp`）、`AppPrefs.kt:62-93`。
- **修復提示**：
  - 將 10ms 常數抽為可配置偏好（10–20ms 範圍）。
  - 增加 key-up、long-press、key-repeat 觸發點與對應偏好。

### GAP-E：缺少 key-repeat 觸覺（#626）
- **現況**：無。
- **上游**：`data/prefs/AppPrefs.kt:73` `hapticOnRepeat`；`input/keyboard/BaseKeyboard.kt:170,189,218`；`input/editing/TextEditingWindow.kt:50`。
- **修復提示**：在重複觸發 key 事件（如刪除鍵長按連發）時呼叫觸覺。

### GAP-F：候選 標籤/文字/註解 顏色單一化（#647）
- **現況**：`ui/CandidateView.kt:47` 固定 `0xFF212121`；無 label/comment 分色。
- **上游**：`data/theme/Theme.kt:35-37` 三色；`CandidateItemUi.kt:31,48-49`、`LabeledCandidateItemUi.kt:30-32`。
- **修復提示**：`CandidateView` 套用三色（text/label/comment）+ 主題化，擴充 `IMEConfig` 供色值。

---

## 5. 建議優先級（供後續 Todo 參考，非本次範圍）

| 優先 | 項目 | 理由 |
|------|------|------|
| P1 | GAP-A 接線 SymbolPicker（餵資料）| 讓既有 UI 不再空轉 |
| P1 | GAP-D 觸覺常數抽離 + key-up/long-press | 低風險、高 UX 增益 |
| P2 | GAP-B 過濾 + 膚色 | 依賴 GAP-A；移植 EmojiModifier |
| P2 | GAP-F 候選三色 | 小規模、立即可視 |
| P3 | GAP-E key-repeat 觸覺 | 依附 GAP-D |
| P3 | GAP-C 剪貼簿 | 屬大型功能，需產品決策（或移除未用欄位）|

---

## 6. 單一 code point emoji 回歸守衛（Guileless#11）

> 一致性守衛：**不得以「整串判斷」取代「逐碼位判斷」**，以免單一 code point 的 emoji 在多碼位序列處理時被誤砍／誤判為不支援。
- 上游 `EmojiModifier.kt:60-70` 以 `emoji.codePoints().toArray()` **逐碼位**處理 modifiable；`59-70,72-88` 各自處理；**不應把 `isValidEmoji(string)` 對整串套用到「可能含 ZWJ / VS16」的多碼位序列而誤判**。
- 標的目前無 emoji 碼，故暫無此風險；**當移植 GAP-B 時必須保留上游的逐碼位邏輯，不得簡化為 substring 包含判斷**。
- 建議在移植前先寫單元測試：`"👍🏽"`（U+1F44D U+1F3FD）等 ZWJ/膚色序列不得被過濾為「不支援」。

---

## 7. Manual QA（驗證）

以下 grep / 讀取為本文件佐證（均於標的與參考樹實際執行）：

```
# 標的：emoji 完全不存在
grep -r "emoji\|Emoji\|EMOJI" app/src/main/java   # 0 命中
grep -r "emoji\|😀\|U+1F"        app/src/main/res   # 0 命中
grep -r "emoji" app/src/main/java/.../SymbolPicker.kt  # 0 命中

# 標的：剪貼簿僅宣告+初始化，未使用
grep -r "clipboard" app/src/main/java   # 僅 ChewingInputMethodService.kt:22,61

# 標的：觸覺
grep -r "vibrate\|performHapticFeedback" app/src/main/java
  # KeyboardView.kt:85-92,119 ; KeyboardView.kt:58 setHaptic

# 上游：觸覺 / repeat / 候選色
grep -r "hapticOnRepeat\|buttonPressVibrationMilliseconds" upstream-ro/app/src/main/java/org/fcitx/fcitx5/android
  # AppPrefs.kt:73 ; BaseKeyboard.kt:170,189,218 ; TextEditingWindow.kt:50

grep -r "candidateTextColor\|candidateLabelColor\|candidateCommentColor" upstream-ro/app/src/main/java/org/fcitx/fcitx5/android
  # Theme.kt:35-37 etc.
```

---

## 8. 結論

標的目前於 emoji / 剪貼簿 / 觸覺 / 候選配色**大多為 GAP**：
- **PARITY-OK（部分）**：觸覺開關 boolean（`haptic_enabled`）。
- **GAP**：完整 emoji picker（GAP-A）、過濾 + 膚色（GAP-B）、剪貼簿純文字資料層（GAP-C）、觸覺配置化 + key-up/long-press/repeat（GAP-D/E）、候選三色（GAP-F）。

**本文件為唯讀差距分析；未寫任何產品程式碼、未 merge、未 push、未改動參考樹。**
