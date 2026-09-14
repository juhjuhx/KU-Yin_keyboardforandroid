# KU-Yin v0.2 Architecture + Donor Audit

This document records the M1/M2 architecture review and donor analysis for
`feat/keyboard-shell-v0.2` / PR #4. It is a decision-support document, not a
second implementation spec. The authoritative product architecture remains
`docs/superpowers/specs/2026-09-13-keyboard-shell-v0.2-design.md`.

## Verified baseline

- PR #4 remains Draft and targets `main`.
- Reviewed branch head: `1d12f49d05ff2c22cadaf93135c741a83b659c4c`.
- GitHub Actions Build run `34825790466` / run #128 completed successfully.
- Artifacts from that exact head: `debug-apk`, `release-apk`, and
  `runtime-smoke-report`.
- The experimental runtime smoke job still has the known headless-emulator IME
  window-visibility limitation; this does not replace physical-device testing.
- Production manifest still has no `android.permission.INTERNET` and keeps
  `android:allowBackup="false"`.

## Architecture verdict

The current v0.2 boundaries are sound enough to continue. Do not replace them
before UI work.

```text
KeyboardView / CandidateView / future Symbols+Emoji surface
                    │
                    │ ImeCommand
                    ▼
            KeyboardController
     RuntimeState / InputMode / KeyboardPage
                    │
                    │ ImeEffect
                    ▼
       ChewingInputMethodService
      Android lifecycle / InputConnection
                    │
                    ▼
       AndroidChewingEngine / libchewing

Settings UI
    │
    ▼
KeyboardPreferencesRepository
    │
    ▼
KeyboardPreferences
    │
    ▼
KeyboardShellLayoutResolver
    │
    ▼
ResolvedKeyboardLayout
```

Source-of-truth rules:

1. libchewing remains the Chinese decoder source of truth.
2. `KeyboardController` remains the runtime transition source of truth.
3. `KeyboardPreferencesRepository` remains the settings source of truth.
4. `ImeCommand` is the UI/backend boundary.
5. `ImeEffect` is the controller/Android framework boundary.
6. `ChewingInputMethodService` executes effects; it must not become a second
   layout/settings state machine.

### Confirmed strengths

- UI emits semantic commands instead of calling `InputConnection` directly.
- Controller transitions are pure and testable.
- Secure editors can normalize to English/ASCII before command handling.
- Chinese -> English with active composition has an explicit
  `CommitComposition` boundary.
- Symbols/emoji already have page-level runtime states and local-only literal
  insertion semantics.
- Decoder-owned candidate paging is preserved.

### Material gaps before v0.2 release

- Physical-device proof of continuous multi-syllable composition is still
  missing.
- `executeChewingKey` still has a suspicious fallback: if libchewing produces
  no state update, the service finishes composition, commits a literal fallback,
  resets the engine, and clears candidates. This is a P1 suspect, not a confirmed
  defect. Do not change it without JNI/device reproduction.
- `ResolvedKey` is intentionally minimal and currently lacks the metadata needed
  for the future Layout Editor path (stable ID, secondary label, visual role).
- Candidate presentation is still list-only and has no explicit presentation
  state model for expand/collapse.
- Symbols and emoji are currently bundled proof-of-concept rows, not separate
  repositories/providers with category/recents behavior.
- Settings already have typed preferences but the user-facing v0.2 editing UX is
  incomplete.

## Google AI Studio donor audit

The user-provided Google AI Studio project is a **UI/UX donor only**. It must
not replace the current production engine, controller, preferences, security
model, or Android effect boundary.

| Donor surface | Decision | Reason / integration rule |
| --- | --- | --- |
| `ime/ui/KuYinKeyboardUi.kt` overall visual hierarchy | **ADAPT** | Reuse spacing, key hierarchy, candidate density and page arrangement as design reference; implement against `ResolvedKeyboardLayout -> KeyboardView`. Do not wholesale migrate PR #4 to Compose. |
| `KeyboardThemeColors` / key visual treatment | **ADAPT** | Useful color/role reference; map to existing `ImePalette`/future visual-role metadata. |
| `CandidateBar` | **ADAPT** | Useful density/interaction reference. Preserve libchewing ordering and decoder-owned paging; candidate selection must not become an unconditional commit boundary. |
| `ZhuyinKeyboardLayout` | **ADAPT** | Visual proportions and labels may inform the current Dachen renderer. Dachen mapping remains the validated production source of truth. |
| `EnglishKeyboardLayout` | **ADAPT** | Useful visual arrangement; behavior must remain current `InputMode.ENGLISH` + `ImeCommand`. |
| `SymbolsKeyboardLayout` | **ADAPT** | Useful page/navigation arrangement; production data remains local and goes through resolver + `InsertText`. |
| `EmojiKeyboardLayout` | **ADAPT** | Useful category/layout reference; production v0.2 stays bundled/local-only with local recents if added. |
| `SingleKeyView` / `ActionKeyView` / `ZhuyinKeyView` | **REWRITE** | Recreate the visual ideas in the existing custom `KeyboardView`; copying Compose components would introduce a second UI architecture. |
| `ime/engine/KuYinEngine.kt` | **DROP** | Creates a second engine/state machine and replaces libchewing semantics. Candidate selection commits and clears composition. |
| `ime/engine/ZhuyinDictionary.kt` | **DROP** | Competes with libchewing and duplicates dictionary/learning behavior. |
| `ime/settings/KeyboardSettings.kt` | **DROP / REFERENCE ONLY** | Creates a second `kuyin_settings` preference store. Map useful setting ideas into `KeyboardPreferences` instead. |
| `ime/api/KuYinExtensionApi.kt` | **DEFER** | Extension-provider idea may be revisited after v0.2; not needed for the approved first-release scope. |
| `ime/sync/UpstreamSyncManager.kt` | **DROP** | Performs network/GitHub polling. Violates the zero-network IME invariant. |
| Firebase AI / AppCheck | **DROP** | Cloud typing path, dependency and privacy cost; outside product scope. |
| OkHttp / Retrofit / logging interceptor | **DROP** | No production network path is allowed. |
| Room feedback database | **DROP** | Adds a second persistent data model and dependencies without a v0.2 requirement. |
| Clipboard bar | **DROP for v0.2** | Sensitive-data surface and outside the approved customization scope. |
| `.env` / secrets plugin / API-key plumbing | **DROP** | IME production must not depend on API secrets. |
| generated JPEG/WebP branding assets | **HOLD** | Do not import until provenance/licensing and final visual direction are reviewed. |
| Compose as the IME rendering framework | **DEFER / GRILL REQUIRED** | Could be evaluated in a separate architectural proposal, not smuggled into PR #4. |

### Donor build/security conflicts confirmed

The AI Studio prototype currently enables Compose and includes Firebase AI,
Firebase AppCheck, Room, OkHttp, Retrofit, a logging interceptor, Secrets Gradle
Plugin and Google Services. Its manifest requests `INTERNET` and enables Android
backup. Those choices are incompatible with the current KU-Yin production
security contract and must not be merged as-is.

The prototype `KuYinEngine` also owns its own `KeyboardMode`, shift state,
Zhuyin composing buffer, dictionary and candidate StateFlows. Its candidate
selection commits the selected text and clears composition, so it cannot be
used as the v0.2 decoder/backend.

## Open-source donor matrix

| Project | License / status | Best reference areas | KU-Yin policy |
| --- | --- | --- | --- |
| **fcitx5-android** | LGPL-family; active Android IME framework | Chewing integration, horizontal + expanded candidate views, candidate paging, keyboard theming, mobile IME lifecycle | **GREEN** for close architectural study; copy/adaptation still requires per-file SPDX/license review. Best candidate donor for M3 candidate UX. |
| **fcitx5-chewing** | LGPL-2.1-or-later | libchewing integration semantics, composition/candidate handling | **GREEN**; highest-priority behavioral reference for decoder semantics. |
| **FlorisBoard** | Apache-2.0 | Abstract key data, computed/conditional key model, layout/profile architecture, theme/emoji/settings UX | **GREEN/YELLOW**; safe architecture reference and potentially reusable Apache code with attribution/review. Do not import its whole framework. |
| **HeliBoard** | GPL-3.0 | Layout formats, key state selectors, functional keys, custom layouts, offline UX | **YELLOW**; architecture/UX reference only unless project licensing strategy changes. |
| **Trime** | GPL-3.0 | Chinese IME frontend patterns, Rime/JNI integration, configurable keyboard/theme semantics | **YELLOW**; reference only for PR #4. Do not copy production source into LGPL project without a deliberate license decision. |
| **AOSP LatinIME** | AOSP / Apache-style source areas; verify per-file headers | Android IME/editor behavior, key/action conventions, keyboard state patterns | **GREEN/YELLOW**; use primary AOSP source and verify each file before reuse. |
| **Google AI Studio prototype** | User-provided prototype; third-party/generated assets still need provenance review | Visual density, candidate bar, English/symbol/emoji presentation, settings grouping | **ADAPT/REWRITE**, never wholesale merge. |

### Concrete upstream references worth studying

- fcitx5-android:
  - `input/candidates/horizontal/HorizontalCandidateComponent.kt`
  - `input/candidates/expanded/ExpandedCandidateLayout.kt`
  - `input/candidates/expanded/window/*`
  These demonstrate a separated horizontal/expanded candidate presentation and
  decoder-driven paging rather than putting all behavior in one view.
- FlorisBoard:
  - `ime/keyboard/KeyData.kt`
  Its `AbstractKeyData -> compute(evaluator) -> KeyData` pattern demonstrates how
  display/input semantics and conditional state can stay data-driven. KU-Yin
  should borrow the principle, not the whole selector DSL in v0.2.
- HeliBoard:
  - `layouts.md`
  Useful concepts include functional-key types, explicit widths, shift/input
  variation/keyboard-state selectors, and sanity checks for user layouts.
  HeliBoard also documents that excessive keys/text can make layouts break,
  reinforcing the need for validation in KU-Yin's future Layout Editor.

## Recommended v0.2 donor strategy

### M3 — Candidate architecture

Use fcitx5-android as the primary candidate UX reference. Introduce an explicit
presentation model before visual polish, while keeping libchewing candidate
ordering/paging authoritative.

Proposed shape (design-level only):

```text
libchewing candidate page
        │
        ▼
CandidateState
  items
  canPageBackward
  canPageForward
  expanded
        │
        ├── horizontal strip
        └── expanded grid/panel
```

Do not implement a second candidate ranking layer.

### M4 — Symbols / Emoji

Keep the current page states and `ImeCommand.InsertText`. Extract bundled symbol
and emoji data from the resolver when categories/recents are introduced. Recent
emoji must remain app-private and local. No network search/API.

### M5 — Configurable bottom row

Keep `BottomRowProfile` + validator as source of truth. UI editing must sanitize
invalid persisted profiles and always preserve a safe route back to letters.
Do not expose arbitrary Dachen remapping in v0.2.

### M6 — Settings

Extend the existing typed `KeyboardPreferencesRepository`; do not add a second
preference store. Reset-to-default must reset shell preferences only and must
not delete the libchewing user dictionary.

### M7 — Visual polish

Port **ideas**, not the AI Studio framework:

- rounded/role-aware key surfaces;
- clearer distinction between character and function keys;
- secondary QWERTY labels on Dachen keys;
- modern candidate spacing;
- coherent English/Symbol/Emoji visual language;
- dark/light parity.

The approved product rule remains: **精簡、高效、美觀**. Visual work is rejected
if it adds noticeable key latency, allocation churn, layout instability or
weakens touch targets/accessibility.

## Proposed future-safe `ResolvedKey` seam — decision required

Before donor UI porting, the current `ResolvedKey(label, command, widthPct,
isSpecial)` needs a small decision. The recommended option is deliberately
smaller than FlorisBoard/HeliBoard's full selector systems.

**Option A — recommended**

Add only:

- stable `id`;
- optional `secondaryLabel`;
- semantic visual `role` (character / function / space / action).

Keep `command` and `widthPct`. Do not introduce selector DSL/popup metadata yet.
This is enough for secondary Dachen legends, theme roles, stable settings/preview
references and a later Layout Editor without overbuilding v0.2.

**Option B — minimal**

Add `secondaryLabel` only. Lowest immediate cost, but future bottom-row/layout
editor work will need another public-model change for stable IDs/roles.

**Option C — full selector model now**

Add Floris-like state selectors, popups and arbitrary metadata immediately.
Not recommended for v0.2; it expands scope well beyond the approved A-level
customization and duplicates complexity before a real user need exists.

No production change should be made for this seam until the option is approved.

## Release blockers / evidence status

- **P0:** none found in this audit.
- **P1 suspect:** `executeChewingKey` fallback can reset an active composition
  when libchewing ignores a key. Requires physical/JNI reproduction before fix.
- **P1 release gate:** real-device continuous phrase + candidate continuation
  still unverified.
- **P2:** candidate presentation, symbols/emoji UX, bottom-row editor and
  Settings UX remain incomplete v0.2 work.
- **P3:** legacy `KeyboardView.onKeyPress` compatibility surface and minor build
  hygiene can be removed later, not during architecture-sensitive slices.

## Next execution order

```text
M-device: physical composition verification (parallel user gate)
    │
    ├── if fallback reset reproduces -> isolated TDD/JNI/device fix
    │
    ▼
ResolvedKey seam decision
    ▼
M3 CandidateState + horizontal/expanded candidate presentation
    ▼
M4 Symbols / Emoji providers + local recents
    ▼
M5 Bottom-row editor + validation
    ▼
M6 Settings UX
    ▼
M7 visual donor adaptation
    ▼
M8 performance / security / accessibility audit
    ▼
M9 final physical-device matrix
    ▼
M10 release preparation
```

PR #4 remains Draft throughout these stages. No main merge or release is
justified until the physical-device acceptance gate is complete.
