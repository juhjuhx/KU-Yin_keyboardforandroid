# 文件權威表

| Document | 狀態 | 範圍／衝突處理 |
|---|---|---|
| AGENTS.md | NORMATIVE | 操作規則/閱讀順序，不創造來源事實或擴大user授權 |
| CURRENT_STATE.md | ACTIVE | 目前狀態，test數字指fresh/historical來源 |
| ARCHITECTURE.md＋architecture/ | NORMATIVE | 實作ownership/interfaces，PROPOSED不算已有功能 |
| DEVELOPMENT/SECURITY/UPSTREAM | NORMATIVE/ACTIVE | build、security、pins與license界線 |
| CODEX_CONTEXT_ANCHOR | ACTIVE | 短續作導航，權威仍canonical/source |
| maintenance/* | ACTIVE | 本輪inventory/backup/review/validation/handoff；不是deviceacceptance |
| ROADMAP/AIE_V1_ROADMAP | ACTIVE FUTURE | future狀態/決策，不覆蓋architecture |
| AIE_V1_ARCHITECTURE | PROPOSED＋已實作S0/S1明示 | 擴充設計；current部分以規範架構校正 |
| docs/adr/* | PER-ADR | accepted只涵蓋其scope；被P0/S1替代的契約不回灌產品 |
| release/AIE_ALPHA_* | HISTORICAL/ACTIVE_RELEASE | 已公開exactassets/來源；後續進度在R1handoff |
| review/*、archive/*、HISTORY、COMPOSE-MAINLINE、REPO-HYGIENE | HISTORICAL/SUPERSEDED | 保留當時證據、由INDEX引當前入口 |
| README | ACTIVE NAVIGATION | 公开入口，不當架構authority |
| 舊translatedREADME/docs分支 | HISTORICAL_REFERENCE | 舊內容需逐份適配，不整支merge |

R1改寫canonical文件以移除競爭中的現在真相；原文可從main@a6a4c65與offlinebundle完整還原。沒有搬數百檔或改寫history。
