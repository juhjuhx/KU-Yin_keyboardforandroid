# R3 Android RC Closure — execution ledger

規格：2026-10-18 使用者 Android Release Candidate Closure。維持現有視覺與 typing；不新增decoder/ranking/touch/ASR。Critical gate 缺件即 NO_GO，不發布。

## Verified entry

R3 `release/rc-closure-r3`／`.worktrees/r3-release-candidate` 起點 main `63c9e4ab8ad7e277013acb5d7cce99228ec1f827`，clean、remote branch不存在。R231 final `e137e37594c04f8c0fd88c01ef911d1df9bcbc49`／runtime `d26fb1b76e1f448f6004910b1189a513a13fc1a9`；僅H0通過，舊AVD已結束。PR18現在`4190e0acb5649c415ca8692708da97a675d65363`，不是舊checkpoint SHA。

外部新evidence `/Users/huang/Desktop/KU-Yin-R3-RC-Closure-2026-10-18-205053/`：00原24工區status/diff／01遠端refs、Issue19、PR/Release完整回讀／02實際Tool Route Packet。24原工區均保留，main、舊tag/APK不動。

## Scope → implementation → test → independent review

1. [x] 唯讀來源盤點、實際skills/tools與兩位orthogonal reviewer派工。ADB連線清單為空；physical NOT_TESTED。無INTERNET/camera/mic需求，沒有裝機。
2. [x] QR RED：現有Compose畫面的像素交ZXing獨立解碼，四個literal URL與UI copy/open assertions；先見有意義失敗，不把初始化/下載失敗當RED。
3. [x] QR GREEN：離線OpenCV BYTE/M產四PNG，完整4-module quiet zone、manifest/hash、test-only ZXing，替換假Canvas。沒有手機掃描證據前移除「可掃描」宣稱，保留官方網址copy/open及非背書文字。density/font/night與既有候選/Dachen/ACK回歸；不擴UI重設計。
4. [x] 已核對R2/R231 lineage，local QR commit `de2ac7d94e0a79f492d11ee165173e65b4562bd7`，再與e137正常merge至隔離RC `185a4e545bd2574ad773e255ea558e6be5f3ab0e`。main/GH PR10/12不動，不重複套patch。README PR18初審REQUEST_CHANGES（未實作鍵盤高度），分開處理；Windows PR13/14不在範圍。
5. [ ] 最終candidate exact SHA：全Python/contracts/JVM/native/bootstrap/Debug/Host/instrumentation；fresh disposable H0→same-boot H1→H2/App5→Host33/32matrix→B01。使用既有runner，僅窄化經批准QR/version/test-dependency allowlist；保留原assertions/timeout/每層stop，不洗SKIP/INCONCLUSIVE。
6. [ ] 本機可識別Debug測試APK（版本高於20003）；公鑰/版本/四ABI/assets/source provenance核對。正式release signing ownership未證、實機未有則SIGNING_GATE/RC_READY阻擋；不生隨機key、不卸載/清資料。
7. [ ] Privacy/native license/source/relink材料、NOTICE導航、About/README、Issue19與handoff/validation/readiness；僅整合approved切片，final SHA gates，所有缺件逐案記錄。No unresolved High/Critical before publication。

## Current assumptions／rollback

- companion handoff bundle未取得；採使用者已允許的本機標準encoder替代，不能編造其manifest或把自產PNG叫官方圖。
- OpenCV只作離線asset tool，ZXing只作test decoder，不加入APK runtime dependency。新圖片是本專案由公開URL產生；逐檔授權與工具版本記錄。
- 沒有實機不改scan claim成「已掃描」，不將Robolectric／emulator當phone scanner。
- 每次新AVD必須重新取得H0；不能重用舊boot result。無健康門檻就保留raw並停止該runtime arm。
- 原main與R231工區不改；RC新commit可正常revert。先保留證據再rollback，無reset/clean/force/data wipe。

## Context control

從本ledger、R3_FINAL_HANDOFF、必要source和exact evidence續作；不重讀全部R1/R2。source review / runtime / physical / signing / legal 是不同gate，不能互相代替。Tool Master是真canonical using-superpowers skill，未發現另有可呼叫MCP Tool Master URI。OCR未送source至未批准provider，不偽報401 fresh check或APPROVE。

## 2026-10-19 checkpoint

- QR component final獨立SPEC/QUALITY PASS；17嚴格quiet-zone反例RED→18原斷言8/8GREEN，12圖完整矩形全白與整數模板另核對。舊07/09/11/14保留，不能以早期partial取代最後18。手機掃描/TalkBack仍NOT_TESTED。
- CI/runtime source未動產品typing/JNI：H0 result hash、serial/user/boot跨transition守門；19兩fake-ADB真RED→20兩GREEN。新CI只允許固定QR product de2、R231 infra e137、composed JVM tests185；21批准QR被舊guard拒RED→22；25root-build/wrapper/JVM七反例RED→26補保護。
- 對R3 PR停止重複的舊ungated實驗job（不是跳過App test）；必須另明確dispatch既有workflow的release-candidate入口，重新fresh H0→H1→H2/App5→Host33/32matrix→B01，不能沿用舊boot。23route RED→24GREEN。完整原assertions/source identities固定不變。
- runtime/最終build尚未執行，CI/runner最後獨立review仍進行。正式key/持有者證明未取得；沒有手機；privacy D2D與native license/relink gate仍OPEN。NO_GO，不發Release。
- 新外部evidence含兩位初審與QR final review。OCR未送source到未批准provider；初次review額度中斷保留，重置後恢復完成QR，不能冒稱當時已APPROVE。
