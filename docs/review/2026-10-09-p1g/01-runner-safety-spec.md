# P1-G Runner Safety Implementation Plan

> Agent：以已授權規格按 sequential TDD 執行；獨立 reviewers 並行唯讀。每組先RED再改runner，沒有實體ADB。

Goal：修復 F01/F02/F03/P01/P02，保留產品/host來源及凍結APK，source gate可供實機預檢。
Architecture：stdlib現有runner，durable schema2綁定serial/user/IME三欄狀態；每mutation前重新user核對+explicit numeric scope，transaction錯誤觸發有界restore。固定source identity manifest和event state-machine支援現有sendStatus(2) envelope。
Tech：Python3 unittest/mock/fake subprocess；不加dependencies。

- [x] F01：USER_01–10。`PYTHONPATH=scripts/tests python3 -B -m unittest test_p1g_safety.UserIdentityTests`；RED assertion是缺user capture／錯user仍mutation。新增read_current_user、逐cmduserguard、shell help capability；scope以numericID，unsupported一律STOP。modified runner與tests/fakes。
- [x] F02：SUBTYPE_01–10。同一focused命令SubtypeTests。讀originalselected raw，限定API35–36、精確originalIME block `InputMethodSubtype array: count=0` 且 sentinel-1；其他BLOCKED_SUBTYPE_RESTORE_UNSUPPORTED。準備保存BLOCKED快照但安裝/選取不可執行；沒有universal subtype命令、不寫secure settings。postrestore核default/enabled/selected三值與user及零subtype證據。
- [x] F03：IO_01–10。IoRecoveryTests：write-ahead intent fsync beforeexecutor，post log failure觸發restore；recovery允許log故障但user guard不放寬，錯誤輸出stderr與private tempfile fallback，沒有durableproof不PASS。select成功result寫入也納入transaction。兩因與原subtype不覆寫。
- [x] 獨立security review：查user/subtype/IO/unsupported/stateoverwrite；有High先修與RED。
- [x] P01：COUNT_01–08，CountTests。持久scripts/p1_instrumentation_manifest.json從實際5/26方法精確建立並記sourcehash，expectedidentity+count與skip policy分開。runner actualcalls硬定5/26。
- [x] P02：PROTO_01–12，ProtocolTests。單一active test/start-finish完整，case-owner/sourcecontract envelope唯一；缺emitNOT_TESTED且suite不可PASS，skip仍INCONCLUSIVE。當前26均一envelope；多expectedcases只對測試明示契約接受，不硬套one-case。
- [x] 全量runner tests／10static contracts／AGENTS bootstrap/JVM/Debug/Release gates。Gradle序列，新build是gate，包內仍四frozenAPK。Release既有missingkeystore照實報告。
- [x] fresh independent final diff review＋negative synthetic protocol rejection；凍結hash／protected來源比對。
- [x] v3 portable package含runner/tests/identitymanifest/runbook/hash/review/metadata，不含private state、temp logs、pycache；只local commits，工區clean，交接08，STOP。

Code style：explicit argv lists、pure validated primitives；`if current_user != captured_user: raise RunnerError("ANDROID_USER_CHANGED")`。不得用shell字串/eval，也不得把平台unknown當safe default。
Acceptance：各finding至少一個可辨識原unsafe runner的assertion RED；所有requestedfakecases GREEN；source mutation READY_FOR_DEVICE_PRECHECK不是實機PASS／普遍OEM相容。user指定fail-closed界線已授權，不另要求設計確認。

收束路由：finishing-a-development-branch已讀，依使用者已明示local-only/keep工作樹，選保留分支，不呈現push/merge選項；verification-before-completion用fresh tests/hash/portable驗證。
