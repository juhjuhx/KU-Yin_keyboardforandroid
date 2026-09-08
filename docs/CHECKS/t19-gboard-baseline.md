# T19: Gboard 對比基準報告

> 2026-09-08 | tier ANALYSIS | Gboard注音 vs android-keyboard
> Status: PENDING (分析基準表，待實機量測驗證)

## 1. 對標對象

- **Gboard Android 注音版**（Google Inc.，閉源）
- **android-keyboard**（本專案，開源 fork）

## 2. 基準指標

### 2.1 鍵尺寸

| 指標 | Gboard 參考值 | android-keyboard 目標 | 當前實現 |
|------|--------------|----------------------|----------|
| 鍵高（直屏） | ~58-62 dp | 60 dp | 60 dp |
| 鍵高（橫屏） | ~44-48 dp | 48 dp | 48 dp |
| 鍵寬 | 10%p（均勻） | 10%p（均勻） | 10%p |
| hGap | 2-3 dp | 3 dp | 3 dp |
| vGap | 4-6 dp | 6 dp | 6 dp |

> 註：Gboard 實際尺寸需實機測量；上表為目測估計值，待 T19 實機驗證後更新。

### 2.2 誤觸率估算

公式：誤觸率 ≈ (鍵間隙 / 鍵尺寸) × 觸控精度因子

| 佈局 | android-keyboard | Gboard（參考） |
|------|-----------------|---------------|
| 直屏 60dp 鍵高 | 6dp vGap / 60dp = 10% | ~8%（估計） |
| 橫屏 48dp 鍵高 | 6dp vGap / 48dp = 12.5% | ~10%（估計） |
| 鄰鍵容錯 | 15% keyHeight | 同級（Gboard 有自動糾錯） |

### 2.3 切換速度

| 指標 | android-keyboard | 目標 |
|------|-----------------|------|
| 鍵盤彈出延遲 | <200ms（體感） | <150ms |
| 候選更新延遲 | libchewing 單鍵 <10ms | <5ms |
| 橫屏切換延遲 | refreshLayout() 重繪 | <50ms |

## 3. 差異分析

### 3.1 優勢（android-keyboard）

- 零 INTERNET：隱私保護
- 開源可審計：LGPL-2.1
- 橫屏自動適配：refreshLayout()
- 鄰鍵容錯：proximityTolerance 可調
- 候選滑動切頁：gestureDetector
- 主題自訂：M3 動態色 + OLED

### 3.2 劣勢（待補）

- libchewing JNI 未整合（核心功能缺失）
- 無簡繁切換 UI（OpenCC 未接）
- 無使用者詞庫（UserDict 未接）
- 無 Emoji/符號選擇器（SymbolPicker 已寫但未接入）
- 無 toolbar（T17 規劃中）

## 4. 下一步

1. 實機測量 Gboard 鍵尺寸（截圖 + dp 换算）
2. 實測 android-keyboard 誤觸率（與 Gboard 並排測試）
3. 記錄 switch speed（從按下到上屏的延遲）
4. 更新本檔為正式對比報告

---

_本檔為基準表，非實測結果。T19 完成後會替換為實測數據。_
