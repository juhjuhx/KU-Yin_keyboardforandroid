# KU-Yin Architecture (normative, Compose/C4 line)

> Supersedes all View-era descriptions. For history see `docs/HISTORY.md` and
> `docs/archive/`. If any older doc contradicts this file, this file wins.

```
Android editor
    │  EditorInfo / InputConnection
    ▼
KuYinInputMethodService
    ├── EditorPolicy.from() ............ sensitive/ASCII/composition/learning normalizer
    ├── EditorEnterPolicy.from() ....... inline Enter label/presentation/behavior
    ├── userPreferredMode/effectiveMode . forced editors override temporarily
    ├── KuYinEngine .................... presentation only: KeyboardMode,
    │                                     ShiftState, editor policy
    └── ComposeDecoderSession .......... ALL Chinese state: preedit,
            │                             candidates, paging, selection,
            │                             space/enter/backspace/punct/reset
            ├── ChewingEngineSession (PRODUCTION when native startup succeeds)
            │       └── AndroidChewingEngine .. sole JNI caller; service-owned
            │               │                   successful startup initializes once
            │               └── JNI chewing_jni.cpp (C++17)
            │                       │  pinned: prebuilt 3587ba33 / src a6a8fa4
            │                       └── libchewing C API (system .dat + userdict.dat,
            │                                            app-private)
            └── ZhuyinDictionarySession (startup-failure fallback)
```

## Boundaries (normative)

- Framework: service owns lifecycle + InputConnection side effects only.
- Policy: `EditorPolicy` + `EffectiveModePolicy`; forced editors can never
  permanently change `userPreferredMode` and can never reach Zhuyin composition.
- Presentation: `KuYinEngine` owns mode/shift/policy. UI reads `engine.mode`
  and `session.state`; UI/service never read engine composition/candidates.
- Decoder: `ComposeDecoderSession` + `DispatchResult(commitText, consumed,
  additionalCommits=[])`. Apply order is always commitText then
  additionalCommits (single shared UI path per action).
- Enter: service snapshots `EditorInfo` primitives into one immutable
  `EditorEnterPolicy`; UI receives only label/presentation. Zhuyin Enter applies
  ordered session commits first and consults the editor policy only when unconsumed.
  `NO_ENTER_ACTION` suppresses standard/custom inline actions; custom labels dispatch
  `actionId`. `performEditorAction(false)` does not trigger a second newline effect.
- Native: only `AndroidChewingEngine` touches JNI symbols. UI never does.
- Storage: system dicts + `userdict.dat` under `noBackupFilesDir/libchewing`;
  `allowBackup=false`. No network, no telemetry, no cloud candidates.
- Fallback: `ZhuyinDictionarySession` stays as startup-failure fallback and
  test/comparison harness; never a user-facing decoder toggle.
- Donor: old View shell (PR #4) is reference only, not a product path.
- UI (M4 target, not this doc's mandate): candidate strip, canonical Dachen
  from `KeyboardLayout.Dachen`, row-inset geometry, unified touch surface,
  SymbolCatalog, long-press model, emoji categories, compact toolbar.

## Test boundaries

Python contracts pin static invariants; JVM tests pin behavior (fresh counts in
`docs/CURRENT_STATE.md`);
instrumentation/runtime smoke covers lifecycle+JNI on device. Runtime-only
gates (touch geometry, rotation, OEM matrix, QR scan art) are never claimed
from static evidence.
