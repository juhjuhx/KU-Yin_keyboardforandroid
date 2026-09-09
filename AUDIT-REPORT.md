# KU-Yin (Chewing) 注音輸入法 - 完整審查報告

**審查時間**: 2026-09-09
**項目路徑**: `D:\666\opencode\android-keyboard`
**最終狀態**: ✅ 構建成功，APK 19.4 MB

---

## 📋 問題診斷與修復記錄

### 問題 1: 鍵盤無法顯示
**根本原因**: KeyMapping.kt 的 Android KEYCODE 與 libchewing xkbcommon 鍵碼不匹配

| 原始錯誤映射 | 修復後 |
|------------|--------|
| A(29) → KEY_A(38) ❌ | KEY_1(10) → ㄅ ✅ |
| B(30) → KEY_B(56) ❌ | KEY_2(11) → ㄉ ✅ |
| 所有鍵位完全錯誤 | 對齊 libchewing standard.rs |

**修復內容**:
- 新增 `charToLibchewingKeyCode` 映射表，直接對應 bopomofo → xkbcommon keycode
- 移除不存在的 `KEYCODE_TRAVERSE_START` 和 `KEYCODE_NUMPAD_EQUAL`
- 修正重複的 `KEYCODE_NUMPAD_ENTER` 條目

### 問題 2: 應用崩潰
**根本原因**: 
1. `handleSpace()` 方法不存在於 `ChewingEngine` 接口
2. JNI 函數簽名不匹配導致 `UnsatisfiedLinkError`

**修復內容**:
- 添加 `handleKeyEvent(keyCode: Int)` 方法到接口
- 修正 JNI 函數名轉換規則 (下劃線 → `_1`)
- 添加 `import android.util.Log` 用於調試

### 問題 3: SettingsActivity 缺失
**根本原因**: `input_method.xml` 引用不存在的 Activity

**修復內容**:
- 創建 `SettingsActivity.kt` 顯示使用指南
- 創建 `activity_settings.xml` 佈局文件
- 在 `AndroidManifest.xml` 中註冊 Activity
- 恢復 `android:settingsActivity` 屬性

### 問題 4: AndroidManifest 配置錯誤
**修復內容**:
- 添加 App 主題 `Theme.AndroidKeyboard`
- 確保 IME Service 有正確的 `intent-filter`
- 設置 `android:exported="true"` 讓系統能綁定

---

## 🎯 最終架構

```
ku-yin-inputmethod/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/androidkeyboard/
│   │   │   ├── SettingsActivity.kt          # ✅ 新增
│   │   │   ├── input/
│   │   │   │   ├── ChewingInputMethodService.kt  # ✅ 修復
│   │   │   │   ├── KeyboardLayout.kt        # ✅ 存在
│   │   │   │   ├── KeyboardView.kt          # ✅ 存在
│   │   │   │   ├── KeyMapping.kt            # ✅ 修復 (核心)
│   │   │   │   └── SymbolPicker.kt          # ✅ 存在
│   │   │   ├── engines/
│   │   │   │   ├── android/
│   │   │   │   │   └── AndroidChewingEngine.kt  # ✅ JNI 封裝
│   │   │   │   └── core/
│   │   │   │       ├── ChewingEngine.kt     # ✅ 接口定義
│   │   │   │       ├── IMEConfig.kt         # ✅ 配置管理
│   │   │   │       └── OpenCCConverter.kt   # ✅ 簡繁轉換
│   │   │   └── ui/
│   │   │       └── CandidateView.kt         # ✅ 候選詞視圖
│   │   ├── cpp/
│   │   │   ├── chewing_jni.cpp              # ✅ JNI 橋接層
│   │   │   ├── CMakeLists.txt               # ✅ 構建配置
│   │   │   └── libchewing-src/              # ✅ libchewing 0.12.x 源碼
│   │   ├── jniLibs/
│   │   │   ├── arm64-v8a/libchewing-jni.so  # ✅ 4 ABI
│   │   │   ├── armeabi-v7a/libchewing-jni.so
│   │   │   ├── x86/libchewing-jni.so
│   │   │   └── x86_64/libchewing-jni.so
│   │   ├── res/
│   │   │   ├── layout/activity_settings.xml  # ✅ 新增
│   │   │   ├── values/strings.xml
│   │   │   ├── values/colors.xml
│   │   │   ├── values/themes.xml
│   │   │   └── xml/input_method.xml
│   │   └── AndroidManifest.xml              # ✅ 已修復
│   └── build.gradle.kts
```

---

## 📱 使用方法

### 啟用步驟
1. **安裝 APK**: 將 `app-debug.apk` 傳輸到手機並安裝
2. **啟用輸入法**: 
   - 進入「設定」→「系統」→「鍵盤與輸入法」
   - 勾選 "KU-Yin (Chewing)" 旁邊的方框
   - 點擊確認啟用
3. **測試**: 打開任何文字輸入框，切換到 KU-Yin 輸入法

### 鍵盤布局 (Standard/Dachen)
```
第1行: 1=ㄅ 2=ㄉ 3=ˋ 4=ˇ 5=ㄓ 6=ˊ 7=˙ 8=ㄚ 9=ㄞ 0=ㄢ -=ㄦ
第2行: Q=ㄆ W=ㄊ E=ㄍ R=ㄐ T=ㄔ Y=ㄗ U=ㄧ I=ㄛ O=ㄟ P=ㄣ
第3行: A=ㄇ S=ㄋ D=ㄌ F=ㄏ G=ㄕ H=ㄘ J=ㄩ K=ㄜ L=ㄠ ;=ㄫ
第4行: Z=ㄈ X=ㄌ C=ㄒ V=ㄒ B=ㄖ N=ㄙ M=ㄡ
第5行: ,=ㄝ /=ㄥ [空格]=確認 [退格]=刪除
```

### 輸入技巧
- **按空白鍵**: 確認當前拼音，顯示候選字
- **候選字選擇**: 點擊候選列中的字詞
- **左右滑動**: 翻頁查看更多候選字
- **退格鍵**: 刪除最後一個拼音或字元

---

## 🔧 技術細節

### JNI 鍵碼轉換
```
libchewing 使用 xkbcommon key codes (evdev + 8):
  KEY_1 = 10, KEY_2 = 11, KEY_Q = 24, KEY_A = 38...

Android 使用自己的 KeyEvent:
  KEYCODE_1 = 8, KEYCODE_Q = 45, KEYCODE_A = 29...

轉換: Android_KEYCODE → libchewing_KEY → chewing_handle_Default()
```

### libchewing 0.12.x 特性
- **標準布局**: `KB_DEFAULT` (Dachen 大千布局)
- **內嵌字典**: `mini.dat` 編譯進靜態庫
- **無外置數據**: `chewing_Init()` 是 no-op
- **Unicode 支持**: UTF-8 編碼

---

## 📦 構建產物

| 組件 | 路徑 | 大小 |
|------|------|------|
| APK | `app/build/outputs/apk/debug/app-debug.apk` | 19.4 MB |
| ARM64 SO | `app/src/main/jniLibs/arm64-v8a/libchewing-jni.so` | 3.5 MB |
| ARM32 SO | `app/src/main/jniLibs/armeabi-v7a/libchewing-jni.so` | 2.5 MB |
| x86 SO | `app/src/main/jniLibs/x86/libchewing-jni.so` | 3.9 MB |
| x86_64 SO | `app/src/main/jniLibs/x86_64/libchewing-jni.so` | 3.7 MB |

---

## ✅ 修復清單

- [x] KeyMapping.kt - 新增 xkbcommon 鍵碼映射表
- [x] ChewingInputMethodService.kt - 移除 handleSpace()，使用 handleKeyEvent(65)
- [x] AndroidManifest.xml - 添加 SettingsActivity 和主題
- [x] SettingsActivity.kt - 創建使用指南頁面
- [x] activity_settings.xml - 創建佈局文件
- [x] input_method.xml - 恢復 settingsActivity 屬性
- [x] 構建驗證 - BUILD SUCCESSFUL

---

## 🎓 教訓總結

1. **鍵碼系統差异**: Android KeyEvent ≠ xkbcommon KEY，必須使用轉換表
2. **接口一致性**: 所有方法必須在接口中聲明，否則編譯失敗
3. **Activity 依賴**: `settingsActivity` 屬性指向不存在的 Activity 會導致崩潰
4. **JNI 命名**: 下劃線在 JNI 中轉為 `_1`，但 Kotlin external fun 不需要

---

**結束語**: 所有關鍵問題已修復，APK 可正常構建和安裝。建議先在手機上測試基本輸入功能，再進行個性化設置。
