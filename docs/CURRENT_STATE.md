# R3.4 圖資整合與 Runtime 門檻

基線main5c7b22c（PR25 README／原創SVG已合併），圖片source43aff1d。十Launcher及RoborazziPNG完整解碼、113Python／11contracts fresh PASS；圖片是source／unit-render證據，不是手機截圖。圖片切片獨立review PASS_SCOPED；最新整合CI與維護者決定見PR24。產品／QR PR20仍Draft，沒有改Kotlin/JNI、native pin、版本、資料或簽章；舊Alpha原樣保留。

R3.3 diagnostic9ac52b5 fresh獨立spec/engineering PASS並推送；唯一health-only CI39734667334最終H0 INCONCLUSIVE／FINAL_WINDOW_UNVERIFIED（初始unfrozen，final frozen）。H1／H2未執行，不盲retry。Host舊CI39515675883的15PASS9FAIL8SKIP仍未閉合，不能稱圖片build等於Runtime PASS。Physical NOT_TESTED；RC／Stable NO_GO。見 [R3.4圖片整合](testing/R3_4_IMAGE_INTEGRATION.md)。

---

# R3.2 圖片切片 — 2026-10-21

本切片基線main8369da25，PR18／22已合併。CI39509158938最終blocking build PASS，experimental runtime FAIL：App5 PASS／Host15 PASS9 FAIL8 SKIP。圖片分支重新生成十個Launcher fallback並通過完整雙decoder與獨立複審；不是原件恢復、手機畫面或Runtime PASS。greeting PNG已以原Roborazzi source重建；真實操作截圖仍OPEN。驗證見 [R3_2_IMAGE_INTEGRITY](testing/R3_2_IMAGE_INTEGRITY.md)。PR20 Draft；RC／Stable NO_GO不變。以下保存前輪時間點。

---

# 當前事實 — 2026-10-21

main 基線 `575cc883288a920e7aba021fb8705f75f465e1c0`；README／社群 PR18 已合併。已公開版本仍為 `v0.2.0-aie.1` Experimental Debug Alpha；PR20 Draft、RC 未發布，Release **NO_GO**。

本輪重新核對公開八資產、APK 20003／0.2.0-aie.1、Debug certificate、四 ABI 與 source/relink 雜湊。main CI39442957936 的 App5 PASS、Host15 PASS／9 FAIL／8 SKIP；blocking build 成功不能掩蓋 experimental Runtime 失敗。

R3.1 隔離分支 runtime feab29f：H0 HEALTHY、H1 INCONCLUSIVE／null root／逾時，H2 及產品 Runtime 未執行。文件保存 commit6e4bcd7 已推原分支；不是 main 的產品 patch。沒有經授權實機驗收，也沒有可核實公開操作截圖。

只需續讀 [交接](release/R3_FINAL_HANDOFF.md)、[驗證](release/PUBLICATION_VALIDATION.md)、[TODO](release/TODO_AND_GATES.md)；架構仍以 [ARCHITECTURE](ARCHITECTURE.md) 為準。以下 R1 紀錄是歷史，不作當前 gate。

---

# CURRENT_STATE

ACTIVE；更新2026-10-12 R1。只記目前事實，歷史測試見review/INDEX與原Gitcommit。

- 本輪起始GitHub main `a6a4c654845338521700121ac06586887d1ec085`；PR#8正常merge；tag `v0.2.0-aie.1`仍指 `2ce6810684fd65a65a3006a224dc786b1f3a5f62`。產品source39acf96；公開experimentalDebug APK20003/0.2.0-aie.1，SHAacdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634。R1不重做S0/S1、不發布新產品。
- Chinese為native production/startup fallback；P0基本中文WORKS_ON_USER_DEVICE（使用者回報，非全matrix）；Englishoffline10kMVP。
- S0完整reset disabled/unsupported、零資料刪除；S1單列橫捲＋展開grid、scratchplanpaging、snapshotguard。ranking/touch/catalog/S2未實作，M4 NOT_VALIDATED。
- 歷史CI38164320915 blockingPASS，API33x86_64 app5/5；host32=15PASS/9FAIL/8SKIP。fixturegeometry/stale與舊B契約需隔離adapter，timeout根因不能全數推定產品。新的結果僅登記maintenance/VALIDATION_MATRIX.md。
- Full physical acceptance OPEN；production NOT_READY，缺正式keystore BLOCKED_BY_SIGNING；erase/migration/OEM/TalkBack/license runtimeinventory仍OPEN。
- R1在隔離chore/mainline-consolidation-r1：來源/工作樹資料盤點、verified外部backup、規範架構10章、文件權威/data政策、doctor小修。Hostadapter另fix/r1-host-fixture-adapter，不改product、不清資料。
- 最新進度／最終SHA/PR/測試：[FINAL_HANDOFF](maintenance/FINAL_HANDOFF.md)，fresh/historical分層：[VALIDATION_MATRIX](maintenance/VALIDATION_MATRIX.md)。

## R1 收束與尚未合併的驗收修正

2026-10-12：文件／doctor／CI治理 PR #9 正常合併 `bf7c5d1d912d77ae8c6c7179d26d72bf835232bc`；產品 source、native pin、簽章與資料 schema 不變。`.worktrees/mainline` 持有 local main，已正常fast-forward；原15工區及4dirty保留，18工區總數含本輪3個。

本輪 fresh baseline emulator run38224799842：App5PASS；Host15PASS／9FAIL／8SKIP。隔離 Host PR #10 未合併：087ff39 run38225649247 Host15PASS／17SKIP；ea5c2b5 run38254806233 blocking buildPASS，但App runtime4PASS／1FAIL（input window visibility），Host未執行。根因UNKNOWN，不能稱emulator flaky或由fixture造成；不跳過App層、不盲retry、不改產品／visibility assertion。Host adapter後續device-free成本修正另列local證據，仍須完整新runtime gate。

詳見[本輪驗證](maintenance/VALIDATION_MATRIX.md)與[交接](maintenance/FINAL_HANDOFF.md)。Physical OPEN／Production NOT_READY／M4 NOT_VALIDATED／S2 NOT_STARTED不變。
