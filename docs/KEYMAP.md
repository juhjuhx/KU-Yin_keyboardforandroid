# 鍵盤佈局與尺寸規格（KEYMAP）

> 狀態：**骨架（skeleton）** — 本檔定義目標鍵盤尺寸規格表（人體工學目標，超 Gboard）。實際量測與驗證於 Todo 16（大千 4x10 直橫屏規格表落地）執行。
>
> ⏳ **量測 TODO（未實作）**：直橫屏的真機尺規截圖量測、鄰鍵誤觸走查、AOSP `getNearestKeys` 邊緣歸位抽查，均由 Todo 16–19 承接，本文僅為規格基準。

---

## 1. 佈局概覽

- 預設注音佈局：**大千（DaChen）4x10**。
- 可切換：Hsu、Eten26。
- 尺寸語義對標 AOSP `%p`（例如 `keyWidth = 10%p` 表示 10 列）。

## 2. 目標尺寸規格表（Target Spec Table）

> 規格為「目標值」，任一實作偏離時**改碼不改表**（Todo 16 原則）。

### 直屏（Portrait）

| 項目 | 目標值 | 單位 |
|------|--------|------|
| 鍵盤總高 | 252 – 280 | dp |
| 鍵高 | 60 | dp（min 56） |
| 鍵寬 | 10 | %p |
| 水平間隙（hGap） | 3 | dp |
| 垂直間隙（vGap） | 6 | dp |
| 候選欄高度 | 44 | dp |
| 空格鍵寬 | 35 | %p |
| Enter 鍵寬 | 15 | %p |
| 長按觸發時間 | 350 | ms |

### 橫屏（Landscape）

| 項目 | 目標值 | 單位 |
|------|--------|------|
| 鍵盤總高 | 180 – 200 | dp |
| 鍵高 | 48 | dp |
| 其餘 | 同直屏（除總高/鍵高） | — |

---

## 3. 待補（TODO）

- [ ] Todo 16：規格表正式落地 + 尺規截圖量測。
- [ ] Todo 17：工具列簡繁一鍵 + per-profile 設定。
- [ ] Todo 19：Gboard 對比走查（速度 / 誤觸率 / 首選命中）。

---

_本檔為 Todo 1 骨架；量測證據於 Todo 16–20 補充。_

---

## 4. 規格凍結：參照交叉驗算（Todo 16 · SPEC-FREEZE）

> 本節於 Todo 16 將 §2 目標表**凍結為規格基準**。對照來源：Futo Keyboard 裝置高度表、AOSP `%p` 語義、上游 0.1.3 RealSize 基線。下列驗算為**靜態推導**（純公式），**非真機量測**——真機尺規驗證 **DEFERRED 至 F3**（見 §6）。

> ⚠️ **凍結範疇**：§2 目標值在本節凍結為「規格」；但任一代碼實作偏離時仍**改碼不改表**（見 §5 各 `UNWIRED` 標記）。下方「GAP」欄標註的是**表與參照無法對齊**或**表值無代碼消費**之處，誠實揭露，**不竄改表值**。

### 4.1 Futo 裝置高度表 vs 本表（含 44dp 候選欄的算式一致性）

Futo Keyboard 裝置高度表（keyboard-only，不含候選欄）與本表交叉驗算。本表「鍵盤總高」= **鍵盤本體 + 44dp 候選欄**（見上下文），故：

| 方向 | Futo keyboard-only | 本表鍵盤總高 | 本表 keyboard-only（總高 − 44dp 候選欄） | 差值 vs Futo | 判讀 |
|------|--------------------|--------------|-------------------------------------------|--------------|------|
| 直屏 | 205.6 dp | 252–280 dp | **208 – 236 dp** | **+2.4 ~ +30.4 dp** | ⚠️ **偏高**：本表 keyboard-only 下限 208 > Futo 205.6，差 2.4dp（落在量測 2dp 容差的邊緣，但 `252 − 44 = 208 ≠ 205.6`，**不符，為真實 GAP**，見 §4.4）。 |
| 橫屏 | 176.0 dp | 180–200 dp | **136 – 156 dp** | **−20 ~ −40 dp** | 🔴 **嚴重偏低**：本表橫屏 keyboard-only（`180−44=136` 至 `200−44=156`）遠低於 Futo 176dp。**不符，為重大 GAP**，見 §4.4。 |
| sw600 | 302.4 dp | —（本表無） | — | — | 本表未定義平板檔，無從驗算。 |
| sw768 | 365.4 dp | —（本表無） | — | — | 本表未定義平板檔，無從驗算。 |

### 4.2 AOSP `%p` 語義驗證

AOSP `Keyboard`/`Row` 的 `keyWidth/keyHeight` 支援 `%p` 前綴（`Pointer > PressMarker` 與 `Keyboard/Row` 的 `keyWidth`），`10%p` = 佔**父容器寬度 10%**，即每列 10 鍵（10 × 10% = 100%）。本表 `鍵寬 10%p`（§2 直屏）對應大千 4x10 的 **10 列** — **一致 ✅**。

- 消費點：`KeyboardView.kt:75` `val kW = key.widthPct * pctToPx - hGap`，其中 `pctToPx = w / rowTotalPct`（`KeyboardView.kt:73`）。Row 總寬以 `widthPct` 合計正規化，`10%p` 語義落地為「均分列寬」。
- **注意**：`widthPct` 為 `Float`、`%` 以「佔列比例」而非「佔全寬」解釋（`pctToPx = w / rowTotalPct`），與 AOSP 嚴格 `%p`（佔全寬百分比）**語義近似但非完全等同**——AOSP 中 `10%p` 是全寬 10%，本實作是「列內 10/100 均分」。此差對 10 鍵滿列無影響，對非滿列（Row 5）有影響（見 §5「空格/Enter UNWIRED」）。**列為觀察項，不竄改表值。**

### 4.3 上游 0.1.3 RealSize vs DisplayMetrics 高度基線

- 上游 0.1.3 以 **RealSize**（`Display.getRealSize` / 真實螢幕像素）作為高度基線（旋轉不閃、高度穩定）。
- 本實作以 **`resources.displayMetrics.density`**（`KeyboardView.kt:96,136`；`init` `KeyboardView.kt:41`）做 px 換算。`displayMetrics` 受 App 視窗可用區域影響，未必等於真實螢幕。
- **判讀**：表值是 **dp 單位**，與 px 換算基線（RealSize vs DisplayMetrics）**正交**——只要 density 正確，同一 dp 表值不因基線不同而變。故此非表值錯誤，但**換算通路需在 F3 真機量測時用 RealSize 校準**，列為 F3 前置項（§6.4）。

### 4.4 GAP 彙總（誠實揭露，不竄改）

| # | 表值 | 對照 | 差異 | 嚴重度 | 處置 |
|---|------|------|------|--------|------|
| G1 | 直屏總高下限 252 | Futo 205.6 + 44 = 249.6 | **+2.4dp** | 輕（>2dp 容差） | 若真機量測落在 252–280 內 **改碼** 貼齊 Futo 249.6；不改表。F3 決策點。 |
| G2 | 橫屏總高 180–200 | Futo 176 + 44 = 220 | **−20 ~ −40dp** | 🔴 重大 | 本表橫屏總高與「Futo 176dp keyboard-only + 44dp 候選欄 = 220dp」矛盾。可能：① 本表 180 為「不含候選欄」的誤標；② 或橫屏有意壓低至 156 鍵高。**留待 F3 真機量測後以「改碼不改表」方向修正實作，並於 §6 記錄**；表值暫凍結、標 `⚠️ 待F3覆核`。 |
| G3 | 空格 35%p / Enter 15%p | AOSP `%p` 合理（35+15=50，另半行放 2 鍵），但 **本表無對應代碼** | — | 🔴 UNWIRED | 見 §5。 |
| G4 | 平板 sw600/sw768 | 本表僅直/橫，無平板檔 | — | 缺口 | 本表**未凍結平板**；若需求涵蓋，於後續 Todo 補表（**本節不改表**）。 |

---

## 5. 代碼連結（Code Linkage）：表值 → 消費點

> 逐項以 **grep 實證**（下列引用為真實匹配行）。任一表值若**無代碼消費**即標 **UNWIRED**。

| 表值（§2） | 消費檔案:行 | 實證 grep / 摘錄 | 狀態 |
|-----------|-------------|------------------|------|
| 鍵高 60dp（直） | `KeyboardView.kt:32` `keyHeightDp = 60f`；`:49` 旋轉分支 `else 60f` | `:49` `keyHeightDp = if (rotation == ROTATION_90/270) 48f else 60f` | ✅ WIRED |
| 鍵高 48dp（橫） | `KeyboardView.kt:49` `48f` | 同上 | ✅ WIRED |
| 鍵寬 10%p | `KeyboardLayout.kt:16` `KeyDef(widthPct: Float = 10f)`；`KeyboardView.kt:75` `kW = key.widthPct * pctToPx - hGap` | `:75` | ✅ WIRED（語義近似 AOSP，見 §4.2） |
| 水平間隙 3dp | `KeyboardView.kt:33` `hGapDp = 3f`；`:67` `hGap = hGapDp * density`；`:75` `- hGap` | `:33`,`:67`,`:75` | ✅ WIRED |
| 垂直間隙 6dp | `KeyboardView.kt:34` `vGapDp = 6f`；`:68` `vGap = vGapDp * density`；`:76` `- vGap`；`:97` onMeasure 加總 | `:34`,`:68`,`:76`,`:97` | ✅ WIRED |
| 候選欄 44dp | `res/values/colors.xml:31` `<dimen name="candidate_bar_height">44dp</dimen>`；`res/layout/activity_input.xml:15` `layout_height="@dimen/candidate_bar_height"` | → 但 `activity_input.xml` **未被任何 .kt inflate**（`ChewingInputMethodService.onCreateInputView` 以程式碼建 FrameLayout，`ChewingInputMethodService.kt:41-70`；無 `R.layout.activity_input` 引用），故候選欄實際高度在 IME 路徑**未落地** | ⚠️ **UNWIRED（實作路徑）** — 44dp 僅活在未使用之 XML；真機 IME 頻道候選欄高度未定。F3 修。 |
| 空格 35%p | **無** — `KeyboardLayout.kt` Row5 空格為 `KeyDef(" ", code=65)`（`:82`），用預設 `widthPct=10f` | grep `space/enter/width` in `KeyboardLayout.kt` 無 35/15 匹配 | 🔴 **UNWIRED** — 表值無代碼消費。 |
| Enter 15%p | **無** — Row5 無獨立 Enter 鍵（僅 ⌫/Space/▼，`KeyboardLayout.kt:78-84`）；全佈局無 15%p | 同上 | 🔴 **UNWIRED** — 表值無代碼消費。 |
| 長按 350ms | `res/values/colors.xml:34` `<integer name="long_press_delay_ms">350</integer>` | grep `long|press|timeout|repeat` in `*.kt` **無 Kotlin 消費**（`KeyboardView.onTouchEvent` 僅處理 DOWN/UP/CANCEL，`KeyboardView.kt:113-129`，**無長按計時器**） | ⚠️ **UNWIRED（實作路徑）** — 350 integer 僅存 XML；IME 現無長按觸發邏輯。F3/後續 Todo 實作。 |

**UNWIRED 清單（需在 F3 或後續 Todo 接線，本節不改表）**：
1. 候選欄 44dp — 實作路徑未用（改接 activity_input.xml 或於 onCreateInputView 給定高度）。
2. 空格 35%p — 無消費（Row5 空格仍 10f）。
3. Enter 15%p — 無消費且無獨立 Enter 鍵。
4. 長按 350ms — 無計時器消費。

---

## 6. F3 真機量測協定（Measurement Protocol）

> 本節為 F3 的**可執行步驟**。設備測試仍待使用者提供真機，**本節未執行、未簽核**；下述為步驟定義，非測量結果。

### 6.1 前置條件
- 真機（實體裝置，非模擬器）安裝 `./gradlew :app:assembleDebug`、`:app:installDebug` 產出之 APK。
- 螢幕關閉「字體縮放」強制，設為預設；關閉 WM 顯示尺寸縮放（開發者選項，「最小寬度」設預設）。

### 6.2 尺規截圖法（Ruler Screenshot）步驟
1. 於輸入框聚焦，喚起鍵盤（直屏）。
2. 截圖（`adb exec-out screencap -p > shot_portrait.png`，或裝置電源鍵截圖）。
3. 以可開圖尺規工具（PPT/ImageJ/GIMP）疊加**全寬參考線**與**逐排參考線**：量鍵盤本體上緣（首排頂）→ 下緣（末排底含 vGap）之 **dp 高度**。dp = px / density；density 由 `adb shell wm density` 取得。
4. 量**候選欄高度**（候選文字區塊頂→底）。
5. 量**單鍵高**（同排任 2 鍵，取其平均值）與**間隙**（hGap/vGap 各取 ≥3 處平均）。
6. 重複 2–5 於**橫屏**（旋轉裝置後重新喚起鍵盤）。
7. 每方向至少 3 次量測，取中位數。

### 6.3 判定標準
- **2dp 容差規則**：任一量得值與 §2 表值差異 **≤ 2dp** → 判定**符合**，表值凍結成立。
- 差異 **> 2dp** → 適用 **改碼不改表**：記錄實測值與表值於本檔（或 F3 量測紀錄），修改**實作代碼**使之與表一致；**表不更動**，除非證實表本身錯誤（如 G2 橫屏總高 vs Futo 220dp）則另於 §6.4 處理。

### 6.4 特別決策點（F3 觸發）
- **G1（直屏下限 252 vs 249.6）**：若實測落 252–280，改碼貼齊 Futo 249.6（keyboard-only 205.6 + 44）。
- **G2（橫屏 180–200 vs Futo 220）**：若實測橫屏遠低於 Futo，重新確認「180 是否誤標為不含候選欄」；**明確後更新本表**（唯一允許改表的例外，須記錄原因）。
- **44dp 候選欄**：量測其真實高度以決定實作接線（§5 UNWIRED #1）。
- **長按 350ms**：實作長按計時器後驗證觸發閾值與 `long_press_delay_ms` 一致（§5 UNWIRED #4）。

### 6.5 量測交付物
- 直/橫各 ≥3 張含尺規參考線截圖（png）。
- 量測對照表：| 表值 | 實測(dp) | 差異 | 判定 | 決策 |。
- 若有「改碼不改表」動作，附對應 code diff 與重新 build 證據。

---

## 7. 凍結狀態與 F3 待辦

| 項 | 狀態 |
|----|------|
| §2 目標表 | 🟡 **已凍結（SPEC-FREEZE）**，但 G2 橫屏總高列 `⚠️ 待F3覆核`；真機驗證 **DEFERRED 至 F3** |
| §4 參照交叉驗算 | ✅ 完成（含 G1–G4 GAP 揭露） |
| §5 代碼連結 | ✅ 完成（4 項 UNWIRED 標記） |
| §6 量測協定 | 📋 定義完成，**未執行**（等待使用者提供真機） |

> **F3 簽核門檻**：當真機量測全數通過且 G2 解決後，本表由「凍結」晉升「**定版（LOCKED）**」。在此之前**不得宣稱已達標**。
