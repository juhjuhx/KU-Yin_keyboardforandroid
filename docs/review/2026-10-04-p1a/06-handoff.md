# KU-YIN P1-A FINAL HANDOFF

SOURCE_OF_TRUTH: `docs/review/2026-10-04-p1a/00-source-of-truth.md` (+ `01/02/03/04/05` same dir)
CANONICAL_REPO: `/Users/huang/KU-Yin_keyboardforandroid` (remote `https://github.com/juhjuhx/KU-Yin_keyboardforandroid.git`)
WORKTREE: `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1a-native-startup-hardening` (detached, isolated; no commit/push performed)
BRANCH: (detached HEAD, no branch)
BASE_SHA: `3d86f7538c47dc929e25cdb0bd0755f8cc3bad60`
FINAL_SHA: `3d86f7538c47dc929e25cdb0bd0755f8cc3bad60` + uncommitted P1-A slice (4 files, see below)
REMOTE_C4_SHA: `origin/c4-compose-libchewing-switch = 3d86f75` (== BASE)
MAIN_SHA: local `main = 653b4a3` (stale); `origin/main = 73e810f`
AHEAD_BEHIND: origin/c4 ahead 42 / behind 1 vs origin/main (merge-base `278bcc2`)

OPENCODEREVIEW:
VERSION: `open-code-review v1.12.11 (a758d9cb) darwin/arm64`
STATUS: BLOCKED_BY_PROVIDER
SESSION: none (preview only; new .md files listed as unsupported_ext, exit 0)
FINDINGS: none possible; provider `ocr llm test` → 401 invalid x-api-key. Re-run on diff when key valid.

ROOT_CAUSE:
FACTORY_FAILURE_BOUNDARY: `NativeStartupGate.start` called `openEngine()` outside try/catch → factory/first-init throw escaped `Service.onCreate`, no fallback.
DOUBLE_INIT: Owner `ensureStarted` did init#1, gate re-ran `engine.init` (#2); `AndroidChewingEngine.init` close/recreates native ctx → wasted recreate + ambiguous failure ownership.
OWNERSHIP_BEFORE: create=Owner, init#1=Owner + init#2=Gate, validate=Gate, close-fail=Gate, factory-fail=nobody.
OWNERSHIP_AFTER: create+init=Owner (once, failure→close+rethrow+no-retain); validate+fallback+fail-close=Gate; shutdown-close=Owner via service.

FILES_CHANGED:
M `app/src/main/java/com/example/ime/service/NativeStartupGate.kt` (protect openEngine; remove duplicate init; KDoc)
M `app/src/main/java/com/example/ime/service/NativeChewingEngineOwner.kt` (init-throw→close→rethrow, no retain)
M `app/src/test/java/com/example/ime/service/NativeStartupGateTest.kt` (+4: factory-fail, init-fail-close-once, no-leak-retry, init-once-identity; strengthened success test to owner-composed single-init)
M `app/src/test/java/com/example/ime/service/NativeChewingEngineOwnerTest.kt` (+1: init-failure-propagates-no-retain-retry)
A `docs/review/2026-10-04-p1a/{00,01,02,03,04,05,06}-*.md` (this file = 06)
ENV-ONLY (untracked? no — check): `local.properties` (`sdk.dir=/opt/homebrew/share/android-commandlinetools`, same as root repo; gitignored env config, not product)

TESTS_ADDED: 5 new methods (gate 4 + owner 1); 1 existing strengthened.

RED_EVIDENCE:
COMMAND: `./gradlew :app:testDebugUnitTest --tests "...NativeStartupGateTest" --tests "...NativeChewingEngineOwnerTest" --rerun-tasks` (JDK 21, `JAVA_HOME=/opt/homebrew/opt/openjdk@21`)
RESULT: 10 tests, 4 failed (factory-fail escaped, init-fail escaped, no-leak escaped, init-once got 2). XML `TEST-...NativeStartupGateTest.xml` (tests=6 failures=4).

GREEN_EVIDENCE:
FOCUSED_TESTS: Gate + Owner + ProductionDecoderResolverTest + ProductionSeamIntegrationTest
COMMAND: same gradlew pattern, post-fix
EXIT: BUILD SUCCESSFUL
JVM: TEST_CLASSES: 35 / TEST_COUNT: 133 / failures=0 errors=0
COMMAND: `./gradlew :app:testDebugUnitTest --rerun-tasks`
EXIT: BUILD SUCCESSFUL
CONTRACTS: COUNT: 10/10 PASS (`scripts/check_*.py`)
COMMAND: `for f in scripts/check_*.py; do python3 "$f"; done`
EXIT: all PASS
NATIVE_BOOTSTRAP: `bash scripts/bootstrap_native_deps.sh` → exit 0 (ready `3587ba33…`)
COMMAND: as shown
EXIT: 0
ASSEMBLE_DEBUG: BUILD SUCCESSFUL
COMMAND: `./gradlew :app:assembleDebug`
EXIT: 0
APK_PATH: `app/build/outputs/apk/debug/app-debug.apk`
APK_SHA256: `3f563cd8ee1ace49cbcdd6998372cd235509b296de24605a608a864b1f6cde89`
APK_SIZE: 29158832 (~29MB)
ASSEMBLE_RELEASE: FAILS expectedly — missing `my-upload-key.jks` (no keystore in repo/baseline; AGENTS.md: release unsigned). NOT a regression.

RUNTIME:
DEVICE: none attached (`adb devices` empty)
ANDROID_API: n/a
ABI: n/a
STATUS: NOT_TESTED (no device/emulator; no runtime claim)

INDEPENDENT_REVIEW:
STATUS: APPROVE (P1-A slice)
FINDINGS: no hidden double-init (only Owner inits); no leak (close-once on fail paths); no `Throwable` swallow; no scope drift (2 prod files); tests non-tautological (RED-proven). Residual: install→open non-atomic across process death; close-throw swallowed by cleanup policy (flagged for future observability).

PRODUCTION_BEHAVIOR:
WHAT_CHANGED: native startup failures (missing paths/factory-throw/init-throw/unready) now always fall back to `ZhuyinDictionarySession` instead of escaping; successful startup performs exactly ONE native init (no delete/recreate churn).
WHAT_DID_NOT_CHANGE: decoder ranking, UI, EditorPolicy, Enter, M4, symbols, RIME, Gradle, ABI/pins/identity, JNI surface, session contracts.

REMAINING_P1:
P1-B: Canonical Dachen UI source / missing ㄕ ㄖ / mapping drift — OPEN
P1-C: non-text editor composition policy — OPEN
P1-D: IME_FLAG_NO_ENTER_ACTION / Enter policy — OPEN

M4_STATUS: NOT_VALIDATED (no measurement in this task; m4 worktree untouched)

DIRTY_STATE:
TRACKED: `M NativeStartupGate.kt, M NativeChewingEngineOwner.kt, M NativeStartupGateTest.kt, M NativeChewingEngineOwnerTest.kt`
UNTRACKED: `docs/review/2026-10-04-p1a/` (+ ignored `local.properties` env file, `app/build/` outputs)

PRESERVED_WORKTREES:
FORENSIC: `.worktrees/c4-compose-libchewing-switch` @ e72acb3 (dirty, untouched)
M4: `.worktrees/m4-performance-baseline` @ 3d86f75 (dirty, untouched)
DONOR: `/Users/huang/Desktop/KU-Yin-v0.2-FINAL/local` (untouched; no port in P1-A)

FINAL_RULING: PASS (static + JVM + contracts + bootstrap + Debug; runtime NOT_TESTED, release unsigned as documented)

NEXT: P1-B CANONICAL DACHEN UI SOURCE
