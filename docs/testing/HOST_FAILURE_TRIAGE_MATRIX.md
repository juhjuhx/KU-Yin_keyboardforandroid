# Host failure triage — R3.3

## Evidence and scope

CI [39515675883](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39515675883): source43aff1d, synthetic merged423a7e; both tree89102170fc74775f0fe69e39f994a859a54c218a. Exact raw XML and `emulator-5554 - 13/testlog/test-results.log` have32 unique case envelopes:15 PASS/9 FAIL/8 INCONCLUSIVE; JUnit has15 PASS/9 FAIL/8 SKIP. Experimental job FAILURE despite overall workflow SUCCESS. This is historical run evidence re-read now, not new Runtime execution. No assertion or result changed.

Paths below relative to `acceptance-host/src/androidTest/java/io/github/juhjuhx/kuyin/acceptancehost/`:
AD=AcceptanceDriver.kt, CT=CoreTypingAcceptanceTest.kt, DA=DachenAcceptanceTest.kt, EA=EditorActionAcceptanceTest.kt, NT=NonTextEditorAcceptanceTest.kt. LOG lines refer to original test-results.log.

## Shared causal boundaries

AD495 re-queries every product node, reads all visibleBounds(497), then filters description at491. A stale node is not necessarily the clear/preedit target. AD538–549 already polls/re-queries; eight composition cases throw at AD197's extra strictPreedit read **after** polling returned false. Their logcat contains65–75 refresh failures over about4.4s. Not proof of one transient event or a wrong decoder.

`strictPreedit`(230–249) traverses live parents/children/text/bounds before copying text+Rect; uniqueClickable(477–480) returns live objects for later click. CT78–80 retains a header during candidate waiting. A later repair must bound **reads**, re-query targets and never replay a tap that may already have been delivered. Do not add a blanket stale catch or widen timeout.

High selector mismatch: CT63–64 and AD430–431 require candidates beside/vertically overlapping preedit-clear. Product `KuYinKeyboardUi.kt:459–485` puts preedit/clear Row then CandidateStrip in a separate following Row. Source proves fixture geometry mismatch, but no candidate bounds in these raw failures proves whether a particular candidate was generated. Fix observer, not Dachen/product geometry.

## Nine FAIL — all retained

| JUnit method / case | Primary boundary / LOG | Last established behavior | Classification / remaining original requirements |
|---|---|---|---|
| CT.zh01NoToneWoIsSelectableAndCommitted / P0-ZH01 | CT19→AD197→230→491→495→497;226 | strict preeditㄨ; secondㄛ click invoked, effect unproven | selector runtime failure; still require actual我 candidate, editor我,commit+1,no action |
| CT.zh02ThirdToneWoIsSelectableAndCommitted / P0-ZH02 | CT25→same;128 | ㄨ only; failure before third tone | same; requireㄨㄛˇ and original completion assertions |
| CT.zh04ContinuousNoTonePhraseCommitsExactlyOnce / P0-ZH04 | CT31→same;177 | ㄨ only; no full phrase proof | same; exact我現在,one commit,not nonempty text |
| CT.zh05ContinuousTonedPhraseCommitsExactlyOnce / P0-ZH05 | CT37→same;79 | ㄨ only; no tone reached | same; original10-glyph sentence and exactly-one effect |
| CT.en02EnglishDictionaryCompletionCommitsActualEditorWord / P0-EN02 | CT52→60→AD220 first;cleanup AD583→461→255→497;275 | four currentText-change checks returned; raw hell not established | primary BLOCKED_SELECTOR then real cleanup stale elevated toFAIL; require hello exact,commit+1,no action |
| DA.b02RealCandidateSelectionThenContinuedComposition / B02 | DA35→AD197…497;319 | ㄋ; secondㄧ click invoked; no candidate click | selector failure; selection must retain unfinished input/no early or double commit; native identity still unobserved |
| DA.b03RealContinuousSentenceCandidateAndSingleFinalCommit / B03 | DA54→same;368 | ㄋ only; no second syllable/correction/Enter | selector failure; preserve DA58–66 no early commit,finalone commit,continuedㄅ |
| EA.d13CompositionCompletesBeforeSend / D13 | EA49→16→AD197…497;577 | initial empty assertion andㄋ observed | require firstEnter composition-only; secondSEND exactlyone,no LF |
| EA.d14CompositionCompletesBeforeNewline / D14 | EA50→16→same;723 | initial empty assertion andㄋ observed | require firstEnter composition-only; secondone LF,no action |

For eight composition FAILs the last explicit launch assertion is AD78 (D13/D14 also EA14 empty composition). Sequence evidence localizes the second glyph via compose loop but does not prove delivery to product. Their cleanup stale is suppressed, not lost. EN02 primary inconclusive is retained in suppressed/log under cleanup failure via sharedTest CaseFailurePolicy.kt11–13; XML alone omits that distinction. No case has been repaired by this triage.

## Eight SKIP — original reason and retest gate

| Method / case | Source / LOG / original reason | Proven subset | Missing evidence / retest gate |
|---|---|---|---|
| CT.zh06CandidateThenContinuedPhrasePreservesFirstCommit / P0-ZH06 | CT44→61→97;12;BLOCKED_SELECTOR | ㄨ,ㄨㄛ preedit; editorㄨㄛ,commit0,action0 | correct next-row candidate selector; first我 commit+1 then我現在 another+1,never overwrite |
| DA.b01CanonicalKeysAndShiRiHaveStrictPreeditEvidence / B01 | DA28;417;ADDITIONAL_WIDTH_AND_ORIENTATION_OBSERVATIONS_REQUIRED | 41 hitboxes/rows/hints,ㄕㄖ strict preedit,clear no effects | second width + orientation transition/recovery with original mapping/assertions |
| EA.d17HostFalseHasObservableActionButIpcScopeRemainsOpen / D17 | EA33;850;HOST_FALSE_NOT_PROPAGATED_BY_ANDROID_IPC | oneSEND id4,host false,text empty,newline0,commit0 | IME-side IPC return semantics unresolved; no silent contract reduction |
| NT.c01NumberTemporarilyUsesEnglish / C01 | NT15;1000;NATIVE_LEARNING_OBSERVER_UNAVAILABLE | inputType2/IC true,tempEnglish,blockedChinese,noeffects | isolated approved native learning observer; UI is not learning proof |
| NT.c02PhoneTemporarilyUsesEnglish / C02 | NT15;1130;same | inputType3,as above | same; no private dictionary reads/reset |
| NT.c03DatetimeTemporarilyUsesEnglish / C03 | NT15;1065;same | inputType4,as above | same |
| NT.c04NullConnectionIsRealOrExplicitlyInconclusive / C04 | NT15;1195;same | inputType0,hostIC=false,visibleEnglish/noeffects | currentIME null boundary/fallback plus learning evidence; old visible keyboard not proof of binding |
| NT.c05ReturningToTextRestoresBothPriorPreferredModes / C05 | NT26;935;same | 2preferred×4restricted=8 visible mode restorations,empty composition | learning + TYPE_NULL boundary,retain all8 transitions |

## Independent disposition

Fresh reviewer `/root/r33_host_triage`: specification INCONCLUSIVE; fixture/observation engineering FAIL with concrete entrances above. Product defect not established. No new host/JNI observer without separate approved boundary. Any new current fixture compile/runtime is distinct from frozen P1-E/P1-G manifests.

Adjacent static risk AD460–465: if clearComposition and active.close both throw, finally may overwrite the first cleanup error. Not shown in this run; need a focused two-failure regression, not a claim it happened.
