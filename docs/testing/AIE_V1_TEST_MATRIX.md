# AIE V1 測試矩陣

S0/S1 已實作；CAN-01～06 有本輪 session/Compose/真 pinned host JNI 證據，實機 DEV-01 仍 NOT_TESTED。新 ranking／learning／touch／symbol 案仍 PLANNED_NOT_RUN；以下保留完整規劃範圍。最終 fresh counts 見 S1 交接，不能以 coverage 替代 release readiness。

| ID / 層 | 可觀察契約 | Test seam / direct evidence |
|---|---|---|
| MAP-01 Kotlin＋Compose touch | 41 canonical keys glyph/code正確，中心/邊界/GAP不吞或雙發 | DachenKeyboardRows＋坐標performTouchInput；不能只semantics click |
| ZH-01 真JNI | ㄨㄛ無調包含我；ㄨㄛˇtone refinement | production pinned adapter；我不必top1 |
| ZH-02 真JNI/editor | 我現在有調/無調連句、candidate→continue | 原nativehost probe＋NativeTypingEditorIntegrationTest；另真裝置 |
| ZH-03 session/editor | Backspace、tone、Space、Enter、punct、mode不丟／雙commit | nearbyexistingP0regressions＋failureACK |
| CAN-01 Compose | 第二backendpage可達（>10原始plans） | visibleprev/next→fake sessiondispatch，boundarydisabled |
| CAN-02 Compose | expand/collapse不nativecommit、候選不丟；keyboard返回 | UI state/command spy與actualcallback，不測implementationmirror |
| CAN-03 Compose gestures | scroll/drag不選字；不同page snapshot一致 | touchswipe＋selectedindex／commitcount |
| CAN-04 Compose/session | decoder更新後舊candidatecallback拒絕、duplicatesidentity正確 | generation/ref＆capturedsnapshot；負案例 |
| CAN-05 Compose rendering | preedit長句/大字級/320360600dp保可操作candidate/controls | bounds＋實際touch；非截图就是runtime |
| CAN-06 真JNI | rawpool/page/display/count/去重分層、paging不改input | nativequery tracing僅synthetic，lastTotal不是uniquecount |
| RANK-01 replay | 同state/dictver順序deterministic、samecandidateidentity | 固定syntheticgoldencorpus；non-topselected正確nativechoice |
| RANK-02 replay | recall@K/MRR/phrase/context品質對baseline | separatepoolmiss/rankmiss；不inventcandidate、不silentrewrite |
| LEARN-01 store/native | explicitconfirmedselection持久化一次，preview不寫 | tempstores＋eventid，native/externalowner不雙計 |
| LEARN-02 store/editor | falseACK、cancel、display、page、scroll不學 | failureIC＋writespy＋restart |
| LEARN-03 policy | password/PIN/nontext/private/NO_PERSONALIZED_LEARNING零writes | actualmetadata policy＋tempfile/nativeobserver；EnglishUI非proof |
| LEARN-04 reset | native＋fallback＋futurestore全部重設，失敗不假成功 | R0真tempuserdict＋MainActivityreset，owner排他關閉 |
| LEARN-05 migration | boundedcount/decay、corrupt recovery、schema upgrades/removal | 可還原fixture、transaction crashcases、並行測試serial |
| TOUCH-01 spatial | 中央literal、遠距prior不override、低confidenceliteral | pureGeometry／TouchResolver fixtures，41keys各positive/negative |
| TOUCH-02 integration | tone/actions/Space/Enter/Backspace/mode/candidate不受correction | 真坐標＋eventidexactlyonce，TalkBacksemantics同target |
| TOUCH-03 consent | rawtrajectory預設不存、opt-inaggregatebounded/reset | storagewritespy，private/fullreset，需先approveddefaults |
| SYM-01 converter | malformedsurrogate拒絕，VS/ZWJ/RI完整，stableID／去重 | versionedcatalogfixtures；不用codepointcount當entrycount |
| SYM-02 search | 繁中/Englishkeywords/categorysearch一致、localeinheritance | CLDRpinnedsource，emoji-sequence另來源核對 |
| SYM-03 persistence/editor | favorites/recents重啟、敏感不寫、完整sequence插入 | tempstore＋真InputConnection，再實機字型；Backspacegrapheme政策待定 |
| EN-01 Kotlin/Compose/editor | hel→hello、Space一個separator、case/Backspace/punctuation | EnglishWordSession/EnglishTypingUiTest及device |
| DEV-01 physical | 候選drag-expand-page-select、長句/editor切換/rotation/no doublecommit | 真KU-Yin軟鍵＋controlledhost；固定serial、授權、restoreproof |
| REL-01 build/release | source/version/hash/signer、install-update不清資料、licenses | APKprovenance＋正常pm update；簽章錯誤停止、不uninstall |

## S0/S1 已執行的驗證路徑

新類別：LearningResetSafetyUiTest、CandidateBrowserStateTest、CandidateBrowserUiTest、CandidateBrowserLifecycleTest、NativeCandidatePagingTest；正式 NativeTypingEditorIntegrationTest 增第二頁選字／繼續輸入案例。

```sh
# 真 host JNI opt-in；沒有這兩個 env 時 native tests 可能 skip，不能宣稱全 PASS
KUYIN_NATIVE_HOST_DIR="$PWD/build/aie-s1-evidence/native" \
KUYIN_NATIVE_HOST_DATA='/private/tmp/kuyin-p0r-native-20261010/fcitx5-android-prebuilt/chewing-dict' \
./gradlew :app:testDebugUnitTest :acceptance-host:testDebugUnitTest --console=plain
python3 -m unittest discover -s scripts/tests -p 'test_p*.py'
python3 scripts/verify.py --static
```

S0-01～10 的 unsupported 分支限制見 S0/01-red-green.md；S1-01～24 對照見 `../review/aie-s1-candidate-browser/02-red-green-evidence.md`。Compose touch 是實際 pointer gesture，沒有以 hardware Enter 當 software callback 證據，但仍是 host framework 模擬。完整 native erase、TalkBack/OEM、裝置 install/update 都未驗證。

Release build 依既有缺 keystore 狀態誠實記錄。新 AIE APK 不能套用凍結 P1 v3 的 APK hash/舊 manifest；先依 AIE 手冊版本核對，不另造 runner。
