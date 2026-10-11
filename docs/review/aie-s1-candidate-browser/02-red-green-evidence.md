# S1 RED／GREEN 證據

原始 logs/XML 位於本工區 `build/aie-s1-evidence/`。所有 Gradle 呼叫串行；JDK21，SDK36.1，固定 native pins。沙箱 socket／測試 selector／缺少 assertion API 的編譯錯誤沒有列作有效 RED。

## 有效 RED

| 回歸 | 原實作結果 | 原始證據 |
|---|---|---|
| stale candidate、wrong page、snapshot／expanded controls | 8 測 6 敗；實際 assertions，非 compilation error | browser-red.log、StateTest-red.xml、UiTest-red.xml |
| snapshot 身份／失效 | 8 測 6 敗 | snapshot-red.log/XML |
| hide 後捕獲舊候選 | 2 測 1 敗；editor 仍收到舊 tap | lifecycle-red.log/XML |
| 跨 generation pointer、橫向高度、hide 後 symbol Enter | 10 測 4 敗；包含早期 font oracle，見下方獨立核對 | gesture-layout-red.log/XML |
| 實際大字級高度裁切 | Native graphics、舊 56dp strip：didOverflowHeight=true，size 34×36，maxHeight=36 | font-native-red.log/XML |

每個回歸只修責任邊界：session 身份、strip node key、bounded height、service input gate。保留原 P0-R 測試 assertions；舊選字測試僅傳入正式必要的當前 CandidateRef。

## Font oracle 核對

修正高度後，Native graphics 的 semantics paragraph 使用 LazyRow 的 Infinity width 重建；measured size=35×50，而 paragraphWidth=2.1474836E9。`hasVisualOverflow` 包含 `size.width < paragraphWidth`，在此無界 paragraph 情況產生假陽性；高度足夠、未省略、完整 visibleEnd=1。獨立 reviewer 核對本機實際 Compose 1.7.0 AAR 位元碼及 XML（font-diagnostic-final.log），並確認不代表 glyph 真被裁切。

移除無必要的 minTextWidth workaround。最終測試仍要求：didOverflowHeight=false、沒有 ellipsis、完整 visibleEnd、左右 glyph bounds 落在 measured width（1px rounding tolerance）。原 56dp Native RED 仍由高度 assertion 捕捉。未降低文字可見契約。

## 全量回歸中的 fixture 校正

補 600dp 案首次編譯誤用 test double 欄位 `selects`，改為既有 `selectionCalls`；該 compile error 不列 RED。隨後全量 264 測 1 敗：`EditorCommitRecoveryTest` 的 unsupported-extracted-text 案在 `onStartInput` 後直接點軟鍵，缺少 `onStartInputView`。新 gate 正確拒絕尚未開始的 view input；保留所有原 assertions 與 getExtractedText=null，只補完整 callback。

官方 [InputMethodService](https://developer.android.com/reference/android/inputmethodservice/InputMethodService#onStartInputView(android.view.inputmethod.EditorInfo,%20boolean)) 定義 input view 開始時在 onStartInput 之後呼叫 onStartInputView；[AOSP doStartInput](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/inputmethodservice/InputMethodService.java) 在可見 soft input 的 restart 路徑也呼叫該 callback。新增 lifecycle 案驗證 start 與 view-start 中間的舊 key 被拒絕、view-start 後正常輸入。失敗 log/XML 保存在 `full-jvm-lifecycle-fixture-failure.log` 與 `EditorCommitRecoveryTest-lifecycle-fixture-failure.xml`；沒有調低 typing assertion。

## 聚焦 GREEN

命令：`:app:testDebugUnitTest --tests com.example.ime.CandidateBrowserStateTest --tests com.example.ime.CandidateBrowserUiTest --tests com.example.ime.CandidateBrowserLifecycleTest --tests com.example.ime.NativeCandidatePagingTest --tests com.example.ime.NativeTypingEditorIntegrationTest`，並指定 `KUYIN_NATIVE_HOST_DIR/DATA`。

`focused-corrected-green.log`：exit 0，5 類 36 測，failure/error/skip 全 0。state12、UI11、lifecycle3、真 native4、真 native/editor6。之後補 600dp/fontScale1.3 的 expand/select/return 案，納入全量驗證；全量最終數字見 handoff 與 verification-summary.json。

## S1-01～24 需求對照

| Cases | 直接測試／限制 |
|---|---|
| 01–04 | UI normal strip、expand/collapse、state preedit/candidates/query/commit 不變 |
| 05–08 | 真 JNI 不同頁、前後邊界、UI page controls 與第一頁返回 |
| 09–10 | immutable scoped refs；單欄位 generation/page/provider/identity 負案例；strip/grid pointer 舊 generation 拒絕 |
| 11 | UI selection 一次＋真 JNI/service/EditText second-page selection→continue |
| 12–13 | 確實 overflow 的 strip/grid，以 chip 起手拖曳，scroll offset 增加且 selection=0；正常 tap selection=1 |
| 14–16 | full string accessibility、48dp targets、空投影頁/空 composition、單項；native graphics 字級 2 |
| 17–19 | 真 service restart/hide、typed stale page、native history 相等及下一音節差分 |
| 20–23 | 原 P0 no-tone／我現在／English／Backspace/Space/Enter 測試，全量及 pinned native probe；沒有更改 decoder 策略 |
| 24 | 320/360/600dp、640×360 landscape、fontScale1/1.3/2、Button semantics/label/48dp bounds |

Compose/Robolectric PASS 不能推論 TalkBack/OEM/實際軟鍵觸感已驗收。裝置案例全部仍 NOT_TESTED。
