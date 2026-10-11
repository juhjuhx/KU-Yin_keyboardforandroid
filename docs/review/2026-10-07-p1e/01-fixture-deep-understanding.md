# P1-E 深讀與證據邊界

既有 ImeHostActivity 在 KU-Yin debug APK 內，只有 multiline EditText＋DONE，沒有 final metadata／action oracle。
既有 4 類 5 測主要為 direct native 與 bind／recreate；API24–29 的 isAcceptingText 不能證明視窗可見，連續句斷言也沒有證明兩音節完整共存或總 commit 次數。保留原測試，不改寫成新的主張。

產品 onStartInput 正規化 EditorPolicy／EditorEnterPolicy；UI Enter 先 session.dispatch，再處理 orderedCommits，只有未 consumed 才進 service.handleEditorAction。
新 host 必須是獨立 application／process，不 mock 這個 Android 邊界、不呼叫產品 service／native。

## EditorInfo 的兩份 snapshot

AOSP TextView 可將 UNSPECIFIED 改成 NEXT／DONE、加入 NAVIGATE flags，並對 multiline 加 NO_ENTER_ACTION。因此普通 TextView 未受控 metadata 不足以涵蓋 raw action 矩陣。
RecordingEditText 先呼叫 super、保存平台 attributes，再真正把 outAttrs 的 inputType／imeOptions／actionLabel／actionId 設為宣告的 primitive config，最後保存 final controlled snapshot。
最後 object 才是交給 IMF 的 metadata；不能只改 diagnostics。兩份 snapshot 分開顯示平台正規化差異，其他 selection／surrounding metadata 與真 EditText 文字編輯仍保留。

TYPE_NULL 保留 super 的真 null，不製造 InputConnection。Host observed INPUT_CONNECTION_PRESENT=false，不代表 IME service.currentInputConnection 一定 null，framework 可能有 raw／dummy 路徑。
若實際裝置沒有當次連線回呼或無法進入真軟鍵流程，子範圍標 INCONCLUSIVE，不拿舊鍵盤或 fake IC 補 PASS。

## Action 與文字

Wrapper 記 ID／count／configured return true 或 false；host 自己消費 action，不 delegate performEditorAction，避免 TextView 再導航、hide 或製造 newline。
commitText、composing、selection、delete 等仍 delegate 真 IC；newline 從真 Editable 當前 LF 計算。commit 呼叫紀錄是額外觀察，不自行拼出文字。

AOSP RemoteInputConnectionImpl 的 action 為 oneway void，忽略 editor boolean。D17 可觀察 host 收到一次 SEND、回 false 與 newline=0；不能宣稱 IME 收到同步 false。
直接 false-return 不 fallback 的語義已有 P1-D JVM 證據；裝置報告須限定 scope，不偽造 IPC 回傳。

## 可見 composition

KU-Yin 內部 preedit 沒有 setComposingText 到 host，editor composing span 或 commit count 不能證明按 Enter 前存在 active composition。
CandidateBar 只在 preedit 非空時出現「清除」Icon。正證據必須是可見清除 marker＋可從鍵面／候選 clickable ancestor 分辨的 preedit Text。
不能因畫面本來顯示 ㄅ／ㄕ／ㄖ 就算 preedit；selector 不可靠則 BLOCKED_SELECTOR／INCONCLUSIVE，不添加隨意的產品 semantics。

## 程序、狀態與未完成範圍

Host instrumentation 在外部 editor 程序；ActivityScenario 只讀自身 oracle，UIAutomator 跨視窗點真正 KU-Yin 軟鍵。
原 default／enabled IME 由固定 serial 的操作手冊／執行器保存、明示 enable／select、最後還原；tests 只斷言 KU-Yin 已 selected，不偷切其他輸入法。沒有 auto-uninstall／pm clear。
Host 沒有 Compose、native、network 或 product dependency。

A03 由實際裝置旋轉人工觀察；host configChanges 保留 recorder，暴露 host orientation／display rotation 與文字／commit counter。
recreate 不等同 rotation，composition 保留／清空只是觀察，沒有新增保存政策。Native learning observer／安全 startup-fault 裝置 fixture 保持 OPEN；debug-only observer 只另作未來提案，P1-E 不改產品 main。
