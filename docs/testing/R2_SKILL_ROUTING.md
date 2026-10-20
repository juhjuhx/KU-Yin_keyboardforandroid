# R2 技能路由紀錄

按實際階段讀取17個相關skill；15個USED、2個BLOCKED。未使用無關視覺／行銷技能，也未改global Codex設定。

| Skill | Phase | Status | 實際用途／限制 |
|---|---|---|---|
| using-superpowers | A–F | USED | 技能入口／控制授權 |
| deep-understanding | A | USED | 真實Android流程及來源 |
| systematic-debugging | A–D | USED | 第一分歧／可反證假說 |
| writing-plans | A | USED | 有依賴的R2診斷計畫 |
| using-git-worktrees | A | USED | 隔離main／保留dirty |
| test-driven-development | B,D | USED | 真正assertion RED→GREEN |
| code-verification | A,F | USED | 證據分層與完整gates |
| code-work | B,D | USED | 限制test-only修改 |
| addyosmani-observability-and-instrumentation | B | USED | metadata觀察／no input logs |
| addyosmani-debugging-and-error-recovery | A–D | USED | 保留原失敗與diagnostic失敗 |
| addyosmani-ci-cd-and-automation | C,F | USED | controlled A/B/C＋failure artifacts |
| code-review-excellence | A,F | USED | severity/source定位 |
| requesting-code-review | A,F | USED | 三個獨立唯讀reviewer |
| receiving-code-review | B,F | USED | 核對finding再用regression修正 |
| verification-before-completion | F | USED | fresh執行／不冒稱runtime |
| official-code-review | F | BLOCKED | 指定Haiku/Sonnet/Opus及4 parallel slots不可用；非聲稱同技能批准 |
| open-code-review | A,F | BLOCKED | v1.12.13，單次provider401，無OCR批准 |

完整provider/path/SHA256：本輪外部 `R2-skill-routing-manifest.json`。USED代表實際指導本輪動作，不代表該技能提供獨立runtime證明。official指定模型不能以本harness的reviewer冒充；仍完成獨立source review。

OCR health只執行一次：`ocr llm test` exit1，Anthropic HTTP401 `invalid x-api-key`，Request-ID `req_011CfzpWrgLXQ13rxGMSGnYh`。沒有傳送產品typing內容／憑證；不反覆retry。
