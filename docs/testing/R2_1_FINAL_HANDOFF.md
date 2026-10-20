# KU-YIN R2.1 ACCEPTANCE EVIDENCE CLOSURE

2026-10-16；本輪只做九gap證據裁定與B01 test-only最小切片，不是產品功能或正式驗收。

## SOURCE

MAIN_SHA: `63c9e4ab8ad7e277013acb5d7cce99228ec1f827`
PR12_SHA: `d6bf2a68f68fe6ea0bd4bbbf79016a863abb29d1`（Draft，未改）
PR10_SHA: `ea5c2b5b3404508c8ab6b0a69642b3a54a8cb565`（HOLD，未改）
R2_1_SOURCE: `b1b5bf6b3939fef7e62893d833b0853e0a08ae7d`
BRANCH: `test/r2-1-acceptance-evidence`
WORKTREE: `.worktrees/r2-1-acceptance-evidence`

後續文件commit與tested source分開；最終docs HEAD由Git及外部R2_1_FINAL_RECEIPT.json核對，不偽造文件自引用SHA。七個test/tool檔正常push新分支，未改main/PR12/PR10，未開新平行產品線或新runner。

## FINAL_R2_CI

Exactd6兩個關聯workflow均completed success：Build38761725033、finiteprobe38761725004。actual synthetic merge `3b5e0845c0c83d145a26aa18803ee9e0dc98a04e`，parents main＋d6、tree與d6完全相同。

- BUILD：blocking Python130／10contracts／bootstrap／Debug與JVM通過；App282中272PASS10nativeSKIP、Host107PASS。既有KSP/AWT背景error annotation保留，非本輪toolchain修復。
- APP_RUNTIME：exact5／5PASS／0FAIL／0SKIP；原first/recreated visibility assertions，observer OFF。
- HOST_RUNTIME：exact33／24PASS／0FAIL／9SKIP；32case envelopes23PASS9INCONCLUSIVE＋infra1PASS；完整start/end/status2 owner／terminal核對，不從workflowgreen或console41/33推結果。
- finiteprobe：App5PASS／Host2PASS，183source hashes對d6、真first/recreate；一個canary／一次typed event的兩副本，非fullHost。

## NINE_GAPS

| case | 本輪裁定 |
|---|---|
| B01 | 新source/123unit/compile通過；有限attempt2 App前置FAIL，B01 NOT_TESTED／完整仍INCONCLUSIVE |
| B02 | INCONCLUSIVE：真candidate/commit/continue已觀察，live native context仍缺 |
| B03 | INCONCLUSIVE：真連句/候選/續輸入已觀察；context與correction/Enter完整語義仍缺 |
| C01 | INCONCLUSIVE：NUMBER UI/EditorInfo已觀察，真native learning未證 |
| C02 | INCONCLUSIVE：PHONE UI/EditorInfo已觀察，真native learning未證 |
| C03 | INCONCLUSIVE：DATETIME UI/EditorInfo已觀察，真native learning未證 |
| C04 | INCONCLUSIVE：真TYPE_NULL/nullIC保留，無可用softkey evidence，不推IMEcurrentIC或policy |
| C05 | INCONCLUSIVE：兩preferred返回TEXT可見restoration已觀察，native readback仍缺 |
| D17 | INCONCLUSIVE：callback false/SEND4恰1/零newline已證；API33 oneway同步false契約待owner裁定 |

完整12欄：[gap ledger](R2_1_ACCEPTANCE_GAP_LEDGER.md)；可見／完整結果分開：[case disposition](R2_1_CASE_DISPOSITION.md)。B01有限測試不能與舊23PASS拼成一次freshfullHost。

## B01 IMPLEMENTATION／TDD

保留原41鍵/ASCII、ㄕㄖstrictpreedit及clear零effect；新增Host Activity P/L/P請求並驗actual config/rotation/兩width、實際IMEwindow bounds、同completed capture41hitboxes與前後epoch、每frame立即驗幾何、固定我候選真tap/恰1commit、rotation零editor effects。setter只還原Host原requestedOrientation，不宣稱更改/復原systemrotation lock或實機旋轉。

source-only review指出並修正mode回退遮蔽、transition prefix漂移、mixed epoch、coverage/已知違約分類、phonetic精確性與candidate內容最小化。03b新oracle10測9有效assertionRED；04routing1有效RED→05focused14GREEN；10review16測9有效RED；11/12兩分類FAIL保留，改先驗frame後17真GREEN123／0／0／0。03sandbox socket阻擋與missing session不算RED；13只是cached recheck。

## FRESH_LOCAL_GATES

App57classes282PASS（native integration10真body、0skip）；Host9classes123PASS；Python131PASS；contracts10PASS；新public cache同pinbootstrap PASS；App/Host Debug與兩instrumentation assemble PASS。16包含App/allassemble，17 finalHost真unit/compile。JNI沿用R1 hash `a696f6402ed2b1044060ba46f487a1204ccda9e7666717acc651b5d6b622adab`，不是本輪重建。詳[validation](R2_1_VALIDATION_MATRIX.md)。

## B01 CI／ENVIRONMENT

run38825146067 exactb1b5bf6：attempt1在setup-android準備NDK28.2.13676358的ZipFileunknownarchive失敗，未到sourceguard/emulator/App/Host，secondary upload無檔。保留原FAIL，不判keyboardFAIL；更深包/下載原因UNRESOLVED。只一次同SHA/config/pin freshVM重跑；attempt2通SDK/sourceguard/KVM後App4PASS1firstVisibilityFAIL，Host/B01 NOT_TESTED。185source一致，5test protocol完整；first show前focus/IC1，timeout remote method/session null、五真failure dumps非空；根因UNRESOLVED。詳[App binding failure](R2_1_APP_BINDING_FAILURE.md)。未改pin、timeout、assertions或增加重試loop。

## PRESERVATION

PRODUCT_CHANGED: NO；NATIVE_CHANGED: NO；USER_DATA_CHANGED: NO。App main/debug/androidTest、Host main、build/catalog/native bootstrap/pins/signing/schema、frozen26 runner/manifest/source全diff0。公開AIE APK acdbacdf…、v2 ZIP787d44f…、v3 ZIP4dffdeb…皆fresh hash不變，未改/覆寫凍結資產。19原工區baseline保留，新增一隔離工區；最終再核外部收據，不讀真userdict/DB/prefs。原R1/R2證據保留。

## INDEPENDENT_REVIEW／PR_STRATEGY

[R2.1 review](R2_1_INDEPENDENT_REVIEW.md)記exact scope收據。限定R2d6 testinfra runtime gate PASS；新B01 source gate APPROVE只供有限CI；App前置實際FAIL，不能批准B01runtime或merge。NativeCase4 High是既有no-reset oracle不足，留ADR未結案；無未解High/Medium阻擋本B01source。PR12仍Draft／不批准fullacceptance或merge；PR10是已包含ancestor0/14，patch superseded，仍HOLD，不重複merge。

OCR1.13.0單次health被automatic approval review拒絕，未執行，狀態BLOCKED_BY_APPROVAL_REVIEW；歷史401非本輪provider結果。specified官方reviewpipeline未跑。

## NEXT_USER_DECISION／STOP

[Native observer ADR](R2_1_NATIVE_OBSERVABILITY_ADR.md)與[platform constraints](R2_1_ANDROID_PLATFORM_CONSTRAINTS.md)均PROPOSED。B02/B03/C需要明確批准極窄read-only debug/main seam後才能實作；不輸出pointer／詞彙、不讀真字典。下一個最小任務優先是App安裝／remote binding前提的限定調查，未自動實作。D17需明確決定可觀察原契約與不可能同步IPC研究scope的關係，不能自行刪gap。C04仍先保留真null，不強造IC。

PHYSICAL_ACCEPTANCE: NOT_TESTED（read-only inventory無裝置，未安裝/選IME）
PRODUCTION_READINESS: NOT_READY
M4: NOT_VALIDATED；AIE-S2: NOT_STARTED

FINAL_RULING: PARTIAL／BLOCKED_BY_APP_VISIBILITY。本輪B01source與九gap研究收束後STOP；無自動merge/release/productionobserver或架構擴張。
