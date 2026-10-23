# R3.7 Host Observer Repair

範圍：TEST_INFRASTRUCTURE_ONLY；原始基線 a6e346dded420a00012678115d04b7a689399e71。沒有新的Emulator／CI／實機。本輪不合併PR20/27。

## 根因與最小修正

原 Driver 對整個產品 package 查詢後逐個讀 visibleBounds；無關的失效 node 可在篩選 label 前拋 Stale。tapPhonetic baseline、clearComposition initial observation、tap timeout診斷另有 poll 外查詢邊界。原 poll 吞掉Stale直到deadline，可能將持續觀察失敗誤當selector缺失。

新查詢先用同package精確desc/text或phonetic textContains縮小集合，再保留原semanticToken、visibility、clickable祖先、唯一性及strict preedit幾何。baseline與cleanup僅在原5s／50ms有界poll內重新查詢；點擊不重試。整份stale observation作廢，不忽略其中單個節點湊出唯一匹配；deadline最後仍Stale則AssertionError保留原cause。timeout診斷使用最後完整觀察的文字。

未修改strictPreedit、candidate predicate、Enter／exactly-once／editor ACK、原CoreTypingAcceptanceTest、CaseFailurePolicy、產品或JNI。UiAutomator內部idle仍可能超過外層觀察期限；不宣稱hard wall-clock保證。

## Fixture與RED/GREEN

Gradle只把canonical AcceptanceDriver byte-identical staged進JVM test source；無第二份手抄observer。使用既有Robolectric4.16.1/UIAutomator2.3.0，shadow僅root、idle、gesture transport。真ByMatcher/UiObject2 getter由AccessibilityNodeInfo.refresh=false拋真StaleObjectException。

最終20項fixture對原Driver：13 FAIL／7 PASS／0 SKIP（exit1）。同一fixture＋修正Driver：20 PASS；完整Host JVM57 PASS／0 FAIL／0 SKIP，獨立Reviewer直接執行；Debug及androidTest assemble均exit0。完整XML／命令／review收據見中央Workspace R3.7-biogm2uj/HOST_FINAL20_RED_XML、HOST_INDEPENDENT_FINAL_GREEN_XML。

- 8個case-ID wrapper（ZH01/02/04/05、B02/03、D13/14）重現共用observer邊界，**不代表8個完整Runtime流程已執行**。
- before-tap及before-clear transient target stale：原Driver RED／新GREEN，gesture=1。
- post-click persistent target stale：原timeout後diagnostic漏出Stale，新明確AssertionError＋cause，gesture=1。
- post-click transient target stale在原Driver已PASS，僅作既有行為保護。
- EN02缺non-clickable preedit leaf仍INCONCLUSIVE；cleanup真正失敗為主error，body保留suppressed，整案FAIL。
- ambiguous/clickable/zero-bounds preedit不被接受；未變preedit仍FAIL且gesture=1。
- 真AcceptanceEditorActivity/RecordingEditText focus、onCreateInputConnection、commit recorder在Robolectric驗證；不是WMS focus／served IME／實機證明。

## Runtime／回退

歷史CI39882220869依然15 PASS／9 FAIL／8 SKIP。九案未做新Runtime驗收；八SKIP原因和原assertion未改。PR27候選區幾何不是本切片。
回退僅revert本切片commit；不動使用者資料、舊工作樹、舊Alpha或簽章。後續須另批准健康環境與固定來源Runtime，不能靠本機57 PASS放行Release。
