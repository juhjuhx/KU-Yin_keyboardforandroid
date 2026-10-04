# P1-D RED/GREEN 證據

目前裁定：LOCAL VERIFIED。最新 A–L gates 與 full 40 類 190 測皆通過，見本文末段及 `final-green/`；下列早期失敗保留為歷史診斷證據。

## RED（產品程式未改）

BASE_SHA：870fc03cbfe8e5107b8ff8dc13478ac7754eec9f。命令：

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --tests 'com.example.ime.EnterPolicyServiceTest' --tests 'com.example.ime.EnterUiWiringTest' --no-build-cache --console=plain
```

exit1；2類23測16failures、0errors，編譯成功。實際service callback／onCreateInputView ComposeView／InputConnection接線取得的assertion RED，非編譯錯誤。XML与gradle.log完整封存於red/。

主要expected→actual：NO_ENTER_ACTION+SEND預期Commit(\n,1)→Action(4)；GO→Action(2)；custom73→masked4；custom+NONE/id0→newline；NO_ENTER_ACTION畫面仍傳送/前往；PREVIOUS無上一步；custom label不出現；metadata被修改後原label仍SEND而dispatch改GO。

composition SEND、空composition單次delegation、orderedCommits、NONE/UNSPEC與false-return不補newline在舊碼已GREEN，作為保留契約。

- PREVIOUS has matching Traditional Chinese presentation：java.lang.AssertionError: Assert failed: The component is not displayed!
- custom newline label remains text rather than newline icon：java.lang.AssertionError: Assert failed: The component is not displayed!
- English and symbols receive the same normalized custom presentation：java.lang.AssertionError: Failed to inject touch input. / Reason: Expected exactly '1' node but could not find any node that satisfies: (Text + EditableText contains '確認' (ignoreCase: false)) /
- custom label cannot bypass NO_ENTER_ACTION presentation：java.lang.AssertionError: Failed to inject touch input. / Reason: Expected exactly '1' node but could not find any node that satisfies: (ContentDescription = '換行' (ignoreCase: false)) /
- NO_ENTER_ACTION suppresses SEND presentation and dispatch：java.lang.AssertionError: Failed: assertDoesNotExist. / Reason: Did not expect any node but found '1' node that satisfies: (Text + EditableText contains '傳送' (ignoreCase: false)) / Node found: / Node #1424 at (l=301.0, t=241.0, r=360.0, b=289.0)px / Focused = 'false' / Text = '[傳送]' / Actions = [OnClick, RequestFocus, SetTextSubstitution, ShowTextSubstitution, ClearTextSubstitution, GetTextLayoutResult] / MergeDescendants = 'true' / Has 51 siblings /
- active composition consumes Enter without also inserting newline：java.lang.AssertionError: Failed to inject touch input. / Reason: Expected exactly '1' node but could not find any node that satisfies: (ContentDescription = '換行' (ignoreCase: false)) /
- NO_ENTER_ACTION suppresses GO presentation and dispatch：java.lang.AssertionError: Failed: assertDoesNotExist. / Reason: Did not expect any node but found '1' node that satisfies: (Text + EditableText contains '前往' (ignoreCase: false)) / Node found: / Node #1804 at (l=301.0, t=241.0, r=360.0, b=289.0)px / Focused = 'false' / Text = '[前往]' / Actions = [OnClick, RequestFocus, SetTextSubstitution, ShowTextSubstitution, ClearTextSubstitution, GetTextLayoutResult] / MergeDescendants = 'true' / Has 51 siblings /
- custom label is shown and dispatches its own id：java.lang.AssertionError: Assert failed: The component is not displayed!
- dispatch uses the same snapshot as presentation until next callback：java.lang.AssertionError: expected:<[Action(id=4)]> but was:<[Action(id=2)]>
- explicit multiline NO_ENTER_ACTION retains newline：java.lang.AssertionError: expected:<[Commit(text= / , cursor=1)]> but was:<[Action(id=4)]>
- NO_ENTER_ACTION suppresses GO dispatch：java.lang.AssertionError: expected:<[Commit(text= / , cursor=1)]> but was:<[Action(id=2)]>
- non text Enter plumbing preserves English and learning restriction：java.lang.AssertionError: expected:<[Commit(text= / , cursor=1)]> but was:<[Action(id=4)]>
- NO_ENTER_ACTION suppresses SEND dispatch：java.lang.AssertionError: expected:<[Commit(text= / , cursor=1)]> but was:<[Action(id=4)]>
- custom action with NO_ENTER_ACTION inserts newline：java.lang.AssertionError: expected:<[Commit(text= / , cursor=1)]> but was:<[Action(id=4)]>
- custom label with NONE still dispatches custom id zero：java.lang.AssertionError: expected:<[Action(id=0)]> but was:<[Commit(text= / , cursor=1)]>
- custom action dispatches custom id instead of masked standard action：java.lang.AssertionError: expected:<[Action(id=73)]> but was:<[Action(id=4)]>

## GREEN

Focused 已執行；完整驗收尚未通過。pure 政策測試在產品實作前寫入；不存在的新 API 不以 compiler 錯誤冒充 RED。

| gate | 新鮮結果 |
|---|---|
| A pure policy | 18/18，exit0 |
| B EnterCommandContract | 5/5，exit0 |
| C service/editor | 6類41/41，exit0 |
| D Compose Enter | 11/11，exit0 |
| E P1-C | 4類25/25，exit0 |
| F P1-B focused | 6/6，exit0 |
| G P1-A | 3類12/12，exit0 |
| H full第二輪 | 40類190測，5 failures、0 errors/skipped，exit1 |
| I contracts / J bootstrap / K Debug / L instrumentation | P1-D 尚未執行，等待 full 修復 |
| required Release | P1-D 尚未執行；既有缺 keystore 不是本輪實測 |

逐步命令與計數在 green-evidence.json；green/ 是 focused XML，不代表全量 GREEN。

## 第一輪全量失敗與 harness 修正

H-full exit1：190測計數中4 failures、1 skipped；新UI focused通過，但後續Dachen tests在setContent遭ComposeIdlingResource timeout。為避免每案60秒重複等待，保存thread dump後只SIGTERM本worktree test worker（exit143）；此為中斷/失敗證據，不是完整GREEN。full-idle-failure/留存log、Dachen XML與dump。

第一個候選根因：新 harness 透過 AndroidView 掛載 service 自己的 ComposeView；其 DisposeOnLifecycleDestroyed 依賴獨立 ImeLifecycleOwner，可能留存內層 composition。新增 tests @After 顯式 disposeComposition、lifecycle.onStop/onDestroy。第二輪 full 仍失敗，所以此措施未證明解決根因。沒有改產品或既有 B 測試，也沒有拉長 timeout。

修正後新EnterUiWiringTest＋DachenKeyboardLayoutTest連續執行：17測0failures/errors/skipped，exit0。需再次full suite，尚不聲稱全量通過。

## 第二輪全量失敗與顺序更正

命令：`JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --no-build-cache --console=plain`。完整退出1；40類190測，5 failures、0errors、0skipped。所有 XML、log、thread dump 保存 full-idle-failure-second/。

5個失敗皆為 DachenKeyboardLayoutTest 的 LEGACY graphics 方法，在 setContent 發生 AppNotIdleException（60秒）；該類 NATIVE graphics 方法通過。其他185測通過。

合跑17測的 XML timestamps：Dachen 14:33:40Z → EnterUi 14:33:50Z；與 full 的 EnterUi → NonTextEditorService → Dachen 方向相反。因此17測不能證明新的 UI 測試已正確釋放資源。Native/legacy sandbox 或 attached ComposeView 重建等仍只是候選，不能記作確定根因。

## 工具阻塞與接續診斷

命令工具及子代理無法建立程序：Too many open files (os error24)，包含非login、一般與核准執行。既有第二輪測試已完整退出。檔案仍可透過 node_repl/fs讀寫；child_process spawn EPERM。Computer Use Terminal 備援明確被工具安全限制禁止，未再嘗試其他方式繞過。

下一次必須先恢復命令工具，再以臨時 JUnit Suite 明確指定 EnterUiWiringTest → DachenKeyboardLayoutTest，不能以 Gradle --tests 參數順序推定執行順序。若重現則縮到新 harness；若未重現再最小加入 NonText/Hitbox/Greeting 前置。確認 detach → dispose/destroy 與 AbstractComposeView.ensureCompositionCreated/onMeasure/registry 的實際關係後才修，不跳過 B 測試、不拉長 timeout、不改產品以掩蓋測試問題。

## 2026-10-05 接續：已確證的 idle 根因

命令工具恢復；重新確認 HEAD870fc03、branch fix/p1d-enter-policy，8個待提交產品/測試（包括新增的既有C測試清理）。前三份diagnostic順序：EnterUi→Dachen17PASS；Greeting(NATIVE)→EnterUi→Dachen18PASS；加入Hitbox/EnterPolicyService/NonText明確6類順序，40測5fail，重現full條件。

idle-probe只縮減診斷中的Dachen方法數，不改回歸閘門；36測1fail。timeout即刻的實際旗標只有hadSnapshotChanges=true；hadAwaitersOnMainClock/hadRecomposerChanges/hadPendingSetContent/hadPendingMeasureLayout全部false。GlobalSnapshotManager.sent=true；dispatcher scheduledTrampolineDispatch/scheduledFrameDispatch=true；toRunTrampolined僅留GlobalSnapshotManager continuation。排除本次timeout由measure/layout或動畫awaiters造成。

三類最小案例（SDK36/LEGACY，無service/native/Enter/AndroidView/Dachen）：先Compose初始化，再無ComposeRule寫mutableState，最後Compose顯示。idle-minimal-red保存3測1fail，最後C逾時60.059s，旗標與原重現一致。唯一控制差異是在writer尾端shadowOf(Looper.getMainLooper()).idle()；idle-minimal-green保存3/3PASS，最後C0.044s。temporary診斷源與XML/log均封存。

實際Compose1.7.0 AAR bytecode：GlobalSnapshotManager.sent CAS與AndroidUiDispatcher已排程旗標阻止重新post；正常AndroidMain dispatcher通知與Compose test clock不同。Robolectric4.16.1 reset清空Handler/Choreographer callback，沒有重設上述Compose旗標。因此未排空寫入通知就reset，會留下有工作而無可執行callback的dispatcher；snapshot-only pending使後續Compose永久idle spin。唯讀idle_diagnosis獨立查閱bytecode/XML確認同一機制。

最小正式測試修正：EnterServiceHarness.close尾端drain main looper；NonTextEditorServiceTest @After同樣drain，因onStartInput現在更新Enter presentation而無UI案例仍有snapshot寫入。沒有修改任何既有assertions、P1-C產品邏輯或P1-B源碼。所有暫時診斷類已移除；Dachen測試與checkpoint原文相同。

修正後同一6類固定順序，完整40測0failures/errors/skipped、exit0（ordered-ui-chain-green）。接著依A→H重新執行fresh gates；完整結果以final-green/jvm-results.json為準，不將歷史失敗XML覆寫為成功。

## 最終驗收（2026-10-05）

final-green/jvm-results.json：A18、B5、C41、D11、E25、F6、G12全exit0；H完整40類190測0failures/errors/skipped、exit0。禁用build cache，test task實際執行，XML封存按gate分目錄。

10個當前Python contracts全部exit0（contracts.json）；native bootstrap exit0並核對固定prebuilt3587ba3355711f0aca50136e787719f6562676b8（bootstrap-result.json）。Debug exit0；compileDebugAndroidTestKotlin exit0；Release exit1僅packageRelease簽名storeFile my-upload-key.jks缺失（build-results.json與rawlog）。最初驗證runner對Release採舊版error字串分類為UNEXPECTED_FAILURE；閱讀實際Gradle錯誤後修正分類，未重跑或修改簽名。

Debug APK：29158832bytes；SHA256 b223620743831e0370a31a65dcc998e6a74dfe70d45e020036c4f8b4468e39c9。adb devices -l exit0、0 targets；runtime NOT_TESTED（runtime.json）。docs更新後check_docs_consistency再次exit0。

最後完整驗證內容與固定review.patch一致；程式提交75b93a3b68867fd6ec4c6adadd3e279d71e98104沒有任何review後source改動。不為歷史idle失敗或只有文件更新重跑無關gates。
