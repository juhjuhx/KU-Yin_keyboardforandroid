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

## 2026-10-12：GitHub integration / experimental Alpha

本階段採使用者准許的較省資源路由；沒有硬湊15–20個skills、重新跑未修改產品的全部Gradle，也未執行全部24個Addy。實際catalog盤點640個nested SKILL.md，未去重，不宣稱670。

| Skill | 實際路徑 | SHA256 | 用途／狀態 |
|---|---|---|---|
| using-superpowers | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/using-superpowers/SKILL.md` | `82c5c8866ad7f5dd4440ce66bd7806ba48a2f13771beae5cf112e53f08fe36ba` | 路由與已授權工作 / LOADED_USED_SCOPED |
| context-optimizer | `/Users/huang/.agents/skills/pro-context-optimizer/SKILL.md` | `4d974efb9e780e47eae08a6df69d2b5c5662c1b25b71935c953092bb034b69d8` | 短入口與持久化TODO / LOADED_USED_SCOPED |
| addyosmani-planning-and-task-breakdown | `/Users/huang/.agents/skills/addyosmani-planning-and-task-breakdown/SKILL.md` | `019f5ea92c7195090d449d24bbec7d7156220b064e8e584f060932dc8bf19218` | 依賴排序 / LOADED_USED_SCOPED |
| addyosmani-source-driven-development | `/Users/huang/.agents/skills/addyosmani-source-driven-development/SKILL.md` | `43721639846981b90bda48b13c9bebf55a6713bf3fb0b29e4893e5a3fad44807` | 固定原生與詞庫來源 / LOADED_USED_SCOPED |
| systematic-debugging | `/Users/huang/.agents/skills/systematic-debugging/SKILL.md` | `85fe505cc2dfeeca5e5ebefd5dfc61132d9d4e1338684b7610d0ea83de7bf39a` | CI first failing boundary / LOADED_USED_SCOPED |
| test-driven-development | `/Users/huang/.agents/skills/test-driven-development/SKILL.md` | `7dee67b4af6bdccc7a914ca34533184d64592d0f5b23aeae631538168db14994` | fake工具RED/GREEN / LOADED_USED_SCOPED |
| addyosmani-ci-cd-and-automation | `/Users/huang/.agents/skills/addyosmani-ci-cd-and-automation/SKILL.md` | `067d15958ad69341fe37600be810e7a19f884fa67045385e48fd41c25dbd8e7d` | 兩層IMEprecondition/手動發布 / LOADED_USED_SCOPED |
| addyosmani-shipping-and-launch | `/Users/huang/.agents/skills/addyosmani-shipping-and-launch/SKILL.md` | `9ef215b2c69d03b29dd9c804af51f57a8d7294da0e9a1097980805300c76de34` | 實驗性發布範圍與限制 / LOADED_USED_SCOPED |
| verification-before-completion | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/verification-before-completion/SKILL.md` | `2befe7fc55bcadaa3d97dd9e8efeb633d2561c0ebe74c5a8b17c4d9e7e4520b3` | 實測hash/CI/重鏈結 / LOADED_USED_SCOPED |
| finishing-a-development-branch | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/finishing-a-development-branch/SKILL.md` | `8db5a922b242dd4e1bf824cb91c13b3e8d8e8a86d6ceaf7f0774eb9cce909d65` | 沿用已批准push/merge/release，保留工區 / LOADED_USED_SCOPED |

平台未提供 `/init`／`/compact` 或 LSP diagnostics 呼叫；持久化入口替代手動reset，使用編譯／合約／git diff／JSON驗證，不聲稱虛構工具執行。OCR沿用最近401證據，未重試。獨立review另外記錄實際結果。

## R1 Mainline Consolidation（2026-10-12）

Canonicalcatalog640 nested SKILL.md，Superpowers6.4.2有15，Addy24entries可用；未把發現等同使用、未硬湊技能數。Tools: functions exec/patch、scoped filesystem/Git/gh、3獨立readers；無LSP diagnostics、/init或/compact API，使用compiler/checks/持久anchor，不改globalconfig。Apps MCP可見metadata；沒有通用MCPskillloader，不讀credentials或發送未授權訊息。OCR v1.12.13 (fabbdb29)，llm test exit1 / Anthropic HTTP401 invalid x-api-key / request req_011CfwdzDtFeoobZcX43CyS1；不重試。

| skill_name | provider / actualpath | source SHA256 | phase | reason / result | status |
|---|---|---|---|---|---|
| using-superpowers | local Skill / `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/using-superpowers/SKILL.md` | `82c5c8866ad7f5dd4440ce66bd7806ba48a2f13771beae5cf112e53f08fe36ba` | A–H | 路由與權限 | LOADED / USED_SCOPED |
| deep-understanding | local Skill / `/Users/huang/.agents/skills/deep-understanding/SKILL.md` | `a83d28eb0274847dc6c3cd4f849f5aaf5bc01ce1ebfa885f9aa91834f37340b6` | A/D | source深讀與owner map | LOADED / USED_SCOPED |
| addyosmani-source-driven-development | local Skill / `/Users/huang/.agents/skills/addyosmani-source-driven-development/SKILL.md` | `43721639846981b90bda48b13c9bebf55a6713bf3fb0b29e4893e5a3fad44807` | D | 根據exactsource與官方contract | LOADED / USED_SCOPED |
| addyosmani-context-engineering | local Skill / `/Users/huang/.agents/skills/addyosmani-context-engineering/SKILL.md` | `1ddb30d5ff1f07b2056e76754b302d5a67cb33423bc698343bac0ceb5df212c3` | A/E | 短入口及canonicalauthority | LOADED / USED_SCOPED |
| addyosmani-documentation-and-adrs | local Skill / `/Users/huang/.agents/skills/addyosmani-documentation-and-adrs/SKILL.md` | `3e161ec612a3f216ff3c5cfd498779ab23bb0d760150c8105f018c3ed63a4552` | D/E | 實作與proposal/歷史區分 | LOADED / USED_SCOPED |
| code-review-excellence | local Skill / `/Users/huang/.agents/skills/code-review-excellence/SKILL.md` | `68df968e01fc7cc234f68ac9439a64e2f70b6c2dc02ad7bdb2928a61058ed8e8` | A/H | 獨立source/qualityreview | LOADED / USED_SCOPED |
| using-git-worktrees | local Skill / `/Users/huang/.agents/skills/using-git-worktrees/SKILL.md` | `085a45ee3de432bdb2768011591d9a882cb6c759e2317f379226451c5618fe8e` | C/G | 兩個已授權隔離工區 | LOADED / USED_SCOPED |
| writing-plans | local Skill / `/Users/huang/.agents/skills/writing-plans/SKILL.md` | `ff07e2f04c08f316b2f9823000ac8f95d5584a65229d00a1a6911ff3af6ed11c` | A–H | 依賴排序與R1_PLAN | LOADED / USED_SCOPED |
| requesting-code-review | local Skill / `/Users/huang/.agents/skills/requesting-code-review/SKILL.md` | `5a3a44a3667800e2dc836829c6b92fada51e6dc58ac144ec05fe59f47d6bcd84` | A/H | 分開fresh-contextreaders | LOADED / USED_SCOPED |
| receiving-code-review | local Skill / `/Users/huang/.agents/skills/receiving-code-review/SKILL.md` | `c9382e92b8f32363566068ecfed19d3b2651eaf40d3942b24840f839dedfc406` | H | 按source驗證/修正實質findings | LOADED / USED_SCOPED |
| code-verification | local Skill / `/Users/huang/.codex/plugins/cache/openai-curated-remote/codex-engineering-guardrails/1.1.1/skills/code-verification/SKILL.md` | `4aab85003f547ac6e1ee12e3ceb3f89d5744af56f3b22a3643e545007474fc87` | H | fresh/historical及traceablegates | LOADED / USED_SCOPED |
| addyosmani-git-workflow-and-versioning | local Skill / `/Users/huang/.agents/skills/addyosmani-git-workflow-and-versioning/SKILL.md` | `0631680069c5aa1a5ac327c01fc37b53013b23fd56b80bf30481097839d6444e` | B/C/H | atomic commits/normalmerge；禁reset條款以user優先 | LOADED / USED_SCOPED |
| addyosmani-deprecation-and-migration | local Skill / `/Users/huang/.agents/skills/addyosmani-deprecation-and-migration/SKILL.md` | `5467c9d11ec40603def930d2ade08cd2dd71e45ead9671b408ccfd94e230e3ea` | E/F | 歷史分類與advisoryKEEP，不自動delete | LOADED / USED_SCOPED |
| open-code-review | local Skill / `/Users/huang/.agents/skills/open-code-review/SKILL.md` | `d7fc77214056c3232b825978272da6ac4874b7825c5ee8b39a98cad19763d4c3` | A/H | providerhealthonce | LOADED / BLOCKED_BY_PROVIDER_401 |
| systematic-debugging | local Skill / `/Users/huang/.agents/skills/systematic-debugging/SKILL.md` | `85fe505cc2dfeeca5e5ebefd5dfc61132d9d4e1338684b7610d0ea83de7bf39a` | G | doctor/fixture根因分開 | LOADED / USED_SCOPED |
| test-driven-development | local Skill / `/Users/huang/.agents/skills/test-driven-development/SKILL.md` | `7dee67b4af6bdccc7a914ca34533184d64592d0f5b23aeae631538168db14994` | G | doctor有效RED2fail→GREEN | LOADED / USED_SCOPED |
| executing-plans | local Skill / `/Users/huang/.agents/skills/executing-plans/SKILL.md` | `ae12b8581157d44c6c1544995dd90111ae5e7669022bd75e8f715fb95f373b8b` | G/H | 已批准prompt按序執行，inline與獨立review | LOADED / USED_SCOPED |
| verification-before-completion | local Skill / `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/verification-before-completion/SKILL.md` | `2befe7fc55bcadaa3d97dd9e8efeb633d2561c0ebe74c5a8b17c4d9e7e4520b3` | H | 不聲稱未跑gates | LOADED / USED_SCOPED |

brainstorming/UI/visual/marketing/Three.js：NOT_APPLICABLE，user已批准收束非newdesign。official-code-review已LOADED；其特定模型四管線不支援，FULL_WORKFLOW=BLOCKED_BY_HARNESS，改採可用 fresh-context reviewers，不宣稱完成該管線。Addy其餘未load條目DISCOVERED_NOT_USED，不宣稱全24執行。Independent reviewers：r1_architecture_audit、r1_storage_build_audit、r1_fixture_audit（後者實作時不自評，final交另reviewer）。

R1 H新增實際載入/使用 addyosmani-ci-cd-and-automation (`/Users/huang/.agents/skills/addyosmani-ci-cd-and-automation/SKILL.md`)：將全Python/10contracts進入blockingCI，保留runtimeXML；不改prerelease/signature流程。

R1 finishing-a-development-branch已loaded/USED_SCOPED：用户已明确normal PR/merge，無需重問generic menu；禁止cleanup/prune以本輪user安全規則為優先。official-code-review已loaded，原文Haiku/Sonnet/Opus模型及4parallel pipeline不適用當前harness，未偽稱全workflow執行；本輪用可用fresh-contextreviewers＋source/gate驗證，無對外comment。

## R1 最終路由補登

完整離線 catalog：canonical 640＋Superpowers 15＋guardrails 2＝657 entries；其中 Addy 24。可呼叫 nested tool metadata 596（含不相關 apps）與 direct tools 已盤點；只使用任務相關 exec/patch/Git/gh/clock/independent collaboration，官方 UIAutomator 文件另以 web 唯讀核對。没有 init/compact/LSP API，無修改 global agent config。

| skill_name | provider / actualpath | SHA256 | phase | reason / result | status |
|---|---|---|---|---|---|
| addyosmani-ci-cd-and-automation | local Skill / `/Users/huang/.agents/skills/addyosmani-ci-cd-and-automation/SKILL.md` | `067d15958ad69341fe37600be810e7a19f884fa67045385e48fd41c25dbd8e7d` | H | blocking Python/10 contracts；保存 XML；不改 signing | LOADED / USED_SCOPED |
| finishing-a-development-branch | local Skill / `/Users/huang/.agents/skills/finishing-a-development-branch/SKILL.md` | `5c8d4b59aedb14c94e2f5d787a3265e858e8f53d4ceffe7ff1c15878a52b0e91` | H | 依已批准 normal PR/merge；保留 branches/worktrees | LOADED / USED_SCOPED |
| official-code-review | local Skill / `/Users/huang/.agents/skills/official-code-review/SKILL.md` | `ae97f416c7e5ae708aa68a64a0cd137ff952c78ffd50f19ab147e19b2713adf3` | H | 已讀；指定模型流程不可執行，使用可用獨立 reviewer | LOADED / BLOCKED_FULL_WORKFLOW |

## R2 路由（2026-10-14，實際使用）

依phase選相關subset；不以catalog數量冒稱執行全部技能。A=來源／root audit，B=passive instrumentation，C=安全TDD，D=emulator／review／handoff。無init／compact／LSP API，使用本輪短context anchor與真compiler/static證據。

| Skill | 實際provider/path | SHA256 | Phase | Status／結果 |
|---|---|---|---|---|
| using-superpowers | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/using-superpowers/SKILL.md` | `82c5c8866ad7f5dd4440ce66bd7806ba48a2f13771beae5cf112e53f08fe36ba` | A–F | USED／技能入口／控制授權 |
| deep-understanding | `/Users/huang/.agents/skills/deep-understanding/SKILL.md` | `a83d28eb0274847dc6c3cd4f849f5aaf5bc01ce1ebfa885f9aa91834f37340b6` | A | USED／真實Android流程及來源 |
| systematic-debugging | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/systematic-debugging/SKILL.md` | `808fc5717aa88ad65efff312b11c186294d3e6ee301afb584e2f86599b137787` | A–D | USED／第一分歧／可反證假說 |
| writing-plans | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/writing-plans/SKILL.md` | `a6c67c1900064347c2a329990dd3c555657c51c3ec53b259a08aa01a2c26139a` | A | USED／有依賴的R2診斷計畫 |
| using-git-worktrees | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/using-git-worktrees/SKILL.md` | `8cfb86f121269e8f7f12361e6795c4f6738828340e28964c9229d365666c9edd` | A | USED／隔離main／保留dirty |
| test-driven-development | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/test-driven-development/SKILL.md` | `64b03fce4aee5a97a93160cea8111f3ba13a17b7c001db4bd5836d67fd10705d` | B,D | USED／真正assertion RED→GREEN |
| code-verification | `/Users/huang/.codex/plugins/cache/openai-curated-remote/codex-engineering-guardrails/1.1.1/skills/code-verification/SKILL.md` | `4aab85003f547ac6e1ee12e3ceb3f89d5744af56f3b22a3643e545007474fc87` | A,F | USED／證據分層與完整gates |
| code-work | `/Users/huang/.codex/plugins/cache/openai-curated-remote/codex-engineering-guardrails/1.1.1/skills/code-work/SKILL.md` | `a3727efa825ff26a2ce67ff60379ad0e3a4bfcfc498c3632514fd662e7a0f3a6` | B,D | USED／限制test-only修改 |
| addyosmani-observability-and-instrumentation | `/Users/huang/.agents/skills/addyosmani-observability-and-instrumentation/SKILL.md` | `99e1024c67ce3c2410b356444ce5784301922ec044d9a022b2d121b729108c55` | B | USED／metadata觀察／no input logs |
| addyosmani-debugging-and-error-recovery | `/Users/huang/.agents/skills/addyosmani-debugging-and-error-recovery/SKILL.md` | `3908746ea39a4c23f3df27a7faa04540ad7834332e0f534922fbfffec630ec54` | A–D | USED／保留原失敗與diagnostic失敗 |
| addyosmani-ci-cd-and-automation | `/Users/huang/.agents/skills/addyosmani-ci-cd-and-automation/SKILL.md` | `067d15958ad69341fe37600be810e7a19f884fa67045385e48fd41c25dbd8e7d` | C,F | USED／controlled A/B/C＋failure artifacts |
| code-review-excellence | `/Users/huang/.agents/skills/code-review-excellence/SKILL.md` | `68df968e01fc7cc234f68ac9439a64e2f70b6c2dc02ad7bdb2928a61058ed8e8` | A,F | USED／severity/source定位 |
| requesting-code-review | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/requesting-code-review/SKILL.md` | `cfcee1b06774e7c0517f1e09be1a11f2d5680257072723e709ddbcf7e08b795a` | A,F | USED／三個獨立唯讀reviewer |
| receiving-code-review | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/receiving-code-review/SKILL.md` | `091df1629510af1b92fc4abd6f96732ebedb4cb2c0f3457e8f2740b0504a2438` | B,F | USED／核對finding再用regression修正 |
| verification-before-completion | `/Users/huang/.codex/plugins/cache/openai-curated-remote/superpowers/6.4.2/skills/verification-before-completion/SKILL.md` | `2befe7fc55bcadaa3d97dd9e8efeb633d2561c0ebe74c5a8b17c4d9e7e4520b3` | F | USED／fresh執行／不冒稱runtime |
| official-code-review | `/Users/huang/.agents/skills/official-code-review/SKILL.md` | `ae97f416c7e5ae708aa68a64a0cd137ff952c78ffd50f19ab147e19b2713adf3` | F | BLOCKED／指定Haiku/Sonnet/Opus及4 parallel slots不可用；非聲稱同技能批准 |
| open-code-review | `/Users/huang/.agents/skills/open-code-review/SKILL.md` | `d7fc77214056c3232b825978272da6ac4874b7825c5ee8b39a98cad19763d4c3` | A,F | BLOCKED／v1.12.13，單次provider401，無OCR批准 |

本輪TDD：old2.3真emulator stale-map lookup RED → same test2.4 GREEN；真bootstrap guard RED/GREEN。官方review模型harness不可用、OCR單次401，不宣稱APPROVE；可用獨立reviewers核對source/raw graphs/archives。詳細結果見[依賴修復](../testing/R2_UIAUTOMATOR_DEPENDENCY_REPAIR.md)與[R2審查](../testing/R2_INDEPENDENT_REVIEW.md)。
