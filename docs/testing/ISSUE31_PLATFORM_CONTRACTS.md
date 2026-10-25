# Issue31 — Android 平台契約修正

基底 `ea3fdb24eec69697285451bf4b612b056f1bcbcd`（PR28），對應main `2a2181e023e0dee0ddff98681e374df10cd5fa8f`。本切片為獨立產品維護候選，不自動合併至main；未啟動Emulator／實機／Release。原PR28的失敗CI不因本機測試而改判。

## 根因與最小處置

|錯誤|處置與不變量|回歸依據|
|---|---|---|
|FeedbackHelper兩項MissingPermission|所有production caller均傳View；移除未使用direct-Vibrator fallback，要求View。保留原keyboard-tap、flags、settings、sound及exception分支；Manifest逐byte不變，不新增VIBRATE|原P3「View haptics need no VIBRATE」保持不變；新PackageManager及View on/off boundary tests|
|Service MissingSuperCall|呼叫super後仍return true，保留現有always-show政策；不改onShowInputRequested、window oracle或editor/session|原Lint分析器；hard-keyboard/no-keyboard callback characterization，非window測試|
|Night NewApi|min24的night style移除API27 item；night-v27保留完整等值style及該item，避免變體漏掉其他dark屬性|Lint＋獨立XML語義比對＋實際resource resolution tests|

Theme.AndroidKeyboard為既有named theme；本切片不啟用舊Activity、不改application的Theme.MyApplication，不改Dachen／candidate identity／orderedCommits／InputConnection ACK／JNI／native pins／版本／簽章。

## 平台來源與限制

[View haptics](https://developer.android.com/develop/ui/views/haptics/haptic-feedback)不需要VIBRATE；[Vibrator](https://developer.android.com/reference/android/os/Vibrator)直接呼叫則需要，這不構成新增權限的理由，因專案明定View-only。

[InputMethodService契約](https://developer.android.com/reference/android/inputmethodservice/InputMethodService#onEvaluateInputViewShown())要求呼叫super。獨立檢查AOSP固定[API24](https://raw.githubusercontent.com/aosp-mirror/platform_frameworks_base/android-7.0.0_r1/core/java/android/inputmethodservice/InputMethodService.java)、[API33](https://raw.githubusercontent.com/aosp-mirror/platform_frameworks_base/android-13.0.0_r1/core/java/android/inputmethodservice/InputMethodService.java)、[API36](https://raw.githubusercontent.com/aosp-mirror/platform_frameworks_base/android-16.0.0_r1/core/java/android/inputmethodservice/InputMethodService.java)：父方法可讀取／快取secure setting，其本體不show/hide/reset decoder；不能宣稱沒有任何副作用。不可直接return super，否則hardware keyboard可把現行true改成false。這不是所有OEM或未來版本的證明。

[API26→27差異](https://developer.android.com/sdk/api_diff/27/changes/android.R.attr)、[資源選擇](https://developer.android.com/guide/topics/resources/providing-resources)支持資源分版；同名style不可只塞單一item假設其他變體自動合併。

## RED／GREEN 與證據層

- 原始Lint直接重現4 errors。初次嘗試加入VIBRATE雖使Lint通過，卻被獨立Reviewer以原P3 contract拒絕；Manifest已還原，拒絕方案與全部失敗收據保存在本機，不作最終成果。
- 合法View-only baseline：12個既有行為characterization通過，Lint仍有2個direct-fallback MissingPermission（exit1）。移除未使用fallback後：同12例PASS，AppLint0errors/53warnings（exit0）。Service及night的characterization原本就PASS，不冒稱新behavioral RED。
- 最早兩次測試夾具的qualifier順序／hardKeyboardHidden初始化錯誤已獨立分類並保留，不算產品RED。
- 測試直接執行SDK33／36；API24／26／27未執行JVM／裝置。View recorder只證平台call boundary；callback true不證Android window/binding。沒有硬體注入、假InputConnection或原oracle變更。

完整本機gates已執行：127 Python、12 contracts、App267 PASS/10 SKIP、Host57 PASS、兩Debug與兩instrumentation編譯、雙模組Lint exit0（App0errors/53warnings、Host0/6），未加suppression或baseline。另有有界mutation：改成return super時硬鍵盤兩例FAIL、無鍵盤兩例PASS；原碼SHA還原後四例GREEN。最初full結果與restore後七檔source SHA相同。exact final source/CI由Issue31、PR與本機handoff對應；本機結果不能冒充GitHub CI。Runtime仍H0 INCONCLUSIVE、H1/H2 NOT_EXECUTED、Host歷史15PASS/9FAIL/8SKIP、physical NOT_TESTED；RC/Stable NO_GO。
