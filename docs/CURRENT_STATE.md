# CURRENT_STATE

ACTIVE；更新2026-10-12 R1。只記目前事實，歷史測試見review/INDEX與原Gitcommit。

- 本輪起始GitHub main `a6a4c654845338521700121ac06586887d1ec085`；PR#8正常merge；tag `v0.2.0-aie.1`仍指 `2ce6810684fd65a65a3006a224dc786b1f3a5f62`。產品source39acf96；公開experimentalDebug APK20003/0.2.0-aie.1，SHAacdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634。R1不重做S0/S1、不發布新產品。
- Chinese為native production/startup fallback；P0基本中文WORKS_ON_USER_DEVICE（使用者回報，非全matrix）；Englishoffline10kMVP。
- S0完整reset disabled/unsupported、零資料刪除；S1單列橫捲＋展開grid、scratchplanpaging、snapshotguard。ranking/touch/catalog/S2未實作，M4 NOT_VALIDATED。
- 歷史CI38164320915 blockingPASS，API33x86_64 app5/5；host32=15PASS/9FAIL/8SKIP。fixturegeometry/stale與舊B契約需隔離adapter，timeout根因不能全數推定產品。新的結果僅登記maintenance/VALIDATION_MATRIX.md。
- Full physical acceptance OPEN；production NOT_READY，缺正式keystore BLOCKED_BY_SIGNING；erase/migration/OEM/TalkBack/license runtimeinventory仍OPEN。
- R1在隔離chore/mainline-consolidation-r1：來源/工作樹資料盤點、verified外部backup、規範架構10章、文件權威/data政策、doctor小修。Hostadapter另fix/r1-host-fixture-adapter，不改product、不清資料。
- 最新進度／最終SHA/PR/測試：[FINAL_HANDOFF](maintenance/FINAL_HANDOFF.md)，fresh/historical分層：[VALIDATION_MATRIX](maintenance/VALIDATION_MATRIX.md)。

## R1 收束與尚未合併的驗收修正

2026-10-12：文件／doctor／CI治理 PR #9 正常合併 `bf7c5d1d912d77ae8c6c7179d26d72bf835232bc`；產品 source、native pin、簽章與資料 schema 不變。`.worktrees/mainline` 持有 local main，已正常fast-forward；原15工區及4dirty保留，18工區總數含本輪3個。

本輪 fresh baseline emulator run38224799842：App5PASS；Host15PASS／9FAIL／8SKIP。隔離 Host PR #10 未合併：087ff39 run38225649247 Host15PASS／17SKIP；ea5c2b5 run38254806233 blocking buildPASS，但App runtime4PASS／1FAIL（input window visibility），Host未執行。根因UNKNOWN，不能稱emulator flaky或由fixture造成；不跳過App層、不盲retry、不改產品／visibility assertion。Host adapter後續device-free成本修正另列local證據，仍須完整新runtime gate。

詳見[本輪驗證](maintenance/VALIDATION_MATRIX.md)與[交接](maintenance/FINAL_HANDOFF.md)。Physical OPEN／Production NOT_READY／M4 NOT_VALIDATED／S2 NOT_STARTED不變。

## R2 test-infrastructure與驗收界線（2026-10-15）

main63c9e4ab8ad7e277013acb5d7cce99228ec1f827、PR10remote ea5c2b5 HOLD不變；Draft PR12已驗證程式checkpoint為03ac3264b4178afccb5781fac6445920bf41dc3a；其後文件HEAD以PR／外部收據核對，不當新runtime證據。只改debug editor／測試／CI／文件；產品／Host main／native pins／signing／schema不變，Host test UiAutomator2.4。新after-focus one-shot與raw dump stdin都有RED/GREEN及獨立窄審查；historical ea5完整因果仍UNRESOLVED。

正常CI38661678107 exact synthetic89f98a9（tree＝PR HEAD03ac）：App5PASS；Host33＝24PASS／0FAIL／9SKIP，matrix32＝23PASS／9INCONCLUSIVE＋infra1PASS。舊9FAIL七案真PASS，B02/B03可見assertions完成但nativecontext gap仍INCONCLUSIVE；舊8SKIP只有ZH06轉PASS。剩餘scope與C04nullIC／空tree詳[R2逐案裁定](testing/R2_HOST_CASE_DISPOSITION.md)，不能稱full acceptance。

本地59b App282含native10真PASS；正常CI App272PASS10nativeSKIP、Host107真PASS，Python130／contracts10／bootstrap／Debug buildPASS。既有KSP/AWT error annotation仍記錄，pin2.3.5未改，不掩蓋工具鏈問題。18原工區保存再驗一致、現19，exact公開APK及release tag不變；未讀刪user DB／字典／prefs。

最新[R2交接](testing/R2_FINAL_HANDOFF.md)／[驗證矩陣](testing/R2_VALIDATION_MATRIX.md)與CODEX_CONTEXT_ANCHOR為續作入口。PR10 HOLD／PR12 Draft；完整觀測gap與historical disposition待處置，physicalOPEN、productionNOT_READY、M4NOT_VALIDATED、S2NOT_STARTED不變；不自動merge或release。

## R2.1 acceptance evidence closure（2026-10-16）

限定test-only B01 geometry及九gap研究；隔離test/r2-1-acceptance-evidence，程式checkpointb1b5bf6b3939fef7e62893d833b0853e0a08ae7d。Production Kotlin/JNI、Host main、signing/pins/schema與user data不動。main63c9e4a、PR12d6bf2Draft、PR10ea5HOLD、公開release不變。

最終R2d6 fresh核CI38761725033／actualmerge3b5e084（完整tree同d6）：App5PASS；Host24PASS0FAIL9SKIP，matrix23PASS9INCONCLUSIVE＋infra1PASS；finite38761725004只5＋2。原始identity/protocol/owner與獨立review均核實，但不批准fullacceptance/merge。C04只有81completed彙總與最後node0，不能說81份逐筆全空。

新B01 local App282／Host123／Python131／contracts10及Debug/instrumentation gates通過；獨立source批准僅供有限CI。run38825146067 attempt1在SDK NDK archive準備失敗，未跑測試；同SHA/設定/pin唯一重跑attempt2 App4PASS1firstVisibilityFAIL，Host/B01未執行；185source與完整protocol／五真failure dumps已核。show前focus/IC具備、timeout後remote method/session null；根因UNRESOLVED，不沿用舊early-show、不再retry。不得把compile或job綠燈算runtime。

B02/B03 native context與內部reset oracle不足、C nativelearning、C04真nullIC、D17 callbackfalse/oneway契約詳[R2.1ledger](testing/R2_1_ACCEPTANCE_GAP_LEDGER.md)／[platform](testing/R2_1_ANDROID_PLATFORM_CONSTRAINTS.md)／[observer ADR](testing/R2_1_NATIVE_OBSERVABILITY_ADR.md)。新observer需另批准，未降scope、未去skip。Physical NOT_TESTED、productionNOT_READY、M4 NOT_VALIDATED／AIE-S2 NOT_STARTED；下一入口[R2.1handoff](testing/R2_1_FINAL_HANDOFF.md)。

## R2.2 IME binding isolation（2026-10-17）

隔離 `fix/r2-2-ime-binding`，source checkpoint `3115002da5fbb5e0344e6bb3b2068dc9c145801b`、base2b8e8e4；只改scripts/current協定manifest／fake tests／manual CI／文件，全部Android source與產品不變。source safety與current5/33/32 ownership限定審查已完成；原remote根因仍UNRESOLVED，未把採證改進當產品fix。

本輪local151 Python PASS／contracts10 PASS與六source固定hash已核。唯一paired run39005467151 exact3115002最終FAIL：A/B均exact5＝4PASS／1FAIL／0SKIP，原kt81first visibility斷言；Host33／matrix32與B01 NOT_TESTED。237source hashes一致，shared四APKguard及兩arm全部phase metadata相符；沒重跑。

兩組在KU-Yin安裝前已可見SystemUI keyguard service ANR，失敗時windowfocus=false／IC0、after-focus show未觸發。舊R2.1則focus/IC1/show已具備，故本輪分類EMULATOR_ENVIRONMENT，不能解除原remote root。C不啟動，先需健康emulator前提調查。新runtime獨立review因quota未完成，不能宣稱APPROVE。

原20工區／3指定凍結資產保持。main63c9e4a、PR12d6bf2Draft、PR10ea5HOLD、release不變；無product/JNI/data變動、實機／merge／release／newobserver／S2／M4。續作：[R2.2最終交接](testing/R2_2_FINAL_HANDOFF.md)、[逐界線裁定](testing/R2_2_RUNTIME_FINDINGS.md)、[驗證矩陣](testing/R2_2_VALIDATION_MATRIX.md)。
