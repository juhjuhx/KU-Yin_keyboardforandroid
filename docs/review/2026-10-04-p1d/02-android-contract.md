# Android Enter 契約

查證日期2026-10-04；compile/target SDK36（minor1）、min24、AGP9.1.1、Kotlin2.2.10、Robolectric4.16.1、Compose BOM2024.09.00。

## 官方解讀

- [EditorInfo](https://developer.android.com/reference/android/view/inputmethod/EditorInfo)：IME_MASK_ACTION=0xff，GO=2/SEARCH=3/SEND=4/NEXT=5/DONE=6/PREVIOUS=7；NONE=1、UNSPECIFIED=0。MASK僅擷取動作metadata，不能代表inline Enter允許使用它。NO_ENTER_ACTION=0x40000000禁止以action取代inline Enter（metadata action仍可能存在）。本產品遵照使用者決策：NONE/UNSPECIFIED都換行，PREVIOUS標籤上一步。
- [actionLabel/actionId](https://developer.android.com/reference/android/view/inputmethod/EditorInfo#actionId)：提供custom label時，以相對應actionId回送，不能顯示custom label卻送maskedstandard code。NO_ENTER_ACTION在inline key優先。
- [InputConnection.performEditorAction](https://developer.android.com/reference/android/view/inputmethod/InputConnection#performEditorAction(int))：參數為standard action或custom actionId。boolean false表示connection無效；沒有指示改寫成換行。保留一次tap一個logical outcome，忽略boolean而不補送newline。

以上為官方契約的繁體中文轉述，非整段引用。explicit flag是本切片主契約；不新增multiline heuristic、raw key-event recovery或泛用IC重試。newline沿用commitText(\n,1)，不使用sendKeyChar（helper會再解讀action）。

## 近似成熟開源來源

[AOSP InputMethodService](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/inputmethodservice/InputMethodService.java) sendDefaultEditorAction(fromEnterKey)以NO_ENTER_ACTION擋inline action；文件明確custom須直接performEditorAction(actionId)，helper不處理custom。因此此helper不能完整接入我們的normalizedcustom政策。

[AOSP LatinIME Enter](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/b41bea65502ce7339665859d3c2c81b4a29194e4/java/src/com/android/inputmethod/latin/LatinIME.java)也將customlabel與actionId配對（任何id值），而非masked值。參考其邊界／契約，不拷貝程式，無新增dependency。其UNSPECIFIED預設action策略與本產品指定newline不同，遵守本產品需求。InputTypeUtils頁面抓取失敗，不宣稱讀過；不靠該頁推導行為。

## 已界定的邊緣案

custom eligibility：actionLabel != null且無NO_ENTER_ACTION，不trim、不以actionId>0作過濾；id0也是合法指定id。label轉String在service邊界snapshot；空字串仍為已供應customlabel（尊重metadata，不猜測另一動作）。custom label恰好換行時，isActionPresentation=true，UI顯示custom文字。

未知maskedaction：既有EditorPolicy normalizeAction對unknown回UNSPECIFIED；新inlinepolicy同樣fallbacknewline，不送未識別action。缺失EditorInfo則UNSPECIFIED/labelnull/id0→newline。

TYPE_CLASS_TEXT|TYPE_TEXT_FLAG_MULTI_LINE加SEND|NO_ENTER_ACTION→newline；只multiline而無explicitNO_ENTER_ACTION仍按action，不加額外推測。
