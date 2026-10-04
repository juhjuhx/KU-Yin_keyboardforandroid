# 05-independent-review — P1-A final diff (2026-10-04)

Reviewed files (production only, 2 files, ~+17/−18):
- `ime/service/NativeStartupGate.kt` — openEngine inside try/catch; duplicate `engine.init` removed; KDoc updated; duplicate KDoc block removed.
- `ime/service/NativeChewingEngineOwner.kt` — init-throw → close-once → rethrow, no retain (retryable).

Five-axis verdict:
- Correctness: factory-throw, init-throw, unready, success-once, reuse, close-once, no-leak all pinned by tests. No hidden double-init path (grep: only Owner calls `init`; gate no longer references `init`). Service `onCreate`/`onDestroy` wiring untouched and still consistent (owner set inside lambda before ensureStarted; onDestroy closes).
- Readability: KDoc states ownership explicitly; no new abstraction, no DI, no API change (`ChewingEngine` untouched).
- Architecture: single init owner (Owner), single fallback decider (Gate), single JNI caller unchanged. No scope drift (no UI/ranking/EditorPolicy/Enter/M4/symbols/RIME/Gradle diff).
- Security: catch stays `Exception`-only (no `Throwable` swallow); `runCatching close` only on cleanup paths; no secrets/logging added; offline-first untouched.
- Performance: one fewer native create/delete/recreate per startup (strictly less JNI work); no hot-path change.

Tautology check: new tests use counting fakes with independent assertions (initCalls/closeCalls/attempts/identity); failure modes verified RED before fix (4 failures), so tests are not vacuous. Owner propagation test asserts throw + retry-creates-good (not self-fulfilling).

Residual risks (accepted, documented):
- `install()`-then-`openEngine()` is not atomic across process death; same as before, out of P1-A scope.
- `close()` throwing inside `runCatching` is swallowed by design (cleanup); a chronically failing close would hide — same as pre-existing policy, flag for future observability (P1 hardening follow-up, not this slice).
- No device proof (RUNTIME_STATUS=NOT_TESTED).

Status: APPROVE for P1-A slice. Remaining P1-B/C/D stay OPEN. M4 NOT_VALIDATED.
