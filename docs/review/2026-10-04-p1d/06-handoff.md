# KU-YIN P1-D FINAL HANDOFF

本輪裁定：PASS — P1-D LOCAL VERIFIED（2026-10-05）。Stage 0 與 Stage 1 均完成；Android 裝置驗收仍為 NOT_TESTED。未開始 M4，未 push。

## LINEAGE

```text
REMOTE_C4_SHA: 3d86f7538c47dc929e25cdb0bd0755f8cc3bad60
P1ABC_LOCAL_BRANCH: fix/p1-series-correctness
P1ABC_CHECKPOINT_SHA: 870fc03cbfe8e5107b8ff8dc13478ac7754eec9f
P1D_BRANCH: fix/p1d-enter-policy
P1D_BASE_SHA: 870fc03cbfe8e5107b8ff8dc13478ac7754eec9f
P1D_FINAL_SHA: 75b93a3b68867fd6ec4c6adadd3e279d71e98104
PUSHED: NO
```

工作區：`/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1d-enter-policy`。

`P1D_FINAL_SHA` 是已建置、測試與獨立審查的程式提交，訊息為 `fix: normalize IME enter action policy`。此交接檔及證據由其後繼的 `docs: record P1-D enter policy verification` 文件提交保存；文件提交自身 SHA 由 `git rev-parse HEAD` 取得，並在最終回報列出。此方式避免在文件內填入無法自我包含的 commit hash，且不 amend／改寫歷史。

## PRE-P1D CONSOLIDATION

```text
FILES_CLASSIFIED: 41 non-ignored；另 2712 ignored metadata
UNKNOWN_FILES: 0
CONSOLIDATION_STRATEGY: 以可證明的檔案／hunk 歸屬依序建立 A、B、C、docs 本機提交；產品與測試 bytes 原樣保留
P1A_COMMIT: a7a036dffd29ec942fc98f78f83cf8ce59d1e6d8
P1B_COMMIT: 6af9476337c911557530e02f10a2d21fc6ca3a3b
P1C_COMMIT: b63fddc821ebb8dd28af309711d5da6dbb8a76c6
DOC_COMMIT: 870fc03cbfe8e5107b8ff8dc13478ac7754eec9f
```

Checkpoint 新鮮驗證：focused 41/41；full 37 類 149 測，0 failures/errors；contracts 10/10；bootstrap exit 0；Debug exit 0。Checkpoint APK 為 29,379,410 bytes，SHA256 `941dbce781f9ad848565ff1ebfa970ce22bac901595369617f5b2c9ce19a4129`。此為 P1-A/B/C 證據，P1-D 另有下列新鮮建置。完整 lineage inventory、技能矩陣及歷史證據限制見 [00-lineage-recovery.md](00-lineage-recovery.md)。

## ROOT CAUSE

```text
NO_ENTER_ACTION_PRESENTATION: 原 onStartInputView 僅解析 masked action，忽略 NO_ENTER_ACTION
NO_ENTER_ACTION_DISPATCH: 原 handleEditorAction 另解析 masked action，忽略 NO_ENTER_ACTION
CUSTOM_ACTION: 原碼未使用 actionLabel/actionId，無法顯示／派送 custom action
DUPLICATE_POLICY: presentation 與 dispatch 分別解讀 metadata，可因 EditorInfo 修改而漂移；PREVIOUS 已能 dispatch，但 label 落到換行
```

以上由當前 base 源碼與實際 assertion RED 證實。全量測試初次發生的 Compose/Robolectric idle timeout 是另一個已修復的測試隔離問題：跨測試未排空 snapshot 通知，Robolectric 重設佇列後 Compose 靜態排程旗標仍保留。最小 3 測試 RED/控制組 GREEN 與顯式排序 40 測試前後對照已存檔；只補 test teardown 排空 main looper，未變更產品 decoder／鍵盤布局或延長 timeout。詳 [04-red-green-evidence.md](04-red-green-evidence.md)。

## ANDROID CONTRACT

```text
NO_ENTER_ACTION: inline Enter 不顯示／派送 standard 或 custom action；優先正規化為換行
MASKED_ACTION: IME_MASK_ACTION 擷取 standard action，僅在 inline eligible 且沒有 custom label 時採用
CUSTOM_ACTION_LABEL: non-null label 對應 custom 行為；空字串原樣保留
CUSTOM_ACTION_ID: performEditorAction(actionId)，包括 0；不改送 unrelated masked action
```

官方 Android EditorInfo／InputConnection 契約、來源與邊界判斷見 [02-android-contract.md](02-android-contract.md)。LatinIME 僅作來源參考，未複製程式或新增 dependency。未新增 multiline heuristic；以明示 NO_ENTER_ACTION 為本階段契約。

## ENTER POLICY

| EditorInfo 條件 | Enter 呈現 | 未 consumed 時的唯一行為 |
| --- | --- | --- |
| NONE | 換行 | InsertNewline |
| UNSPECIFIED | 換行 | InsertNewline |
| GO | 前往 | performEditorAction(2) |
| SEARCH | 搜尋 | performEditorAction(3) |
| SEND | 傳送 | performEditorAction(4) |
| NEXT | 下一步 | performEditorAction(5) |
| DONE | 完成 | performEditorAction(6) |
| PREVIOUS | 上一步 | performEditorAction(7) |
| CUSTOM | 原 actionLabel | performEditorAction(actionId) |
| NO_ENTER_ACTION | 換行 | InsertNewline；優先於以上所有 action |

未知 masked action 也落到換行。呈現與派送共用 immutable `EditorEnterPolicy`；UI 不讀原始 EditorInfo／imeOptions。

## COMPOSITION-FIRST CONTRACT

```text
ACTIVE_COMPOSITION: session.dispatch(ImeCommand.Enter) consumed 後只 commit orderedCommits；當次不執行 editor action／newline
EMPTY_COMPOSITION: session 未 consumed 才委派 normalized editor policy 一次
DOUBLE_EFFECT_PREVENTION: 每次 Enter 一個 logical outcome；不新增 performEditorAction(false) 後 newline fallback
```

真實 service／Compose UI／ChewingEngineSession 接線測試涵蓋非空 composition 首次 Enter 完成、第二次才執行 action／newline，及 ordered output。Session／libchewing completion semantics 未修改。

## FILES_CHANGED

產品 3 檔：

- `app/src/main/java/com/example/androidkeyboard/input/EditorEnterPolicy.kt`：新增 primitive-input pure policy。
- `app/src/main/java/com/example/ime/KuYinInputMethodService.kt`：同一 policy snapshot 供呈現與 InputConnection 派送。
- `app/src/main/java/com/example/ime/ui/KuYinKeyboardUi.kt`：三種布局接收 normalized label／presentation boolean，保留 composition-first。

新增測試 4 檔：

- `app/src/test/java/com/example/androidkeyboard/input/EditorEnterPolicyTest.kt`：18 個 pure JVM 決策測試。
- `app/src/test/java/com/example/ime/EnterServiceHarness.kt`：真 service callback／ComposeView 與 recording InputConnection／JNI seam。
- `app/src/test/java/com/example/ime/EnterPolicyServiceTest.kt`：12 個 service 接線測試。
- `app/src/test/java/com/example/ime/EnterUiWiringTest.kt`：11 個 Compose 接線測試。

既有測試 1 檔：`app/src/test/java/com/example/ime/NonTextEditorServiceTest.kt`，僅新增 `@After` main-looper drain，未更動 P1-C assertions。

8 檔精確 bytes／SHA256 見 `final-green/review-manifest.json`。`p1d-pending-source-inventory.json` 為前次 PARTIAL 當下的 7 檔歷史快照，非最終 inventory。

文件：本目錄 00–06、RED/GREEN/診斷原始證據，及 `docs/CURRENT_STATE.md`、`docs/ARCHITECTURE.md` 更新。舊交接保存在 `previous-partial-handoff.md`；失敗證據未覆蓋。LSP 工具不可用，記錄為 UNAVAILABLE；Kotlin production／test／instrumentation 編譯已實測通過。

## RED EVIDENCE

```text
COMMAND: JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --tests 'com.example.ime.EnterPolicyServiceTest' --tests 'com.example.ime.EnterUiWiringTest' --no-build-cache --console=plain
FAILED: 23 測 16 failures，0 errors，exit 1；compiler 成功
WHY: NO_ENTER_ACTION 仍派送 SEND/GO；custom ID 73 錯送 masked 4；PREVIOUS label 缺漏；label／dispatch snapshot 漂移
```

`red/` 保留兩類 XML 與 log；[04-red-green-evidence.md](04-red-green-evidence.md) 列失敗名稱、expected／actual。不是以 missing API 的編譯失敗代替 RED。

## GREEN EVIDENCE

```text
FOCUSED: A pure 18；B EnterCommandContract 5；C service/editor 41；D Compose 11；E P1-C 25；F P1-B 6；G P1-A 12
COMMAND: final-green/jvm-results.json 保存逐 gate 完整命令；JAVA_HOME=/opt/homebrew/opt/openjdk@21，Gradle serial／--no-build-cache／--console=plain
RESULT: A–H 全部 exit 0，0 failures/errors/skipped

JVM: 最新完整全量
COMMAND: ./gradlew :app:testDebugUnitTest --no-build-cache --console=plain
CLASS_COUNT: 40
TEST_COUNT: 190
FAILED: 0 failures；0 errors；0 skipped
EXIT: 0

CONTRACTS: 全部目前 scripts/check_*.py
COUNT: 10
FAILED: 0
EXIT: 0

BOOTSTRAP: scripts/bootstrap_native_deps.sh；使用隔離 task cache，未改 native pins
EXIT: 0

DEBUG_BUILD: ./gradlew :app:assembleDebug --no-build-cache --console=plain
EXIT: 0
APK_PATH: /Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1d-enter-policy/app/build/outputs/apk/debug/app-debug.apk
APK_SIZE: 29,158,832 bytes
APK_SHA256: b223620743831e0370a31a65dcc998e6a74dfe70d45e020036c4f8b4468e39c9

INSTRUMENTATION_COMPILE: ./gradlew :app:compileDebugAndroidTestKotlin --no-build-cache --console=plain
EXIT: 0
RELEASE_BUILD: ./gradlew :app:assembleRelease --no-build-cache --console=plain
EXIT: 1
STATUS: BLOCKED_BY_EXISTING_MISSING_KEYSTORE
REASON: packageRelease 的 storeFile my-upload-key.jks 不存在；未改 signing／未簽署 release
```

最新 XML、命令、各 gate log、APK metadata 見 `final-green/`。原始 review.patch／thread dump 的空白亦原樣保留；文件正文及產品／測試的 whitespace check 通過，未為格式检查改寫原始證據。Release 的失敗文字已按原始 log 分類；未聲稱 Release PASS。程式提交與已審查／驗證 source manifest 逐 bytes 相同；其後只更新文件，不重複無必要 Gradle 測試。

## RUNTIME

```text
DEVICE: adb devices -l 無裝置／模擬器；exit 0，device_count 0
API: N/A
ABI: N/A
STATUS: NOT_TESTED
EVIDENCE: final-green/runtime.json
```

未安裝 APK、未執行 physical-device acceptance。Robolectric 與 instrumentation compile 不等於 runtime PASS；不得寫 production ready。

## OPENCODEREVIEW

```text
VERSION: v1.12.11 (a758d9cb)
STATUS: BLOCKED_BY_PROVIDER
SESSION: none
FINDINGS: 本階段一次 provider health exit 1；401 authentication_error invalid x-api-key
```

未循環重試、未讀出／修改／暴露 credentials，未聲稱 OCR 掃描完成。後續重新配置 provider 後可重掃。

## INDEPENDENT REVIEW

```text
STATUS: APPROVE
FINDINGS: 兩份 fresh readonly final review 均無可確認的新重大問題
```

`p1d_policy_review` 核對 Android normalization／custom ID／NO_ENTER_ACTION／PREVIOUS／共享 snapshot／composition-first／scope；`p1d_ui_test_review` 核對三布局與真 service 接線、非空 composition、ordered commits、RED assertions、teardown 及全量 XML。共同 patch SHA256：`401484b25829a350164975a9a48718e80768a5cc46a49eefec94b97379811b90`。細節見 [05-independent-review.md](05-independent-review.md)。此為本機審查裁定，非裝置驗收或 remote merge 授權。

## REGRESSION STATUS

```text
P1-A: LOCAL VERIFIED；focused 12 與 full PASS；runtime NOT_TESTED
P1-B: LOCAL VERIFIED；focused 6 與 full PASS；runtime NOT_TESTED
P1-C: LOCAL VERIFIED；focused 25 與 full PASS；runtime NOT_TESTED
P1-D: LOCAL VERIFIED；pure／service／Compose／full PASS；runtime NOT_TESTED
```

P1-C NUMBER／PHONE／DATETIME／TYPE_NULL 仍禁止 Zhuyin composition 與 personalized learning，暫時呈現 ENGLISH，返回 text editor 恢復 user preferred mode。P1-A/B/C 產品 ownership／policy／Dachen／decoder 保護檔 bytes 未變，見 `final-green/protected-source-check.json`。

## REMAINING

```text
PHYSICAL_DEVICE_ALPHA: OPEN
M4: NOT_VALIDATED；未開始
RELEASE_SIGNING: existing missing keystore；未 release/sign
```

其他 5 工作區的 HEAD、完整 status 與既有 dirty 20 檔 SHA256 都與初始 snapshot 相同，見 `final-green/preserved-worktrees-final-check.json`；P1-A/B/C checkpoint 工作區 clean。未操作 donor archives。未 push／merge／改 native pins／ABI／app identity／candidate ranking／decoder semantics／keyboard geometry。

## FINAL RULING

PASS — P1-D LOCAL VERIFIED，範圍為本機靜態、JVM、contracts、native bootstrap 與 Debug 建置。Runtime 個別維持 NOT_TESTED。

## NEXT

P1 PHYSICAL-DEVICE ACCEPTANCE + ALPHA CONSOLIDATION

本階段完成後 STOP；不自動開始 M4。
