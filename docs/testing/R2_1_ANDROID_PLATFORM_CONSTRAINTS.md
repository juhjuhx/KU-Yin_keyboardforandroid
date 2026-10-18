# R2.1 Android 平台限制

基線R2d6；平台原碼固定`android-13.0.0_r1`，不是由較新main推回API33。來源與摘錄hash保存external `Android13官方來源/02-官方來源成功收據.json`。原要求與建議分開，尚未變更任何case契約。

## B01 orientation／width

使用[Activity.setRequestedOrientation](https://developer.android.com/reference/android/app/Activity#setRequestedOrientation(int))只請求Host Activity方向；平台可能忽略請求，不能setter成功當轉向成功。測試讀實際resources.configuration/display rotation，並讀[AccessibilityWindowInfo TYPE_INPUT_METHOD bounds](https://developer.android.com/reference/android/view/accessibility/AccessibilityWindowInfo#getBoundsInScreen(android.graphics.Rect))，只篩KU-Yin package，不記其他app內容。Host已處理orientation|screenSize，無需改Host main。

portrait→landscape→portrait僅在三個actual frames/兩個實際IME width及原嚴格soft-key/editor assertions全執行後才閉合本裝置B01子契約；emulator方向變化不是實體手機旋轉，多尺寸/OEM相容性另驗。讀取前後epoch穩定降低混版風險，不聲稱accessibility多次IPC是atomic snapshot。

## C04 TYPE_NULL

[TYPE_NULL官方定義](https://developer.android.com/reference/android/text/InputType#TYPE_NULL)不提供一般rich input connection的保證；raw key路徑也非所有IME均支持。API33 [TextView](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/core/java/android/widget/TextView.java#L8975)的onCheckIsTextEditor排除TYPE_NULL，normal onCreateInputConnection可真null。Host保留super null，不能補假BaseInputConnection、換inputType或強造焦點。

但Host null不等於IME currentInputConnection必null：[IMM](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/core/java/android/view/inputmethod/InputMethodManager.java#L1329)另有fallback BaseInputConnection；[InputMethodService](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/core/java/android/inputmethodservice/InputMethodService.java#L1794)可回started或binding connection。這只是可能平台路徑，不證本輪實際走到哪個。

最小後續可採原有focus/show呼叫的passive checkpoint：focused/attached/token/Insets／原show結果、EditorInfo epoch；不得增加requestFocus/show、用isActive/checkFocus改變時序或延timeout。必要service生命周期診斷也需另設計批准。當前null callback＋空product tree不能證temporary mode/native flag，仍INCONCLUSIVE；**未證平台一律禁止顯示IME**。

## D17 callback false與IPC

API33官方鏈：[IInputContext.aidl](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/core/java/com/android/internal/view/IInputContext.aidl#L35)為oneway，performEditorAction為void；[RemoteInputConnection](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/core/java/android/inputmethodservice/RemoteInputConnection.java#L322)委給[Invoker](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/core/java/com/android/internal/inputmethod/IInputContextInvoker.java#L322)，其boolean只反映Binder invocation是否RemoteException；[RemoteInputConnectionImpl](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/core/java/com/android/internal/inputmethod/RemoteInputConnectionImpl.java#L644)丟棄host callback boolean。因此該跨程序協定不提供host false同步返回，不可造假的observer。不得推廣為所有Android版本或任意同程序IC皆同語義。

原P1-alpha矩陣D17只寫host false／SEND恰1／無額外newline；較強同步false研究要求與它有來源差異。**提案**：對可觀察原條件保留action4/count+1、callback false、text/commit/newline delta0；同步false列PLATFORM_UNOBSERVABLE研究限制，不自動當產品失敗。須使用者記錄批准後才能變更當前case disposition；舊SKIP不可回填PASS，本輪尚未移除原skip或改test。

## 結論界線

C04為當次precondition/observability gap而非已證產品缺陷；D17同步返回受API33協定限制。此文件不縮減驗收、不新增production/JNI seam、不代表physical／release通過。
