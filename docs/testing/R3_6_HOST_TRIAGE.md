# R3.6 Host 逐案證據處置

來源main `2a2181e023e0dee0ddff98681e374df10cd5fa8f`；[CI39882220869](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39882220869)。這是舊CI原始XML及case envelopes的新核對，不是新Runtime。15PASS／9FAIL／8SKIP維持原樣。PR27 head `3d6bab7a6baf9f9fd97e379f6d0cedc48f799e59` 尚未Runtime驗證，Draft/HOLD。

## 九項FAIL

|Case|方法|最後可證明preedit|失敗邊界|Runtime修復|
|---|---|---|---|---|
|P0-ZH05|`zh05ContinuousTonedPhraseCommitsExactlyOnce`|ㄨ|tapPhonetic:197 → strictPreedit:230 → productObjects:495 → visible:497|NOT_VERIFIED|
|P0-ZH02|`zh02ThirdToneWoIsSelectableAndCommitted`|ㄨ|tapPhonetic:197 → strictPreedit:230 → productObjects:495 → visible:497|NOT_VERIFIED|
|P0-ZH04|`zh04ContinuousNoTonePhraseCommitsExactlyOnce`|ㄨ|tapPhonetic:197 → strictPreedit:230 → productObjects:495 → visible:497|NOT_VERIFIED|
|P0-ZH01|`zh01NoToneWoIsSelectableAndCommitted`|ㄨ|tapPhonetic:197 → strictPreedit:230 → productObjects:495 → visible:497|NOT_VERIFIED|
|P0-EN02|`en02EnglishDictionaryCompletionCommitsActualEditorWord`|ENGLISH模式；精確最後文字UNKNOWN|body selector INCONCLUSIVE；cleanup Stale（兩因保留）|NOT_VERIFIED|
|B02|`b02RealCandidateSelectionThenContinuedComposition`|ㄋ|tapPhonetic:197 → strictPreedit:230 → productObjects:495 → visible:497|NOT_VERIFIED|
|B03|`b03RealContinuousSentenceCandidateAndSingleFinalCommit`|ㄋ|tapPhonetic:197 → strictPreedit:230 → productObjects:495 → visible:497|NOT_VERIFIED|
|D13|`d13CompositionCompletesBeforeSend`|ㄋ|tapPhonetic:197 → strictPreedit:230 → productObjects:495 → visible:497|NOT_VERIFIED|
|D14|`d14CompositionCompletesBeforeNewline`|ㄋ|tapPhonetic:197 → strictPreedit:230 → productObjects:495 → visible:497|NOT_VERIFIED|

共通8案：UiDevice.findObjects(By.pkg(...))取得live nodes後逐個visibleBounds；在同一次query/遍歷就可能stale，不是已證明跨鍵長期快取。poll內Stale被吞、timeout後再次strictPreedit漏出Stale。logcat有refresh failures，缺window/node ID及Accessibility event時間，不能歸因Compose重建。最後精確JUnit assertion invocation沒有逐條trace，維持UNKNOWN；上表是直接已記錄的strict-preedit checkpoint。

EN02：body awaitPreedit缺唯一non-clickable leaf，cleanup又在clearComposition→visible:497 Stale；CaseFailurePolicy將後者升為主error、前者suppressed。沒有到達hello幾何；不能把候選區修正稱為此案修復。

## 八項SKIP（保留）

|Case|直接缺口／補測條件|
|---|---|
|P0-ZH06|visible soft target 我 is absent/ambiguous|
|B01|ADDITIONAL_WIDTH_AND_ORIENTATION_OBSERVATIONS_REQUIRED|
|D17|HOST_FALSE_NOT_PROPAGATED_BY_ANDROID_IPC|
|C05|NATIVE_LEARNING_OBSERVER_UNAVAILABLE|
|C01|NATIVE_LEARNING_OBSERVER_UNAVAILABLE|
|C03|NATIVE_LEARNING_OBSERVER_UNAVAILABLE|
|C02|NATIVE_LEARNING_OBSERVER_UNAVAILABLE|
|C04|NATIVE_LEARNING_OBSERVER_UNAVAILABLE|

ZH06已到ㄨㄛ、editor composing為ㄨㄛ而commit0；候選「我」absent/ambiguous。B01已41hitboxes與ㄕ/ㄖ通過，第二寬度/方向缺。D17已host false/action1/id4，但不證IPC同步false回傳。C01/02/03/C05缺native learning observer；C04/C05另保留TYPE_NULL當次IME session/fallback InputConnection邊界。不得改SKIP為PASS。

## PR27與最小後續修正

PR27三點必要Host切片更正candidate須位於header下方、第一鍵列上方；main UI KuYinKeyboardUi.kt:459–485確實是header後接CandidateStrip。這主要對應ZH06 selector缺口。8個phonetic FAIL與EN02尚未進candidate selector，不受此幾何patch修復。不要將PR27落後main的圖片/README兩點diff逆向套回main。

下一個安全repair要對真tapPhonetic/strictPreedit→查詢邊界建立deterministic node generation fixture：第二glyph後舊node讀bounds Stale、新query回新generation；驗證有界fresh query取得改變的preedit，持續Stale仍明確失敗並保留原因。EN02另測缺leaf及cleanup兩因；ZH06用實際舊predicate RED、新predicate唯一命中候選GREEN。不要先把例外換成INCONCLUSIVE，亦不只測純geometry鏡像。

本輪health-only實驗不安裝任何APK，不能同輪重跑這17案。因此這裡只有triage與repair contract，Host RED/GREEN及Runtime修復均NOT_EXECUTED。未新增無法真實執行的mock PASS，不聲稱九案已修復。

## 獨立審查

Reviewer `/root/r36_host_evidence` 直接核對原XML、test-results envelopes、logcat與main/PR27 source，報告上述證據與限制；未執行Gradle或Android。原exactly-once、strict preedit、candidate/editor assertions全部保留。本分支只有文件，不改產品或測試行為。
