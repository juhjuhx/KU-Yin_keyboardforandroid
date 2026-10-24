# Final Repository Map

- app：production Kotlin/Compose、JNI/C++、offline English assets；不含新ASR/排名/觸控引擎。
- acceptance-host：獨立Views editor/recorder、原App外部驗收assertions、同源JVM observer fixture。
- scripts：固定native bootstrap、frozen acceptance runner/manifest、另由stacked PR提出的純離線readiness（未整合前不屬main）、可重現資產、contracts/credential tripwire。
- .github/workflows：read-only build/artifact、明確opt-in Runtime、issue-only upstream watcher；無自動production publication。
- docs/ARCHITECTURE及architecture/：規範架構；CURRENT_STATE只記現況；HISTORY/review/archive保留歷史；MAINTENANCE_BACKLOG是未來工作。
- 原始Gitrepo只有一個common object store；多工區不等於多個產品主線。根工區可能是使用者dirty docs分支，不應reset；從fresh main建立用途明確的新分支。
- 本機KU-Yin-Workspace是private control/evidence/index，不整包上傳。Agent從START_HERE恢復；GitHub貢獻者從AGENTS/FINAL_AGENT_HANDOFF恢復。
