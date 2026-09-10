# Repository Consolidation v0.1.1-alpha Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Consolidate KU-Yin into one clean mainline, reduce documentation clutter, formalize upstream/security/release documentation, add safe upstream monitoring, and publish v0.1.1-alpha.

**Architecture:** Preserve the current Android/Kotlin → JNI/C++ → libchewing architecture. This plan changes repository organization, documentation, release metadata, CI/upstream automation, and versioning only; it does not redesign the input engine.

**Tech Stack:** Android InputMethodService, Kotlin 1.9.22, AGP 7.4.2, Gradle 7.6.4, JNI/C++17, libchewing pinned prebuilt artifacts, GitHub Actions.

**Spec:** `docs/archive/2026-09-10-repository-consolidation-design.md`

## Global Constraints

- Preserve production input behavior.
- Keep `main` as the only long-lived branch after verified merge.
- Do not auto-merge unverified upstream native updates.
- Do not commit APK binaries to Git history.
- Keep v0.1.1-alpha a prerelease; release APK remains unsigned.
- Do not claim full runtime/OEM compatibility beyond verified evidence.

---

### Task 1: Consolidate documentation surface

**Files:**
- Create/update: `README.md`, `docs/README.md`, `docs/ARCHITECTURE.md`, `docs/DEVELOPMENT.md`, `docs/HISTORY.md`, `docs/ROADMAP.md`, `docs/SECURITY_AUDIT.md`, `docs/UPSTREAM.md`
- Move language docs to: `docs/i18n/README.en.md`, `docs/i18n/README.zh-CN.md`
- Remove obsolete root/status reports and nonessential translations.

- [ ] Keep the root README complete enough for users: upstream, capabilities, install/use guide, build, architecture, security, roadmap, acknowledgements, contributing, license.
- [ ] Consolidate P0–R4 recovery history into one factual iteration report.
- [ ] Remove contradictory point-in-time reports from the public surface.
- [ ] Keep historical design records only under `docs/archive/` when they still add value.

### Task 2: Normalize upstream and attribution records

**Files:**
- Update: `NOTICE`, `README.md`, `docs/UPSTREAM.md`, `CONTRIBUTING.md`

- [ ] Record current immutable prebuilt pin `3587ba3355711f0aca50136e787719f6562676b8`.
- [ ] Record libchewing source pin used by that prebuilt `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`.
- [ ] Distinguish production dependencies from reference-only projects.
- [ ] Preserve AI-assisted contribution attribution as tooling history, not human/legal authorship.

### Task 3: Add safe upstream monitoring

**Files:**
- Create: `.github/workflows/upstream-watch.yml`
- Create: `scripts/check_upstream.sh`

- [ ] Compare pinned `fcitx5-android/prebuilt` SHA with upstream master.
- [ ] Report latest libchewing release for awareness.
- [ ] On change, create/update a GitHub issue with exact old/new refs and validation checklist.
- [ ] Never update `main` directly from the watcher.

### Task 4: Security/performance/data-leak audit

**Files:**
- Create/update: `docs/SECURITY_AUDIT.md`, `SECURITY.md`

- [ ] Search for INTERNET/network permissions and remote endpoints.
- [ ] Search for keystroke/plaintext logging and clipboard persistence.
- [ ] Confirm app-private native data path and backup policy.
- [ ] Review JNI lifecycle/resource ownership and candidate/composition state boundaries.
- [ ] Record unresolved risks separately from verified controls.

### Task 5: Prepare v0.1.1-alpha

**Files:**
- Update: `app/build.gradle.kts`, `.github/workflows/prerelease.yml`, `CHANGELOG.md`, `APK/README.md`

- [ ] Set `versionCode = 2` and `versionName = "0.1.1-alpha"`.
- [ ] Update prerelease workflow tag/title/assets to v0.1.1-alpha.
- [ ] Keep release asset explicitly unsigned.
- [ ] Generate SHA-256 checksums in the release workflow.

### Task 6: Verify cleanup branch

- [ ] Run repository static contracts in CI.
- [ ] Run native bootstrap.
- [ ] Run JVM tests.
- [ ] Build Debug APK.
- [ ] Build unsigned Release APK.
- [ ] Preserve runtime smoke as non-blocking until its headless-window assertion is corrected.

### Task 7: Merge and branch consolidation

- [ ] Review PR diff for accidental production changes.
- [ ] Squash merge cleanup branch into `main` after required build checks are green.
- [ ] Verify main CI.
- [ ] Publish v0.1.1-alpha prerelease from main.
- [ ] Delete `master`, `fix/p0-p4-ime-recovery`, and the temporary cleanup branch only after main contains all intended content.
