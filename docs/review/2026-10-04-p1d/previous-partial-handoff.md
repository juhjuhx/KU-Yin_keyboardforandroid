# KU-YIN P1-D FINAL HANDOFF

本輪裁定：PARTIAL。Stage 0 完成；P1-D 正式碼與回歸測試已寫入，但全量測試未通過，未建立 P1-D commits。此檔是可接續交接，不是完成宣告。

## LINEAGE

REMOTE_C4_SHA: 3d86f7538c47dc929e25cdb0bd0755f8cc3bad60
P1ABC_LOCAL_BRANCH: fix/p1-series-correctness
P1ABC_CHECKPOINT_SHA: 870fc03cbfe8e5107b8ff8dc13478ac7754eec9f
P1D_BRANCH: fix/p1d-enter-policy
P1D_BASE_SHA: 870fc03cbfe8e5107b8ff8dc13478ac7754eec9f
P1D_FINAL_SHA: NOT_COMMITTED
P1D_CURRENT_HEAD: 870fc03cbfe8e5107b8ff8dc13478ac7754eec9f
PUSHED: NO

工作區：/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1d-enter-policy。
末次 branch ref 以檔案讀取確認如上；最後 git status 因命令工具 EMFILE 未能重新執行。P1-D 3產品+4測試及審查資料仍未提交；不可宣稱 clean。

## PRE-P1D CONSOLIDATION

FILES_CLASSIFIED: 41 non-ignored；另2712 ignored metadata
UNKNOWN_FILES: 0
CONSOLIDATION_STRATEGY: 實際獨立 hunks/檔案可歸屬，依 A/B/C/docs 四本機提交，產品與測試 bytes 原樣保留
P1A_COMMIT: a7a036dffd29ec942fc98f78f83cf8ce59d1e6d8
P1B_COMMIT: 6af9476337c911557530e02f10a2d21fc6ca3a3b
P1C_COMMIT: b63fddc821ebb8dd28af309711d5da6dbb8a76c6
DOC_COMMIT: 870fc03cbfe8e5107b8ff8dc13478ac7754eec9f

Checkpoint 新鮮focused41/41；full37類149測0failures/errors；contracts10/10；bootstrap exit0；Debug exit0。APK29379410bytes，sha256 941dbce781f9ad848565ff1ebfa970ce22bac901595369617f5b2c9ce19a4129。這是 P1-A/B/C APK，不能當作 P1-D 建置或 runtime 證據。

## ROOT CAUSE

NO_ENTER_ACTION_PRESENTATION: 舊 onStartInputView 僅 mask action，忽略 NO_ENTER_ACTION
NO_ENTER_ACTION_DISPATCH: 舊 handleEditorAction 再 mask options，忽略 NO_ENTER_ACTION
CUSTOM_ACTION: 舊碼未讀 actionLabel/actionId
DUPLICATE_POLICY: label/dispatch各自解析metadata；mutable EditorInfo改動可導致漂移；PREVIOUS dispatch存在但label落到換行

以上源碼與實際 assertion RED 已證實。另全量測試的 LEGACY idle 失敗根因尚未確證，詳04，不混同產品Enter缺陷。

## ANDROID CONTRACT

NO_ENTER_ACTION: inline Enter不可替換為standard/custom action，normalized為換行
MASKED_ACTION: IME_MASK_ACTION提取標準action，僅在inline eligible且沒有customlabel時使用
CUSTOM_ACTION_LABEL: non-null label對應custom行為；空字串仍保留，不任意invent fallback
CUSTOM_ACTION_ID: 直接performEditorAction(actionId)，包括0；不改送maskedaction

官方來源与邊界判斷詳02-android-contract.md；LatinIME僅參考，未複製程式／新增dependency。

## ENTER POLICY

NONE: 換行 / InsertNewline
UNSPECIFIED: 換行 / InsertNewline
GO: 前往 / action2
SEARCH: 搜尋 / action3
SEND: 傳送 / action4
NEXT: 下一步 / action5
DONE: 完成 / action6
PREVIOUS: 上一步 / action7
CUSTOM: 原label / customactionId
NO_ENTER_ACTION: 優先於上述任意action，換行 / InsertNewline

## COMPOSITION-FIRST CONTRACT

ACTIVE_COMPOSITION: session.dispatch(ImeCommand.Enter) consumed後只commit orderedCommits；當次不editor action／newline
EMPTY_COMPOSITION: session未consumed才委派normalizedpolicy一次
DOUBLE_EFFECT_PREVENTION: 不新增performEditorAction(false)後newline fallback；composition-first與decoder實作未改

## FILES_CHANGED

產品3檔：

- app/src/main/java/com/example/androidkeyboard/input/EditorEnterPolicy.kt（新增 immutable primitive-input policy）
- app/src/main/java/com/example/ime/KuYinInputMethodService.kt（同一政策同步presentation/dispatch）
- app/src/main/java/com/example/ime/ui/KuYinKeyboardUi.kt（接收normalized presentation boolean，不解析EditorInfo）

新增測試4檔：

- app/src/test/java/com/example/androidkeyboard/input/EditorEnterPolicyTest.kt（18 pure tests）
- app/src/test/java/com/example/ime/EnterServiceHarness.kt（真service callback/ComposeView；recording InputConnection/JNI seam）
- app/src/test/java/com/example/ime/EnterPolicyServiceTest.kt（12 tests）
- app/src/test/java/com/example/ime/EnterUiWiringTest.kt（11 tests）

p1d-pending-source-inventory.json記錄7檔bytes與SHA256。review資料均在本目錄。

## RED EVIDENCE

COMMAND: JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --tests 'com.example.ime.EnterPolicyServiceTest' --tests 'com.example.ime.EnterUiWiringTest' --no-build-cache --console=plain
FAILED: 23測16failures，0errors，exit1；compiler成功
WHY: NO_ENTER_ACTION仍Action(4/2)、custom73錯送masked4、PREVIOUS缺label、presentation/dispatch snapshot漂移等真實assertion

red/保留兩類XML與log；詳04列失敗名稱、expected/actual。

## GREEN EVIDENCE

FOCUSED: A18 / B5 / C41 / D11 / E25 / F6 / G12，全部exit0
COMMAND: green-evidence.json逐步保存完整命令
RESULT: focused通過，full未GREEN

JVM: 第二輪完整full
CLASS_COUNT: 40
TEST_COUNT: 190
FAILED: 5 failures，0errors，0skipped
EXIT: 1

COMMAND: JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --no-build-cache --console=plain
5個Dachen LEGACY方法在setContent idle timeout，NATIVE方法通過；其餘185測通過。full-idle-failure-second/全40XML/log/dump保留。第一輪full被中斷也保留，未冒稱GREEN。

CONTRACTS: P1-D NOT_RUN
COUNT: checkpoint10/10為Stage0證據，不挪用為P1-D新鮮結果
FAILED: N/A
EXIT: N/A

BOOTSTRAP: P1-D NOT_RUN
EXIT: N/A

DEBUG_BUILD: P1-D NOT_RUN
EXIT: N/A
APK_PATH: N/A
APK_SIZE: N/A
APK_SHA256: N/A

INSTRUMENTATION_COMPILE: P1-D NOT_RUN
RELEASE_BUILD: P1-D NOT_RUN；existing missing-keystore status保留，尚無本輪Release實測

## RUNTIME

DEVICE: 本輪P1-D尚未重新列舉Android targets
API: N/A
ABI: N/A
STATUS: NOT_TESTED
EVIDENCE: 無runtime驗收；Robolectric不等於physical-device acceptance

## OPENCODEREVIEW

VERSION: v1.12.11 (a758d9cb)
STATUS: BLOCKED_BY_PROVIDER
SESSION: none
FINDINGS: 本輪一次health exit1，401 authentication_error invalid x-api-key；未重试／暴露／修改credentials

## INDEPENDENT REVIEW

STATUS: Stage0 p1abc_audit APPROVE；P1-D final review NOT_RUN
FINDINGS: GREEN後最終審查尚無前提。idle_diagnosis僅做順序證據診斷，非P1-D APPROVE；命令工具EMFILE使其無法獨立讀檔。詳05。

## REGRESSION STATUS

P1-A: LOCAL VERIFIED at checkpoint；P1-D focused12 pass
P1-B: LOCAL VERIFIED at checkpoint；P1-D focused6 pass，但current full5 UI fail，待修
P1-C: LOCAL VERIFIED at checkpoint；P1-D focused25 pass
P1-D: PARTIAL / UNCOMMITTED / FULL_NOT_GREEN

## REMAINING

PHYSICAL_DEVICE_ALPHA: OPEN，P1-D全部gates通過後才進行
M4: NOT_VALIDATED；未開始
RELEASE_SIGNING: existing missing keystore；未release/sign

## FINAL RULING

PARTIAL

原因1：full未通過，不能建立P1-D verified commits。
原因2：exec_command（root/child，普通/核准，含非login）無法建立程序，Too many open files (os error24)。只剩檔案讀寫；node child_process spawn EPERM；Computer Use Terminal安全限制禁止使用，未繞過。需要恢復本次命令工具工作階段才能續跑。

其他5工區既有dirty20檔SHA256核對未變（preserved-worktrees-content-check.json）；未操作donor。未push／merge／改nativepins／ABI／appidentity／decodersemantics／鍵盤geometry。

## NEXT

立即接續 P1-D：

1. 恢復命令工具，重新git status／branch／HEAD；依本檔與pending-source-inventory核對7檔。
2. 明確排序的臨時JUnit Suite：EnterUiWiringTest → DachenKeyboardLayoutTest。Gradle --tests參數不保證順序；之前17測實為Dachen→EnterUi，不能視為修復證據。
3. 若不重現，最小加入NonText/Hitbox/Greeting前置；若重現，核對目前Compose的AbstractComposeView.disposeComposition/onMeasure/ensureCompositionCreated與idle registry，確認detach/dispose/destroy順序。只修新增harness，不延長timeout或略過B測試。
4. 修正後重跑必要focused、full；full GREEN後依序contracts、bootstrap、Debug、instrumentationcompile、requiredRelease，量測freshAPK；列舉裝置，若沒有target則runtime NOT_TESTED。
5. GREEN後 fresh independent final review，解決真實findings，再建立local code/docs兩commit並檢查clean；不push。
6. 才進 P1 PHYSICAL-DEVICE ACCEPTANCE + ALPHA CONSOLIDATION。M4保持NOT_VALIDATED，本階段完成後STOP。
