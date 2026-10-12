# R1 獨立審查

基線a6a4c65；以actualsource/diff/backup evidence review，沒有把作者摘要作approval。reviewers先唯讀後各scope final；Host作者不能自評自身補丁。

## 第一輪 source findings 與處置

| Severity / location | Finding / evidence | 處置 |
|---|---|---|
| MEDIUM AGENTS架構/README C12/SECURITY_AUDIT | 舊fallback-production/native-target、舊resetHIGH/signing漂移 | R1規範入口/source-grounded10章更新；原文Git歷史保留 |
| MEDIUM project_doctor.py:38 | app/.deps和bootstrap/CMake actualstage不同 | 有效RED2fail→GREEN2pass；commit8e3482a |
| MEDIUM acceptance-host driver/P0/Btests | 舊same-rowselector、stale外側read、選字不提交舊契約 | separatefix/r1-host-fixture-adapter，currenttyping預期保持、原archives不改 |
| MEDIUM data_extraction_rules.xml | OEM D2D backup範圍不能以allowBackupfalse泛化 | 文件明示OPEN；不偷偷改product/privacy政策 |
| MEDIUM native installer | 既存非空systemdict無revision/hash更換 | inventory/風險記錄，未稱目前損毀；futureisolatefix |
| MEDIUM feedback/entity/UI | exportJsoncontrolchars、delete/clear無ACK即success、Issueversion硬編 | 現存產品followup，R1未改DB/behavior |
| MEDIUM manual prerelease workflow | freshCIkey/onlyAPK+SHA，未source/relink/update-safe流程 | 保留OPEN與發布禁用判斷；本輪不新release |
| LOW docsstate/error/testscope | externalcursorreset不關inputgate；insertACK不同delete；不存在tracker類；fallback在Kotlin | reviewer發現，逐項按source修正文檔 |

## 獨立裁定（本輪已完成的來源／保存部分）

- r1_architecture_audit：重讀三處修正後APPROVE architecture substance；無新增productfinding。nativeACK/editorACK、snapshotidentity、scratchplans、fallbacktrial、futureboundary符合source。CI補Python/10contracts與XML正確；其P3版本同步已寫為baseline7→R1all10（actualdoc再次核對）。native/Gradle/remote/runtime不在此approval範圍。
- r1_storage_build_audit：APPROVE doctor/docs/保存。独立重算569SHA全部match；536material原始/副本bytes一致；15原工區HEAD/status/patch/untracked不變；4restoreclone逐項吻合，兩bundleverify exit0。privateexcluded未進material，TAUIexactbytesKEEP。
- r1_fixture_audit：先source-grounded故障classification、後獨立worktree作者；finalpatch必交其他reviewer，不以作者GREEN當reviewAPPROVE。

## 原有品質限制

歷史host15PASS/9FAIL/8SKIP維持；timeout唯一根因未證，沒有删assertions求green。physical/OEM/TalkBack/erase/licenseruntime完整性仍OPEN；production NOT_READY、M4 NOT_VALIDATED、S2未開始。最終gates和remoteSHA以VALIDATION_MATRIX/FINAL_HANDOFF為準。

OCR v1.12.13 / Anthropic401 invalidx-api-key / request req_011CfwdzDtFeoobZcX43CyS1 / exit1；BLOCKED_BY_PROVIDER，沒有OCR APPROVE。

## Supplemental stress verdict

r1_storage_build_audit獨立讀probe/source/log/manifest：180秒timeout不阻擋docs/doctor/CI治理merge，probe未改且非必要CIgate；必須明列NOT_COMPLETED，不稱stressPASS。四compile步PASS與nativeJVM4＋6短流程PASS不同scope。只有最後輸出JSON，無法定位停在哪案，禁止聲稱longstream是已證根因。traceback/manifest保存於外部native-probe.log與build/r1-evidence/native。

## PR／fixture 追加審查

PR #9 actual range a6a4c65..c590d364 在正常 merge 前由 architecture及storage/build reviewers核 source／privacy metadata／doctor／CI diff，無 High，已合併 bf7c5d1。

`r1_architecture_audit` 對 fixture 087ff39 的9檔、71測 XML與composing interval replacement實作核准；whitespace regex finding經原始字元查驗屬false positive並正式撤回。後續 frozen/current provenance 実際 patch 也取得該 reviewer scoped APPROVE：8份source exact bytes 53,224；missing硬FAIL、tamper與unsupported shape拒絕；current app5／host32 exact set／count／unique。Storage reviewer核准方案，final turn usage-limit未完成actual patch approval，不偽稱其追加核准。

Source-level UIAutomator2.3.0成本已核：UiObject2 getters、hashCode／equals會呼叫getAccessibilityNodeInfo → waitForIdle＋refresh；equals可能將stale轉false。首輪runtime15PASS／17SKIP不能說修好；下一個窄fixture capture切片與CI證據單獨判定，不變產品或放寬strict assertions。

## 最新 runtime 阻擋與安全處置審查

`r1_architecture_audit` 重讀 run38254806233 raw XML／log與fail-fast source，核實App4PASS／1FAIL為第一次visibility（recreate未開始），Host task未執行；CI真正checkout `42fb9afb59b54e8e0e11859f508ba554d939379a`。reviewer **APPROVE bounded disposition**：PR10 HOLD，不盲retry、不改App/assertions；已合併PR9的文檔／工具交付可正常完成。沒有把source不變推論成emulator flake，也沒有將未跑Host記成skip／PASS。
