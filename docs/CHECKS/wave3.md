# Wave 3 CHECKS — docs-only audit（zh-TW）

> 日期：2026-09-09
> 層級：**LIGHT**（docs-only audit，**不寫任何產品程式碼、不 merge、不 push、不觸碰其他檔案**）
> 產出：本文件（`docs/CHECKS/wave3.md`）——唯一覆寫的檔案
> 審計範圍：T16 KEYMAP freeze、T17 converter re-sync、T18 manifest verdict、T19 STATUS deferred、R2 code deltas
> 基線：`git -C D:\666\opencode\android-keyboard status --short`（見 §0，**只報告，未更動**）

---

## 0. 基線（Baseline）

```
git -C D:\666\opencode\android-keyboard status --short
```

結果（**僅報告；本次審計未改動任何既有檔**）：

```
 M app/src/main/AndroidManifest.xml
 M app/src/main/cpp/CMakeLists.txt
 M app/src/main/cpp/chewing_jni.cpp
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
 M app/src/main/res/values/colors.xml
 M app/src/main/res/values/strings.xml
 M app/src/main/res/values/themes.xml
 M app/src/main/res/values/tooltips.xml
 M app/src/main/res/xml/input_method.xml
 M build_all.ps1
 M docs/CHECKS/wave2.md
 M docs/IOS-NOTES.md
 M docs/KEYMAP.md
?? AUDIT-REPORT.md
?? BUILD-SUCCESS.md
?? docs/AUDIT-R3.md
?? docs/CANDIDATE-SPEC.md
?? docs/EMOJI-HAPTICS-SPEC.md
?? docs/INTEGRATION.md
?? docs/RECONCILE-agnes.md
?? docs/REVIEW-2026-09-09.md
?? docs/STATUS-2026-09-09.md
?? docs/THEME-SPEC.md
```

工作樹於審計前已 dirty（18 個 modified + 大量 untracked），此為既有狀態；本次**僅**覆寫 `docs/CHECKS/wave3.md`。

---

## 1. 檢查項目 1：Wave 3 輸入之存在性

| # | 輸入（Task） | 檔案 | 存在 | 行數 | 判定 |
|---|-------------|------|------|------|------|
| P-1 | T16 KEYMAP freeze | `docs/KEYMAP.md` | ✅ | **163** | **PASS** |
| P-2 | T17 converter re-sync | `app/.../input/ChewingInputMethodService.kt` | ✅ | **201** | **PASS** |
| P-3 | T18 manifest verdict | `app/src/main/AndroidManifest.xml` | ✅ | **24** | **PASS** |
| P-4 | T18 manifest verdict | `app/src/main/res/xml/input_method.xml` | ✅ | **7** | **PASS** |
| P-5 | T18 manifest verdict | `app/build.gradle.kts`（applicationId） | ✅ | 有 `applicationId` | **PASS** |
| P-6 | T19 STATUS | `docs/STATUS-2026-09-09.md` | ✅ | **244** | **PASS** |
| P-7 | T19 baseline | `docs/CHECKS/t19-gboard-baseline.md` | ✅ | **71** | **PASS** |
| P-8 | T20 self-check | `docs/CHECKS/wave3-t20.md` | ✅ | **91** | **PASS** |
| P-9 | R2 deltas — KeyDef.code | `app/.../input/KeyboardLayout.kt` | ✅ | **92** | **PASS** |
| P-10 | R2 deltas — dismiss key | `app/.../input/KeyboardLayout.kt`（L83） | ✅ | `▼` 鍵存在 | **PASS** |
| P-11 | R2 deltas — KeyMapping | `app/.../input/KeyMapping.kt` | ✅ | **96** | **PASS** |

**Manual-QA 1：PASS**（11 項輸入全部存在且非空）。

---

## 2. 檢查項目 2：驗收閘（Acceptance Gates）

### 2.1 T16 — KEYMAP freeze（SPEC-FREEZE + G1/G2 + 4 UNWIRED + F3 gate）

#### 2.1.1 SPEC-FREEZE 凍結狀態

- KEYMAP.md §7（L154-163）明確記載：`§2 目標表 🟡 已凍結（SPEC-FREEZE）`，G2 橫屏總高列標 `⚠️ 待F3覆核`。
- §4（L55-93）交叉驗算（Futo/AOSP/上游）已完成；G1–G4 四個 GAP 已揭露。
- §5（L96-117）代碼連結已完成，4 項 UNWIRED 標記完畢。
- **判定：PASS** — 凍結狀態已記錄。

#### 2.1.2 G1（直屏下限 252dp vs Futo 249.6dp）

- KEYMAP.md §4.4 G1（L89）記載：差值 +2.4dp（>2dp 容差），列為「輕（>2dp 容差）」，若真機量測落在 252–280 內「改碼貼齊 Futo 249.6；不改表」。
- §7 之 G2 標記 `⚠️ 待F3覆核`，但 G1 未列 `待F3覆核`（因為 G1 差值 +2.4dp 在邊緣，可由 F3 決策）。
- **判定：PASS** — G1 已揭露且有明確處置指引。

#### 2.1.3 G2（橫屏 180–200dp vs Futo 220dp）

- KEYMAP.md §4.4 G2（L90）記載：差值 −20~−40dp，標 `🔴 重大`，列為「本表橫屏總高與 Futo 220dp 矛盾」，`留待 F3 真機量測後以「改碼不改表」方向修正實作`。
- §7 列 `⚠️ 待F3覆核`。
- **判定：PASS** — G2 已揭露為重大 GAP，明確 DEFERRED 至 F3。

#### 2.1.4 4 項 UNWIRED

KEYMAP.md §5（L108-116）明確列出 4 項 UNWIRED：

| # | UNWIRED 項 | 代碼證據 | 判定 |
|---|-----------|---------|------|
| U1 | 候選欄 44dp — 實作路徑未用 | `activity_input.xml` 未被任何 .kt inflate；ChewingInputMethodService.kt 以 `44f * density` 程式碼建 `candH`（L65） | PASS（UNWIRED 已記錄） |
| U2 | 空格 35%p — 無消費 | KeyboardLayout.kt Row5 空格仍為 `widthPct=10f`（L82） | PASS（UNWIRED 已記錄） |
| U3 | Enter 15%p — 無消費且無獨立 Enter 鍵 | Row5 無 Enter 鍵（L78-84：⌫/Space/▼） | PASS（UNWIRED 已記錄） |
| U4 | 長按 350ms — 無計時器消費 | XML 存 `long_press_delay_ms=350`，但 KeyboardView.onTouchEvent 僅處理 DOWN/UP/CANCEL（L113-129），無長按計時器 | PASS（UNWIRED 已記錄） |

- **判定：PASS**（4 項 UNWIRED 全數於 KEYMAP.md §5 標記並附代碼連結）。

#### 2.1.5 F3 gate

- KEYMAP.md §6（L120-150）定義 F3 真機量測協定，含步驟、判定標準（2dp 容差）、特別決策點。
- §7 簽核門檻：`當真機量測全數通過且 G2 解決後，本表由「凍結」晉升「定版（LOCKED）」。在此之前不得宣稱已達標`。
- **T19 deferred**（見 §2.4），F3 無真機數據，故 F3 gate 狀態 = **DEFERRED**。
- **判定：PASS** — F3 gate 已定義，且正確 DEFERRED。

#### 2.1.6 T16 總判定

| 子項 | 判定 |
|------|------|
| SPEC-FREEZE 凍結 | **PASS** |
| G1 揭露 | **PASS** |
| G2 揭露 | **PASS** |
| 4 UNWIRED 標記 | **PASS** |
| F3 gate | **DEFERRED**（無真機數據） |

---

### 2.2 T17 — Converter re-sync（ChewingInputMethodService L96-99）

- 任務描述：`service L96-99` 指 ChewingInputMethodService.kt 的 converter re-sync 邏輯。
- Code reality：`ChewingInputMethodService.kt:96-99` 實際位於 `onStartInput` 方法內：
  - L98：`converter.enabled = config.conversionEnabled`
  - L99：`converter.init(config.s2tProfile, config.t2sProfile)`
- 功能：每次 `onStartInput` 時重讀 SharedPreferences 設定，將 converter 狀態與 config 同步。若使用者在 Settings 中切換 profile/toggle，無需重啟 process。
- **判定：PASS** — re-sync 機制存在且位於指定行號範圍。

#### T17 總判定

| 子項 | 判定 |
|------|------|
| converter.enabled sync | **PASS**（L98） |
| converter.init sync | **PASS**（L99） |
| 於 onStartInput 觸發 | **PASS**（L96） |

---

### 2.3 T18 — Manifest verdict（zero INTERNET + single subtype + applicationId placeholder）

#### 2.3.1 零 INTERNET permission

- AndroidManifest.xml L3：`<uses-permission android:name="android.permission.VIBRATE" android:maxSdkVersion="30" />`
- **無** `android.permission.INTERNET` 宣告。
- **判定：PASS** — 零 INTERNET 已確認。

#### 2.3.2 Single subtype

- input_method.xml L6：`<subtype android:label="@string/ime_name" android:imeSubtypeLocale="zh_TW" android:imeSubtypeMode="keyboard" />`
- 僅 **1 個** subtype（zh_TW keyboard mode），無多餘 subtype。
- **判定：PASS** — 單一 subtype 已確認。

#### 2.3.3 applicationId placeholder

- app/build.gradle.kts L11：`applicationId = "com.example.androidkeyboard"`
- `com.example.androidkeyboard` 為 Android Studio 預設 placeholder（非正式發佈用 applicationId）。
- AndroidManifest.xml 中 service 套件名稱同為 `com.example.androidkeyboard`（L14, L16）。
- **判定：PASS** — applicationId 仍為 placeholder，需於發佈前（T23/T24）更換為正式 ID（如 `io.github.<user>.kuyin`）。

#### 2.3.4 其他 manifest 檢查

| # | 項目 | 結果 | 判定 |
|---|------|------|------|
| SEC-1 | VIBRATE maxSdkVersion=30 | ✅ | PASS |
| SEC-2 | SettingsActivity exported=true + LAUNCHER | ✅（L5-12） | PASS |
| SEC-3 | Service BIND_INPUT_METHOD permission | ✅（L17） | PASS |
| SEC-4 | meta-data → @xml/input_method | ✅（L21） | PASS |

#### T18 總判定

| 子項 | 判定 |
|------|------|
| 零 INTERNET | **PASS** |
| Single subtype | **PASS** |
| applicationId placeholder flagged | **PASS**（placeholder 已記錄） |
| 安全性 | **PASS** |

---

### 2.4 T19 — STATUS（BLOCKED on device → DEFERRED）

#### 2.4.1 狀態文件存在性

- `docs/STATUS-2026-09-09.md` 存在，244 行，由 Agnes (Hermes Agent) 於 2026-09-09 14:05 生成。
- `docs/CHECKS/t19-gboard-baseline.md` 存在，71 行，標 `PENDING（分析基準表，待實機量測驗證）`。

#### 2.4.2 實機量測狀態

- STATUS 報告 §5（L169-206）列出「下一步行動」：需連接真機（adb logcat）、測試 APK 安裝、調試鍵盤彈出。
- t19-gboard-baseline.md §4（L62-67）列出 4 項後續步驟（實機測量 Gboard 鍵尺寸、實測誤觸率、記錄 switch speed、更新對比報告）。
- **無真機量測數據**：基準表中所有數值均為「目測估計」或「推導」（t19-gboard-baseline.md L23：`Gboard 實際尺寸需實機測量；上表為目測估計值`）。
- **判定：DEFERRED** — 正確記錄為 deferred，無任何指標被偽造為 PASS。

#### 2.4.3 T19 對 Wave 3 其他閘之影響

- T19 deferred 導致 **F3 gate 無真機數據**（KEYMAP.md §6.5 量測交付物未交付）。
- 由此：G1 的 +2.4dp 判定無法以實測確認；G2 的 −20~−40dp 偏差無法以實測驗證。
- **所有 T19 衍生之門檻（T19-derived gates）均記錄為 DEFERRED，從未標記為 PASS**。

#### T19 總判定

| 子項 | 判定 |
|------|------|
| 狀態文件存在 | **PASS** |
| 實機數據缺失 | **DEFERRED**（正確記錄） |
| 無偽造指標 | **PASS**（未將估計值標為 PASS） |
| F3 gate 連鎖 DEFERRED | **DEFERRED**（正確傳遞） |

---

### 2.5 驗收閘總結

| Task | 閘項 | 判定 | 備註 |
|------|------|------|------|
| T16 | SPEC-FREEZE 凍結 | **PASS** | KEYMAP.md §7 |
| T16 | G1 揭露 | **PASS** | KEYMAP.md §4.4 G1 |
| T16 | G2 揭露 | **PASS** | KEYMAP.md §4.4 G2，🔴 重大 |
| T16 | 4 UNWIRED 標記 | **PASS** | KEYMAP.md §5 U1-U4 |
| T16 | F3 gate | **DEFERRED** | 無真機數據 |
| T17 | converter re-sync | **PASS** | ChewingInputMethodService.kt:96-99 |
| T18 | 零 INTERNET | **PASS** | AndroidManifest.xml（無 INTERNET） |
| T18 | Single subtype | **PASS** | input_method.xml（1 個 zh_TW） |
| T18 | applicationId placeholder | **PASS** | build.gradle.kts L11 |
| T19 | 實機量測 | **DEFERRED** | 無真機；基準為估計值 |
| T19 | F3 連鎖 DEFERRED | **DEFERRED** | 正確傳遞，從未 PASS |

> **Manual-QA 2：PASS**（所有 T16/T17/T18 閘 PASS；T19-derived 閘正確 DEFERRED）。

---

## 3. 檢查項目 3：R2 code deltas 影響 Wave 3

以下為自 Wave 2 之後（或規格撰寫後），code 實際改變、影響 Wave 3 任務之 delta。每項附 `file:line` 證據。

### 3.1 KeyDef.code（鍵碼直通構建）

- **Delta**：`KeyboardLayout.kt:14-19` `KeyDef` 新增 `code: Int = -1`（L18）與 `isSpecial: Boolean = false`（L17）。每個注音/聲調鍵於 Row 1-4 標註 `code = <ASCII>`（L29-80）。
- **消費點**：`ChewingInputMethodService.kt:153-158` 以 `key.code` 為權威 keycode（`val libchewingKey = key.code` L155），`chewing.handleKeyEvent(libchewingKey)` L159。僅在 `code <= 0` 時 fallback 至 `KeyMapping.getLibchewingKeyCode`（L164-168）。
- **KeyMapping 降級**：`KeyMapping.kt:13-27` docblock 明確宣告「KeyDef.code 才是權威來源，本 map 降級為 label-hint-only」。`charToLibchewingKeyCode` 移除了重複碼位（L79: ㄉ→50, ㄋ→115, ㄊ→119 為 canonical hint）。
- **影響 T16**：KEYMAP.md §5 之「鍵寬 10%p」連結至 `KeyboardLayout.kt:16` `widthPct: Float = 10f`，此為正確消費。T16 凍結表值與 code 一致。
- **影響 T18**：T18 對照表（wave3.md 舊版 L54-96）列出之 KeyEvent 列與 `KeyboardLayout.kt` 之 `code` 值**不完全一致**（例如舊 wave3.md L56 列 `KEYCODE_A` 但 KeyboardLayout.kt L29 列 `code=49` 即 KEY_1）。**此為舊 wave3.md 對照表之錯誤**（KEYCODE_A=29 vs 實際 49），本次覆寫已移除該過時對照表。
- **判定：PASS** — KeyDef.code 權威化完整落地。

### 3.2 Display table（顯示表 40 鍵）

- **Delta**：`KeyboardLayout.kt:28-84` 大千佈局 5 列 40 鍵全部標註 `code`：
  - Row 1（L29-40）：11 鍵，code 49-45（數字行）
  - Row 2（L42-53）：10 鍵，code 113-112（Q-P）
  - Row 3（L55-66）：10 鍵，code 97-59（A-L + ;）
  - Row 4（L68-76）：7 鍵，code 122-109（Z-M，部分鍵跨碼位：ㄉ=X=120, ㄊ=C=99, ㄋ=V=118）
  - Row 5（L78-84）：5 鍵，含 ⌫/Space(65)/▼
- **Total**：43 個 KeyDef（含 ⌫、Space、▼），其中注音/聲調 40 鍵。
- **KeyMapping 對照**：`KeyMapping.kt:41-88` `charToLibchewingKeyCode` 含 35 個 entry（因去重：ㄉ/ㄋ/ㄊ 各僅 1 個 canonical entry）。
- **判定：PASS** — display table 完整覆蓋大千 40 鍵。

### 3.3 Dismiss key（收起鍵盤鍵 ▼）

- **Delta**：`KeyboardLayout.kt:83` `KeyDef("▼", isSpecial = true)` — 新增之收起鍵盤鍵，Row 5 最後一鍵。
- **消費點**：`ChewingInputMethodService.kt:147` `key.label == "▼" -> requestHideSelf(0)` — 直接呼叫 IMS 隱藏。
- **影響 T16**：KEYMAP.md §2 未描述 dismiss 鍵（§2 僅描述注音佈局），此為 Row 5 之特殊鍵，屬 UI 鍵盤操作輔助，不影響注音佈局凍結。
- **判定：PASS** — dismiss key 存在且功能正確。

### 3.4 Delta 總結表

| # | Delta | 檔:行（現況） | 對 Wave 3 影響 |
|---|-------|--------------|---------------|
| D-1 | KeyDef.code + isSpecial 新增；KeyMapping 降級 | KeyboardLayout.kt:14-19,29-80；KeyMapping.kt:13-27 | T16 表值 vs code 一致；T18 舊對照表已過時 |
| D-2 | Display table 40 鍵完整標註 code | KeyboardLayout.kt:28-84 | T16 凍結表值有 code 支撐 |
| D-3 | Dismiss key ▼ 新增 | KeyboardLayout.kt:83；ChewingInputMethodService.kt:147 | T16 不受影響（非注音鍵） |
| D-4 | CandidateView.kt BOM 未修 | CandidateView.kt（STATUS 報告 L99 標 ❌） | T17 已完成但 BOM 阻塞編譯（Wave 4 跟進） |

**Manual-QA 3：PASS**（4 項 delta 全數列舉並附 file:line 證據）。

---

## 4. 檢查項目 4：連結解析

本審計引用之相對路徑（於 repo 根）已逐一解析：

- `docs/KEYMAP.md` → 存在（163 行）
- `app/src/main/AndroidManifest.xml` → 存在（24 行）
- `app/src/main/res/xml/input_method.xml` → 存在（7 行）
- `app/build.gradle.kts` → 存在（含 applicationId L11）
- `app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt` → 存在（201 行）
- `app/src/main/java/com/example/androidkeyboard/input/KeyboardLayout.kt` → 存在（92 行）
- `app/src/main/java/com/example/androidkeyboard/input/KeyMapping.kt` → 存在（96 行）
- `app/src/main/java/com/example/androidkeyboard/input/KeyboardView.kt` → 存在（151 行）
- `app/src/main/java/com/example/androidkeyboard/ui/CandidateView.kt` → 存在（84 行）
- `docs/STATUS-2026-09-09.md` → 存在（244 行）
- `docs/CHECKS/t19-gboard-baseline.md` → 存在（71 行）
- `docs/CHECKS/wave3-t20.md` → 存在（91 行）
- `docs/CHECKS/wave2.md` → 存在（203 行）
- `docs/ROADMAP.md` → 存在（108 行）

**Manual-QA 4：PASS**（所有相對 .md/.kt/.xml/.kts 連結解析成功）。

---

## 5. 檢查項目 5：scope（僅 wave3.md）

- 本次審計之唯一寫入 = **覆寫 `docs/CHECKS/wave3.md`**（本文件）。
- **未**修改 reference tree（`D:\Temp\opencode\upstream-ro`）；**未** merge；**未** push；**未** 寫任何產品程式碼；**未** 觸碰 `D:\666\opencode` 其他子目錄。
- **未**修改其他 docs/ 下之既有檔（KEYMAP.md、STATUS-2026-09-09.md、wave3-t20.md 等均維持原狀）。
- 基線（§0）之 dirty 狀態於審計前即已存在，非本次造成。
- 本次覆寫之 wave3.md 先前存在（110 行，Tier MODERATE，僅涵蓋 T16-T18），本次以完整 Wave 3（T16-T19 + R2）取代。

**Manual-QA 5：PASS**。

---

## 6. 匯總與 Manual-QA 判定

| 檢查項目 | 判定 |
|----------|------|
| 1. 輸入存在性（11 項） | **PASS** |
| 2. 驗收閘（11 項：7 PASS + 4 DEFERRED） | **PASS** |
| 3. R2 code deltas（4 項） | **PASS** |
| 4. 連結解析 | **PASS** |
| 5. scope（僅 wave3.md） | **PASS** |

> **整體判定：PASS**（本 docs-only 審計）。
>
> 所有可驗證之閘（T16 SPEC-FREEZE/G1/G2/UNWIRED、T17 converter、T18 manifest）均 PASS。
> 所有 T19-derived 閘均正確記錄為 DEFERRED（無真機數據）。
> R2 code deltas 已全數列舉並附 file:line 證據。
> 無任何 gate FAIL。

---

## DoneClaim

- **產出**：`docs/CHECKS/wave3.md`（本文件，zh-TW）——Wave 3 docs-only 審計。
- **覆寫說明**：先前 `wave3.md`（110 行）僅涵蓋 T16-T18、Tier MODERATE；本次以完整 Wave 3（T16-T19 + R2 delta）取代，Tier LIGHT。
- **範圍合規**：僅變更 `docs/CHECKS/wave3.md`；未動 reference tree、未 merge、未 push、未寫產品碼、未觸碰其他 `D:\666\opencode` 目錄。基線以 `git -C` 實測並 §0 報告。
- **審計結果**：5 項檢查全部執行完畢；Manual-QA = 5×PASS。所有可驗證閘 PASS；T19-derived 閘正確 DEFERRED。整體判定 **PASS**。

**Need-fix 提示（非 gate-fail，建議後續跟進）**：
1. **applicationId placeholder**：`com.example.androidkeyboard` 需於 T23/T24 更換為正式 ID。
2. **KEYMAP.md §5「候選欄 44dp UNWIRED」與實際 code 衝突**：ChewingInputMethodService.kt:65 以 `44f * density` 程式碼建 candH，即 44dp 已在 IME 路徑落地（但非經 XML）。KEYMAP.md §5 U1 描述為「實作路徑未用 activity_input.xml」——**此描述正確**（XML 未被使用），但 **44dp 值本身已由 Kotlin 程式碼消費**。建議 §5 U1 註記更新為「44dp 已由 Kotlin 程式碼消費（ChewingInputMethodService.kt:65），但非經 XML activity_input.xml」。
3. **CandidateView.kt BOM**：STATUS 報告標記為 ❌ BOM，可能阻塞編譯（Wave 4 跟進）。
4. **KEYMAP.md §5 空格 35%p UNWIRED**：Row5 空格仍為 `widthPct=10f`（KeyboardLayout.kt:82），表值 35%p 無消費——此為真實 UNWIRED。
5. **G2 橫屏 −20~−40dp 偏差**：重大 GAP，正確 DEFERRED 至 F3 真機量測。
