# KU-Yin 接續短入口 — Issue31候選

先讀AGENTS、CURRENT_STATE、docs/testing/ISSUE31_PLATFORM_CONTRACTS.md。產品main仍2a2181e，PR28baseEA；本分支fix/r38-lint31-platform-contracts是獨立平台契約修正，非已發行版本。

View-only回饋不加權限，super仍保留true，night-v27保留完整style。本機127Python/12contracts、App267PASS/10SKIP、Host57PASS、兩Debug與兩instrumentation編譯、雙Lint0errors；exactSHA與新CI看當次PR／Issue31及本機收據，不能把本機結果當遠端CI。

不啟動Emulator／H1/H2，不改原App/Host assertions、Dachen、decoder/editor/native pins，不合併PR20/27，不發Release。依單一維護Issue工作。Runtime歷史Host15/9/8、H0inconclusive，實機NOT_TESTED，新RC/Stable NO_GO。

R3.8來源／授權／安全歷史入口保存在docs/maintenance/FINAL_AGENT_HANDOFF.md；最新本機跨Agent入口為KU-Yin-Workspace的START_HERE。
