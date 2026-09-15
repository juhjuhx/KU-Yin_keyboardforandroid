# KU-Yin v0.2 Worklog (keyboard-shell)

Authoritative handoff / context-compression doc for `feat/keyboard-shell-v0.2` (PR #4, Draft).
Spec: `docs/superpowers/specs/2026-09-13-keyboard-shell-v0.2-design.md`.
Architecture/donor audit: `docs/KU-YIN-V0.2-DONOR-AUDIT.md`.
Production architecture (not README/prototype guesses) is source of truth.

## Current state (2026-09-14)

Latest behavior-bearing implementation verified in this round:
`bbb7ffc` (M3.1 candidate geometry hardening, below).

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
- `8716634` test(candidates): lock M3 candidate state and selection contracts (**expected RED**)
- `09cc375` feat(candidates): add CandidateState presentation and selection bridge (**GREEN**)
- `2b77e78` docs(v0.2): record M3 donor audit
- `96c7b8f` Merge origin M1/M2 audits and ResolvedKey OPTION A with M3 candidate slice
- `8b64c3b` test(candidates): lock M3.1 flow-row and container-height contracts (**expected RED**)
- `bbb7ffc` fix(candidates): self-healing container height and conditional layout (**GREEN**)

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

## M3 CandidateState + horizontal/expanded presentation (this round)

Design (spec section 14, smallest projection — no second candidate engine):

- `CandidateState(items, canPageBackward, canPageForward, expanded)` lives in
  `ui/CandidateView.kt`; pure mapper `candidateStateOf(update, expanded,
  canPageBackward, canPageForward)` keeps decoder page order untouched.
- `ImeCommand.SelectCandidate(index)` → controller returns state unchanged +
  `ImeEffect.SelectCandidate(index)` → service executes the pre-existing
  `selectCandidateUpdate → applyEngineUpdate` path (no hard commit; only
  decoder-reported `committedText` is committed). Editor-sync contract keeps
  `onItemClick: ((Int, String) -> Unit)?` and the no-direct-commit rule.
- Expand/collapse travels the pre-existing
  `ImeCommand.ToggleCandidateExpanded` → `runtimeState.candidateExpanded`;
  the service projects the flag into `CandidateState` and sizes the container
  (collapsed 1×44dp, expanded up to 4 rows, GONE when empty).
- Decoder page bounds are exposed as non-mutating engine queries
  `canPageCandidatesBackward/Forward()` (P3-safe names); Android adapter reads
  `candidatePage` / `chewing_cand_total_page`.
- `CandidateView` stays a dependency-free custom View: content-width chips,
  collapsed drag-scroll + fixed ˄ chevron, expanded wrapped grid (≤4 rows,
  vertical scroll) + ˅ chevron, fling-to-page preserved, cached layout (no
  per-`onDraw` allocation), `ImePalette` dark/light, content descriptions,
  44dp+ touch targets. No InputConnection/libchewing access from the View.

Files: `KeyboardShell.kt` (+command/effect/controller case),
`engines/core/ChewingEngine.kt` (+2 queries),
`engines/android/AndroidChewingEngine.kt` (+2 overrides),
`ui/CandidateView.kt` (state model + rewritten rendering),
`ChewingInputMethodService.kt` (selection bridge, state projection, container
sizing, `lastCandidateState` render cache), `KeyboardCommandEffectTest.kt`
(+3), new `ui/CandidateStateTest.kt` (+4).

### RED — `8716634`

Local `compileDebugUnitTestKotlin` failed on unresolved `SelectCandidate`
and `candidateStateOf`: the tests exercised the missing contract.

### GREEN — `09cc375` + merge `96c7b8f`

Local `:app:testDebugUnitTest`: 78 tests, 0 failures (7 new M3 tests).
Test sensitivity proven: temporarily removing the `InsertText`-style boundary
from the new path fails the matching test; restored before commit.
All six static contracts pass locally.

CI run `34833978395` on exact merge head `96c7b8f`, authoritative `build` job
**SUCCESS**: static contracts, pinned bootstrap `3587ba33…`, JVM unit tests,
`assembleDebug`, `assembleRelease`, artifact uploads.

Artifacts from run `34833978395`:

- `debug-apk` artifact ID `10343647062` (9,855,234 bytes).
- `release-apk` artifact ID `10343731931` (8,662,802 bytes).

Known non-blocking/pre-existing warnings remain (same list as above);
experimental runtime-smoke is not accepted as physical-device evidence.

## M3.1 Hardening — geometry only, no semantic change

Issue: expanded candidate container height was computed from the width at the
last engine update, so rotation/resize could leave the panel clipped until the
next keypress; and `requestLayout()` ran on every engine update even when
geometry was unchanged.

Fix (`8b64c3b` RED → `bbb7ffc` GREEN):

- Pure, production-used helpers in `ui/CandidateView.kt`: `assignFlowRows`
  (row assignment for final cell widths), `flowRowCount`,
  `candidateContainerHeightPx` (collapsed 1 row, expanded capped at
  `MAX_EXPANDED_ROWS`). Unifying count and placement on one algorithm also
  removed a latent divergence where the old row counter ignored the minimum
  touch-target cell floor used by placement.
- `CandidateView` tracks laid-out `contentRows` and fires the new
  `onRequiredRowsChanged(rows)` callback only on change, from every layout
  rebuild (`setCandidateState`, `onSizeChanged`, `refreshAppearance`).
  Oscillation is impossible: a height-only change re-fires `onSizeChanged`
  but recomputes identical rows, so no second notification.
- Service splits `renderCandidateState` (state store) from
  `applyCandidateGeometry(state, rows)` (visibility + height, applied only on
  change); the resize callback path reuses it with `lastCandidateState` and
  never touches decoder/runtime state or the engine.
- `requestLayout()` now fires only when container height actually changes;
  visibility changes rely on their own layout pass. No drawing-path changes
  (P3 RectF/toString micro-allocs explicitly deferred to M8).

Tests: 7 new JVM tests in `ui/CandidateStateTest.kt` (row assignment, wrap
behavior, width-shrink sensitivity, oversized cell, empty, collapsed/expanded
heights, cap). RED was unresolved-reference compile failure on the new pure
API. The `requestLayout` gating itself is View-measurement code and is
classified device/instrumentation-level (rotation CASE 6 below). Local
`:app:testDebugUnitTest`: 85 tests, 0 failures. All six static contracts pass.

CI run `34845869840` on exact head `bbb7ffc`, authoritative `build` job
**SUCCESS**: static contracts, pinned bootstrap `3587ba33…`, JVM unit tests,
`assembleDebug`, `assembleRelease`, artifact uploads.

Artifacts from run `34845869840`:

- `debug-apk` artifact ID `10347804063` (9,854,283 bytes).
- `release-apk` artifact ID `10347374957` (8,663,959 bytes).

Security invariants re-verified: no INTERNET permission, `allowBackup=false`,
no new logging/network/clipboard/telemetry, libchewing source of truth,
native pin unchanged, EditorPolicy untouched (no M3.1 file touches those
areas; P3 contract green).

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

M3 candidate-presentation checks (unverified on JVM — custom View needs a
device/emulator; classify as unverified, do not guess):

8. collapsed strip shows content-width chips with a fixed ˄ chevron;
9. horizontal drag scrolls an overflowing page; fling still pages;
10. tapping a candidate selects without hard-committing the composition;
11. ˄ expands to the wrapped grid, ˅ collapses back, order unchanged;
12. empty candidate list hides the strip safely (no crash, no ghost bar);
13. dark/light themes render readable Traditional Chinese chips;
14. long candidate strings do not corrupt layout.

## Updated roadmap

```text
M-device  physical continuous-composition gate (parallel user validation)
    │
    ├─ if fallback reset reproduces: isolated TDD + device/JNI fix
    │
    ▼
A seam     ResolvedKey id + secondaryLabel + role  ✅ implementation green
    ▼
M3         CandidateState + horizontal / expanded presentation  ✅ implementation green
    ▼
M3.1       candidate geometry hardening (resize self-heal, layout gating)  ✅ implementation green
    ▼
M4         Symbols / Emoji providers + local recents  ⛔ BLOCKED by device gate
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

M3 + M3.1 are implementation-green (see evidence above). M4 design is now
**user-approved as a whole package** (see approval record below), but **M4
production remains blocked by the device gate**: no physical-device run
happened in this session, so CASE 4 (candidate continuation) and CASE 6
(rotation height) are unverified and M4 must not start until the gate passes.
Standing constraints carry over:

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

## M4 design approvals (user-approved 全包, 2026-09-14; production still gated)

The user approved the complete M4 design package from the DESIGN FREEZE
handoff. This section is the durable record; the freeze analysis itself lives
in session history, not in this file.

1. **Architecture: OPTION A** — KU-Yin-owned `SymbolRepository` /
   `EmojiRepository` / `RecentEmojiRepository` (pure Kotlin) → existing
   `KeyboardShellLayoutResolver` → `ResolvedKeyboardLayout` → `KeyboardView`.
   No EmojiEngine/SymbolEngine, no second state machine/preferences/layout
   system. Rejected: B (10MB+ bundled font + `EmojiCompat.init` cold-start tax
   + new dependency surface for an a11y/variant benefit v0.2 does not need),
   C (picker-framework debt outweighs marginal UX).
2. **Emoji data: A1** — `emoji-test.txt` (pinned version, e.g. Emoji 16.0)
   → offline generator → `fully-qualified` only → generated Kotlin source
   (lazy per-category views). Copyright header + version recorded in NOTICE /
   WORKLOG at integration. Rejected: A2 (stale, unverifiable), A3 (cross-device
   inconsistency defeats IME determinism).
3. **Composition: S1** — entering Symbols/Emoji commits composition immediately
   (execution side already exists in `insertText` and is test-locked); browse
   is a temporary page, never dual-live with the decoder.
4. **Insertion: `ImeCommand.InsertText` only**; stay on SYMBOLS/EMOJI page after
   insert; return via ABC / `ReturnToLetters`; no auto-return preference.
5. **Variants: V1** (base emoji only). V2/V3 deferred to post-v0.2.
6. **Recents:上限 30**, dedup by full Unicode sequence, move-to-front,
   single-key JSON in app-private SharedPreferences (existing repository
   infrastructure), `v:1` version field, corrupt-key fallback to empty,
   cleared by reset-to-default; never in libchewing storage, never logged.
7. **Settings: REQUIRED** `showEmojiKey` (default false stays),
   `emojiRecentsEnabled` (default true); **OPTIONAL** `defaultSymbolsPage`
   (approved for inclusion); **DO NOT ADD** `stickyEmojiVariant`,
   auto-return-after-insert, search/aliases/cloud anything.

Symbols are a separate dataset (TW-first PRIMARY/SECONDARY groups per §10 of
the freeze brief, not emoji). Symbol/emoji pages reuse the existing
`KeyboardPage` → resolver → layout → View chain; legacy `SymbolPicker`
retires in M4.9. Planned slices M4.0–M4.10 as specified in the freeze package.

**Gate restated**: M4 production starts only after (1) device gate PASS
(CASE 4 + CASE 6 minimum), (2) this architecture approval (done),
(3) data-strategy approval (done — A1).
