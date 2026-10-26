# Final Agent Handoff — KU-Yin Maintenance

## 入口與當前裁定

先讀AGENTS → CURRENT_STATE → 本檔 → MAINTENANCE_BACKLOG，再核對git status/HEAD/origin/main及PR/Actions。最新來源由GitHub actual refs和Issue19的收束收據決定，不假定文件內未來merge SHA。

本輪允許：test infrastructure、安全CI、標準授權文字、文件，以及另獲明確批准的Issue31最小平台契約維護。App Kotlin僅該Issue切片；JNI/native pins/applicationId/version/簽章/使用者資料不變。原始ccb2811與22c2d66另推送保存；從main2a2181e移植必要小切片，不整包merge舊分支。

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

## 歷史停止點（PR28@ea3fdb24，非新的整合裁定）
本候選僅Draft：新鮮App lint4 errors/77warnings（兩個VIBRATE、MissingSuperCall、API27屬性）未處置，且最後獨立審查因額度中斷而REVIEW_INCOMPLETE。ENGINEERING_CLOSURE=PARTIAL_BLOCKED，不能宣稱已合併或全部完成；main實際SHA看GitHub。此候選的LICENSE/CI修正尚未合併時，不能套用為main已修復。

## Issue31整合接續

已批准PR28＋PR34，沿用六個原始提交；`integrate/r38-issue31-engineering`只另補文件的歷史／當前標示。完整main→integration diff及exact-head CI／Reviewer receipt才是正常merge依據。[Issue31](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/31)連結最終整合PR與結果。本機全新工區冷建置觸及15分鐘上限（exit124），保留日誌；不改timeout或測試oracle，也不冒稱全套本機通過。

PR29四個離線readiness檔案保持獨立HEAD與diff；合併後可在確認差異不變時retarget至main，不自動整合。原PR28／34分支保留，不force-push。原PR20／27及Alpha八資產不變。Runtime／實機／新Release門檻仍未通過。

## 公開頁面最終收束

PR35 已於 main08e4 整合，Issue31關閉；舊PR28／34的停止點保留作歷史。新 README 只重排公開說明，保留原 Hero、基金會直接連結、實際技術與使用限制。新增七組有 manifest 的品牌／技術排版，不是操作截圖；原始source及renderer位於scripts/generate_publication_media.py。

本輪沒有產品組字／Dachen／JNI／pin／version／signing改動，也不啟動Emulator Runtime。最新 main CI、PR checks 與產物以 Actions exact SHA 及本機 FINAL_PUBLICATION_REPORT 為準；不能把固定08e4的工程結果複製成未驗新Runtime。

六份草稿保全於local-only parking42b88a9與Workspace manifest；歷史dirty四工區保護不變。桌面資料只經snapshot、完整backup、restore及獨立batch review才會移入既有Workspace，舊路徑以map解析；不可在下輪依舊絕對路徑盲目重跑歷史腳本。

工程轉Maintenance Mode。下一輪只從START_HERE／Task Ledger選有明確授權的單一Issue；新Release、實機、H0/H1/H2及完整erase仍各有獨立門檻。
