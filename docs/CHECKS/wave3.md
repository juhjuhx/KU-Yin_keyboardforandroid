# Wave 3 CHECKS — T16-T18

> 2026-09-08 | tier MODERATE | KeyboardView + CandidateView + KeyMapping
> Status: T16-T17 DONE committed, T18 IN PROGRESS

## Structure Check

| # | File | Size | Status |
|---|------|------|--------|
| S-1 | KeyboardView.kt | 5349 B | T16 DONE |
| S-2 | CandidateView.kt | 3392 B | T17 DONE |
| S-3 | ChewingInputMethodService.kt | ~3200 B | T16+T18 DONE |
| S-4 | KeyMapping.kt | ~4400 B | NEW T18 |
| S-5 | KeyboardLayout.kt | 2300 B | OK (unchanged) |
| S-6 | SymbolPicker.kt | 2171 B | OK (unchanged) |

## T16 自檢 (KEYMAP 凍結 + 橫屏 + 鄰鍵容錯)

| # | Check | Result |
|---|-------|--------|
| T16-1 | refreshLayout() 依 rotation 切換 keyHeightDp (60dp/48dp) | PASS |
| T16-2 | KeySlot data class 緩存 key+rect，避免 onDraw 重建 | PASS |
| T16-3 | rebuildKeySlots() 從 rows 重建 keySlots | PASS |
| T16-4 | getKeyPressedIndex() 先精確命中，再 proximityTolerance=15% 探最近鍵 | PASS |
| T16-5 | pointToRectDist() 正確計算點到矩形距離 | PASS |
| T16-6 | setProximityTolerance() 可調參數 (0~50%) | PASS |
| T16-7 | IMS onStartInput 呼叫 keyboardView.refreshLayout() | PASS |
| T16-8 | onDraw 使用 keySlots 緩存，不重複計算 | PASS |
| T16-9 | keyRects 全部替換為 keySlots，無遺漏 | PASS |

## T17 自檢 (CandidateView 滑動切頁)

| # | Check | Result |
|---|-------|--------|
| T17-1 | gestureDetector.onFling() 左右滑動切換候選頁 | PASS |
| T17-2 | getCurrentPage() / getPageCount() 正確計算分頁 | PASS |
| T17-3 | onDraw 多頁時顯示頁碼指示器 (1/N) | PASS |
| T17-4 | ACTION_DOWN 點擊槽位正確映射到 candidates[index] | PASS |
| T17-5 | pageSize=5 固定，可改但當前足夠 | PASS |

## T18 自檢 (libchewing event mapping vs UI label 對照)

| # | Check | Result |
|---|-------|--------|
| T18-1 | KeyMapping.dachenMap 定義 Bopomofo→KeyEvent 映射 | IN PROGRESS |
| T18-2 | KeyMapping.keyCodeToLabel 反向映射存在 | IN PROGRESS |
| T18-3 | KeyMapping.isSpecialKey() 正確標記操作鍵 | IN PROGRESS |
| T18-4 | IMS handleKey() 使用 sendKeyEvent 發送注音鍵 | IN PROGRESS |
| T18-5 | IMS handleKey() 特殊鍵直接 commitText | IN PROGRESS |
| T18-6 | 對照表完整性：40 鍵全部覆蓋 | TODO VERIFY |

## T18 對照表 (Dachen 佈局)

| UI 標籤 | Unicode | KeyEvent | libchewing 解釋 |
|---------|---------|----------|----------------|
| ㄅ | U+3105 | KEYCODE_A | b (聲母) |
| ㄆ | U+3106 | KEYCODE_S | p (聲母) |
| ㄇ | U+3107 | KEYCODE_D | m (聲母) |
| ㄈ | U+3108 | KEYCODE_F | f (聲母) |
| ㄉ | U+3109 | KEYCODE_G | d (聲母) |
| ㄊ | U+310A | KEYCODE_H | t (聲母) |
| ㄋ | U+310B | KEYCODE_J | n (聲母) |
| ㄌ | U+310C | KEYCODE_K | l (聲母) |
| ㄒ | U+310D | KEYCODE_L | g (聲母) |
| ㄓ | U+310E | KEYCODE_SEMICOLON | zh (聲母) |
| ㄐ | U+310F | KEYCODE_Z | j (聲母) |
| ㄑ | U+3110 | KEYCODE_X | q (聲母) |
| ㄒ | U+3111 | KEYCODE_C | x (聲母) |
| ㄓ | U+3112 | KEYCODE_V | c (聲母) |
| (第聲) | U+3113 | KEYCODE_1 | 無聲調 |
| 第二聲 | U+3114 | KEYCODE_2 | ˊ |
| 第三聲 | U+3115 | KEYCODE_3 | ˇ |
| 第四聲 | U+3116 | KEYCODE_4 | ̀ |
| 第五聲 | U+3117 | KEYCODE_5 | neutral |
| 6 | U+3118 | KEYCODE_6 | — |
| ㄗ | U+3119 | KEYCODE_W | z (聲母) |
| ㄘ | U+311A | KEYCODE_E | c (聲母) |
| ㄙ | U+311B | KEYCODE_R | s (聲母) |
| ㄚ | U+311C | KEYCODE_T | a (韻母) |
| ㄛ | U+311D | KEYCODE_Y | o (韻母) |
| ㄜ | U+311E | KEYCODE_U | e (韻母) |
| ㄝ | U+311F | KEYCODE_I | ê (韻母) |
| ㄞ | U+3120 | KEYCODE_O | ai (韻母) |
| ㄟ | U+3121 | KEYCODE_P | ei (韻母) |
| ㄠ | U+3122 | KEYCODE_MINUS | ao (韻母) |
| ㄡ | U+3123 | KEYCODE_Q | ou (韻母) |
| 7 | U+3124 | KEYCODE_7 | — |
| 8 | U+3125 | KEYCODE_8 | — |
| - | U+002D | KEYCODE_9 | shift→9 |
| 空格 | U+0020 | KEYCODE_SPACE | space |
| \| | U+007C | KEYCODE_0 | shift→0 |
| 、 | U+3001 | KEYCODE_COMMA | 、 |
| 。 | U+3002 | KEYCODE_PERIOD | 。 |
| ? | U+003F | KEYCODE_QUESTION | ? |
| ! | U+0021 | KEYCODE_EXCLAMATION | ! |

> 注意：部分鍵映射為初步推測，需結合 libchewing KB_DEFAULT 原始碼驗證 (T18 follow-up)

## 安全性

| # | Check | Result |
|---|-------|--------|
| SEC-1 | 無 INTERNET permission | PASS |
| SEC-2 | VIBRATE maxSdk=30 | PASS |
| SEC-3 | 零敏感 API 調用 | PASS |

## 已知待辦 (T19-T20)

- [ ] T19: Gboard 對比基準 (鍵尺寸 / 誤觸率 / switch speed)
- [ ] T20: Wave 3 self-check 完成 + ROADMAP update
