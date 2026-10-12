# R1 驗證矩陣

本輪證據逐層列示。CI workflow SUCCESS不代表所有runtime cases通過；未執行不借歷史PASS。

| Gate | Evidence freshness | 結果 | Scope |
|---|---|---|---|
| GitHubmain/tag/release/PR inventory | FRESH | PASS | maina6a4c65/tag2ce與8exactreleaseassets，no writes |
| fsck/submodule/worktrees | FRESH | PASS | primary15＋historicalrepo1；無unreachablecommit；dirty4保持 |
| backupbundle/restore | FRESH | PASS | 4dirtyclonebyte/diff/statusrestore；569hashes0mismatch |
| projectdoctor RED | FRESH | 2FAIL expected / exit1 | oldstageMISSING與obsolete錯OK，非syntax |
| projectdoctor GREEN | FRESH | 2PASS / exit0 | narrowdiagnosticfix8e3482a |
| Python suite | FRESH | 109PASS / exit0 | 含fakeADB，非device操作 |
| static contracts | FRESH initial | 10/10 PASS exit0 | 初次platform描述遺漏已按source校正，contracts-final.json |
| bootstrap | FRESH | PASS / exit0 | freshcache/private/tmp/kuyin-r1-native-20261012 |
| App JVM / host JNI | FRESH | 54類265PASS/0skip exit0 | nativepagination4＋native/editor6全部實際執行 |
| Host original JVM | FRESH | 3類37PASS/0skip exit0 | 非physical |
| Debug/host/instrumentation | FRESH | 全部assemble成功 exit0 | app/host/test artifacts只驗建置，不替換發布APK |
| supplemental native stress | FRESH | TIMEOUT 180s / NOT_COMPLETED | Cargo/JNI/Kotlincompile4步PASS，未有最終JSON；根因UNKNOWN |
| Host adapter unit／instrumentation | FRESH | 5類71 PASS／0skip，instrumentation assembled | RED 20/11、23/3、composition 11/6；後續 scoped-idle 切片另驗 |
| Frozen/current provenance | FRESH | Python 113 PASS／10 contracts PASS | CI fdd8385 RED 109中1fail；8 exact frozen sources＋current app5/host32；原 runner／manifest 未改 |
| Release build | FRESH | BLOCKED_BY_SIGNING / exit1 | packageRelease因my-upload-key.jks不存在；無key生成／data修改 |
| JSON/YAML/doclinks/Mermaid | FRESH | PASS | 2 maintenanceJSON／3 YAML／131 links零missing，7 charts Mermaid11 parser PASS |
| HistoricalCI38164320915 | HISTORICAL | blockingPASS/app5PASS/host15PASS9FAIL8SKIP | 不算本輪新驗證 |
| PR #9 blocking CI / merge | FRESH | PASS／MERGED | run38224799842；merge bf7c5d1，獨立審查APPROVE |
| PR #9 CI JVM | FRESH XML | App265（10SKIP）／Host37（0SKIP）；0failure/error | 本機host JNI10已實際PASS；Linux CI缺host JNI所以10skip |
| PR #9 emulator baseline | FRESH XML | App5PASS；Host15PASS／9FAIL／8SKIP | 保存原始 connected XML；不以非blockingjob overall success遮蓋 |
| Host 087ff39 emulator | FRESH HTML/log | App5PASS；Host15PASS／0FAIL／17SKIP | 舊workflow無XML；全部new gaps仍未驗證；與原9FAIL並列保留 |
| PR #10 ea5c2b5 CI | FRESH XML | blocking buildPASS；App4PASS／1FAIL；Host NOT_EXECUTED | run38254806233；IME visibility assertion失敗；根因UNKNOWN，不盲retry |
| Host scoped-idle local follow-up | FRESH in progress | PENDING local evidence | 只test process capture成本；不改App precondition或strict assertions |
| Physical acceptance | NOT_EXECUTED | OPEN | 本輪不宣稱裝置PASS |
