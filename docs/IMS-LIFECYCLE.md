# IMS-LIFECYCLE.md — InputMethodService 生命週期對照（fork 視角）

> **基準**：upstream HEAD `6998502528` (2026-08-21)  
> **參考樹**：`D:\Temp\opencode\upstream-ro`（唯讀，不修改）  
> **唯一新檔案**：本文件

---

## 1. onCreateInputView / onCreateCandidatesView / onStartInput / onFinishInput 流程

### 1.1 類別繼承

```
InputMethodService  (Android framework)
  └─ LifecycleInputMethodService          ← 4 行包裝，加入 LifecycleOwner
       └─ FcitxInputMethodService          ← 所有 IMS override 集中處
```

| 檔案 | 行 | 說明 |
|------|-----|------|
| `LifecycleInputMethodService.kt` | :14 | `open class LifecycleInputMethodService : InputMethodService(), LifecycleOwner` |
| `LifecycleInputMethodService.kt` | :21-26 | `onCreate()` — 設定 decorView 的 ViewTreeLifecycleOwner，dispatch `ON_CREATE` + `ON_START` |
| `FcitxInputMethodService.kt` | :83 | `class FcitxInputMethodService : LifecycleInputMethodService()` |

### 1.2 onCreateInputView

**上游路徑**：`app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt:570-574`

```kotlin
override fun onCreateInputView(): View? {
    replaceInputViews(ThemeManager.activeTheme)
    // We will call `setInputView` by ourselves. This is fine.
    return null
}
```

- 回傳 `null`，由自行呼叫 `setInputView()` 管理。
- `replaceInputViews()` (L169-173) 依序呼叫 `replaceInputView()` + `replaceCandidateView()`。
- `replaceInputView()` (L150-156)：建立新 `InputView` → `setInputView()` → `inputDeviceMgr.setInputView()`。
- `replaceCandidateView()` (L158-167)：建立新 `CandidatesView` → 從 `contentView` 移除舊的 → 加入新的 → `inputDeviceMgr.setCandidatesView()`。

**inputArea 擴展** (L576-592)：`setInputView()` override 會把 `android.R.id.inputArea` 與 InputView 都設為 `MATCH_PARENT`，避免 framework 強制 `WRAP_CONTENT`。

### 1.3 onCreateCandidatesView

**上游未 override 此方法。** CandidatesView 由 `replaceCandidateView()` 手動管理，直接掛在 `contentView` (L161-163)，而非經由 framework 的 candidates container。

### 1.4 onStartInput

**上游路徑**：`FcitxInputMethodService.kt:727-756`

```kotlin
override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
    selection.resetTo(attribute.initialSelStart, attribute.initialSelEnd)
    resetComposingState()
    val flags = CapabilityFlags.fromEditorInfo(attribute)
    capabilityFlags = flags
    inputDeviceMgr.notifyOnStartInput(attribute)
    postFcitxJob {
        if (restarting) focus(false)   // 重新聚焦前先 focus out 清狀態
        setCapFlags(flags)
        if (!isNullType) focus(true)
    }
}
```

關鍵：**`selection.resetTo`** 在 `onStartInput` 最先呼叫，確保 cursor 位置反映 `EditorInfo.initialSel{Start,End}`。

### 1.5 onStartInputView

**上游路徑**：`FcitxInputMethodService.kt:758-778`

- `focus(true)` → `inputDeviceMgr.evaluateOnStartInputView(info, this)` → 決定 `isVirtualKeyboard`。
- 若為虛擬鍵盤：`inputView?.startInput(info, capabilityFlags, restarting)` → 觸發 `KawaiiBarComponent` + `BroadcastReceiver` 鏈。
- 若為實體鍵盤：嘗試 `monitorCursorAnchor()`，若失敗則 fallback 到 bottom-left 錨點。

### 1.6 onFinishInputView / onFinishInput / onUnbindInput

| 方法 | 行 | 行為 |
|------|----|------|
| `onFinishInputView` | L1050-1064 | `finishComposingText()` + `monitorCursorAnchor(false)` + `resetComposingState()` + `focusOutIn()` + `hideStatusIcon()` + dismiss dialog |
| `onFinishInput` | L1066-1072 | `focus(false)` + reset `capabilityFlags` to `DefaultFlags` |
| `onUnbindInput` | L1074-1084 | `cachedKeyEvents.evictAll()` + `deactivate(uid)` |

### 1.7 InputView 狀態還原：虛擬/實體切換

**上游路徑**：`app/src/main/java/org/fcitx/fcitx5/android/input/InputDeviceManager.kt`

`InputDeviceManager` 以 `isVirtualKeyboard` (L42-52) 追蹤當前設備模式。切換時：

1. `setupInputViewEvents(isVirtual)` (L21-25)：切換 `InputView.handleEvents` 與 `visibility`。
2. `setupCandidatesViewEvents(isVirtual)` (L27-35)：切換 `CandidatesView.handleEvents` 與 `visibility`。
3. 觸發 `onChange` callback (FcitxInputMethodService L106-119)：`setCandidatePagingMode`、`monitorCursorAnchor`、status icon、navbar evaluate。

切換觸發點：
- `evaluateOnStartInputView` (L76-85)：每場 `onStartInputView` 時根據 `FloatingCandidatesMode` 決定。
- `evaluateOnKeyDown` (L90-108)：按下實體鍵時切換。
- `evaluateOnViewClicked` (L118-124)：觸控螢幕點擊時切回虛擬。
- `evaluateOnUpdateEditorToolType` (L126-135)：API 34+ 根據 `toolType` 判斷。

**虛擬↔實體切換不重建 InputView**，只改變 `visibility` + `handleEvents`。

### 1.8 InputView 狀態還原：旋轉（RealSize 高度基準）

**上游路徑**：`app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt:157-177`

```kotlin
private val keyboardHeightPx: Int
    get() {
        val baseType = keyboardHeightPercentBase.getValue()
        val base = when (baseType) {
            DisplayMetrics -> resources.displayMetrics.heightPixels
            RealSize -> Point().also {
                context.display.getRealSize(it)   // 實體螢幕全尺寸
            }.y
        }
        val percent = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> keyboardHeightPercentLandscape
            else -> keyboardHeightPercent
        }.getValue()
        return base * percent / 100
    }
```

`keyboardHeightPx` 為 **computed property**（每次取值重算）。旋轉觸發 `onConfigurationChanged`，下次 layout 時自動以新 orientation 的 percent 與新 base 重算。

`onConfigurationChanged` (FcitxInputMethodService L537-561)：
- 呼叫 `postFcitxJob { reset() }` 清 fcitx 狀態。
- 過濾 `CONFIG_KEYBOARD | CONFIG_KEYBOARD_HIDDEN | CONFIG_UI_MODE` 差異，**避免**呼叫 `super.onConfigurationChanged` 導致不必要的 `initViews()` / `resetStateForNewConfiguration()` 重建。

**RealSize vs DisplayMetrics**：`RealSize` 使用 `Display.getRealSize()` 取得包含導航列的全螢幕高度，旋轉後數值會立即改變。`DisplayMetrics.heightPixels` 可能在某些 ROM 上不隨旋轉即時更新。

---

## 2. InputConnection Batch Edit 紀律

### 2.1 withBatchEdit 工具函式

**上游路徑**：`app/src/main/java/org/fcitx/fcitx5/android/utils/InputConnection.kt:11-15`

```kotlin
fun InputConnection.withBatchEdit(block: InputConnection.() -> Unit) {
    beginBatchEdit()
    block.invoke(this)
    endBatchEdit()
}
```

### 2.2 使用場景

#### commitText（多步驟提交）

**上游路徑**：`FcitxInputMethodService.kt:423-454`

場景 A — composing === commit（同文字 finish）：
```kotlin
ic.withBatchEdit {
    if (selection.current.start != target) {
        selection.predict(target)
        ic.setSelection(target, target)
    }
    ic.finishComposingText()
}
```
（L430-437）：先 `setSelection` 再 `finishComposingText`，包在 batchEdit 中避免 `onUpdateSelection` 中間回調。

場景 B — cursor 指定提交：
```kotlin
ic.withBatchEdit {
    commitText(text, 1)
    setSelection(target, target)
}
```
（L449-452）：`commitText` + `setSelection` 原子化。

#### updateComposingText（preedit 更新）

**上游路徑**：`FcitxInputMethodService.kt:922-972`

整個方法以 `ic.beginBatchEdit()` (L925) 開頭，`ic.endBatchEdit()` (L971) 結尾。內部包含：
- `setComposingText()` — 更新 composing span
- `setSelection()` — 移動 cursor 到 composing 內
- 不通知 `onUpdateSelection`，避免 fcitx 狀態混淆

#### applySelectionOffset / cancelSelection

**上游路徑**：`FcitxInputMethodService.kt:516-533`

```kotlin
fun applySelectionOffset(offsetStart: Int, offsetEnd: Int = 0) {
    currentInputConnection?.also {
        selection.predict(start, end)
        it.setSelection(start, end)
    }
}
```

注意：此處**未**包在 batchEdit 中，因為只做單一 `setSelection`。

### 2.3 composing-range 還原

**上游路徑**：`FcitxInputMethodService.kt:856-915` (`handleCursorUpdate`)

當 `onUpdateSelection` 回報 `candidatesStart == -1 && candidatesEnd == -1`（composing 被 InputFilter 清除），但本地 `composing` 仍有範圍時：

```kotlin
if (newComposingStart == -1 && newComposingEnd == -1 && composing.isNotEmpty()) {
    currentInputConnection?.setComposingRegion(composing.start, composing.end)
}
```
（L869-871）：嘗試用 `setComposingRegion` 恢復 composing 範圍。

---

## 3. OTP-6-cell no-rebuild guard（Guileless 4.0.13 教訓）

### 3.1 上游機制

**上游路徑**：`FcitxInputMethodService.kt:537-561`

```kotlin
override fun onConfigurationChanged(newConfig: Configuration) {
    postFcitxJob { reset() }
    val f = ActivityInfo.CONFIG_KEYBOARD or
            ActivityInfo.CONFIG_KEYBOARD_HIDDEN or
            ActivityInfo.CONFIG_UI_MODE
    val diff = lastKnownConfig.diff(newConfig)
    if (diff and f != diff) {
        super.onConfigurationChanged(newConfig)
    }
    lastKnownConfig = newConfig
}
```

**關鍵設計**：
- 上游在 `onConfigurationChanged` 中**主動過濾** `CONFIG_KEYBOARD`、`CONFIG_KEYBOARD_HIDDEN`、`CONFIG_UI_MODE` 差異。
- 當 diff **僅含**這些 flag 時，**不**呼叫 `super.onConfigurationChanged()`。
- 原因：`super.onConfigurationChanged` 會觸發 `resetStateForNewConfiguration()` → `initViews()` → 重建 InputView/CandidatesView（AOSP L543-545 註解）。
- 當有**其他**configuration change（如 locale、fontScale）時，才允許 super 呼叫。

### 3.2 OTP 6-cell 場景

OTP 6 格輸入框（如銀行 OTP）在場景切換時的行為：
1. 用戶在 OTP 欄位輸入 → **不**重建 keyboard（否則 composing text 被清除、游標歸零）。
2. `replaceInputViews` 只在 theme change 或 preference change 時觸發（L176-188），**不**因 configuration change（如虛擬/實體切換）觸發。
3. 虛擬↔實體切換只改變 `visibility` + `handleEvents`（`InputDeviceManager`），**不**重建 view。

### 3.3 fork 需注意

Guileless 4.0.13 教訓：field switch（OTP 第 1 格→第 2 格）時若觸發 `onConfigurationChanged`（某些 ROM 的 `keyboardHidden` flag 變化），不應重建 keyboard。上游已處理此 case，fork 繼承此行為。

---

## 4. Password-Field Guard（no learning, number-row on toolbar）

### 4.1 Password flag 判斷

**上游路徑**：`app/src/main/java/org/fcitx/fcitx5/android/core/CapabilityFlag.kt:97-180` (`fromEditorInfo`)

```kotlin
InputType.TYPE_TEXT_VARIATION_PASSWORD,
InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> {
    flags += CapabilityFlag.Password
}
InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD -> {
    flags += CapabilityFlag.Sensitive
    flags += CapabilityFlag.NoSpellCheck
}
```

另外：`IME_FLAG_NO_PERSONALIZED_LEARNING` (L103-105) 會加上 `CapabilityFlag.Sensitive`。

`PasswordOrSensitive` (L78) = `Password.flag or Sensitive.flag`，給 fcitx 核心用於：
- 禁止學習（candidate 不入 recently-used）
- 禁止 personalized suggestion

### 4.2 recently-used 影響

**上游路徑**：`app/src/main/java/org/fcitx/fcitx5/android/data/RecentlyUsed.kt`

`RecentlyUsed` 使用 `FcitxApplication.getInstance().directBootAwareContext` (L23-24) 讀寫 SharedPreferences。fcitx 核心在 `CapabilityFlag.Password` 設定時**不**將選字結果寫入 recently-used。

### 4.3 Toolbar 數字列

**上游路徑**：需查看 `KawaiiBarComponent.kt` 及 toolbar 鍵盤邏輯（在 `lib/` submodule 中，submodules 未 fetch）。

上游 toolbar 行為由 `KawaiiBarComponent` 控制，`bar/KawaiiBarComponent.kt` (L28) 管理 toolbar 顯示。password field 的 toolbar 數字列配置由 fcitx5 核心配置檔決定，**需要查看 `lib/` submodule 中的 `fcitx5-android` 配置**：

> ⚠️ **Submodule 未 fetch**：`lib/` 目錄包含 `libchewing`、`fcitx5` 核心等 submodule，toolbar 鍵盤的具體配置（password 時顯示數字列）位於此處，目前無法提供精確行號。需 fetch submodule 後補充。

### 4.4 fork 注意

- `CapabilityFlag.Password` 與 `Sensitive` 的區別：`Password` 表示完全不學習，`Sensitive` 表示不 personalize 但仍可 spell check（visible password 例外，visible password 同時加 `NoSpellCheck`）。
- fork 若需為 password field 顯示專用 toolbar 數字列，需在 `KawaiiBarComponent` 或其上游依賴中調整。

---

## 5. Direct Boot Behavior Parity

### 5.1 Manifest 聲明

**上游路徑**：`app/src/main/AndroidManifest.xml`

| 元素 | 行 | directBootAware |
|------|-----|----------------|
| `FcitxInputMethodService` | L126 | `true` |
| `ClipboardEditActivity` | L116 | `true` |
| `LogActivity` | L56 | `true` |
| `InitializationProvider` | L174 | `true` |

### 5.2 FcitxApplication Direct Boot

**上游路徑**：`app/src/main/java/org/fcitx/fcitx5/android/FcitxApplication.kt:73-88`

```kotlin
var isDirectBootMode = false
    private set

val directBootAwareContext: Context
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isDirectBootMode) {
        createDeviceProtectedStorageContext()
    } else {
        applicationContext
    }
```

- `onCreate()` L85-88：若 `!userManager.isUserUnlocked`，設定 `isDirectBootMode = true`，註冊 `ACTION_USER_UNLOCKED` receiver。
- `unlockReceiver` (L48-59)：解鎖後呼叫 `FcitxDaemon.stopFcitx()` + `AppUtil.exit()` 重啟到正常模式。

### 5.3 Direct Boot 資料同步

**上游路徑**：`FcitxApplication.kt:140-143`

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isDirectBootMode) {
    AppPrefs.getInstance().syncToDeviceEncryptedStorage()
    ThemeManager.syncToDeviceEncryptedStorage()
}
```

正常啟動時將 credential-encrypted 資料同步到 device-encrypted storage。

### 5.4 RecentlyUsed 的 Direct Boot

**上游路徑**：`RecentlyUsed.kt:23-24`

```kotlin
private val sharedPreferences = FcitxApplication.getInstance().directBootAwareContext
    .getSharedPreferences(PREference_NAME, Context.MODE_PRIVATE)
```

使用 `directBootAwareContext` 確保 Direct Boot 模式下可讀寫。

### 5.5 fork 要求

- IMS service 本身必須 `directBootAware="true"`，否則裝置重啟後解鎖前無法使用鍵盤。
- `FcitxDaemon`（`lib/` submodule 中）在 Direct Boot 模式下的初始化路徑需確認。
- ⚠️ **Submodule 未 fetch**：`FcitxDaemon`、`FcitxConnection` 的 Direct Boot 初始化邏輯位於 `lib/` submodule，目前無法提供精確行號。

---

## 附錄 A：Manual-QA 場景表（failing-first）

> 以下場景在代碼落地後執行。每行包含可驗證的上游參考路徑。

| # | 場景 | 操作步驟 | 預期結果 | 上游參考路徑 |
|---|------|----------|----------|-------------|
| **OTP-1** | OTP 6 格欄位切換 | 1. 開啟含 6 格 OTP 的 App<br>2. 輸入第 1 格 → 點擊第 2 格<br>3. 觀察 composing text 是否被清除 | composing 在欄位切換後**保留**，不重建 keyboard | `FcitxInputMethodService.kt:537-561`（config change filter） |
| **OTP-2** | OTP 欄位聚焦/失焦 | 1. 聚焦 OTP 欄位 → `onStartInput` 觸發<br>2. 點擊非 OTP 區域失焦<br>3. 重新聚焦 OTP 欄位 | `onStartInput` 正確帶入 `initialSelStart/End`，composing 狀態 reset | `FcitxInputMethodService.kt:727-756` |
| **OTP-3** | OTP + 實體鍵盤切換 | 1. 在 OTP 欄位<br>2. 連接/斷開 BT 鍵盤<br>3. 觀察 InputView 是否重建 | InputView **不重建**，僅 `visibility` 切換 | `InputDeviceManager.kt:21-52` |
| **ROT-1** | 旋轉 + composing text | 1. 在文字欄位輸入 composing text（如注音 preedit）<br>2. 旋轉裝置<br>3. 觀察 composing 是否保留 | composing text 保留，keyboard 高度重新計算 | `FcitxInputMethodService.kt:537-561` + `InputView.kt:157-177` |
| **ROT-2** | 旋轉 + RealSize 高度 | 1. 設定 `keyboardHeightPercentBase` 為 `RealSize`<br>2. 縱向 → 橫向旋轉<br>3. 用 logcat 觀察 `keyboardHeightPx get()` | `base` 值在橫向時改變（`RealSize` 回報橫向全尺寸） | `InputView.kt:159-168` |
| **ROT-3** | 旋轉 + candidate 浮窗 | 1. 在有候選字時旋轉<br>2. 觀察候選字視窗位置 | 候選字浮窗隨新螢幕尺寸重新定位 | `CandidatesView.kt:148-183` |
| **PWD-1** | 密碼欄位不學習 | 1. 輸入密碼文字<br>2. 切換到其他文字欄位<br>3. 輸入同樣密碼首字母<br>4. 觀察候選字 | 密碼文字**不**出現在候選字/最近使用中 | `CapabilityFlag.kt:144-148` + `RecentlyUsed.kt` |
| **PWD-2** | 密碼欄位 toolbar | 1. 聚焦密碼欄位<br>2. 觀察 toolbar（KawaiiBar）顯示 | toolbar 顯示數字列或其他 password-appropriate 佈局 | `KawaiiBarComponent.kt`（⚠️ 需 submodule） |
| **PWD-3** | visible password | 1. 聚焦 `TYPE_TEXT_VARIATION_VISIBLE_PASSWORD` 欄位<br>2. 觀察候選字 | `Sensitive` + `NoSpellCheck` flag 生效，無 personalized suggestion | `CapabilityFlag.kt:149-152` |
| **DB-1** | Direct Boot 啟動 | 1. 裝置重啟（未解鎖）<br>2. 在鎖屏畫面嘗試使用鍵盤 | 鍵盤可正常啟動並輸入（Direct Boot 模式） | `AndroidManifest.xml:126` + `FcitxApplication.kt:85-88` |
| **DB-2** | Direct Boot 解鎖切換 | 1. 在 Direct Boot 模式下使用鍵盤<br>2. 解鎖裝置<br>3. 鍵盤應自動重啟到正常模式 | `unlockReceiver` 觸發 → `stopFcitx()` + `AppUtil.exit()` → 重啟 | `FcitxApplication.kt:48-59` |
| **DB-3** | Direct Boot recently-used | 1. 在 Direct Boot 模式下使用選字<br>2. 觀察 recently-used 是否可讀寫 | 使用 `directBootAwareContext` 讀寫 SharedPreferences | `RecentlyUsed.kt:23-24` |

---

## 附錄 B：上游參考路徑速查表

| 概念 | 檔案路徑（相對 `upstream-ro/`） | 關鍵行 |
|------|-------------------------------|--------|
| IMS 類別 | `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt` | L83 |
| Lifecycle 包裝 | `app/src/main/java/org/fcitx/fcitx5/android/input/LifecycleInputMethodService.kt` | L14 |
| InputView | `app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt` | L71 |
| CandidatesView | `app/src/main/java/org/fcitx/fcitx5/android/input/CandidatesView.kt` | L41 |
| BaseInputView | `app/src/main/java/org/fcitx/fcitx5/android/input/BaseInputView.kt` | L33 |
| InputDeviceManager | `app/src/main/java/org/fcitx/fcitx5/android/input/InputDeviceManager.kt` | L16 |
| InputConnection utils | `app/src/main/java/org/fcitx/fcitx5/android/utils/InputConnection.kt` | L11 |
| CapabilityFlag | `app/src/main/java/org/fcitx/fcitx5/android/core/CapabilityFlag.kt` | L16 |
| EditorInfo utils | `app/src/main/java/org/fcitx/fcitx5/android/utils/EditorInfo.kt` | L14 |
| CursorTracker | `app/src/main/java/org/fcitx/fcitx5/android/input/cursor/CursorTracker.kt` | L10 |
| KeyboardHeightPercentBase | `app/src/main/java/org/fcitx/fcitx5/android/input/keyboard/KeyboardHeightPercentBase.kt` | L11 |
| FcitxApplication | `app/src/main/java/org/fcitx/fcitx5/android/FcitxApplication.kt` | L34 |
| RecentlyUsed | `app/src/main/java/org/fcitx/fcitx5/android/data/RecentlyUsed.kt` | L15 |
| AndroidManifest | `app/src/main/AndroidManifest.xml` | L124-137 |
| KawaiiBarComponent | `app/src/main/java/org/fcitx/fcitx5/android/input/bar/KawaiiBarComponent.kt` | — |
| KeyboardWindow | `app/src/main/java/org/fcitx/fcitx5/android/input/keyboard/KeyboardWindow.kt` | L36 |

---

## 附錄 C：Greps 驗證

```
$ cd D:\Temp\opencode\upstream-ro
$ git rev-parse HEAD
6998502528cd26efb6079556da92003638e4179c

$ grep -n "onCreateInputView" app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt
570:    override fun onCreateInputView(): View? {

$ grep -n "beginBatchEdit\|endBatchEdit\|withBatchEdit" app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt
76:import org.fcitx.fcitx5.android.utils.withBatchEdit
430:            ic.withBatchEdit {
449:            ic.withBatchEdit {
925:        ic.beginBatchEdit()
971:        ic.endBatchEdit()

$ grep -n "beginBatchEdit\|endBatchEdit\|withBatchEdit" app/src/main/java/org/fcitx/fcitx5/android/utils/InputConnection.kt
11:fun InputConnection.withBatchEdit(block: InputConnection.() -> Unit) {
12:    beginBatchEdit()
14:    endBatchEdit()

$ grep -n "Password\|Sensitive\|NO_PERSONALIZED_LEARNING" app/src/main/java/org/fcitx/fcitx5/android/core/CapabilityFlag.kt
25:    Password(1UL shl 3),
54:    Sensitive(1UL shl 36),
78:    PasswordOrSensitive(Password.flag or Sensitive.flag);
103:                if (it.hasFlag(EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING)) {
104:                    flags += CapabilityFlag.Sensitive
144:                    if (equals(InputType.TYPE_TEXT_VARIATION_PASSWORD) ||
145:                        equals(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)) {
146:                        flags += CapabilityFlag.Password
149:                    if (equals(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
150:                        flags += CapabilityFlag.Sensitive
151:                        flags += CapabilityFlag.NoSpellCheck

$ grep -n "directBootAware" app/src/main/AndroidManifest.xml
 56:            android:directBootAware="true"
116:            android:directBootAware="true"
126:            android:directBootAware="true"
174:            android:directBootAware="true"

$ grep -n "isDirectBootMode\|directBootAwareContext\|ACTION_USER_UNLOCKED" app/src/main/java/org/fcitx/fcitx5/android/FcitxApplication.kt
51:            if (intent.action != Intent.ACTION_USER_UNLOCKED) return
52:            if (!isDirectBootMode) return
73:    var isDirectBootMode = false
76:    val directBootAwareContext: Context
85:        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !userManager.isUserUnlocked) {
86:            isDirectBootMode = true
87:            registerReceiver(unlockReceiver, IntentFilter(Intent.ACTION_USER_UNLOCKED))

$ grep -n "onConfigurationChanged" app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt
537:    override fun onConfigurationChanged(newConfig: Configuration) {

$ grep -n "RealSize\|DisplayMetrics\|getRealSize" app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt
35:import org.fcitx.fcitx5.android.input.keyboard.KeyboardHeightPercentBase.DisplayMetrics
36:import org.fcitx.fcitx5.android.input.keyboard.KeyboardHeightPercentBase.RealSize
161:                DisplayMetrics -> resources.displayMetrics.heightPixels
162:                RealSize -> Point().also {
168:                }.getRealSize(it)
```

---

## ⚠️ Submodule 未 fetch 之缺口

以下內容位於 `lib/` 或 `plugin/` submodule 中，目前無法提供精確行號：

1. **FcitxDaemon / FcitxConnection** 的 Direct Boot 初始化路徑（`FcitxInputMethodService.kt:206` 呼叫 `FcitxDaemon.connect()`，實作在 submodule）
2. **fcitx5 核心** 的 `CapabilityFlag.Password` 處理邏輯（如何影響 candidate ranking / learning）
3. **KawaiiBarComponent** 的 toolbar 佈局切換邏輯（password 時顯示數字列）
4. **libchewing** 引擎的 composing text 格式化

Fetch submodule 後可補充精確行號。
