# KU-Yin 維護接續入口

先讀AGENTS.md → CURRENT_STATE.md → maintenance/FINAL_AGENT_HANDOFF.md → maintenance/MAINTENANCE_BACKLOG.md，再讀必要source。歷史review/AGENT聊天不是競爭中的當前真相。

公開Alpha仍v0.2.0-aie.1/20003，無新Release。R3.8只整合Host observer、offline readiness、CI/secret/license/doc修正；app組字核心/JNI/pins不改。H0 INCONCLUSIVE，H1/H2未執行，Host歷史15/9/8；實機NOT_TESTED，RC/Stable false。

本機可讀$HOME/KU-Yin-Workspace/00-Control-Center/START_HERE.md並執行status/verify/resume-plan；先核對Git HEAD/dirty和WriterLock。不重掃全磁碟、不重新搬R3.5 archive、不改其他Agent環境。這個入口不授權自動新功能、Emulator、刪資料、merge或Release。

下一工作由Issue/MAINTENANCE_BACKLOG挑單一可驗證slice；RRO/初始化完成訊號、Room刪除ACK/D2D/媒體權利/實機簽章仍有明確缺口。無init/compact專用API，不冒稱調用。
