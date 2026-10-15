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

每次`runtime_smoke.sh`明示執行都收集CI user／device／package／service/window快照與APK hash/signer/native-assets；`KUYIN_R2_OBSERVER=true`只開App debug metadata及失敗現場screenshot，不是所有shell採證的開關。腳本拒絕非CI、多device、physical／非qemu及user≠0；ime/settings明確user0，App install後與每次IME mutation前重驗user。AOSP13 IMMS的`ime enable/set --user`介面已有直接source證據；此CI profile不能冒稱通用OEM/實機安全runner。

diagnostic Exception先保存，再執行原assert；若assert失敗，同一AssertionError附suppressed原diagnostic error。assert通過但diagnostic失敗也會顯示fail。Shell final capture/terminal寫入／PRESENT-ABSENT probe錯誤不能回傳成功，原App非零exit保留且不跑Host。沒有新增JUnit status事件或硬體Enter注入。

## 受控runtime trace

run38459179007實際source698d107c：A original smoke與B/C observer均App5PASS／0SKIP；新Activity實例證明recreate執行。B final EditorInfo／IC與window focus在request前已建立，IMM false仍後續fromIme=true＋WindowInsets visible；C initial window在request前已有visible。不同query在不同timestamp，不能把瞬時service snapshot當所有callback計數或原ea5失敗時刻。

三組API33/default/x86_64/freshpixel7/user0、same source pins／dictionaryassets；產品source一致，但native ELF/whole APK hashes不同，不称byte-for-byte binary control。實際APK/source/signer metadata都保存于外部controlled-runtime各variant/build/r2-runtime-evidence。source identity與artifact identity分開。

當時原首次visibility失敗尚未重現；後續same-source重現見下節，historical ea5完整因果仍UNRESOLVED。新Host trace只记錄fixed getter phase／owner／numeric metadata，見[R2有限探測](R2_HOST_OBSERVATION_PROBE.md)。

## 2026-10-15 同source重現與focus前置修正

3fc run38563975932 App4PASS1firstVisibilityFAIL，Host未執行；與38563163760相同181 source manifest／catalog/API33/user0/fingerprint。CI signer／wholeAPK hash不同，不能稱binary-identical；16項native/assets hash相同。

失敗：102791ms Insets request、102797ms IMM=false/not-served（windowFocus=false/IC0）；102807ms focus=true、102808ms真IC1；112817ms原期限到仍false。成功輪request前已focus/IC/visible，反證不能把成功單純歸新post。舊ea5曾served，完整IMMS／historical因果仍UNRESOLVED。

採證缺口：LIVE_FAILURE_DUMP markers有五種kind，但只有first-screen.png；Android13 UiAutomationConnection使用Runtime.exec，不解析`>`，stdout原被丟棄。PNG確認無鍵盤；post-cleanup dump不可替代live時service/input_method。原markers不作raw證據。

51兩個真precondition assertion RED；51b另含destroy有效RED，但loseFocus case的queue順序不合格。52/52c/52d保留：它先post(show)後enqueue(false)，controller.idle先執行合法show，clearLogs把它抹掉；不能要求focus恢復後重播已完成request。

52e以真ViewRoot先enqueue(false)但不drain，assert焦點尚true；再request，idle後assert焦點false且0show；regain後原Insets→IMM各1，八測全部GREEN。實作只debug one-shot pending/posted，after-focus post當下重驗attached/token/view/windowfocus與destroy，失焦保留pending。原40×250ms visibility assertion不變；原立即Insets＋postedIMM有意改為同一after-focus post的順序，不能稱原時序byte-identical。當時獨立review只批准此source切片，runtime尚待；後續有限結果見末節。

## 原始dump傳輸修復與證據界線

3895625使用API31+ `executeShellCommandRw("sh")`，透過stdin交給shell處理固定診斷命令與quoted owned destination；驗證exit0且實檔非空才寫persistence_verified。descriptor皆use關閉，失敗仍保留在原assertion boundary。沒有API34限定clearCache或假造shell重導向支援。

Transport自檢在`VISIBILITY_RESULT stage=recreated visible=true`後才執行：固定payload→檔案→讀回，標記`TRANSPORT_SELF_CHECK`。不改首次/recreate visibility判定、show/focus次數或期限。成功輪的self-check file只證明AndroidRW傳輸；不能冒稱同輪採到5個失敗dump。沿用pull目錄`live-failure`亦可能只含self-check，須按事件類型解讀。

53實際host Runtime.exec測試5測4有效AssertionError RED；54真shell GREEN。55最後selfcheck instrumentation真compile；59b App282真執行PASS。這些device-free結果不證明Android Binder RW，當時尚待exact3895625有限emulator38660489452；完成結果見末節。

## 38660489452有限runtime實測

exact3895625，App5與兩個Host strict cases皆PASS／零skip。初次request仍在112291ms windowFocus=false/IC0，fixture沒有發show；112302ms focus、112304ms IC1後，112313ms Insets、112315ms IMM才發出。**首次IMM回false**，其後IC2／platform show，113448ms原WindowInsets判true；不能稱focus/IC等同remote session ready或IMM一定accepted。recreate為新Activity generation，原assert true；113799ms recreated=true後113823ms才typedTRANSPORT_SELF_CHECK。

同一檔的兩份pulled25byte副本皆`R2_SYNTHETIC_TRANSPORT_V1`，證實API33 RW固定payload傳輸；沒有本輪五個真失敗dump。terminal original_exit0/evidence_exit0，183source hashes一致。same-source單輪成功與可見的延後dispatch證明窄機制執行，完整ea5因果及長期可靠率仍未確立。

## 正常完整CI的觀測界線

38661678107／actual89f（tree＝PR03ac）App原5全PASS，observer OFF，原assert及recreate執行；不能捏造此輪被動focus／IC事件，細時序來自前述有限probe。Host32matrix envelopes均完成protocol，但23PASS／9INCONCLUSIVE，不是全驗收。正常runtime APK／signer hash依該VM runner實檔metadata單獨記錄，不能套用有限probe／本機簽章。
