# CURRENT_STATE (what is true NOW — no future architecture here)

- Release: `v0.2.0-alpha.1` = old commit `278bcc2` (pre-C3.5; NOT this branch).
- main: `73e810f` = old Android main + unrelated `taui-workspace/` web files.
- C4 branch: `c4-compose-libchewing-switch` (D3a–D3h landed, pushed; see ledger).
- Production decoder: `ChewingEngineSession` (native, D3d cutover).
  Native owner: service-owned `NativeChewingEngineOwner` (init-once/close-once).
  Fallback: `ZhuyinDictionarySession`, startup-only via `NativeStartupGate`.
  Learning: `NativeLearningPolicy` maps EditorPolicy to native (single source).
- Tests: 35 JVM classes / 129 @Test, all green; +2 emulator instrumentation
  (native CASE-4, continuous-sentence structural). Contracts: exit 0 (incl. CI-trigger + NDK pins).
- Build: assembleDebug green (~29MB); blocking CI runs on C4 push and is green.
- Runtime: emulator smoke green incl. native CASE-4/continuous-sentence (experimental
  job, continue-on-error). Physical device: NOT TESTED. QR donation art: decorative
  until scanned.
- Open gates: D3 major gate (clean test + CI); device QA incl. wider D3h corpus.
- Known debt: main contains TAUI files (recorded, revert separately, no rewrite);
  `ROW_*` vs canonical Dachen duplication pending M4; PR #4 = donor, PR #6 = docs-only.
