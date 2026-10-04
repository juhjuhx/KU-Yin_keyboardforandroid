# 01-deep-understanding — P1-A native startup (2026-10-04, C4 @ 3d86f75)

Scope: `NativeStartupGate.start` + `NativeChewingEngineOwner.ensureStarted/close` +
service `onCreate/onDestroy` wiring + `AndroidChewingEngine.init/close` + `ProductionDecoderResolver`.
Read in depth from P1-A worktree (== C4 HEAD); no implementation yet.

## 1. Actual call graph (service onCreate)

`KuYinInputMethodService.onCreate` (KuYinInputMethodService.kt:47-71):
1. `engine = KuYinEngine(this)` (presentation/fallback engine, always created)
2. `NativeStartupGate.start(install={ nativePaths = LibChewingDataInstaller.ensureInstalled(this) }, openEngine={ owner = NativeChewingEngineOwner{...AndroidChewingEngine(...) }; nativeOwner = owner; owner.ensureStarted() })`
3. `session = ProductionDecoderResolver.resolve(nativeEngine, engine)` → native session or dictionary fallback
4. `onDestroy`: `nativeOwner?.close(); nativeOwner = null; nativeEngine = null`

## 2. Defect A — CONFIRMED: `openEngine()` outside protected boundary

`NativeStartupGate.start` (NativeStartupGate.kt:26-48):
- `install()` is inside try/catch → null fallback. GOOD.
- Line 35 `val engine = openEngine()` is OUTSIDE any try/catch. If the factory throws
  (e.g. `nativePaths == null → error("libchewing paths missing")`, or `AndroidChewingEngine(...)`
  init-block throws, or `owner.ensureStarted()` → `factory()` / `created.init()` throws),
  the exception escapes `start()` → escapes `Service.onCreate` → **no fallback, service startup crash path**.
- Only `engine.init()` (gate's own second init) is protected (lines 36-47), not creation/first-init.
- Existing `NativeStartupGateTest` covers installer-fail, success, unready-close — but has NO
  factory-throw case, so the hole is untested. **Defect A is real and must be fixed in P1-A.**

## 3. Defect B/C — CONFIRMED: duplicate init ownership with native side effects

- `NativeChewingEngineOwner.ensureStarted` (Owner.kt:18-26): `factory()` → `created.init(DACHEN)` → store. Init #1.
- Gate then runs `engine.init(DACHEN)` AGAIN (Gate.kt:37) on the returned engine. Init #2.
- `AndroidChewingEngine.init` (AndroidChewingEngine.kt:38-67) is NOT idempotent bookkeeping:
  `closeNativeContext()` (chewing_delete) + `chewing_new2(...)` + kb/mode setup + `ready=true`.
  Second init **destroys and recreates the native context** created by the first.
- Consequence: every successful startup pays create→init→delete→recreate; a failure between
  the two inits has ambiguous ownership (who closes the half-initialized engine?).
- Existing `NativeChewingEngineOwnerTest` pins owner init-once in isolation, but the
  gate+owner composition double-init is untested. **Defects B/C are real (same root cause:
  two init owners); P1-A must establish ONE init owner.**

## 4. Ownership map (before fix)

| Step | Who creates | Who inits | Who validates | Who closes | Who decides fallback |
|---|---|---|---|---|---|
| factory/open | Owner.factory (+Service closure) | Owner.ensureStarted (init#1) | — | nobody on factory-throw | nobody (escapes) |
| gate init | — | Gate.start (init#2) | Gate (`isReady`) | Gate (`runCatching close`) | Gate (null) |
| shutdown | — | — | — | Owner.close via Service.onDestroy | — |

Failure-boundary gaps: `openEngine()` throw escapes; first-init throw escapes from gate's
perspective (thrown inside `openEngine` lambda before gate's try); unready-after-second-init
is handled, but the first context was already leaked/recreated.

## 5. What P1-A must establish (design target, no code yet)

- ONE init owner (owner inits exactly once per created engine; gate never re-inits).
- Protected creation+init boundary: `openEngine()` call itself inside try/catch → null fallback.
- Failure cleanup: init-throw/unready → close exactly once → null.
- Reuse/close-once preserved: `ensureStarted` returns same engine; `close` idempotent.
- Fallback preserved: null → `ZhuyinDictionarySession`; success path unchanged otherwise.
- No scope drift: no UI/ranking/EditorPolicy/Enter/M4/symbols/RIME/Gradle changes.

## 6. Test inventory (existing, all currently GREEN by report; will recount fresh)

- `NativeStartupGateTest` (3): success-ready, installer-fail-no-open, unready-closed-null. MISSING: factory-throw, first-init-throw, init-count==1.
- `NativeChewingEngineOwnerTest` (3): reuse-once, reset-no-recreate, close-once. MISSING: init-failure propagation/close semantics at owner level (decide in design whether owner or gate owns it — prefer gate owns fallback, owner owns reuse/close).
- `ProductionDecoderResolverTest` (2), `ProductionSeamIntegrationTest`, `ProductionEditorPolicyTest`: regression net, untouched by P1-A.
- Toolchain note: this session `java -version` finds NO system Java (`Unable to locate a Java Runtime`); Gradle must use repo-pinned JDK 21 toolchain (CI `build.yml`, DEVELOPMENT.md) — regroup before GREEN runs.

## 7. Open questions for design (03)

- Q1: gate takes `openEngine: () -> ChewingEngine` where the lambda currently embeds owner creation + ensureStarted. After single-owner fix, does the lambda become owner-factory wiring (gate calls `owner.ensureStarted()` inside its protected boundary) or does the service compose owner-outside/gate-inside? Prefer smallest diff that keeps service wiring readable.
- Q2: `Error` vs `Exception`: gate catches `Exception` today; `error("...paths missing")` throws `IllegalStateException` (caught). Keep `Exception` boundary (do NOT broaden to `Throwable` without security review).
- Q3: close-failure: `runCatching { engine.close() }` swallows close errors today — keep (cleanup path must not mask original failure), but assert close-called-once in tests via fakes.
