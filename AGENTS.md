# AGENTS.md

## Project source of truth

Read in this order before making changes (later overrides earlier on conflict):

1. `AGENTS.md` (this file: instructions + order)
2. `docs/CURRENT_STATE.md` (what is active NOW)
3. `docs/ARCHITECTURE.md` (normative architecture)
4. `docs/DEVELOPMENT.md` (canonical build/toolchain)
5. `SECURITY.md` (security policy)
6. `docs/SECURITY_AUDIT.md` (evidence-backed audit)
7. `docs/UPSTREAM.md` (dependency/provenance truth)
8. `docs/ROADMAP.md` (future only)
9. `docs/HISTORY.md` (historical only)
10. `docs/COMPOSE-MAINLINE.md` (migration record only)
11. `docs/archive/` (superseded documents)

`README.md` is the public landing page, not an architecture authority.

## Core architecture

`InputMethodService → EditorPolicy (+preferred/effective mode) / UI → ComposeDecoderSession → ZhuyinDictionarySession (production) → Compose UI → InputConnection`, with `ChewingEngineSession → AndroidChewingEngine → JNI/C++ → libchewing C API` wired and tested as the D3 production target.

Keep Android lifecycle, UI and JNI boundaries explicit. UI should not call native symbols directly.

## Development rules

- Fix root causes, not CI symptoms.
- Add the narrowest regression test for behavior changes.
- Do not claim runtime readiness from `assembleDebug` alone.
- Do not float native dependency revisions.
- Do not copy code from reference keyboards without license/provenance review.
- Do not add network typing paths, content logging or telemetry without an explicit privacy design and user consent model.
- Keep release signing status truthful: current release artifact is unsigned.

## Required validation

Run Python contracts, `scripts/bootstrap_native_deps.sh`, JVM tests, Debug build and Release build. Changes to IME lifecycle/native behavior require runtime evidence where feasible.
