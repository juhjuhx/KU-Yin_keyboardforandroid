# P1-D 獨立最終審查

FINAL_REVIEW_STATUS: APPROVE（本機靜態／封存驗證範圍）

P1D_CODE_SHA: 75b93a3b68867fd6ec4c6adadd3e279d71e98104

PATCH_SHA256: 401484b25829a350164975a9a48718e80768a5cc46a49eefec94b97379811b90

完整patch與8檔指紋：final-green/review.patch、review-manifest.json。GREEN後使用requesting-code-review派出兩個fresh-context唯讀reviewer；code-review-excellence含Kotlin guide與official-code-review高訊號規則用於本機diff。本任務沒有PR，省略PR狀態／GitHub留言流程，不使用不可用Claude模型別名；沒有外部留言、push或merge。receiving-code-review要求先核證因果，診斷期間已否定未證實的nested-view/graphics候選。

## p1d_policy_review — APPROVE

獨立核對固定patch、8檔指紋與11個protected base檔。政策優先序、custom id含0、PREVIOUS、immutable snapshot、三布局presentation、composition-first/orderedCommits、單次editor effect均符合需求。核對官方EditorInfo/InputConnection文件。

服務dispatch不再重讀raw EditorInfo；NO_ENTER_ACTION壓過std/custom；沒有performEditorAction false後newline fallback。未發現可確認的新重大bug或AGENTS違反。唯讀核對A–H封存XML/logs；未重新執行Gradle。

## p1d_ui_test_review — APPROVE

核對service102–103/214–227、UI三布局bool接線與245–247 Enter順序。真實service.onCreateInputView已掛載；composition測試先點注音並確認非空preedit，第一下completion、第二下action/newline。Recording JNI搭配真ChewingEngineSession；ordered輸出fake只提供session result，仍驗證真UI→service→InputConnection接線。Service private-method reflection由UI接線測試補足。

cleanup沒有吞錯、跳測或改assertions。3類minimalRED→只drain writer主looper的GREEN與ordered40支持修正；Dachen測試與base原文一致。獨立重算RED23測16個實際assertion failure，final full40類190測0failures/errors/skipped。未發現introduced高訊號bug或明確規範違反。

## 已處理的測試問題

idle_diagnosis恢復工具後獨立讀源碼、Compose1.7.0／Robolectric4.16.1位元碼與實際XML，確認reset遺失global snapshot dispatcher callback而sent/scheduled旗標仍true的可重現機制。無service/native/Enter/Dachen的三類minimal亦重現；單一main-looper drain控制解決。Nested ComposeView、native→legacy或複雜layout非必要條件，不列為產品root cause。詳04與idle-minimal-red/green。

## OCR

VERSION=v1.12.11 (a758d9cb)。唯一provider health401 authentication_error；BLOCKED_BY_PROVIDER，SESSION=none。沒有retry、外露／修改金鑰或宣稱OCR完整掃描成功。由兩份獨立本機審查補足本slice審查；不冒充provider結果。

## 範圍與限制

Stage0 p1abc_audit APPROVE仍有效；其既有Gate readiness close+Owner shutdown再close caveat詳00，P1-D未改此baseline。Release因既有keystore缺失受阻；沒有Android targets，runtime NOT_TESTED。APPROVE不代表device acceptance、production readiness、merge或push授權。
