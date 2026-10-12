# R1 驗證矩陣

ACTIVE；逐個gates待最後fresh命令補登。未執行不借歷史PASS。

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
| Hostadapter unit/instrumentation | FRESH in progress | RED verified；GREEN pending | separatefixturebranch，非runtime |
| Release build | FRESH | BLOCKED_BY_SIGNING / exit1 | packageRelease因my-upload-key.jks不存在；無key生成／data修改 |
| JSON/YAML/doclinks/Mermaid | FRESH | PASS | maintenanceJSON/workflowPyYAML/121links0missing，7chartsmermaid11parserPASS |
| HistoricalCI38164320915 | HISTORICAL | blockingPASS/app5PASS/host15PASS9FAIL8SKIP | 不算本輪新驗證 |
| R1GitHubCI/PRmerge | NOT_EXECUTED_YET | PENDING | reviewer/gates後正常merge |
| Physical acceptance | NOT_EXECUTED | OPEN | 本輪不宣稱裝置PASS |
