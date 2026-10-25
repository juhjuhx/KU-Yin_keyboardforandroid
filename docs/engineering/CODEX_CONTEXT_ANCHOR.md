# KU-Yin 接續短入口 — R3.8 Issue31工程整合

先讀AGENTS、CURRENT_STATE、docs/maintenance/FINAL_AGENT_HANDOFF.md。使用者已批准PR28＋PR34的最小平台維護；integration分支`integrate/r38-issue31-engineering`保留原ancestry，沒有重造六個提交。PR29仍獨立；PR20／27不納入。

Issue31：View-only回饋不加權限，super仍保留true，night-v27保留完整style。舊PR28的Lint4 errors／REVIEW_INCOMPLETE收據是歷史，不代表新的整合裁定；舊PR34的CI40148115742 PASS也不能代替整合CI。本輪冷建置exit124不算全套本機PASS。最終main／integration SHA、CI jobs、Reviewer Integration Approval及merge狀態以[Issue31](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/31)連結的PR為準。

不啟動Emulator／H1/H2，不改原App/Host assertions、Dachen、decoder/editor/native pins，不發Release。Runtime歷史Host15/9/8、H0 INCONCLUSIVE，實機NOT_TESTED，新RC/Stable NO_GO。下一次仍按單一維護Issue與明確scope工作，不能由handoff自動授權測試或發布。

本機跨Agent入口：KU-Yin-Workspace/00-Control-Center/START_HERE.md；先status/resume-plan、核對HEAD與WriterLock，不重掃Desktop或搬移舊工區。
