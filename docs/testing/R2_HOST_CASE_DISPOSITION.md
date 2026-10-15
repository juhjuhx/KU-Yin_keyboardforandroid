# R2 Host 個案裁定

來源：controlled run38459179007，source7f474c41／actual synthetic698d107c。A/B/C各自原始XML、32 unique JUnit identities與32 case envelope逐項核對；source manifest的每個測試blob SHA與canonical source相同。普通run38459178799也得到C相同15PASS／0FAIL／17SKIP。

A/B Host皆15PASS9FAIL8SKIP；C的9個FAIL全部變為SKIP／INCONCLUSIVE，**0項已驗證修復**。原App5在三組都執行並PASS；歷史ea5 App4PASS1FAIL、Host未執行不能混用。

| Case | JUnit method | A原main | C前輪7f | 當次理由／完整scope |
|---|---|---|---|---|
| P0-ZH06 | `CoreTypingAcceptanceTest#zh06CandidateThenContinuedPhrasePreservesFirstCommit` | SKIP | SKIP／INCONCLUSIVE | BLOCKED_SELECTOR：fresh capture 未完成；soft-key/preedit契約未證實 |
| P0-ZH05 | `CoreTypingAcceptanceTest#zh05ContinuousTonedPhraseCommitsExactlyOnce` | FAIL | SKIP／INCONCLUSIVE | BLOCKED_SELECTOR：fresh capture 未完成；soft-key/preedit契約未證實 |
| P0-ZH02 | `CoreTypingAcceptanceTest#zh02ThirdToneWoIsSelectableAndCommitted` | FAIL | SKIP／INCONCLUSIVE | BLOCKED_SELECTOR：fresh capture 未完成；soft-key/preedit契約未證實 |
| P0-ZH04 | `CoreTypingAcceptanceTest#zh04ContinuousNoTonePhraseCommitsExactlyOnce` | FAIL | SKIP／INCONCLUSIVE | BLOCKED_SELECTOR：fresh capture 未完成；soft-key/preedit契約未證實 |
| P0-ZH01 | `CoreTypingAcceptanceTest#zh01NoToneWoIsSelectableAndCommitted` | FAIL | SKIP／INCONCLUSIVE | BLOCKED_SELECTOR：fresh capture 未完成；soft-key/preedit契約未證實 |
| P0-EN02 | `CoreTypingAcceptanceTest#en02EnglishDictionaryCompletionCommitsActualEditorWord` | FAIL | SKIP／INCONCLUSIVE | BLOCKED_SELECTOR：fresh capture 未完成；soft-key/preedit契約未證實 |
| B02 | `DachenAcceptanceTest#b02RealCandidateSelectionThenContinuedComposition` | FAIL | SKIP／INCONCLUSIVE | BLOCKED_SELECTOR：fresh capture 未完成；soft-key/preedit契約未證實 |
| B03 | `DachenAcceptanceTest#b03RealContinuousSentenceCandidateAndSingleFinalCommit` | FAIL | SKIP／INCONCLUSIVE | BLOCKED_SELECTOR：fresh capture 未完成；soft-key/preedit契約未證實 |
| B01 | `DachenAcceptanceTest#b01CanonicalKeysAndShiRiHaveStrictPreeditEvidence` | SKIP | SKIP／INCONCLUSIVE | ADDITIONAL_WIDTH_AND_ORIENTATION_OBSERVATIONS_REQUIRED：portrait可見／preedit部分證據；第二寬度／rotation未完成 |
| D13 | `EditorActionAcceptanceTest#d13CompositionCompletesBeforeSend` | FAIL | SKIP／INCONCLUSIVE | BLOCKED_SELECTOR：fresh capture 未完成；soft-key/preedit契約未證實 |
| D14 | `EditorActionAcceptanceTest#d14CompositionCompletesBeforeNewline` | FAIL | SKIP／INCONCLUSIVE | BLOCKED_SELECTOR：fresh capture 未完成；soft-key/preedit契約未證實 |
| D17 | `EditorActionAcceptanceTest#d17HostFalseHasObservableActionButIpcScopeRemainsOpen` | SKIP | SKIP／INCONCLUSIVE | HOST_FALSE_NOT_PROPAGATED_BY_ANDROID_IPC：Host action/false可見；IME同步false IPC scope仍OPEN |
| C05 | `NonTextEditorAcceptanceTest#c05ReturningToTextRestoresBothPriorPreferredModes` | SKIP | SKIP／INCONCLUSIVE | NATIVE_LEARNING_OBSERVER_UNAVAILABLE：可見mode／editor部分證據；native learning observer仍OPEN |
| C01 | `NonTextEditorAcceptanceTest#c01NumberTemporarilyUsesEnglish` | SKIP | SKIP／INCONCLUSIVE | NATIVE_LEARNING_OBSERVER_UNAVAILABLE：可見mode／editor部分證據；native learning observer仍OPEN |
| C03 | `NonTextEditorAcceptanceTest#c03DatetimeTemporarilyUsesEnglish` | SKIP | SKIP／INCONCLUSIVE | NATIVE_LEARNING_OBSERVER_UNAVAILABLE：可見mode／editor部分證據；native learning observer仍OPEN |
| C02 | `NonTextEditorAcceptanceTest#c02PhoneTemporarilyUsesEnglish` | SKIP | SKIP／INCONCLUSIVE | NATIVE_LEARNING_OBSERVER_UNAVAILABLE：可見mode／editor部分證據；native learning observer仍OPEN |
| C04 | `NonTextEditorAcceptanceTest#c04NullConnectionIsRealOrExplicitlyInconclusive` | SKIP | SKIP／INCONCLUSIVE | BLOCKED_SELECTOR：fresh capture 未完成；soft-key/preedit契約未證實 |

C04在A觀察null IC後仍因native learning observer不足；B是TYPE_NULL_SOFT_INPUT_UNOBSERVABLE；C為BLOCKED_SELECTOR（69 completed空tree，0可見product nodes）。不能算non-text contract PASS。

P0-ZH06已有candidate「我」commit_count=1／CURRENT_TEXT="我"的部分真editor證據，後續composition觀測失敗；不能當continuous phrase PASS。B01完整width/orientation、A03 rotation、A04安全fault、C native learning、D17 Binder仍OPEN。

目前15 PASS為D01–D12、D15、D16、D18；保留原actual EditorInfo、visible soft key、action ID/count、newline delta、exactly-once assertions。其餘實體30-case acceptance不由JUnit總數推定。

9案常見UiObject2 refresh/stale，但先前case總attempt包含cleanup，不等於一個5秒deadline的attempt。需[R2有限phase probe](R2_HOST_OBSERVATION_PROBE.md)分離body/cleanup與getter／REGISTER。9f trace已將10案BODY/CLEANUP定位REGISTER；741 source再定位QUERY_REGISTER。完整source與raw結果分別保存，不把compile當runtime。

外部主證據：`/Users/huang/Desktop/KU-Yin-R2-Verification-2026-10-13/CI新執行/controlled-results.json`、controlled-runtime原始XML/logcat及「受控比較生命週期與根因獨立覆核.md」。舊incomplete-parser JSON保留，不作identity證據；修正後以nested testcase與source manifest hash核對5/32 identities。

## 當前狀態（完整新Host尚待）

9f full38524582991仍15PASS17SKIP，前輪九FAIL沒有算修復；C04已直接觀察null InputConnection與temporary ENGLISH，但native learning observer缺口仍INCONCLUSIVE。741有限ZH06→ZH01均QUERY_REGISTER stale。3fc的same stale-map identity regression已真RED→GREEN，但不是上述任何matrix row；38563975932因App firstVisibility FAIL未執行Host；當時新3895625有限sequence38660489452尚待結果；完成後的有限成功另列下節，不改歷史table。完整新Host會另逐案追加exact owner／原assertion執行與text/action/commit結果，不把SKIP當PASS。

## 3895625有限成功：38660489452

原App5PASS；Host指定2個identities與兩個sendStatus(2) case envelopes皆PASS、零skip，原始1→2→0 lifecycle、numtests2、OK2及terminal-1完整。183個source hashes對exact commit零mismatch。

- P0-ZH06：可見軟鍵strict preedit→tap我，actual text我／commit_count1；再strict preedit→tap現在，actual我現在／commit_count2；action/newline皆0。
- P0-ZH01：無聲調ㄨㄛ可見→tap我，actual我／commit_count1；action/newline0。

兩案總stale分別13／2；保留的recent entries皆為PARENT（ZH06只保留最近8條，前5次phase無原始記錄），在原bounded read內恢復；不稱零stale或效能提升。這輪只證明上述有限行為，不將其他8個舊FAIL、fullHost或physical scope推定PASS。下一步完整33JUnit／32envelopes。
