# P0-R Android editor effects

沒有可用 Android 裝置。`RUNTIME=NOT_TESTED`、`PRODUCT_TYPING_ACCEPTANCE=OPEN_AFTER_FIX`；使用者的失敗報告沒有被 JVM／compile 結果關閉。

## Effects ownership

`KuYinKeyboardUi → DispatchResult.orderedCommits → KuYinInputMethodService → currentInputConnection.commitText`。Service 是 Android effects 邊界；decoder 產生文字，UI 不把 preedit 當成已送出。

- 非空 preedit 用 `setComposingText` 投影，記錄 editor generation／InputConnection 及 composing range。
- 候選點選、Space／Enter 先完成 composition；組字 Enter 被 consumed，不附帶 editor action／newline。
- 自身 selection callback 不重置 decoder；外部 cursor／span 改變會重置，包含游標不變但 editor 移除 span。
- `commitText=false` 保存未確認 ordered tail 並阻止後續輸入，不自動重送。Editor 可能已寫入卻回 false，重送會 double commit；使用者可明確捨棄，敏感 editor 遮蔽失敗文字。
- 切 editor 不把上一欄的失敗文字送進新欄；空 composition 不刪 unowned editor selection。

## 真 JNI＋EditText

`NativeTypingEditorIntegrationTest` 5 案以同 pin Rust libchewing、production JNI／adapter、正式 service／Compose keyboard，接到 Robolectric 真 EditText InputConnection：

1. ㄨㄛ無聲調，候選「我」可選，editor =「我」。
2. ㄨㄛˇ，editor =「我」。
3. 無聲調連打我現在，editor =「我現在」。
4. 有聲調連打我現在，editor =「我現在」。
5. 選「我」後繼續「現在」，editor 最終 =「我現在」。

最新 5／5 通過，0 skipped，XML 在 `build/p0r-evidence/final-app-xml/`。Synthetic trace 記錄 event、可見注音／Dachen ASCII、provider、preedit、候選、commit requested/result、editor delta。診斷僅在測試程式，product／release 沒有記錄私人的 typing text。

其他真 EditText tests 覆蓋 false ACK／ordered tail、自己的 callback、外部 cursor/span、editor 切換、不刪 unowned text；候選 UI 回歸驗證 duplicate label 使用 snapshot index。

## 實機準備與限制

新的 `CoreTypingAcceptanceTest.kt` 有 6 方法：無／有聲調我、兩種我現在、分段繼續輸入、英文 hell→hello。採實際 KU-Yin package 可見／唯一 clickable soft-key，assert host CURRENT_TEXT、commit count、action delta，沒有硬體 Enter 代替。

兩個 androidTest APK 已 assemble；沒有 install／IME selection／settings mutation。**舊 v3 runner 固定 P1 hashes 與 26-test manifest，不可用來測新的 P0 APK／32-method host suite。** 裝置可用且獲授權後，固定 serial，先保存 IME 狀態並通過既有安全 preflight，再執行新的 P0 class。

未證明：跨 process Binder／OEM editor、手機已裝 bytes、實際觸控速度、所有 app compatibility。A04 safe fault、native-learning physical observer、D17 oneway Binder、physical rotation 等 P1 限制保留。
