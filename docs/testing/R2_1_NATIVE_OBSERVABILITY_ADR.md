# R2.1 Native 觀測 ADR

**PROPOSED／NOT_IMPLEMENTED／需使用者另行批准。** 基線d6bf2a6；本輪B01不含本提案。

## 問題與來源

外部Host不同UID，不能讀private `KuYinInputMethodService.nativeEngine`。`AndroidChewingEngine.personalizedLearningEnabled` 已透過既有JNI getter讀autoLearn；ctx=0卻也回false，所以false不能單獨證明native learning disabled。不需為讀既有boolean增加JNI export。

B02/B03的live native context不能用Kotlin物件identity或pointer前後相同推斷。`AndroidChewingEngine.restoreAfterUnavailableSelection` 直接呼叫native reset/replay，可能避開 `NativeCase4Test.ResetCountingEngine` 的外層reset計數；該test selection後才取baseline，也未拒unavailableReason。**High是no-reset測試證明力缺口，不是已重現產品reset defect。** 既有10個host-native測試不補足live service observer。來源：`AndroidChewingEngine.kt:169–183,435–444`、`NativeCase4Test.kt:14–19,58–74`、`ChewingEngineSession.kt:64–69`（行號對d6）。

## 最小介面提案

唯讀、bounded數值snapshot：`runNonce`、`caseNonce`、`editorEpoch`、`serviceGeneration`、`provider`、`ready`、`liveContextPresent`、opaque `liveContextGeneration`、live create/delete/reset累積計數、`autoLearn=ENABLED|DISABLED|UNAVAILABLE`、inputType/imeOptions、policy/effective/preferred mode、lifecycle phase。native pointer/address、key history、preedit、candidate、字典內容／路徑均禁止輸出。

live與scratch獨立：所有live init/reset/internal restore/close路徑計數；scratch `:memory:`正常create/delete不算live reset。觀察窗口在onStartInput/onStartInputView合法reset完成後開啟，候選tap前後及next key後同epoch無缺漏；provider必須native/ready，不能fallback=false當成功。counter僅證adapter涵蓋呼叫，不能證libchewing所有內部語義。

跨UID出口僅能位於debug source/manifest；explicit endpoint、signature permission、明確Host package/signer驗證、短效synthetic nonce、bounded ring/overflow可見。fresh disposable emulator與opt-in是場景限制，不是認證。service private instance沒有現成安全registry；若需極窄main事件sink，**仍屬production seam，必須批准後再實作**，不能先加hook或用反射宣稱純test-only。JNI ABI/pins保持不變。

## 個別驗收

- B02：baseline在首key前，候選gesture/next key後live generation不變、reset/create/delete delta0，並保留現有真editor恰1commit／prefix／action斷言。
- B03：同上，還須另外定義原candidate correction→Enter語義；context穩定不是整句正確的充分條件。
- C01/02/03：真收到當次EditorInfo，settled start-view後getter=false；C05兩種preferred模式每一transition及return TEXT按policy讀回。
- C04：必須有當次editorEpoch，不能借前案flag；先保留真nullIC與binding缺口。

**配置getter不是persistence proof。** 真零寫入證據需獨立synthetic temp store與supported native write observer；不讀／hash／reset真userdict、不以UI英文或字典hash未變當關閉學習證明。本輪未實作學習／字庫刪除或啟用測試學習。

## 後續gate與rollback

先審查上述窄介面及認證，再TDD：missing/stale epoch、ctx0/fallback、ring overflow、internal reset、release absence均有negative tests。需證release manifest/DEX/native symbols無觀測端點、無network／raw內容；所有保留的real UI/editor assertions要重跑。回退整個observer commit，不migration／不改user data。未批准前維持B/C INCONCLUSIVE。

官方：[Application Sandbox](https://source.android.com/docs/security/app-sandbox)、[EditorInfo no personalized learning](https://developer.android.com/reference/android/view/inputmethod/EditorInfo#IME_FLAG_NO_PERSONALIZED_LEARNING)。獨立source詳細證據在external「原生與編輯器八項缺口獨立研究.md」與exact Android13來源收據，不把研究當runtime。
