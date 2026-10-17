# R2.2 Binding Isolation Implementation Plan

> 由主agent依executing-plans序列執行；獨立review只讀。

**Goal:** 以可追溯A/B與必要C定位遠端IME binding缺席，不預設reinstall根因。
**Architecture:** 重用runtime_smoke與既有strict protocol parser；明列current33 source contract，frozen26不改。所有新操作受CI/emulator/user0 fence。
**Tech Stack:** Bash/Python/GitHub Actions/API33 Android instrumentation。

- [x] 核對remote/local HEAD、保存20原工區狀態，建立fix/r2-2-ime-binding。
- [x] source/control-flow深讀寫入R2_2_BINDING_RESEARCH.md。
- [x] 修改scripts/tests/test_runtime_smoke_sequence.py先驗直接instrument路徑與platform events採證RED。
- [x] 建current source identity/case manifest與scripts/r2_ci_protocol.py，重用P1 strict lifecycle parser（僅adapter的current case allowlist），負例包括missing/duplicate/wrong owner/FAIL＋PASS emit。
- [x] runtime_smoke新增opt-in A/B路由與相同schema snapshots，不改default原流程；同4APK identity hash前後驗。
- [x] 原有限workflow新增manual binding A/B scope；同job先建4APK，fresh sequential emulator，raw保存，不把A negative控制改PASS。
- [x] Python151、contracts10、shell/YAML/source-preservation gates與固定六檔獨立experiment設計審查；只批准一次paired實驗。
- [x] local atomic source commit3115002並正常test branch push；一次A/B run39005467151執行中。必要C仍須由A/B證據決定。
- [x] 主agent回核raw/protocol/XML/事件：A/B都4PASS1FAIL；237 source相符。install前SystemUI ANR→windowfocusfalse/IC0，舊remote根因UNRESOLVED；不臆造rootfix，不啟動C。
- [ ] BLOCKED：App前置FAIL，fullHost33/32matrix與B01均NOT_TESTED；不得盲retry。實機NOT_TESTED。
- [x] source safety／平台／current identity契約有完成的獨立收據，canonical docs及handoff整理。
- [ ] BLOCKED：新runtime independent review因quota未完成，不能自評批准；PR12 Draft、PR10HOLD。
- [x] docs與source/CI保存完成；最終docs commit／SHA與remote核對以external R2_2_FINAL_RECEIPT.json為準，不在文件自引用。

執行命令：`python3 -m unittest discover -s scripts/tests -v`、全部scripts/check_*.py、`bash -n scripts/runtime_smoke.sh`。Android來源不改，用source byte guard核對，不重編已凍結產品。CI同job建4APK後每arm核exacthash；不得以本機新key替換release。

## 實作前檢核結果

29 source snapshot28全程固定：Python151 PASS/exit0；contracts10、shell/YAML/docs/diff PASS。Android source/frozen runner/manifest/license/pin/build diff0。原RED與中途FAIL/ERROR分開保存：12 baseline3 assertions exit1；13 pkgabsence/primary兩assertions exit1；20是fakewatchdog ERROR不算RED，20b是user guard真assertionRED；24 checkpoint與27wrongusercleanup亦真RED，最後151均GREEN。只fakebinding host-process watchdog因命令量增長由10→30s，Android原40×250ms及全部UI/selector等待不改。未取得runtime rootcause或App/Host新PASS。
