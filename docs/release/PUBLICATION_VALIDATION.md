# Publication Closure 驗證表

2026-10-21；本輪文件基線 main `575cc883288a920e7aba021fb8705f75f465e1c0`。區分「本輪重新執行」、「本輪讀取既有 CI 原始證據」與「歷史未重跑」。文件／工程 quality 的通過不等於產品 release 通過。

## 本輪直接執行

| 檢查／命令 | 結果 | 證據及界線 |
|---|---|---|
| R3.1 十份文件 backup + SHA256 + 獨立目錄還原 | PASS：10/10 | 原始稿 bytes 不變；保存後文件更新 commit6e4bcd7；不含私人 DB／金鑰 |
| R3.1 `python3 -m unittest discover -s scripts/tests -v` | 212 PASS、exit0 | unittest128.724s，外層128.912s；不是 Android Runtime |
| R3.1 全部 `scripts/check_*.py` | 10 PASS、exit0 | 僅原隔離分支單元／靜態證據 |
| 本文件分支同一 Python 命令 | 109 PASS、exit0 | unittest2.311s，外層2.446s；scripts 沒改，與 R3.1 的212不是同一 suite |
| 本文件分支全部 `scripts/check_*.py` | 10 PASS、exit0 | 包含 docs consistency、Dachen、Editor、build、privacy contracts |
| 公開 Alpha 八件下載 bytes／GitHub API digest | 8/8 PASS | `gh release download`；七項外層 SHA256SUMS 也一致；舊 Release 未修改 |
| `apksigner verify --verbose --print-certs <公開APK>` | exit0，v2 PASS | Android Debug cert46488e99…；非 production key custody／手機更新保證 |
| `aapt dump badging`／`dump permissions` | exit0 | appId／20003／0.2.0-aie.1／四ABI；無 INTERNET、CAMERA、RECORD_AUDIO |
| APK vs 公開 provenance | 4 native＋8 dictionary 項 PASS | 只驗 bytes，不冒充重新編譯 |
| source/relink tar 內部 SHA256SUMS | 4806/4806 PASS | 5868 archive entries 唯讀盤點；未 extract／執行包內程式或重建 |
| 圖片解碼及來源分類 | 13 項盤點完成；2 PASS／11 FAIL | 失敗如實保留，無真實操作截圖；不做 product runtime 推論 |
| `adb devices -l` | exit0；只有既有 ARM64 emulator | 沒有經授權實機；未接管 emulator、安裝或改 IME |

原始命令、退出碼、hash、XML 與 review 保存在外部 `KU-Yin-發佈收束-20261021-h3peyl7s`。13/14 為本分支 Python/contracts 收據，08/12 為公開資產核對，09/11 為 main XML 解析。外部檔含本機保存資訊，未打包上傳。

## 本輪取得的既有 CI XML

[main Build CI39442957936](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39442957936)，exact source575cc883。blocking build **SUCCESS**，experimental runtime job **FAILURE**，即使 Workflow 整體 success 也不能稱 Runtime 通過。

| 層 | XML 直接結果 | 未證明 |
|---|---|---|
| App JVM | 265 total：255 PASS／10 SKIP／0 FAIL | Android lifecycle／實機 |
| Host JVM | 37 PASS／0 FAIL／0 SKIP | 真實 Editor／native runtime |
| App instrumentation | 5 PASS／0 FAIL／0 SKIP | RC／所有 OEM 或整體矩陣 |
| Host instrumentation | 32 total：15 PASS／9 FAIL／8 SKIP | 不是後續分支33 tests／32-case matrix 的完整執行 |

JVM XML 的原 timestamp 範圍是 `2026-10-11T19:03:37.717Z` 至 `2026-10-11T19:04:39.897Z`；這是本輪從該 CI artifact 讀回的既有報告，不宣稱10月21日重新執行 JVM。是否來自 cache／up-to-date 未在本輪證明。

九項失敗：ZH01/02/04/05、EN02、B02/B03、D13/D14；XML stack 均有 `androidx.test.uiautomator.StaleObjectException`，發生於 UIObject2 accessibility/bounds 路徑。這是直接失敗線索，不等於已證明 decoder 或 IME 產品根因；本輪沒有修改 assertions、fixture 或重跑至綠。八項 SKIP 仍為 SKIP，不能列 PASS。

[R3.1 CI39426302308](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39426302308) exact feab29f：H0 HEALTHY，H1 **INCONCLUSIVE**。真 argv 有 `shell -tt`，59-byte null-root error／無 XML／3006ms timeout；before unfrozen/HAS_DRAWN、after frozen/DRAW_PENDING、final 恢復。時間相鄰不能證明 frozen 或 PTY 的因果，root **UNRESOLVED**。H2／App5／Host33／32-case／B01 **NOT_EXECUTED**。

## 未執行／阻擋

- 本文件最終 HEAD 沒有新 Gradle／native bootstrap／instrumentation build／Android CI；未改產品、工具或 workflow，先檢查已存在的 exact main 證據，避免無假說的昂貴 Runtime 重跑。不能寫成 final HEAD build PASS。
- 沒有 authorized physical device：實機30-case、QR camera scan、TalkBack、跨 App 及保資料升級 **NOT_TESTED**。
- 圖卡方向待使用者答覆；真實截圖待擷取及逐張審查，不輸出假畫面。
- OCR 已知 config 不可用且使用者拒絕未批准外傳，**BLOCKED_BY_PROVIDER**；本輪沒有重試或上傳 source/log。使用獨立本地 read-only reviewers。
- 完整 native licensing／fresh relink equivalence、正式簽章管理、OEM D2D、full learning erase 仍 OPEN。

最終 JSON／連結／diff／source preservation 及獨立審查結果附於 [review](PUBLICATION_INDEPENDENT_REVIEW.md)；未獲產品驗收。Release 維持 **NO_GO**。
