# CANDIDATE-SPEC — Todo 12 候選欄落差分析（read-only 規格）

> 作用：`.omo/plans/android-keyboard.md` Todo 12（:105-109）的 read-only 前置落差分析，
> 供實作設計與 wave 拆分使用。本文件只列證據與修補方向，不改動任何產品代碼。
> 上游參照：`D:\Temp\opencode\upstream-ro` @ `6998502528cd26efb6079556da92003638e4179c`（read-only）。
> 產出日期：2026-09-09。語言：zh-TW。路徑旁標 `target:`＝本計畫、`up:`＝上游。

---

## 0. 結論摘要

| # | 領域 | 判定 | 一句話 |
|---|------|------|--------|
| 1 | 候選分頁 | **GAP（部分可行）** | target 有 libchewing 原生每頁 5 詞分頁；無上游 0.0.6 的 Paging 3 按需載入與縱向/網格捲動 |
| 2 | 展開／收起 | **GAP** | target 無 expand 按鈕、無展開視窗、無 cutoff 判定；上游有 `ExpandButtonStateMachine`＋獨窗 |
| 3 | 按壓 popup 預覽 | **GAP** | target `onTouchEvent` 按下即送鍵、抬手即重置；無 PreviewAction／浮層 |
| 4 | 長按 350ms 符號表 | **GAP** | target 無 `LongPress` / `Popup.Keyboard` / swipe 選符機制；上游 `KeyDef.Popup` 全套缺失 |
| 5 | 物理鍵盤選字（含組合鍵） | **GAP** | target service 無 `onKeyDown`/`onKeyUp`；硬體鍵盤完全無路由 |
| 6 | R2b cand_open 序列 | **PARITY-OK** | `buildCandidates():144` 已對每次刷新 open；service 每鍵後 `updateCandidates()`，序列正確 |

對照 Todo 12 Accept（plan :107「候選分頁、展開/收起、物理鍵盤數字選字、按壓預覽、長按 350ms 符號表，全具」）：
除分頁有最小實作外，其餘四項全部未落地；本文件逐項給出上游證據與建議落點。

---

## 1. 候選分頁（Paging）

**上游（0.0.6）**：Paging 3 按需載入。
- `up: app/src/main/java/org/fcitx/fcitx5/android/input/candidates/expanded/CandidatesPagingSource.kt`
  - :13 `CandidatesPagingSource(fcitx, total, offset) : PagingSource<Int, CandidateWord>`
  - :18-23 `load()`：`startIndex = params.key ?: offset`；`pageSize = params.loadSize`；
    `fcitx.runOnReady { getCandidates(startIndex, pageSize) }`——捲動才向 fcitx daemon 拉下一窗。
  - :24-29 手算 `prevKey/nextKey`；:30 `LoadResult.Page`。
  - :34 `getRefreshKey() = null`（每次都從 offset 重載）。
- `up: .../candidates/expanded/PagingCandidateViewAdapter.kt`（46 行）：`PagingDataAdapter` 接收 `PagingData<CandidateWord>`。
- `up: .../candidates/expanded/GridPagingCandidateViewAdapter.kt`（47 行）：`LruCache` 記量測寬，展開網格複用。
- `up: .../candidates/horizontal/HorizontalCandidateComponent.kt`
  - :169-197 `onCandidateUpdate(CandidateListEvent.Data)`：依 `fillStyle`（Never/Auto/AlwaysFillWidth）算
    `layoutMinWidth/layoutFlexGrow` 與 `secondLayoutPassNeeded`（:179-190），:192 `adapter.updateCandidates(candidates, total)`。

**target**：
- `target: app/src/main/java/com/example/androidkeyboard/ui/CandidateView.kt`
  - :22 `pageSize = 5`（每頁固定 5 詞）；:36-37 `getCurrentPage()/getPageCount()`；
  - :30-34 `setCandidates()` 重置 `currentPage=0`；:39-48 `onDraw()` 只繪 `subList(currentPage*pageSize, …)`。
  - 分頁 = libchewing 原生頁（`chewing_cand_choice_per_page`）+ 手動 `currentPage` 切片，**無按需載入、無 RecyclerView、無捲動**。
- `target: .../engines/android/AndroidChewingEngine.kt`
  - :147 `pageSize = chewing_cand_choice_per_page(nativeCtx)`；:148 `start = _candPage * pageSize`；
  - :154-163 `nextPage()`、:165-172 `prevPage()` 用 `chewing_cand_total_page` 控制 `_candPage` 並重建。
  - 資料面已具備 offset 切片能力（:148-149），只是 UI 無承接。

**GAP 判定**：核心缺口在 UI 層——target 無「捲動才載」模型。上游引擎可回任意 `getCandidates(start,limit)` 窗，
target 的 libchewing 原生模型一次只有一頁可枚舉；若要按需載入需沿用原生頁（每頁即一窗），不必強制上 Paging 3。

**修補方向**：
1. 保留 libchewing 每頁模型，把 `CandidateView` 改為 RecyclerView（或 `ListView`）＋ `FlexboxLayoutManager`
   （對齊 `HorizontalCandidateComponent`），`nextPage/prevPage` 改為滑動觸發。
2. 若要精準對齊上游 0.0.6：JNI 加 `chewing_cand_enumerate` 型視窗讀取（offset+limit），配 PagingSource 對照層。

---

## 2. 展開／收起（Expand / Collapse）

**上游**：
- `up: .../input/bar/ExpandButtonStateMachine.kt`：展開鍵狀態機（事件 `ExpandedCandidatesUpdated`、
  布林鍵 `ExpandedCandidatesEmpty` 等，見 HorizontalCandidateComponent :21-22 import）。
- `up: .../candidates/horizontal/HorizontalCandidateComponent.kt` :79-85 `refreshExpanded(childCount)` →
  `bar.expandButtonStateMachine.push(ExpandedCandidatesUpdated, ExpandedCandidatesEmpty to (adapter.total == childCount))`：
  由「目前顯示數 vs 總數」驅動展開能否動作（空候選禁用）。
- `up: .../candidates/expanded/window/BaseExpandedCandidateWindow.kt`（215 行）
  - :49 abstract class；:75 `onCreateCandidateLayout(): ExpandedCandidateLayout`；
  - :156-157 `onAttached()` push `ExpandedCandidatesAttached`；
  - :164-166 收集 `horizontalCandidate.expandedCandidateOffset` → `windowManager.attachWindow(KeyboardWindow)`；
  - :152-154 `prevPage()/nextPage()` abstract；:195-196 `onDetached()` push 關閉。
- 展開＝**獨立視窗**（`attachWindow(KeyboardWindow)`）＋ 網格佈局（Grid adapter 之 LruCache 量測）。

**target**：
- `CandidateView.kt` 全檔 84 行：無按鈕、無展開、無二態、無寬度判定；service 亦無 expand 事件。
- 佈局 `FrameLayout`（建立 KeyboardView＋CandidateView＋SymbolPicker 三層，見 service :64-72）。

**GAP 判定**：整套機制（狀態機＋獨立視窗＋網格）缺失。

**修補方向**（對齊上游但不照搬專案管理）：
1. target 尺度較小：先做**同視窗展開**——捲動露出第 6 詞時把 `pageSize` 增為網格（`expandedCandidateGridSpanCount` 概念），
   不開第二 Window（避 `InputMethodWindowManager` 依賴）。
2. 若日後要對齊上游：引 `ExpandButtonStateMachine`（欄位化事件），CandidatesView 同時綁定狀態機與建議列數。

---

## 3. 按壓 Popup 預覽（Press Preview）

**上游**：
- `up: .../input/keyboard/BaseKeyboard.kt`
  - :54 `popupOnKeyPress`（AppPrefs，可關）；:320-338 `Popup.Preview`：
    `GestureType.Down → PopupAction.PreviewAction(view.id, content, bounds)`；`Up → DismissAction`。
  - :295-319 `Popup.AltPreview`：加 `Move → PreviewUpdateAction`（觸發與否切 content/alternative）。
  - :124 `KeyDef.Appearance.AltText` 鍵使用 AltPreview。
- `up: .../input/keyboard/KeyDef.kt` :115-116 `sealed class Popup { open class Preview(val content: String) }`。
- `up: .../input/keyboard/CustomGestureView.kt`：`GestureType.Down` 事件源；`KeyView` 453 行含背景/高亮遮罩/ripple。
- `up: .../input/popup/PopupComponent.kt`：`UniqueComponent`；:41 `showingEntryUi HashMap<Int, PopupEntryUi>`、:42 `dismissJobs`；
  `PreviewAction/PreviewUpdateAction/DismissAction` 經 `PopupEntryUi` 渲染浮層並以 job 排定消失。

**target**：
- `target: .../input/KeyboardView.kt` :113-129 `onTouchEvent`：`ACTION_DOWN` → 命中鍵、`vibrate()`、`onKeyPress?.invoke(...)`；
  `ACTION_UP/CANCEL` → `pressedKeyIndex = -1` 重繪。**無任何按下浮層**。:131-144 只有 proximity 鄰鍵走查誤觸修正。

**GAP 判定**：按壓預覽完全缺失。

**修補方向**：
1. `KeyboardView` 增加「按下暫存鍵帽字樣」繪製（膨脹字／放大鏡效果）：`pressedKeyIndex >= 0` 時在 `onDraw`
   於該鍵 rect 上方畫放大的文字卡片，手指抬起清除——UI 面最小改動。
2. 完整對齊：`KeyView.setHoveredDrawable` 概念＋按下/抬起事件對 `PopupAction.PreviewAction/DismissAction`。
   勿在 `onDraw` 用 `postDelayed` 做過長延遲（見 §6 主執行緒紀律）。

---

## 4. 長按 350ms 符號表（Long-press Popup）

**上游**：
- `up: .../input/keyboard/KeyDef.kt` :115-130
  - `Preview`（按壓）；`AltPreview(content, alternative)`（按壓＋上滑切 alt）；
  - :120-125 `sealed class Keyboard { Preset(label, transformPunctuation) / Explicit(items) }`——**長按符號表**；
  - :127-129 `Menu(items)`——長按選單。
- `up: .../input/keyboard/KeyDefPreset.kt`：符號表預設——如 :172 `,`→`Popup.Preview(",")`＋`Popup.Keyboard.Preset(",")`、
  :25/:36-38 數字與逗號族、:46/:58-60 字母族 AltPreview＋Keyboard.Preset、:235 Menu。
- `up: .../input/keyboard/BaseKeyboard.kt`
  - :208-213 `Behavior.LongPress → setOnLongClickListener { onAction(act) }`（長按執行 `KeyAction`）；
  - :248-340 `def.popup` 佈線：:273-279 `Popup.Keyboard` → `onPopupAction(ShowKeyboardAction(view.id, it, bounds))`，
    :251-257 `Popup.Menu` → `ShowMenuAction`；:280-294 長按後 swipe 移動焦點、:288-290 `GestureType.Up → onPopupTrigger(view.id)`；
  - :477-485 `onPopupTrigger` 把 popup 內累積的 pending `KeyAction` 以 `Source.Popup` 送出後 `DismissAction`。
- `up: .../input/keyboard/TextKeyboard.kt`
  - :140 `Source.Popup`；:177 `transformPopupPreview`（標點映射）；:183-202 `onPopupAction`：
    PreviewAction 做標點轉換、ShowKeyboardAction 依 `Preset → punctuation`、`Explicit → 原 items`。
- `up: .../input/popup/PopupComponent.kt`＋`PopupEntryUi/PopupKeyboardUi/PopupContainerUi`：
  `ShowKeyboardAction` 渲染迷你鍵盤，swipe 收集 `KeyAction`，`TriggerAction` 送出。
- `up: .../input/keyboard/CustomGestureView.kt` :56-58 `longPressTriggered/longPressEnabled/longPressJob` 判長按。

**target**：`KeyboardView.kt` 無 `OnLongClickListener`、無 popup、無 swipe；`KeyboardLayout.kt` 無 popup 資料結構（全為靜態字面鍵）。
Grep 確證 target 全樹無 `long`/`popup`/`OnLongClick` 命中。

**GAP 判定**：長按符號表整套缺失（資料、時序、手勢、UI 四層）。

**修補方向**：
1. `KeyboardLayout` 增 `popups: Map<String, List<String>>`（字元→符號清單），預設同 `KeyDefPreset` 常用符號態。
2. `KeyboardView` 佈 `OnLongClickListener`；長按觸發後吃下本次 touch 序列（`customGestureView` 的
   `longPressTriggered` 概念），swipe 選符、抬手 commit、離手取消。
3. **AI 紀律（plan :108 failure 條）**：長按一律走 UI 主執行緒（`view.post { … }`），
   符號等待期間不得卡住 libchewing worker——Guileless 長按刪除教訓：長按打斷引擎即打回。

---

## 5. 物理鍵盤候選（數字選字／組合鍵）

**上游（0.1.0/0.1.3）**：
- `up: .../input/InputDeviceManager.kt`
  - :21-25 `setupInputViewEvents`；:27-35 `setupCandidatesViewEvents`：`cv.handleEvents = !isVirtualKeyboard`、
    虛擬鍵盤時**隱藏 CandidatesView**（物理鍵盤才顯示候選列）；:37-40 `setupViewEvents`；:42+ `isVirtualKeyboard`。
- `up: .../input/FcitxInputMethodService.kt`
  - :242-244 `FcitxKey_Return → handleReturnKey()`、`FcitxKey_Left → handleArrowKey(KEYCODE_DPAD_LEFT)`、
    `FcitxKey_Right → handleArrowKey(KEYCODE_DPAD_RIGHT)`（實體鍵 0-9 → fcitx 選第 N 候選）；
  - :627 `forwardKeyEvent`、:645 `onKeyDown`、:656 `onKeyUp`、:680-683 `onBindInput → activate(uid, pkgName)`。
- `up: .../input/keyboard/BaseKeyboard.kt` :166 空白鍵左右 swipe → `FcitxKey_Right/Left`（實體鍵「游標移動」同一路徑）。

**target**：
- `target: .../input/ChewingInputMethodService.kt` 全 171 行：無 `onKeyDown`/`onKeyUp` 覆寫（grep 全樹無 `KeyEvent`）；
  :107-145 `handleKey(key: String)` 只處理螢幕鍵的**字元→libchewing keycode** 路由（:132 `KeyMapping.getLibchewingKeyCode`）。
- 數字符號：:140-142 無 keycode 的字元 `ic.commitText(key,1)`——數字會當一般字元上屏，不會進候選選取。

**GAP 判定**：實體鍵（藍牙/USB/螢幕投影鍵盤）完全無路由；Accept「物理鍵盤數字選字」未落地。

**修補方向**：
1. `ChewingInputMethodService` 覆寫 `onKeyDown`：實體數字鍵 `KEYCODE_1..KEYCODE_0` →
   `chewing.selectCandidate(n-1)`（若正在候選）＋ `updateCandidates()`；`KEYCODE_DPAD_LEFT/RIGHT` →
   `chewing.prevPage()/nextPage()`；`KEYCODE_ENTER` → `commitCandidate(getCandidates().first())`。
2. 佈局顯示規則對齊上游 `setupCandidatesViewEvents`：實體鍵盤作用時才顯示候選列。
3. 多字元上屏仍走 `handleKey`（組合鍵＝實體鍵觸發選候選＋上屏，即「組合鍵選字」驗收 happy-path）。

---

## 6. R2b cand_open 序列驗證（PARITY-OK）

- **R2b 原題（RECONCILE-agnes.md :59 H4）**：`chewing_cand_open` 從未被調用；service 普通按鍵分支從不
  `updateCandidates()`。**R2b 已落地（:73-79）**。
- **現況**：
  - `target: .../engines/android/AndroidChewingEngine.kt` :141-152 `buildCandidates()`：
    :144 `chewing_cand_open(nativeCtx)`（讀 choices 前先 open），:145-151 依 `_candPage * pageSize` 切片。
  - :72-79 `handleKeyEvent(code)`：`chewing_handle_default` 消費 → 刷新 `_preedit/_candidates`（`_candPage = 0`）。
  - service :128/:139 每次鍵（空格／一般字元）消耗後都 `updateCandidates()`；:147-151 `updateCandidates()`
    取 `chewing.getCandidates()` (:88 回 `_candidates`) → `candidateView.setCandidates()`。
- **序列正確**：key → cand_open → enumerate → UI 刷新，且 `nextPage/prevPage`（:154-172）重建時亦重新 open。
  無 stale / 未 open 讀取；與上游「open 在該次候選刷新前」語義一致（差異僅上游由 fcitx daemon 驅動，target 在行程內）。

**判定**：PARITY-OK。**保留要求（回歸防護）**：任何新實體鍵選字／長按路徑必須先經
`buildCandidates()`（或對應 open）再讀取，禁止直讀 `chewing_cand_string_by_index_static` 而跳過 open。

---

## 7. 建議實作順序（供 wave 拆分參考）

1. **§6 已收斂**，不需再動。
2. **§3 按壓預覽**（最小 UI 風險、無新依賴）→ commit `feat(candidates): press preview`。
3. **§4 長按符號表**（需布局資料＋手勢；守主執行緒紀律）→ commit `feat(candidates): longpress popup`。
4. **§5 物理鍵盤選字**（service 加 `onKeyDown` ＋ 顯示規則）→ commit `feat(candidates): physical-kb selection`。
5. **§1＋§2 分頁強化＋展開**（RecyclerView 化＋ExpandButtonStateMachine 概念）最後做，因牽動 `CandidateView` 改型。

每段皆列 `file:line` 對照表於 commit body；Todo 12 結束時更新 `docs/CHECKS/` 相應 wave 檔。

---

## Appendix A — 證據清單

| 項目 | 位置 |
|---|---|
| 上游 PagingSource | `up: candidates/expanded/CandidatesPagingSource.kt:13,16-30,34` |
| 上游 展開狀態機 | `up: input/bar/ExpandButtonStateMachine.kt`；`candidates/horizontal/HorizontalCandidateComponent.kt:21-22,79-85` |
| 上游 展開視窗 | `up: candidates/expanded/window/BaseExpandedCandidateWindow.kt:49,75,152-157,164-166,195-196` |
| 上游 按壓預覽 | `up: input/keyboard/BaseKeyboard.kt:54,295-338`；`input/keyboard/KeyDef.kt:115-116`；`input/popup/PopupComponent.kt:41-42` |
| 上游 長按符號表 | `up: input/keyboard/KeyDef.kt:120-129`；`KeyDefPreset.kt:25,36-38,46,58-60,172,235`；`BaseKeyboard.kt:208-213,248-294,471-485`；`TextKeyboard.kt:140,177,183-202`；`CustomGestureView.kt:56-58` |
| 上游 物理鍵盤 | `up: input/InputDeviceManager.kt:21-42`；`input/FcitxInputMethodService.kt:242-244,627,645,656,680-683`；`keyboard/BaseKeyboard.kt:166` |
| target 分頁 | `target: ui/CandidateView.kt:22,30-37,39-48`；`engines/android/AndroidChewingEngine.kt:145-172` |
| target 無展開/預覽/長按 | `target: ui/CandidateView.kt`（84 行全）；`input/KeyboardView.kt:113-129` |
| target 物理鍵盤 | `target: input/ChewingInputMethodService.kt:107-145`（無 onKeyDown/onKeyUp） |
| R2b 序列 | `target: engines/android/AndroidChewingEngine.kt:72-79,88,141-152`；`input/ChewingInputMethodService.kt:128,139,147-151`；`docs/RECONCILE-agnes.md:59,73-79` |
| 計畫引用 | `D:\666\opencode\.omo\plans\android-keyboard.md:105-109` |