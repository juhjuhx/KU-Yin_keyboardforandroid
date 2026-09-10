# KU-Yin Next Steps

> Scope: light documentation/status pass first; no heavy architecture or toolchain migration in this round.
> Baseline: `fix/p0-p4-ime-recovery` after static GREEN on GitHub Actions run #63.

## Immediate objective

Preserve the current static-build verified state, close the remaining R4 documentation gaps, then prepare a focused runtime-validation round.

## N0 — Documentation reconciliation

- [x] Verify current PR head and latest CI.
- [x] Verify Debug/Release APK artifact creation.
- [x] Re-check R4-A findings against the current branch.
- [x] Create `docs/PROJECT_STATUS.md` as the factual status source.
- [ ] Rewrite root README so it no longer describes the repository as a scaffold/fork mirror.
- [ ] Rewrite BUILD.md around the actual GitHub Actions/native bootstrap path.
- [ ] Rewrite SECURITY.md to contain only implemented/verified guarantees and explicit limitations.
- [ ] Correct CHANGELOG.md claims that refer to unavailable layouts/OpenCC/user-dict features.
- [ ] Simplify CONTRIBUTING.md to match the actual toolchain and CI.
- [ ] Reconcile PR #1 body with the current static GREEN / runtime-pending state.

## N1 — R4-A closeout check

R4-A is a documentation/context gate, not a production-code gate.

Current assessment:

- fresh re-audit of branch/CI: **complete**
- current root causes recorded in the R4 closeout document: **complete**
- static verification status reconciled: **complete**
- runtime verification status reconciled: **pending**, because dynamic evidence does not exist yet
- PR/recovery-document final closeout: **pending until runtime verification**

R4-A should remain partially open until the dynamic gate is completed.

## N2 — Runtime verification preparation

No implementation changes should be started until the test procedure is explicit.

Prepare a reproducible Android runtime checklist for:

1. install Debug APK
2. verify KU-Yin appears in `ime list -s`
3. enable and select the IME
4. focus a normal text editor
5. verify Dachen keyboard rendering
6. verify Dachen composition and candidate commit
7. verify ASCII digits/lowercase/Shift/uppercase
8. verify Space / Backspace / Enter/editor action
9. focus password / FORCE_ASCII fields and verify ASCII surface
10. verify no-personalized-learning policy behavior
11. move cursor outside composition and verify reconciliation
12. restart input / switch apps repeatedly and verify no crash

The next implementation round should only fix failures observed in this runtime checklist.

## N3 — Second review

After dynamic verification or runtime fixes:

- review correctness
- review simplicity / accidental complexity
- review architecture boundaries
- review privacy/security behavior
- review performance-sensitive input/render paths
- record findings in PR/repository
- re-run full static CI after any fix

Alibaba OpenCodeReview can be used as a secondary reviewer when an OCR-capable execution environment and model configuration are available. Deterministic tests/builds remain the authoritative gate.

## N4 — Post-R4 work, intentionally deferred

Do not mix these into the runtime-validation round:

- API 36 / AGP / Gradle / Kotlin migration
- accessibility virtual-node implementation
- OpenCC production integration
- Hsu / Eten26 visual layouts
- full symbol / emoji system
- clipboard
- gesture typing
- prediction / language-model features
- Compose migration

## Exit criteria for the next round

The next round ends when either:

1. dynamic Android IME verification is GREEN and evidence is recorded, or
2. a reproducible runtime failure is isolated with logs and a minimal follow-up fix plan.
