# R2.1 獨立審查

本頁記錄真正執行的scope，不繼承舊APPROVE，也不把作者的audit當獨立審查。

## 分工與直接證據

- CI reviewer獨立完成d6 finite183source hashes、App5＋Host2 protocol/XML、JVM272PASS10nativeSKIP＋Host107與synthetic merge/tree；其後quota中斷。沒有它的full33 APPROVE。
- Lifecycle reviewer另從raw XML／testlog重新算normal38761725033：5App／33Host完整identities，32owner envelopes、terminal、checkout3b5e084與d6 tree一致；與作者結果相符後裁定 **APPROVE_TESTINFRA_RUNTIME_GATE**，不是full acceptance/merge。
- Privacy/preservation reviewer直接審B01七個程式檔、原33identity／frozen26、原始9份HostXML123/0/0/0與17真compile/unit log，批准b1b5bf6 source-only local commit／正常push供finiteCI；沒有批准尚未執行的runtime。
- 原生／平台八gap由另一source pass核actual private owner、內部reset路徑、JNI getter與exact API33官方來源；不是拿UI模式代替學習／context。兩份新ADR/constraints另窄審無重大事實錯誤，僅接受PROPOSED狀態。

## Findings與處置

| Finding | severity／位置 | 處置與證據 |
|---|---|---|
| 原NativeCase4外層reset oracle漏internal restore/reset，baseline在selection後且未拒unavailable | High／`NativeCase4Test.kt:14–19,58–74`；`AndroidChewingEngine.kt:435–444`（d6） | no-reset證明力缺口；本輪未改App test或產品，不把既有PASS當B02/B03完整證明；留observer ADR，非已重現產品defect |
| transition後selectMode真切換可能遮蔽回退 | Medium／B01 driver | 改只讀awaitMode，未派額外mode切換 |
| transition後拿新prefix可能掩蓋duplicate/text drift | Medium／B01 driver | before/after固定零effect oracle；4negative assertion RED→GREEN |
| 41鍵分時採集混layout | Medium／B01 driver | 同一completed capture＋前後geometryepoch；不聲稱平台atomicity |
| coverage不足誤標FAIL／真clip被後續gap遮蔽 | Medium／geometry oracle與caller | 每個已觀測frame立即驗真幾何；最後驗跨frame coverage；缺證INCONCLUSIVE、違約AssertionError。11/12兩FAIL保留，12b/17真GREEN |
| 我候選可能掩蓋錯phonetic | Medium／B01 | tap前strict `ㄨㄛ`；原ㄕ/ㄖexactpreedit不變 |
| 全候選列表可能含真裝置個人詞 | privacy scope | 新紀錄僅固定我target bounds／count；不記其他詞；只用freshsyntheticCI，實機另計畫 |
| C04把81completed彙總說成81份逐筆全空 | evidence precision／歷史文字 | 本輪只述last_node_count=0＋計數；無逐筆snapshot證據，不重寫歷史forensic或推policyPASS |

B01 source final unresolvedHigh/Medium=0；上述NativeCase4 High仍存在於更強native no-reset acceptance範圍，不能被source批准消除。

## 外部收據

原始證據目錄：`KU-Yin-R2.1-Verification-2026-10-16/`。
- `B01最終來源審查與有限CI推送收據.md` SHA256 `da71e06a7ca52e92bc986807e7ca19a28b95b1e24c1d7f083cf1cb8b1db3e411`
- `完整normal-runtime獨立審核.md` SHA256 `6632355cca5861aa2cd3fadec01a190220f5857b61a56696c693f7e8d792c7b6`
- `完整runtime與平台窄審最終scope收據.json` SHA256 `f6e4f482dbc9bb0c807bb16cfe32b22be441da037c3e228287ea7aaaaab51b0a`
- `原生與編輯器八項缺口獨立研究-sourcehash收據.json` SHA256 `e2350f8cd22c2070d8e942ed991fb7ff3ef4180dfad4966b279c7ff49ea1406b`

- `B01-attempt2獨立判讀scope收據.json` SHA256 `0c8007c10256fb6544259b1f6210999d914dd83abaf9c9b54a79026b09870188`（新attempt2失敗獨立裁定，非runtime批准）

報告中的source bytes／raw logs／官方excerpt來源可由各JSON重算。本頁不複製私人內容，未執行實機、沒有production/JNI變更。

## OCR／官方pipeline

OCR實際binary1.13.0；單次health命令被automatic approval review拒絕，原因是使用本機認證向未明確核准的外部provider傳測試對話，**未執行**。`BLOCKED_BY_APPROVAL_REVIEW`；歷史1.12.13 HTTP401獨立標舊證據，無freshprovider結果／OCR APPROVE。未繞過或反覆重試。official-code-review指定模型harness不可用且PR12為Draft，未冒稱完整官方pipeline。

## 合併建議

PR12 exact d6的限定testinfra CI/runtime gate通過；仍Draft，不由九個INCONCLUSIVE自動換取full驗收或merge。PR10 ea5是d6 ancestor（0/14），patch已被PR12涵蓋/後續修正supersede，不重複合併舊patch；PR10 HOLD。沒有main mutation/release。R2.1新B01 runtime須單獨覆核後才給case verdict。

## 新B01有限attempt2的實際阻擋

source185hash一致；App4PASS1firstVisibility原assertionFAIL，Host/B01未執行，不能給B01新PASS。show前window/view focus、attached/token與真IC1，原show已通平台served gate；失敗時遠端method/session null。不能用舊early-show或before-app remote-null單獨解釋。五真failure dumps首次非空實證保留。Lifecycle reviewer已獨立核原XML／log／live採證，根因仍UNRESOLVED，後續限定App binding前提；不批准merge或新geometry runtime。外部 `B01-attempt2首次可見性失敗獨立判讀.json` 保留完整protocol／五dump／source／平台因果分類；其runtime gate為HOLD。

R2.1 source批准與文件待證快照批准，不消除這次真AppFAIL；沒有把失敗改SKIP、跳前置或retry至綠。九gap完整狀態仍INCONCLUSIVE，PR12應暫不merge，PR10HOLD。

新runtime **High（current CI gate）**：原App first visibility FAIL阻擋Host/B01，未解；**Medium（causal evidence）**：遠端binding/process事件窗口未完整，根因UNRESOLVED。這兩項不等於七個新test-only source有已確認產品defect，也不能用source APPROVE消除。最終doc-only批准只批准如實記錄FAIL與界線，不批准執行gate。
