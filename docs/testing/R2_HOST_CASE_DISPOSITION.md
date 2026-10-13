# R2 Host 個案裁定

歷史主證據是run38224799842／38258286202 raw XML、testlog及case logcat；兩者同32 identities、15PASS／9FAIL／8SKIP。ea5 run38254806233是App4PASS1FAIL，Host未跑，不能混用。

完整獨立歷史讀取與hash在外部 `CI唯讀比對/`。目前9個failure共同出現UiAutomator stale，尚不等同9個產品defect；原8skip是明確observability/selector不足。

| Case | JUnit method（package io.github.juhjuhx.kuyin.acceptancehost） | Historical | 直接原因 | R2 runtime裁定 |
|---|---|---|---|---|
| P0-ZH06 | `CoreTypingAcceptanceTest#zh06CandidateThenContinuedPhrasePreservesFirstCommit` | SKIP ← INCONCLUSIVE | `BLOCKED_SELECTOR` | NOT_RUN；保留原契約 |
| P0-ZH05 | `CoreTypingAcceptanceTest#zh05ContinuousTonedPhraseCommitsExactlyOnce` | FAIL | `StaleObjectException / ASSERTION_OR_RUNTIME_FAILURE` | NOT_RUN；保留原契約 |
| P0-ZH02 | `CoreTypingAcceptanceTest#zh02ThirdToneWoIsSelectableAndCommitted` | FAIL | `StaleObjectException / ASSERTION_OR_RUNTIME_FAILURE` | NOT_RUN；保留原契約 |
| P0-ZH04 | `CoreTypingAcceptanceTest#zh04ContinuousNoTonePhraseCommitsExactlyOnce` | FAIL | `StaleObjectException / ASSERTION_OR_RUNTIME_FAILURE` | NOT_RUN；保留原契約 |
| P0-ZH01 | `CoreTypingAcceptanceTest#zh01NoToneWoIsSelectableAndCommitted` | FAIL | `StaleObjectException / ASSERTION_OR_RUNTIME_FAILURE` | NOT_RUN；保留原契約 |
| P0-EN02 | `CoreTypingAcceptanceTest#en02EnglishDictionaryCompletionCommitsActualEditorWord` | FAIL | `StaleObjectException / ASSERTION_OR_RUNTIME_FAILURE` | NOT_RUN；保留原契約 |
| B02 | `DachenAcceptanceTest#b02RealCandidateSelectionThenContinuedComposition` | FAIL | `StaleObjectException / ASSERTION_OR_RUNTIME_FAILURE` | NOT_RUN；保留原契約 |
| B03 | `DachenAcceptanceTest#b03RealContinuousSentenceCandidateAndSingleFinalCommit` | FAIL | `StaleObjectException / ASSERTION_OR_RUNTIME_FAILURE` | NOT_RUN；保留原契約 |
| B01 | `DachenAcceptanceTest#b01CanonicalKeysAndShiRiHaveStrictPreeditEvidence` | SKIP ← INCONCLUSIVE | `ADDITIONAL_WIDTH_AND_ORIENTATION_OBSERVATIONS_REQUIRED` | NOT_RUN；保留原契約 |
| D13 | `EditorActionAcceptanceTest#d13CompositionCompletesBeforeSend` | FAIL | `StaleObjectException / ASSERTION_OR_RUNTIME_FAILURE` | NOT_RUN；保留原契約 |
| D14 | `EditorActionAcceptanceTest#d14CompositionCompletesBeforeNewline` | FAIL | `StaleObjectException / ASSERTION_OR_RUNTIME_FAILURE` | NOT_RUN；保留原契約 |
| D17 | `EditorActionAcceptanceTest#d17HostFalseHasObservableActionButIpcScopeRemainsOpen` | SKIP ← INCONCLUSIVE | `HOST_FALSE_NOT_PROPAGATED_BY_ANDROID_IPC` | NOT_RUN；保留原契約 |
| C05 | `NonTextEditorAcceptanceTest#c05ReturningToTextRestoresBothPriorPreferredModes` | SKIP ← INCONCLUSIVE | `NATIVE_LEARNING_OBSERVER_UNAVAILABLE` | NOT_RUN；保留原契約 |
| C01 | `NonTextEditorAcceptanceTest#c01NumberTemporarilyUsesEnglish` | SKIP ← INCONCLUSIVE | `NATIVE_LEARNING_OBSERVER_UNAVAILABLE` | NOT_RUN；保留原契約 |
| C03 | `NonTextEditorAcceptanceTest#c03DatetimeTemporarilyUsesEnglish` | SKIP ← INCONCLUSIVE | `NATIVE_LEARNING_OBSERVER_UNAVAILABLE` | NOT_RUN；保留原契約 |
| C02 | `NonTextEditorAcceptanceTest#c02PhoneTemporarilyUsesEnglish` | SKIP ← INCONCLUSIVE | `NATIVE_LEARNING_OBSERVER_UNAVAILABLE` | NOT_RUN；保留原契約 |
| C04 | `NonTextEditorAcceptanceTest#c04NullConnectionIsRealOrExplicitlyInconclusive` | SKIP ← INCONCLUSIVE | `NATIVE_LEARNING_OBSERVER_UNAVAILABLE` | NOT_RUN；保留原契約 |

可審查修正來自local `4fc43ffe29a361ea7916abf0e319bdf42dea8eb6`：完整UI樹snapshot、stale/recovery分類、同一deadline scoped idle。未移除32-test identities、strict preedit、composition span、settled editor delta及exactly-once斷言。97個Host pure JVM tests只證明工具pure logic；新實際case結果必須另填。

087ff39歷史15PASS／17SKIP不算修復。原FAIL→SKIP不列為repaired；INCONCLUSIVE與observable PASS分開；A04、native learning、TYPE_NULL、D17 Binder、rotation/B context仍不得豁免。
