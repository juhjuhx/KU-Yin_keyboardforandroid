# Architecture document provenance (2026-09-23, verified via branch diffs)

Method: `git diff --stat 34a758b origin/<branch> -- AGENTS.md README.md SECURITY.md docs/`
for main, feat/compose-mainline-v0.3, fix/pre-c4-production-hardening,
feat/mainline-docs-i18n, feat/keyboard-shell-v0.2 (+ PR #4/#6 file lists).

| Document | main / compose-mainline / pre-c4 | i18n branch | keyboard-shell (old) | Verdict |
|---|---|---|---|---|
| AGENTS.md | identical (stale arch line) | identical | diverged (old era) | No newer correct version exists → rewrite fresh here |
| README.md | identical | +25 translations only | +152 rewritten (old era) | Same → rewrite deltas only where stale |
| docs/ARCHITECTURE.md | identical (View doc, self-declared old) | +8 appendix | −53 (removed elsewhere) | Same → full rewrite here (C12 was already planned) |
| docs/DEVELOPMENT.md | identical (JDK17-era) | identical | — | Same → rewrite here |
| docs/ROADMAP.md | identical | identical | — | Same → cleanup here |
| docs/SECURITY_AUDIT.md | identical (API33-era) | identical | — | Same → rewrite here |
| docs/HISTORY.md | identical | identical | — | Correct as history, keep |

Conclusion: the "correct doc exists elsewhere" hypothesis is REJECTED. All live
lines share the same stale docs. Fix fresh on the C4 line; do not cherry-pick.
PR #4 = View donor shell (draft, reference only). PR #6 = README/i18n docs only.
