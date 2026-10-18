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

## 完整正常CI：03ac326／38661678107（最新）

PR HEAD `03ac3264b4178afccb5781fac6445920bf41dc3a`；actual synthetic merge `89f98a93b66a630327415ece25c3f4a9cb8b0c52`，parents為該HEAD與main63c9e4a，完整tree相同。API33/default/x86_64/pixel7/user0，App5PASS／零skip；Host33JUnit＝24PASS／0FAIL／9SKIP，其中infra1PASS非matrix，32case envelopes＝23PASS／9INCONCLUSIVE。

原始根目錄：外部R2證據 `CI新執行/normal-03ac326-runtime/`；每列owner的原logcat及`testlog/test-results.log`、XML可追溯，完整case JSON另存`normal-03ac326-case-emissions.json`。`normal-03ac326-protocol-audit.json`核33個start/end、exact identities、32個case owner、無duplicate、terminal-1。未修改frozen P1-G26 parser／manifest；此為current suite離線證據覆核。

### 全case／owner與證據

「可觀測」PASS僅指該列scope中原assertions已實際完成；不能取代右列完整驗收。actual欄為最後editor snapshot摘要，其他中間phase保留在原envelope。

| Case／JUnit owner | 原要求 | actual evidence | 可觀測 | 完整result／限制 |
|---|---|---|---|---|
| P0-ZH06 `CoreTypingAcceptanceTest#zh06CandidateThenContinuedPhrasePreservesFirstCommit` | 選我後續組現在、保留prefix／各完成一次 | text="我現在"; commit=2; action=0; newline=0 | PASS | PASS／原assertions完成 |
| P0-ZH05 `CoreTypingAcceptanceTest#zh05ContinuousTonedPhraseCommitsExactlyOnce` | 含聲調我現在連續組字／一次完成 | text="我現在"; commit=1; action=0; newline=0 | PASS | PASS／原assertions完成 |
| P0-ZH02 `CoreTypingAcceptanceTest#zh02ThirdToneWoIsSelectableAndCommitted` | 第三聲我可選且commit1 | text="我"; commit=1; action=0; newline=0 | PASS | PASS／原assertions完成 |
| P0-ZH04 `CoreTypingAcceptanceTest#zh04ContinuousNoTonePhraseCommitsExactlyOnce` | 無聲調我現在連續組字／一次完成 | text="我現在"; commit=1; action=0; newline=0 | PASS | PASS／原assertions完成 |
| P0-ZH01 `CoreTypingAcceptanceTest#zh01NoToneWoIsSelectableAndCommitted` | 無聲調ㄨㄛ→候選我→editor我、commit1 | text="我"; commit=1; action=0; newline=0 | PASS | PASS／原assertions完成 |
| P0-EN02 `CoreTypingAcceptanceTest#en02EnglishDictionaryCompletionCommitsActualEditorWord` | 英文hell→hello候選、正確editor commit | text="hello"; commit=1; action=0; newline=0 | PASS | PASS／原assertions完成 |
| B02 `DachenAcceptanceTest#b02RealCandidateSelectionThenContinuedComposition` | 候選一次提交後續組字／native context身分 | text="你ㄅ"; commit=1; action=0; newline=0 | PASS | INCONCLUSIVE／NATIVE_CONTEXT_IDENTITY_UNOBSERVABLE |
| B03 `DachenAcceptanceTest#b03RealContinuousSentenceCandidateAndSingleFinalCommit` | 連句候選一次提交後續組字／native語意 | text="你好ㄅ"; commit=1; action=0; newline=0 | PASS | INCONCLUSIVE／NATIVE_CONTEXT_AND_SENTENCE_SEMANTICS_UNOBSERVABLE |
| B01 `DachenAcceptanceTest#b01CanonicalKeysAndShiRiHaveStrictPreeditEvidence` | 41鍵／ㄕㄖ真preedit／不同寬度方向 | text=""; commit=0; action=0; newline=0 | PASS | INCONCLUSIVE／ADDITIONAL_WIDTH_AND_ORIENTATION_OBSERVATIONS_REQUIRED |
| D04 `EditorActionAcceptanceTest#d04SearchDispatchesOneAction` | SEARCH action3一次/newline0 | text=""; commit=0; action=1; newline=0 | PASS | PASS／原assertions完成 |
| D07 `EditorActionAcceptanceTest#d07DoneDispatchesOneAction` | DONE action6一次/newline0 | text=""; commit=0; action=1; newline=0 | PASS | PASS／原assertions完成 |
| D02 `EditorActionAcceptanceTest#d02UnspecifiedInsertsOneNewline` | UNSPECIFIED newline1/action0 | text="\n"; commit=1; action=0; newline=1 | PASS | PASS／原assertions完成 |
| D15 `EditorActionAcceptanceTest#d15ExplicitMultilineInsertsNewline` | multiline+NO_ENTER_ACTION newline1 | text="\n"; commit=1; action=0; newline=1 | PASS | PASS／原assertions完成 |
| D05 `EditorActionAcceptanceTest#d05SendDispatchesOneAction` | SEND action4一次/newline0 | text=""; commit=0; action=1; newline=0 | PASS | PASS／原assertions完成 |
| D13 `EditorActionAcceptanceTest#d13CompositionCompletesBeforeSend` | 首次組字完成／第二次SEND，無雙效果 | text="你"; commit=1; action=1; newline=0 | PASS | PASS／原assertions完成 |
| D18 `EditorActionAcceptanceTest#d18AllThreeLayoutsWireRealSoftEnter` | ZH/EN/SYMBOLS softEnter SEND及newline接線 | text="\n\n\n"; commit=3; action=0; newline=3 | PASS | PASS／原assertions完成 |
| D01 `EditorActionAcceptanceTest#d01NoneInsertsOneNewline` | NONE newline1/action0 | text="\n"; commit=1; action=0; newline=1 | PASS | PASS／原assertions完成 |
| D09 `EditorActionAcceptanceTest#d09SendWithNoEnterActionInsertsNewline` | SEND+NO_ENTER_ACTION newline1/action0 | text="\n"; commit=1; action=0; newline=1 | PASS | PASS／原assertions完成 |
| D03 `EditorActionAcceptanceTest#d03GoDispatchesOneAction` | GO action2一次/newline0 | text=""; commit=0; action=1; newline=0 | PASS | PASS／原assertions完成 |
| D11 `EditorActionAcceptanceTest#d11CustomActionUsesCustomId` | custom確認→action73一次 | text=""; commit=0; action=1; newline=0 | PASS | PASS／原assertions完成 |
| D14 `EditorActionAcceptanceTest#d14CompositionCompletesBeforeNewline` | 首次組字完成／第二次newline，無雙效果 | text="你\n"; commit=2; action=0; newline=1 | PASS | PASS／原assertions完成 |
| D06 `EditorActionAcceptanceTest#d06NextDispatchesOneAction` | NEXT action5一次/newline0 | text=""; commit=0; action=1; newline=0 | PASS | PASS／原assertions完成 |
| D12 `EditorActionAcceptanceTest#d12NoEnterActionOverridesCustomAction` | NO_ENTER_ACTION優先custom→newline1 | text="\n"; commit=1; action=0; newline=1 | PASS | PASS／原assertions完成 |
| D10 `EditorActionAcceptanceTest#d10GoWithNoEnterActionInsertsNewline` | GO+NO_ENTER_ACTION newline1/action0 | text="\n"; commit=1; action=0; newline=1 | PASS | PASS／原assertions完成 |
| D08 `EditorActionAcceptanceTest#d08PreviousDispatchesOneAction` | PREVIOUS action7一次/newline0 | text=""; commit=0; action=1; newline=0 | PASS | PASS／原assertions完成 |
| D17 `EditorActionAcceptanceTest#d17HostFalseHasObservableActionButIpcScopeRemainsOpen` | SEND一次、host false／Binder scope | text=""; commit=0; action=1; newline=0 | PASS | INCONCLUSIVE／HOST_FALSE_NOT_PROPAGATED_BY_ANDROID_IPC |
| D16 `EditorActionAcceptanceTest#d16CustomNewlineLabelRemainsTextAction` | custom label換行仍action73 | text=""; commit=0; action=1; newline=0 | PASS | PASS／原assertions完成 |
| C05 `NonTextEditorAcceptanceTest#c05ReturningToTextRestoresBothPriorPreferredModes` | 原ZH／EN偏好經non-text返回／學習 | text=""; commit=0; action=0; newline=0 | PASS | INCONCLUSIVE／NATIVE_LEARNING_OBSERVER_UNAVAILABLE |
| C01 `NonTextEditorAcceptanceTest#c01NumberTemporarilyUsesEnglish` | NUMBER metadata／temporary ENGLISH／native學習 | text=""; commit=0; action=0; newline=0 | PASS | INCONCLUSIVE／NATIVE_LEARNING_OBSERVER_UNAVAILABLE |
| C03 `NonTextEditorAcceptanceTest#c03DatetimeTemporarilyUsesEnglish` | DATETIME metadata／temporary ENGLISH／native學習 | text=""; commit=0; action=0; newline=0 | PASS | INCONCLUSIVE／NATIVE_LEARNING_OBSERVER_UNAVAILABLE |
| C02 `NonTextEditorAcceptanceTest#c02PhoneTemporarilyUsesEnglish` | PHONE metadata／temporary ENGLISH／native學習 | text=""; commit=0; action=0; newline=0 | PASS | INCONCLUSIVE／NATIVE_LEARNING_OBSERVER_UNAVAILABLE |
| C04 `NonTextEditorAcceptanceTest#c04NullConnectionIsRealOrExplicitlyInconclusive` | TYPE_NULL真IC邊界／可觀測policy／學習 | text=""; commit=0; action=0; newline=0 | INCONCLUSIVE | INCONCLUSIVE／BLOCKED_SELECTOR |

額外infra：`StaleUiObjectIdentityRegressionTest#destroyedEditorKeepsMapIdentityButCannotBeReadAsCurrentUi` PASS；原getter必stale、既存map identity不得throw，沒有case envelope。

### 舊9FAIL與8SKIP的逐項處置

- 舊9FAIL：P0-ZH01／02／04／05、P0-EN02、D13、D14七案此輪PASS，原可見soft-key／preedit／exact candidate／editor commit/action斷言真執行。B02、B03可見輸入／候選／commit／繼續組字斷言已走完，分別抵達原本已存在的NATIVE_CONTEXT_IDENTITY_UNOBSERVABLE及NATIVE_CONTEXT_AND_SENTENCE_SEMANTICS_UNOBSERVABLE；完整仍INCONCLUSIVE，不計FAIL→SKIP為修復。
- 舊8SKIP：P0-ZH06現在PASS；B01、C01–C05、D17七案仍INCONCLUSIVE。B01已驗41鍵及ㄕㄖ真preedit但缺第二寬度／方向；C01/02/03/05可見mode assertions通過仍缺native learning observer；D17可見SEND1／false／newline0仍缺同步Binder回傳證明。
- C04只有final input_type0、ime_options1、真nullIC；81個completed snapshot仍無product node，後續capture未在原deadline完成而BLOCKED_SELECTOR。不能將NULL IC或空tree推成IME端fallback IC／temporary mode／學習PASS。

**完整Host驗收=PARTIAL；不是fullPASS。** 無新product defect由本輪raw證實，剩餘是觀測／驗收scope缺口。不得改assertions、故意展示不存在的TYPE_NULL軟鍵、增加production observer或刪native requirements以強行通過。PR10仍HOLD。

### 本輪實際測試APK身份

以下SHA由正常CI runner對實檔計算並apksigner驗證，非本機重建／公開Alpha資產；四者該VM cert SHA256均`56c8e3c485aadf3090081b04c1d3e7620fa51096a546def0cb05e75436171ebb`。

| CI路徑 | bytes | SHA256 |
|---|---:|---|
| `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk` | 970016 | `22747fa517174c2b77a9bde83e7c1829896bb156a1d7bb7232b8dec87fc11c15` |
| `app/build/outputs/apk/debug/app-debug.apk` | 30380975 | `2808678598cf5b98d1534c232116aafa32a061ae141e1d89d869541fd1dece31` |
| `acceptance-host/build/outputs/apk/androidTest/debug/acceptance-host-debug-androidTest.apk` | 1100804 | `b9b90898e7d6961632c38b9487c73f17bd0d591a814a46568040ddfc2fe4dd55` |
| `acceptance-host/build/outputs/apk/debug/acceptance-host-debug.apk` | 861628 | `a50f2d764e545bc33754bc0e9f448d3798ef0a6adf7882f53f4612f7a8e48ae9` |
