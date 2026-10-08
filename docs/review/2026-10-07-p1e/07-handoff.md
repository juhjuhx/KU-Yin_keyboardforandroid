# KU-YIN P1-E ACCEPTANCE FIXTURE HANDOFF

## LINEAGE

BASE_SHA: 5c6bcad34c4048e40ce4e871e70e176c10195bcf
BRANCH: test/p1-alpha-acceptance-fixture
FINAL_SHA: 6595b68403232187eba4d9ec10c7f0689c2b5b61
PUSHED: NO
WORKTREE: /Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1-alpha-acceptance-fixture
HOST_COMMIT: ee325fe7ffaeb9894162b02484212f746208e605
MATRIX_COMMIT: 33170bfa42194b3774774dc0914e3d3b9ace3443
DEVICE_RUNNER_COMMIT: 6595b68403232187eba4d9ec10c7f0689c2b5b61

FINAL_SHA 指最後經驗證的 fixture 源碼 commit。交接與證據另以 docs commit 保存，避免把文件自己的 commit hash 寫入自身；最終文件 HEAD 可用 `git rev-parse test/p1-alpha-acceptance-fixture` 核對。
原 Alpha 基線與 frozen package 已核對；原8工區 HEAD／status／既有dirty bytes、v1兩APK＋ZIP不變。指定2026-10-07審查目錄保留，最後封裝日期2026-10-08，未回填Git作者或時間。

## PRODUCT SOURCE

PRODUCT_FILES_CHANGED: 0（87個既有app main／debug／androidTest tracked檔案逐byte同base）
PRODUCT_BEHAVIOR_CHANGED: NO

未修改 decoder／session／UI／JNI／EditorPolicy／EditorEnterPolicy／大千／learning policy；未變 native pins／ABI／applicationId。既有4類5測保留原義。證據 product-source-protection.json／preservation-final.json。

## HOST

MODULE: :acceptance-host
APPLICATION_ID: io.github.juhjuhx.kuyin.acceptancehost
ACTIVITY: AcceptanceEditorActivity
EXPORTED: YES（整個APK為testOnly helper）
COMPOSE: NO
NETWORK_PERMISSION: NONE（host main merged manifest無permission）
PRODUCT_OR_LIBCHEWING_DEPENDENCY: NONE
MIN_SDK: 24
COMPILE_SDK: 36.1

## EDITOR ORACLE

CONFIG_INPUT_TYPE: primitive Intent inputType
CONFIG_IME_OPTIONS: primitive Intent imeOptions
CUSTOM_ACTION_LABEL: setImeActionLabel＋final outAttrs；確認／換行等測試label
CUSTOM_ACTION_ID: primitive customActionId；矩陣73，0亦合法
FINAL_EDITORINFO_CAPTURED: IMPLEMENTED（super後保存platform，再真正覆寫四欄並保存final；runtime未執行）
TYPE_NULL: 保留super真null；無回呼／無軟鍵前提時INCONCLUSIVE，不製造IC

## INPUT CONNECTION RECORDER

ACTION_COUNT: exact wrapper呼叫次數
LAST_ACTION_ID: exact收到的actionCode
ACTION_RETURN_VALUE: 配置true／false；host消費action，不delegate製造第二effect
NEWLINE_COUNT: 真EditText當前文字LF計數／before-after delta
CURRENT_TEXT: 真Editable文字，固定synthetic cases
LAST_COMMIT_TEXT: 真commitText呼叫觀察；正常文字/composing/delete仍delegate

Host boolean不由Android oneway Binder回傳至IME。D17只觀察host收到SEND一次、回false與newline0；同步IME false不fallback語義由既有P1-D JVM另證，不能假稱裝置已證。

## TEST MATRIX READY

A_CASES: A01/A02既有smoke PACKAGED_NOT_RUN；A03人工actual rotation準備完成；A04安全fault fixture OPEN
B_CASES: B01–B03真軟鍵／preedit／candidate路徑 COMPILE_READY_NOT_RUN；第二寬度／方向、context及句子語義範圍另列OPEN
C_CASES: C01–C05 editor/mode/composition/restoration AUTOMATION_READY_NOT_RUN；native learning observer OPEN；TYPE_NULL可能INCONCLUSIVE
D_CASES: D01–D18 AUTOMATION_READY_NOT_RUN；D18三布局同時測SEND與NO_ENTER_ACTION newline
HOST_INSTRUMENTATION: 3類／26個@Test，只編譯成功
FULL_MATRIX: 30列，全部NOT_TESTED

Selector只用可見文字／contentDescription、產品package與唯一clickable hitbox；清除旁strict preedit與鍵面分開。缺selector標BLOCKED_SELECTOR，不用hardware Enter、adb input keyevent、direct native或座標補PASS。
結果只PASS／FAIL／INCONCLUSIVE／NOT_TESTED；case emit連結JUnit owner/terminal，skip不是PASS。Genuine cleanup錯誤必須FAIL並重拋，原scope reason與cleanup細節保留。

## REMAINING NON-AUTOMATED

NATIVE_LEARNING_OBSERVER: OPEN，未加產品main/debug observer
STARTUP_FAULT_DEVICE_CASE: OPEN，未注入destructive fault
ROTATION: MANUAL_READY_NOT_TESTED；實際旋轉，不以recreate代替；preedit保留/清空只是觀察
OTHER: 真實IC／OEM／API24–29可見性與accessibility selector需裝置核對；B部分native語義無external observer

## BUILD EVIDENCE

HOST_UNIT: PASS，3類37測／0 failure/error/skipped，exit0；host-final-green.log與XML
HOST_APK: PASS，:acceptance-host:assembleDebug exit0；host-build.log
HOST_TEST_APK: PASS，:acceptance-host:assembleDebugAndroidTest exit0；host-final-androidtest.log
APP_UNIT: PASS，40類190測／0 failure/error/skipped，exit0；app-verification.log與XML
APP_DEBUG: PASS，:app:assembleDebug exit0；新build只用於gate，v2沿用frozen產品bytes
APP_TEST_APK: PASS，:app:assembleDebugAndroidTest exit0；原4類5測未執行
CONTRACTS: PASS，10/10／exit0；contracts-final.log
BOOTSTRAP: PASS，既有固定SHA四ABI與字典／exit0；bootstrap.log
RUNNER_UNIT: PASS，24個pure/fake-subprocess tests／exit0；不是真ADB
RELEASE: BLOCKED_EXISTING_MISSING_KEYSTORE，packageRelease exit1；未簽署/發佈

RED證據：host28測25fail→GREEN28；cleanup policy9測2fail→host全量GREEN37；runner Bscope三subcases fail→GREEN23；select雙錯誤24測1fail→GREEN24。所有RED均保存log/XML，不把source-set編譯錯誤或無裝置行為當RED。
新編譯app APK僅四ABI build-ID不同；移除.note.gnu.build-id的暫存副本逐byte相同。沒有改APK，沒有將新b7b67…hash取代frozen b223620…。

## ARTIFACTS

KUYIN_APP_SHA256: b223620743831e0370a31a65dcc998e6a74dfe70d45e020036c4f8b4468e39c9
KUYIN_APP_BYTES: 29158832
KUYIN_TEST_SHA256: 3938b3e3c1fb711ed6141d491a3841839e098dfb2c2ae81924b94f0d6106b8bd
KUYIN_TEST_BYTES: 965496
HOST_APK_SHA256: 8e53e8933e1b436b3192dab4319a7da1fe58e98afba038447924f51e1481d829
HOST_APK_BYTES: 861628
HOST_TEST_SHA256: 5db907bd2e131742ce817139785d2f4a9a5cb7254ccd13839bfcbe6eb6277bd2
HOST_TEST_BYTES: 1028719
PACKAGE_V2_SHA256: 787d44f788b172dad942087fcffb2e34e1bdad8bfcdeebb7a02b086ab5a3e65d
PACKAGE_V2_BYTES: 28096851
PACKAGE_V2_PATH: /Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1-alpha-acceptance-fixture/build/p1-alpha-acceptance-v2-fixture/KU-Yin-P1-Alpha驗收包-v2-fixture.zip
SIGNER_CERTIFICATE_SHA256: 46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94

四者apksigner verify／manifest identity/target核對exit0，使用同一有效Debug certificate。Host APK無.so/Compose/產品service/libchewing DEX參照。
ZIP CRC與內四APK hash核對PASS，418 entries；可攜runner離線驗artifact PASS。ZIP內含updated runbook/matrix、manifest/hash/signers、fixture source/architecture與review證據；本最終交接與ZIP自身SHA放ZIP外側避免self hash。

## OPENCODEREVIEW

VERSION: v1.12.12 (182898c)
STATUS: BLOCKED_BY_PROVIDER
SESSION: NONE
FINDINGS: 無OCR審查結果；一次anthropic health exit1／HTTP401 invalid x-api-key，request req_011CfnkCr2PvNq6SwcBDSBz4

沒有重複相同認證重試、沒有讀出或改key；不得標OCR PASS。

## INDEPENDENT REVIEW

STATUS: APPROVE_FOR_FIXTURE_PREPARATION
FINDINGS: 2個Medium與1個Optional已修正、各自RED/GREEN與fresh覆核；無remaining blocker

Host oracle獨立source review APPROVE；Android矩陣獨立source review APPROVE；runner／模擬tests／08/09獨立review APPROVE並fresh24/24測通過。僅是fixture準備裁定；runtime不是PASS。完整證據06-independent-review.md。

## RUNTIME

DEVICE: 使用者表示目前無實機；未提供target serial
STATUS: NOT_TESTED
DEVICE_SEQUENCE: NOT_EXECUTED
PHYSICAL_ACCEPTANCE: OPEN
PRODUCTION_RELEASE: NOT_READY
M4: NOT_VALIDATED

## FINAL RULING

READY_FOR_DEVICE

## NEXT

P1 ALPHA PHYSICAL-DEVICE EXECUTION

裝置到位後核對artifact、保存原IME狀態、安裝精確四APK、明示enable/select，跑原5測與host矩陣、補人工/不可觀察範圍、還原並逐case裁定。維持本機歷史；本輪STOP，不push、不開始M4、不宣稱P1完整實機驗收或production ready。
