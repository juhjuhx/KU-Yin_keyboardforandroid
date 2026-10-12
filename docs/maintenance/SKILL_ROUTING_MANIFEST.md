# R1 Skill routing manifest

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

brainstorming/UI/visual/marketing/Three.js：NOT_APPLICABLE，user已批准收束非newdesign。official-code-review有安裝但本輪尚無PR時NOT_APPLICABLE；取得PR後按需路由。Addy其餘未load條目DISCOVERED_NOT_USED，不宣稱全24執行。Independent reviewers：r1_architecture_audit、r1_storage_build_audit、r1_fixture_audit（後者實作時不自評，final交另reviewer）。
