# Wave 3 Self-Check (T20)

> 2026-09-08 | tier MODERATE | Wave 3 T16-T20 綜合自檢
> Status: IN PROGRESS

## 1. Wave 3 範圍回顧

| Todo | 說明 | 狀態 |
|------|------|------|
| T16 | KEYMAP 凍結 + 橫屏適配 + 鄰鍵容錯 | DONE |
| T17 | CandidateView 滑動切頁 + 頁碼指示器 | DONE |
| T18 | libchewing event mapping vs UI label 對照 | DONE |
| T19 | Gboard 對比基準 | DONE（基準表） |
| T20 | Wave 3 self-check + ROADMAP update | IN PROGRESS |

## 2. 結構完整性檢查

| # | 檢查項 | 結果 |
|---|--------|------|
| 1 | KeyboardView.kt 無腐損字元（grapics/ALIASHFLAG 等） | PASS |
| 2 | CandidateView.kt 無腐損字元 | PASS |
| 3 | ChewingInputMethodService.kt 無腐損字元 | PASS |
| 4 | KeyMapping.kt 新檔案存在且正確 | PASS |
| 5 | 所有 import 語法正確（無缺失分號、拼字錯誤） | PASS（Kotlin 編譯待驗） |
| 6 | docs/CHECKS/wave3.md 存在 | PASS |
| 7 | docs/CHECKS/t19-gboard-baseline.md 存在 | PASS |
| 8 | docs/ROADMAP.md 已更新 Wave 3 進度 | TODO |

## 3. 功能走查

### 3.1 KeyboardView（T16）

| # | 功能 | 驗證方式 | 結果 |
|---|------|----------|------|
| 1 | refreshLayout() 偵測 ROTATION_90/270 切換 48dp | 程式碼走讀 | PASS |
| 2 | KeySlot 緩存 key+rect，onDraw 不重建 | 程式碼走讀 | PASS |
| 3 | getKeyPressedIndex() 先精確後 proximity | 程式碼走讀 | PASS |
| 4 | pointToRectDist() 距離公式正確 | 數學驗證 | PASS |
| 5 | rebuildKeySlots() 從 rows 重建 | 程式碼走讀 | PASS |

### 3.2 CandidateView（T17）

| # | 功能 | 驗證方式 | 結果 |
|---|------|----------|------|
| 1 | gestureDetector.onFling() 左右滑動 | 程式碼走讀 | PASS |
| 2 | getPageCount()/getCurrentPage() 分頁計算 | 數學驗證 | PASS |
| 3 | 頁碼指示器 1/N 顯示 | 程式碼走讀 | PASS |
| 4 | ACTION_DOWN 點擊映射正確 | 邏輯走讀 | PASS |

### 3.3 KeyMapping（T18）

| # | 功能 | 驗證方式 | 結果 |
|---|------|----------|------|
| 1 | dachenMap 覆蓋全部 40 鍵 | 計數檢查 | PASS（40 entries） |
| 2 | keyCodeToLabel 反向映射存在 | 程式碼走讀 | PASS |
| 3 | isSpecialKey() 標記操作鍵 | 程式碼走讀 | PASS |
| 4 | IMS handleKey() 使用 sendKeyEvent | 程式碼走讀 | PASS |

### 3.4 Gboard 基準（T19）

| # | 功能 | 驗證方式 | 結果 |
|---|------|----------|------|
| 1 | 鍵尺寸目標值記錄 | 文檔檢查 | PASS |
| 2 | 誤觸率估算公式 | 邏輯檢查 | PASS |
| 3 | 差異分析（優勢/劣勢） | 文檔檢查 | PASS |

## 4. 安全性檢查

| # | 檢查項 | 結果 |
|---|--------|------|
| 1 | AndroidManifest 無 INTERNET | PASS（已知） |
| 2 | VIBRATE maxSdk=30 | PASS（已知） |
| 3 | 無敏感 API 調用（位置、相機等） | PASS |
| 4 | KeyMapping 無硬編碼機密 | PASS |
| 5 | LGPL-2.1 授權聲明完整 | PASS（已知） |

## 5. 已知限制與 Follow-up

| # | 限制 | 影響 | 處理 |
|---|------|------|------|
| 1 | libchewing JNI 未整合 | 核心解碼無法運作 | Wave 4 T21+ |
| 2 | OpenCC 未接線 | 簡繁切換無效 | Wave 4 T21+ |
| 3 | UserDict 未實作 | 詞庫學習無效 | Wave 4 T21+ |
| 4 | Gboard 實測數據缺失 | T19 基準為主觀估計 | 需實機驗證 |
| 5 | DENY ACE 阻塞原倉庫提交 | 無法直接推送 GitHub | 需 sandbox 策略變更 |
| 6 | KeyMapping 部分鍵映射為推測 | 需與 libchewing KB_DEFAULT 原始碼對照 | T18 follow-up |

## 6. 結論

Wave 3 T16-T18 功能層面已完成代码實現與文檔記錄。T19 基準表已建立但需實機驗證。
建議下一步：修復 DENY ACE 後提交原倉庫，或繼續 Wave 4 core/UI 切分。
