# 03-p1a-design — single init ownership + protected startup boundary

Status: DESIGN (no production code changed yet). Implements §9–§10 of the Luna prompt.

## OWNERSHIP (after fix)

| Step | Owner | Action |
|---|---|---|
| create | `NativeChewingEngineOwner` (via injected factory) | `factory()` exactly once per lifetime |
| initialize | `NativeChewingEngineOwner` ONLY | `init(DACHEN)` exactly once per created engine |
| validate readiness | `NativeStartupGate` | `isReady` check after owner returns engine |
| close on failure | `NativeStartupGate` | `runCatching { engine.close() }` then null |
| close on shutdown | `NativeChewingEngineOwner.close()` via service `onDestroy` | close-once |
| fallback decision | `NativeStartupGate` | null → service resolves `ZhuyinDictionarySession` |

Gate NEVER calls `engine.init()` again. Owner NEVER decides fallback (it throws/propagates;
gate converts to null). Service wiring keeps creating the owner inside `openEngine`
(it already does) so `nativeOwner` is still set for `onDestroy` close — but init happens once.

## FAILURE BOUNDARY

`NativeStartupGate.start` protects, in order:
1. `install()` throw (any `Exception`) → null (unchanged).
2. `openEngine()` throw (factory throw, first-init throw, anything from owner creation) →
   null, no engine retained. **THIS IS THE NEW BOUNDARY (defect A fix).**
3. `init` — no longer in gate (removed duplicate; defect B/C fix).
4. `!isReady` → close once → null (unchanged).
5. `close()` itself throwing during cleanup → swallowed via `runCatching` (unchanged;
   cleanup must not mask the original failure).

Catch `Exception` only (NOT `Throwable`): `OutOfMemoryError`/fatal programming errors must
still crash loudly. `error("libchewing paths missing")` is `IllegalStateException` → caught. Good.

## EXPECTED FLOW

```
install()
  └─ throw → null (fallback)
openEngine() = { create owner; owner.ensureStarted() [factory + init-once inside] }
  └─ throw → null (fallback)              ← NEW
engine.isReady?
  ├─ true  → return engine (init count == 1)
  └─ false → close once → null            ← unchanged
service: null → ZhuyinDictionarySession; non-null → ChewingEngineSession
onDestroy: owner.close() (close-once, idempotent)
```

## Why this shape (smallest change)

- Alternative considered: move owner creation out of the lambda into the service and pass
  the owner in. Rejected: larger service diff, `nativeOwner` assignment timing gets tricky
  on failure paths (must still close a half-created owner). Keeping owner-inside-lambda and
  widening gate's try/catch is a ~10-line diff touching exactly one production file.
- Alternative considered: owner catches init failure itself and returns null. Rejected:
  owner is a lifecycle container (reuse/close), not a fallback decider; null-return would
  split fallback logic across two classes.
- `AndroidChewingEngine.init` close/recreate semantics deliberately NOT touched: with single
  init ownership the recreate path only runs on explicit re-`ensureStarted`-after-close,
  which the owner forbids (`closed` flag). No JNI change in P1-A.

## Test plan (RED first, §11 mapping)

Gate tests (`NativeStartupGateTest`, pure JUnit):
1. INSTALL_FAILURE_FALLS_BACK — exists (keep).
2. FACTORY_FAILURE_FALLS_BACK — NEW: `openEngine` throws → null, factory counted, no init.
3. FIRST_INIT_FAILURE_FALLS_BACK — NEW: fake whose `init` throws → null, `closeCalls == 1`.
4. UNREADY_ENGINE_FALLS_BACK — exists (keep).
5. SUCCESS_INITIALIZES_ONCE — NEW (strengthens existing success test): `initCalls == 1`, identity.
6. FAILED_CREATION_DOES_NOT_LEAK — NEW: after factory-throw, gate holds no engine; a second
   `start` with good factory still succeeds (proves no retained invalid state).

Owner tests (`NativeChewingEngineOwnerTest`, pure JUnit):
7. OWNER_REUSE_DOES_NOT_REINIT — exists (keep).
8. CLOSE_ONCE — exists (keep).
9. OWNER_INIT_FAILURE_PROPAGATES — NEW: factory-ok + init-throw → `ensureStarted` throws,
   engine NOT retained (second `ensureStarted` retries factory). This pins owner as
   non-swallowing container; gate's test #3 pins the fallback conversion.

Note on #3 vs #9: no double coverage — #9 asserts throw + no-retain at owner level;
#3 asserts null + close-once at gate level. Both needed, different seams.
