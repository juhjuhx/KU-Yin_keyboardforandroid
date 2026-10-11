# P1-D Enter 深讀（實作前）

BASE_SHA: 870fc03cbfe8e5107b8ff8dc13478ac7754eec9f。以本機 consolidated P1-A/B/C 為準。

## 真正產品資料流

Manifest 只註冊 KuYinInputMethodService；舊 ChewingInputMethodService 是未註冊 View 參考路徑。MainActivity 的 Compose preview 固定換行、直接提交\n，不是 InputMethodService。

EditorInfo → onStartInput → EditorPolicy.from(inputType,imeOptions) → learning/reset/effective mode（P1-C已修） → onStartInputView → currentActionLabel → onCreateInputView 傳 String 至 KuYinKeyboardUi。

ZH Enter：session.dispatch(ImeCommand.Enter) → DispatchResult → orderedCommits（commitText，再additionalCommits）→逐筆 InputConnection.commitText(text,1) → 僅 !consumed 時 handleEditorAction。

ENGLISH/SYMBOLS Enter 直接委派 editor callback；從 ZHUYIN 切換時既有 switchPresentationMode 先 Complete＋orderedCommits 再 setMode，因此正常切換已清掉composition。EMOJI沒有 Enter key。此次不重設這些 session/mode 語義。

ChewingEngineSession.completeComposition：preedit 空→unconsumed不碰native；非空→engine.commitUpdate→project。DictionarySession 非空先候選或原注音 completion→consumed=true；空→false。EnterCommandContractTest 五案與ModeSwitchContractTest三案已讀。

## 源碼證實的缺陷

- Service 原155–162：onStartInputView只mask options，GO/SEARCH/SEND/NEXT/DONE標籤；未讀NO_ENTER_ACTION。
- Service 原219–228：handleEditorAction再次mask當前EditorInfo；NONE/UNSPEC才commitText(\n,1)，其他masked值performEditorAction。未讀NO_ENTER_ACTION。
- custom actionLabel/actionId兩處都忽略；label/custom dispatch不受支援。
- PREVIOUS會performEditorAction(7)，但畫面fallback顯示換行。
- label在callback時決策、dispatch在tap時重新解讀metadata，可能漂移。performEditorAction boolean目前忽略，無newline fallback；此行為保留。
- 三種layout原本以actionLabel==換行挑icon；custom label若剛好換行會被圖示取代。必須傳獨立normalized presentation布林，避免字串碰撞。

## 搜尋與回歸面

已全repo搜尋currentActionLabel/handleEditorAction/performEditorAction/IME_MASK_ACTION/NO_ENTER_ACTION/IME_ACTION/actionLabel/actionId/ImeCommand.Enter/onPerformEditorAction/commitText newline/sendKeyEvent/KEYCODE_ENTER。

EditorPolicy.action仍是既有domain metadata給ImeSessionController／未註冊View服務，非Compose Enter presentation或dispatch。不改P1-C composition/learning判斷。ProductionEditorPolicyTest、ProductionSeamIntegrationTest、NonTextEditorServiceTest及所有EditorPolicy測試已讀。

## 邊界

Service保留Android lifecycle/InputConnection；新pure EditorEnterPolicy從 primitive options/String?/id產出一次決策。UI只收label與isActionPresentation；不收EditorInfo、不讀flag/mask/id。decoder/session/JNI不改。

## 完成後接線

onStartInput / onStartInputView 只透過同一updateEnterPolicy抽取 primitive options、String label、id；currentEnterPolicy同時提供UI label與isActionPresentation。handleEditorAction只when normalized behavior，不再讀currentInputEditorInfo或mask。UI維持session Enter→orderedCommits→僅!consumed才editor callback；三布局均傳入presentation bool。MainActivity preview的預設字串判斷僅用於既有preview/直接layout API，不重解讀EditorInfo。

final-green/protected-source-check.json確認A/C policy/ownership、Dachen rows/constants、session decoder、原B tests、native bootstrap/build pins全部維持base bytes；程式提交只含review-manifest列8檔。
