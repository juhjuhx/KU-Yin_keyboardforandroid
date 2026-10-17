# KU-YIN R2.2 BINDING ROOT CAUSE

2026-10-17。**有限研究已交付；原remote binding根因UNRESOLVED，本輪runtime遭EMULATOR_ENVIRONMENT阻擋。沒有宣稱修復完成。**

## SOURCE

MAIN_SHA: `63c9e4ab8ad7e277013acb5d7cce99228ec1f827`
R2_2_SOURCE_SHA: `3115002da5fbb5e0344e6bb3b2068dc9c145801b`
BASE_SHA: `2b8e8e4efff73741319fb00daf4cb4e58101e4e8`
BRANCH: `fix/r2-2-ime-binding`
WORKTREE: `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/r2-2-ime-binding`

source與後續docs HEAD分開。最終docs SHA由Git及external R2_2_FINAL_RECEIPT.json核對，避免自引用虛構SHA。正常push test branch，沒有修改main、PR12或PR10，沒有merge／release。

## FAILED_BASELINE_RUN／CONTROLLED_EXPERIMENTS

舊run38825146067 attempt2@b1b5bf6，App4PASS1firstVisibilityFAIL，具windowfocus／真IC1，remote/session未ready，原根因仍未解除。

唯一paired [run39005467151](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39005467151) attempt1 exact3115002，job38m43s FAIL；A原install-select-connected，B先裝App/AppTest再select/direct。237 source hashes及current manifest核對相符，四APK一次build／guard及兩arm全部phase metadata相符。沒有重跑；C NOT_EXECUTED，因兩組before-install已SystemUI ANR，新增覆裝無法隔離原問題。

APP_RUNTIME: A=4PASS／1FAIL／0SKIP（exact5）；B=4PASS／1FAIL／0SKIP（exact5）。同一kt81原first visibility assertion，5start/5finish及terminal完整，沒有改成skip。
HOST_RUNTIME: NOT_TESTED；matrix32全NOT_TESTED；B01_RUNTIME: NOT_TESTED。不得用歷史24PASS／9SKIP補本次結果。

## ROOT_CAUSE／FIRST_DIVERGENCE

本輪兩組在KU-Yin還未安裝時已出現SystemUI keyguard service ANR。失敗時可見系統對話框覆蓋editor，window_focused=false／ICcount0；原after-focus show尚未發出。B selectedKU但bound仍LatinIME，A無bound；這是環境前提失效，信心HIGH。

原R2.1則focus/IC/show具備，沒有同一ANR；不能把新的較早失敗當其根因。ROOT_CAUSE: UNRESOLVED（historical remote method/session）；SystemUI更深boot原因也UNRESOLVED。

PACKAGE_REPLACEMENT_EVENTS: A有select後installPackageLI與測後cleanup；B無A之覆裝事件但同樣失敗；normalinstrumentation兩組皆有start/finished instr，不能由A覆裝唯一歸因。SERVICE_BINDING_TRACE、first window/PNG與raw細節：[逐界線裁定](R2_2_RUNTIME_FINDINGS.md)。

## IMPLEMENTATION／RED／GREEN

六source檔：runtime_smoke.sh opt-in實驗／current33 adapter與manifest／兩個fake test檔／既有manual workflow。Android來源、原app/Host assertions與timeout不變。關鍵時間窗server-only proto，不以fulltextclientdump干擾show；primary protocol失敗與secondarycapture錯誤分開、wronguser failclosed、sameAPK guards、App失敗停止Host。這是研究採證修正，不是原binding修復。

RED: 12 baseline3有效assertions、13 package/primary、20b user、24 phase、27 wrongusercleanup。07／14／19／20中途FAIL或hostwatchdogERROR照存，ERROR不冒稱RED；只new fake-binding process watchdog10→30s，Android40×250ms及UI assertions未改。
GREEN: fixed6files snapshot28→29全Python151PASS／exit0／103.654s；10contracts、shell/YAML/docs/diff PASS，protected source diff0。CI nativebootstrap及四APK一次build/sourceguard SUCCESS；runtime仍FAIL。沒有另跑未改Android的JVM或把compile當真devicePASS。詳[驗證矩陣](R2_2_VALIDATION_MATRIX.md)。

## INDEPENDENT_REVIEW

source safety角色的完整md/json（hash逐一核對）批准exact六檔一次paired experiment，無未解High/Medium阻擋該limitedscope；其最後聊天quota不抹去已寫成收據，也不擴大批准。平台角色另完成exact API33／AGP worker研究及current5/33/32 contract書面核對，所有輸入／report hash已核。

current protocol提醒：CLI0可含missing emit/INCONCLUSIVE，必須獨立核required emits；metadata count／sourcehash亦不由evaluate自動驗。這些邊界明列，不稱fullHostPASS。本次未到Host，沒有隱藏missingcase。

新runtime raw獨立review因reviewer quota未完成；未改派重試相同額度、未偽稱runtimeAPPROVE。主agent實際回核237來源／252artifact manifest、A XML+完整protocol、B完整protocol、兩組ANR/focus/IC/PNG、舊R2.1反證，僅列author evidence audit。Final/runtime review gate仍OPEN，不推薦merge。OCR本輪NOT_RUN；歷史401與R2.1 approvalreview拒絕非fresh provider結果。

## PRESERVATION／STATUS

PRODUCT_CHANGED: NO；NATIVE_CHANGED: NO；DATA_CHANGED: NO。app/src、acceptance-host/src／build/pins／signing/schema／frozen runner/manifest/license不變，沒有讀刪userdict／Room／prefs。20原工區保存，總21；指定公開AIEAPK與v2/v3 ZIP三hash未變。CI disposable emulator由action結束，未接實機；本機watch已正常因jobFAIL退出，非留存測試程序。

PR12_RECOMMENDATION: KEEP_DRAFT，尚無healthy App5 runtime與獨立final審查。
PR10_VERDICT: HOLD，既有patch在PR12 ancestry內，不重複merge。
PHYSICAL_ACCEPTANCE: NOT_TESTED／OPEN。
PRODUCTION_READINESS: NOT_READY；M4 NOT_VALIDATED；AIE-S2 NOT_STARTED。

## EVIDENCE／NEXT_ACTION

external：`/Users/huang/Desktop/KU-Yin-R2.2-Verification-2026-10-16/`。先讀RUNNING_CONTEXT_ANCHOR.json／R2_2_FINAL_RECEIPT.json，再讀44-controlled-AB-analysis.json；45是252檔hashmanifest，40為exactrun metadata，41原artifacts、42完整CIlog；29／30為localgates，36為保存收據。兩次watch僅同run唯讀監看，原watchnetworktimeout不當CIFAIL。

下一最小任務：**disposable emulator health precondition／SystemUI ANR根因的有限調查**，failclosed保存環境錯誤，不dismiss／timeout補丁。健康環境及independent review可用後才續原binding隔離；不以本輪raw批准product改動、newobserver、release或M4。STOP。
