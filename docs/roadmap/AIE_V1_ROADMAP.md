# AIE V1 Roadmap

日期2026-10-11；S0/S1 已批准（Option1），實作／驗證狀態見 S1 交接；S2–8 仍 PROPOSED。沿用P0-R，不重建／不換decoder／不改visualidentity。先候選，再品質與學習，再touch/symbols；每片獨立gate，不因compile自動進下一phase。

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

## S0/S1 已完成的最小工作鏈

- [x] Source84dcf9a/P0 lineage、全部12原worktree與frozen APK核對；fresh independent source audit。
- [x] S0正式reset UI有效RED、unsupported/no-deletion修正、232測/native近鄰GREEN、限定安全APPROVE；localcommit ea5f185。
- [x] S1從S0HEAD隔離；真pin native API分析、scratch plan metadata、immutablepage/ref與stale事件RED/GREEN。
- [x] 正式UI單列/展開/收合/前後頁、真overflow pointer/跨世代/320360600/landscape/fontScale；不改ranking/Dachen geometry。
- [x] 真host JNI4分頁案、真native/service/EditText6案；App265/Host37（實際rerun）/Python104/contracts10；bootstrap/nativeprobe/Debug/instrumentationbuild。
- [x] 兩位獨立source/hostreview APPROVE綁39acf96；新 Debug20003/0.2.0-aie.1、source/version/hash/signer/手冊，原APK保留。
- [ ] 使用者手機候選瀏覽與P0-R打字復驗；RUNTIME_NOT_TESTED，未安裝/刪除個人資料。

完整 learning erase 不是S0已支援功能：需要另審nativeowner排他／flush/drop error／全storetransaction／reinit／rollback／敏感與migration政策；當前只是停用不安全入口。S2以後未批准，不混入本片。

## 後續決策順序

A候選UX → B個人化default/reset/export與touchopt-in → C literal/conservative/aggressive correction → D維持license與可用資料 → F symbols/kaomoji/search → E GitHubalpha/F-Droid/Play渠道。

每次只問一群；已approved不重問。userdata、architecture或license難以反轉時才寫acceptedADR；ADR0006已記錄 Option1 accepted；後續決策未批准。

## S0/S1 本輪裁定

S0 的完整刪除不支援，以 unsupported/no-deletion 修正誤報；S1 從通過安全 gate 的 S0 commit `ea5f1853b3d4aba9f6c0deb1fbcd0e092b1cdac8` 開始。上述早期工作分解保留規劃來源，最終命令／測試／版本／APK／剩餘問題以 `../review/aie-s1-candidate-browser/06-handoff.md` 為準。

下一步先由使用者實機復驗候選瀏覽與 P0-R 打字；AIE-S2 取得／排名仍未批准。沒有自動執行學習／touch／symbols／M4 或 release。
