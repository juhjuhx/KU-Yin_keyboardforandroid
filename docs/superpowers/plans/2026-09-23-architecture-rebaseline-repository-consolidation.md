# Architecture Rebaseline + Repository Consolidation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebaseline stale KU-Yin architecture docs to the Compose/C4 truth, add anti-drift tooling, and consolidate workspace, without touching production behavior.

**Architecture:** Docs-only + static Python tooling + hygiene; zero production Kotlin changes except commented-dependency cleanup in `app/build.gradle.kts`. Every claim verified against repo files, never from memory.

**Tech Stack:** Markdown docs, Python 3 stdlib scripts, git worktree at `/tmp/ku-yin-c4`.

---

### Task 1: OCR delegation review of D2.5 range

**Files:**
- Read: `git diff e72acb3..34a758b` (already captured)

- [ ] **Step 1: Run delegation preview**
  Run: `ocr delegate preview --format json --from e72acb3 --to 34a758b` (done: 8 reviewable + 5 excluded)
  Expected: mode=range, merge_base=e72acb3
- [ ] **Step 2: Get rule groups**
  Run: `ocr delegate rule --format json <8 paths>` (done: Kotlin group + Python group)
- [ ] **Step 3: Host-review each file against its rule group**
  Grep `!!`, `GlobalScope`, dead symbols in the 8 files; record verdicts in `docs/review/OPEN-CODE-REVIEW-BASELINE.md`
  Expected: no Critical/High (verified this turn)
- [ ] **Step 4: Record 401 limitation**
  Full LLM review blocked (invalid Anthropic key); delegation path is the working substitute. Record session IDs.

### Task 2: Docs provenance

**Files:**
- Create: `docs/review/ARCHITECTURE-DOCUMENT-PROVENANCE.md`

- [ ] **Step 1: Compare doc trees across refs**
  Run: `git diff --stat 34a758b origin/<branch> -- AGENTS.md README.md SECURITY.md docs/` for main, feat/compose-mainline-v0.3, fix/pre-c4-production-hardening, feat/mainline-docs-i18n, feat/keyboard-shell-v0.2
  Expected: main/compose-mainline/pre-c4 identical except 3 new local files; i18n only translations; keyboard-shell diverged historically
- [ ] **Step 2: Write provenance verdicts**
  No newer correct architecture doc exists anywhere → drift fixed fresh on C4.

### Task 3: Rewrite source-of-truth docs

**Files:**
- Rewrite: `docs/ARCHITECTURE.md`
- Create: `docs/CURRENT_STATE.md`
- Rewrite: `docs/DEVELOPMENT.md` (versions from wrapper/catalog/build files)
- Rewrite: `docs/ROADMAP.md` (DONE/CURRENT/NEXT/LATER)
- Rewrite: `docs/SECURITY_AUDIT.md` (target 36, Compose, clipboard-write, dual learning)
- Modify: `AGENTS.md` (source-of-truth order)
- Create: `docs/adr/0001..0005-*.md`

- [ ] **Step 1: Verify every version number against files**
  Run: `grep` wrapper (9.3.1), catalog (AGP 9.1.1, Kotlin 2.2.10), build (compile/target 36, min 24, NDK 28.2.13676358, CMake 3.22.1)
  Expected: matches table in DEVELOPMENT.md
- [ ] **Step 2: Write files, then re-read each once for consistency**
- [ ] **Step 3: Run drift checker (Task 4) to prove consistency**

### Task 4: Anti-drift + doctor + verify tooling

**Files:**
- Create: `scripts/check_docs_consistency.py`
- Create: `scripts/project_doctor.py`
- Create: `scripts/verify.py`

- [ ] **Step 1: Write check_docs_consistency.py** (derives toolchain from files, greps stale markers)
- [ ] **Step 2: Run it**
  Run: `python3 scripts/check_docs_consistency.py`
  Expected: exit 0
- [ ] **Step 3: Write project_doctor.py + verify.py, run `--static`**
  Run: `python3 scripts/project_doctor.py`, `python3 scripts/verify.py --static`
  Expected: contracts 7/7 exit 0; doctor reports MISSING toolchain honestly

### Task 5: Scaffold + reference audits

**Files:**
- Create: `docs/review/AI-STUDIO-SCAFFOLD-AUDIT.md`
- Create: `docs/review/REFERENCE-ARCHITECTURE-COMPARISON.md`

- [ ] **Step 1: Classify 8 commented deps in app/build.gradle.kts**
  Remove camera×4/location/coil/datastore/navigation dead comments (privacy-adjacent, roadmap-absent); keep accompanist.permissions only if justified → remove too (unused, roadmap-absent). Record decision.
- [ ] **Step 2: Write both docs from reads (no new research beyond cited prior findings)**

### Task 6: Workspace inventory + consolidation

- [ ] **Step 1: Inventory every KU-Yin path** (write `review-artifacts/LOCAL-WORKSPACE-INVENTORY.md`, local-only)
- [ ] **Step 2: Consolidate recovery copies** into `~/KU-Yin-recovery/2026-09-23/` (copy+shasum; delete ONLY byte-identical proven-redundant)
- [ ] **Step 3: .gitignore audit** (add `review-artifacts/`, verify build/.deps/IDE entries)

### Task 7: Commits + static verification

- [ ] **Step 1: Commit 1 docs rebaseline**
- [ ] **Step 2: Commit 2 tooling**
- [ ] **Step 3: Run all 7 contracts fresh**
  Expected: 7× exit 0
- [ ] **Step 4: Final report (A–Z)**

## Self-Review

- Spec coverage: RC2 §2–§21 each maps to a task above (OCR→1, provenance→2, docs→3, tooling→4, scaffold→5, workspace→6, commits→7). ✓
- No placeholders: all steps have exact paths/commands. ✓
- No D3/M4/production-behavior tasks included. ✓
