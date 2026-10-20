# R3 發布裁定

2026-10-19；candidate source `b98206267b3a65c452db1661bb3bae910ec61f6a`。**RC_READY=false；STABLE_READY=false；RELEASE_VERDICT=NO_GO；RELEASE_URL=NOT_PUBLISHED**。版本標籤 `0.2.0-rc.1` 是未發布 Debug artifact 的身分，不是 RC gate 通過宣告。

| Gate | 裁定 | 尚缺 |
|---|---|---|
| 最小產品修正 | QR component PASS | 四URL標準編碼／獨立畫面解碼完成；手機scan未測，UI明示未驗證 |
| Android runtime | BLOCKED_AT_H1 | 唯一CI H0 HEALTHY；H1擷取XML逾時，H2/App5/Host33/32-case/B01未執行；深層根因未解 |
| 實機／更新 | NOT_TESTED | 沒有授權連線手機；核心輸入、候選、Enter、跨editor、更新保資料都不能用JVM代替 |
| UI/accessibility | PARTIAL | QR密度／字級／橫向Compose測試已做；全鍵盤layout／contrast／TalkBack／真手機未測，未重設計 |
| 隱私 | OPEN | 無新增產品network/camera/mic；Room/prefs D2D excludes缺口、OEM行為及完整erase仍未關閉 |
| 簽章 | BLOCKED_FOR_FORMAL_RELEASE | 本機沿用舊Debug cert、appID相同、20004>20003；長期Release key custody／復原策略未證，實際update未測 |
| Native source/relink | MATERIALS_AVAILABLE／PARTIAL | 新 exact-source bundle＋四ABI fresh relink已做；Rust source rebuild等價、完整compiler/runtime license subset及法律核准未完成 |
| 獨立review | 分scope完成 | QR source／CI source通過；runtime observation PARTIAL；artifact靜態通過，不能合併成整體APPROVE |
| GitHub | Draft review only | main／舊tag／Release不變；PR10HOLD、PR12Draft、PR18獨立review，不能自動merge |

## 簽章／資料保留政策

本機新 APK certificate SHA256 `46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94` 與公開Alpha相同。它符合一般更新必要靜態條件，**尚未在手機執行保資料更新**。CI使用不同Debug key，其產物不得冒充可更新公開Alpha的下載檔。

不生成替代Release key、不讀出私鑰／密碼、不卸載／清資料。若 owner 選擇不同正式certificate，必須先決定 application identity、Android signing lineage支援及資料保留方案；不能承諾改cert還能一般原地更新。回退source可正常revert；回退已安裝新版需另建更高versionCode、相同signer的修正包，不能要求降級／清字典。

## 下一個最小任務

先以本輪 H1 raw 定義 test-only capture 診斷：為何 `/dev/tty` 只有提示而無XML、host timeout與display freeze的時序關係。保留原 assertions與bounded stop；先取得可反證的根因及RED/GREEN，再提出一次新健康環境runtime計畫。不得直接重跑完整Binding A/B。

Formal signer決策、實機owner授權、D2D policy與完整license inventory各自關gate；未通過不建立新Release。S2／M4／ASR／Windows voice皆不在此輪。
