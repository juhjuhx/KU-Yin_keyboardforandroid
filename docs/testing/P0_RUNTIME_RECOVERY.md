# P0 Runtime 恢復與 Host oracle 修正

本切片只改 test-only `acceptance-host` 與 opt-in CI。產品 Kotlin、JNI、native pins、版本、簽章及原 App 五個 Runtime tests 不變。它修正測試 Editor 的生命週期與觀察方式；不是已證明的 KU-Yin 解碼器缺陷修復。

## 直接 Runtime 證據

- [尺寸觀測 CI40861263970](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/40861263970)，source `aaf76d2ffa67b95b82c2f41250e7ed491885b2a3`：真 `android.R.id.content` 高度 1731→0→956；sizeChange／clearFocus 後 Editor 無焦點。H1 另因 XML command 未正常 EOF 保持 INCONCLUSIVE，不能把這次當完整 oracle PASS。
- [恢復 CI40904988107](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/40904988107)，source `cf4f54f36802f7e935aca909257a5a92c99b8b73`：同 boot H0/H1 HEALTHY；真 View/root/node/window focus 均成立，XML command 自然 exit0。僅在曾有焦點、content 暫時零尺寸且仍 resumed/window-focused 的邊界，正尺寸恢復後嘗試一次 requestFocus；pause、detach、其他焦點或失敗請求均不能形成重試迴圈。
- [完整執行 CI40908132614](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/40908132614)，source `14e94c07ad1cdaeb6098743f86cc1b3b6a85b254`：同 boot H0/H1 通過；原 App **5 PASS / 0 FAIL / 0 SKIP**，包含真 IME visibility/recreate/native composition。Host **15 PASS / 6 FAIL / 11 SKIP**。本次結果保留；後續修正不會回寫成 PASS。

原始證據是 Actions 的 `f4-focus-health-evidence` artifact；四 APK、命令、退出碼、測試原始輸出與前後健康觀測分開保存。`PRODUCT_RUNTIME_DISPOSITION.json` 是 continuation 的最後狀態；`STAGE_DISPOSITION.json` 只描述較早的 H0/H1 checkpoint。

## Host 停止點與修正契約

| 範圍 | 直接失敗 / 根因 | 修正與不可放寬條件 |
|---|---|---|
| ZH01 / ZH02 | `p0SoftTarget` 讀 applicationPackage 時 Stale，尚未 gesture | 整輪 query 作廢、有界重新取得；click 不在重試範圍 |
| B02 / B03 | 初次 candidate scan 在既有 poll 外讀 visibleBounds 時 Stale | 當輪重查 preedit、鍵列、候選；最終 clickable hitbox 必須也在候選帶 |
| 候選幾何 | 舊 adapter 要求候選與 preedit 同列且在 clear 右側，與目前 Column→CandidateStrip 不符 | 只允許 preedit 下方、鍵列上方的可見候選；指定 label 須唯一，不任意挑同名 hitbox |
| D13 / D14 | before.currentText 已包含真 composing「你」，舊 append oracle 錯誤預期「你你」 | 讀真 Editable composing span；只替換該 range、完整保留前後文、commit+1、ACK=true、span 清除；原第二次 Enter / Send / newline 檢查保留 |

`CoreTypingActions.kt` 透過原有 sharedTest sourceSets 同時編入 JVM 與 instrumentation，測試的是同一個 observer。真 UiObject2 refresh=false / fresh-generation fixture 不替換查詢或 getter；只替代 transport。composing 回歸使用真 RecordingEditText／平台 InputConnection，不是假的 Editor。

Android [`InputConnection.commitText`](https://developer.android.com/reference/android/view/inputmethod/InputConnection#commitText(java.lang.CharSequence,int)) 會替換 composing range。最後字串相同不等於 exactly-once；負例必須能拒絕兩次 commit 即使文字相同。

## 證據邊界

- 唯一可放行的次序：fresh H0 → H1 → H2 / 原 App5 → Host32；新 boot 不能沿用舊收據。
- 32 個既有功能 identities 不減少；R2 branch-only 額外 infrastructure case 不在 main。frozen P1 APK / manifest 不變，current source ledger 另行更新。
- B01 多寬度／旋轉、native context／learning observer、TYPE_NULL 及 Binder false-return 缺口仍須逐案記錄。FAIL→SKIP 不算修復。
- Emulator PASS 不是手機、保資料升級、QR 掃描、OEM D2D、key custody 或完整授權通過。此文沒有發布授權；RC / Stable 維持 NO_GO。
