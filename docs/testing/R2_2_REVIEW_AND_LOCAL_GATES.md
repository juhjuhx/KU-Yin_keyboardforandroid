# R2.2 獨立審查與本地驗證

Source checkpoint `3115002da5fbb5e0344e6bb3b2068dc9c145801b`，六個程式／測試檔；Android source未改。外部`R2_2_EXPERIMENT_SAFETY_REVIEW.md/json`親核actualdiff、rawRED/GREEN、sourcehash與三個凍結資產，批准只限固定六檔localcommit／正常testbranchpush／一次manualpairedA/B。沒有runtime/root/fullacceptance/merge/release批准。Reviewer最後聊天因quota中止，但事前寫成的完整md/json與source/evidence SHA均已由主agent逐一驗證，沒有把quota失敗當新approval。

獨立平台角色另讀exactAPI33片段與AGP9.1.1 cachedbytecode，資料在`R2_2_INDEPENDENT_SEQUENCE_RESEARCH.md/json`及`R2_2_ANDROID_PRIMARY_SOURCE_RECEIPT.json`；不預設Gradle reinstall根因。

| gate | fresh結果／exactscope |
|---|---|
| 原runtime fake tests baseline | 14 PASS（03） |
| 原source新3regressions | 12有效assertionRED，exit1，舊runtime只在temp副本，未改原工區 |
| parser/critical observer/primary safety | 05/08/13/20b/24/27真assertionRED；20watchdogERROR不是RED |
| 全Python最終29 | 151 PASS，exit0，103.654s，六檔snapshot28全程unchanged |
| previous22/26 | 150/151PASS各自舊snapshot，不冒稱finalbytes |
| static23/30 | 10contracts，shell/YAML/docs/diff通過；最後guard後再核source固定 |
| protectedAndroid/frozen source | 相對2b8 diff0，appID/ABI/pin/簽章/schema不變 |
| 三指定凍結artifact | 公開AIE APK acdbacdf…／v3 ZIP4dffdeb…／v2 ZIP787d44f…，主agent及reviewer fresh hash皆不變；非全archive盤點 |

已關閉的experiment缺陷：freshmissingpackage誤判、完整IMMS的clientcheckFocus介入、secondarydump擋primary、wronguser App-only與EXIT跨client採證、App/Host phase重用、B繞過hashgate。這些不是已證產品typing/rootdefect。

Run39005467151 raw現已回收：A/B均exact5＝4PASS1FAIL0SKIP，Host/B01 NOT_TESTED。兩組在install前已有SystemUI ANR且未取得windowfocus/IC，與舊R2.1不同。詳R2_2_RUNTIME_FINDINGS；獨立runtime review因quota未完成，不能以既有source approval代替。A/B有installer/transport/順序load confounds；samejob sameAPK不代表原歷史run同binary。Host若有SKIP/INCONCLUSIVE不轉PASS；proto缺remote/session不假null。R2.1firstvisibility FAIL保留；PR12Draft、PR10HOLD，無實機/merge/release。OCR本輪不重試；歷史401與前輪auto-review阻擋均不冒稱本輪provider結果。

current5/33/32 manifest另由平台reviewer完整md/json重建核對，所有source/report hash由主agent驗證，沒有High identity/owner錯配；其最後聊天quota失敗發生在報告寫成後，僅承認該已寫source-contract scope。Medium CLI0可缺emit/INCONCLUSIVE、Low metadata非evaluate自驗明列；source相符不等於runtime批准。後續raw沒有再取得獨立審查，final gate仍OPEN。
