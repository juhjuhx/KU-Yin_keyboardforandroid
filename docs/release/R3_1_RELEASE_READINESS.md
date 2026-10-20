# R3.1 Release Readiness

更新：2026-10-20。`RC_READY=false`、`STABLE_READY=false`、`RELEASE_VERDICT=NO_GO`、`RELEASE_URL=NOT_PUBLISHED`。

2026-10-21續作：遠端main已因PR18的README／community文件更新為575cc883；PR18已MERGED，產品PR20仍Draft，原Runtime／Release裁定不變。文件保存與磁碟可寫狀態已恢復，不能因此解鎖發行。

## Critical gates

| Gate | 裁定 |
| --- | --- |
| H1 真 editor 擷取 | 新CI39426302308 H1 INCONCLUSIVE／null root／timeout；不是修復完成 |
| H0→H1→H2／App5／Host33／32-case／B01 | 未完成，不拼接歷史 runs |
| 核心 physical acceptance／QR scan／資料保留更新 | NOT_TESTED，需指定裝置 owner 授權 |
| 正式 signing／key custody | BLOCKED；既有 local Debug signer 相容不等於正式簽章方案批准 |
| privacy／OEM D2D／完整 learning erase | OPEN；backup exclusion 不是加密，完整 erase 仍 disabled |
| native LGPL/Rust runtime/file-level notices | 原 relink materials 保留；完整法律／發行核對仍 OPEN |
| 最終整合來源與 Runtime 獨立審查 | test-only 部分限定 source review，不代替產品或整體 release 批准 |

## 整個 PR20 的獨立審查

Reviewer 實讀 main63→de2d9df 的整合 diff（125 files），不是只讀 QR。原報告保留於外部 `R3_1_ENTIRE_PR20_INDEPENDENT_REVIEW.md`。

- M1 MEDIUM：RC guard 缺少 runtime orchestration 保護。本輪新增固定 b982 runtime 及 committed/dirty 反例；health 只批准已審查的 97645c，其他 infra 保持 e137。來源 gate 已通過，不宣稱 Android runtime 通過。
- M2 MEDIUM inherited：既有手動 prerelease publisher 可產生不同 CI Debug signer，沒有完整 release gates。沒有執行 publisher；發行前必須另行收束，不由本輪擴大修 publisher。
- 本輪沒有新產品／JNI／字典／Room／preferences／signing 變更。已發布 v0.2.0-aie.1 與舊包不動。

OpenCodeReview 沒有向未批准 provider 上傳 source/log；沿用歷史 `BLOCKED_BY_PROVIDER`，沒有新 401 健康重試或偽造 APPROVE。規格與工程品質的限定審查、Runtime／實機／license／signing gate 分別裁定。

截至原2026-10-20審查，PR10 HOLD、PR12 Draft、PR18獨立review、PR20 Draft；截至2026-10-21 PR18已merged，其餘不變。本輪沒有自動合併產品／測試PR，沒有新 release、Stable、S2/S3/S4/S5、M4 或 ASR。

最新整合 executable `feab29f53583c4bb8dfc4cffe13243a36a3e9d06` 的一次有界 CI：Python212與contracts10 PASS、H0 HEALTHY、H1 INCONCLUSIVE。沒有執行 App/Host/B01，不借用歷史綠燈。原 Android root/binding failure 尚未解除，不能開新 Release。
