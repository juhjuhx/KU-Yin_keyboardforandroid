# ADR 0007：主線與文件權威收束

Status：ACCEPTED（maintenance scope，2026-10-12使用者R1執行prompt）。不變產品privacy/license/decoder/UI決策。

## Context

GitHubmain已含P0/P1/AIE；多個localworktree與歷史review仍存在，AGENTS/README/security的舊production描述漂移，不能再建立平行產品真相。

## Decision

GitHubmain唯一產品主線；CURRENT_STATE記當前、ARCHITECTURE＋10章規範ownership、ROADMAP只future、review/archive/HISTORY保留historical。Canonicalentry來源SHA與fresh/historical證據明示，短anchor導航，不刪大量歷史檔。每切片normalPR＋獨立review，no force/rewrite。

localmain採正常fast-forward同步；dirty/forensic/M4/donor工區保留，branchlineage按ancestry非名稱。TAUI先保存與列consumers，不擅自移除；完整data/privatebackup另外決策。Gitbundle保存refs reachableobjects，dirty另patch/copy並isolatedrestoreproof。

## Consequences

SOURCE/Git/test證據優先於舊chat；snapshot/generation、nativeACK/editorACK保持，不能以圖把future模組畫成production。沒有任何erase/migration/newrelease授權。可用normalrevert回退code/docs，私人資料rollback不能靠Gitrevert。
