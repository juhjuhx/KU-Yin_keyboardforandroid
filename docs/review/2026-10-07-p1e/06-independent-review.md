# P1-E 獨立審查

## Host oracle 初輪 code review

Reviewer：獨立子任務 alpha_acceptance_review/apk_evidence/certificate_check；未參與 P1-E 實作。
範圍：acceptance-host main／pure tests／shared matrix、Gradle module／settings／catalog。
裁定：APPROVE（限定以上範圍）；0 Critical／Important findings。

逐檔確認：
- RecordingEditText：super 真實 IC → platform snapshot → 真正 outAttrs 四欄覆寫 → final；null 原樣返回，非 null 才包 wrapper。
- action 唯一 recorder、配置 boolean、不 delegate；文字與 composing／selection／deletion 仍走平台。
- state 同步、不可變、revision 鎖內更新、observer 鎖外發佈，render 回 UI thread。
- newline 取真 currentText 的 LF；commit observation 不製造文字。
- D17 純邏輯核對 host false／一次 ID=4 action／newline=0，沒有宣稱 false 已經跨 IPC 回 IME。
- shared matrix 不進 host main；沒有產品 module／Compose／native／network 依賴；獨立 testOnly helper。

Reviewer 直接解析 RED／GREEN XML：28 測／25 failures → 28 測／0 failures／0 errors／0 skipped，testcase 名稱一致。RED 含 20 個 assertion／comparison failures 及 5 個空矩陣 runtime failures，沒有以 missing class／compiler error 充當 RED。
Scoped diff --check 與 XML 解析 exit0。Reviewer 未跑 Gradle／ADB／OCR，build log 是 root 已執行的原始證據。

## 最終整合審查

裝置矩陣 reviewer：p1e_upstream_review（未寫 Android 測試）；runner reviewer：certificate_check（未寫執行器）。各自只讀獨立範圍，root 執行整合 gates。

| Finding | 等級／證據 | 修正與覆核 |
| --- | --- | --- |
| B 類 observable PASS 誤升 full acceptance PASS | Medium／高信心，reviewer pure repro | 新增B01–03 scope limits；RED23測／3subcases失敗→GREEN23；覆核通過 |
| genuine cleanup failure 被原 inconclusive 降成 skip | Medium／高信心，source分支直接可證 | test-only pure merge：RED9／2fail→GREEN；共享 classifier、防止非poll Stale分類漂移，保留error_details/suppressed；矩陣最終APPROVE |
| select primary失敗遇restore失敗時主因不見 | Optional／fake subprocess重現 | RED24／1fail→GREEN24；保存兩因select-result與console，安全restore決策不變 |

矩陣最終裁定：APPROVE（限定源碼），未見剩餘High／Medium。Driver SHA256=29e55f50702f352f95f083e58cb7e71fbc79b4e10297a07e1b0a821cbc7516a4。
逐碼確認D01–D18、D16文字與icon差異、D17IPC限定、D18兩variant三布局、C05雙preferred／effective mode、TYPE_NULL honest gap、真候選與preedit selector、有界同步、唯一case JSON與重新拋錯。
Runner最終裁定：APPROVE（限定runner／模擬測試／08、09文件），無未解Critical／Important／Optional。
Reviewer新鮮執行24/24 pure／fake tests、exit0，另核對兩份RED。固定serial、signature STOP、其他IME變更STOP、run finally restore、scope缺口及missing case NOT_TESTED均維持。

Root整合裁定：APPROVE_FOR_FIXTURE_PREPARATION。此為test-only基礎設施的源碼、編譯、JVM與artifact覆核，不是runtime／physical驗收APPROVE。
Runtime／真實 IC／跨程序 selector／rotation 皆 NOT_TESTED。

## OpenCodeReview

v1.12.12 (182898c)，唯一 health attempt exit1／provider anthropic／HTTP401 invalid x-api-key。
OPEN_CODE_REVIEW_STATUS=BLOCKED_BY_PROVIDER。未重複相同認證重試；沒有 OpenCodeReview PASS。
