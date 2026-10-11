# 00-source-of-truth — P1-A (2026-10-04)

BASE_SHA (detached, all P1-A work here): `3d86f7538c47dc929e25cdb0bd0755f8cc3bad60`
P1-A worktree: `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1a-native-startup-hardening` (detached, clean at creation)
REMOTE_C4_SHA: `origin/c4-compose-libchewing-switch = 3d86f75` (matches BASE_SHA)
MAIN_SHA (local `main`): `653b4a3d9daaea94a2391a2e3175b7a02784845e` (stale local ref)
REMOTE_MAIN_SHA: `origin/main = 73e810f5e0274abd366e9b3a7388a08df8b73eaf`
MERGE_BASE (origin/main vs origin/c4): `278bcc2a40feca425bfe89eeded3d78410cf452e`
AHEAD_BEHIND (origin/c4 vs origin/main): ahead 42 / behind 1 (recomputed live)
TAGS: v0.1.0-alpha, v0.1.1-alpha, v0.1.2-alpha, v0.2.0-alpha.1

## Worktree classification

- CANONICAL_REFERENCE: `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/c4-clean-baseline` @ 3d86f75 (detached; pre-existing dirty: `M AGENTS.md, docs/ARCHITECTURE.md, docs/CURRENT_STATE.md` + untracked `KU_YIN_C4_CLEAN_BASELINE.md, KU_YIN_DOC_RECONCILIATION.md`; product source = HEAD)
- CURRENT P1-A WORKTREE: `.worktrees/p1a-native-startup-hardening` @ 3d86f75 (detached, clean; only new `docs/review/2026-10-04-p1a/` will be added + P1-A slice)
- FORENSIC_PRESERVE (DO NOT MODIFY): `.worktrees/c4-compose-libchewing-switch` @ e72acb3 (branch `c4-compose-libchewing-switch`, behind remote 37; dirty product/test changes preserved)
- EXPERIMENT (DO NOT MERGE WHOLESALE): `.worktrees/m4-performance-baseline` @ 3d86f75 (detached; dirty `M app/build.gradle.kts` + untracked M4 drafts/benchmarks; M4 = NOT_VALIDATED)
- FORENSIC_OTHER: `.worktrees/fix-pre-c4-production-hardening` @ 3f02976 (branch `fix/pre-c4-production-hardening`)
- STALE_DOCS: root `/Users/huang/KU-Yin_keyboardforandroid` @ fe1e96f (branch `feat/mainline-docs-i18n`, dirty `M AGENTS.md` + untracked handoff doc)
- DONOR (NOT canonical): `/Users/huang/Desktop/KU-Yin-v0.2-FINAL/local` (AI Studio export + Wave1-5 donor logic; reusable atoms inventoried separately, no port in P1-A)
- ARCHIVE: `/Users/huang/Desktop/KU-Yin-v0.2-FINAL/完整審查_2026-10-04/` (review map only, not product; treat assertions as reproduce-list)

## Live GitHub state (reverified 2026-10-04)

- Remote: `https://github.com/juhjuhx/KU-Yin_keyboardforandroid.git`
- `origin/c4-compose-libchewing-switch = 3d86f75` == BASE_SHA (remote C4 clean)
- Local C4 branch `e72acb3` is forensic/stale (behind remote 37), NOT the remote truth
- Review package noted main=73e810f / release tag lineage from merge-base 278bcc2; local `main=653b4a3` is stale vs `origin/main=73e810f`; tags listed above
- CI/Actions exact-SHA result + open/stale PR list + branch protection: NOT re-queried in this pass (no GitHub state change authorized); review package records blocking-build green + experimental runtime-smoke FAIL at same SHA — treat as assertion to reverify, not truth

## Canonical architecture (code-verified, C4 @ 3d86f75)

`KuYinInputMethodService.onCreate → NativeStartupGate.start(install, openEngine) → NativeChewingEngineOwner.ensureStarted → AndroidChewingEngine (sole JNI caller) → JNI/libchewing`; `ProductionDecoderResolver.resolve(nativeEngine, KuYinEngine)` → native `ChewingEngineSession` or fallback `ZhuyinDictionarySession`. `userPreferredMode` service-owned; `EffectiveModePolicy` presentation-only; UI result path `DispatchResult + orderedCommits`; C4 Enter semantics preserved.
