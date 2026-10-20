# CURRENT_STATE

## 當前：發佈文件收束／R3.1 checkpoint（2026-10-21）

ACTIVE。GitHub main 已由 README／社群 PR18 更新為 `575cc883288a920e7aba021fb8705f75f465e1c0`，相對原63c9e4a僅README／community文件；公開 Alpha/tag 不變。隔離 `fix/r3-1-h1-capture`：H1 code `97645c238dcecc43e677de625bf7049bd487d700`，有限 CI source `feab29f53583c4bb8dfc4cffe13243a36a3e9d06`；不改產品/JNI、原 App/Host assertions、signing 或資料。

- 原 H1 的 frozen-before 與 marker-only XML transport 分別記錄，不推定 freeze→timeout 單因。UIAutomator dump 只加控制終端 `shell -tt`；保留原 3s/10s、focus/IC/defaultIME/user/boot 與完整 XML gates。
- 最終 regression 對原 source meaningful RED；本機 health37、guard13、有限 workflow route4、contracts10 PASS。獨立來源審查及子行程 timeout 清理有直接證據；不代表 Android H1 PASS。
- 本機 full Python 保存結果遭 ENOSPC，未列 PASS。一次新 [H1-only CI39426302308](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39426302308) exact feab29f 最終 FAILURE：獨立執行完整 Python212／89.463s、contracts10與四APK/source guard PASS；H0 HEALTHY、H1 INCONCLUSIVE。ui-0有`-tt`但null root／無XML／3006ms timeout，after WMS frozen=true；原斷言fail closed，未重跑、未執行H2/產品/App/Host/B01。
- RC source guard 新增 b982 runtime pin、單獨 97645c health pin，其他 infra e137 保留；actual guard exit0。整個 PR20 獨立 review 的 inherited prerelease/signing gate 缺口仍 OPEN。
- 2026-10-21：磁碟已恢復約14GiB可用；未額外刪資料，不宣稱是本Agent釋出。十份原未提交文件已hash備份／還原驗證；fresh local Python212 PASS，原ENOSPC丟失結果仍不追認。歷史磁碟滿載的唯一目錄原因未能回溯證明。
- 實機/QRscan/保資料更新/正式key custody/D2D/license gate未完，RC=false、Stable=false、NO_GO、NOT_PUBLISHED；PR18已合併，PR10 HOLD、PR12/20 不自動合併。

續作入口：[R3.1交接](release/R3_1_FINAL_HANDOFF.md)、[根因](release/R3_1_ROOT_CAUSE.md)、[驗證](release/R3_1_RUNTIME_VALIDATION.md)。原Android根因仍UNRESOLVED；已依H1拒絕保存raw停止。下一最小工作是UIAutomator accessibility root與host window replacement的受控因果調查，不用health-only或歷史成功取代產品驗收。

## R3 RC closure 歷史 checkpoint（2026-10-19）

本節為HISTORICAL；以下R1/R2亦是各自時間點記錄。當時 main `63c9e4ab8ad7e277013acb5d7cce99228ec1f827`、公開Alpha/tag不變。隔離 `release/rc-closure-r3`，product QR commit `de2ac7d94e0a79f492d11ee165173e65b4562bd7`；整合R231後 build/runtime source `b98206267b3a65c452db1661bb3bae910ec61f6a`。後續文件HEAD不冒充新的runtime證據。

- 已修捐贈假QR及OSI/EFF完整官方網址：標準離線PNG、完整quiet zone、8 focused tests及12 rendered captures；保留既有視覺、copy/open、直接交組織與無背書說明。手機scan／TalkBack未驗，UI明示。typing/session/JNI／native pins／Dachen geometry不動。
- Fresh local：Python205 PASS／contracts10 PASS；App JVM290＝280PASS/10native-hostSKIP，Host JVM123PASS；四APK build exit0；新四ABIrelink `.text`／`.rodata`與本機APK相同，完整ELF／跨環境APK不宣稱相同。
- 唯一CI [39152444358](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39152444358) exactb982／FAILURE：H0HEALTHY；H1INCONCLUSIVE／EDITOR_CAPTURE_UNVERIFIED，UI dump 3004ms timeout／無XML。H1before WMS frozen=true、after/finalfalse；深層根因UNRESOLVED。H2／產品install／App5／Host33/32case／B01 NOT_EXECUTED，未盲retry／改timeout／鬆assertions。
- 新本機Debug20004／0.2.0-rc.1，SHA `c171a3455acb1cb4a3b16ca0eb65e483620c35a224a308f398ad65c1867f0416`，同舊Alpha cert46488…；CI使用不同cert，四APK／native完整bytes亦不同，分開保存，不能互換證據。
- 實機／保資料update／完整UI／OEM D2D／fullerase／正式key custody／完整Rust runtime license subset仍OPEN。RC_READY=false、Stable=false、NO_GO、NOT_PUBLISHED。新source/relink包已備，不能代替法律或runtime gate。
- PR10HOLD、PR12Draft、PR18Draft獨立review；不merge、不改舊Release、不啟S2/M4/ASR。下一最小任務：依H1raw隔離XML capture缺口，先因果／RED-GREEN再新runtime階段。

續作只讀 [R3交接](release/R3_FINAL_HANDOFF.md)、[驗證矩陣](release/R3_RELEASE_VALIDATION_MATRIX.md)、[readiness](release/R3_RELEASE_READINESS.md)及必要source；歷史不要當目前PASS。

## R1 起的歷史紀錄

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

兩組在KU-Yin安裝前已有SystemUI keyguard service ANR事件；A當下視窗可見、B當下隱藏，到before-app／失敗才可見（R2.3依raw勘誤）。失敗時windowfocus=false／IC0、after-focus show未觸發。舊R2.1則focus/IC1/show已具備，故本輪分類EMULATOR_ENVIRONMENT，不能解除原remote root。C不啟動，先需健康emulator前提調查。新runtime獨立review因quota未完成，不能宣稱APPROVE。

原20工區／3指定凍結資產保持。main63c9e4a、PR12d6bf2Draft、PR10ea5HOLD、release不變；無product/JNI/data變動、實機／merge／release／newobserver／S2／M4。續作：[R2.2最終交接](testing/R2_2_FINAL_HANDOFF.md)、[逐界線裁定](testing/R2_2_RUNTIME_FINDINGS.md)、[驗證矩陣](testing/R2_2_VALIDATION_MATRIX.md)。


## R2.3 emulator health（2026-10-18）

隔離fix/r2-3-emulator-health，base1ef5ba4、runtime source5ef8a979850d1588d6be68aca103e12cb51f4a6d。新增H0唯讀三態／H1既有真editor gate、補binding job KVM權限；全部Android sources／JNI／pins／signing／user資料不變。local181Python、10contracts及獨立限定source APPROVE；CI39047679642同source181Python／contracts／JVMtasks／四APKbuild成功，但job最終FAIL。

唯一fresh AVD H0=INCONCLUSIVE／exit3：第一次currentFocus=null，final取樣Launcher恢復；沒有觀測到SystemUI ANR，last-anr明示none。H0沒有用後次正常狀態覆寫初次原因，任何KU/hostinstall、H1／H2／App5／Host33／matrix32／B01均NOT_EXECUTED，B skipped。真KVMusable、boot38.428s仍不等於healthPASS，原ANR及remote根因未解。R2.2 B安裝前ANR window隱藏的勘誤已校canonical入口；原raw／舊工區保留。

source239/239、artifact103檔及四APK workspace hashguards核對；本輪APK signer／JVM逐test XML未封存，不推測count/identity。21原工區與公開APK/v2/v3三指定資產保持。main63c9e4a、PR12Draft、PR10HOLD、release不變；實機NOT_TESTED／productionNOT_READY／S2NOT_STARTED／M4NOT_VALIDATED。入口[R2.3交接](testing/R2_3_FINAL_HANDOFF.md)與[驗證矩陣](testing/R2_3_VALIDATION_MATRIX.md)。runtime獨立review overall NEEDS_REVISION，另有OPEN P2：probe未明確拒displayFrozen=true，離線反例可得HEALTHY，本run沒有假PASS。下一最小工作是frozen guard與H0焦點readiness的有界唯讀觀測；本輪遵STOP，不改碼或再嘗試binding。


## R2.3.1 health probe correction（2026-10-18）

隔離 `fix/r2-3-1-health-probe`，base9622586、runtime source `d26fb1b76e1f448f6004910b1189a513a13fc1a9`。F1/P2 frozen guard已以真raw單變數counterexample RED/GREEN及獨立oracle處置；unknown/缺失/空值/矛盾與重複不放行。H0 initial/final無retry、保留first assessment與ADB時間/raw/non-atomic metadata；不改Android/JNI/原Appvisibility assertions。

唯一health-only CI39080190653 exactsource SUCCESS：Python192 PASS、10contracts PASS、H0 HEALTHY/exit0；initial/final均明確unfrozen／Launcherfocus、same user0/boot、lastanr none，沒有APK install或H1/H2/App/Host。這不證明原ANR／remote binding根因已解，也不把兩組WMS reads當atomic或持續健康。Local192 PASS/131.431s、10contracts；source與manual路由兩位限定review PASS。最後actual runtime獨立review首次遇額度限制；使用者續作且過恢復時段後完成，**Spec/Quality PASS限H0**，獨立核100artifact／31命令、raw重建HEALTHY；原中斷履歷保留。

只推送新branch，NO_PR（既有PR trigger會執行其他runtime）；main63c9e4a、PR10HOLD／PR12Draft／舊release不變。指定舊APK/v2/v3hash及原22工區HEAD/status/tracked diff保持；字典/Room/prefs不碰。H1/H2待另一明確階段，physical NOT_TESTED／production NOT_READY／M4 NOT_VALIDATED／S2 NOT_STARTED。R3 QR與watcher僅初審、未修；release/rc-closure-r3工區仍main63且clean。入口：[R2.3.1交接](testing/R2_3_1_FINAL_HANDOFF.md)／[驗證](testing/R2_3_1_VALIDATION_MATRIX.md)。
