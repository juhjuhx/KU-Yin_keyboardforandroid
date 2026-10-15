# R2 獨立審查

## 診斷準備切片

三個唯讀review方向分開：CI歷史／orchestration、Android lifecycle／test validity、privacy／source preservation。Reviewer沒有修改shared source或裝置。此審查不等於實際runtime或根因批準。

| Finding | Severity | Source／直接證據 | 處置 |
|---|---|---|---|
| capture exception掩蓋原visibility assertion | High | ImeRuntimeSmokeTest新增capture-first；原邊界4測2 assertion failures | 新debug boundary保留相同primary＋suppressed，4/4GREEN；新增onActivity亦保護；不送額外JUnit status |
| terminal寫入失敗仍exit0 | High | runtime_smoke EXITtrap／fake terminal為directory | 真RED 2!=0；失敗設定evidence_status，原App7保留；GREEN |
| install期間user改變仍enable/set | Medium | fake during_install switch5後發生mutation | RED後install完成重驗、mutation前重驗、明確user0；GREEN |
| B/C只比單向source keys | Medium | controlled workflow source guard | 完整filtered dict＝keys+digests；script/bootstrap/Gradle provenance補全 |
| snapshot probe transport failure當ABSENT | Medium | fake ADB exit13但原script0 | 真RED後PRESENT/ABSENT sentinel＋unknown/transport FAIL；GREEN |

`/root/r2_lifecycle_read`已核對原三個precondition wait、visibility wait及shell body與main逐字相同，40×250ms不改；原show/focus次數與順序保留；無新增IMM checkFocus API。直接核對09兩份XML各4/0/0/0及instrumentation compile。Source準備可批準，runtime尚未跑。

`/root/r2_ci_evidence_read`自行跑focused10測PASS，另外temporary fake faults驗證terminal/capture/probe fail→2、原App7→7且不跑Host、UNKNOWN sentinel拒絕；最新script/tests/workflow SHA核對一致。裁定可以正常push draft diagnostic PR，沒有未解High／Medium。完整報告與來源manifest保存在外部`CI唯讀比對/R2_CI編排獨立覆核.md`／JSON。前輪report文字與同步修改後hash有時間差，已明記不把舊finding套在新bytes。

`/root/r2_privacy_preservation_review`獨立重算R1 archive1185 hashes、536 original material references、18原工區所有heads/status/diff hashes以及8份frozen source，全部一致；protected diff＝0 bytes。只位於debug/androidTest，release無新diagnostic source set／network權限。Screenshot與platform dump本身可能有畫面內容，安全性依賴workflow fresh disposable synthetic emulator，不能稱任意emulator都安全。沒有隱私／保存範圍的blocking source defect，可push diagnostic draft PR。

三個方向僅批準診斷準備source；後續emulator case evidence review尚待執行，不繼承尚不存在的runtime APPROVE。

## OCR／其他技能

OpenCodeReview v1.12.13，單次provider health HTTP401 invalid x-api-key，BLOCKED_BY_PROVIDER；沒有OCR APPROVE。official-code-review所指定Haiku/Sonnet/Opus harness不可用，未冒稱完整官方pipeline；採上述真獨立source reviewers。

## 合併界線

PR10維持HOLD。須先有支持根因／disposition的actual trace、App5原assertionPASS、適用Host32實際執行與每個舊failure/skip裁定，再對exact PR source/merge SHA做獨立驗證。UNIT／compile／draft PR批準不能替代。

## 受控runtime與新觀測切片

Lifecycle reviewer獨立覆核controlled source manifest：A96、B98、C98個App/runtime source hash，0mismatch；89個app/src/main blob全同。原始XML三組App5均PASS、recreate真執行。B已focus/token/IC但IMM accepted=false之後平台顯示成功，C初次request前已visible；舊ea5 root未重現、UNKNOWN。原始A/B Host9FAIL使最終workflow exit1；不覆蓋為步驟success。外部「受控比較生命週期與根因獨立覆核.md」SHA3201337e5f46d491c848978da947b236c162a92c2866aa9c5ae7827b73a8cad5。

新source9f6d2e8獨立讀實際13-file diff、各source hashes、原始RED/GREEN與保護清單；裁定APPROVE_DIAGNOSTIC_CI_SLICE_ONLY，未解High/Medium=0，不批準merge/runtime/fullacceptance。新增发現两Medium：8-item共享ring可被cleanup淘汰BODY；clearComposition與ActivityScenario.close双failure可覆盖前者。分别23、25b有效RED→26 GREEN107；最後close-order26b compile再核對。環境fence、deadline、gesture、strict assertions與原normalBuild不變。

外部「獨立審查-Host觀測與失敗邊界-2026-10-14.md」及同名JSON包含source/evidence hashes、107 XML attrs、122 Python、1185 archive checksum與18原worktree／8份frozen來源檔雜湊全一致。Probe／普通最新CI仍待結果；不會用該source approval繼承runtime approval。

## UiAutomator窄修復的獨立裁定

Lifecycle reviewer直接讀38561946857／8c raw App5PASS＋infra1FAIL、180source hashes全對；source33 assertNull實際StaleObjectException，DESTROYED與getter-stale先通過。再讀38563163760／3fc raw App5PASS＋same infra1PASS、181hashes全對、相同API33/user0；裁定有效primitive RED→GREEN，不批准historical visibility root或完整Host。精確2.4 AAR hash/source/manifest與resolved graph亦核對。

Privacy/preservation reviewer獨立871行App runtime graph與單獨42c/42b Host runtime同一，uiautomator只Host androidTest；current33 identities／frozen26 manifest分開；1185archive、536materials、18舊工區與8份frozen來源檔雜湊再hash一致。發現真native bootstrap path未受guard（Medium），46有效RED→47七測GREEN修正；APPROVE_TEST_ONLY_DEPENDENCY_AND_GUARD，未解High/Medium0。不批准runtime/fullacceptance/merge/release。49全Python130在其report時間後結束，因此不偽稱report含該全量結果。

外部「失效節點runtime紅燈與依賴範圍獨立覆核.md」及「獨立審查-UiAutomator依賴隔離與保存-2026-10-14.md」／JSON含actual source/raw hashes。補圖前Medium證據缺口需以42b/42c覆核更新收據，不隱藏先前發現。新sequence/full suite需另外runtime review。

## 2026-10-15 focus／rawdump與本地前提覆核

Reviewer直接檢查same3fc失敗XML、per-test log與PNG：confirmed local test-precondition early-show；舊ea5曾served，未批准完整歷史root。debug focus8測GREEN的queued-focus oracle以真ViewRoot事件順序成立，原4assertions與40×250ms不弱化；只批准33b3ac8 source。報告「視窗焦點fixture修正與時序oracle獨立覆核.md」SHA a310ab041916763a0f6e4e71e7c0246c641801da87b438fbd6599a19c57d8805。

Rawdump reviewer讀3檔與API31+RW contract、53真RED／54GREEN，核對typed selfcheck只在recreated已true之後，不前移visibility判定；ca740f538feb13e5a7780e33a92f7c714bb5f7901ffb42ef071fcc4165ea44ed為source-only批准。

59b另獨立重算57份App XML及8份Host XML：282／0／0／0且native兩類10 body實際執行；Host107 UP-TO-DATE，四APK亦UP-TO-DATE，最新真正compile在55。14publicfiles／新cache exact3587ba3／4詞庫hash全部核實；舊cache為空，55與過早59保留FAIL；native tests/build/bootstrap未改，沒有弱化原assertions。外部「原始dump修復本地gate與詞庫前提恢復獨立覆核.md」SHA dd401a105153931261482112a110961c6264cd9d7f09a53fcb8899c839688a68，原始XML已另凍結。無High，可3895625 atomic commit及有限synthetic emulator實驗。

以上均不批准actual AndroidRW／五個真失敗dump、focus runtime、fullHost、完整ea5 root或PR10merge。新CI及各case仍需另一輪實際evidence review。

## 38660489452有限runtime覆核

Reviewer直接驗183source hashes、App5／Host2原XML與1→2→0 protocol、兩個typed case envelope、實際editor我／commit1及我現在／commit2、兩份25byte canary，全部吻合。首次show確實延後至focus/IC後，但IMM仍false後才visible；不把local IC等同remote ready。審查批准有限runtime及更新Draft PR12進完整normalCI，不批准fullHost／完整ea5因果／merge。另修正Low文檔：13／2是總stale，ZH06只保留8條recent PARENT，不能斷言全部13的phase；舊待結果敘述標歷史。完整外部收據另存同證據目錄。

## 完整正常runtime最終覆核

外部「正常完整runtime與逐案契約獨立覆核.md」SHA a82e88604e049bae17b56ac98de104c3e23c7cc84244c6083033482ebd6cb403；JSON cd4f45e62eacae928a650a59384fcb803d411742d9bbb37f9d4c1f5f22d66945。Reviewer自行讀App5、Host33原XML／protocol／32owner envelopes、exact merge signed Git object與tree、source183及原source assertions：App5PASS；Host24PASS9SKIP0FAIL，其中matrix23PASS9INCONCLUSIVE、infra1PASS。正常App observer OFF、原recreate與visibility assertions真執行。

舊9FAIL七案原契約真PASS；B02/B03到原有context／semantics gap仍不算完整修復。舊8SKIP只ZH06轉PASS；C04只有nullIC及81個空completed snapshot，不作完整nontext PASS。stale總69，不由recent ring推全phase或宣稱效能改善。批准完整「執行與已觀測testinfra契約」範圍，無High；不批准fullmatrix、historical ea5完整因果或merge，PR10 HOLD／PR12 Draft。

CI reviewer此次有partial：核對merge parents/tree與389個JVM cases；其後額度中斷，沒有完整APPROVE。它提出KSP pin漂移疑點，已由實際git diff、tomllib比較與另一reviewer證偽：catalog只uiautomator2.3→2.4，googleDevtoolsKsp仍2.3.5。KSP/AWT error annotation真存在，舊7f/9f亦有相同訊息；不隱藏、不把pin未改誤說工具鏈無錯，另留後續工具診斷。

65保存收據重新核18原工區HEAD/status/diff與公開APK；0mismatch、19總數、product protected diff0，未重算整個archive或讀私人DB。最新docs仍做links／consistency／diff gate；doc-only提交不變更03ac已驗程式source，未將doc commit冒充新的runtime執行。
