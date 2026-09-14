# KU-Yin v0.2 Worklog (keyboard-shell)

Authoritative handoff / context-compression doc for `feat/keyboard-shell-v0.2` (PR #4, Draft).
Spec: `docs/superpowers/specs/2026-09-13-keyboard-shell-v0.2-design.md`.
Architecture/donor audit: `docs/KU-YIN-V0.2-DONOR-AUDIT.md`.
Production architecture (not README/prototype guesses) is source of truth.

## Current state (2026-09-14)

Latest behavior-bearing implementation verified in this round:
`382969c77058928180e403d116a33553393e9d87`.

- Branch: `feat/keyboard-shell-v0.2`; base `main` remains untouched.
- PR #4 remains **Draft**. Do not merge `main`; do not publish a release.
- User approved the future-safe ResolvedKey seam as **Option A**.
- TDD RED commit `c95770c` intentionally added contract tests before production fields existed; Build #131 failed at `compileDebugUnitTestKotlin` on missing `id`, `secondaryLabel`, `KeyRole`, and `role`, while the static P0/P1/P2/P3 contracts passed.
- GREEN commit `382969c` adds the approved ResolvedKey metadata and preference behavior.
- GitHub Actions Build run `34829848579` / Build #132 main `build` job completed **SUCCESS** on exact implementation head `382969c`: all static contracts, pinned native bootstrap, JVM unit tests, `assembleDebug`, `assembleRelease`, and artifact uploads passed.
- Experimental runtime-smoke for Build #132 is still not physical-device proof and may complete independently of the authoritative build gate.
- M1 architecture review and M2 Google AI Studio/open-source donor audit remain recorded in `docs/KU-YIN-V0.2-DONOR-AUDIT.md`.

Recent commits:

- `9dbeadc` fix(ui): restore v0.2 keyboard shell compilation
- `a91300a` test(ime): lock composition commit boundaries
- `1d12f49` docs(v0.2): record R0-R6 worklog and handoff
- `f62b150` docs(v0.2): add architecture and donor audit
- `175e143` docs(v0.2): refresh architecture and donor handoff
- `c95770c` test(ui): lock future-safe resolved key contract (**expected RED**)
- `382969c` feat(ui): add stable resolved key metadata (**GREEN**)

## Architecture (as built, not aspirational)

```text
┌───────────────────────────────────────────┐
│                Keyboard UI                │
│ KeyboardView / CandidateView             │
│ SymbolPicker (compat) / Symbols / Emoji  │
└────────────────────┬──────────────────────┘
                     │ ImeCommand
                     ▼
┌───────────────────────────────────────────┐
│            KeyboardController             │
│ RuntimeState / InputMode / Page           │
│ BottomRowProfile + Validator              │
└────────────────────┬──────────────────────┘
                     │ ImeEffect (8 kinds)
                     ▼
┌───────────────────────────────────────────┐
│         ChewingInputMethodService         │
│ dispatchCommand → reduce → execute        │
│ Android lifecycle / InputConnection       │
└────────────────────┬──────────────────────┘
                     ▼
┌───────────────────────────────────────────┐
│      AndroidChewingEngine / libchewing    │
│      pinned prebuilt 3587ba33             │
└───────────────────────────────────────────┘

Settings UI
    ↓
KeyboardPreferencesRepository
    ↓
KeyboardPreferences
    ↓
KeyboardShellLayoutResolver
    ↓
ResolvedKeyboardLayout
    ↓
KeyboardView
```

Boundaries enforced:

- View emits `ImeCommand` only; it does not own InputConnection, decoder, or preferences.
- Service executes Android/framework/engine effects only.
- `KeyboardController` is runtime-transition source of truth.
- `KeyboardPreferencesRepository` is settings source of truth.
- libchewing is the Chinese decoder source of truth.
- `ResolvedKeyboardLayout` is the UI-facing layout contract.

M1 verdict remains: **keep these boundaries**. Do not replace them with the Google AI Studio engine, a second state machine, or a second preference store.

## Decisions already locked

- Chinese → English with active composition uses `CommitComposition` before switching. English → Chinese switches directly.
- EditorPolicy remains authoritative over preferences for secure/ASCII-only editors.
- Legacy `KeyboardView.onKeyPress` / `setLayout(KeyDef)` is retained only as a compatibility surface; removal is not part of the current slice.
- No speculative JNI/composition change without device reproduction.
- Google AI Studio is a **UI/UX donor only**; libchewing/current backend stays.
- No wholesale Compose migration inside PR #4 without a new approved architectural decision.
- **ResolvedKey Option A is now locked and implemented.** v0.2 exposes a small future-safe semantic key contract without introducing a full selector DSL or arbitrary per-key remapping.

## ResolvedKey Option A — implemented contract

Current UI-facing key model:

```text
ResolvedKey
├─ id              stable semantic/slot identity
├─ label           primary visible label
├─ secondaryLabel  optional presentation-only legend
├─ role            CHARACTER / FUNCTION / SPACE / ACTION
├─ command         ImeCommand
└─ widthPct        layout weight
```

Implementation rules:

- Dachen physical keys use semantic IDs based on the physical input code, e.g. `dachen:input:49` for `ㄅ` / `1` and `dachen:input:47` for `ㄥ` / `/`.
- Dachen `secondaryLabel` exposes the physical ASCII legend (`1`, `/`, etc.) without changing the command sent to libchewing.
- English physical keys use stable IDs such as `english:input:113`; shift changes `q` → `Q` visually while keeping the same stable ID and physical input command.
- Functional legacy keys use semantic IDs such as `english:shift` and semantic roles rather than the old `isSpecial` boolean.
- Symbol/emoji keys use deterministic page/row/column slot IDs; bottom-row keys use deterministic slot IDs (`bottom:left:*`, `bottom:center`, `bottom:right:*`). This makes future layout-editor overrides stable even if a visible label changes.
- `KeyboardPreferences.showSecondaryLabels=false` clears presentation metadata only; key `id`, `role`, and `command` remain unchanged.
- `KeyboardView` now carries the new metadata through its rendering model, but **secondary-label drawing and role-based visual styling are intentionally not part of this slice**.
- No composition, candidate-selection, JNI, or libchewing behavior changed in Option A.

This is the maximum metadata scope for v0.2 at this stage. Do **not** add a FlorisBoard/HeliBoard-style selector DSL, popup schema, arbitrary Dachen remapping, or Compose migration without a new design decision.

## M1/M2 architecture + donor findings

Full matrix: `docs/KU-YIN-V0.2-DONOR-AUDIT.md`.

### Google AI Studio prototype

**ADAPT / REWRITE as design reference:**

- keyboard visual hierarchy, spacing and action-key emphasis;
- candidate bar density/presentation;
- English / Symbols / Emoji page arrangement;
- theme/settings grouping ideas.

**DROP from production v0.2:**

- `KuYinEngine` and `ZhuyinDictionary` (second decoder/state machine);
- separate `kuyin_settings` store;
- Firebase AI / AppCheck;
- OkHttp / Retrofit / logging interceptor;
- network GitHub polling (`UpstreamSyncManager`);
- Room feedback database;
- clipboard collection/bar;
- `.env` / API-key plumbing;
- manifest INTERNET permission / backup-enabled configuration.

Generated branding assets remain on hold until provenance/licensing and visual identity are explicitly reviewed.

### Open-source donors

- **fcitx5-android / fcitx5-chewing** — primary candidate/composition references; LGPL-family and architecture-compatible. Study horizontal/expanded candidate components and libchewing integration first.
- **FlorisBoard** — strong abstract-key/layout/profile reference; Apache-2.0. Borrow the data-driven principle, not its full framework.
- **HeliBoard** — useful custom-layout/state-selector UX reference, but GPL-3.0; reference-only unless licensing strategy changes.
- **Trime** — useful Chinese IME/Rime frontend reference, GPL-3.0; reference-only for PR #4.
- **AOSP LatinIME** — Android IME/editor/layout reference; verify per-file headers before any reuse.

Option A intentionally adopts only the small common denominator validated across mature keyboard projects: stable key identity, display metadata, semantic role, command, and width. No donor source code was copied in this slice.

## Open questions / suspects for device verification

1. `executeChewingKey` fallback: a libchewing-ignored keystroke mid-composition falls through to literal commit + `chewing.reset()`, wiping composition. This is still a P1 **suspect**, not a confirmed defect. Reproduce on JNI/device before changing it.
2. Service-level continuous composition / candidate continuation cannot be fully proven by plain JVM tests (service needs Android framework; engine needs JNI). Controller boundaries are locked by tests; full proof still requires device input such as `我應該是`, `我是誰`, `今天晚上要去哪裡`.
3. Local build outputs `app/src/main/assets/` (dictionary `.dat`) and `app/src/main/cpp/.deps/` come from bootstrap. A future hygiene commit may add them to `.gitignore`; not part of the current usability slice.

## Security observations

Current production invariants remain:

- production manifest has **no `android.permission.INTERNET`**;
- `android:allowBackup="false"` remains set;
- no cloud/telemetry/API-secret path was imported;
- no clipboard collection was imported;
- no key/preedit/candidate logging was added;
- libchewing system/user data remains app-private;
- native dependency pin remains `3587ba3355711f0aca50136e787719f6562676b8`.

Build #132 passed the repository's P3 architecture/security contract before compilation/tests/build. The Option A slice is layout metadata only and adds no permissions, persistence, network, or decoder behavior.

## Verification evidence

### RED — `c95770c`

Build #131 / run `34829121770` was intentionally expected to fail:

- Dachen/P0/editor/P1/P2/P3 static contracts: passed.
- `compileDebugUnitTestKotlin`: failed because production `ResolvedKey` did not yet contain the newly required `id`, `secondaryLabel`, `role`, or `KeyRole` type.
- This proves the new tests exercised the missing contract rather than trivially passing.

### GREEN — `382969c`

Build #132 / run `34829848579`, authoritative `build` job:

- Dachen source contract: passed.
- P0 keyboard action contract: passed.
- editor synchronization contract: passed.
- P1 build contract: passed.
- P2 UI contract: passed.
- P3 architecture/security contract: passed.
- pinned native bootstrap: passed at `3587ba3355711f0aca50136e787719f6562676b8`.
- JVM unit tests: `BUILD SUCCESSFUL`.
- Debug APK: assembled and uploaded.
- Release APK: assembled and uploaded.

Artifacts from exact implementation run `34829848579`:

- `debug-apk` artifact ID `10342075740`, artifact ZIP SHA-256 `3070ac0f23b07fb4090a0ae7f00dd5dd670dc0a09d56135a156d406678049d30`.
- `release-apk` artifact ID `10341269819`, artifact ZIP SHA-256 `dc07253bf0a4b52f0570f101d2405c5131280789ca705c85c6e0efe4dbcdc478`.

Known non-blocking/pre-existing warnings remain:

- `SettingsActivity.getPackageInfo(String, Int)` deprecation warning.
- release lint reports Kotlin metadata-version warnings but `lintVitalRelease` and `assembleRelease` still complete successfully.
- experimental runtime-smoke is not accepted as physical-device evidence.

## Device verification

**Not done for v0.2 in this session.** This remains a release blocker.

Physical-device priority checks:

1. multi-syllable/long-phrase Chinese composition;
2. candidate selection followed by continued composition;
3. Chinese → English auto-commit and immediate English typing;
4. English → Dachen return;
5. password/FORCE_ASCII editor stays safe English;
6. secondary Dachen legends render correctly once the visual layer is implemented;
7. later v0.2 shell pages/settings survive restart and reset safely.

## Updated roadmap

```text
M-device  physical continuous-composition gate (parallel user validation)
    │
    ├─ if fallback reset reproduces: isolated TDD + device/JNI fix
    │
    ▼
A seam     ResolvedKey id + secondaryLabel + role  ✅ implementation green
    ▼
M3         CandidateState + horizontal / expanded presentation
    ▼
M4         Symbols / Emoji providers + local recents
    ▼
M5         configurable bottom-row Settings UI + validation
    ▼
M6         remaining Settings UX
    ▼
M7         AI-Studio donor visual adaptation in current KeyboardView
            + secondary labels / role-based styling
    ▼
M8         performance / security / accessibility audit
    ▼
M9         final physical-device matrix
    ▼
M10        release preparation
```

## Next handoff

Start M3 from the exact Option A implementation contract above. Candidate work must preserve these constraints:

- candidate ordering/paging remains decoder-owned;
- candidate selection must not hard-commit the whole composition unless libchewing reports committed text;
- `runtimeState.candidateExpanded` / `ImeCommand.ToggleCandidateExpanded` remain the UI-state boundary;
- no second candidate ranking engine;
- no cloud/network donor;
- horizontal/expanded UI may adapt fcitx5-android concepts after per-file license review.

Every agent continuing this branch should read, in this order:

1. `docs/superpowers/specs/2026-09-13-keyboard-shell-v0.2-design.md`
2. `docs/KU-YIN-V0.2-WORKLOG.md`
3. `docs/KU-YIN-V0.2-DONOR-AUDIT.md`
4. current PR #4 diff / current branch head

Do not infer current truth from an old chat or from the Google AI Studio prototype.
