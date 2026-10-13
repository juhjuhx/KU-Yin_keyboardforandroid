# R2 Android IME lifecycle 證據模型

| 邊界 | 必須觀察 | 既有狀態 |
|---|---|---|
| A registered | exact canonical service list | 原assert通過 |
| B enabled | user-scoped enabled list | 原assert通過，缺當刻user capture |
| C selected | current numeric user＋exact component | 原selected assert通過，非binding證據 |
| D editor focus | Activity/view identity、attached/token/windowFocus/viewFocus | 未保存 |
| E connection | real EditText.super onCreateInputConnection之後的final EditorInfo與connection presence | 未保存 |
| F request | 既有Insets/IMM request時間、controller presence、IMM Boolean | 未保存return |
| G start input | service dump binding/input-started/editor identity | 有source callback，無runtime checkpoint |
| H start view | service dump input-view-started | 有source callback，無runtime checkpoint |
| I window | API30+ root WindowInsets IME visibility＋window state | 首次assert false |
| J recreate | 新Activity generation＋再次可見 | ea5未到達 |

source：app/src/debug/java/com/example/androidkeyboard/runtime/ImeHostActivity.kt；app/src/androidTest/java/com/example/androidkeyboard/runtime/ImeRuntimeSmokeTest.kt；app/src/main/java/com/example/ime/KuYinInputMethodService.kt:158/179。

TEST_ONLY observer須維持原show次數/順序，不在poll呼叫IMM.isActive/isAcceptingText。被動metadata不含editor text/hint/password/keystroke。failure先固定原10秒判定，然後在use內採dump；dump晚於false的時間明記，不能將採證中延遲出現的window回算PASS。

沿用平台InputMethodService.dump，記binding/connection/inputStarted/inputViewStarted/show/window狀態；這是瞬時狀態，不能冒稱完整callback事件計數。API24–29 acceptingText不是window visibility。CI screenshot/hierarchy只在fenced disposable emulator synthetic session採集，不能套到任意實機。

每次`runtime_smoke.sh`明示執行都收集CI user／device／package／service/window快照與APK hash/signer/native-assets；`KUYIN_R2_OBSERVER=true`只開App debug metadata及失敗現場screenshot，不是所有shell採證的開關。腳本拒絕非CI、多device、physical／非qemu及user≠0；ime/settings明確user0，App install後與每次IME mutation前重验user。AOSP13 IMMS的`ime enable/set --user`介面已有直接source證據；此CI profile不能冒稱通用OEM/實機安全runner。

diagnostic Exception先保存，再執行原assert；若assert失敗，同一AssertionError附suppressed原diagnostic error。assert通過但diagnostic失敗也會顯示fail。Shell final capture/terminal寫入／PRESENT-ABSENT probe錯誤不能回傳成功，原App非零exit保留且不跑Host。沒有新增JUnit status事件或硬體Enter注入。
