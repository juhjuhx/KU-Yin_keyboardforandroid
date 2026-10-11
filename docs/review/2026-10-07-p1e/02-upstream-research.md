# P1-E 上游研究與 source/gap 初審

研究日期：2026-10-07。受審 base：`5c6bcad34c4048e40ce4e871e70e176c10195bcf`。本文件只做源碼、官方文件與授權研究；沒有執行 Gradle、ADB、OCR 或 Android runtime，沒有修改產品碼、下載外部大型 code 到工作區、push 或開始 M4。研究採 `addyosmani-source-driven-development`／`code-review-excellence`。公開上游以只讀 web 與 curl 取得；curl 的內容留在記憶體中。

## 初審裁定與 material 陷阱

**接受獨立 `:acceptance-host` Android Views oracle 的方向。既有 debug host／4 類 5 測不足以完成 P1-C/D；fixture 可以在無裝置時完成建置與自有契約驗證，產品 runtime 結果仍必須維持 `NOT_TESTED`。** 此裁定是 bounded 設計初審，不是假稱尚未完成的 fixture 已通過 code review。

最重要的六項契約：

1. `super.onCreateInputConnection(outAttrs)` 後先複製 **platform snapshot**；再真正覆寫 `outAttrs.inputType / imeOptions / actionLabel / actionId` 為宣告 case 的 primitive config，最後才複製 **final controlled snapshot**。不得只改顯示的 diagnostic；不得將 controlled metadata 宣稱為完全未受控的 TextView metadata。
2. TextView 會將 UNSPECIFIED 改為 NEXT／DONE、加入 NAVIGATE flags，並對 multiline 加 `NO_ENTER_ACTION`。要測 D02 原始 action=0 或自訂 action，必須在上述最後一個 boundary 固定實際 metadata；D15 則必須明示 MULTI_LINE＋NO_ENTER_ACTION。
3. `TYPE_NULL` 必須保留 `super` 產生的真 `null` IC；不能以 wrapper(null)、BaseInputConnection 或 fabricated connection 代替。單獨記錄 editor boundary 的 `connectionPresent=false`，不推定 IME service 端不存在系統 fallback／dummy connection。
4. 非 null IC 才包 `InputConnectionWrapper(original, false)`。`performEditorAction(id)` 只記收到的 ID、count、配置的 return boolean，再回該 boolean；**不 delegate action**。一般文字的 `commitText`、selection、delete 等繼續 delegate，這才是 host 消費 action 而文字正常進 EditText 的 oracle。
5. 在真跨程序 Android 路徑，host return false 未必回送 IME：AOSP 的 oneway Binder action handler 忽略 editor boolean。D17 runtime 可證 host 收到一次 SEND、host 回 false 與 newline delta=0；不能據此宣稱 IME 收到同步 false。同步 false 不 fallback 的產品語義由既有 JVM service contract 另證。
6. 真 IME preedit 證據必須從產品的 preedit chip／可辨識 hierarchy 取得。鍵面本來就顯示 ㄅ／ㄕ／ㄖ，單一 `By.text("ㄅ")` 或 `By.text("ㄕ")` 存在不能證明 preedit。host 文字、native direct test、IC composing span、`isAcceptingText` 都不能替代這份證據。

## 現有 source/gap 深讀

以下是 base 源碼的實際 coverage，不是 runtime 失敗判定。既有測試保留原義。

| 來源 | 已有能力 | 尚缺／不能推出的結論 |
| --- | --- | --- |
| `app/src/debug/.../ImeHostActivity.kt` | 單一 multiline EditText；DONE；minLines=3；focus/show request | 無 config matrix、final EditorInfo snapshot、custom action label/id、action count、false-return fixture、connection presence。setImeOptions 不等於最終傳給 framework 的 EditorInfo。 |
| `ImeRuntimeSmokeTest.nativeEngineSurfacesVisibleDachenPhoneticPreedit` | 直接 AndroidChewingEngine：dict/init、learning=false、key1 產生 ㄅ | 不經 production service／真正 soft-key hitbox／host IC；不能證 C policy 在實際 onStartInput 到 native 的路徑。 |
| `ImeRuntimeSmokeTest.imeBindsAndSurvivesEditorRecreate` | enable/select prerequisite；ActivityScenario host 顯示、recreate；最後 dumpsys 引用 service | 非 physical rotation；無 action ID/count。API≥30 用 IME WindowInsets；API24–29 僅 isAcceptingText，後者不等於視窗可見。 |
| `NativeCase4Test.selectionCommitsSegmentAndNextKeyContinuesSameContext` | direct native s/u/3、select、key1；同 context、不 reset | 實際 assertion 是 selection 保留 preedit；方法名稱不能證立即 commit。未測 Compose candidate tap 或 editor side effect。 |
| `ContinuousSentenceTest.nihaoCoexistsConvertsCorrectsAndCommitsOnce` | direct native preedit 非空、有 CJK、有 candidates、select 後非空、final committedText 非空 | 未計算所有 commit 次數；未斷言兩音節完整保留、final preedit 清空或 editor 恰好一次 commit。 |
| `ExampleInstrumentedTest.useAppContext` | package identity | 無 P1 行為 coverage。 |
| `scripts/runtime_smoke.sh` | bootstrap/installDebug；未 pin serial 的 adb enable/set；connectedDebugAndroidTest；bounded polling | 會重建／裝 app、未保存／還原原 IME、多裝置 serial 不固定。不是本輪 frozen-product acceptance 入口。 |

Alpha [01-實機驗收深讀](../2026-10-07-p1-alpha/01-實機驗收深讀.md) 與 [02-驗收矩陣](../2026-10-07-p1-alpha/02-驗收矩陣.md) 的上述 coverage 判讀與源碼相符。兩份文件所稱「debug-only fixture」由本次使用者明示的獨立 `:acceptance-host` 取代；原 debug host 不必擴建。30 個 case 不可由 direct native、JVM 或 APK build 代填 physical PASS。

產品 `KuYinKeyboardUi.kt` 的 `CandidateBar` 在 preedit 非空時顯示 Text(preedit) 與 description「清除」；`KuYinInputMethodService.kt` 沒有向 editor `setComposingText`，commit/newline 才走 `commitText`。因此 host composing span 為空仍可能有產品內部 composition。C01～C05 的 learning disabled 也不能只由英文鍵盤或 userdict hash 推定；獨立 host 未增加產品 observer，這一 gate 維持外部驗收缺口。

## 官方 Android 契約與受控 metadata rationale

官方 [EditorInfo.NO_ENTER_ACTION](https://developer.android.com/reference/android/view/inputmethod/EditorInfo#IME_FLAG_NO_ENTER_ACTION) 明定此 flag 禁止 action 取代 inline Enter，並說明 TextView 會為 multiline 自動設它。[actionId](https://developer.android.com/reference/android/view/inputmethod/EditorInfo#actionId) 是有 actionLabel 時由 `performEditorAction` 回送的 command ID。[TextView.setImeActionLabel](https://developer.android.com/reference/android/widget/TextView#setImeActionLabel(java.lang.CharSequence,%20int)) 將 label/id 放入連線時的 EditorInfo。

以 Android 16 `frameworks/base` commit `99b01a65cc4c104933788b3143285ab6bae65827` 為 implementation evidence：[TextView.java](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/android/widget/TextView.java#10078) 的 `onCreateInputConnection` 先填 inputType、imeOptions、label/id，之後才加 NAVIGATE flags、把 UNSPECIFIED 改 NEXT／DONE、加 NO_ENTER_ACTION，再建立 EditableInputConnection 與 selection/surrounding metadata。`onCheckIsTextEditor` 對 TYPE_NULL 回 false，因此 `super` 此路徑回 null。

本次控制四欄位是有意建立宣告 editor cases，讓 D01/D02 與 custom cases deterministic，並保留兩個 snapshot 供檢視 TextView 原始正規化差異。保留 `super` 提供的其他 selection／hint／surrounding 等 metadata；四項宣告配置覆寫後不得再有改寫。這不是驗證任意第三方 app 的未受控 TextView metadata；是驗證 KU-Yin 接收此 editor configuration 時的既定行為。應另以 oracle 契約斷言「回呼中的 outAttrs 四欄位 == final snapshot == case config」。

TYPE_NULL 要有獨立 null-return assertion。[InputConnectionWrapper.setTarget](https://developer.android.com/reference/android/view/inputmethod/InputConnectionWrapper#setTarget(android.view.inputmethod.InputConnection)) 特別指出非 null wrapper 的 null target 不能模擬 null connection；多數 delegate 方法亦會 NPE。若 TYPE_NULL 在裝置不能達到所需 shown/bound 前提，記錄該 prerequisite 為 INCONCLUSIVE／NOT_TESTED，不能改出假 IC 再判 PASS。

[EditableInputConnection.java](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/com/android/internal/inputmethod/EditableInputConnection.java#173) 的 action 會呼叫 TextView.onEditorAction 並回 true；[TextView.onEditorAction](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/android/widget/TextView.java#8314) 可觸發 listener、NEXT／PREVIOUS focus、DONE hide、或合成 Enter。故 recorder 若再 delegate action，會變成另一個 side-effect producer；只用 OnEditorActionListener 亦不能控制 IC 的 return false。fixture 必須自己消費 action，再回配置 boolean。

[IRemoteInputConnection.aidl](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/com/android/internal/inputmethod/IRemoteInputConnection.aidl#39) 是 oneway；[RemoteInputConnectionImpl.performEditorAction](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/android/view/inputmethod/RemoteInputConnectionImpl.java#722) 呼叫 editor 的 boolean 方法但不傳回結果。這是 D17 證據界線，不取消 host 的 false-return 配置與 local contract。

[InputMethodManager.restartInput](https://developer.android.com/reference/android/view/inputmethod/InputMethodManager#restartInput(android.view.View)) 用於已連接 editor 外部變動後重新建立 context；配置變更必須完成主執行緒更新，重新 focus／restart，再等新的 connection generation／case ID。`showSoftInput` 回傳僅表示 request 已送，不表示視窗已顯示；[isAcceptingText](https://developer.android.com/reference/android/view/inputmethod/InputMethodManager#isAcceptingText()) 是 served view 接受 full text edits 的狀態。等待必須採 bounded condition polling，不能固定 sleep 後直接假定成功。

## CTS 與跨程序驗收模式

[CTS InputMethodServiceTest](https://android.googlesource.com/platform/cts/+/adc36fbed552102026317d8b155071a19925f3eb/tests/inputmethod/src/android/view/inputmethod/cts/InputMethodServiceTest.java) 用 helper Activity＋EditText 在 editor boundary 包 InputConnectionWrapper、記 count，並以當次 editor marker 與 event stream 等待 lifecycle。它示範小型受控 editor 與明確 completion condition 的模式；KU-Yin 可自行重實作此模式，不需要導入 CTS internal libraries 或改裝 MockIme。

[CTS TestActivity](https://android.googlesource.com/platform/cts/+/fa1d53beb05a7685651bfb22e116fde533923fcb/tests/inputmethod/util/src/android/view/inputmethod/cts/util/TestActivity.java) 在 helper Views host 建立 editor；[helper manifest](https://android.googlesource.com/platform/cts/+/fa1d53beb05a7685651bfb22e116fde533923fcb/tests/inputmethod/util/AndroidManifest.xml) 示範 helper app/test activities 與另程序 service。P1-E 應有自有 applicationId／Views activity／唯讀 diagnostics，與產品 apk、Compose、JNI 依賴分離。

[MockIme](https://android.googlesource.com/platform/cts/+/a2a5adae6bb2286789f2d427b923cbf5c6ff0afa/tests/inputmethod/mockime/src/com/android/cts/mockime/MockIme.java) 是 framework end-to-end 測試用 IME，提供 command/event tracing；[MockImeSession](https://android.googlesource.com/platform/cts/+/a2a5adae6bb2286789f2d427b923cbf5c6ff0afa/tests/inputmethod/mockime/src/com/android/cts/mockime/MockImeSession.java) 選取 MockIme、等待 selection 完成、close 時 `ime reset`。直接導入它會切換被測 IME，與驗收 KU-Yin 的目的不符；其 close 亦不保證精確還原原先 default IME。本輪只參考事件／session 邊界，runbook 要保存與還原明示原 IME。CTS [PollingCheck](https://android.googlesource.com/platform/cts/+/f3cd8c8a306dc1b67ca218353b5150a982e6c4d8/common/device-side/util/src/com/android/compatibility/common/util/PollingCheck.java) 示範 predicate＋timeout＋poll interval；可自行寫較小的 bounded polling，不複製整套 CTS harness。

UIAutomator 2.3.0 的正式 release 是 2024-02-21；官方目前主指南已改為 2.4 API。此 fixture 應引用 [2.3.0 release notes](https://developer.android.com/jetpack/androidx/releases/test-uiautomator#2.3.0) 與 [legacy API 指南](https://developer.android.com/training/testing/other-components/ui-automator-legacy)，使用 UiDevice／UiObject2／By／Until，不能誤用 2.4 `uiAutomator { onElement... }` DSL。它從 accessibility hierarchy 操作跨 app／system UI；不可把 Compose in-process test rule 當成另一個 IME process 的 UI controller。

selector 契約：host diagnostics／editor／case controls 用自有 resource ID，搭配 host package；鍵盤用產品 package＋當前 visible text／contentDescription＋clickable/hierarchy 限定。必須檢查匹配唯一性，查不到或多個合理匹配即保留證據並停止該 case。`UiObject2.click` 點擊可見 bounds 的中心；`Until.findObject`／自有 condition 等待 UI、metadata generation 與 side-effect delta。2.3.0 有 [hasParent／hasAncestor](https://developer.android.com/reference/androidx/test/uiautomator/BySelector#hasAncestor(androidx.test.uiautomator.BySelector))，但實際 Compose accessibility hierarchy 仍須在裝置核對，不能從源碼假設穩定 parent 深度。

D13/D14 順序必須是：固定 case readiness → 真 soft-key click 建立 composition → 證明產品 preedit chip 非空 → baseline action/newline/commit counts → click 當前 Enter → bounded 等 delta → 再查 composition 消失／第二次 Enter effect。preedit 優先由「清除」所在的限定 chip subtree 與預期 preedit Text 共同佐證。若 accessibility tree 無法分辨 chip 與鍵面，標 INCONCLUSIVE 並用人工畫面 observation；不得用 adb KEYCODE_ENTER、硬編座標或 direct native call 代填 UI PASS。

## 授權與 provenance 分類

分類針對此次用途，並非允許任意整庫複製。`SAFE_TO_REIMPLEMENT` 指從公開 API／模式獨立寫小型自有實作；`SAFE_TO_COPY_WITH_LICENSE_NOTICE` 僅指檔案明列相容授權、若未來真複製必須保留 copyright/license/NOTICE 並記 provenance。本輪未複製任何外部程式碼。官方 API 頁面沒有取得可固定 source SHA，列 N/A；不將文檔頁面當作已完成逐檔授權審核的 code donor。以下 SHA 分別標明 commit 與 Git blob SHA-1，不混稱 SHA256。

| 精確來源／檔案 | revision／blob | 檔案授權 | 分類與本輪處置 |
| --- | --- | --- | --- |
| [EditorInfo API](https://developer.android.com/reference/android/view/inputmethod/EditorInfo) | N/A，官方 live API 文件 | 文檔頁面；沒有複製文字／code | REFERENCE_ONLY：mask／flag／label/id 契約。 |
| [InputConnectionWrapper API](https://developer.android.com/reference/android/view/inputmethod/InputConnectionWrapper)；[AOSP source](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/android/view/inputmethod/InputConnectionWrapper.java) | commit `99b01a65cc4c104933788b3143285ab6bae65827`；blob `9c3067ce8d637e7748a29cd92f462c6dfd114d4f` | source header Apache-2.0 | SAFE_TO_REIMPLEMENT：自己的 nullable boundary／最小 action wrapper；不複製 wrapper 實作。 |
| [TextView.setImeActionLabel API](https://developer.android.com/reference/android/widget/TextView#setImeActionLabel(java.lang.CharSequence,%20int))；[TextView.java](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/android/widget/TextView.java) | 同上 commit；blob `99fe0cbdca253024bb79a85ae0f5a0393860d7b5` | source header Apache-2.0 | REFERENCE_ONLY：super 正規化、TYPE_NULL 與 action 預設副作用；不移植 TextView。 |
| [EditableInputConnection.java](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/com/android/internal/inputmethod/EditableInputConnection.java) | 同上 commit；blob `0e4f04faf641e5e75ed19a7d7f8aa64072b2fb0e` | Apache-2.0 | REFERENCE_ONLY：說明不 delegate action 的理由。 |
| [RemoteInputConnectionImpl.java](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/android/view/inputmethod/RemoteInputConnectionImpl.java)；[IRemoteInputConnection.aidl](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/com/android/internal/inputmethod/IRemoteInputConnection.aidl) | 同上 commit；blob `3e8575324352facd056d9b9fa3fcd0447c7d0dc3`／`baaf99a36d4d19b99d4f61a2fcb6ea20d77326b5` | 各檔 Apache-2.0 | REFERENCE_ONLY：D17 boolean 在 oneway Binder 不回傳的界線；不使用 internal API。 |
| [InputMethodManager API](https://developer.android.com/reference/android/view/inputmethod/InputMethodManager) | N/A，官方 live API 文件 | 文檔頁面；沒有複製文字／code | SAFE_TO_REIMPLEMENT：focus/show/restart 呼叫與 bounded readiness；不把 request boolean 當 visibility。 |
| [UIAutomator 2.3.0 release](https://developer.android.com/jetpack/androidx/releases/test-uiautomator#2.3.0)／[legacy guide](https://developer.android.com/training/testing/other-components/ui-automator-legacy)／[BySelector](https://developer.android.com/reference/androidx/test/uiautomator/BySelector)／[Until](https://developer.android.com/reference/androidx/test/uiautomator/Until) | artifact `androidx.test.uiautomator:uiautomator:2.3.0`；文檔 SHA N/A | 文檔頁面；沒有複製 code | SAFE_TO_REIMPLEMENT：test-only 跨程序 selector／polling；禁止使用 2.4 DSL。 |
| [CTS MockIme.java](https://android.googlesource.com/platform/cts/+/a2a5adae6bb2286789f2d427b923cbf5c6ff0afa/tests/inputmethod/mockime/src/com/android/cts/mockime/MockIme.java) | commit `a2a5adae6bb2286789f2d427b923cbf5c6ff0afa`；blob `779bba8e915940dcf7ff3c23eae6c9c0a9a38535` | Apache-2.0 | NOT_NEEDED：會替換被測 IME，不導入整套 mock IME。 |
| [CTS MockImeSession.java](https://android.googlesource.com/platform/cts/+/a2a5adae6bb2286789f2d427b923cbf5c6ff0afa/tests/inputmethod/mockime/src/com/android/cts/mockime/MockImeSession.java) | 同上 commit；blob `9a5eba8ab84d01cc5a528b50c3d022cc189fd4bb` | Apache-2.0 | REFERENCE_ONLY：session/event/polling 思路；不採其 ime reset 作精確還原。 |
| [CTS InputMethodServiceTest.java](https://android.googlesource.com/platform/cts/+/adc36fbed552102026317d8b155071a19925f3eb/tests/inputmethod/src/android/view/inputmethod/cts/InputMethodServiceTest.java) | commit `adc36fbed552102026317d8b155071a19925f3eb`；blob `a9cec5104ef5276ed960199c298a246767e5f6f9` | Apache-2.0 | SAFE_TO_REIMPLEMENT：Views editor＋wrapper callback recorder＋marker readiness。 |
| [CTS TestActivity.java](https://android.googlesource.com/platform/cts/+/fa1d53beb05a7685651bfb22e116fde533923fcb/tests/inputmethod/util/src/android/view/inputmethod/cts/util/TestActivity.java)／[AndroidManifest.xml](https://android.googlesource.com/platform/cts/+/fa1d53beb05a7685651bfb22e116fde533923fcb/tests/inputmethod/util/AndroidManifest.xml) | commit `fa1d53beb05a7685651bfb22e116fde533923fcb`；blob `caaa16ecb40fc18b3152df15bb3476f7ea665014`／`1d0ce4735c419bea88267a921f5932e4987639e9` | 各檔 Apache-2.0 | SAFE_TO_REIMPLEMENT：獨立 helper Views host；不複製 CTS starter／internal dependencies。 |
| [CTS PollingCheck.java](https://android.googlesource.com/platform/cts/+/f3cd8c8a306dc1b67ca218353b5150a982e6c4d8/common/device-side/util/src/com/android/compatibility/common/util/PollingCheck.java) | commit `f3cd8c8a306dc1b67ca218353b5150a982e6c4d8`；blob `a976a92643acb73833cf314dbf2449055660d3da` | Apache-2.0 | SAFE_TO_COPY_WITH_LICENSE_NOTICE：小型 polling helper 的可複製授權已確認；本輪選自有重實作，未複製。 |
| [Fcitx5 EditorInfoWindow.kt](https://github.com/fcitx5-android/fcitx5-android/blob/e6199a2801b0c76e1baeff7a48f6b18e910e9dd7/app/src/main/java/org/fcitx/fcitx5/android/input/editorinfo/EditorInfoWindow.kt)／[EditorInfoParser.kt](https://github.com/fcitx5-android/fcitx5-android/blob/e6199a2801b0c76e1baeff7a48f6b18e910e9dd7/app/src/main/java/org/fcitx/fcitx5/android/input/editorinfo/EditorInfoParser.kt) | commit `e6199a2801b0c76e1baeff7a48f6b18e910e9dd7`；blob `ab2f2146ae0b829e2979b22fcbd8fd070d69d56a`／`a5115fa9344c6042f221082437f2deabea19e0a9` | 各檔 SPDX LGPL-2.1-or-later | REFERENCE_ONLY：Inspector 在 attach 時讀 service.currentInputEditorInfo，parser 顯示 class/variation/action/flags；fixture 只需自己的 primitive snapshot，不引入 reflection、clipboard、theme、window manager。 |
| [FlorisBoard EditorInstance.kt](https://github.com/florisboard/florisboard/blob/d4005e881359925f6007b00b94760472014a0eea/app/src/main/kotlin/dev/patrickgold/florisboard/ime/editor/EditorInstance.kt) | commit `d4005e881359925f6007b00b94760472014a0eea`（2026-09-26）；blob `e99cdd9f0a14113fcec0879a57944e7473b0edc7` | 檔頭 Apache-2.0 | REFERENCE_ONLY：performEnterAction 直接回 IC.performEditorAction；performEnter 分 raw key／newline。不移植 editor state 機制。其後 `4e2d8ee8fb0baf668d0e2605b4e10fe3a59d086e` 已刪此檔；所以引用歷史 exact SHA，不能聲稱 current main 還在此位置。 |
| [HeliBoard RichInputConnection.java](https://github.com/HeliBorg/HeliBoard/blob/bc2b91189d60692090bcf30448ba35a9feef8110/app/src/main/java/helium314/keyboard/latin/RichInputConnection.java)／[InputLogic.java](https://github.com/HeliBorg/HeliBoard/blob/bc2b91189d60692090bcf30448ba35a9feef8110/app/src/main/java/helium314/keyboard/latin/inputlogic/InputLogic.java) | commit `bc2b91189d60692090bcf30448ba35a9feef8110`；blob `e6f8551465b16b63b0fe19616e839cd031d3ee09`／`1722a5b6f908f3e09353701f6b962f82b6da8a38` | 各檔 SPDX `Apache-2.0 AND GPL-3.0-only` | LICENSE_REVIEW_REQUIRED：不複製。只讀理解 actionId／IC 接線；其 UNSPECIFIED 送 action0 與 KU-Yin D02 newline 契約不同。 |
| [AOSP LatinIME.java](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/b41bea65502ce7339665859d3c2c81b4a29194e4/java/src/com/android/inputmethod/latin/LatinIME.java) | commit `b41bea65502ce7339665859d3c2c81b4a29194e4`；blob `0f3d28976d457b4741b466b4b10076abd61acf3c` | 檔頭 Apache-2.0 | REFERENCE_ONLY：custom action 送 actionId、標準 action 送 masked action；同樣把 UNSPECIFIED 送 action0。不同 keyboard policy 不能推翻 P1-D 已核定 newline 行為，不移植 LatinIME dispatch。 |

## 接續 fixture review 的必要 gate

初審未發現需要產品依賴、Compose host 或 JNI 的理由。code review 應直接核對：獨立 app/test APK；真正 final outAttrs fidelity；TYPE_NULL 真 null；唯一 action recorder 且不 delegate；文字 delegate 與 newline delta；配置 false 的 local assertion；跨程序唯一 selectors；preedit 正證據與不能量測時的 INCONCLUSIVE；bounded waits 與新 case generation；固定測試內容／case ID 的最小 diagnostics，無真實輸入記錄或 logcat dumping。

build、selftest 與產品 runtime 要分開報告。source/gap 初審完成不代表 30 case runtime PASS；本文件沒有任何 Android 裝置執行結果。

文件檢查：兩個本機相對連結均存在、五種 provenance 分類都有列入、只新增本文件。此 session 沒有可呼叫的 `lsp_diagnostics` 工具；Markdown 做文字／連結與 whitespace 檢查，不將其稱為 LSP 結果。
