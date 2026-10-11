# KU-YIN AIE V1 S0/S1 IMPLEMENTATION HANDOFF

## SOURCE

REVIEW_BASE_SHA: `84dcf9a85d43b079a053b5b78e6fdcaa90a5bd2f`
P0_PRODUCT_SHA: `9f5db676a17b5770a738b5c20da937f9211e5a34`
S0_FINAL_SHA / S1_BASE_SHA: `ea5f1853b3d4aba9f6c0deb1fbcd0e092b1cdac8`
FINAL_SOURCE_SHA: `39acf96c6cfc058106315f3553e1c8b88438f4f7`
S0_BRANCH: `fix/aie-s0-learning-reset-safety`
S1_BRANCH: `feat/aie-s1-candidate-browser`
WORKTREE: `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/aie-s1-candidate-browser`
DOCUMENTATION_HEAD: 本檔後的本地 docs commit，以 `git rev-parse HEAD`／桌面交付 metadata 為準；不在 Git 檔案自填自引用 hash。
PUSHED / MERGED / RELEASED: **NO**

## S0 LEARNING RESET

ROOT_CAUSE: 原設定只清 fallback frequency prefs，native userdict 沒清，卻顯示成功。native context仍可能 checkpoint/Drop 寫回；沒有安全全 store erase/flush/rollback API契約。
BEHAVIOR: 停用完整reset、明示暫不支援／不刪資料，移除 fallback-only dialog/clear/成功通知。沒有刪native/fallback/settings，也未更動 learning policy。
TESTS: 正式設定UI 3測，原版有效 RED 2敗；S0 gate App50類232測，native/editor5案。最終全量仍包含3個reset案及敏感policy回歸。
SAFETY_VERDICT: **APPROVED_FOR_UNSUPPORTED_MITIGATION**；完整原生＋fallback erase **OPEN/UNSUPPORTED**，不能寫 native-reset PASS。

## S1 CANDIDATE BROWSER

NATIVE_PAGING: 沿用 P0 disposable scratch/replay，adapter每10plans一頁；只增純 metadata，不操作live context翻頁/refresh。plan pages 不代表unique候選總數。
CANDIDATE_SNAPSHOT: compositionGeneration＋snapshotGeneration＋provider/page＋opaque selection identity；session 校驗當前ref/page request，舊 tap／回同頁舊ref／duplicate paging拒絕。
COMPOSE_UI: 單列橫捲＋48dp展開；有界preedit header、完整文字語義；expanded adaptive grid＋前/後/頁碼，暫代 main key region，收合回原mode/大千幾何。
EXPAND_COLLAPSE: 零 native query／零commit；hide/editor/reset清展開與失效page，view-start才接受軟鍵。指標跨snapshot不選到新候選。
SELECTION: 原nativeidentity/fullbuffer驗證＋orderedCommits/InputConnection ACK單一路徑；第二頁候選→真host EditText一次→繼續輸入「我」。
RANKING / TOUCH / SYMBOL / PERSONAL_PREVIEW: **UNCHANGED**，沒有 S2 實作。

## FILES CHANGED

S0：MainActivity reset入口、LearningResetSafetyUiTest、S0安全/ADR文件。
S1 source commit20檔：app版本；ChewingEngine/AndroidChewingEngine純page metadata；ComposeSession/ChewingEngineSession/ZhuyinDictionarySession；KuYinKeyboardUi＋新CandidatePanel；service input-view gate；四個新state/UI/lifecycle/nativepaging tests；native editor新第二頁案例；原session tests補當前ref；EditorCommitRecovery fixture補完整view callback。原 assertions 保留。

S1為同一可用的薄垂直功能（ref契約＋正式UI＋tests）保留一個source commit，避免中間UI仍派送無ref而無法選字；S0和handoff/doc commit分開。沒有無關重構。全部12個原worktree HEAD/status與開始快照一致，AGENTS未改。

## RED EVIDENCE

browser8測6敗、snapshot8測6敗、hide2測1敗、pointer/layout/hide-action10測4敗；另真Nativegraphics舊56dp strip高度 overflow RED。命令、失敗名、font oracle精確修正與fixture callback校正見 [RED/GREEN](02-red-green-evidence.md)。compile/沙箱錯誤不冒充RED。

## GREEN EVIDENCE

| Gate | 本輪結果／原始 evidence |
|---|---|
| S1 focused | 5類36測0failure/error/skip、exit0；其後新增600dp/view-start案包含全量 |
| App JVM | **54類265測**，failure/error/skip=0，exit0；full-jvm-green.log/XML |
| Host JVM | **3類37測**，實際 `--rerun`，failure/error/skip=0，exit0 |
| Compose candidate | UI12＋lifecycle4；實際overflow pointer/按鍵callback/bounds，非device |
| Native | paging4/4、native/service/EditText6/6；重建pinned JNI/probe exit0，沒有skip |
| Python | 104測、exit0；包含fakeADB runner，沒有實機mutation |
| Static contracts | 10/10、exit0；static-final.log |
| Bootstrap | 固定source/prebuilt、exit0；bootstrap log |
| Debug | assembleDebug exit0；source commit後再驗證同APK hash |
| Instrumentation | app/host assembleDebugAndroidTest exit0，**COMPILED_NOT_RUN** |
| Release | exit1：既有my-upload-key.jks缺失；沒有新key／簽release，非PASS |

限定性能：macOS host nextPageUpdate，20樣本、S1 median22.375ms/p95 26.113ms；非Androidlatency／改善主張。[原始方法與限制](03-performance-baseline.md)。

## NEW APK

PATH: `/Users/huang/Desktop/KU-Yin-AIE-S1-2026-10-11/KU-Yin-AIE-S1-candidate-browser-debug.apk`
SHA256: `acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634`
BYTES: `30364775`
VERSION_CODE / NAME: `20003 / 0.2.0-aie.1`
APPLICATION_ID: `io.github.juhjuhx.kuyin`
SIGNER_SHA256: `46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94`
SOURCE_SHA: `39acf96c6cfc058106315f3553e1c8b88438f4f7`
ABI: armeabi-v7a、arm64-v8a、x86、x86_64；minSdk24/target36。
LIBCHEWING_PIN: source `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`；prebuilt `3587ba3355711f0aca50136e787719f6562676b8`。

APK內所有assets（native dictionaries、Englishlexicon及license/source）與P0 byte相同；JNI `.text/.rodata` 四ABI相同，整個JNI ELF hash因重新建置不同，不宣稱整檔一致。檔案size變小沒有移除entry/ABI/assets；兩APK都是966entries。詳細buildUTC、sources/assets hashes、簽章、命令/XML見桌面 `APK來源與驗證.json`、`evidence/`。

正常更新的static條件已驗證：相同applicationId/signer、versionCode高於20002。**實際安裝/更新 NOT_TESTED**；signature mismatch 時停止，不 uninstall/clear。原P0 APK hash14082…與frozen v2/v3 ZIP及4APK精確hash均未變。

## DEVICE

DEVICE: NONE（ADB inventory exit0，空列表）。
INSTALLED / CANDIDATE_PAGING / EDITOR_COMMIT: **DEVICE_NOT_TESTED**。
HOST_EDITOR_COMMIT: VERIFIED_IN_ROBOLECTRIC_WITH_REAL_JNI，不能代替裝置文字驗收。
PHYSICAL_ACCEPTANCE: OPEN；RELEASE: NOT_READY；M4: NOT_VALIDATED。
MANUAL: [手機復驗手冊](05-device-checklist.md)，桌面交付包也有副本。沒有裝置mutation／personal-learning deletion。

## REVIEW

OCR: v1.12.13，Anthropic401，**BLOCKED_BY_PROVIDER**；無OCR approval。
INDEPENDENT: 兩名S1reviewer **APPROVE**，綁定source39acf96；S0限定安全APPROVE；沒有未解High。[審查處置與限制](04-independent-review.md)。

## REMAINING / NEXT

- 使用者實機復驗新候選瀏覽、基本中文／連句、英文、editor switching、TalkBack/OEM、正常更新。
- 完整native/fallback reset、privacy/erase release gate仍OPEN；native preview仍不讀personal userdict，frequency策略未改。
- English既存跨prefix pointer風險保留於ledger；本片沒有聲稱修正全部英文snapshot gesture。
- 限定hostpage樣本之外的render/expand/selection latency未量測。
- **AIE-S2 Candidate Retrieval and Ranking** 是候選後續議題，**未批准、未開始**。先完成本片手機復驗，再另定其scope與replay criteria；沒有自動push/merge/release/M4。

FINAL_RULING: **S0安全mitigation＋S1 LOCAL_VERIFIED；READY_FOR_USER_DEVICE_RETEST**。
