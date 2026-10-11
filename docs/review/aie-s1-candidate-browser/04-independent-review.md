# S0/S1 最終獨立審查

S0：`/root/s0_initial_audit` 直接核對 reset diff、實際 pinned archive 與 RED/全量 XML，裁定 **APPROVED_FOR_UNSUPPORTED_MITIGATION**。完整 erase 沒有獲得支援／驗收；詳 S0/02-independent-review.md。

S1 兩位獨立唯讀 reviewer：`/root/s1_initial_audit`、`/root/s1_ui_final_review`。實際讀取 commit/diff、測試源碼、XML、APK、native section hashes、建置紀錄，不只採信作者摘要。

SOURCE_SHA: `39acf96c6cfc058106315f3553e1c8b88438f4f7`
BASE_SHA: `ea5f1853b3d4aba9f6c0deb1fbcd0e092b1cdac8`
FINAL_VERDICT: **APPROVE — S1 來源與 host verification 限定**，沒有未解 HIGH。

## 發現與處置

| Review issue | 處置與直接證據 |
|---|---|
| strip 重用 node，down→snapshot更新→up 可選到新候選 | 有效 pointer RED，item key 加 snapshot revision；strip/grid negative 與正常 tap 綠 |
| 兩個短字的 swipe 沒有真 overflow | 改成 overflow long candidates、chip起手，檢查 scroll offset 真增加且零 selection |
| 大字級高度／landscape占高 | Native graphics 56dp 高度 RED→adaptive strip；grid 按高度有界縮小，320/600/font/landscape 綠 |
| hidden symbol Enter/delete bypass UI input gate | 有效 symbol Enter RED；service action/delete boundary gate，hide 與 view-start lifecycle 綠 |
| stale ref 負案例同時含多個錯誤，不能證每欄位 | 從當前合法 ref 只改 generation/page/provider/identity 單欄位，selectionCalls保持0 |
| font overflow oracle 不適用 LazyRow Infinity | 兩 reviewer核真 1.7.0 bytecode/XML；改用 height、ellipsis、完整 visibleEnd＋glyph bounds；移除無理由 minwidth |
| 舊 recovery fixture缺 view-start callback | 核固定 AOSP Android7/16 路徑，補callback、保留原 assertions；新增 gate reopen 案 |
| 文檔宣稱長 preedit 完整視覺顯示 | 改明示 bounded/maxLines2/Ellipsis，完整字串保留於 semantics |

## 審查證據

- source commit20檔逐 byte 與已審工作檔一致，`git diff HEAD -- app` 為空。
- App54類265測、fresh Host3類37測，failure/error/skip全部0；UI12、lifecycle4、nativepaging4、nativeeditor6皆綠。
- Debug與兩 instrumentation build exit0。Release exit1，原有 `my-upload-key.jks` 缺失，沒有 PASS 或新 signing key。
- APK 30,364,775 bytes，SHA256 `acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634`；版本20003/0.2.0-aie.1。
- native adapter只有metadata增補；四 ABI 的 JNI `.text/.rodata` 與P0相同；全部assets相同，JNI/nativepins/ranking/learning策略未變。

## 剩餘範圍

English 跨prefix pointer 是 BASE 既存 index-node/lambda 更新風險；guard 可拒 captured舊callback，不保證 down→prefix update→up 身份。記入ledger，未在本片改English後端，也未宣稱已修正。完整reset、personal preview/ranking、TalkBack/OEM、physical驗收與release仍 OPEN。這些不以 host compile/test 改寫為 PASS。

## OpenCodeReview

本輪版本 v1.12.13（fabbdb29）；`ocr llm test` exit1，Anthropic HTTP401 invalid x-api-key，request `req_011CfukguqdyTTrfSRGwUSxd`。**BLOCKED_BY_PROVIDER**，無review session/approval；未重試相同認證，未讀／輸出密鑰。官方 PR review skill載入但本輪沒有PR，不發布review comment。
