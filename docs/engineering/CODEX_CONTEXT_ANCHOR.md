# KU-Yin 接續短入口 — 公開頁面與維護模式

先讀 AGENTS → CURRENT_STATE → [F3主線維護交接](../maintenance/F3_MAINLINE_HANDOFF.md) → [最終維護交接](../maintenance/FINAL_AGENT_HANDOFF.md) → MAINTENANCE_BACKLOG，再以實際 Git refs／PR／CI 核對來源。

- PR35 已整合 Issue31 的最小平台契約、Host observer、CI／安全與授權文字維護；固定工程基線為 main `08e4f69820e2ac9631f9fb1de7153824956d8f77`，Build/Lint PASS。後續文件 commit 不等於新 APK 已發布。
- README 現為下載、功能邊界、實際架構與維護入口；原創 Hero／七組品牌社群圖卡各有來源與 SHA。[媒體清單](../community/media/README.md)明列操作截圖0／BLOCKED，品牌或 Roborazzi 不是操作證明。
- 最新公開下載仍為 `v0.2.0-aie.1` Experimental Debug Alpha。限定CI40613937769 H0 HEALTHY／H1 INCONCLUSIVE，H2 NOT_EXECUTED；Host Runtime 歷史15/9/8，physical NOT_TESTED。新 RC／Stable NO_GO；TEST_ONLY CI artifact不是無損更新包。
- PR20／27保持 Draft/HOLD；PR29獨立的 OFFLINE_SPECIFICATION_ONLY。不自動整合、重啟Emulator、開新功能或發布。

本機唯一入口：`$HOME/KU-Yin-Workspace/00-Control-Center/START_HERE.md`。先 status／verify／resume-plan、核對 Writer Lock 和當前 HEAD，只恢復 Ledger 的一項維護工作；不要全磁碟搜尋或移動既有 Git 工區。

六份舊 README 草稿已經獨立 backup／restore 與 local-only parking commit `42b88a9d7a8ccfc997ea0d2db065a720bddca995` 保全，不能當作 main 改動重套。私有 Workspace 的歸檔 Journal／還原證明以本機交接為準，不整包上傳 GitHub。

F3：Room40／QR42已整合至 main8a48902；PR41 僅偏好 D2D 政策仍 Draft。候選版本與建置狀態看實際 PR／CI，不把20006 TEST_ONLY 當公開新版本。
