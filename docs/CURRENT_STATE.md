# CURRENT_STATE (what is true NOW — no future architecture here)

- Release: `v0.2.0-alpha.1` = old commit `278bcc2` (pre-C3.5; NOT this branch).
- main: `73e810f` = old Android main + unrelated `taui-workspace/` web files.
- C4 branch: `c4-compose-libchewing-switch` (D3a–D3h landed, pushed; see ledger).
- Production decoder: `ChewingEngineSession` (native, D3d cutover).
  Native owner: service-owned `NativeChewingEngineOwner` (init-once/close-once).
  Fallback: `ZhuyinDictionarySession`, startup-only via `NativeStartupGate`.
  Learning: `NativeLearningPolicy` maps EditorPolicy to native (single source).
- Tests: local P1-A/B/C/D verification has 40 JVM classes / 190 tests, all green;
  P1-B includes rendered Dachen coverage at 320dp/360dp. Existing Android instrumentation
  has 4 classes / 5 test methods (native smoke, IME recreate, CASE-4, continuous-sentence
  structural, package identity). Compilation/package success does not prove device execution.
  Contracts: 10/10 exit 0 (incl. CI-trigger + NDK pins).
- Build: local P1-D assembleDebug green (~29MB). Historical C4 blocking CI was reported
  green; the local P1 series / Alpha preparation has not been pushed and has no fresh CI run.
- Runtime: historical C4 emulator smoke included native CASE-4/continuous-sentence
  (experimental job, continue-on-error); it does not validate local P1-A/B/C/D.
  Local P1-series runtime: NOT_TESTED (no current Android target). QR donation art: decorative
  until scanned.
- Open gates: exact P1-A/B/C/D physical-device acceptance, controlled editor matrix
  coverage, production signing, and device QA incl. wider D3h corpus.
- P1-B local slice: Compose Dachen rows derive from `KeyboardLayout.Dachen`;
  missing ㄕ/ㄖ and lower-row hint drift fixed; 11/10/10/10 phonetic/tone keys,
  backspace after row 4. Device runtime for this slice: NOT_TESTED (no device).
- P1-C local slice: editors without composition use temporary English mode and
  disable personalized learning; text return restores the user's preferred mode.
  Verified via policy + actual onStartInput + Compose tests; runtime NOT_TESTED.
- P1-D local slice: one immutable `EditorEnterPolicy` supplies inline Enter label,
  icon presentation and dispatch. `NO_ENTER_ACTION` yields newline; custom labels
  dispatch their custom IDs; PREVIOUS uses 上一步. Consumed composition Enter never
  also dispatches an editor action/newline. Debug and instrumentation compilation pass;
  Release packaging remains blocked by the existing missing keystore. Runtime NOT_TESTED.
  Review/evidence: `docs/review/2026-10-04-p1d/06-handoff.md`; local commits only, no push.
- Known debt: main contains TAUI files (recorded, revert separately, no rewrite);
  remaining M4 work NOT_VALIDATED; PR #4 = donor, PR #6 = docs-only.

- P1 Alpha preparation (2026-10-07): isolated `test/p1-alpha-acceptance` from P1-D docs
  `94e9191`; byte-identical P1-D Debug APK plus existing instrumentation APK prepared.
  Fresh ADB inventory has no target; runtime stays NOT_TESTED. Full P1 editor/action
  matrix has 30 unexecuted cases and requires a controlled debug host/action recorder.
  Review/runbook: `docs/review/2026-10-07-p1-alpha/05-交接.md`. No product source change,
  no release signing, no push, no M4 work.
