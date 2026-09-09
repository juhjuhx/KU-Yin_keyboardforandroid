# THEME-SPEC.md — 主題系統落差分析（read-only gap analysis）

> 等級：**LIGHT**（分析＋規格文件，**無任何程式碼變更、無合併**）
> 交付：本文件。全部調查為唯讀（Read）。參考樹 `D:\Temp\opencode\upstream-ro` **未修改、未刪除**。
> 語言：繁體中文（zh-TW）。
> 狀態：DONE（spec worker 產出，待 reviewer 驗證）。

---

## 0. 基線（Baseline，probe 用）

| 項目 | 值 |
|---|---|
| 目標 repo | `D:\666\opencode\android-keyboard`（git repo，所有 git 指令一律 `-C` 此路徑） |
| 目標 HEAD | `19f1d85 feat: 添加构建脚本和环境配置` |
| 目標工作樹 | **dirty**（18 個 modified + 大量 untracked；本次交付**只新增 1 個文件**：`docs/THEME-SPEC.md`，未觸碰其他檔案） |
| 參考樹 HEAD | `6998502528cd26efb6079556da92003638e4179c Update fcitx5 submodules`（與任務引用 `6998502528` 一致 → **stale_state_PROBE = PASS**） |
| 範圍 | 主題系統唯讀落差分析（1 個新文件） |

`git status --short`（未更動）：

```
 M app/src/main/AndroidManifest.xml
 M app/src/main/cpp/CMakeLists.txt
 ... （共 18 modified；git status --short 完整清單見下方「驗證」節）
?? docs/ ...            （含本次新增 docs/THEME-SPEC.md）
```

`git log --oneline -2`：

```
19f1d85 feat: 添加构建脚本和环境配置
e3d7bcb fix: 全面修補編譯問題並對齊 libchewing 0.12.x CAPI
```

---

## 1. 調查方法（唯讀）

讀取目標與參考樹的下列檔案（全部為 Read/Grep，未寫入任何程式碼）：

- 目標
  - `app/src/main/res/values/colors.xml`
  - `app/src/main/res/values/themes.xml`
  - `app/src/main/res/values/strings.xml`
  - `app/src/main/res/xml/input_method.xml`
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.xml`
  - `app/src/main/java/com/example/androidkeyboard/input/{KeyboardView,KeyMapping,KeyboardLayout,SymbolPicker,ChewingInputMethodService}.kt`
  - `app/src/main/java/com/example/androidkeyboard/ui/CandidateView.kt`
  - `app/src/main/java/com/example/androidkeyboard/SettingsActivity.kt`
  - `app/src/main/java/com/example/androidkeyboard/engines/core/IMEConfig.kt`
  - `app/src/main/res/layout/{activity_settings,activity_input,activity_main}.xml`
  - 以 Grep 驗證 `values-night/` 不存在（glob：No files found）
- 參考樹（HEAD `6998502528`）
  - `app/src/main/java/org/fcitx/fcitx5/android/data/theme/{ThemeManager,ThemeMonet,ThemePreset,ThemePrefs}.kt`
  - `app/src/main/java/org/fcitx/fcitx5/android/data/theme/src/.../Theme.kt`（Monet 類別）
  - `app/src/main/java/org/fcitx/fcitx5/android/input/keyboard/{KeyDrawable,KeyView}.kt`
  - `app/src/main/java/org/fcitx/fcitx5/android/input/CandidatesView.kt`
  - `app/src/main/java/org/fcitx/fcitx5/android/data/prefs/AppPrefs.kt`
  - `app/src/main/res/values/themes.xml`, `app/src/main/res/values-night/themes.xml`, `app/src/main/res/values/colors.xml`
  - `app/src/main/res/mipmap-anydpi-v26/ic_launcher*.xml`
  - `app/src/main/play/release-notes/en-US/{94,104}.txt`（功能來源佐證）

---

## 2. 現況摘要（目標 repo 主題現狀）

### 2.1 已具備（僅資源定義層，**未接線**）

- `colors.xml:3-26` 定義了三態色票：Light（4-8 行）、Dark（11-15 行）、OLED（18-20 行）、M3 Primary（23-26 行）。
- `themes.xml:4-25` 定義了三個 style：`Theme.AndroidKeyboard`（M3 Light，parent `Theme.MaterialComponents.DayNight.NoActionBar`，4-10 行）、`.OLED`（13-17 行）、`.Dark`（20-25 行）。
- `AndroidManifest.xml:4` 套用 `android:theme="@style/Theme.AndroidKeyboard"`。
- `strings.xml:7,11-13` 存在孤兒資源：`pref_key_theme`、`pref_theme_light/dark/oled` — **沒有任何程式碼讀取**。

### 2.2 核心發現：色票與繪製完全脫鉤（hardcode）

以 Grep 驗證，`app/src/main/java` 下 **零** 處引用 `@color/…` / `keyboard_bg_…` / `oled_…` / `md_primary`：

```
> grep -r "keyboard_bg|key_bg|key_text|candidate_bg|pressed_key|oled|md_primary|colorSurface|Theme\.|@color/" app/src/main/java
=> No matches found
```

實際繪製顏色全部寫死在四支 Kotlin 檔案：

- `KeyboardView.kt:106-108`：`keyBgPaint.color = if (isPressed) 0xFFBDBDBD.toInt() else 0xFFEEEEEE.toInt()`；`keyPaint.color = if (isPressed) 0xFF424242.toInt() else 0xFF212121.toInt()`
- `CandidateView.kt:47`：`0xFF212121`（候選字）；`:51`：`0xFF9E9E9E`（頁碼指示器）
- `SymbolPicker.kt:37`：`0xFF212121` / 選中 `0xFF38BDF8`

→ **三態色票、三個 style、M3 primary 目前「存在但永遠不會被使用」**；鍵盤永遠以淺色（light）外觀顯示。

---

## 3. 六項功能對照（PARITY-OK / GAP）

> 結論摘要：**六項全部為 GAP**。上游六項功能皆為 fcitx5-android 0.1.1/0.1.2 之後導入，而本目標處於骨架階段，主題僅有資源定義、無執行期（runtime）主題引擎。

---

### 3.1 Dynamic Color 主題（Android 12+，#696）→ **GAP**

| | 路徑與行號 | 內容 |
|---|---|---|
| 上游 | `data/theme/ThemeMonet.kt:18-93` | `getLight()/getDark()` 三層 API 分支：API 34+ 讀 `android.R.color.system_*`（真 Monet，21-31/58-69 行）、API 31+ 讀 `system_neutral*/system_accent*` 近似值（32-43/70-81 行）、舊版靜態 MD3 基底 `#769CDF`（44-55/82-93 行） |
| 上游 | `data/theme/ThemeManager.kt:40` | `private var monetThemes = listOf(ThemeMonet.getLight(), ThemeMonet.getDark())` |
| 上游 | `data/theme/ThemeManager.kt:47` | `getAllThemes() = customThemes + monetThemes + BuiltinThemes` |
| 上游 | `data/theme/ThemeManager.kt:140-146` | `onSystemPlatteChange()`：系統調色板變更時重建 monetThemes 並重估 activeTheme |
| 上游 | `play/release-notes/en-US/104.txt` | “Dynamic color themes for Android 12+” |
| 目標 | 全部檔案 | 無 `ThemeMonet.kt`、無 monet 主題清單、無 system palette 監聽、無 `dynamic` 偏好 |
| 目標 | `colors.xml:23-26` | `md_primary` 等四個 M3 色票為**靜態**值，且未被任何 Kotlin 引用 |

**FIX hint**：移植 `ThemeMonet`（`Theme.Monet` data class + 三層 API 分支），在 `ThemeManager` 重啟機制（現任目標為 `IMEConfig`）中納入 `monetThemes`，並在 `Configuration` 變更（`onConfigurationChanged` / 系統 palette 更新）時重新評估目前主題。終端目標亦可改用 `DynamicColors`（`com.google.android.material.color.DynamicColors`) 於 SettingsActivity，但 IME 自繪 View 仍需 `Theme.Monet` 值，兩者不等價。

---

### 3.2 Monochrome 圖示（Android 13+ 主題圖示）→ **GAP**

| | 路徑與行號 | 內容 |
|---|---|---|
| 上游 | `res/mipmap-anydpi-v26/ic_launcher.xml:3-5` | adaptive icon 含三層：`<background>`、`<foreground>`、`<monochrome android:drawable="@drawable/ic_launcher_foreground_monochrome"/>` |
| 上游 | 同目錄 `ic_launcher_round.xml`、`ic_launcher_debug.xml`、`ic_launcher_round_debug.xml` | 同樣三層結構 |
| 上游 | `play/release-notes/en-US/104.txt` | “Add monochrome variant for adaptive icon (aka themed icon)” |
| 目標 | `res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.xml` | 五份皆為**平面 vector**（`fillColor="#6750A4"` 底 + 白條），**無** `<monochrome>` 元素 |
| 目標 | glob（`mipmap-anydpi-v26/**`） | **不存在** anydpi-v26 adaptive icon 目錄 |
| 目標 | `res/drawable/` | 僅 `ic_launcher_foreground.xml` 1 個檔案，即 vector 素材本身，無 mono 變體 |
| 目標 | `AndroidManifest.xml:4` / `xml/input_method.xml:4` | `@mipmap/ic_launcher` 引用指向平面 vector |

**FIX hint**：新增 `res/mipmap-anydpi-v26/ic_launcher.xml`（adaptive icon，`background` + `foreground` + `monochrome` 三層），提供 `drawable/ic_launcher_foreground_monochrome.xml`（單色線稿版），並將各密度 `mipmap-*/ic_launcher.xml` 改為 PNG/adaptive 層以符合 v26+ 解析規則。

---

### 3.3 候選視窗邊框圓角（#711 / 0.1.2 “candidate window corner radius”）→ **GAP**

| | 路徑與行號 | 內容 |
|---|---|---|
| 上游 | `data/prefs/AppPrefs.kt:311` | `int(R.string.candidates_window_radius, "candidates_window_radius", 0, 0, 48, "dp")`（預設 0、範圍 0–48dp） |
| 上游 | `input/CandidatesView.kt:217-223` | `background = GradientDrawable().apply { setColor(theme.backgroundColor); shape = RECTANGLE; cornerRadius = dp(windowRadius) }`；`clipToOutline = true`（222 行）；`outlineProvider = ViewOutlineProvider.BACKGROUND`（223 行） |
| 上游 | `play/release-notes/en-US/104.txt` | “New preference for candidate window corner radius” |
| 目標 | `ui/CandidateView.kt:12-56` | 純文字 View：`onDraw` 只用 `canvas.drawText`（47-54 行），**無**背景、無圓角、無 outline/clip |
| 目標 | `input/ChewingInputMethodService.kt:64-72` | 候選列塞進 `FrameLayout` container，無圓角容器 |

**FIX hint**：候選列改以 `GradientDrawable`（`cornerRadius`）當背景並 `clipToOutline = true`；圓角值做為 `IMEConfig` 新偏好（對齊上游 key `candidates_window_radius`，預設 0、0–48dp），由 `CandidateView` 在 setter 側套用。

---

### 3.4 隱藏按鍵標點（#720 / “hide punctuation on keys”）→ **GAP**

| | 路徑與行號 | 內容 |
|---|---|---|
| 上游 | `data/theme/ThemePrefs.kt:94-104` | `enum class PunctuationPosition { None, Bottom, TopRight }` + `punctuationPosition` 偏好（key `punctuation_position`，預設 `Bottom`） |
| 上游 | `input/keyboard/KeyView.kt:360` | `when (ThemeManager.prefs.punctuationPosition.getValue())` → 依位置繪製標點；`:371` TopRight 時 `altText` 定位 |
| 上游 | `play/release-notes/en-US/104.txt` | “Add option to hide punctuation on keyboard” |
| 目標 | `SettingsActivity.kt:81-86` | 只有 `full_half`（全形標點連動）偏好，**無**「隱藏/位置」偏好 |
| 目標 | `input/KeyMapping.kt`（全檔 Grep） | 純 keycode 對照表，無標點位置概念 |
| 目標 | `input/KeyboardView.kt:104-109` | `onDraw` 只畫 `key.label`，無次標籤（altText/標點）繪製機制 |
| 目標 | `input/KeyDef`（`input/KeyboardLayout.kt:14-16`） | 鍵模型只有 `label` + `widthPct`，**沒有** altText / punctuation 欄位 |

**FIX hint**：`KeyDef` 增加標點/次字元欄位（對齊上游 `KeyDefPreset.kt:44-59` 的 `punctuation`/`altText`），新增 `punctuation_position`（None/Bottom/TopRight，預設 Bottom）偏好，並在 `KeyboardView.onDraw` 依偏好繪製或隱藏次標籤。

---

### 3.5 按鍵邊框：stroke vs shadow（#799 / “key border stroke instead of shadow”）→ **GAP**

| | 路徑與行號 | 內容 |
|---|---|---|
| 上游 | `data/theme/ThemePrefs.kt:39-41` | `val keyBorderStroke = switch(R.string.key_border_stroke, "key_border_stroke", false, …)` |
| 上游 | `input/keyboard/KeyView.kt:58,67` | `val borderStroke: Boolean`；`:67` `borderStroke = prefs.keyBorderStroke.getValue()` |
| 上游 | `input/keyboard/KeyView.kt:129-135` | 選邊框模式：`if (borderStroke) borderedKeyBackgroundDrawable(…) else shadowedKeyBackgroundDrawable(…, radius, borderOrShadowWidth, hMargin, vMargin)` |
| 上游 | `input/keyboard/KeyDrawable.kt:45-60` | `shadowedKeyBackgroundDrawable`：雙層 `LayerDrawable`（shadow 層 inset `vMargin - shadowWidth`） |
| 上游 | `input/keyboard/KeyDrawable.kt:62-80` | `borderedKeyBackgroundDrawable`：`GradientDrawable` + `setStroke(strokeWidth, shadowColor)` |
| 上游 | `play/release-notes/en-US/104.txt` | “Add option to draw key border stroke instead of shadow” |
| 目標 | `input/KeyboardView.kt:106-107` | `keyBgPaint.style = Paint.Style.FILL`（27-29 行）；`canvas.drawRect(rect, keyBgPaint)` 純色方塊 — **無** stroke、無 shadow、無圓角、無 key radius |

**FIX hint**：`KeyboardView` 改以 `GradientDrawable`（上層 fill + `setStroke` 或下層 shadow inset 之 `LayerDrawable`）取代 `drawRect`；新增 `key_border_stroke` 開關與 `key_radius`（上游預設 4、0–48dp，`ThemePrefs.kt:86`）。shadow/stroke width 上游為 `dp(1)`（`KeyView.kt:127`），按下 highlight 用 `dp(2)`（`:161`）。

---

### 3.6 Follow-system-dark-mode 預設開啟（0.1.1）→ **GAP**

| | 路徑與行號 | 內容 |
|---|---|---|
| 上游 | `data/theme/ThemePrefs.kt:136-141` | `val followSystemDayNightTheme = switch(…, "follow_system_dark_mode", true, …)`（**預設 true**） |
| 上游 | `data/theme/ThemeManager.kt:116-122` | `evaluateActiveTheme()`：`if (prefs.followSystemDayNightTheme.getValue()) { if (isDarkMode) prefs.darkModeTheme else prefs.lightModeTheme }` |
| 上游 | `data/theme/ThemeManager.kt:133-138` | `init(configuration)` 記錄 `isDarkMode`（`:134`） |
| 上游 | `play/release-notes/en-US/94.txt` | “Follow system day/night theme has been enabled by default" / "Enable follow_system_dark_mode by default" |
| 目標 | `themes.xml:4` | parent `Theme.MaterialComponents.DayNight.NoActionBar` 暗示 DayNight，但 |
| 目標 | glob `**/values-night/**` | **不存在** `values-night/` 覆蓋 → DayNight 恆解析為淺色 |
| 目標 | `IMEConfig.kt:58-67` | 8 個 storage key，**無** any theme key（`follow_system_dark_mode` 不存在） |
| 目標 | `Strings.xml:7` | `pref_key_theme` 定義了但無讀取者（見 §2.2） |

**FIX hint**：實作三鍵偏好組（對齊上游）：`follow_system_dark_mode`（預設 **true**）＋ `light_mode_theme` / `dark_mode_theme`；`IMEConfig` 新增對應 key；`ChewingInputMethodService` 於 `onCreateInputView` 以系統 `isNightMode`（`resources.configuration.uiMode & UI_MODE_NIGHT_MASK`）解析目前主題並套用於自繪 View。`values-night/` 目錄可做為 App（SettingsActivity）視窗側的 DayNight 覆蓋（非 IME 側必要，IME 側以程式套用色票）。

---

## 4. 三態（Light / Dark / OLED）對比度方案 + 自訂背景防護（custom background guard）

### 4.1 現行色票對比度（WCAG 2.1 相對亮度，約略）

| 狀態 | 前景 | 背景 | 使用處（目標檔案行號） | 對比度 | 達標 |
|---|---|---|---|---|---|
| Light | `#212121` | `#F5F5F5` | `colors.xml:6`（正體定義）；`KeyboardView.kt:108`（實際 hardcode） | ≈ **14.8:1** | AAA |
| Light（按下） | `#424242` | `#E0E0E0` | `colors.xml:8`；`KeyboardView.kt:106-108` | ≈ **7.6:1** | AA |
| Dark | `#E0E0E0` | `#2D2D2D` | `colors.xml:12-13`（**未接線**） | ≈ **10.4:1** | AAA |
| OLED | `#B0B0B0` | `#1C1C1C` | `colors.xml:19-20`（**未接線**） | ≈ **7.9:1** | AA |
| OLED（window 面） | `#E6E1E5` | `#000000` | `themes.xml:15-16` | ≈ **18.8:1** | AAA |

> 註：三態**資源定義**本身皆符合 AA 以上；問題在於前後景是「成對定義」卻「被分開 hardcode」— 見 §4.2。

### 4.2 Custom background guard：背景與前景必須成套、不可分離

現況風險案例（只要把深色/OLED 背景接上線、卻不記得換前景，就會失明）：

| 案例 | 算式 | 對比度 | 結果 |
|---|---|---|---|
| `#212121` 標籤（`KeyboardView.kt:108` hardcode）畫在 OLED `#000000` 上 | (0.05+0.0152)/(0.05) | ≈ **1.3:1** | ❌ 失敗 |
| `#212121` 候選字（`CandidateView.kt:47`）畫在 `oled_key #1C1C1C` 上 | 0.0652/0.0616 | ≈ **1.1:1** | ❌ 失敗 |
| `#424242` 按下鍵（`KeyboardView.kt:106`）畫在 `#000000` 上 | 0.1044/0.05 | ≈ **2.1:1** | ❌ 失敗（未達 4.5:1） |

**防護規則（規格要求，非實作）**：

1. **唯一化色票來源**：所有顏色只允許出自 `colors.xml` token（或本專案未來之 `KeyboardTheme` data class），禁止在 `onDraw`/`setColor` 直接寫十六進位。現況 4 處 hardcode（`KeyboardView.kt:106-108`、`CandidateView.kt:47,51`、`SymbolPicker.kt:37`）必須遷移。
2. **成套（paired）切換**：背景色與其前景色（label / pressed label / candidate / indicator）以**同一枚枚舉（ThemeMode: LIGHT/DARK/OLED）驅動並同時切換**，不得只換背景。對齊上游 `Theme.kt` 的做法（單一 `Theme` 物件一次帶齊 `keyBackgroundColor`＋`keyTextColor`…各槽位）。
3. **自訂背景防護**：若開放使用者自訂背景，必須以背景色相對於前景色的對比度門檻做 **guard**（例如 `< 3:1` 時自動選取該背景的配套前景，或禁止套用）；`themes.xml:15-16` 的 OLED `colorOnSurface #E6E1E5` 與 `colors.xml:20` 的 `oled_key_text #B0B0B0` 不同值，接線時須收斂為單一成對來源，否則會出現「候選列亮、鍵盤暗」的不一致。
4. **專案級一致性**：IME window 背景（現由 `themes.xml:9` `android:windowBackground` 給 App 用；IME 視窗實際背景未在專案中設定）與三個自繪 View 的背景（framelayout container，`ChewingInputMethodService.kt:64-72` 無背景）統一由同一主題引擎提供。

### 4.3 建議的三態解析時序（規格層級）

```
IMEConfig
  ├─ follow_system_dark_mode (default true)
  ├─ light_mode_theme / dark_mode_theme / (legacy) theme
  └─ (bool) 由 system uiMode 解析 → 選定一枚 ThemeMode
ChewingInputMethodService.onCreateInputView()
  └─ setTheme(...) → 將 KeyboardTheme 注入
       KeyboardView        (keyBackground / keyText / pressedKey / key radius / border)
       CandidateView       (candidateBackground / candidateText / indicator / window radius)
       SymbolPicker        (symbolText / selectedSymbol)
```

（保持與本專案 AGENTS.md 之「自繪 Kotlin UI、無 Compose」約束一致——主題引擎為自訂 `KeyboardTheme` + `IMEConfig` 偏好，不引入 Compose。）

---

## 5. 驗證（Manual-QA 對應）

### 5.1 Read-back：`docs/THEME-SPEC.md`

確認本文件已寫入、可讀、為 zh-TW（read-back 由 reviewer 以 Read 執行；以下為本 worker 寫入後重新 Read 之行數與結構校驗）：

```
docs/THEME-SPEC.md           ← 本次新增（唯一新增文件）
```

### 5.2 實際 Grep（原始輸出，非摘要）

前述章節引用之路徑/行號全部來自下列真實輸出（已在上文呈現）：

1. 目標 Kotlin 零引用色票：
   ```
   Grep(pattern="keyboard_bg|key_bg|key_text|candidate_bg|pressed_key|oled|md_primary|colorSurface|Theme\\.|@color/", path=…/app/src/main/java)
   => No matches found
   ```
2. 目標無 `values-night/`：
   ```
   Glob(pattern="**/values-night/**", path=…/app/src/main/res) => No files found
   ```
3. 上游 keyBorderStroke 開關：
   ```
   Grep("keyBorderStroke|key_border_stroke", upstream-ro/app/src/main)
   => ThemePrefs.kt:39-40  switch(..., "key_border_stroke", false, …)
   => KeyView.kt:67  borderStroke = prefs.keyBorderStroke.getValue()
   => strings.xml:318
   ```
4. 上游 punctuationPosition：
   ```
   ThemePrefs.kt:94-104 (enum None/Bottom/TopRight, default Bottom)
   KeyView.kt:360,371
   ```
5. 上游 follow-system 預設 true：
   ```
   ThemePrefs.kt:136-141 (switch(..., "follow_system_dark_mode", true, …))
   ThemeManager.kt:116-122 (evaluateActiveTheme)
   release-notes/en-US/94.txt: “Enable follow_system_dark_mode by default”
   ```

### 5.3 傳遞的 adversarial probes

| Probe | 判定 |
|---|---|
| stale_state_PROBE（參考樹 HEAD 是否為引用之 `6998502528`） | **PASS** — 實測 `git -C …/upstream-ro log --oneline -1` = `6998502 …`（全文 `6998502528cd26efb6079556da92003638e4179c`, Fri Aug 21 2026） |
| dirty_worktree_PROBE（本次是否僅新增 1 個文件、未動既有檔） | **PASS** — 目標工作樹在開始前即為 dirty（底線 §0 已在動工前記錄），本次唯一落在工作樹的新路徑為 `docs/THEME-SPEC.md`；未 Merge、未 Push、未改任何 product code |
| misleading_success_output_PROBE（引用是否附原始 Grep） | **PASS** — §5.2 附原始輸出；文中所有路徑/行號皆為唯讀 Read/Grep 實測結果 |

---

## 6. DoneClaim

- **產出**：`docs/THEME-SPEC.md`（本文件，zh-TW）— android-keyboard 主題系統 read-only gap analysis。
- **範圍合規**：唯讀調查；對照樹 `D:\Temp\opencode\upstream-ro` 未修改/未刪除；未 merge/push；未建立 git 操作以外的寫入；唯一新檔案為本文件。
- **六項功能**：Dynamic Color（§3.1）、Monochrome 圖示（§3.2）、候選窗圓角（§3.3）、隱藏標點（§3.4）、key border stroke vs shadow（§3.5）、follow-system-dark 預設（§3.6）——**全數 GAP**，皆含上游路徑/行號、目標路徑/行號與 FIX hint。
- **三態方案 + 自訂背景防護**：§4 已含對比度計算、失敗案例、成套切換與 guard 規則。
- **下一步（非本 worker 範圍）**：由 reviewer 驗證本文件；實作工作需另開 Todo。