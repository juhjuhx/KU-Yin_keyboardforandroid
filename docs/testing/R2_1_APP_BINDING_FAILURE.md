# R2.1 App 前置 visibility 失敗

**ACTIVE BLOCKER／ROOT_CAUSE_UNRESOLVED。** run[38825146067](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/38825146067)，attempt2，exact `b1b5bf6b3939fef7e62893d833b0853e0a08ae7d`；manual geometry有限scope，Host尚未執行。這不是B01 geometry或產品typing已被證實的defect。

## 直接結果

App5＝4PASS／1FAIL／0SKIP；失敗為`ImeRuntimeSmokeTest#imeBindsAndSurvivesEditorRecreate`原first visibility assertion（kt81）。5個start/end與4×0、1×-2、terminal-1完整，沒有protocol truncation。first=false，recreate未到達；runner original_exit1/evidence_exit0，正確不執行Host。沒有B01 case envelope／HostXML，不能標B01 FAIL或PASS。

source manifest185/185 SHA對b1；App全部來源與R2d6相同，b1只改Host測試／optional geometry scope。相同source不等於相同整包APK/環境，不能由此直接推產品bug、flaky或observer導致。R2d6 normal5＋33及finite5＋2的成功原收據保留，不被新FAIL改寫，也不能拿來覆蓋本次FAIL。

## 新的時序與平台邊界

| 當次觀察 | elapsed ms／結果 |
|---|---|
| window focus | 90914，true；真IC count0→1於90916 |
| 原view request | 90939，focus/window/attached/token均true、IC1 |
| 原Insets show | 90944，controller存在 |
| 原IMM show | 90953，accepted=false；focus/window/attached/token true、IC1 |
| 原10s deadline | 100976，firstVisible=false |
| failure raw採證 | 100997–101044，五個txt都真非空／persistence_verified=true；另PNG無鍵盤 |


因此**不能沿用舊early-show（windowFocus=false／IC0）作此輪根因**。first-input-method raw顯示：user0、KU-Yin registered/enabled/selected；Host真正served EditableInputConnection且isActive；但system/client `mCurMethod=null`、`mEnabledSession=null`、`mBoundToMethod=false`、`mInputShown=false`。ShowRequested/ExplicitlyRequested=true，不等於遠端method/session已連上。

first-service dump為「No services match」active component；這不等於未註冊IME（`first-input-method.txt` 列出已註冊service；active `first-service.txt` 本身沒有列出服務）。before-app曾有active service pid、mInputStarted=false；before-app remote-null在R2d6成功輪也出現，不能單憑這個前置狀態作因果。

原per-test兩個showSoftInput debug log對應exact API33 `InputMethodManager.java:1879`，位於served gate通過之後；因此本輪在show當下也已通過served檢查，不只是10秒後才served。[AOSP API33 IMM](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/core/java/android/view/inputmethod/InputMethodManager.java#L1870)。

第一個可確定失敗邊界：editor focused／IC建立且show已served，遠端IME method/session與可見window仍未取得。**為何remote未連上仍UNRESOLVED**；不能把registration／selection／localIC視為整條IMF已就緒。

## 支持／反證與待證假說

- 支持binding/lifecycle前提問題：local editor就緒、selected正確但remote-null／無active service；不是單純候選selector或B01方向變化，因Host完全未執行。
- IMM accepted=false本身不是充分根因：R2歷史有限run38660489452亦曾false後成功visible（見[R2交接](R2_FINAL_HANDOFF.md)，不是本輪新run）。
- 反證舊結論：show前已有window focus與IC1；after-focus one-shot只能修已證的早show條件，不能保證remote session。
- 安裝順序／target process重啟假說：before-app與live package sourceDir不同，腳本先select再呼叫Gradle connected（包含target安裝）；需要精確package replacement／service disconnect／rebind trace及控制組。此為假說，未把安裝路徑差異當成已證force-stop因果。
- 產品service crash、provider startup、emulator transport／系統重綁原因均未在本輪獲充分證明；不從無service dump推出crash。

## 界線與下一個最小工作

不再retry此已執行的visibility FAIL，不延timeout、不額外show/checkFocus、不跳App前置、不改product/JNI，也不開始另一runner。先另案限定App target安裝／service binding時序，控制exact App test/產品、API/user/install順序，保留原visibility/recreate assertions。若確證產品缺陷再提窄product修復批准；B01已有source/單元測試，等前提恢復後才執行。

本次首次取得五個真failure-path dumps，不再只有canary；只證固定五種shell命令在此次API33失敗路徑有非空結果，不泛稱所有Android/OEM診斷可靠。所有資料來自fresh CI synthetic editor。

原始external：`b01-attempt2-runtime/`、`27-b01-attempt2-evidence-audit.json`、job log與185source manifest；獨立覆核收據另見[R2.1 review](R2_1_INDEPENDENT_REVIEW.md)。attempt1 SDK錯誤另保留，不能與本次AppFAIL混為一件。
