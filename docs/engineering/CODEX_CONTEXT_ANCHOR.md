# KU-Yin 續作短入口

2026-10-21，ACTIVE：發佈文件收束／R3.1 checkpoint。先讀 AGENTS → [CURRENT_STATE](../CURRENT_STATE.md) 頂端 → [ARCHITECTURE](../ARCHITECTURE.md) → [R3.1交接](../release/R3_1_FINAL_HANDOFF.md) 與必要 source/raw，不重讀整庫歷史。

- GitHub main `575cc883288a920e7aba021fb8705f75f465e1c0`，PR18已merged，僅README/community變更；原本地main工區未重設。R3.1工區 `.worktrees/r3-1-h1-capture`、branch `fix/r3-1-h1-capture`；H1 source `97645c238dcecc43e677de625bf7049bd487d700`，CI source `feab29f53583c4bb8dfc4cffe13243a36a3e9d06`。文件 HEAD 用 Git 核對，不能冒充新 runtime 證據。
- 原 R3 b982 CI39152444358：H0 HEALTHY；H1 INCONCLUSIVE／EDITOR_CAPTURE_UNVERIFIED。before frozen=true、UI dump只有33bytes marker／無XML／3004ms timeout。傳輸假設有誤，freeze→timeout 因果仍 UNRESOLVED。
- 最小修正只加 UIAutomator `adb shell -tt`；原3s/10s、XML/focus/IC/IME/user/boot/final gates 全保留。最終測試對原source真RED；本機health37、guard13、有限workflow4、contracts10 PASS。獨立 source review／timeout kill-reap／actual guard已核；不等於 Android H1 PASS。
- 唯一新 [CI39426302308](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39426302308) exact feab29f／FAILURE：Python212/89.463s、contracts10、固定四APK/source guard PASS；fresh H0 HEALTHY，H1 INCONCLUSIVE/EDITOR_CAPTURE_UNVERIFIED。`-tt`已執行但59bytes null-root錯誤／無XML／3006ms timeout；before unfrozen/HAS_DRAWN、after frozen/DRAW_PENDING且window identity變化、final恢復。因果仍UNRESOLVED。只裝host，無H2/產品install/IME切換/App/Host，已停止不重跑。
- 磁碟已於新preflight恢復約14GiB可用，本Agent未刪任何資料，歷史唯一滿載目錄不明；十份未提交文件已SHA備份並還原驗證，fresh local Python212 PASS。原ENOSPC丟失結果不追認。新證據 `/Users/huang/Desktop/KU-Yin-發佈收束-20261021-h3peyl7s/`。不清其他project、字典、Room、prefs、工區或build歷史。
- 外部 `/Users/huang/Desktop/KU-Yin-R3.1-H1-Capture-20261019-211633/`：00工區、03實際skills/tool route、10原raw、15最終RED、19contracts、20/21guard、22/23workflow、24preservation、25CIraw176檔、26manifest/log/receipt、獨立原報告。原R3 artifacts/relink/raw留在 `KU-Yin-R3-RC-Closure-2026-10-18-205053`，不複製成新產品證據。
- 新source guard：runtime固定b982、health獨立97645c、其他infra保持e137。整PR20審查仍有舊publisher/signing gate缺口；不能只審QR就發行。
- 實機NOT_TESTED；沒有owner+serial授權，不install或動IME／資料。正式key custody、QRphone scan、TalkBack、安全update、D2D/full erase、完整license gates OPEN。RC=false、Stable=false、NO_GO、NOT_PUBLISHED。PR18已合併；PR10HOLD、PR12/20不自動合併，公開v0.2.0-aie.1/tag不動。
- 下一步：針對既有raw的UIAutomator accessibility null-root與host window replacement建立受控觀察計畫，不先認定early-show/flaky/產品bug。只有可信新H1 PASS，才在另一明確階段fresh same-boot H0/H1/H2→App5→Host33/32matrix→B01。不能拼接歷史runs；不啟S2/M4/ASR。
- 未向未批准OCR provider上傳；無fresh401/APPROVE。沒有LSP/init/compact專用API；此短入口是持久context，沒有改AutoPilot或全域設定。
