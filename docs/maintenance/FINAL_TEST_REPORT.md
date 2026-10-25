# R3.8 原PR28歷史測試收據

本檔以下保留原PR28停止點，並非Issue31整合的當前裁定。R3.7數字是歷史；本輪新鮮驗證以PR exacthead、Actions job及中央收據為準。main-based Host來源來自22c2d66；readiness等價移植有單獨審查，不整包merge舊Guard/R2。

已取得本機行為證據：release policy5項先5 assertion RED再GREEN；secret scanner原6項＋merge-only private alias第7項均有負例RED→GREEN；完整stack（d450e18，包括下一個Readiness切片）Python141項PASS，當中16項readiness不計入這個Host/security/docs PR。第一個PR（含merge-only第7項scanner）單獨126項PASS，完整stack舊141項是d450時間點，不能混算。原Driver的20項/13FAIL RED及Host57GREEN屬R3.7歷史，需本輪重新執行才可列fresh。

整合首次全套122項含1 frozen/current ledgerFAIL及2 fake-tool10s逾時，收據保留；不增加timeout、未改該fake fixture。隔離相同fixture3項7.173s通過，之後序列化全套141項12.531s通過；未證明特定host resource是根因，不稱flaky修復。Frozen manifest未改，現在核對6595b684 immutable Git blobs，current test來源另列R3_8_INSTRUMENTATION_SOURCES.json；CI須完整history。

編譯／lint／CI最終結果需附exacthead收據後才能merge；Workflow總結不代替每個job。本輪不dispatchrun_runtime，不執行Emulator或實機；H0仍INCONCLUSIVE、H1/H2未執行、Host歷史15/9/8。10個native opt-in JVM SKIP不能當Native Runtime PASS。

最終GitHub CI、merge SHA與來源manifest由Issue19及FINAL_AGENT_HANDOFF連接；不在本文件預填未存在的未來merge SHA。

## 新鮮Gradle／Lint裁定

stack91f4940：App JVM265 total＝255PASS/10SKIP/0FAIL；Host57PASS/0SKIP。兩模組Debug及instrumentation assemble完成。聚合Gradle **exit1**：App lint4 errors/77warnings，Host lint0errors/6warnings。

4項：FeedbackHelper.kt:38/:41 MissingPermission(VIBRATE)、KuYinInputMethodService.kt:92 MissingSuperCall、values-night/themes.xml:16 NewApi(API27/min24)。app tree對main未改，屬既有source在本輪首次直接驗證出的阻擋；不新增suppress/baseline、不改visibility或權限湊綠。先前offline lint元件缺快取已正常解析，不能拿setup failure代替這次真lint FAIL。

CI新增獨立blocking lint job，保留完整report；--continue只讓兩模組都分析，最終失敗退出碼保留。新增job的policy負例有RED→GREEN。最終Reviewer在額度中斷前未核准最後delta：**REVIEW_INCOMPLETE，僅Draft PR，不合併main**。原143/127等最終數量以該來源fresh收據為準，不把舊141/126混作新結果。

## Issue31工程整合的獨立證據層

- 歷史PR34@ea186d77199b6c4a0198063c180b5cf683084869：CI40148115742 Build與blocking Lint PASS；Python127、App267 PASS/10 native SKIP、Host57 PASS；CI Lint App0errors/77warnings、Host0/7。Runtime SKIPPED。
- 新隔離工區、同ea186d7來源：Python127、12contracts、tracked/history credential tripwire、固定pin native bootstrap exit0；聚合Gradle冷建置exit124。Host XML57 PASS及Host Lint0errors/6warnings已產出，App／其餘未完成項不能列為本機PASS。原日誌保存在中央Workspace的R3.8-Integration31收據，不上傳私有Workspace。
- 文件標示修正後的最終integration HEAD須重新取得CI與完整diff獨立Integration Approval。[Issue31](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/31)及整合PR保留exact commit、job、XML／Lint artifacts及merge收據。不可用單一子分支或舊數字代替。
- 本輪沒有Emulator、裝置或Release；Host Runtime仍是歷史15 PASS/9 FAIL/8 SKIP。
