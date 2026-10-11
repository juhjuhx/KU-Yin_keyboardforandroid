# AIE V1 Skill Routing Matrix

日期2026-10-11。catalog discovery是metadata盤點；LOADED不是已完成workflow、BLOCKED不是PASS。沒有宣稱執行全部24Addy。

## Catalog discovery

- `/Users/huang/.agents/skills`：344 entries；實體symlink以realpath識別，不重複算安裝。
- `/Users/huang/.claude/skills`：285 entries；實體symlink以realpath識別，不重複算安裝。
- `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills`：15 entries；實體symlink以realpath識別，不重複算安裝。
- `/Users/huang/.codex/skills`：255 entries；實體symlink以realpath識別，不重複算安裝。
- `/Users/huang/.config/opencode/skills`：312 entries；實體symlink以realpath識別，不重複算安裝。
- `/Users/huang/.opencode/skills`：30 entries；實體symlink以realpath識別，不重複算安裝。
- 復核更正：`/Users/huang/.opencode/skills` 實際存在；先前「不存在」的描述有誤，與上方30 entries一致。
- harness metadata提供Linear `list_agent_skills`；不是通用檔案skillloader，與KU-Yin未建立domain關聯，未呼叫。無通用MCP `skills.list/read` tool。
- tools：exec/read/rg/web、collaborationread-onlyagents；無LSP diagnostics工具，Markdown以links/JSON/static/diff驗證，不冒稱LSP PASS。

## 初始工程審查矩陣（84dcf9a時間點）

A=source/deepread；B=獨立audit；C=upstream/architectureoptions；D=decision/roadmap/docsverification。每節路由相關技能，沒有凑數。

| Skill | 實際路徑/provider | Source SHA256 | Phase | 用途 | loaded/result |
|---|---|---|---|---|---|
| superpowers:using-superpowers | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/using-superpowers/SKILL.md` | `82c5c8866ad7f5dd4440ce66bd7806ba48a2f13771beae5cf112e53f08fe36ba` | A/B/C/D | 啟動／遵守批准gate、tool適配 | YES / USED |
| brainstorming | `/Users/huang/.agents/skills/brainstorming/SKILL.md` | `a5f0aabeb53dcf8b0845c16f255b9cd72e75ed2ce7c176b0a03bbbd23808a18f` | A/D | 先讀碼、三種候選UX選項、批准前不實作 | YES / USED_SCOPED；文字功能決策，本輪不開visualcompanion |
| deep-understanding | `/Users/huang/.agents/skills/deep-understanding/SKILL.md` | `a83d28eb0274847dc6c3cd4f849f5aaf5bc01ce1ebfa885f9aa91834f37340b6` | A/B | 深讀實際pipeline並持久化報告；交由使用者review | YES / USED |
| systematic-debugging | `/Users/huang/.agents/skills/systematic-debugging/SKILL.md` | `85fe505cc2dfeeca5e5ebefd5dfc61132d9d4e1338684b7610d0ea83de7bf39a` | B | 分離paging/viewport/ranking根因，resetisolatedrepro | YES / USED_RESEARCH_ONLY |
| dispatching-parallel-agents | `/Users/huang/.agents/skills/dispatching-parallel-agents/SKILL.md` | `d42c5f61b077eea533768081450d27cf6c759f694e3adb1c6e25dae73bb3a0c1` | B | 三個互不寫檔的fresh-contextreadonlyworker | YES / USED；收到部分findings後usage-limit，無finalapprove |
| code-review-excellence | `/Users/huang/.agents/skills/code-review-skill-main/SKILL.md` | `0a17fe3afac4b34d823e582aee0bfebb6430246fa78d06704b1aa94eef6e4b35` | B/C | 確切source、嚴重度、testgap、避免nits | YES / USED |
| code-verification | `/Users/huang/.codex/plugins/cache/openai-curated-remote/codex-engineering-guardrails/1.1.1/skills/code-verification/SKILL.md` | `4aab85003f547ac6e1ee12e3ceb3f89d5744af56f3b22a3643e545007474fc87` | B/D | 需求到證據、比例checks、review不改product | YES / USED |
| requesting-code-review | `/Users/huang/.agents/skills/requesting-code-review/SKILL.md` | `5a3a44a3667800e2dc836829c6b92fada51e6dc58ac144ec05fe59f47d6bcd84` | B/D | 以真source與獨立scope派reviewer | YES / USED_PARTIAL_NO_FINAL_SIGNOFF |
| receiving-code-review | `/Users/huang/.agents/skills/receiving-code-review/SKILL.md` | `c9382e92b8f32363566068ecfed19d3b2651eaf40d3942b24840f839dedfc406` | B/D | 用source驗證回報，不盲照改 | YES / USED |
| verification-before-completion | `/Users/huang/.agents/skills/verification-before-completion/SKILL.md` | `ea52d15aabaf72bc6b558efe2c126f161b53961090ddcd712000273bfe8c7b6c` | D | hash、static、doclinks、scope和工區保護 | YES / USED |
| open-code-review | `/Users/huang/.agents/skills/open-code-review/SKILL.md` | `d7fc77214056c3232b825978272da6ac4874b7825c5ee8b39a98cad19763d4c3` | B | v1.12.13 providerhealth一次 | YES / BLOCKED_BY_PROVIDER_401 |
| grill-with-docs | `/Users/huang/.agents/skills/grill-with-docs/SKILL.md` | `3e35025119ba03efe7983dfdec3c8b96918142e3978512f3c2bf66d428310c7e` | D | 一群一題＋推薦值；ADR只記真批准 | YES / USED_AWAITING_ANSWER |
| addyosmani-context-engineering | `/Users/huang/.agents/skills/addyosmani-context-engineering/SKILL.md` | `1ddb30d5ff1f07b2056e76754b302d5a67cb33423bc698343bac0ceb5df212c3` | A/B/D | 隔離RoutePackets、canonicaldoc與stateanchors | YES / USED |
| addyosmani-source-driven-development | `/Users/huang/.agents/skills/addyosmani-source-driven-development/SKILL.md` | `43721639846981b90bda48b13c9bebf55a6713bf3fb0b29e4893e5a3fad44807` | A/C | 固定revision的官方source、filelicense與API | YES / USED_RESEARCH_ONLY |
| addyosmani-documentation-and-adrs | `/Users/huang/.agents/skills/addyosmani-documentation-and-adrs/SKILL.md` | `3e161ec612a3f216ff3c5cfd498779ab23bb0d760150c8105f018c3ed63a4552` | A/C/D | 提案≠accepted，歷史不刪，更新currenttruth | YES / USED |
| addyosmani-api-and-interface-design | `/Users/huang/.agents/skills/addyosmani-api-and-interface-design/SKILL.md` | `922c3d499252079f6f4f2ae9c932d8cd05993c9a72b465b892c59975e59b6c29` | C/D | CandidateRef／ACKownership與模組契約提案 | YES / USED_DESIGN_PROPOSAL_ONLY |
| addyosmani-security-and-hardening | `/Users/huang/.agents/skills/addyosmani-security-and-hardening/SKILL.md` | `966cda61759b22683e57216a435baf4f586f4af48787db4e47cc66ee8830d164` | B/C | sensitive/learning/reset/license/supplychain邊界 | YES / USED_REVIEW_ONLY |
| addyosmani-planning-and-task-breakdown | `/Users/huang/.agents/skills/addyosmani-planning-and-task-breakdown/SKILL.md` | `019f5ea92c7195090d449d24bbec7d7156220b064e8e584f060932dc8bf19218` | D | 垂直small slices、deps/entry/exit/rollback；不執行codeplan | YES / USED_PROVISIONAL_ROADMAP |
| visual-atelier v2 | `/Users/huang/.agents/skills/visual-atelier/SKILL.md` | `d9ffe1f0cf933b960d63105ccd5201edd2f37d87b59bdb0596948260b4f34a12` | C/D | 只檢視component一致/可操作；使用者保留視覺要求優先 | YES / LOADED_SCOPED_AUDIT；未產圖/新美術/改字體 |
| writing-plans | `/Users/huang/.agents/skills/writing-plans/SKILL.md` | `ff07e2f04c08f316b2f9823000ac8f95d5584a65229d00a1a6911ff3af6ed11c` | D/IMPLEMENTATION | 已讀流程；等acceptedS1spec再寫完整executableplan | YES / LOADED_DEFERRED |
| official-code-review | `/Users/huang/.agents/skills/official-code-review/SKILL.md` | `ae97f416c7e5ae708aa68a64a0cd137ff952c78ffd50f19ab147e19b2713adf3` | B | PR-specific；本輪無PR、無授權comment | YES / LOADED_NOT_APPLICABLE_NO_PR |

## 24 Addy entries：實際inventory

| Name | Canonical path | 本輪狀態 |
|---|---|---|
| addyosmani-api-and-interface-design | `/Users/huang/.agents/skills/addyosmani-api-and-interface-design/SKILL.md` | USED_DESIGN_PROPOSAL_ONLY |
| addyosmani-browser-testing-with-devtools | `/Users/huang/.agents/skills/addyosmani-browser-testing-with-devtools/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-ci-cd-and-automation | `/Users/huang/.agents/skills/addyosmani-ci-cd-and-automation/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-code-review-and-quality | `/Users/huang/.agents/skills/addyosmani-code-review-and-quality/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-code-simplification | `/Users/huang/.agents/skills/addyosmani-code-simplification/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-context-engineering | `/Users/huang/.agents/skills/addyosmani-context-engineering/SKILL.md` | USED |
| addyosmani-debugging-and-error-recovery | `/Users/huang/.agents/skills/addyosmani-debugging-and-error-recovery/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-deprecation-and-migration | `/Users/huang/.agents/skills/addyosmani-deprecation-and-migration/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-documentation-and-adrs | `/Users/huang/.agents/skills/addyosmani-documentation-and-adrs/SKILL.md` | USED |
| addyosmani-doubt-driven-development | `/Users/huang/.agents/skills/addyosmani-doubt-driven-development/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-frontend-ui-engineering | `/Users/huang/.agents/skills/addyosmani-frontend-ui-engineering/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-git-workflow-and-versioning | `/Users/huang/.agents/skills/addyosmani-git-workflow-and-versioning/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-idea-refine | `/Users/huang/.agents/skills/addyosmani-idea-refine/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-incremental-implementation | `/Users/huang/.agents/skills/addyosmani-incremental-implementation/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-interview-me | `/Users/huang/.agents/skills/addyosmani-interview-me/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-observability-and-instrumentation | `/Users/huang/.agents/skills/addyosmani-observability-and-instrumentation/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-performance-optimization | `/Users/huang/.agents/skills/addyosmani-performance-optimization/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-planning-and-task-breakdown | `/Users/huang/.agents/skills/addyosmani-planning-and-task-breakdown/SKILL.md` | USED_PROVISIONAL_ROADMAP |
| addyosmani-security-and-hardening | `/Users/huang/.agents/skills/addyosmani-security-and-hardening/SKILL.md` | USED_REVIEW_ONLY |
| addyosmani-shipping-and-launch | `/Users/huang/.agents/skills/addyosmani-shipping-and-launch/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-source-driven-development | `/Users/huang/.agents/skills/addyosmani-source-driven-development/SKILL.md` | USED_RESEARCH_ONLY |
| addyosmani-spec-driven-development | `/Users/huang/.agents/skills/addyosmani-spec-driven-development/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-test-driven-development | `/Users/huang/.agents/skills/addyosmani-test-driven-development/SKILL.md` | DISCOVERED_NOT_INVOKED |
| addyosmani-using-agent-skills | `/Users/huang/.agents/skills/addyosmani-using-agent-skills/SKILL.md` | DISCOVERED_NOT_INVOKED |

## 後續相關技能（不宣稱已執行）

`test-driven-development`、`code-work`、Addy TDD/incremental/performance/observability/codequality/CI/shipping均installed；批准進入對應phase時才載入。`grill-me`與`plan-interrogate`installed但本輪選grill-with-docs，不重複問同群。ui-ux-pro-max/gpt-taste/stitch/design-md已metadata發現，前者可在批准nativeUXspec後用；web展示字體/GSAP/Stitch均非本輪deliverable，不load。

具體phase route：A使用using/brain/deep/context/source/doc/verif（7）；B using/deep/debug/parallel/review/request/receive/verif/OCR/security（10）；C using/source/doc/api/security/review/visual/deep（8）；D using/brain/grill/doc/api/planning/context/verif/verification（9）。同一技能不需反覆讀檔；必要source引用見報告。

## S0/S1 本輪路由（實際載入／使用結果）

A=來源/審查/隔離設計；B=S0安全TDD；C=S1/native/ComposeTDD；D=獨立審查/驗證/收束。每phase選相关subset，不以 quota 載入無關技能。catalog 24 Addy 是盤點；本片新增實際執行 quality/simplification，其餘並未因此宣稱全部24已執行。最高可用思考由本harness決定，不偽稱切換未提供的模型設定。

| Skill | Actual path/provider | SHA256 | Phase / reason | Result |
|---|---|---|---|---|
| using-superpowers | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/using-superpowers/SKILL.md` | `82c5c8866ad7f5dd4440ce66bd7806ba48a2f13771beae5cf112e53f08fe36ba` | A/B/C/D / route | USED |
| deep-understanding | `/Users/huang/.agents/skills/deep-understanding/SKILL.md` | `a83d28eb0274847dc6c3cd4f849f5aaf5bc01ce1ebfa885f9aa91834f37340b6` | A / actualnative/reset/session/UItrace | USED |
| systematic-debugging | `/Users/huang/.agents/skills/systematic-debugging/SKILL.md` | `85fe505cc2dfeeca5e5ebefd5dfc61132d9d4e1338684b7610d0ea83de7bf39a` | A/B/C / separaterootcauses,bytecodeoracle | USED |
| writing-plans | `/Users/huang/.agents/skills/writing-plans/SKILL.md` | `ff07e2f04c08f316b2f9823000ac8f95d5584a65229d00a1a6911ff3af6ed11c` | A/B / S0/S1PLAN/entrygate | USED |
| using-git-worktrees | `/Users/huang/.agents/skills/using-git-worktrees/SKILL.md` | `085a45ee3de432bdb2768011591d9a882cb6c759e2317f379226451c5618fe8e` | A/B / twoisolatedlineages | USED |
| test-driven-development | `/Users/huang/.agents/skills/test-driven-development/SKILL.md` | `7dee67b4af6bdccc7a914ca34533184d64592d0f5b23aeae631538168db14994` | B/C / behaviorREDbeforeproduct | USED |
| executing-plans | `/Users/huang/.agents/skills/executing-plans/SKILL.md` | `ae12b8581157d44c6c1544995dd90111ae5e7669022bd75e8f715fb95f373b8b` | B/C / approvedinlineplan | USED_SCOPED |
| code-work | `/Users/huang/.codex/plugins/cache/openai-curated-remote/codex-engineering-guardrails/1.1.1/skills/code-work/SKILL.md` | `a3727efa825ff26a2ce67ff60379ad0e3a4bfcfc498c3632514fd662e7a0f3a6` | B/C / boundedproductiondiff | USED |
| code-verification | `/Users/huang/.codex/plugins/cache/openai-curated-remote/codex-engineering-guardrails/1.1.1/skills/code-verification/SKILL.md` | `4aab85003f547ac6e1ee12e3ceb3f89d5744af56f3b22a3643e545007474fc87` | A/D / proportionalfreshchecks | USED |
| dispatching-parallel-agents | `/Users/huang/.agents/skills/dispatching-parallel-agents/SKILL.md` | `d42c5f61b077eea533768081450d27cf6c759f694e3adb1c6e25dae73bb3a0c1` | A/D / readonlyindependentreview | USED |
| code-review-excellence | `/Users/huang/.agents/skills/code-review-skill-main/SKILL.md` | `0a17fe3afac4b34d823e582aee0bfebb6430246fa78d06704b1aa94eef6e4b35` | A/D / severityactualsource | USED |
| requesting-code-review | `/Users/huang/.agents/skills/requesting-code-review/SKILL.md` | `5a3a44a3667800e2dc836829c6b92fada51e6dc58ac144ec05fe59f47d6bcd84` | A/D / scope/source/testreview | USED |
| receiving-code-review | `/Users/huang/.agents/skills/receiving-code-review/SKILL.md` | `c9382e92b8f32363566068ecfed19d3b2651eaf40d3942b24840f839dedfc406` | B/C/D / verifyfindings/RED/corrections | USED |
| verification-before-completion | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/verification-before-completion/SKILL.md` | `2befe7fc55bcadaa3d97dd9e8efeb633d2561c0ebe74c5a8b17c4d9e7e4520b3` | B/C/D / XML/build/hash/counts | USED |
| addyosmani-code-review-and-quality | `/Users/huang/.agents/skills/addyosmani-code-review-and-quality/SKILL.md` | `93e60cde419b1e18bc9055e8a9fd76a5aeb3836806b3407e5a4802459c4314fb` | C/D / correctness/security/architecturereview | USED |
| addyosmani-code-simplification | `/Users/huang/.agents/skills/addyosmani-code-simplification/SKILL.md` | `5917c8224f345d2ba2c28ab7d084a738fcc29c628b9eaccf83c8ba85d02cf4fd` | C/D / CandidatePanelpureboundary,nounrelatedrefactor | USED_SCOPED |
| visual-atelier v2 | `/Users/huang/.agents/skills/visual-atelier/SKILL.md` | `d9ffe1f0cf933b960d63105ccd5201edd2f37d87b59bdb0596948260b4f34a12` | A/C / theme/font/geometryconsistencyonly | USED_SCOPED_NO_REDESIGN |
| open-code-review | `/Users/huang/.agents/skills/open-code-review/SKILL.md` | `d7fc77214056c3232b825978272da6ac4874b7825c5ee8b39a98cad19763d4c3` | A/D / singleproviderhealth | BLOCKED_BY_PROVIDER_401 |
| official-code-review | `/Users/huang/.agents/skills/official-code-review/SKILL.md` | `ae97f416c7e5ae708aa68a64a0cd137ff952c78ffd50f19ab147e19b2713adf3` | A/D / PRspecifictoolnotapplicable | LOADED_NOT_APPLICABLE_NO_PR |
| brainstorming | `/Users/huang/.agents/skills/brainstorming/SKILL.md` | `a5f0aabeb53dcf8b0845c16f255b9cd72e75ed2ce7c176b0a03bbbd23808a18f` | A / approvedOption1noprepeatquestion | USED_SCOPED_ALREADY_APPROVED |
| finishing-a-development-branch | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/finishing-a-development-branch/SKILL.md` | `8db5a922b242dd4e1bf824cb91c13b3e8d8e8a86d6ceaf7f0774eb9cce909d65` | D / useralreadydirectedlocalkeep/nopush | USED_KEEP_LOCAL |

S0 reviewer確認unsupported mitigation、S1兩reviewer簽核source `39acf96c6cfc058106315f3553e1c8b88438f4f7`；沒有physical PASS。Visual-atelier保留原美術，沒有新art/fonts/theme。Official-code-review沒有PR/comment；OCR沒有approval。finishing skill的generic選項已由使用者本prompt明確localcommits/no push/no merge決定，保留branches/worktrees，無需重問。LSP diagnostics工具未提供，使用Kotlin/JNIcompiler/測試/JSON/Markdownlinks/gitdiffcheck，沒有假稱LSP PASS。
