# R1：外部 host fixture 適配交接

工區：`.worktrees/r1-host-fixture-adapter`；分支 `fix/r1-host-fixture-adapter`；base `a6a4c654845338521700121ac06586887d1ec085`。本次只改 `acceptance-host` 的 androidTest／sharedTest／JVM tests 及本文件；沒有修改 `app/`、native、product permissions、個人字典或既有凍結 parser／manifest，作者未執行 Git／裝置操作；提交與 PR 由整合者負責。

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
