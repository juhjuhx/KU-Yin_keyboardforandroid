# AIE V1 Roadmap

日期2026-10-11；PROPOSED、FIRST_APPROVED_SLICE=NONE。沿用P0-R，不重建／不換decoder／不改visualidentity。先候選，再品質與學習，再touch/symbols；每片獨立gate，不因compile自動進下一phase。

| Phase | Entry / 決策 | Deliverable / TODO | Evidence / Exit | Rollback |
|---|---|---|---|---|
| 0 審查 | P0來源/APK核對 | 深讀、issueledger、upstream/license、architectureoptions；獨立review部分完成 | source/contract核對；使用者UX答案＋待補完整independentreview | docs localcommit可逆，不動product |
| 1 候選瀏覽 | UX ADR批准＋S1 spec批准 | currentpage strip/expand/prev/next；preedit限寬；scroll/tap/state | RED→GREEN Compose/nearby native/editor；大字級/多頁/實機瀏覽無lost/double/stale | 整片localrevert；無datamigration |
| 2 取得／排名 | 第一片實機可用；CandidateRef contract/replay corpus | pool/page/display可觀察；個人preview只讀spike；identity-preserving ranker，透明score | recall@K/MRR與延遲基線；候選/原音序不變，差分replay | baseline ranker可切回；不換nativepins |
| 3 個人學習 | reset HIGH先關閉；privacydefaults/schema ownership批准 | native＋supplemental分工；ACK event去重；bounded/decay；reset/removal/migration/recovery | 敏感零writes、restart persistence、erase驗證、corruption/migration與ACKfalse tests | schema先備份/recovery；不能只gitrevert資料 |
| 4 觸控適應 | literalgeometry實測；correction/consent批准 | geometry oracle→nearby scores→bounded spatial/context resolver；central穩定；TalkBack保留 | 座標boundary/center/gesture、baseline correction品質；實機雙寬度/方向 | literal模式defaultfallback，無未經批准的rawlogs |
| 5 符號 | source/license與UX批准 | attributableSymbolCatalog→keywords→recents/favorites；kaomoji自有或明授權資料 | UTF8/VS/ZWJ/RI/invalidsurrogate/search/duplicate/persistence/editor tests | versionedcatalog可退，userdata兼容 |
| 6 多裝置／效能 | 前phasecorrectness通過，測量plan批准 | API/ABI/OEM/rotation/keyboard/editor；baseline後才定threshold | 實際metrics/原始證據、沒有correctness變差；完整acceptance裁定 | 回退惡化實驗；不跳M4未批准工作 |
| 7 Mainline/CI/release硬化 | high/blocker關閉；integration/signing/licensechannel批准 | 乾淨sourceoftruth、requiredCI、native/dictionary notices、CONTRIBUTING、updatecompat | freshCI/實機、signedprovenance、LGPL source/relink包；不能把Debug當release | localintegration可review；公開ref操作另授權 |
| 8 公開Alpha/Beta | 明確publicrelease授權＋readiness全gate | 版本化APK/source/licenses/privacy/changelog/support渠道 | 平台發布/下載更新驗證 | 保留相容上版與已定migration；不刪userdict |

## 第一片可批准的工作分解（沒有啟動實作）

### S1-1 當前頁前後頁接線（預估中，3–4檔）

Files：`ime/ui/KuYinKeyboardUi.kt`、建議`ime/ui/CandidatePanel.kt`、新`ime/CandidateBrowsingUiTest.kt`；session保持唯一owner，必要時僅增UI參數。

- [ ] 先寫fake session >10項、pageflags/邊界與candidate index事件的UI test；驗證現版無visible paging/command而RED。
- [ ] 加明確paging control，傳現有PageForward/Backward；無下一頁不可派送，projection失敗不得無限跳頁。
- [ ] 同snapshot selection與editor exactlyonce、decoderupdate後staletap拒絕；不把跨頁index合併為global ordinal。
- [ ] focused GREEN＋independentdiff review＋localcommit；不得此處重排候選或換nativepin。

### S1-2 展開／收合與可用寬度（S1-1後；3–5檔）

- [ ] 寫expand不commit、不改phonetics；collapse恢復鍵區；drag不candidateclick；empty/restart/editor切換state測試，觀察RED。
- [ ] 依ADR實作同theme網格＋boundedpreedit與可訪問controls；不新建session、不readEditorInfo。
- [ ] 320/360/600dp、fontScale1/1.3/2、短/長preedit/phrase測試；expansion與pagechange不丟candidate。
- [ ] nearbyP0 no-tone／我現在／English／Enter／nontext/stale regressions、真nativehost與Debug/Androidtests compile；serialGradle。
- [ ] 新版本APK provenance＋授權裝置真scroll-expand-select/editor；無device則保持NOT_TESTED，不宣布體驗PASS。

### R0 重設學習的獨立修正（可另批准，先於Phase3與release）

- [ ] 用tempstore＋現有resetUI寫RED證明native仍在。
- [ ] 審查native owner排他reset/close/reopen、安全取消pendingcomposition、全store成功/失敗UI策略。
- [ ] 只在使用者按confirm時重設選定scope；完整write/verify後才成功通知，failure不eraseotherstores。
- [ ] failure injection／restart／native和fallbackstore／sensitivepolicy GREEN與獨立安全review；本輪不刪真資料。

## 後續決策順序

A候選UX → B個人化default/reset/export與touchopt-in → C literal/conservative/aggressive correction → D維持license與可用資料 → F symbols/kaomoji/search → E GitHubalpha/F-Droid/Play渠道。

每次只問一群；已approved不重問。userdata、architecture或license難以反轉時才寫acceptedADR；本輪ADR0006為proposed。
