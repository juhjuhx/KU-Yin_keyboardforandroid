# KU-Yin R4 Closing Pass

> Branch: `fix/p0-p4-ime-recovery`
> Approved scope: 2026-09-09
> Baseline head before this pass: `fc26378d074bb9ffe04e0c77d95b9a12109e8516`
> Latest implementation/static-verification head before the documentation-only cleanup: `7fab4442ba3f544acd12e8c12be6a033249af552`
> Green CI evidence: Build run #63 (`34359833239`)

## Purpose

Close the remaining R4 usability gap without expanding into the later accessibility, toolchain-migration, emoji, clipboard, gesture-typing, or theme-engine tracks.

This document is the authoritative R4 closeout checklist. Current factual status is summarized in `docs/PROJECT_STATUS.md`; immediate follow-up work is in `docs/NEXT_STEPS.md`. Earlier P0-P4 and Recovery v2 plans remain useful history, but where their status or sequencing conflicts with these files, the newer files win.

## Fresh-review findings

1. `scripts/check_dachen_contract.py` scanned both Dachen and ASCII layouts into one code-to-label map, allowing ASCII comma/period to overwrite the Dachen entries. The failing contract was a false positive and is now scoped to the Dachen source block.
2. `KeyAction.ENTER` existed in production layout without runtime dispatch; semantic editor actions and fallback Enter handling are now wired.
3. The password-surface contract required digits and `KeyAction.SHIFT`; the ASCII surface now contains both and supports shifted uppercase projection.
4. `EditorPolicy` and `ImeSessionController` were previously tested but not connected to `ChewingInputMethodService`; session policy is now wired into runtime code.
5. `onUpdateSelection()` was previously missing; editor/native composition reconciliation is now wired.
6. Repository-side static verification is green. Android runtime/device readiness is still unverified.

## R4 scope

### R4-A — Audit/context ledger

- [x] Re-audit current branch head and latest CI instead of trusting prior status.
- [x] Record current root causes in this authoritative closeout file.
- [x] Reconcile repository documentation and PR status with the static-GREEN state.
- [ ] Perform final documentation/PR reconciliation after dynamic Android verification.

R4-A is therefore **complete for static recovery and intentionally remains open for final runtime closeout**.

### R4-B — Repair the test harness

- [x] Scope the Dachen fast contract to `KeyboardLayout.Dachen` only.
- [x] Keep JVM tests as the behavioral source of truth for key layout semantics.
- [x] Keep Python contracts limited to cheap build/provenance/integration invariants.

### R4-C — Wire session policy into Android runtime

- [x] Create `EditorPolicy` from every `onStartInput()` editor.
- [x] Begin an `ImeSessionController` session.
- [x] Switch rendered keyboard surface between Dachen and ASCII.
- [x] Apply personalized-learning policy to libchewing.
- [x] Suppress composition/candidates in ASCII/sensitive sessions.
- [x] Reconcile cursor movement via `onUpdateSelection()`.

### R4-D — Complete the practical ASCII/core action surface

- [x] Add digits to ASCII layout.
- [x] Add semantic Shift and shifted uppercase projection.
- [x] Route Backspace/Space/Input directly in ASCII sessions.
- [x] Route Enter through the editor's requested IME action when one exists.
- [x] Use a normal Enter key event when no semantic editor action is requested.

### R4-E — Static verification

Verified on Build run #63 at `7fab4442ba3f544acd12e8c12be6a033249af552`:

- [x] all fast contracts
- [x] JVM unit tests
- [x] native dependency bootstrap
- [x] Debug APK
- [x] Release APK
- [x] artifact upload

Artifacts from run #63:

- `debug-apk`: 9,809,947 bytes, artifact digest `sha256:5f96003360b62fc318fb2c2a3166908a88ec3da68f6aae51e9cdcea8f0ed068d`
- `release-apk`: 8,632,454 bytes, artifact digest `sha256:7db202a94f17012b2934ed069ea9475dc4bf4e3e16a41c4caef8edfa3699afda`

These are GitHub Actions artifact digests, not a signed-release APK checksum manifest.

### R4-F — Dynamic Android verification

Required before runtime-ready claims:

- [ ] Android recognizes KU-Yin as an IME
- [ ] enable/switch succeeds
- [ ] keyboard renders
- [ ] ASCII typing, Shift, digits, Space, Backspace, Enter
- [ ] Dachen composition and candidate commit
- [ ] password/FORCE_ASCII policy
- [ ] no-personalized-learning policy
- [ ] selection reconciliation
- [ ] restart/app-switch smoke
- [ ] no crash

### R4-G — Second review and closing

- [ ] Addy-style five-axis review: correctness, simplicity, architecture, security, performance.
- [ ] PR-review-toolkit / receiving-code-review pass where tooling is available.
- [ ] Alibaba OpenCodeReview pass when an OCR-capable execution environment is available.
- [ ] Record review findings in PR/repository.
- [ ] Re-run full static verification after runtime-derived fixes, if any.
- [ ] Keep PR draft until runtime evidence exists and final closeout is reconciled.

## Deferred beyond R4

- per-key accessibility virtual nodes
- API 36 / broad AGP-Kotlin-Gradle migration
- full emoji/symbol system
- clipboard manager
- gesture typing
- language-model prediction
- Compose migration
- large theme engine
