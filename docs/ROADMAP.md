# Roadmap（FUTURE only）

ACTIVE future入口，R1 2026-10-12。當前事實見CURRENT_STATE，規範見ARCHITECTURE。詳細AIE entry/exit/rollback見[分階段roadmap](roadmap/AIE_V1_ROADMAP.md)。不以此文件授權新功能。

## DONE

applicationId與toolchain更新、native production/startupfallback、P1-A/B/C/D、P0核心typing/EnglishMVP、AIE S0unsupportedreset/S1candidatebrowser已有main source。v0.2.0-aie.1 experimental Alpha已公開；不重做decodercutover或候選UX選項批准。R1為docs/inventory/工具收束，非產品擴張。

## 最小下一步

1. currentHostfixture適配後case-by-case emulator/physical editor證據；保留舊15/9/8與INCONCLUSIVE範圍。
2. exact公開APK手機候選scroll/expand/page/select/continue及editor切換；不能用JVM代替。
3. 已確認privacy/update/feedback/Englishpointer議題，各自隔離fix與ADR，不混進治理或清userdata。
4. 完整learningerase／migration／export、OEM D2D策略、production signing與完整license/reproreview需獨立設計及決策。

## 未開始

AIE-S2 retrieval/ranking、personal supplementallearning、TouchResolver、SymbolRepository/search、newnativebackend、M4測量、F-Droid/Play／productionrelease。均非R1授權；不可因compile或Alpha公開自動開始。舊C4/M4路線保留Git/HISTORY/review/archive，不當當前要求。

dependencyupdates只immutable pin與reviewedPR；正常merge/rollback與既有資料保存優先，禁止floatingupstream或重簽破壞updatecompat。
