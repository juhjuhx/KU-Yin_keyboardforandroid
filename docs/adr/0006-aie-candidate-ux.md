# ADR 0006 — AIE 首個候選瀏覽切片

- Status: PROPOSED / AWAITING_USER_DECISION
- 日期：2026-10-11；基線3072aff／產品9f5db67。
- Problem：固定44dp列中preedit/工具擠壓候選；只有當前backend page可捲，UI未接翻頁/展開。
- Evidence：深讀報告§4；ledger AIE-01/03；UI:220–255,444–568，session:73–85。
- Recommended：單列＋點擊展開當前頁多列網格＋前後頁；展開暫時取代主鍵區，收合恢復。preedit始終可辨、展開可讀完整；不改主題/大千/decoder順位。
- Alternatives：常駐兩排（增加IME高度）、可配置候選區（增加state/test複雜度）。
- Impact：只接既有paging與UIviewport，selection仍同snapshot；不增加nativeplugin或個人store。
- Reversibility：UI slice可以local revert；無data migration。
- Question：第一片要「單列＋展開網格」、「常駐兩排」或「可配置區域」？
- Approved answer：尚未收到。沒有accepted ADR或first approved implementation。

此提案是功能方向，尺寸/字級、無障礙與gesture需在批准後用spec及測試確認。沒有新圖稿；沿用Compose品牌。
