# KU-Yin R4 Closing Pass

> Branch: `fix/p0-p4-ime-recovery`
> Approved scope: 2026-09-09
> Baseline head before this pass: `fc26378d074bb9ffe04e0c77d95b9a12109e8516`

## Purpose

Close the remaining R4 usability gap without expanding into the later accessibility, toolchain-migration, emoji, clipboard, gesture-typing, or theme-engine tracks.

This document is the authoritative implementation checklist for the R4 closing pass. Earlier P0-P4 and Recovery v2 plans remain useful history, but where their R4 status or sequencing conflicts with this file, this file wins.

## Fresh-review findings

1. `scripts/check_dachen_contract.py` currently scans both Dachen and ASCII layouts into one code-to-label map, so ASCII comma/period overwrite the Dachen entries. The current CI failure is therefore a contract false positive.
2. `KeyAction.ENTER` exists in production layout but the service does not dispatch it.
3. The RED password-surface test requires digits and `KeyAction.SHIFT`, while production ASCII layout still lacks both.
4. `EditorPolicy` and `ImeSessionController` are tested but not wired into `ChewingInputMethodService`; password/FORCE_ASCII/no-learning decisions therefore have no runtime effect yet.
5. `onUpdateSelection()` is not wired, so editor/native composition can drift when the cursor leaves the composing range.
6. PR #1 remains draft and runtime readiness is still unverified.

## R4 scope

### R4-A — Audit/context ledger

- [x] Re-audit current branch head and latest CI instead of trusting prior status.
- [x] Record current root causes in this authoritative closeout file.
- [ ] Reconcile PR body and recovery docs after static/dynamic verification.

### R4-B — Repair the test harness

- [ ] Scope the Dachen fast contract to `KeyboardLayout.Dachen` only.
- [ ] Keep JVM tests as the behavioral source of truth for key layout semantics.
- [ ] Keep Python contracts limited to cheap build/provenance/integration invariants.

### R4-C — Wire session policy into Android runtime

- [ ] Create `EditorPolicy` from every `onStartInput()` editor.
- [ ] Begin an `ImeSessionController` session.
- [ ] Switch rendered keyboard surface between Dachen and ASCII.
- [ ] Apply personalized-learning policy to libchewing.
- [ ] Suppress composition/candidates in ASCII/sensitive sessions.
- [ ] Reconcile cursor movement via `onUpdateSelection()`.

### R4-D — Complete the practical ASCII/core action surface

- [ ] Add digits to ASCII layout.
- [ ] Add semantic Shift and shifted uppercase projection.
- [ ] Route Backspace/Space/Input directly in ASCII sessions.
- [ ] Route Enter through the editor's requested IME action when one exists.
- [ ] Use a normal Enter key event when no semantic editor action is requested.

### R4-E — Static verification

Required evidence from the PR head:

- [ ] all fast contracts
- [ ] JVM unit tests
- [ ] native dependency bootstrap
- [ ] Debug APK
- [ ] Release APK
- [ ] artifact upload

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
- [ ] PR-review-toolkit / receiving-code-review pass.
- [ ] Alibaba OpenCodeReview pass when an OCR-capable execution environment is available.
- [ ] Record review findings in PR/repository.
- [ ] Re-run full verification after fixes.
- [ ] Keep PR draft until all repository-side gates are green and runtime evidence exists.

## Deferred beyond R4

- per-key accessibility virtual nodes
- API 36 / broad AGP-Kotlin-Gradle migration
- full emoji/symbol system
- clipboard manager
- gesture typing
- language-model prediction
- Compose migration
- large theme engine
