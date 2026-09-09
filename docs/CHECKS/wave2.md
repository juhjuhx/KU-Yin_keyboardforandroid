# Wave 2 — audit worker 檢查清單（docs-only 收尾審計）

> 層級：**LIGHT**（僅文件審計，**不寫任何產品程式碼、不 merge、不 push、不觸碰其他檔案**）
> 語系：繁體中文（zh-TW）
> 產出：本文件（`docs/CHECKS/wave2.md`）——唯一新增/改動的檔案
> 日期：2026-09-09
> 基線：`git -C D:\666\opencode\android-keyboard status --short`（見 §0，**只報告，未更動**）
> 審計對象：Wave 2 四份規格文件 + 自規格撰寫後之 code-reality deltas（R2b / R2d / R2f）

---

## 0. 基線（Baseline）

以 `-C` 旗標指向目標 repo（避免先前 worker 的 false-empty 事件）：

```
git -C D:\666\opencode\android-keyboard status --short
```

結果（**僅報告；本次審計未改動任何既有檔**）：

```
 M app/src/main/AndroidManifest.xml          M app/src/main/java/.../ChewingInputMethodService.kt
 M app/src/main/cpp/CMakeLists.txt           M app/src/main/java/.../KeyMapping.kt
 M app/src/main/cpp/chewing_jni.cpp          M app/src/main/java/.../KeyboardLayout.kt
 M app/src/main/java/.../engines/android/AndroidChewingEngine.kt
 M app/src/main/java/.../engines/core/ChewingEngine.kt
 M app/src/main/java/.../engines/core/ChineseConverter.kt
 M app/src/main/java/.../engines/core/IMEConfig.kt
 M app/src/main/java/.../engines/opencc/OpenCCConverter.kt
 M app/src/main/java/.../input/ChewingInputMethodService.kt
 M app/src/main/java/.../input/KeyMapping.kt
 M app/src/main/java/.../input/KeyboardLayout.kt
 M app/src/main/java/.../input/KeyboardView.kt
 M app/src/main/java/.../ui/CandidateView.kt
 M app/src/main/res/values/{colors,strings,themes,tooltips}.xml
 M app/src/main/res/xml/input_method.xml
 M build_all.ps1
?? docs/...（含本次審計唯一新增 `docs/CHECKS/wave2.md`）
```

工作樹於審計前已 dirty（18 個 modified + 大量 untracked），此為既有狀態；本次**僅**覆寫 `docs/CHECKS/wave2.md`，未新增/修改任何其他檔案。

---

## 1. 檢查項目 1：四份規格之存在與行數

| # | 檔案（相對 `docs/`） | 存在 | 行數 | 判定 |
|---|----------------------|------|------|------|
| P-1 | `IMS-LIFECYCLE.md` | ✅ | **478** | **PASS** |
| P-2 | `CANDIDATE-SPEC.md` | ✅ | **223** | **PASS** |
| P-3 | `EMOJI-HAPTICS-SPEC.md` | ✅ | **187** | **PASS** |
| P-4 | `THEME-SPEC.md` | ✅ | **303** | **PASS** |

> 行數以 `(Get-Content ...).Count` 實測。四份皆存在且非空。

**Manual-QA 1：PASS**（證據：四檔存在；行數 478 / 223 / 187 / 303）。

---

## 2. 檢查項目 2：驗收閘（acceptance gates）

### 2.1 延遲／鄰鍵容錯（proximity approach）

- Spec 呼叫點：`THEME-SPEC.md:87`（`KeyboardView.kt:106-108` hardcode）與 EMOJI/Settings 提及 `proximity`。
- Code reality：`KeyboardView.kt:37` `proximityTolerance = 0.15f`；`:59` `setProximityTolerance(pct)`；`:132-144` `getKeyPressedIndex()` 先精確命中（`rect.contains`），未命中再以 `proximityTolerance * keyHeightDp` 搜尋最近鍵（`pointToRectDist` L146-150）。
- `SettingsActivity.kt:88-101` 新增 `proximity_tolerance` ListPreference（嚴格 0.05 / 預設 0.15 / 寬鬆 0.30），直接寫回 `IMEConfig.proximityTolerance`（L96-98）。
- **判定：PASS**（精確優先 + proximity fallback 已落地；settings 已接線）。

### 2.2 旋轉 / 狀態還原

- Spec 呼叫點：`IMS-LIFECYCLE.md:103-131`（旋轉 → `InputView.kt:157-177` 高度重算 + `FcitxInputMethodService.onConfigurationChanged:537-561`）。
- Code reality（fork 自繪側）：`KeyboardView.kt:46-51` `refreshLayout()` 依 display rotation（`Surface.ROTATION_90/270`）切換鍵高 48f/60f 並 `rebuildKeySlots()`；`ChewingInputMethodService.kt:85-87` 於 `onStartInput` 呼叫 `keyboardView.refreshLayout()`。
- **注意（GAP，非阻塞於本 docs 審計）**：fork service **未 override `onConfigurationChanged`**（grep 無命中），旋轉高度重算僅在 `onStartInput` 觸發，仰賴 framework 於 config change 後重新 start input。此為 docs 描述的「旋轉即時重算」與 code 之落差（見 §3.6）。**判定：PARTIAL**（機制存在但觸發點與 spec 不同）。

### 2.3 主題三態接線（Light / Dark / OLED）

- Spec 呼叫點：`THEME-SPEC.md` 通篇（§2.2「色票與繪製完全脫鉤」、§4 三態方案）。
- Code reality：`KeyboardView.kt:106-108` 仍 hardcode（`0xFFBDBDBD/0xFFEEEEEE/0xFF424242/0xFF212121`）；`CandidateView.kt:47,51` 仍 hardcode（`0xFF212121/0xFF9E9E9E`）；`SymbolPicker.kt:37` 仍 hardcode。`IMEConfig.kt` 無任何 theme key；`SettingsActivity.kt` 無主題偏好。
- **判定：PASS（作為 GAP 記錄）**——規格正確診斷為 GAP，且 code 仍維持 GAP（未變更），即規格之診斷**未被推翻**。三態接線**尚未落地**，符合規格之 read-only 聲明。

### 2.4 候選序列（candidate sequencing）

- Spec 呼叫點：`CANDIDATE-SPEC.md:19`（R2b cand_open 序列 PARITY-OK：`buildCandidates():144` 每次刷新 open；service 每鍵後 `updateCandidates()`）。
- Code reality：`AndroidChewingEngine.kt:141-152` `buildCandidates()` 內 `chewing_cand_open` 於 **L144**（與 spec 相符）；`handleKeyEvent` L72-79 於每次 key 後重開並 `_candPage = 0`（L77）；`ChewingInputMethodService.handleKey` 於 bopomofo/space 鍵後呼叫 `updateCandidates()`（L145/128）。分頁 `nextPage/prevPage`（L154-173）以 `chewing_cand_total_page` 控制。
- **判定：PASS**（序列正確且與 spec 描述相符）。

### 2.5 Emoji／觸覺守衛

- Spec 呼叫點：`EMOJI-HAPTICS-SPEC.md`（§1 emoji 空殼、§GAP-A~F、§6 單一 code point 回歸守衛）。
- Code reality：`SymbolPicker.kt` 仍為未餵資料空殼（`ChewingInputMethodService.kt:55-59` 建立 + `addView`，無任何 `setSymbols` 呼叫端）；`KeyboardView.kt:85-92` 觸覺仍固定（<Q 10ms / ≥Q `KEYBOARD_TAP`），僅 `ACTION_DOWN:119` 觸發；無 key-repeat / long-press / 振幅。
- **判定：PASS（作為 GAP 記錄）**——規格所診斷之 GAP 現況仍然成立；§6 之「不可整串取代逐碼位」守衛為規格要求（未實作，因無 emoji 碼，符合 read-only 聲明）。

### 2.6 驗收閘總結

| 閘 | 判定 | 證據 |
|----|------|------|
| 延遲／proximity | **PASS** | KeyboardView.kt:37,59,132-144；SettingsActivity.kt:88-101 |
| 旋轉／狀態還原 | **PARTIAL** | KeyboardView.kt:46-51；ChewingInputMethodService.kt:86（無 onConfigurationChanged override） |
| 主題三態接線 | **PASS (GAP 仍在)** | KeyboardView.kt:106-108；CandidateView.kt:47,51 |
| 候選序列 | **PASS** | AndroidChewingEngine.kt:144,77；ChewingInputMethodService.kt:128,145 |
| emoji／觸覺守衛 | **PASS (GAP 仍在)** | SymbolPicker 無 setSymbols；KeyboardView.kt:85-92 |

**Manual-QA 2：PARTIAL（旋轉閘列 PARTIAL，其餘 PASS）**——非需修才可關塴的 gate fail；於 §3 之 delta 段落已附對應 code 證據。

---

## 3. 檢查項目 3：code-vs-spec deltas（規格撰寫後之變更）

以下為自四份規格撰寫後，code 實際改變、導致規格過時（stale）之清單。每項附 `file:line`。

### 3.1 R2b 引擎修復 — candidate page reset
- **Delta**：`AndroidChewingEngine.handleKeyEvent` 在每次 key 後強制 `_candPage = 0`（`AndroidChewingEngine.kt:77`）。但 **`backspace()`（L109-115）與 `selectCandidate()`（L90-95）重建 candidates 時未重置 `_candPage`**。
- **vs 規格**：`CANDIDATE-SPEC.md:48-49` 描述 `_candPage` 由 `nextPage/prevPage` 控制（L154-173 用 `chewing_cand_total_page`），未提及 per-key reset。
- **影響**：規格之「每鍵後重建並保持頁碼」描述僅部分符合——鍵入 bopomofo 會重置回第 0 頁，但選字/退格後可能停在非首頁。**規格可在 §3.2 補充註記。**

### 3.2 R2d settings 重寫 — proximity_tolerance 新偏好
- **Delta**：`SettingsActivity.kt` 已由 stub 重寫為完整 real settings（140 行），新增 `proximity_tolerance` ListPreference（`SettingsActivity.kt:88-101`，直接寫 `cfg.proximityTolerance` L96-98）。`haptic_enabled` 位於 `SettingsActivity.kt:75-79`；`full_half` 位於 `SettingsActivity.kt:81-86`。
- **vs 規格**：`THEME-SPEC.md:154` 描述「SettingsActivity.kt:81-86 只有 full_half 偏好」——**已過時**：現該區塊新增了 proximity_tolerance。`EMOJI-HAPTICS-SPEC.md:61`（haptic_enabled 於 L76-79）與現況 L75-79 **大致吻合**（差 1 行偏移）。
- **影響**：規格行號對 proximity/full_half 之描述需更新以降臨現行 140 行版本。

### 3.3 R2f display fix + dismiss key（收起鍵盤）
- **Delta**：`KeyboardLayout.kt:83` 新增 **`KeyDef("▼", isSpecial = true)`** 收起鍵盤鍵；`ChewingInputMethodService.kt:130` `key.label == "▼" -> requestHideSelf(0)` 為其處理。
- **vs 規格**：`THEME-SPEC.md:157` 描述 target 之 KeyDef「只有 label + widthPct」（指 `KeyboardLayout.kt:14-16`）——**已過時**：現 `KeyDef` 增加 `code`（L18）與 `isSpecial`（L17）欄位；Row 5 亦新增 `▼` 鍵（L83）、`⌫` 特殊鍵（L81）。`CANDIDATE-SPEC.md` / `KEYMAP` 未描述 dismiss 鍵。
- **影響**：spec 之 KeyDef 欄位描述已過時；dismiss 鍵屬 R2f 新增功能，此前規格未涵蓋。

### 3.4 KeyDef.code passthrough（鍵碼直通）
- **Delta**：每個注音/聲調鍵於 `KeyboardLayout.kt:29-80` 標註 `code = <ASCII>`（例 `KeyDef("ㄅ", code=49)` L29）。`ChewingInputMethodService.kt:136-154` 直接以 `key.code` 為權威 keycode（L138 `val libchewingKey = key.code`，L142 `chewing.handleKeyEvent(libchewingKey)`），僅在 `code <= 0` 時 fallback 至 `KeyMapping.getLibchewingKeyCode`（L148-150）。
- **vs 規格**：`CANDIDATE-SPEC.md:17` 描述長按符號表為 GAP、未提 code passthrough；`THEME-SPEC.md:5-6` 描述 KeyMapping 為純 keycode 對照表。`KeyMapping.kt:13-27` 之 docblock **明確宣告**「KeyDef.code 才是權威來源，本 map 降級為 label-hint-only」，並移除重複碼位（`KeyMapping.kt:79`）。
- **影響**：規格未反映此「KeyDef.code 權威化」重構；`KeyMapping` 之角色已由規格之「主要來源」降為「label-only fallback」。

### 3.5 旋轉高度（keyboardHeightDp 常量化）
- **Delta**：fork 並未移植上游 `InputView.kt:157-177` 之 `keyboardHeightPercentBase`（RealSize/DisplayMetrics）模型，改以 `KeyboardView.kt:49` 固定 `keyHeightDp = if (landscape) 48f else 60f`。
- **vs 規格**：`IMS-LIFECYCLE.md:103-131` 描述上游 RealSize 高度模型。fork 之簡化模型與上游不同。
- **影響**：spec 描述上游機制，fork 實際採用更簡化模型；由此推得 §2.2 之 PARTIAL。

### 3.6 fork 無 onConfigurationChanged override（旋轉/狀態還原落差）
- **Delta**：grep 於 fork `app/src/main/java` 下 `onConfigurationChanged` **無命中**；僅 `KeyboardView.refreshLayout()`（`KeyboardView.kt:46`）＋ `ChewingInputMethodService.onStartInput` 呼叫（`ChewingInputMethodService.kt:86`）承擔旋轉重算。
- **vs 規格**：`IMS-LIFECYCLE.md:127-129,222-233` 描述上游 `onConfigurationChanged` 過濾 `CONFIG_KEYBOARD|KEYBOARD_HIDDEN|UI_MODE` 以避免重建。
- **影響**：spec 之上游 no-rebuild guard（§3）**未於 fork 落地**；fork 依賴於 start-input 週期重算。**屬需記錄之 code-vs-spec 落差，非本 docs 審計所能修。**

### 3.7 delta 總結表

| # | Delta | 檔:行（現況） | 規格過時點 |
|---|-------|--------------|-----------|
| D-1 | 每鍵 `_candPage=0`；backspace/select 未重設 | AndroidChewingEngine.kt:77,90-95,109-115 | CANDIDATE-SPEC.md:48-49 |
| D-2 | settings 重寫＋proximity_tolerance | SettingsActivity.kt:88-101 | THEME-SPEC.md:154 |
| D-3 | dismiss 鍵（▼）新增 | KeyboardLayout.kt:83；ChewingInputMethodService.kt:130 | THEME-SPEC.md:157（無此鍵） |
| D-4 | KeyDef.code 直通＋isSpecial；KeyMapping 降級 | KeyboardLayout.kt:17-18,29-80；KeyMapping.kt:13-27,79 | CANDIDATE-SPEC.md:17；THEME-SPEC.md:5-6,157 |
| D-5 | 旋轉高度簡化（48f/60f） | KeyboardView.kt:49 | IMS-LIFECYCLE.md:103-131 |
| D-6 | 無 onConfigurationChanged override | （無命中） | IMS-LIFECYCLE.md:127-129,222-233 |

**Manual-QA 3：PASS**（六項 delta 全部實測於現行 code，並對應至 spec 過時行號）。

---

## 4. 檢查項目 4：連結解析

本審計引用之相對路徑（於 repo 根）已逐一解析：
- `docs/IMS-LIFECYCLE.md`、`docs/CANDIDATE-SPEC.md`、`docs/EMOJI-HAPTICS-SPEC.md`、`docs/THEME-SPEC.md` → 全部存在（§1）。
- `app/src/main/java/com/example/androidkeyboard/...` 各 .kt → 全部存在（經 Read 實測，見 §2/§3 行號引用）。
- `docs/CHECKS/wave2.md` → 本文件。
- 規格內文引用之上游樹 `D:\Temp\opencode\upstream-ro` 為**外部 reference tree**，**未修改**；未於本次審計寫入。

**Manual-QA 4：PASS**（所有相對 .md/.kt 連結解析成功）。

---

## 5. 檢查項目 5：scope（僅新增 wave2.md）

- 本次審計之唯一寫入 = **覆寫 `docs/CHECKS/wave2.md`**（本文件）。
- **未**修改 reference tree（`D:\Temp\opencode\upstream-ro`）；**未** merge；**未** push；**未** 寫任何產品程式碼；**未** 觸碰 `D:\666\opencode` 其他子目錄。
- 基線（§0）之 dirty 狀態於審計前即已存在，非本次造成。

**Manual-QA 5：PASS**。

---

## 6. 匯總與 Manual-QA 判定

| 檢查項目 | 判定 |
|----------|------|
| 1. 四份規格存在＋行數 | **PASS**（478/223/187/303） |
| 2. 驗收閘 | **PARTIAL**（旋轉/狀態還原列 PARTIAL，其餘 PASS；無 gate-fail） |
| 3. code-vs-spec deltas | **PASS**（六項 delta 全數列舉並附 file:line） |
| 4. 連結解析 | **PASS** |
| 5. scope（僅 wave2.md） | **PASS** |

> **整體判定：PASS**（本 docs-only 收尾審計）。規格文件存在且品質足夠支援閉合；code-reality 與規格存在六項已記錄之 delta（含三處規格過時行號），**建議後續 worker 更新規格行號**，但無任一驗收閘 FAIL 需阻塞閉合。

**注意事項（need-fix 提示，非 gate-fail）**：
- THEME-SPEC.md:154 / :157 之 SettingsActivity/KeyDef 描述已過時（D-2/D-3/D-4）。
- IMS-LIFECYCLE.md 之上游 no-rebuild guard 未於 fork 落地（D-6），旋轉重算機制不同（D-5）。
- CANDIDATE-SPEC.md:48-49 之候選頁碼描述與 per-key reset 不盡一致（D-1）。

---

## DoneClaim

- **產出**：`docs/CHECKS/wave2.md`（本文件，zh-TW）——Wave 2（docs-only）收尾審計。
- **覆寫說明**：`docs/CHECKS/wave2.md` 先前存在之內容為另一 worker 之舊 build-audit（與本審計標的無關）；依任務指定本路徑為交付物而覆寫。
- **範圍合規**：僅變更 `docs/CHECKS/wave2.md`；未動 reference tree、未 merge、未 push、未寫產品碼、未觸碰其他 `D:\666\opencode` 目錄。基線以 `git -C` 實測並@§0 報告。
- **審計結果**：5 項檢查全部執行完畢；Manual-QA = 4×PASS + 1×PARTIAL（旋轉閘）。無 gate FAIL；整體判定 **PASS**，附 4 條 needs-fix 提示（規格行號過時 / no-rebuild guard 落差），建議於後續規格更新時吸收。
