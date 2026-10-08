# P1-E 來源、範圍與缺口

BASE_SHA: 5c6bcad34c4048e40ce4e871e70e176c10195bcf
BRANCH: test/p1-alpha-acceptance-fixture
WORKTREE: /Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1-alpha-acceptance-fixture
INITIAL_STATUS: CLEAN
REMOTE_C4: cached 3d86f7538c47dc929e25cdb0bd0755f8cc3bad60

已完整讀 current Alpha 00–06、P1-A/B/C/D handoffs與existingdebughost／4類5方法instrumentation／runtime_smoke.sh。A/B/C handoffs為當時未提交歷史，最新lineage由P1-D/Alpha本機commits覆蓋；不倒退回detached dirty state。v1三個binary的size/hash已先核對並保存在lineage.json，不覆寫。

使用者P1-E prompt 是既已核定的架構／執行規格：獨立 acceptance-host module、primitive Intent config、final EditorInfo oracle、real InputConnection wrapper、UIAutomator software-key evidence。Brainstorming 的 architectural path只補具體責任邊界，不重問已決定的方案；writing-plans採本輪指定04位置；執行不等待裝置或再次授權local commits。前段泛稱GitHub推送由後段DO NOT PUSH明確限定，本輪只本機。

## 證據層

P1-A/B/C/D目前產品碼已本機verified；full40/190/0是歷史，P1-E會fresh再跑。既有instrumentation4類5測只package未device執行，不能證30-case。外部host／action counter缺口由本slice補；nativelearningobserver／安全startupfaultinjection／physical acceptance仍OPEN。M4不開始。

## Skills路由（各階段採用的相關技能，7–15 個）

| Phase | Skills | 產出 |
| --- | --- | --- |
| A source/research | superpowers:using-superpowers, deep-understanding, open-code-review, code-verification, code-review-excellence, official-code-review, requesting-code-review, receiving-code-review, addyosmani-context-engineering, addyosmani-source-driven-development, addyosmani-documentation-and-adrs | source/coverage deep-read、一次OCR health、upstream primary references |
| B design | superpowers:brainstorming, writing-plans, deep-understanding, addyosmani-api-and-interface-design, addyosmani-planning-and-task-breakdown, addyosmani-spec-driven-development, addyosmani-test-driven-development, addyosmani-incremental-implementation | immutable primitive contracts、bounded implementation plan |
| C implementation | test-driven-development, executing-plans, code-work, systematic-debugging, addyosmani-test-driven-development, addyosmani-incremental-implementation, addyosmani-code-simplification, addyosmani-code-review-and-quality, verification-before-completion | pure JVM RED/GREEN、real editor wrapper、serial build integration |
| D final | code-verification, verification-before-completion, requesting-code-review, receiving-code-review, open-code-review, code-review-excellence, addyosmani-code-review-and-quality, addyosmani-ci-cd-and-automation, addyosmani-shipping-and-launch, addyosmani-documentation-and-adrs, finishing-a-development-branch | fresh gates、independent review、v2包、local commits、STOP |

技能catalog掃描canonical ~/.agents/skills（640個SKILL.md）及 ~/.codex skills/plugin。未安裝／不適用的skill不假稱使用；不引入UI藝術／行銷／3D／benchmark skills。官方PR review只套高訊號本機review，沒有PR/外部留言；模型別名與工具以實際可用allowlist為準。

## OCR

ocr binary /opt/homebrew/bin/ocr，version v1.12.12 (182898c)。唯一 provider health `/opt/homebrew/bin/ocr llm test` exit1，provider anthropic，HTTP401 invalid x-api-key；Request-ID存ocr-health.log；session none。OPEN_CODE_REVIEW_STATUS=BLOCKED_BY_PROVIDER。不改／讀出credentials、不再重試相同auth；持續local independent review。

## Tasks

- [x] source/lineage/v1核對、isolation、技能catalog與一次OCR health。
- [x] 深讀host/oracle/binder/selector差距；設計責任分界。
- [x] pure fixture tests RED（實際assertion失敗）、GREEN。
- [x] final metadata／RecordingEditText／recording wrapper與diagnostic IDs。
- [x] D01–D18、C01–C05、B/A prepared UIAutomator surfaces。
- [x] fresh host/app gates與source/v1 preservation。
- [x] independent review/fixes。
- [x] v2 fixture package／local commits／handoff／STOP。
