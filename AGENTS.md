# AGENTS.md

## Project source of truth

Read in this order before making changes: `README.md`, `docs/ARCHITECTURE.md`, `docs/DEVELOPMENT.md`, `docs/UPSTREAM.md`, `SECURITY.md`, `docs/SECURITY_AUDIT.md`, `docs/ROADMAP.md`.

Historical recovery records live in Git history and `docs/archive/`; do not treat old plans as current behavior.

## Core architecture

`InputMethodService → EditorPolicy / ImeSessionController / UI → ChewingEngine → AndroidChewingEngine → JNI/C++ → libchewing C API`.

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
