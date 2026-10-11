# AIE V1 問題台帳

基線：3072aff／產品9f5db67。2026-10-11。CONFIRMED_SOURCE 為讀碼證據；未標成 runtime reproduction。需求缺口與推測分開。

| ID | 等級 | 發現／直接位置 | 證據狀態 | 最小處置／關閉條件 |
|---|---|---|---|---|
| AIE-01 | HIGH | Compose UI 未派送 PageForward/Backward/Toggle；KuYinKeyboardUi:220–255,422–568；session:73–85已有命令 | CONFIRMED_SOURCE | S1 翻頁/展開真實接線；>10 candidates 可達、snapshot 不 stale、選字一次 |
| AIE-02 | HIGH | 「重設學習」MainActivity:699 只清 fallback prefs（ZhuyinDictionary:1369），native userdict仍在 | CONFIRMED_SOURCE，裝置未執行 | 獨立安全 reset slice，service-owned native close/reset/reopen、全 store驗證成功才通知；不可先直接刪使用者檔 |
| AIE-03 | MEDIUM | 44dp Row 中 unbounded preedit 與 72dp工具共佔寬，candidates weight剩餘；UI:444–511 | 結構已確認，長句/字級量化待測 | 長 preedit/320dp/大字級 deterministic Compose regression，再限寬/換呈現 |
| AIE-04 | MEDIUM | 顯示候選來自 :memory:，native頻率sort預設false；adapter:347、pin editor:101/phrase:246 | CONFIRMED_SOURCE，體驗影響待 replay | personal read-only preview與rankownership spike，不盲開 live cand_open |
| AIE-05 | MEDIUM | 每 snapshot 為當頁候選各建scratch/replay/枚舉；adapter:289–343，同步UI dispatch:161–164 | 成本路徑已確認，latency UNMEASURED | 先量冷/暖retrieval p50/p95、allocation/context counts；不得以觀察當benchmark |
| AIE-06 | MEDIUM | paging/native probe與semantics clicks不覆蓋真正scroll-expand/座標touch；CandidateIdentityUiTest:22–36 | CONFIRMED_TEST_GAP | 增Compose drag/real touch/多頁/decoder更新後舊tap測試；實機補OEM/TalkBack |
| AIE-07 | LOW | QR圖樣是URL SHA bits，DonationCard:123–144，不是QR encoding | CONFIRMED_SOURCE | 保留foundation links與credits；發布前獨立換合規encoder或明示裝飾，需真掃描 |
| AIE-08 | MEDIUM | AGENTS舊decoder、ROADMAP舊D3CURRENT、security summary混歷史 | CONFIRMED_DOC_DRIFT | 以canonical最新sections校正；舊報告不重寫、AGENTS保留 |
| AIE-09 | DESIGN_DECISION | native auto_learn 先於Android commit ACK（pin editor:715、service:286） | CONFIRMED_ORDER；非已證重複write defect | 外部learning只在ACK成功recordonce；nativelearn時點另做pinAPI spike，決策前不雙計 |
| AIE-10 | DESIGN_DECISION | 39符號/40emoji、無search/personal touch/Englishlearning | 確認現況，不是舊測試回歸 | 各phase資料、隱私、license批准後分片實作 |
| AIE-11 | BLOCKER | 發布仍缺完整裝置、簽章策略/CI mainline與nativeLGPL散布完成證據 | RELEASE_GATE_OPEN | RELEASE_READINESS逐項關閉；debug build不是正式release |

## 可執行的隔離重現設計（本輪未新增測試）

- AIE-01：fake session 第一頁10項且canPageForward=true。render UI，尋找可見下一頁／展開，滑至最後後尋找第11項；目前無dispatch入口。新增test應因行為缺失RED，不能以missing import當RED。
- AIE-02：臨時測試目錄放native字典fixture，fallback prefs另存freq；點現有reset，確認UI成功但native值/bytes未清。只用測試資料；正式reset前需建立native owner排他性與active composition策略。
- AIE-03：320dp、preedit20字、fontScale1/1.3/2；斷言候選/清除/展開target可見且可操作，drag不commit。不要先把structure風險寫成已測失敗。
- AIE-04：兩份相同pin字典、只一份有明確訓練詞。native讀取與preview排名分層比較；候選不能被外部重新排序為另一native index。

## 審查紀錄

三名GPT-6.1 SOL/max獨立代理只讀：候選、privacy/state、touch/symbol。收到定位發現後均usage-limit中斷，沒有FINAL_APPROVE。主代理直接核對以上source，完整fresh-context final audit待下一批准slice後補。
OCR health：v1.12.13(fabbdb29)，Anthropic401 invalid x-api-key，exit1，request `req_011Cftij3vNu1bC3An5JZVXj`，session NONE。初次sandbox DNS拒絕不算provider狀態，網路可用後只做一次真正health；不重試認證。

## 2026-10-11 S0/S1 follow-up（不重寫原審查）

- AIE-02：MITIGATED_BY_UNAVAILABLE_STATE；commit ea5f185，正式入口 disabled/unsupported，零刪除／零假成功。完整 erase 能力 OPEN。
- AIE-01/03/06：S1 已接線 snapshot/page/strip/grid，新增行為 RED／GREEN、真 host JNI/editor、320/360/600dp/landscape/font tests；local final gate 與 physical 限制以 S1 handoff 為準。
- AIE-05：有限 macOS host nextPageUpdate samples 已量測（S1 median22.375ms），不是 Android latency 或改善證據，詳 S1/03-performance-baseline.md。
- AIE-04/07/09/10/11：仍 OPEN 或 DESIGN_DECISION；沒有排名、QR、native learning ACK ownership、touch/symbol、public release 變更。
- 新 snapshot pointer 與 hide→finish gap 由獨立 review 發現並實際 RED，於 S1 修正；完整 final review 另記，沒有承襲舊片不存在的 APPROVE。

- EN-pointer（既存 MEDIUM follow-up）：P0與S1 English strip 均為index node重用；英文state guard攔captured舊callback，但不能保證 down→prefix變更→up 的起手snapshot。尚未修／未測完整跨prefixgesture，本片沒有改English session/ranking，不冒稱同中文ref保護。
