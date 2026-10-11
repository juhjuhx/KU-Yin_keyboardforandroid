# 04-test-evidence — P1-A GREEN (2026-10-04)

## RED (before fix, `--rerun-tasks`, JDK 21)
- Command: `./gradlew :app:testDebugUnitTest --tests "com.example.ime.service.NativeStartupGateTest" --tests "com.example.ime.service.NativeChewingEngineOwnerTest" --rerun-tasks`
- Result: 10 tests, **4 failed** (all new P1-A boundary tests):
  - `factory failure falls back to null without crash` — IllegalStateException escaped (defect A)
  - `first init failure closes engine once and falls back` — escaped / double-init path
  - `failed creation retains no engine and next start can succeed` — escaped
  - `installer success plus ready engine returns engine with single init` — initCalls==2 vs 1 (defect B/C)
- XML: `app/build/test-results/testDebugUnitTest/TEST-com.example.ime.service.NativeStartupGateTest.xml`
  (`tests=6 failures=4`); Owner suite 4/4 pass (propagation test is post-fix behavior pin).

## GREEN (after fix)
- Focused: Gate + Owner + `ProductionDecoderResolverTest` + `ProductionSeamIntegrationTest` → BUILD SUCCESSFUL.
- Full JVM: `./gradlew :app:testDebugUnitTest --rerun-tasks` → BUILD SUCCESSFUL;
  XML totals: **classes=35, tests=133, failures=0, errors=0** (was 129 @Test baseline; +4 new gate/owner tests… note: one existing success test was strengthened, net new = 5 test methods across 2 files: gate +4, owner +1).
- Contracts: 10/10 `scripts/check_*.py` PASS.
- Native bootstrap: `bash scripts/bootstrap_native_deps.sh` → exit 0 (`KU-Yin native dependencies ready: 3587ba33…`).
- assembleDebug → BUILD SUCCESSFUL; `app/build/outputs/apk/debug/app-debug.apk`
  (29,158,832 bytes, sha256 `3f563cd8ee1ace49cbcdd6998372cd235509b296de24605a608a864b1f6cde89`).
- assembleRelease → FAILS as expected: missing `my-upload-key.jks` (keystore never in repo;
  C4 baseline has no keystore either; AGENTS.md: "current release artifact is unsigned").
  NOT a regression.
- Runtime: `adb devices` → empty (daemon started, no device/emulator attached).
  RUNTIME_STATUS=NOT_TESTED. No device claim made.
