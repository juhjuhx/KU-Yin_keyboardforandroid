# Final Agent Handoff — KU-Yin Maintenance

## 入口與當前裁定

先讀AGENTS → CURRENT_STATE → 本檔 → MAINTENANCE_BACKLOG，再核對git status/HEAD/origin/main及PR/Actions。最新來源由GitHub actual refs和Issue19的收束收據決定，不假定文件內未來merge SHA。

本輪允許：test infrastructure、安全CI、標準授權文字、文件與增量Workspace整理。App Kotlin/JNI/native pins/applicationId/version/簽章/使用者資料不變。原始ccb2811與22c2d66另推送保存；從main2a2181e移植必要小切片，不整包merge舊分支。

## 固定產品門檻

公開v0.2.0-aie.1 Experimental Alpha與八資產不變。H0 INCONCLUSIVE，H1/H2未執行；Host歷史15PASS9FAIL8SKIP；實機NOT_TESTED；RC/Stable false；新Release NOT_PUBLISHED。RRO caller／Freeze root UNRESOLVED。TEST_ONLY CI artifact不是更新相容APK、RC或Release。

Root LICENSE文字已恢復標準LGPL2.1，不表示舊包或future二進位全部合規；D2D、Room delete ACK、完整learning erase、品牌JPG權利、正式signer/保資料update/QR手機掃描仍有維護項。

## 接續規約

1. 本機先讀$HOME/KU-Yin-Workspace/00-Control-Center/START_HERE.md與ledger/checkpoint；不要全磁碟source discovery。
2. 檢查沒有其他writer，取得既有WriterLock；新任務只恢復尚未完成的單一Issue。
3. 原dirty工區、frozen APK/ZIP、private資料不改；不重置/清理/重寫歷史。
4. 測試分層記錄命令/exit/source/hash及PASS/FAIL/SKIP；缺實證寫NOT_TESTED。只有exacthead checks及獨立review通過才合併授權工程範圍。
5. 不因本檔或定時『繼續』自動啟新功能、Emulator、Release、背景排程。

安全恢復命令：
```sh
cd "$HOME/KU-Yin-Workspace"
python3 tools/kuyin_workspace.py status
python3 tools/kuyin_workspace.py resume-plan
```

Source/security/license/test/repo/archive對照見本目錄FINAL_*。本輪以正常開源維護為出口；未完工作進Issue與MAINTENANCE_BACKLOG，不再無限開新R系列。
