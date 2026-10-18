# R2.1 驗收缺口與實作計畫

ACTIVE；2026-10-16。基線 d6bf2a68f68fe6ea0bd4bbbf79016a863abb29d1，main63c9e4a。先核最終CI原始XML/protocol，再做B01。Production Kotlin/JNI、private data與凍結資產不動；完整驗收不因SKIP改名通過。

## 深讀：實際責任

`DachenAcceptanceTest.kt` 的B01已驗41鍵ASCII/hitbox/排序，真ㄕ/ㄖ與strict preedit，clear前後editor文字、commit/action不變；最後明確INCONCLUSIVE，尚無第二寬度/方向資料。`AcceptanceDriver`只操作可見真軟鍵，immutable UI snapshot與`EditorSnapshot`為不同oracle。Host Manifest已宣告`orientation|screenSize`，Activity的recorder不因這兩項configChanges重建；不需要改Host main。`AcceptanceEditorActivity`已有host_orientation/display_rotation診斷；`ExternalUiObservation`有界讀完整product tree。

B02/B03真候選/commit/繼續輸入已觀察，external editor不能直接證明service native context identity或語義。C的UI/EditorInfo證據不等同JNI learning getter或persistence。C04真TYPE_NULL/nullIC不能強行換成fakeIC。D17 host false不是IPC同步返回值。各內部observer僅提出ADR，不在本輪實作。

## B01 已授權邊界與步驟

- [x] source/19工區盤點，先存external快照；建立隔離`test/r2-1-acceptance-evidence`。
- [x] final R2 CI build/App5/Host33/finite5+2逐一核XML、protocol、actualmerge與SHA，不以workflow成功推PASS。
- [x] 在既有B01內補portrait→landscape→portrait；只對Host Activity request orientation，finally還原original requestedOrientation，不寫Secure Settings或全域rotation lock。需實測actualconfiguration、displayrotation、window/IME bounds、41個可見hitboxes與兩個不同width，不認request成功等同實際轉向。
- [x] 保留原ㄕ/ㄖ strict preedit與clear零effect，並實作每方向的synthetic preedit／候選snapshot／editor前後採證（尚未runtime）；跨transition保留組字狀態僅觀察，不新增preservation產品契約。
- [x] d6原B01 envelope重現missing evidence；pure geometry反例有效RED→GREEN，編譯/回歸完成，未用missingAPI編譯錯誤當RED。
- [ ] emulator B01真執行：BLOCKED，App前置4PASS1FAIL後Host未啟動；不得跳前置。
- [x] 原B01名稱/identity與frozen26 manifest保留；只更新當前B01內容，不增刪/跳過測試或延timeout。若orientation未生效記INCONCLUSIVE；真輸入違反原斷言記FAIL並保留source。
- [x] 獨立source review與本地atomic commit完成；新App真FAIL與Host未執行另作evidence review。PR12仍Draft/HOLDmerge、PR10HOLD，不自動merge。

## 本輪最終範圍裁定

B01=IMPLEMENTED_BLOCKED_BY_APP_VISIBILITY；B02/B03=NATIVE_OBSERVER_ADR_REQUIRED；C01/C02/C03/C05=LEARNING_OBSERVER_ADR_REQUIRED；C04=INCONCLUSIVE_PLATFORM_CONSTRAINT_DOCUMENTED；D17=CONTRACT_DECISION_REQUIRED。未取得新runtime前九案全維持INCONCLUSIVE。官方平台來源及逐案完整欄位已記錄於同目錄constraints/ADR/disposition。

## 安全與回退

只改acceptance-host androidTest/sharedTest/test與必要測試工具文件。Git baseline可回退整個切片；不動main/PR10/PR12 source、release或原worktree。外部 `/Users/huang/Desktop/KU-Yin-R2.1-Verification-2026-10-16/` 保存原始證據。使用者本輪已明確授權B01 bounded implementation，故不重問架構/UX；任何production/JNI observer仍需獨立批准。

## 九案逐項證據契約

下列CURRENT_EVIDENCE的d6 finalCI均指38761725033／actual merge3b5e084，已核原XML與protocol；不是R2.1新碼runtime。共用來源：Dachen/NonText/EditorActionAcceptanceTest、AcceptanceDriver；native細節見[observer ADR](R2_1_NATIVE_OBSERVABILITY_ADR.md)，平台見[constraints](R2_1_ANDROID_PLATFORM_CONSTRAINTS.md)。

### B01

- **ORIGINAL_REQUIREMENT**：41 canonical鍵、portrait/landscape與第二寬度；真ㄕ/ㄖ及preedit。
- **OBSERVABLE_SUBCONTRACT**：可見完整key/ASCII與hitbox；各方向strict preedit、固定我candidate/單次commit。
- **UNOBSERVABLE_SUBCONTRACT**：本案不證native identity；emulator不是實體旋轉／全OEM。
- **CURRENT_EVIDENCE**：d6 finalCI原B01可見子契約完成，最後INCONCLUSIVE；R2.1新增test-only oracle16測GREEN。
- **MISSING_EVIDENCE**：R2.1 exactsource geometry emulator run；physical width/rotation。
- **ANDROID_PLATFORM_CONSTRAINT**：requestedOrientation可忽略；必讀actual config/rotation與IMEwindow bounds。
- **PRODUCT_RISK**：窄／旋轉版面可能clip，未實測前不排除產品問題。
- **PROPOSED_OBSERVATION**：三個穩定layout frame，前後epoch；41hitbox、固定候選target/count、text/effect前後。
- **IMPLEMENTATION_SCOPE**：僅Host androidTest/sharedTest/test、既有finite CI/script；無UI幾何變更。
- **SAFETY_RISK**：不切全域rotation設定；finally還原Host原request；不記個人候選。
- **TEST_STRATEGY**：geometry negative RED9→GREEN；review分類/副作用RED9→GREEN；真App5前置→原B01 identity。
- **VERDICT**：R2.1 NOT_TESTED（App前置4PASS1visibilityFAIL，Host未執行）；完整仍INCONCLUSIVE。

### B02

- **ORIGINAL_REQUIREMENT**：真候選後繼續組字，同live native context、不reset／換context、無額外commit。
- **OBSERVABLE_SUBCONTRACT**：原candidate單次commit、prefix保留、nextㄅstrictpreedit均真執行。
- **UNOBSERVABLE_SUBCONTRACT**：live context/reset/create/delete與native state連續性。
- **CURRENT_EVIDENCE**：d6 finalCI可見子契約完成，context gap INCONCLUSIVE。
- **MISSING_EVIDENCE**：同editor epoch、同live服務instance的完整操作窗口counters。
- **ANDROID_PLATFORM_CONSTRAINT**：不同UID無公開API讀private服務；pointer可重用且非安全identity。
- **PRODUCT_RISK**：native reset漏觀測；舊NativeCase4 wrapper oracle有High證明力缺口，不是已確認產品defect。
- **PROPOSED_OBSERVATION**：opaque generation／live reset/create/delete counters，scratch獨立；拒fallback/unavailable。
- **IMPLEMENTATION_SCOPE**：ADR only；涉及main seam需另批准，不改JNI。
- **SAFETY_RISK**：禁止pointer/文字/字典內容；debug出口必須認證且release absent。
- **TEST_STRATEGY**：negative internal reset／unavailable／stale epoch／fallback，加原soft-key/editor assertions。
- **VERDICT**：INCONCLUSIVE；OBSERVER_APPROVAL_REQUIRED。

### B03

- **ORIGINAL_REQUIREMENT**：連句conversion/candidate correction→Enter／續輸入；native context與整句語義。
- **OBSERVABLE_SUBCONTRACT**：原兩音節preedit／未提前commit、候選整段單次commit、續ㄅ保留prefix。
- **UNOBSERVABLE_SUBCONTRACT**：內部context／無reset；candidate correction及Enter原始語義關係。
- **CURRENT_EVIDENCE**：d6 finalCI可見子契約完成，完整語義gap INCONCLUSIVE。
- **MISSING_EVIDENCE**：live observer及對原B03 correction→Enter契約的記錄決定。
- **ANDROID_PLATFORM_CONSTRAINT**：context generation不變不代表正確詞句；外部Host無native state讀口。
- **PRODUCT_RISK**：僅length≥2/第一候選不足證全部句子語義。
- **PROPOSED_OBSERVATION**：同B02 counters＋固定synthetic correction sequence與最終editor期望。
- **IMPLEMENTATION_SCOPE**：ADR only；不改decoder／ranking或暗用D13/D14替代B03。
- **SAFETY_RISK**：不得記真句子，僅固定synthetic語料。
- **TEST_STRATEGY**：保留原strict assertions，對錯詞／多commit／internalreset加反例。
- **VERDICT**：INCONCLUSIVE；OBSERVER_AND_CONTRACT_DECISION_REQUIRED。

### C01

- **ORIGINAL_REQUIREMENT**：NUMBER inputType2，temporary ENGLISH、無注音composition、native learning disabled。
- **OBSERVABLE_SUBCONTRACT**：實際EditorInfo及UI模式／注音阻擋。
- **UNOBSERVABLE_SUBCONTRACT**：live JNI autoLearn與實際learning persistence。
- **CURRENT_EVIDENCE**：d6 finalCI NUMBER可見assertions完成，native observer缺口。
- **MISSING_EVIDENCE**：native ready/live present＋settled getter=false；零寫入更強要求另隔離store。
- **ANDROID_PLATFORM_CONSTRAINT**：Android不替NUMBER自動關libchewing，必由產品policy套用。
- **PRODUCT_RISK**：UI英文不能證autoLearn關；ctx0 getter false會假陽性。
- **PROPOSED_OBSERVATION**：tri-state autoLearn＋provider/ready/editorEpoch，不輸出詞彙。
- **IMPLEMENTATION_SCOPE**：ADR only；不接觸真userdict。
- **SAFETY_RISK**：真個人字典不可讀hash/reset，synthetic isolated store另核。
- **TEST_STRATEGY**：fake policy只作unit；live service/JNI真readback才可補此層。
- **VERDICT**：INCONCLUSIVE；LEARNING_OBSERVER_REQUIRED。

### C02

- **ORIGINAL_REQUIREMENT**：PHONE inputType3，其餘同C01。
- **OBSERVABLE_SUBCONTRACT**：實際PHONE metadata、temporary ENGLISH與阻擋。
- **UNOBSERVABLE_SUBCONTRACT**：同C01 native設定／持久化。
- **CURRENT_EVIDENCE**：d6 finalCI PHONE可見assertions完成。
- **MISSING_EVIDENCE**：當次PHONE editor epoch真readback。
- **ANDROID_PLATFORM_CONSTRAINT**：不能拿NUMBER的結果替PHONE。
- **PRODUCT_RISK**：setter呼叫意圖與當次native狀態可能不同。
- **PROPOSED_OBSERVATION**：同C01，獨立PHONE transition。
- **IMPLEMENTATION_SCOPE**：ADR only，保留fake service test與真runtime層界線。
- **SAFETY_RISK**：同C01無詞庫存取。
- **TEST_STRATEGY**：各自PHONE→TEXT transition及readback；不可只驗模式。
- **VERDICT**：INCONCLUSIVE；LEARNING_OBSERVER_REQUIRED。

### C03

- **ORIGINAL_REQUIREMENT**：DATETIME inputType4，其餘同C01。
- **OBSERVABLE_SUBCONTRACT**：實際DATETIME metadata、temporary ENGLISH與阻擋。
- **UNOBSERVABLE_SUBCONTRACT**：同C01 native設定／持久化。
- **CURRENT_EVIDENCE**：d6 finalCI DATETIME可見assertions完成。
- **MISSING_EVIDENCE**：當次DATETIME editor epoch真readback。
- **ANDROID_PLATFORM_CONSTRAINT**：不能拿PHONE/NUMBER替代。
- **PRODUCT_RISK**：UI/EditorInfo正確不證nativeflag。
- **PROPOSED_OBSERVATION**：同C01，獨立DATETIME transition。
- **IMPLEMENTATION_SCOPE**：ADR only；不變更學習政策。
- **SAFETY_RISK**：同C01無詞庫存取。
- **TEST_STRATEGY**：各自DATETIME→TEXT transition及readback，保留嚴格阻擋斷言。
- **VERDICT**：INCONCLUSIVE；LEARNING_OBSERVER_REQUIRED。

### C04

- **ORIGINAL_REQUIREMENT**：真TYPE_NULL=0，保留super nullIC；可觀察才測mode/無組字/learning。
- **OBSERVABLE_SUBCONTRACT**：當次platform/final TYPE_NULL與nullIC callback。
- **UNOBSERVABLE_SUBCONTRACT**：當次IME service binding/start/view、temporary mode及native狀態。
- **CURRENT_EVIDENCE**：d6 finalCI BLOCKED_SELECTOR；真nullIC之後缺可用softkey evidence。
- **MISSING_EVIDENCE**：被動focus/show/lifecycle當次epoch trace；不能借前case flag。
- **ANDROID_PLATFORM_CONSTRAINT**：TYPE_NULL不是rich editor；IMM可能另有fallback IC；null host不等於IME current null，不保證可見。
- **PRODUCT_RISK**：根因未定，不能宣稱全平台禁止或產品PASS。
- **PROPOSED_OBSERVATION**：記原有show/focus結果與Insets，不多呼叫、不造IC；必要service seam另批准。
- **IMPLEMENTATION_SCOPE**：本輪官方研究；不修改Host main、service或等待期限。
- **SAFETY_RISK**：不查surroundingText、不forceIME、不用hardwarekey取代softkey。
- **TEST_STRATEGY**：保持真null、分別證received/lifecycle/window；缺證即INCONCLUSIVE。
- **VERDICT**：INCONCLUSIVE；PLATFORM/PRECONDITION_OBSERVABILITY_GAP。

### C05

- **ORIGINAL_REQUIREMENT**：兩preferred經nontext再回TEXT恢復；native learning依policy恢復。
- **OBSERVABLE_SUBCONTRACT**：preferred ZHUYIN/ENGLISH真恢復，C04 transition另留缺口。
- **UNOBSERVABLE_SUBCONTRACT**：每nontext/returntext實際native readback；C04中途mode本身。
- **CURRENT_EVIDENCE**：d6 finalCI兩preferred迴圈可見assertions完成。
- **MISSING_EVIDENCE**：同editorEpoch learning false→TEXT按policy恢復。
- **ANDROID_PLATFORM_CONSTRAINT**：preferred ENGLISH不等同TEXT policy learning false。
- **PRODUCT_RISK**：只看UI可漏掉policy/lifecycle setter時序。
- **PROPOSED_OBSERVATION**：onStartInput與onStartInputView合法reset後settled numeric readback。
- **IMPLEMENTATION_SCOPE**：ADR only；不覆寫偏好或變更product policy。
- **SAFETY_RISK**：同C01，不能讀真learning store。
- **TEST_STRATEGY**：兩preferred每transition，reject舊epoch/fallback/ctx0，保留mode/restoration assertions。
- **VERDICT**：INCONCLUSIVE；LEARNING_OBSERVER_REQUIRED。

### D17

- **ORIGINAL_REQUIREMENT**：原P1矩陣為host回false、SEND只一次、無自動newline；目前另保留同步IPC false研究scope。
- **OBSERVABLE_SUBCONTRACT**：實際Host callback false、actionID4/count+1、text/commit/newline delta0。
- **UNOBSERVABLE_SUBCONTRACT**：API33 oneway不提供callback false同步回IME的通道。
- **CURRENT_EVIDENCE**：d6 finalCI可見原條件全執行；仍明確INCONCLUSIVE未改標。
- **MISSING_EVIDENCE**：需求owner對原可見驗收與不可提供IPC研究scope的明確裁定。
- **ANDROID_PLATFORM_CONSTRAINT**：AIDL oneway void；invoker boolean是transport不是editor消費回傳。
- **PRODUCT_RISK**：把hostfalse當IMEfalse可能誤判；不得造同步boolean。
- **PROPOSED_OBSERVATION**：提案維持外部單次action/零fallback可見契約，平台限制單列；需批准。
- **IMPLEMENTATION_SCOPE**：本輪研究與提案；不移除skip、不改原測試。
- **SAFETY_RISK**：不加同步IPC、不新增action或newline。
- **TEST_STRATEGY**：原真softkey/hosteffects照跑；可另unit false-return seam，但不能替代跨process。
- **VERDICT**：INCONCLUSIVE；CONTRACT_DECISION_REQUIRED。

## 本輪執行終態

source b1有限run38825146067 attempt1只SDKarchive FAIL；唯一重跑App4PASS1firstVisibilityFAIL，Host/B01未執行，無新geometry結果。停止後續source修正或retry，保留App完整FAIL、185source／5protocol／五真dump；見[新App binding阻擋](R2_1_APP_BINDING_FAILURE.md)。此輪不能補任何九案的fullPASS。
