# R3.3 Workspace checkpoint

## Scope and source

Remote main `8369da25fa54ab762dfceed457e71a08767a642e` re-queried unchanged. Product PR20 `de2d9dfaa296a5cdeabde405d686720b615ce9e2` and image PR24 `43aff1d168ddc18d68229b52423aab99dd24c151` remain Draft. R3.3 test-only source starts from R3.2 docs `fab39196fdf3761d84839446c7a76c4361e9dea9` (runtime `8acf4b24c63c8c8474e8ea95b50054977902ecaa`), itself descended from observed main. No product integration or PR merge.

## Local navigation and preservation

Local-only control center: `~/KU-Yin-Workspace/README.md`. It indexes existing physical sources, evidence, artifacts, media and releases; it is not another repository or public release bundle. Main repo baseline:28 worktrees/4 dirty,95,352 file metadata,70 safe symlinks,0 physical migrations/0 deletion. This checkpoint adds one isolated worktree, so29 main-repo worktrees after. The original28 HEAD/branch/status and20 dirty/untracked file digests were compared before/after. Seven secondary Git repos are recorded separately; four historical restore copies are dirty and20 additional nonsecret files have snapshot digests.

Two original manifests contain641+153 evidence entries. Existing paths,size,mtime unchanged; hashes are inherited from the previous full verification, explicitly **not fresh content proof**.63 selected existing files freshly hashed:40 dirty/restore files,2 evidence manifests,21 PR24 assets/source/manifests.34 private-path metadata exclusions include14 conservative vendor namespace matches; no private content copied, hashed or uploaded. Known-hash duplicate candidates:120 groups, KEEP_ALL; not a full disk duplicate scan.

`10-Source/current-main` points to clean publication checkout HEAD4258dda9; tree521dcecd equals remote main8369da25. Local main remains63c9e4a; no branch was reset/fast-forwarded. Do not edit old worktrees through links. Central root0700 is access control, not encryption or read-only protection.

## Independent review and limits

Fresh workspace reviewer `/root/r33_workspace_review`: specification PASS; engineering PARTIAL/Low only. Early-overwrite/private-parent/Git-exit/link-preflight findings closed; FILE_MANIFEST now publishes via temp+fsync+replace. Small JSON indices still lack a multi-file atomic transaction/recovery fault-injection proof. No unresolved High/Medium in workspace scope. Discovery is bounded to KU-Yin-name matches at known user folders; it cannot assert finding arbitrary unrelated-name deep directories.

Runtime review is separate and incomplete after reviewer quota exhaustion. Product/Native/Release/data remain unchanged. No new CI, physical-device operation, push, PR merge or release at this checkpoint. Resume from [H1 observation](../testing/R3_3_H1_PROCESS_OBSERVATION.md), [Host triage](../testing/HOST_FAILURE_TRIAGE_MATRIX.md) and local control center task ledger. Release NO_GO.
