# KU-Yin v0.2 Worklog (keyboard-shell)

Authoritative handoff / context-compression doc for `feat/keyboard-shell-v0.2` (PR #4, Draft).
Spec: `docs/superpowers/specs/2026-09-13-keyboard-shell-v0.2-design.md`.
Architecture/donor audit: `docs/KU-YIN-V0.2-DONOR-AUDIT.md`.
Production architecture (not README/prototype guesses) is source of truth.

## Current state (2026-09-14)

Implementation baseline reviewed for this round:
`1d12f49d05ff2c22cadaf93135c741a83b659c4c`.

- Branch: `feat/keyboard-shell-v0.2`; base `main` remains untouched.
- PR #4 remains **Draft**. Do not merge `main`; do not publish a release.
- GitHub Actions Build run `34825790466` / run #128 completed **SUCCESS** on
  exact head `1d12f49`.
- Exact-run artifacts: `debug-apk`, `release-apk`, `runtime-smoke-report`.
- M1 architecture review and M2 Google AI Studio/open-source donor audit are now
  recorded in `docs/KU-YIN-V0.2-DONOR-AUDIT.md`.
- No production code was changed during M1/M2.

Recent implementation/handoff commits before M1/M2:

- `9dbeadc` fix(ui): restore v0.2 keyboard shell compilation
- `a91300a` test(ime): lock composition commit boundaries
- `1d12f49` docs(v0.2): record R0-R6 worklog and handoff

## Architecture (as built, not aspirational)

```text
┌──────────────────────────────────────┐
│             Keyboard UI              │
│ KeyboardView / CandidateView         │
│ SymbolPicker (compat) / Symbols/Emoji│
└─────────────────┬────────────────────┘
                  │ ImeCommand
                  ▼
┌──────────────────────────────────────┐
│         KeyboardController           │
│ RuntimeState / InputMode / Page      │
│ BottomRowProfile + Validator         │
└─────────────────┬────────────────────┘
                  │ ImeEffect (8 kinds, exhaustive when)
                  ▼
┌──────────────────────────────────────┐
│      ChewingInputMethodService       │
│ dispatchCommand → reduce → execute   │
│ Android lifecycle / InputConnection  │
└───────────┬──────────────────────────┘
            ▼
┌──────────────────────────────────────┐
│   AndroidChewingEngine / libchewing  │
│   (pinned prebuilt 3587ba33)         │
└──────────────────────────────────────┘

Settings UI → KeyboardPreferencesRepository → KeyboardPreferences
→ KeyboardShellLayoutResolver → ResolvedKeyboardLayout → KeyboardView
```

Boundaries enforced:

- View emits `ImeCommand` only (no InputConnection/Engine/SharedPreferences access).
- Service executes Android/framework effects only.
- `KeyboardController` is runtime-transition source of truth.
- `KeyboardPreferencesRepository` is settings source of truth.
- libchewing is decoder source of truth.

M1 verdict: **keep these boundaries**. Do not replace them with the Google AI
Studio engine, a second state machine, or a second preference store.

## Decisions already locked

- Compile blocker fixed minimally (`ResolvedKey::toRenderedKey` callable
  reference → `it.toRenderedKey()` lambda). No KeyboardView rewrite.
- R2 bridge audit: `executeEffect` covers all 8 `ImeEffect`s with unique paths.
  No filler refactor is needed.
- Chinese → English with active composition uses `CommitComposition` before
  switching. English → Chinese switches directly.
- EditorPolicy remains authoritative over preferences for secure/ASCII-only
  editors.
- Legacy `KeyboardView.onKeyPress` / `setLayout(KeyDef)` is retained only as a
  compatibility surface; removal is not part of the current slice.
- No speculative JNI/composition change without device reproduction.
- Google AI Studio is a **UI/UX donor only**; libchewing/current backend stays.
- No wholesale Compose migration inside PR #4 without a new approved
  architectural decision.

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

Generated branding assets are on hold until provenance/licensing and visual
identity are explicitly reviewed.

### Open-source donors

- **fcitx5-android / fcitx5-chewing** — best candidate/composition references;
  LGPL-family and architecture-compatible. Study horizontal/expanded candidate
  components and libchewing integration first.
- **FlorisBoard** — strong abstract-key/layout/profile reference; Apache-2.0.
  Borrow the data-driven principle, not its full framework.
- **HeliBoard** — useful custom-layout/state-selector UX reference, but GPL-3.0;
  reference-only unless licensing strategy changes.
- **Trime** — useful Chinese IME/Rime frontend reference, GPL-3.0; reference-only
  for PR #4.
- **AOSP LatinIME** — Android IME/editor/layout reference; verify per-file
  headers before any reuse.

### Next model decision before UI donor port

Current `ResolvedKey(label, command, widthPct, isSpecial)` is intentionally thin.
The M1/M2 audit recommends a small future-safe extension before visual donor
work:

- stable `id`;
- optional `secondaryLabel`;
- semantic visual `role` (character / function / space / action).

Do **not** add a full selector DSL/popup metadata in v0.2. This recommendation
requires explicit approval before production code changes.

## Open questions / suspects for device verification

1. `executeChewingKey` fallback: a libchewing-ignored keystroke mid-composition
   falls through to literal commit + `chewing.reset()`, wiping composition.
   This is still a P1 **suspect**, not a confirmed defect. Reproduce on JNI/device
   before changing it.
2. Service-level continuous composition / candidate continuation cannot be fully
   proven by plain JVM tests (service needs Android framework; engine needs JNI).
   Controller boundaries are locked by tests; full proof still requires device
   input such as `我應該是`, `我是誰`, `今天晚上要去哪裡`.
3. Local build outputs `app/src/main/assets/` (dictionary `.dat`) and
   `app/src/main/cpp/.deps/` come from bootstrap. A future hygiene commit may
   add them to `.gitignore`; not part of M1/M2.

## Security observations

M1/M2 re-checked the key production invariant rather than trusting the donor:

- production manifest has **no `android.permission.INTERNET`**;
- `android:allowBackup="false"` remains set;
- no cloud/telemetry/API-secret path was imported;
- no key/preedit/candidate logging was added;
- libchewing system/user data remains app-private;
- native dependency pins were not changed.

The AI Studio prototype conflicts with these rules (`INTERNET`, backup enabled,
Firebase/network dependencies), so it cannot be wholesale merged.

## Verification evidence

Exact implementation head `1d12f49`:

- CI Build run `34825790466` / #128: **SUCCESS**.
- 6 Python contracts: passed in the preceding R0-R6 verification.
- JVM unit suite: 69/69 in the preceding R0-R6 verification.
- Debug and Release APK artifacts were generated on the exact head.
- Experimental runtime smoke remains limited by the known headless-emulator
  `input window should become visible` assertion; it is not used as proof of
  physical-device usability.

M1/M2 itself is documentation/review-only; no new behavioral verification is
claimed from a docs commit.

## Device verification

**Not done in this session.** This remains a release blocker.

Physical-device priority checks:

1. multi-syllable/long-phrase Chinese composition;
2. candidate selection followed by continued composition;
3. Chinese → English auto-commit and immediate English typing;
4. English → Dachen return;
5. password/FORCE_ASCII editor stays safe English;
6. later v0.2 shell pages/settings survive restart and reset safely.

## Updated roadmap

```text
M-device  physical continuous-composition gate (parallel user validation)
    │
    ├─ if fallback reset reproduces: isolated TDD + device/JNI fix
    │
    ▼
Decision  ResolvedKey future-safe seam (id + secondaryLabel + role?)
    ▼
M3        CandidateState + horizontal / expanded presentation
    ▼
M4        Symbols / Emoji providers + local recents
    ▼
M5        configurable bottom-row Settings UI + validation
    ▼
M6        remaining Settings UX
    ▼
M7        AI-Studio donor visual adaptation in current KeyboardView
    ▼
M8        performance / security / accessibility audit
    ▼
M9        final physical-device matrix
    ▼
M10       release preparation
```

## Handoff rule

Every agent continuing this branch should read, in this order:

1. `docs/superpowers/specs/2026-09-13-keyboard-shell-v0.2-design.md`
2. `docs/KU-YIN-V0.2-WORKLOG.md`
3. `docs/KU-YIN-V0.2-DONOR-AUDIT.md`
4. current PR #4 diff / current branch head

Do not infer current truth from an old chat or from the Google AI Studio
prototype.
