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

## R2 active investigation（2026-10-15）

main精確63c9e4ab8ad7e277013acb5d7cce99228ec1f827；PR9/11合併，PR10 remote ea5c2b5 OPEN/HOLD，local reviewed Host4fc43ff尚未merge main。R2保持product/pins/signing/資料不變，只修改debug editor、instrumentation、工具及Host test依賴。

同source3fc2c89的38563975932已重現App4PASS／1firstVisibilityFAIL：window focus及InputConnection尚未建立便show，平台明確not-served。已確認test precondition缺陷，debug after-focus one-shot修正有8個Robolectric測試；舊ea5曾served，完整歷史IMMS根因仍UNRESOLVED。另修正raw dump重導向沒有真正寫檔的觀測缺陷。exact3895625有限emulator38660489452已App5及ZH06/ZH01皆PASS、零skip，真editor文字我／我現在及commit次數符合；完整Host仍須新CI。

59b本地App57classes／282測試真重跑PASS（含native integration10）；Host107此次UP-TO-DATE，非fresh重跑。Python130、contracts10及四APKassemble PASS。55的3個native失敗源自舊host詞庫路徑變空；新獨立cache重新bootstrap相同pin後59b通過，原失敗證據保留。UiAutomator2.3→2.4只Host androidTest，真stale-map primitive有emulator RED→GREEN；最新full Host仍15PASS／17SKIP，不是完整acceptance。現在Host33JUnit＝32case＋1infra，frozen26 manifest不變。

最新[階段交接](testing/R2_FINAL_HANDOFF.md)／[R2證據矩陣](testing/R2_VALIDATION_MATRIX.md)；歷史R1文件保留。Physical OPEN、production NOT_READY、PR10 HOLD、S2/M4未啟動。
