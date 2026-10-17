# KU-Yin 續作短入口

2026-10-17，當前R2.2 IME remote-binding因果隔離。先讀[CURRENT_STATE](../CURRENT_STATE.md)、[ARCHITECTURE](../ARCHITECTURE.md)、[R2.2交接](../testing/R2_2_FINAL_HANDOFF.md)與[逐界線裁定](../testing/R2_2_RUNTIME_FINDINGS.md)，不重讀整批歷史。

- main `63c9e4ab8ad7e277013acb5d7cce99228ec1f827`；PR12 `d6bf2a68f68fe6ea0bd4bbbf79016a863abb29d1`仍Draft，PR10 `ea5c2b5b3404508c8ab6b0a69642b3a54a8cb565`仍HOLD。release v0.2.0-aie.1/source2ce6810不變，不merge／release。
- R2.2工區 `.worktrees/r2-2-ime-binding`、branch `fix/r2-2-ime-binding`、base `2b8e8e4efff73741319fb00daf4cb4e58101e4e8`；source commit `3115002da5fbb5e0344e6bb3b2068dc9c145801b`已正常push，僅六個scripts／tests／workflow檔。全部Android／JNI／pin／appID／signing／schema／frozen runner與manifest不動。產品source39acf96；文件HEAD與runtime source分開。
- 原firstVisibility失敗run38825146067 attempt2仍有效：App4PASS1FAIL、focus/IC具備、timeout remote/session null，rootUNRESOLVED。normal am instrument也force-stop/restart target；AGP exactworker將target/test送installer，但configuration不是actual replacement proof。
- 本輪fixed6files Python151PASS／contracts10、有效assertion RED、source/hashguard及獨立PASS_FOR_BOUNDED_EXPERIMENT。僅允許一次A/B；不是root／runtime／fullacceptance／merge批准。原20工區HEAD/status/diff/cached及公開AIEAPK/v2/v3三指定資產exacthash保存。
- 唯一paired CI [39005467151](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39005467151) exact3115002；shared四APK一次建置，A original install-select-connected App-only，B exact App/AppTest安裝後select/direct instrumentation；B App5全通後才Host33／32case，再原B01。唯一run最終FAIL；A/B均exact5＝4PASS1FAIL0SKIP，原kt81firstvisibility；Host33／matrix32／B01未執行。237source核對及sharedAPKguard成功。不盲retry；C不啟動，因兩組install前已SystemUI ANR。
- 關鍵時間窗採API33 server-only IMMS proto，fulltext dump會呼叫client checkFocus。proto無remote method/session欄位，缺bool不是false；currentuser0另明讀。primaryprotocol先記，secondarycapture錯誤分開；wronguser拒跨client採證。
- external `/Users/huang/Desktop/KU-Yin-R2.2-Verification-2026-10-16/`，先讀RUNNING_CONTEXT_ANCHOR.json及latestCI收據。151／static source28、29／30，preservation36；watch33因本機networktimeout失敗，不是CIFAIL，已接回39同run。不得重跑另一workflow冒充此實驗。
- R2/R2.1歷史見[R2.1交接](../testing/R2_1_FINAL_HANDOFF.md)。B02/B03 native context、C learning observer未批准；C04保留真nullIC，D17 oneway同步false不可造證。新observer／降acceptance scope需另決策。
- Physical NOT_TESTED／production NOT_READY／M4 NOT_VALIDATED／AIE-S2 NOT_STARTED。OCR本輪NOT_RUN、R2.1 health被approvalreview拒未執行；歷史401非fresh。無LSP/init/compact API，不改AutoPilot/globalconfig。本輪已回收A/B raw及handoff。下一最小任務是健康emulator precondition／SystemUI ANR有限調查；先failclosed環境錯誤，不dismiss／clear／timeout補丁，再續原binding隔離。

- 新raw在install前已有可見SystemUI ANR（keyguard service timeout），failure windowfocusfalse/IC0；與舊R2.1focus/IC1/show前提不同，分類EMULATOR_ENVIRONMENT，原remote根因UNRESOLVED。before-app selectedKU不是bound/session；B仍boundLatinIME。沒有rootfix RED/GREEN。
- source safety／平台研究／current5/33/32契約md/json均已寫成並核hash，僅各自scope有效。新runtime獨立review因quota未完成，final gate OPEN；不重試相同額度或宣稱runtimeAPPROVE。
- external40 exactCI metadata、41原252檔artifact、42完整joblog、44-analysis／兩組protocolreparse、45-artifacthashmanifest。A/B皆FAIL／原App重現表象但環境更早阻擋，不合併舊24PASS與本輪結果。最終docsHEAD／remote／保存收據見R2_2_FINAL_RECEIPT.json。
