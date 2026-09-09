# KU-Yin R4 Closing Pass

> Branch: `fix/p0-p4-ime-recovery`
> Approved scope: 2026-09-09
> Baseline head before this pass: `fc26378d074bb9ffe04e0c77d95b9a12109e8516`
> Static-verification head: `daab482d686a2051f070c676b2b88ff66c479a54`
> Green CI evidence: Build run #62 (`34356281564`)

## Purpose

Close the remaining R4 usability gap without expanding into the later accessibility, toolchain-migration, emoji, clipboard, gesture-typing, or theme-engine tracks.

This document is the authoritative implementation checklist for the R4 closing pass. Earlier P0-P4 and Recovery v2 plans remain useful history, but where their R4 status or sequencing conflicts with this file, this file wins.

## Fresh-review findings

1. `scripts/check_dachen_contract.py` scanned both Dachen and ASCII layouts into one code-to-label map, allowing ASCII comma/period to overwrite the Dachen entries. The failing contract was a false positive and is now scoped to the Dachen source block.
2. `KeyAction.ENTER` existed in production layout without runtime dispatch; semantic editor actions and fallback Enter handling are now wired.
3. The password-surface contract required digits and `KeyAction.SHIFT`; the ASCII surface now contains both and supports shifted uppercase projection.
4. `EditorPolicy` and `ImeSessionController` were previously tested but not connected to `ChewingInputMethodService`; session policy is now wired into runtime code.
5. `onUpdateSelection()` was previously missing; editor/native composition reconciliation is now wired.
6. PR #1 remains draft. Repository-side static verification is green, while device/runtime readiness is still unverified.

## R4 scope

### R4-A — Audit/context ledger

- [x] Re-audit current branch head and latest CI instead of trusting prior status.
- [x] Record current root causes in this authoritative closeout file.
- [ ] Reconcile PR body and recovery docs after static/dynamic verification.

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

Required evidence from the PR head:

- [x] all fast contracts
- [x] JVM unit tests
- [x] native dependency bootstrap
- [x] Debug APK
- [x] Release APK
- [x] artifact upload

Evidence from Build run #62 at `daab482d686a2051f070c676b2b88ff66c479a54`:

- `debug-apk` artifact uploaded successfully; artifact digest `sha256:871a3ed22eceefb22515e8086995d14aa527f999876fa35c2bc1361a145d08e6`
- `release-apk` artifact uploaded successfully; artifact digest `sha256:81c6b0731ff5faa70aee6eb726b167f362beb2f00f30b19ed7af74ad8049285f`

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
