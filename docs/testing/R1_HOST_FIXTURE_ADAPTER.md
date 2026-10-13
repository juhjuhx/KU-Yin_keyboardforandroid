# R1：外部 host fixture 適配交接

工區：`.worktrees/r1-host-fixture-adapter`；分支 `fix/r1-host-fixture-adapter`；base `a6a4c654845338521700121ac06586887d1ec085`。本次改 `acceptance-host` 的 androidTest／sharedTest／JVM tests、凍結來源與當前 identities 的 Python regression fixtures 及本文件；沒有修改 `app/`、native、product permissions、個人字典或既有凍結 parser／manifest，作者未執行 Git／裝置操作；提交與 PR 由整合者負責。

## 問題與適配

舊 CI host HTML 是 32 案、15 PASS／9 FAIL／8 skipped。八個組字 failure 的 stale stack 出在 `tapPhonetic` polling 後的未保護補讀；EN02 頂層 error 出在 cleanup，不能據此判定英文 decoder 故障。原候選 selector 要與 clear chip 同列，但 S1 已是 preedit/header Row 加下方 CandidateStrip。舊 B02/B03 要 candidate tap 不提交，與 P0/S1 的即時完成契約相反。

目前適配如下：

- `ExternalUiObservation` 將 UIAutomator hierarchy 讀成完整 immutable snapshot；stale 使整份讀取失效，從 fresh query 重讀，不略過單節點。捕獲上限 1,000 nodes，超限保留 `BLOCKED_SELECTOR`，不使用部分樹。
- `freshUiRead` 只有 read callback，使用同一 absolute deadline；stale retry 不重設期限，逾時才完成的結果也不接受。Android bridge 對應真正 `StaleObjectException`；普通 JVM 測試以可控制 clock／邊界 exception 驗證此機制。UIAutomator 單次 API 呼叫不能由 fixture 強制中止，因此有界是 polling／結果接受的界線，不保證阻塞 API 的硬 wall-clock 中斷。
- 所有產品 selector 從 snapshot 讀屬性；live UiObject2 只用於最後一次 gesture。`click()` 在 retry 之外；gesture stale 不自動重送，仍為真失敗。
- strict preedit 保留唯一 clear marker、同最小 subtree、唯一同列非 clickable 文字 leaf；拒絕 keyboard glyph、candidate、外部 package、雙 leaf 與歧義。不同 node 即使同 label／bounds 也不合併成假唯一。
- collapsed 候選須位於 clear/header 下方、當次實際鍵盤首排上方；expanded grid 使用 header 與 page controls 的可見邊界。沒有鍵盤／pager anchor 就拒絕猜測。英文 soft key 也受鍵盤首排邊界限制，避免點到同字母候選。不把 Compose testTag 當 resource ID，不假定 onClickLabel 是 contentDescription，不使用 BMP-only 漢字 regex。
- P0 六案與 B02/B03 共用完成路徑：真可見 candidate tap 一次，等待實際 editor effect 與 quiet window，斷言 actual text、commit delta **1**、lastCommitText 等於選定完整 label、action/newline delta **0**、clear marker 消失，再安定後重查。B02/B03 後續 `ㄅ` 必須是新組字、保留已提交 prefix且不再次 commit。計算期望值使用組字前 prefix，不重複附加含 composing projection 的中途 currentText。
- fresh read deadline 耗盡時保留 `BLOCKED_SELECTOR`；不以早先 snapshot 宣稱 decoder 未更新。其他 assertion／runtime／真 cleanup failure 仍由既有 `CaseFailurePolicy` 處理，沒有將 FAIL 改成 PASS。
- D13/D14 的既存 oracle 將含 composing projection 的 `before.currentText` 再附加 commit，會錯算文字。現在 Driver 在同一 host UI-thread callback 讀取真 `RecordingEditText.text`、`BaseInputConnection.getComposingSpanStart/End` 與 immutable editor snapshot，先核實兩者文字相同；shared oracle 按真 UTF-16 interval replacement 保留 prefix／suffix。missing／invalid span 不改用 append 猜測，仍 fail closed。Enter 後 quiet window 重驗 commit delta **1**、action/newline delta **0**、非空 commit、正確替換文字，並確認真 host span 已為 `-1/-1`、可見 clear marker 消失；第二次 Enter 維持原 normalized action／newline 契約，沒有 callback false 補償。

B02/B03 的既有 `NATIVE_CONTEXT…UNOBSERVABLE` 結果保留。B01 的額外寬度／方向、C 系列的 learning observer、D17 的 Android IPC boolean 範圍也未移除。B03 不再於候選完成後要求舊組字存在；composition Enter 仍由 D13/D14 原斷言測試。四個 instrumentation class 的原 32 個方法 identity 不變，舊凍結 archives 不回填新 expectations。

## RED／GREEN 證據

所有本地記錄在 git-ignored `build/r1-fixture-evidence/`。

| 階段 | 結果 | 記錄 |
|---|---|---|
| 首次 sandbox attempt | Gradle lock socket 被拒絕，**不是 RED** | `00-sandbox-blocked.log` |
| 抽出舊同列 selector／單次外側 read／B02/B03 no-commit 契約的可執行 RED | 20 tests、11 assertion failures、0 errors／skip；exit 1 | `01-red.log`、`01-red.xml`、`01-red-summary.json`、`01-red-adapter.kt`、`01-red-tests.kt` |
| 首輪接線 GREEN | Host 4 類 57 tests、0 failure／error／skip；instrumentation APK 組裝成功；exit 0 | `02-green.log`、各 `02-green-TEST-*.xml` |
| 邊界 RED | 23 tests、3 assertion failures：foreign preedit text、重疊 target、讀取超過 deadline；exit 1 | `03-boundary-red.log`、`03-boundary-red.xml` |
| 候選／stale 階段 GREEN | Host 4 類 **60 tests**（原 37＋新增 23），0 failure／error／skip；instrumentation APK 組裝成功；exit 0 | `04-final-green.log`、各 `04-final-green-TEST-*.xml`、`04-final-green-summary.json` |
| timeout 分類交接編譯 | 同一 host JVM gate＋更新後 instrumentation APK 組裝 | `05-handoff-green.log`、`05-handoff-green-exit.txt`、各 XML |
| D13/D14 replacement oracle RED | 共 11 tests：oracle 9＋space/tab 2；6 assertion failures、0 errors／skip；token 2 案即 PASS，regex 未改；exit 1 | `09-composition-red.log`、各 `09-composition-red-TEST-*.xml`、`09-composition-red-oracle.kt`、`09-composition-red-tests.kt` |
| 最終 host GREEN | **5 類 71 tests**（原 37＋snapshot/read/completion/token 25＋composition replacement 9），0 failure／error／skip；更新後 instrumentation APK 組裝成功；exit 0 | `10-composition-green.log`、各 `10-composition-green-TEST-*.xml`、`10-composition-green-summary.json` |

RED 測試執行真 shared selector／read／completion oracle，最終 instrumentation 也呼叫同一實作；不是 missing import、compile error 或將產品 decoder mock 成通過。snapshot 幾何是來源推導的 synthetic hierarchy，仍未證明裝置實際 accessibility tree 相同。

環境：

```text
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
```

RED 命令：

```bash
./gradlew :acceptance-host:testDebugUnitTest --tests io.github.juhjuhx.kuyin.acceptancehost.VisibleUiAdapterTest --console=plain
```

GREEN／編譯命令：

```bash
./gradlew :acceptance-host:testDebugUnitTest :acceptance-host:assembleDebugAndroidTest --console=plain
```

沒有可呼叫的 `lsp_diagnostics` 工具；以實際 Kotlin JVM／AndroidTest compilation 取得型別與編譯診斷。root 的整合 app／host／Python gates 是另一層證據，需在其交接中報告。

## 下一層驗證與未證明範圍

本次 `RUNTIME=NOT_RUN`，不能將 pure tests 或 APK 組裝稱為 32 案 runtime PASS，也未證明原 preedit timeout 根因消失。

下一次應以新 host／test APK 與當前 32-test inventory 執行獨立 CI emulator suite，保存 raw case envelopes、JUnit XML、cause/suppressed stack 與必要的 synthetic hierarchy／畫面。逐案區分已達成斷言、selector 缺口、原定 INCONCLUSIVE 與 product failure，禁止將 skipped 計為 PASS。fresh read 若仍 timeout，先核對實際 hierarchy／組字觀察與事件，不增加盲目 click retry 或放寬 strict preedit／delta。

再以 exact 發布產品 APK 做手機候選捲動、展開、翻頁、選取提交與繼續輸入復驗；不使用舊凍結 v3 parser／hash 驗新 host，不為測試清除個人字典。physical／OEM／TalkBack、完整 learning erase、native identity observer 與 production readiness 仍 OPEN。

## 獨立審查

2026-10-12，fresh reviewer `/root/r1_architecture_audit` 直接核對最終 9 檔 diff、09 RED／10 GREEN XML 與 source hashes，裁定 **APPROVE（僅 fixture 修正範圍）**。沒有未解 blocking finding。曾提出的 whitespace regex finding 經原始 source 字元核對為 false positive，正式撤回；space／tab regression 在 RED 階段即 PASS，regex 未修改。外部原始證據備份於 `/Users/huang/Desktop/KU-Yin-Engineering-Archive-2026-10-12/r1-fixture-evidence/`；runtime 結果依後續 CI raw evidence，不從此核准推論。

## 全量 CI 的 frozen/current 契約整合

PR #10 首次納入全 Python gate，run `38225839753` 在 109 tests 中產生一個真 assertion RED：原 `test_source_hashes_match_frozen_instrumentation_sources` 將 6595b684 的 8 個 frozen SHA 套在已更新 Dachen／Driver 上。原 runner、manifest、其 26-test identities 與 frozen APK 不可重算。現在完整保留 8 份 6595b684 exact source bytes（53,224 bytes）作不編譯的 `scripts/tests/fixtures/p1e-frozen-instrumentation/`；仍按原 8 digest 核對，缺檔由原 skip 改硬 FAIL，新增 missing／tamper negative tests。另 `current_instrumentation_identities.json` 明列本次原有 app 5／host 32 identities，對 current source 比較完整 set、count、unique count，未支援的 annotation 形狀 fail closed。這項修正讓歷史 proof 與 current inventory 各自受驗證，沒有換掉 frozen parser 或放寬 runtime assertions。

全 Python **113 tests PASS／exit 0**、10 contracts PASS（外部 `fixture-python-green.log`／`fixture-contracts-green.json`）。凍結原件 bytes 與原 source base 比對一致；歷史 source fixtures 不含使用者資料、不加入任何 APK。

## Scoped idle read：來源成本與最小 fixture 修正

CI `38225649247`（087ff39）仍是 32 案中的 **15 PASS／17 SKIP／0 FAIL**，不能將原 9 FAIL 改 SKIP 稱為 runtime 修好。該輪舊 artifact 沒有 raw JUnit XML／完整 case error details，也沒有單次 capture 耗時；17 SKIP 的個別原因仍未證明。

依本案 pinned AndroidX UIAutomator **2.3.0** 的 [Google Maven 原始來源](https://dl.google.com/dl/android/maven2/androidx/test/uiautomator/uiautomator/2.3.0/uiautomator-2.3.0-sources.jar)（SHA-256 `e2073bbf776baf17ffe232e2eb38a158d93a4d5911eff48a1a5bb9025f8d005a`），`UiObject2.getAccessibilityNodeInfo()` 每次先呼叫 `UiDevice.waitForIdle()` 再 refresh；text／package／parent／children／bounds 等 getters 及 registry 使用的 hashCode／equals 都會經過這條路徑。`Configurator` 預設 timeout 為 10,000 ms，`QueryController.waitForIdle(timeout)` 吞掉 TimeoutException、只寫 library warning。已證實重複呼叫成本機制，尚未量測本輪裝置實際耗時；不能推論每個 getter 必定等待 10 秒或這是所有 SKIP 的根因。

`scopedIdleRead` 與 Android bridge 共用以下生命週期：取得原 timeout → 以同一 absolute deadline 的剩餘時間 explicit idle 一次 → 檢查 deadline 未耗盡 → 暫設 timeout 0 → 完整 capture → finally 恢復原 timeout。同一 process 的 scoped captures 以共用 lock 序列化，stale 每次 attempt 都先恢復再由 fresh query 重讀；未增加或重設 5 秒 deadline。零 timeout 仍保留 getter 的 refresh／library 呼叫，沒有把觀察改成 XML 或部分樹。

初始 idle／disable／capture／assertion／stale 的一般錯誤仍傳出。若 restore 也失敗，保留原 primary throwable 並附 suppressed；單獨 restore failure 直接失敗；帶 suppressed 的 stale 不 retry，避免把 restore failure 吞掉。最後一次 gesture 與 actual editor quiet-window 均在 scope 外，沒有 click retry，strict preedit、actual composition span、exactly-one commit、action/newline delta 與原 32 案 identities 未改。

既有 case envelope 的 `ui_reads` 只新增 capture／attempt／complete／node counts、累積與最大耗時、最後 deadline 及 complete 狀態，不含 raw hierarchy、文字或 keystroke。`UiObject2.equals()` 本身可能吞 stale 而回 false，registry 因而可能重複登記；這是此 library transport 的限制，不宣稱 atomic snapshot。不同 node 不按 label／bounds 合併，歧義仍拒絕，1,000-node 上限仍 fail closed。

新增純 JVM regression 直接執行 shared helper，使用 fake Configurator boundary／clock 驗證正常順序、idle 耗盡、late capture、各種 error 恢復、primary/suppressed 保留及同 deadline stale retry。`11-scoped-red.log/xml` 是 **15 tests／15 assertion failures／0 errors／0 skip，exit 1**；可執行 baseline passthrough 已接到 External capture，不是 missing import 或重寫 oracle 的假 RED。完整 GREEN 是 **6 類 86 tests／0 failure／0 error／0 skip，exit 0**（原 71＋本次 15）；Kotlin JVM／AndroidTest compilation、host test task 與 AndroidTest package/assemble 均 fresh executed。證據為 `12-scoped-green.log`、各 XML、summary 與 command JSON；最初工具核准 timeout 尚未執行命令，同一命令依工具指示重試一次後成功，另留 approval note。

本切片 focused RED：`./gradlew :acceptance-host:testDebugUnitTest --tests io.github.juhjuhx.kuyin.acceptancehost.ScopedIdleReadTest --console=plain`。完整 GREEN：`./gradlew :acceptance-host:testDebugUnitTest :acceptance-host:assembleDebugAndroidTest --console=plain`。`13-scoped-actual-diff.patch`／source hashes／scope review 保存五個授權檔案的實際修改；protected app／host main／build manifest／frozen sources 的 diff 為空，diff 與新檔 whitespace checks PASS。沒有可呼叫的 LSP 工具，以上 fresh Kotlin compilation 提供編譯診斷。

本切片 **RUNTIME=NOT_RUN**；下一輪僅在既有 app runtime prerequisite 可完成後，以相同 32 案與嚴格 editor oracle 核對 raw case XML/envelopes 與 `ui_reads`。觀察與分類不足仍 INCONCLUSIVE；不從純測試或編譯推論 17 SKIP 消失，也不因 Layer1 的產品 runtime failure 改動 app 或降低斷言。

## 2026-10-13 收尾：外層錯誤路由與 composite failure

Root 實際 diff review 發現 `Driver.poll` 外層仍無條件略過 stale；root 與獨立 architecture reviewer 另確認兩處 deadline catch 會把含 suppressed restore failure 的錯誤轉成 `BLOCKED_SELECTOR`，以及帶真 suppressed failure 的 `CaseInconclusive`／JUnit assumption 仍可能終止為 SKIP。這三個 routing 缺口是本次 fixture 的收尾修正，不是產品 timeout 根因證據。

現在 `poll` 對 stale 使用同一 `retryableScopedStale` predicate；`captureUi` 與 `poll` 的 deadline catch 遇 suppressed error 直接 rethrow，保留 primary／suppressed。普通 host metadata stale 維持讀取 retry；普通 deadline 維持原 INCONCLUSIVE。`CaseFailurePolicy.isInconclusive()` 只接受 suppressed 鏈全為 scope limitations 的純 INCONCLUSIVE，真錯誤或 cycle fail closed。實際 `acceptanceCase` 呼叫同一 shared `terminalCaseFailure`：純 `CaseInconclusive` 才包成 JUnit assumption；composite `CaseInconclusive` 保留原 primary；composite 原 JUnit assumption 以 `AssertionError(cause=original)` 終止，避免 JUnit 仍將其算成 skip，原 suppressed evidence 保留在 cause。ordinary failures 仍保持 identity；cleanup policy 也不將 composite primary 降級。

可執行 RED 先抽出原 terminal routing 並接到真正 Driver，保留舊 type-only classifier。`15-composite-policy-red.log/xml/summary` 為 **20 tests／7 java.lang.AssertionError failures／0 errors／0 skip，exit 1**；不是 missing import、compile error 或在測試中重寫 oracle。其餘 13 案包含原 9 案與純 INCONCLUSIVE 的正常分類。

最終命令：`./gradlew :acceptance-host:testDebugUnitTest :acceptance-host:assembleDebugAndroidTest --rerun-tasks --console=plain`。`16-final-green.log`／各 XML／summary／command JSON 證明 **6 類 97 tests／0 failures／0 errors／0 skip，exit 0**（原 71＋scoped 15＋composite policy 11）；**53 tasks executed**，含 Kotlin JVM／AndroidTest compilation、真 host unit task 與 AndroidTest package/assemble。`14-outer-stale-green*` 是先前 86 案的中間階段；11–15 的歷史證據全部保留。沒有可呼叫的 LSP 工具，以這次 fresh Kotlin compilation 提供編譯診斷。

本次最終七檔 diff／hash／scope evidence 為 `17-final-actual-diff.patch`、`17-final-source-hashes.json`、`17-final-scope-review.json`；16 command JSON 的六個 Kotlin execution hashes 與最終 source 核對一致。13 的五檔 hash 是歷史階段，不能套在最終 Driver。strict preedit、composition span／settled actual editor oracle、exactly-one commit、action/newline delta、gesture 與原 instrumentation identities 未改。app／host main／build manifests／frozen sources diff 為空；本作者僅用 read-only Git 核對 diff，沒有 Git mutation、裝置操作或遠端 CI。

Pinned dependency provenance 位於 `/Users/huang/Desktop/KU-Yin-Engineering-Archive-2026-10-12/r1-fixture-evidence/uiautomator-2.3.0-source-provenance.json`，包含官方 URL、當時來源 SHA／94,344 bytes、method locations、本機 exact AAR 與 bytecode disassembly 路徑／SHA。原 source jar 僅在 Python memory 檢查，bytes 未持久保存；本輪沒有重抓來源，JSON 明確保留此限制。root 負責獨立 final review、local commit 與 offline backup。

最終仍是 **RUNTIME=NOT_RUN**。純測試與 instrumentation APK 編譯不證明 17 SKIP 已修復、32 案 runtime PASS 或產品 readiness；本輪未重新執行 remote CI。
