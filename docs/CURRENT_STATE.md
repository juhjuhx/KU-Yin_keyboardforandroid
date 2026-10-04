# CURRENT_STATE (what is true NOW — no future architecture here)

- Release: `v0.2.0-alpha.1` = old commit `278bcc2` (pre-C3.5; NOT this branch).
- main: `73e810f` = old Android main + unrelated `taui-workspace/` web files.
- C4 branch: `c4-compose-libchewing-switch` (D3a–D3h landed, pushed; see ledger).
- Production decoder: `ChewingEngineSession` (native, D3d cutover).
  Native owner: service-owned `NativeChewingEngineOwner` (init-once/close-once).
  Fallback: `ZhuyinDictionarySession`, startup-only via `NativeStartupGate`.
  Learning: `NativeLearningPolicy` maps EditorPolicy to native (single source).
- Tests: local verified P1-A/B/C checkpoint has 37 JVM classes / 149 tests, all green;
  P1-B adds rendered Dachen coverage at 320dp/360dp; +2 emulator instrumentation
  (native CASE-4, continuous-sentence structural). Contracts: exit 0 (incl. CI-trigger + NDK pins).
- Build: assembleDebug green (~29MB); blocking CI runs on C4 push and is green.
- Runtime: emulator smoke green incl. native CASE-4/continuous-sentence (experimental
  job, continue-on-error). Physical device: NOT TESTED. QR donation art: decorative
  until scanned.
- Open gates: D3 major gate (clean test + CI); device QA incl. wider D3h corpus.
- P1-B local slice: Compose Dachen rows derive from `KeyboardLayout.Dachen`;
  missing ㄕ/ㄖ and lower-row hint drift fixed; 11/10/10/10 phonetic/tone keys,
  backspace after row 4. Device runtime for this slice: NOT_TESTED (no device).
- P1-C local slice: editors without composition use temporary English mode and
  disable personalized learning; text return restores the user's preferred mode.
  Verified via policy + actual onStartInput + Compose tests; runtime NOT_TESTED.
- Known debt: main contains TAUI files (recorded, revert separately, no rewrite);
  remaining M4 work NOT_VALIDATED; PR #4 = donor, PR #6 = docs-only.
