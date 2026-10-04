# P1-D 本機歷史恢復

日期：2026-10-04。Stage 0；不代表 P1-D 已完成。

## 來源與安全邊界

- 工作區：`/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1a-native-startup-hardening`。
- 初始 HEAD／REMOTE_C4_SHA：`3d86f7538c47dc929e25cdb0bd0755f8cc3bad60`；detached；cached diff 空。
- ancestry exit 0；cached origin/c4 與指定基線一致。未 fetch 或修改遠端。
- 已執行 rev-parse、branch、status、stat、完整 diff、cached diff、log -10、worktree list。
- A/B/C 所有 Markdown 完整讀取，JSON/XML 完整解析，B PNG 已檢視。A 的舊 XML 僅指向 build 路徑，已被新測試覆寫；不偽造封存。
- 依 AGENTS 順序讀權威文檔。ARCHITECTURE 的 decoder production/future 與測試數過時；以實際碼及 CURRENT_STATE 為當前事實。
- 使用者已提供執行步驟、決策表與 local commit 授權，START NOW 覆蓋技能的一般再次確認步驟；深讀與計畫仍先落檔。
- 不 push/main merge/改寫歷史，不刪其他 worktree，不碰 donor/native pins/ABI/app identity，不做 M4/UI redesign。

## 逐檔歸屬

`lineage-inventory.json` 是初始所有 changed/untracked 非 ignored 檔的 bytes/SHA256。`ignored-output-inventory.tsv` 逐檔列初始 ignored 檔，僅 metadata。local.properties 分類 UNRELATED 環境；.DS_Store 為 TEMP_EVIDENCE 保留不提交；build/.cxx/.gradle/native staged assets 為 BUILD_OUTPUT。

FILES_CLASSIFIED: 41；UNKNOWN_FILES: 0；IGNORED_FILES: 2712。

| 檔案 | 分類 | 歸屬證據 |
|---|---|---|
| `app/src/main/java/com/example/androidkeyboard/input/EditorPolicy.kt` | P1C_PRODUCT | A/B/C 00/06 與實際 diff |
| `app/src/main/java/com/example/ime/engine/ZhuyinConstants.kt` | P1B_PRODUCT | A/B/C 00/06 與實際 diff |
| `app/src/main/java/com/example/ime/service/EffectiveModePolicy.kt` | P1C_PRODUCT | A/B/C 00/06 與實際 diff |
| `app/src/main/java/com/example/ime/service/NativeChewingEngineOwner.kt` | P1A_PRODUCT | A/B/C 00/06 與實際 diff |
| `app/src/main/java/com/example/ime/service/NativeStartupGate.kt` | P1A_PRODUCT | A/B/C 00/06 與實際 diff |
| `app/src/main/java/com/example/ime/ui/DachenKeyboardRows.kt` | P1B_PRODUCT | A/B/C 00/06 與實際 diff |
| `app/src/main/java/com/example/ime/ui/KuYinKeyboardUi.kt` | P1B_PRODUCT | A/B/C 00/06 與實際 diff |
| `app/src/test/java/com/example/androidkeyboard/input/EditorPolicyTest.kt` | P1C_TEST | A/B/C 00/06 與實際 diff |
| `app/src/test/java/com/example/ime/NonTextEditorServiceTest.kt` | P1C_TEST | A/B/C 00/06 與實際 diff |
| `app/src/test/java/com/example/ime/service/EffectiveModePolicyTest.kt` | P1C_TEST | A/B/C 00/06 與實際 diff |
| `app/src/test/java/com/example/ime/service/NativeChewingEngineOwnerTest.kt` | P1A_TEST | A/B/C 00/06 與實際 diff |
| `app/src/test/java/com/example/ime/service/NativeLearningPolicyTest.kt` | P1C_TEST | A/B/C 00/06 與實際 diff |
| `app/src/test/java/com/example/ime/service/NativeStartupGateTest.kt` | P1A_TEST | A/B/C 00/06 與實際 diff |
| `app/src/test/java/com/example/ime/ui/DachenKeyboardLayoutTest.kt` | P1B_TEST | A/B/C 00/06 與實際 diff |
| `docs/CURRENT_STATE.md` | P1C_DOC | B/C 累積狀態，C 最終37/149，共同文檔提交 |
| `docs/review/2026-10-04-p1a/00-source-of-truth.md` | P1A_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1a/01-deep-understanding.md` | P1A_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1a/02-open-code-review.md` | P1A_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1a/03-p1a-design.md` | P1A_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1a/04-test-evidence.md` | P1A_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1a/05-independent-review.md` | P1A_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1a/06-handoff.md` | P1A_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1b/00-來源與範圍.md` | P1B_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1b/01-RED測試.xml` | P1B_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1b/02-GREEN測試.xml` | P1B_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1b/03-四排大千預覽.png` | P1B_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1b/04-驗證結果.json` | P1B_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1b/05-程式審查.md` | P1B_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1b/06-交接.md` | P1B_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/00-來源與範圍.md` | P1C_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/01-RED/TEST-com.example.androidkeyboard.input.EditorPolicyTest.xml` | P1C_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/01-RED/TEST-com.example.ime.NonTextEditorServiceTest.xml` | P1C_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/01-RED/TEST-com.example.ime.service.EffectiveModePolicyTest.xml` | P1C_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/01-RED/TEST-com.example.ime.service.NativeLearningPolicyTest.xml` | P1C_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/02-GREEN/TEST-com.example.androidkeyboard.input.EditorPolicyTest.xml` | P1C_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/02-GREEN/TEST-com.example.ime.NonTextEditorServiceTest.xml` | P1C_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/02-GREEN/TEST-com.example.ime.service.EffectiveModePolicyTest.xml` | P1C_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/02-GREEN/TEST-com.example.ime.service.NativeLearningPolicyTest.xml` | P1C_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/04-驗證結果.json` | P1C_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/05-程式審查.md` | P1C_DOC | A/B/C 00/06 與實際 diff |
| `docs/review/2026-10-04-p1c/06-交接.md` | P1C_DOC | A/B/C 00/06 與實際 diff |

## 重建策略

產品 hunks／測試檔互不交錯，可安全以 A 2產品+2測試、B 3產品+1測試、C 2產品+4測試提交。然後單獨提交既有文檔與 lineage；不人工重播、不修改作者或時間。

P1-A 歷史文檔宣稱新增5個 methods 有誤：Gate 3→6 (+3，success 只是改名強化)，Owner 3→4 (+1)，合計 +4；與129→133、focused10相符。此处追加更正，保留原時點材料。

## OCR 與工具限制

ocr --version exit0：v1.12.11 (a758d9cb)。本輪唯一 ocr llm test exit1：401 authentication_error invalid x-api-key。OPEN_CODE_REVIEW_STATUS=BLOCKED_BY_PROVIDER；SESSION=none；不重試／讀出／修改金鑰。

LSP diagnostics tool／CLI 不存在；以 compiler／必要 lint 提供替代證據，不聲稱已跑 LSP。

## 任務技能矩陣

已掃描 canonical catalog；Addy Osmani 24 個技能依 Android 行為範圍選用，無關 UI/影片/行銷不執行。此工具介面不提供主模型思考強度切換；深度工作以來源、RED/GREEN、獨立審查證明。

| 階段 | 路由技能（各10個） | 驗收 |
|---|---|---|
| 0 lineage | using-superpowers, deep-understanding, open-code-review, code-review-excellence, official-code-review, addyosmani-context-engineering, addyosmani-source-driven-development, addyosmani-git-workflow-and-versioning, addyosmani-planning-and-task-breakdown, verification-before-completion | 完整歸屬、唯讀獨立審查、本機 checkpoint fresh gates |
| 1 Enter | deep-understanding, systematic-debugging, writing-plans, test-driven-development, addyosmani-source-driven-development, addyosmani-api-and-interface-design, addyosmani-spec-driven-development, addyosmani-test-driven-development, addyosmani-incremental-implementation, addyosmani-code-simplification | 深讀、official contract、決策表、RED、最小修正 |
| 2 review | verification-before-completion, code-review-excellence, receiving-code-review, requesting-code-review, official-code-review, addyosmani-code-review-and-quality, addyosmani-api-and-interface-design, addyosmani-documentation-and-adrs, addyosmani-git-workflow-and-versioning, addyosmani-context-engineering | 新鮮 GREEN、獨立審查、local commits、handoff、STOP |

- [x] 實際 dirty tree、三個目錄深讀、逐檔分類（UNKNOWN=0）。
- [x] independent Stage0 review、A/B/C/local docs commits。
- [x] consolidated fresh gates、checkpoint SHA。
- [x] 隔離 P1-D worktree；Enter trace、official Android contract。
- [x] 決策表、TDD RED、minimal fix。
- [x] focused/full/contracts/bootstrap/Debug/instrumentation GREEN；Release既有keystore受阻。
- [x] independent final review APPROVE、local code commit；文件提交與handoff完成後STOP。

## 獨立初審補註

唯讀 p1abc_audit 指出 baseline 既有 Gate readiness-failure close 後，Owner 仍可能在 service.onDestroy 再呼叫 close；AndroidChewingEngine.close 可重入。不是 P1-A 新增回歸，不在歷史恢復階段修改。P1-A 的 close-once 只精確適用 init-throw 清理與 Owner 自己的重複 close，不擴張為所有 gate-validation+shutdown 情況。

## 本機產品提交

LOCAL_BRANCH: `fix/p1-series-correctness`

P1A_COMMIT: `a7a036dffd29ec942fc98f78f83cf8ce59d1e6d8`

P1B_COMMIT: `6af9476337c911557530e02f10a2d21fc6ca3a3b`

P1C_COMMIT: `b63fddc821ebb8dd28af309711d5da6dbb8a76c6`

獨立初審 p1abc_audit = APPROVE；0 未歸屬產品/測試，P1-C 與交接一致。保留其他 worktrees。

## Fresh consolidated checkpoint

P1_SERIES_BASE_SHA: `3d86f7538c47dc929e25cdb0bd0755f8cc3bad60`

P1_SERIES_FINAL_SHA / P1D_BASE_SHA: `870fc03cbfe8e5107b8ff8dc13478ac7754eec9f`

DOC_COMMIT: `870fc03cbfe8e5107b8ff8dc13478ac7754eec9f`

LOCAL_BRANCH: `fix/p1-series-correctness`

A/B/C 產品/測試 bytes 與提交前清冊完全相同；git status 空。focused41/41；禁用cache完整37類149測0failures/errors；contracts10/10；bootstrap exit0（sandbox DNS exit128不算產品失敗，核准網路執行成功）；assembleDebug exit0。詳見 checkpoint-evidence.json。

P1-D 工作區：`/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1d-enter-policy`；branch `fix/p1d-enter-policy`；初始 HEAD等於checkpoint，status乾淨。common repo指向主庫.git。local.properties與已驗證的ignored native inputs複製至隔離區，沒有複製build/test結果。

## 前次停止點（2026-10-04，PARTIAL；已於2026-10-05接續）

Stage0已完成；P1-D focused各gate通過，但第二輪 full40類190測中B UI5fail，尚未完成驗收或local code/docs commits。HEAD/branch ref仍870fc03cbfe8e5107b8ff8dc13478ac7754eec9f，pending3產品+4測試及review資料。命令工具EMFILE使後續診斷/Gradle/contracts/bootstrap/build/git status無法執行，不聲稱clean。

P1-D新增.kotlin/sessions/*.salive為BUILD_OUTPUT，不提交；待命令工具恢復後只清理本worktree生成檔。p1d-pending-source-inventory.json記錄7個待提交程式/測試bytes與SHA256。preserved-worktrees-content-check.json核對其餘5工作區既有dirty的20檔SHA均未變；未宣稱已重跑git status。所有其他worktree與donor未被本輪修改。

## 最終接續結果

命令工具恢復，重新git status/HEAD/branch並核對原7檔SHA相同。最小Robolectric snapshot案例RED/control後，只增測試fixture main-looper drain（newHarness與既有C test）。A–H全部fresh PASS，full40/190/0fail；Python10/10、bootstrap、Debug、instrumentationcompilePASS。Release缺既有keystore，runtime NOT_TESTED。兩個fresh-context獨立reviewer APPROVE。

P1D_CODE_COMMIT: 75b93a3b68867fd6ec4c6adadd3e279d71e98104

P1-D final-green review-manifest記錄8檔（3產品／4新增test-helper／1既有test cleanup）；initial lineage清冊仍是Stage0原時點41檔，不以P1-D bytes覆寫。沒有改A/B/C product semantics、decoder/native pins/ABI/appidentity/geometry。ARCHITECTURE只修正已證實的production/fallback描述、加入Enter seam與移除過時test數；CURRENT_STATE更新fresh結果及runtime分界。
