# P1-E 受控驗收工具設計

## 模組邊界

新增 `:acceptance-host`，applicationId／namespace 為 `io.github.juhjuhx.kuyin.acceptancehost`。
採 platform Activity／Views／EditText、AGP 9.1.1 built-in Kotlin、compileSdk 36.1／minSdk 24；不依賴 KU-Yin 產品、Compose、JNI、libchewing 或網路套件。
Manifest 為 testOnly=true、allowBackup=false；AcceptanceEditorActivity exported=true，方便明確 component 啟動。這是全體 test helper APK，沒有改產品 exported 設定。
UIAutomator 固定 2.3.0，只在 androidTest 使用；其餘重用現有 AndroidJUnit4／test core／runner／JUnit。

## Primitive 協定與純邏輯

| Intent key | 型別 | 預設 |
| --- | --- | --- |
| caseId | String | DEFAULT |
| inputType | Int | 1 |
| imeOptions | Int | 0 |
| customActionLabel | String 或 null | null |
| customActionId | Int | 0 |
| performEditorActionReturnValue | Boolean | true |
| initialText | String | 空字串 |
| minLines | Int，必須 >0 | 1 |

錯誤型別明確拒絕；TYPE_NULL=0、空 label、custom ID=0 都原樣保存。
CaseConfig／EditorInfoSnapshot／EditorSnapshot／EditorRecorder 為純 Kotlin；IntentCaseConfig 只轉 primitive Map。
Recorder 保存不可變 snapshot、同步更新 revision；observer 在鎖外通知，Activity 派回主執行緒。紀錄 actual text、LF 數、action ID／次數／實際配置回傳值，以及 commit 呼叫觀察；commit 紀錄不自行拼文字。

## 真實 editor 邊界

RecordingEditText 呼叫 super.onCreateInputConnection，先複製 platform metadata；再真正設定 outAttrs 的 inputType／imeOptions／actionLabel／actionId，最後複製 final controlled metadata。
平台可能改寫 action／flags，兩份 snapshot 分別呈現，不將受控宣告冒稱為未受控 TextView metadata。
原 IC 為 null 就返回 null；非 null 才包 InputConnectionWrapper。action 由 host 獨自消費、紀錄並回配置 boolean；文字、composition、selection、deletion 仍 delegate 真實 EditText。
D17 可觀察 host 回 false 與 action／newline 效果，但不能宣稱 Android oneway IPC 把 false 傳回 IME。

## 穩定 diagnostics

Resource IDs：editor、case_id、actual_input_type、actual_ime_options、actual_action_label、actual_action_id、input_connection_present、editor_action_count、last_editor_action_id、last_action_return_value、newline_count、current_text、last_commit_text、commit_count、snapshot_revision、platform_input_type、platform_ime_options、platform_action_label、platform_action_id、ime_visibility、host_orientation、display_rotation。

各 value 使用 primitive 字串，null 為 `<null>`，與空字串分開。ACTUAL 是真正 final outAttrs；不顯示預期 policy 作為假診斷。測試只用固定 synthetic 字串，host 不保存到磁碟或寫入文字 log。
Host instrumentation 可在 ActivityScenario.onActivity 讀同一 immutable oracle snapshot；操作 IME 必須使用限定產品 package 的可見文字／contentDescription selector。
API≥30 記錄 WindowInsets IME visibility；24–29 顯示 UNAVAILABLE，不能以 isAcceptingText 代證視窗可見。

## 矩陣與限制

測試矩陣在 src/sharedTest/java，只供 test／androidTest；不把 KU-Yin Enter 決策放進 host main。
D01–D18 檢查真正 metadata、可見 Enter、精確 action count／ID 與 newline 差量。D13／D14 必須先證明 preedit chip，再檢查第一次 Enter 只完成 composition，第二次才 action／newline；不能把鍵面注音當作 preedit。
D16 分辨自訂文字「換行」與 normal Enter icon；D17 對 IPC 因果範圍保持 INCONCLUSIVE。
C01–C05 準備非文字 editor、暫時英文、composition 禁用與 mode restoration；native learning physical observer 維持 OPEN。
B01 使用真軟鍵 ㄕ／ㄖ；B02／B03 準備真候選互動，不推定 native context 身分或完整句子正確性。
A01／A02 保留舊層；A03 由實際旋轉人工觀察，composition 保留／清空不是已凍結契約；A04 不注入產品 fault。

結果只有 PASS／FAIL／INCONCLUSIVE／NOT_TESTED。缺少或不唯一 selector 標 BLOCKED_SELECTOR、保留證據並 assumption skip；skip 不是 PASS。KU-Yin 未被選取則 setup 明確失敗，instrumentation 不自行 enable／set。
採 bounded condition polling；不以 hardware Enter、adb input keyevent、direct native 或座標補出軟鍵證據。

## 未來 observer 邊界

native learning 若要取得真實 live service 值，優先研究 debug source-set 專用 observer；須先證明 release tree／DEX 沒有 observer、不暴露文字或 learning 操作，再另行設計審查。
P1-E 不修改 app/src/main 或自行增加 observer；不導入 Macrobenchmark，不開始 M4。
