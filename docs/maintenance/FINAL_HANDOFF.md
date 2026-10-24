> HISTORICAL R1 RECEIPT. 當前入口：[FINAL_AGENT_HANDOFF](FINAL_AGENT_HANDOFF.md)。以下保留原始歷史，不作新鮮證據。

# KU-Yin R1 主線收束交接

## 來源與工程裁定

- MAIN_HEAD_BEFORE：`a6a4c654845338521700121ac06586887d1ec085`。
- 已驗證工程主線：`bf7c5d1d912d77ae8c6c7179d26d72bf835232bc`，正常 [PR #9](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/pull/9) merge；head `c590d36460eb8d08f3473bb2b3dfa199e92b90cc`。
- 本文件屬 `docs/r1-verification-handoff` 的最後文件交付；文件合併後精確 main SHA／PR／merge receipt 記在外部 `R1_FINAL_RECEIPT.json`。不預填自身未來 merge SHA；可用 `git rev-parse origin/main` 與 GitHub PR mergeCommit 核對。
- RELEASE_TAG_UNCHANGED：`v0.2.0-aie.1` → `2ce6810684fd65a65a3006a224dc786b1f3a5f62`；8 release assets 未改。產品 source `39acf96c6cfc058106315f3553e1c8b88438f4f7`；發布APK `acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634`，沒有新release。

**R1 文件／架構／工具與保存工作可交付；Host runtime 修正 HOLD，未接受。** 產品 Kotlin／JNI、native pins、ABI、applicationId、signer、DB schema 與使用者設定未改。沒有正常 merge Host PR #10，不能稱候選 runtime 修好。

## 文件與主線入口

[規範架構](../ARCHITECTURE.md)＋architecture/10章，CURRENT_STATE、DEVELOPMENT、SECURITY_AUDIT、docs/README、history indexes、ADR0007與document authority matrix已由實際source重建；future S2／touch／symbol／learning erase不冒稱實作。Skill manifest區分657個發現條目與實際使用，OCR／特定review管線的BLOCKED另列。

日後使用 `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/mainline` 的 `main`（正常 fast-forward）；原repo root仍是使用者dirty docs branch，不要reset或覆蓋。工程rollback用正常revert；所有舊branches KEEP，不刪遠端／工區。

## 保存與還原

外部位置：`/Users/huang/Desktop/KU-Yin-Engineering-Archive-2026-10-12/`。

- WORKTREE_COUNT_BEFORE／AFTER：15／18（另獨立D2.5 repo不混入）。原15工區所有HEAD/status保持；4dirty的patch、cachedpatch、untracked bytes已備份並在4個隔離clone還原一致。
- 初始branch snapshot30 refs；工程merge後／文件remote推送前snapshot33 refs；最終refs數由receipt真命令量測，不預估。
- 原工區追蹤檔案共6,824 occurrences、untracked10、ignored32,755，全部保留；main起始730檔。536 material references／470 unique backup copies，兩Git bundles，569 baseline SHA核對0mismatch。
- FILES_REMOVED=0，branch／worktree removed=0；TAUI2原檔與native source/relink materials KEEP。bundle僅保存reachable Git objects，不等同dirty或完整objects store。
- 個人userdict／Room／preferences／裝置證據／credentials只辨認，不做私人備份、不進GitHub。DATABASES_UNCHANGED，device-local DB inspection=NOT_TESTED。

外部 raw evidence包含 doctor-red/green、python-tests、contracts-final、bootstrap、gradle-results、native-probe、restoreproof、ci-pr9、ci-host087、ci-host-ea5c2b5與fixture evidence；baseline SHA256SUMS不覆寫，後續證據另有supplemental checksum／receipt。

## 本輪驗證

- project_doctor：2個真 assertion RED → 2 PASS；diagnostic staging path改為bootstrap實際的 `app/src/main/cpp/.deps/libchewing`。
- 主線切片本地：Python109 PASS、10 contracts PASS；App54類265 PASS／0skip（native paging4＋editor/native6實際執行），Host3類37 PASS；bootstrap與app／host／兩instrumentation APK assemble exit0。
- 2 maintenance JSON／3 YAML／131 local links／7 Mermaid parser／diff check通過，精確counts見[驗證矩陣](VALIDATION_MATRIX.md)與raw files。
- PR #9 CI：blocking build PASS；JVM App265含10 SKIP（Linux無host JNI），Host37零skip；fresh emulator App5 PASS、Host15 PASS／9 FAIL／8 SKIP。
- Release build：缺既有 `my-upload-key.jks`，`BLOCKED_BY_SIGNING`；沒有產生key。
- Supplemental native stress：四compile steps PASS，Java180s TIMEOUT／NOT_COMPLETED，根因UNKNOWN，不叫native stress PASS。

## 未合併 Host 切片與阻擋

[PR #10](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/pull/10)保留獨立test-only branch。

1. 新 geometry／完整snapshot／read-only stale retry／candidate completion／actual composing interval replacement：有效RED與Host71 JVM PASS，instrumentation編譯，独立source reviewer scoped APPROVE。
2. Frozen/current provenance：CI run38225839753產生109中1個真 assertion FAIL，原因是原6595frozen ledger被拿去核current source。8份exact frozen source（53,224bytes）獨立保存；原8hash、runner／manifest／APK不變；current app5／host32 exact identities另驗。Python113＋10contracts PASS，獨立APPROVE。
3. 087ff39 emulator run38225649247：App5 PASS；Host15 PASS／0 FAIL／17 SKIP，不能當修復。
4. ea5c2b5 run38254806233：blocking build PASS；App **4 PASS／1 FAIL**，`ImeRuntimeSmokeTest#imeBindsAndSurvivesEditorRecreate` :72 的 `KU-Yin input window should become visible` assertion失敗；**Host NOT_EXECUTED**。實際 CI checkout `42fb9afb59b54e8e0e11859f508ba554d939379a`（ea5c2b5 → bf7c5d1 synthetic merge）；失敗在第一次 show，recreate尚未開始。產品／App測試／pins與baseline完全相同，root cause UNKNOWN，不假定fixture造成或emulator flaky。
5. source證實UIAutomator getter/hash/equals有重複idle/refresh成本；其scoped-idle device-free follow-up另記local證據，未宣稱runtime修好。

不刪／放寬visibility或strict preedit／exact commit assertions，不跳過App層，也不盲重跑到綠。Host PR暫不merge；未驗證部分保留INCONCLUSIVE／NOT_TESTED，原9FAIL／8SKIP證據保留。

## 審查與下一個最小任務

Architecture／storage／backup／doctor／CI獨立審查APPROVE，source location與處置見[INDEPENDENT_REVIEW](INDEPENDENT_REVIEW.md)。OCR v1.12.13／Anthropic401：BLOCKED_BY_PROVIDER，無OCR APPROVE。

NEXT_ACTION：先在受限test infrastructure follow-up定位App IME visibility failure與Host完整capture成本，取得有可見軟鍵／exact editor效果的runtime證據，再決定PR #10；確認產品缺陷才另獨立P1修復，不能在本輪偷偷改App。之後實機驗收S1 paging／candidate commit與privacy。

PHYSICAL_ACCEPTANCE=OPEN（本輪ADB0裝置；無mutation）；PRODUCTION_READINESS=NOT_READY；M4=NOT_VALIDATED；AIE-S2=NOT_STARTED。
