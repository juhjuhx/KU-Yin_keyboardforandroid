# R2 獨立審查

## 診斷準備切片

三個唯讀review方向分開：CI歷史／orchestration、Android lifecycle／test validity、privacy／source preservation。Reviewer沒有修改shared source或裝置。此審查不等於實際runtime或根因批准。

| Finding | Severity | Source／直接證據 | 處置 |
|---|---|---|---|
| capture exception掩蓋原visibility assertion | High | ImeRuntimeSmokeTest新增capture-first；原邊界4測2 assertion failures | 新debug boundary保留相同primary＋suppressed，4/4GREEN；新增onActivity亦保護；不送額外JUnit status |
| terminal寫入失敗仍exit0 | High | runtime_smoke EXITtrap／fake terminal為directory | 真RED 2!=0；失敗設定evidence_status，原App7保留；GREEN |
| install期間user改變仍enable/set | Medium | fake during_install switch5後發生mutation | RED後install完成重驗、mutation前重驗、明確user0；GREEN |
| B/C只比單向source keys | Medium | controlled workflow source guard | 完整filtered dict＝keys+digests；script/bootstrap/Gradle provenance補全 |
| snapshot probe transport failure當ABSENT | Medium | fake ADB exit13但原script0 | 真RED後PRESENT/ABSENT sentinel＋unknown/transport FAIL；GREEN |

`/root/r2_lifecycle_read`已核對原三個precondition wait、visibility wait及shell body與main逐字相同，40×250ms不改；原show/focus次數與順序保留；無新增IMM checkFocus API。直接核對09兩份XML各4/0/0/0及instrumentation compile。Source準備可批准，runtime尚未跑。

`/root/r2_ci_evidence_read`自行跑focused10測PASS，另外temporary fake faults驗證terminal/capture/probe fail→2、原App7→7且不跑Host、UNKNOWN sentinel拒絕；最新script/tests/workflow SHA核對一致。裁定可以正常push draft diagnostic PR，沒有未解High／Medium。完整報告與來源manifest保存在外部`CI唯讀比對/R2_CI編排獨立覆核.md`／JSON。前輪report文字與同步修改後hash有時間差，已明記不把舊finding套在新bytes。

`/root/r2_privacy_preservation_review`獨立重算R1 archive1185 hashes、536 original material references、18原工區所有heads/status/diff hashes以及8份frozen source，全部一致；protected diff＝0 bytes。只位於debug/androidTest，release無新diagnostic source set／network權限。Screenshot與platform dump本身可能有畫面內容，安全性依賴workflow fresh disposable synthetic emulator，不能稱任意emulator都安全。沒有隱私／保存範圍的blocking source defect，可push diagnostic draft PR。

三個方向僅批准診斷準備source；後續emulator case evidence review尚待執行，不繼承尚不存在的runtime APPROVE。

## OCR／其他技能

OpenCodeReview v1.12.13，單次provider health HTTP401 invalid x-api-key，BLOCKED_BY_PROVIDER；沒有OCR APPROVE。official-code-review所指定Haiku/Sonnet/Opus harness不可用，未冒稱完整官方pipeline；採上述真獨立source reviewers。

## 合併界線

PR10維持HOLD。須先有支持根因／disposition的actual trace、App5原assertionPASS、適用Host32實際執行與每個舊failure/skip裁定，再對exact PR source/merge SHA做獨立驗證。UNIT／compile／draft PR批准不能替代。
