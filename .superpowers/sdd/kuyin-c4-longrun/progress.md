# KU-Yin C4 long-run ledger (authoritative after compaction)

- Branch: `c4-compose-libchewing-switch` @ `f959b4e` (worktree `/tmp/ku-yin-c4`, clean)
- Remote branch: still `e72acb3` (nothing pushed)
- Baseline: `e72acb3` → D2.5 `e3b3097`/`34a758b` → RC2 docs/tooling → `f959b4e`
- Toolchain root: `$HOME/.local/share/kuyin-toolchain/` (user-local, session env only)
- Gate states: STATIC PASS (8/8) / OCR delegation PASS, managed BLOCKED / JVM NOT RUN / BUILD NOT RUN / RUNTIME NOT RUN / DEVICE NOT TESTED
- D3 ENGINEERING: HOLD (needs JVM+build green)
- Phase 0 COMPLETE (2026-09-23 ~23:54 CST): toolchain self-provisioned
  (Temurin 21.0.12.1, SDK platform-36/build-tools-36/platform-tools,
  NDK 28.2.13676358, CMake 3.22.1 — all exact pins);
  bootstrap exit 0 (prebuilt 3587ba33); static 8/8;
  JVM 102/102 PASS 25 classes (all 12 critical groups incl. CASE-4);
  assembleDebug exit 0 (1m18s); APK 29MB sha256 ae749d0f…,
  4 ABIs × libchewing-jni.so + tsi/word/swkb/symbols.dat;
  pushed e72acb3..cc94325 to c4 (CI does NOT trigger on c4 push — workflow
  runs only on main/PR-to-main; no CI evidence obtainable without PR);
  adb present, zero devices → runtime NOT RUN; device NOT TESTED.
- Phase 1 preflight verdict: DispatchResult KEEP (all 10 D3 behaviors expressible;
  no refactor). Ruling recorded; no ADR needed (no material change).
- Test-fix ledger: EffectiveModePolicyTest android imports ×2 (TEST DEFECT, mine).
- Warnings: 4× Icons.Filled.Backspace deprecation (pre-existing, harmless).
- Next: Phase 2 D3a (service owns ONE AndroidChewingEngine, init-once/close-once,
  RED lifecycle-count tests first) on branch c4, HEAD cc94325.
- D3a DONE (2026-09-24): NativeChewingEngineOwner (factory-injected, DACHEN init-once,
  resetSession never recreates, close-once + defensive repeats no double-free) + 3 RED-first
  lifecycle-count tests. Evidence: RED = 3× unresolved reference (ownership missing, not toolchain);
  GREEN focused PASS; full JVM 105/105 (26 files, 0 fail); assembleDebug PASS (~29MB);
  STATIC P3 + production-compose OK. Commit e76332f. Next: D3b safe native startup
  (LibChewingDataInstaller → AndroidChewingEngine → Dachen init, failure falls back to DictionarySession).
- D3b DONE (2026-09-24): NativeStartupGate (install-then-open, installer failure → null
  without opening engine; unready engine → close + null fallback; no user decoder toggle;
  pure logic, no android.util.Log so plain JUnit covers fallback paths) + 3 RED-first tests.
  Evidence: RED = 3× unresolved reference; one mid-GREEN fix (Log-not-mocked → dropped Log,
  root cause, single change); full JVM 108/108 (27 files, 0 fail); assembleDebug PASS;
  STATIC P3 + production-compose OK. Commit 0a6d17b. Next: D3c EditorPolicy → native
  personalized learning mapping.
- D3c DONE (2026-09-24): NativeLearningPolicy single-source map
  (enabled = allowPersonalizedLearning && !forceAscii; normal true, no-learning/password/
  forceAscii false; restore-by-construction: pure function of current policy, service already
  re-applies per onStartInput; service-side wiring lands with D3d cutover, not half-wired now).
  Evidence: RED = 4× unresolved reference (+1 import-path fix, test-only);
  full JVM 112/112 (28 files, 0 fail); assembleDebug PASS; STATIC P3 + production-compose OK.
  Commit c58882c. Next: D3d production cutover (normal ChewingEngineSession, DictionarySession
  fallback-only, behavior/construction test RED while still on DictionarySession).
- D3d DONE (2026-09-24): ProductionDecoderResolver (ready native → ChewingEngineSession,
  null → ZhuyinDictionarySession fallback-only) + 2 RED-first construction tests; service
  onCreate wires NativeStartupGate → NativeChewingEngineOwner(AndroidChewingEngine via
  LibChewingDataInstaller paths) → resolver; onStartInput maps NativeLearningPolicy to native;
  onDestroy closes owner once. Evidence: RED = 2× unresolved reference; full JVM 114/114
  (29 files, 0 fail); production-compose + P3 contracts OK; assembleDebug PASS (~29MB).
  Commit 7bd4626. Next: D3e editor-effect semantics.
- Slice verification (2026-09-24, D3a–D3d coherent): verify.py --static → verify OK (fresh);
  testDebugUnitTest + assembleDebug → BUILD SUCCESSFUL, 114/114 (29 files, 0 fail), APK ~29MB
  (fresh read); OCR delegation re-attempted on cc94325..HEAD → still 401 (session 46beff04…,
  --resume when key fixed); manual excellence review (strongest available equivalent):
  no blocking issues — owner close-once/null-out sound, gate Exception boundary covers
  installer+init (externals only reachable when ready; defensive-try hardening deferred to
  runtime/native-hardening phase), learning map matches directive incl. forceAscii guard,
  resolver trivially correct, service null-safe on all native-failure paths. Push: NOT
  performed (ledger rule: needs human approval). Next: D3e entry investigation.
- D3e entry investigation (2026-09-24, read-only): session-level Enter is covered —
  EnterCommandContractTest 5 tests (dict active/empty/unmatched-raw, chewing active/empty),
  UI falls back to onPerformEditorAction when unconsumed, service handleEditorAction maps
  GO/SEARCH/SEND/NEXT/DONE else commits newline. No RED written: D3e has no repo spec and
  the remaining editor-effect surface (post-action state, native paging + Enter interaction)
  needs a defined behavior list before test-first. Do NOT guess-implement; define D3e
  behaviors next, then RED→GREEN per slice.
- Toolchain self-provision (2026-09-23 ~22:48+): Temurin 21.0.12.1 OK,
  cmdline-tools OK, platform-tools/platform-36/build-tools-36/cmake OK,
  NDK 28.2 downloading (~14% at 22:5x, PID 54487, log /tmp/ndk-install.log)
- Next: finish toolchain provision → bootstrap → `verify --static` → `clean testDebugUnitTest` → `assembleDebug`
- Push: NOT PERFORMED until local gate green + human approval (prepared cmd on record)
- Forbidden: main merge/push, force-push, tags, releases, signing, Rime, INTERNET permission, telemetry
- Ruling (2026-09-24 emergency correction): old backup clone @34a758b was incorrectly selected after context recovery. Authoritative state is remote/local c4-compose-libchewing-switch @cc94325. Legacy local Waves/MERGE_SPEC (/Users/huang/KU-Yin-C4-D2.5-repo, /Users/huang/KU-Yin_keyboardforandroid@old-main, Desktop/KU-Yin-v0.2-FINAL/local research/design/WAVE*) are donor-only and cannot replace the C4/D3 plan. Resume point is D3a native lifetime owner. Cost if wrong: regress verified D2.5 work, reintroduce stale View-era architecture, divert into unapproved donor Waves. NOTE: docs/CURRENT_STATE.md stale (says 34a758b local-only/99 pending) — do not let it override Git/test evidence; refresh in isolated docs commit after D3a.
