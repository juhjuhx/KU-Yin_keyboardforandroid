# R2 最終階段交接

2026-10-15。本輪已交付test-infrastructure修正、完整CI及獨立證據覆核；完整驗收裁定仍PARTIAL，PR10 HOLD。不得將工程切片完成解讀為P1／實機／production完成。

## 來源與範圍

| 用途 | 精確來源 |
|---|---|
| MAIN_SHA | 63c9e4ab8ad7e277013acb5d7cce99228ec1f827，未改 |
| PR10_SHA | ea5c2b5b3404508c8ab6b0a69642b3a54a8cb565，OPEN/HOLD |
| reviewed local Host | 4fc43ffe29a361ea7916abf0e319bdf42dea8eb6，已正常merge進R2，未merge main |
| R2_WORKTREE／branch | /Users/huang/KU-Yin_keyboardforandroid/.worktrees/r2-ime-visibility-runtime；fix/r2-ime-visibility-runtime |
| 程式checkpoint | 3895625df8a70647de599652b58c268a10b5613b |
| PR12已驗證程式／docs checkpoint HEAD | 03ac3264b4178afccb5781fac6445920bf41dc3a |
| normal CI actual synthetic merge | 89f98a93b66a630327415ece25c3f4a9cb8b0c52，parents=main＋03ac；tree4b71a4c9c574e4ee11b7ead1a800011e3492b9a6與03ac相同 |
| product source／release tag | 39acf96c6cfc058106315f3553e1c8b88438f4f7；v0.2.0-aie.1→2ce6810684fd65a65a3006a224dc786b1f3a5f62，皆不變 |

本交接後續doc-only commit與tested source分開；最終local／remote HEAD以git log、PR及外部receipt為準，不偽造自引用SHA。最終文件可正常push，但其自動CI未完成前不得冒稱已是新的runtime實測。沒有main merge、新release或簽章變更。

## ROOT_CAUSE／CONFIDENCE

- **HIGH，已直接重現的Host缺陷**：UiAutomator2.3 existing-map key.hashCode也refresh live node；QUERY_REGISTER可throw stale。真Activity destroyed-node regression38561946857有效RED，僅Host androidTest依賴2.4後same regression38563163760 GREEN，getter仍stale。原bounded read及candidate／preedit斷言保留。
- **HIGH，local test precondition**：same3fc run38563975932 request在windowFocus=false／IC0便show，平台not-served；其後焦點才到。debug33b3ac8 one-shot延至focus後post並重驗，8測GREEN。修後有限與正常App5原assert/recreate皆執行；首次IMM仍可false後才visible，不能把focus/IC等同remote session ready。
- **HIGH，觀測缺陷**：原五個LIVE_FAILURE_DUMP markers沒有raw txt，Runtime.exec不解`>`。3895625改API31+RW stdin＋exit0/nonempty驗證；5個JVM shell測試GREEN，真API33固定payload roundtrip已驗。一個25byte檔／一次typed事件的兩pull副本，不是兩獨立實驗；五個真失敗dump修後未驗。
- **UNRESOLVED，historical ea5完整因果**：該輪曾served但缺當刻focus/IC/IMMS全trace；不能把新not-served重現直接當舊輪完整因果，也不稱flaky。

## FILES_CHANGED／PRODUCT_CHANGED

PRODUCT_CHANGED=NO。Production Kotlin／JNI、Host main、native pins、applicationId／ABI、signing、DB schema不變。改動集中debug ImeHostActivity／診斷、App/Host測試及sharedTest、Host test-only UiAutomator、scripts/CI安全採證與文件。catalog parsed data除uiautomator2.3→2.4全部相同，KSP2.3.5未改。UI／decoder／ranking／learning行為未變。

## APP_TEST_RESULTS／HOST_TEST_RESULTS

正常run [38661678107](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/38661678107)，API33/default/x86_64/pixel7/user0：

- App：5PASS、0FAIL、0SKIP；原visibility／recreate assertions真執行，observer關閉，不用額外IMM checkFocus補實驗。
- Host：33JUnit＝24PASS、0FAIL、9SKIP。其中infra1PASS非matrix；32case envelopes＝23PASS、9INCONCLUSIVE。
- Protocol：5／33個完整start/end及exact identities，32個case owner對source，無duplicate／缺漏，terminal-1；script original_exit0/evidence_exit0。不能從exit0或experimental job非blocking推fullPASS。
- 逐案expected／actual／owner／scope在[HOST_CASE_DISPOSITION](R2_HOST_CASE_DISPOSITION.md)，原始XML／logcat／testlog與APK身份在外部normal-03ac326-runtime。

PREVIOUS_9_FAILURE_DISPOSITIONS：ZH01／02／04／05、EN02、D13／14七案真正PASS；B02／B03原可見candidate/text/commit/continue assertions完成，之後到既存native context／sentence semantics gap，完整仍INCONCLUSIVE，**不計FAIL→SKIP為修復**。

PREVIOUS_8_SKIP_DISPOSITIONS：ZH06轉PASS；B01、C01–C05、D17七案仍INCONCLUSIVE。B01缺第二寬度／方向；C01/02/03/05缺native learning observer；D17缺同步Binder false-return證明。C04真TYPE_NULL/nullIC後81個completed空tree，沒有可見soft-key policy證據，保留BLOCKED_SELECTOR；不強行製造IC／鍵盤。

## 其他fresh／historical gate

- local59b：App57classes282真執行PASS、0skip，含真hostJNI integration10；Host107與四assemble當時UP-TO-DATE，最新selfcheck真compile在55。JNI binary沿用R1 hash，非本輪重建。
- normal CI：App282＝272PASS＋10nativeSKIP；Host107真執行PASS。Python130／10contracts／同pinbootstrap／Debug buildPASS，source是03ac／validated89f merge。
- CI有既有`kspDebugUnitTestKotlin` AWT背景NullPointerException error annotation，後續BUILD SUCCESS／XML零failure；舊7f/9f log亦見相同字串，不能說無toolchain error或KSP pin漂移。本輪未修其根因。
- 55／59的native3FAIL保留：舊host詞庫路徑空、一次新cache過早啟動。57新獨立cache pin3587ba3及4詞庫／14publicfiles hash驗證後59b通過，未改assertions迎合GREEN。

## DATA_PRESERVATION

65-post-runtime-preservation.json重新核對18原工區HEAD/status/diff hashes全部一致，現共19；root userdirty／M4不動。保護source diff0；公開APK SHA acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634不變。未讀／複製／刪私人字典、DB、prefs、keys或私人輸入；device-local DB inspection NOT_TESTED。R1 archive1185／536materials的完整hash覆核沿用先前獨立收據，不冒稱65又重算全部archive。

## INDEPENDENT_REVIEW／PR10_VERDICT

source、focus／rawdump、有限runtime、文件與保存已有獨立窄批准；完整normal evidence審查已完成，外部「正常完整runtime與逐案契約獨立覆核.md」SHA a82e88604e049bae17b56ac98de104c3e23c7cc84244c6083033482ebd6cb403，獨立核原始33／32 protocol及每個gap，批准testinfra執行與已觀測契約，不能繼承source-only APPROVE。CI reviewer此次額度中斷，只有partial核對，沒有其完整APPROVE；KSP漂移疑點已git diff/tomllib與另一reviewer證偽。OCR v1.12.13單次401 BLOCKED_BY_PROVIDER；官方指定模型harness不可用，均無假批准。

PR10_VERDICT=HOLD；PR12保持Draft。未批准完整historical因果、full acceptance／physical或merge；不透過放寬assertions、延timeout、刪案例或新增未授權production observer消除gap。

## NEXT_ACTION

限定驗收／觀測缺口處置：B01方向與寬度、B02/B03 nativecontext與語意、C學習及TYPE_NULL、D17 Binder的scope需明確證據／紀錄決策；historical ea5 disposition仍須review。新native observer需另一個受限設計審查，不在此輪偷改產品。沒有授權實機session，不宣稱physical PASS。Release NOT_READY；M4 NOT_VALIDATED；AIE-S2 NOT_STARTED。

外部完整證據：`/Users/huang/Desktop/KU-Yin-R2-Verification-2026-10-13/`，機器入口RUNNING_CONTEXT_ANCHOR.json。此輪停止新增工程切片，不自動merge／release／M4。
