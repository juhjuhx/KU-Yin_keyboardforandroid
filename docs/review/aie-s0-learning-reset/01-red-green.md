# S0 RED/GREEN 與能力邊界

## RED（產品修改前）
`./gradlew :app:testDebugUnitTest --tests com.example.LearningResetSafetyUiTest --console=plain` exit1，3測2敗。
- complete learning reset is unavailable rather than falsely successful：期待 disabled，實際 enabled。
- old reset entry cannot partially erase learning or settings or files：測試 fixture fallback freq_我=7，原 confirm 之後=0，native fixture 仍在。
- fallback only reset API still clears its own test store：PASS。
證據 `build/aie-s0-evidence/red.log`、`red.xml`。前兩輪 LazyColumn selector 定位錯誤以及 sandbox lock socket 問題未計入有效 RED；定位改為唯一垂直 scroll container。

## scenario disposition
| Case | 本輪證據／範圍 |
|---|---|
| S0-01 fallback API | pure Android prefs/Robolectric API 成功；不向使用者宣稱 full reset |
| S0-02 native reset success | UNSUPPORTED；沒有 full erase 嘗試 |
| S0-03 failure不可成功 | 原 full reset UI 停用、無 confirm/success path |
| S0-04 partial failure | prevention：不刪任何 store，明示 unsupported，故不產生 partial delete |
| S0-05 settings | 人工 prefs fixture 保留 |
| S0-06 deleted state復活 | N/A，不宣稱任何刪除；真正 erase/reinit 驗證 OPEN |
| S0-07 unrelatedfiles | 人工 unrelated 與 native fixture bytes 保留 |
| S0-08 sensitive/private | 現有 NativeLearningPolicy/EditorPolicy tests；private mode 未新增 |
| S0-09 active context | no deletion、no native呼叫；完整 active erase仍UNSUPPORTED，非模擬 native證明 |
| S0-10 native reinit | 既有真 pinned JNI P0初始化/typing gate重新執行，非reset success證明 |

完整學習 erase release gate 仍 OPEN。S0採使用者明確允許之停用/unsupported分支，安全缺陷可關閉為 MITIGATED_BY_UNAVAILABLE_STATE，不能報 native erase PASS。

## GREEN
`JAVA_HOME=…21 ANDROID_HOME=…commandlinetools KUYIN_NATIVE_HOST_DIR=…p0r-evidence/unicode-final-green KUYIN_NATIVE_HOST_DATA=…chewing-dict ./gradlew :app:testDebugUnitTest --console=plain` exit0。50 classes / 232 tests / 0 failures / 0 errors / 0 skipped。S0新增3測、NativeTypingEditorIntegrationTest真JNI→service/Compose→Robolectric EditText5/5、P0核心229全量通過。log `build/aie-s0-evidence/green-full.log`；bootstrap exit0；fresh static contracts10/10 exit0。
