# P1-D Enter Implementation Plan

**Goal:** 同一normalized政策產出label/presentation與unconsumed Enter行為，保持composition-first。
**Architecture:** EditorInfo只在service邊界抽取primitive值。EditorEnterPolicy為immutable純決策，不持有InputConnection。UI傳遞label/isActionPresentation；dispatch只when normalized behavior。
**Tech Stack:** Android36/Kotlin2.2.10/JUnit4/Robolectric4.16.1/Compose。

## 決策表（寫測試前定義）

| metadata | label | isActionPresentation | unconsumed行為 |
|---|---|---|---|
| NONE | 換行 | false | commitText(\n,1) |
| UNSPECIFIED/unknown/null | 換行 | false | commitText(\n,1) |
| GO | 前往 | true | performEditorAction(2) |
| SEARCH | 搜尋 | true | performEditorAction(3) |
| SEND | 傳送 | true | performEditorAction(4) |
| NEXT | 下一步 | true | performEditorAction(5) |
| DONE | 完成 | true | performEditorAction(6) |
| PREVIOUS | 上一步 | true | performEditorAction(7) |
| custom label/id | 原label | true | performEditorAction(id) |
| 任意standard/custom+NO_ENTER_ACTION | 換行 | false | commitText(\n,1)，不action |
| activecomposition+SEND | SEND presentation | true | session completion commits；不action |
| activecomposition+NO_ENTER_ACTION | 換行 | false | session completion commits；不newline |

## 實作範圍與任務

- [x] 新增 EnterPolicyServiceTest、EnterUiWiringTest：先對目前能編譯的真實callback/IC/UI測NO_ENTER_ACTION、custom、PREVIOUS、snapshot drift及composition consumption。Gradle focused須exit1且assertion為實際缺陷，不接受compiler錯誤。保存RED XML與失敗expected/actual。
- [x] 新增 pure EditorEnterPolicyTest：上表standard/fullflags/custom/id0/empty label/unknown；無Robolectric，獨立預期，不從產品資料生成。
- [x] 新增 app/src/main/java/com/example/androidkeyboard/input/EditorEnterPolicy.kt：class private constructor(label,behavior)，isActionPresentation由behavior推導；Behavior=InsertNewline/PerformStandardAction(actionCode)/PerformCustomAction(actionId)。from(imeOptions,actionLabel:String?=null,actionId:Int=0)先NO_ENTER_ACTION，再custom，再standard，else newline。
- [x] Service：currentEnterPolicy mutable state取代currentActionLabel；同一updateEnterPolicy(info)在onStartInput與onStartInputView更新。onCreateInputView傳policy.label與policy.isActionPresentation；handleEditorAction只when behavior，單一IC effect，不mask/no retry。
- [x] UI：增加可選isActionPresentation（預設維持既有preview呼叫的換行圖示判斷）；service明確提供normalized值，三layout接收同值，icon由此值決定。geometry/colors/session Enter順序皆不改。
- [x] focused順序：pure→EnterCommandContract→service/editor→Compose→P1-C→P1-B→P1-A。再full JVM（禁cache）→所有Pythoncontracts→bootstrap→Debug→instrumentationcompile→Release（repo required，缺keystore如實記錄）。Gradle不平行。
- [x] independent review、解決真實finding，local code/docs commits，handoff，STOP。

## 測試方法與命令

JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --tests 'com.example.ime.EnterPolicyServiceTest' --tests 'com.example.ime.EnterUiWiringTest' --no-build-cache --console=plain

Service使用既有P1-C測試的注入方式：不呼叫onCreate/JNI，真實service callback＋real session＋recording InputConnection；framework mStartedInputConnection/mInputEditorInfo由reflection注入（API36 javap已核對欄位）。UI由實際onCreateInputView的ComposeView掛上測試host，不自行拼service callback。compiler/host設定失敗先修harness，不當RED。

每個tap只驗證一種logical outcome；activecomposition先建立nonempty preedit，第一tap只能completion、第二tap才editor action/newline。orderedCommits另驗證first+additional順序。performEditorAction false不加newline。policy immutable snapshot後metadata變動不得造成label/dispatch漂移，下一callback才更新。

使用者既定流程已批准inline執行與localcommit；writing-plans一般額外執行方式詢問省略。若查證出未界定產品決策才STOP。

## 測試清理補充

正式碼仍3檔。新增4個test/helper，另NonTextEditorServiceTest只有@After main-looper drain及imports，assertions未改。無UI的service callback現在寫入presentation state，須在Robolectric reset前送出snapshot通知；最小3類RED/control、ordered40與full190證明此範圍。temporary診斷類與B診斷插入均已移除／原文復原。
