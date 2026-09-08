# Wave 4 T21 Self-Check

> 2026-09-08 | tier MODERATE | engines/core split 完成自檢
> Status: PASS

## 1. 結構完整性

| # | 檢查項 | 結果 |
|---|--------|------|
| 1 | engines/core/ChewingEngine.kt 存在且為 interface | PASS |
| 2 | engines/core/ChineseConverter.kt 存在且為 interface | PASS |
| 3 | engines/core/IMEConfig.kt 存在且為 class | PASS |
| 4 | engines/android/AndroidChewingEngine.kt 存在且實作 ChewingEngine | PASS |
| 5 | engines/opencc/OpenCCConverter.kt 存在且實作 ChineseConverter | PASS |
| 6 | ChewingInputMethodService.kt 導入 engines/core | PASS |
| 7 | CandidateView.kt slotW bug 已修復 | PASS |
| 8 | 所有 import 語法正確（無拼字錯誤） | PASS |

## 2. 依賴邊界檢查（核心規則）

| # | 檢查項 | 結果 |
|---|--------|------|
| 1 | UI 層（KeyboardView/CandidateView）不 import engines.android | PASS |
| 2 | UI 層不 import JNI 相關 package | PASS |
| 3 | IMS 層通過 ChewingEngine interface 呼叫解碼器 | PASS |
| 4 | AndroidChewingEngine 是唯一切入 JNI 的入口（stub 中 TODO 標註） | PASS |
| 5 | OpenCCConverter 是唯一切入 OpenCC C API 的入口（stub 中 TODO 標註） | PASS |
| 6 | IMEConfig 封裝所有 SharedPreferences 存取 | PASS |

## 3. 功能走查

### 3.1 ChewingEngine 介面

| # | 方法 | 狀態 |
|---|------|------|
| 1 | init(layout) | PASS (stub) |
| 2 | reset() | PASS (stub) |
| 3 | handleKeyEvent(keyCode) | PASS (stub, 回傳 false) |
| 4 | getPreedit() | PASS (stub) |
| 5 | getCandidates() | PASS (stub) |
| 6 | selectCandidate(index) | PASS (stub) |
| 7 | commit() | PASS (stub) |
| 8 | backspace() | PASS (stub) |
| 9 | toggleFullHalf() | PASS (stub) |
| 10 | loadUserDict(path) | PASS (stub) |
| 11 | saveUserDict(path) | PASS (stub) |

### 3.2 IMEConfig

| # | 設定鍵 | 預設值 | 結果 |
|---|--------|--------|------|
| 1 | layout | DACHEN | PASS |
| 2 | fullHalf | false | PASS |
| 3 | s2tProfile | S2TW | PASS |
| 4 | t2sProfile | TW2S | PASS |
| 5 | conversionEnabled | true | PASS |
| 6 | hapticEnabled | true | PASS |
| 7 | proximityTolerance | 0.15 | PASS |

### 3.3 IMS 整合

| # | 行為 | 結果 |
|---|------|------|
| 1 | onCreate() 初始化 config + chewing + converter | PASS |
| 2 | onCreateInputView() 讀取 config.hapticEnabled/proximityTolerance | PASS |
| 3 | onStartInput() 調用 config.applyTo(chewing) | PASS |
| 4 | onFinishInput() 調用 chewing.reset() | PASS |
| 5 | handleKey(\ back\) 先嘗試 chewing.backspace() | PASS |
| 6 | handleKey(\ \) 透過 converter.simplifyToTraditional() 處理 | PASS |
| 7 | commitCandidate() 透過 converter 轉換後提交 | PASS |

## 4. 安全性檢查

| # | 檢查項 | 結果 |
|---|--------|------|
| 1 | 無硬編碼機密/金鑰 | PASS |
| 2 | 無 Unsafe 反射 | PASS |
| 3 | 無 exec()/Runtime.exec() | PASS |
| 4 | stub 中有 TODO 標註未來 JNI 整合點 | PASS |

## 5. 已知限制

| # | 限制 | 影響 | 處理 |
|---|------|------|------|
| 1 | libchewing JNI 尚未整合 | 核心解碼無法運作 | 後續 commit 接入 chewing_*.h |
| 2 | OpenCC C API 尚未整合 | 簡繁切換無效 | 後續 commit 接入 opencc.h |
| 3 | user dict 檔案存取未實作 | 詞庫學習無效 | 後續 commit 接入 chewing_add_userphrase |

## 6. 結論

Wave 4 T21 結構拆分完成。UI → core → JNI 的三層架構已建立，
JNI 實作以 stub 形式预留，後續可直接替換為真實 libchewing 呼叫。
CandidateView slotW 變數名稱錯誤已修復（原 slotWidth 不存在）。

**整體評估：PASS**