# R2.2 有限 A/B 實驗契約

ACTIVE；2026-10-17。這是test infrastructure因果研究，不是已修復或產品驗收。

## 常量與改變

| 項目 | A | B |
|---|---|---|
| Android來源/原App斷言/timeout | exact2b8e8e4，同一built APK | 完全相同 |
| SDK/user/AVD | API33 default x86_64 pixel7,user0,fresh | 相同設定，另一freshAVD |
| Debug signer/native/assets/APK | 一次建4APK，source manifest及SHA256SUMS | 同job同4bytes，入B前hashguard |
| App安裝/執行 | 原installDebug→select→connected | App/AppTest先install→select→am instrument |
| 前置採證 | server-only proto＋package/PID/events | 同schema；不同流程多出的install checkpoint明列 |
| 結束邊界 | 原App5後停止，Host未要求 | App5/0/0後Host33，再focused原B01 |

不得將一個arm的PASS拼到另一個arm的matrix。A App-only不是完整suite。B current Host33／32 owners與frozen P1-E26分開，嚴格parser共用原state machine的私有module instance，不改原runner/manifest。未知case、wrong owner、duplicate/missing、skip/failure、truncation都保留其原語義；INCONCLUSIVE不轉PASS。B01仍是同identity原assertions，不改geometry。

## 來源與安全

原始實作：`scripts/runtime_smoke.sh` opt-in `KUYIN_R22_BINDING_ARM=legacy|preinstalled`；default原路徑保持。`scripts/r2_ci_protocol.py`只驗協定/serverproto、不操作ADB；current manifest sourcefiles SHA對2b8。workflow為既有`r2-host-observation-probe.yml`新增manual `host_scope=binding`，不是自動CI或新production runner。

CI/唯一emulator/qemu/user0 fences；before-install明確missing-package可記ABSENT，安裝後唯一base.apk/hash必須match。signature錯誤不uninstall/clear。App原JUnit/protocol先持久化，採證／user identity錯誤另保留，不將primaryFAIL覆寫。App/Host選取前phase使用不同路徑保留，不覆寫前一epoch。原資料字典/偏好/DB不讀、不刪；沒有新觀察seam或JNI。

關鍵時間窗拒fullIMMS client dump；proto empty/text/corrupt/rootless拒絕，缺bool保留None/未序列化，不假remote=null。fulltext只結果後；user改變必須failclosed，不改另一user或呼叫其client採證。所有採證僅fresh synthetic CI，舊evidence/ZIP/APK不覆寫。

## 因果與失敗裁定

A/B都PASS＝本次未重現；都FAIL＝共同因素未隔離；AFAIL/BPASS＝orchestration相關、非replacement唯一證明。必要C在freshB流程只加exactapp覆裝，不再select/重show，不改timeout；需要actual replacement/process/binding鏈與相同firstvisibility phenotype才可提高信心。先由direct evidence定位第一分歧，unknown仍unknown。no-restart只研究、不用來規避原cold/process生命週期。

local fake工具原10s watchdog不等於Androidvisibility。新binding流程多次獨立Python/SDK虛擬命令，10s造成可重現hostwatchdog ERROR；僅該fakefixture改30s，原default10s、Android40×250ms/UI/selector都沒變。ERROR不算RED，20b已取得真正user0 assertion RED。所有中途FAIL/ERROR原log保留。

## 進入CI的gate

Focused/full Python、10contracts、shell/YAML/diff、Android/frozen source及公開artifact hashes、獨立limitedexperiment審查。CI先sourceguard/compile4APK及exact hashes；A/B各留outcome/terminal，任一失敗不把workflow標green。整job60分鐘涵蓋兩freshAVD及已量測約30分鐘的fullHost，不改任何個別assertion等待。

Runtime數字與根因只在新raw回收後寫入R2_2_FINAL_HANDOFF，不沿用historical success。PR12 Draft、PR10HOLD，無merge/release/S2/M4/實機授權。
