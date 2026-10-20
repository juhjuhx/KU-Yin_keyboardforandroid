# R3.1 H1 Capture Recovery — Implementation Plan

> 執行方式由使用者指定：主作者依TDD增量修正，獨立唯讀review後再驗證。既有授權已涵蓋此最小test-only切片，不重問generic execution menu。

**Goal:** 先修復可信H1證據擷取，再依同boot健康gate決定是否執行產品驗收。任何Critical缺證維持NO_GO。
**Architecture:** 沿用emulator_health與既有CI。產品、JNI、native pins、原App/Host assertions不改；擷取通道與readiness／實際產品狀態分開。
**Tech Stack:** Python unittest、ADB shell/UIAutomator、API33 disposable CI、既有instrumentation。
**Spec:** 使用者KU-YIN R3.1 FINAL RELEASE BLOCKER CLOSURE；基線PR20 de2d9df、runtime b982062，main63c9e4a。

## Constraints／review focus

- 不重跑完整Binding A/B，不加timeout、不刪測試／鬆assertions，不用missing/skipped替PASS。
- 四APK和產品source identity固定；真手機需owner、serial、更新與IME變更的明確授權。
- noninteractive dump可能沒有控制TTY；成功提示不等於XML。驗證真正輸出而非grep source。
- timeout仍fail closed；錯focus／真nullIC／frozen任一bracket／changeduser／defaultIME都不放行。
- 不把POSIX characterization、fakeADB、health-only或Build成功當Android產品Runtime／physical PASS。

## Steps

1. [x] 真raw result／commands／ui-0／WMS與source深讀；唯讀核main/PR20、保存24原工區與原raw，建立隔離fix/r3-1-h1-capture。
2. [x] 平行兩個唯讀review：H1平台假說與整PR20／隱私／release；先來源與獨立反例，不能先認定timeout單因。
3. [x] 基線執行／失敗保存；最終helper對原source meaningful RED（15），不把Sandbox EPERM／缺import當RED。
4. [x] 最小capture source修正、37 focused及獨立review；原3s/10s與strictoracle保持。actual guard13、有限route4、contracts10 PASS；本機全量ENOSPC不列PASS，新CI212真PASS。
5. [x] 唯一H1-only CI39426302308 exactfeab29f：freshH0 HEALTHY、只裝原host、H1 INCONCLUSIVE/nullroot+timeout；176raw/hash保存，停止，沒有安裝產品／H2。
6. [ ] BLOCKED_H1：App5／Host33／32matrix／B01未執行。下一因果調查不自動重跑完整Binding A/B或放寬gate。
7. [x] 實機inventory／owner plan與整PR20 source/release審查；六份R3_1文件、exactSHA與限制已收納。實機、完整privacy/license/signing及release仍OPEN，未假裝完成。

## Rollback

只正常revert新branch提交；不改R3／R1/R2工區，不改舊release/tag／APK或私人資料。最後未完成的review標REVIEW_INCOMPLETE；quota中斷保存handoff後停止，不重試耗額度。
