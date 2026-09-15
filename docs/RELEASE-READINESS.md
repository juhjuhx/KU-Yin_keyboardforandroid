# KU-Yin Release Readiness (v0.2 line)

> Living document for the `feat/keyboard-shell-v0.2` → first public release
> path. Status fields are point-in-time; re-verify before acting.
> Last reviewed: 2026-09-15 at `842c223` (+ audit fixes listed below).

## 1. Release-readiness gap matrix

| Requirement | Current state | GitHub | Play | F-Droid | Severity | Required action |
|---|---|---|---|---|---|---|
| applicationId | `io.github.juhjuhx.kuyin` (applied R1; namespace/packages unchanged) | OK | Final ✅ | Final ✅ | — | Done; old alpha installs do not migrate (accepted) |
| versionCode/versionName | 20001 / `0.2.0-alpha.1` (applied R1); tags v0.1.0–v0.1.2 released | OK | Scheme ✅ | Scheme ✅ (+ per-ABI ops later) | — | Tag `v0.2.0-alpha.1` at release cut |
| compileSdk/targetSdk | 33 / 33 | OK | BLOCKER (need 36+ since 2026-08-31; extension to 2026-11-01 possible) | OK (any) | P0 for Play | Dedicated toolchain slice (§5) |
| minSdk | 24 | OK | OK | OK | — | Preserve unless proven otherwise |
| AGP/Gradle/Kotlin/JDK | 7.4.2 / 7.6.4 / 1.9.22 / 17 | OK | Need 8.9.1+ / 8.11.1+ (JDK 17 unchanged) | OK (F-Droid server provides its own; versions declared in metadata) | P1 for Play | Same slice as targetSdk (§5) |
| NDK/CMake | r27.3 / 3.22.1 | OK | Need r28+ (or r27 link flags) for 16K default | Server NDK declared via `ndk:` | P1 for Play | Same slice; verify final ELF (§6) |
| 16 KB page size | arm64 `.so` measured 4K → UNALIGNED | OK | BLOCKER for API 35+ targets | OK (device-dependent) | P0 for Play | NDK route + CI gate (§6) |
| APK signing | unsigned dev artifact | OK w/ clear label | BLOCKER (needs upload key + Play App Signing) | F-Droid signs itself | P0 for Play | Signing design → approval (§7) |
| AAB | locally proven (`bundleRelease` works); CI lane added in this audit | N/A | BLOCKER (new apps must ship AAB) | N/A (APK) | P1 for Play | CI lane added; verify on next green run |
| ABIs | armeabi-v7a, arm64-v8a, x86, x86_64 (single fat APK) | OK | OK (AAB splits automatically) | Needs per-ABI version-code scheme later | P2 | Decide at F-Droid submission |
| Privacy policy | `PRIVACY.md` draft added (code-mapped) | Recommended | BLOCKER (URL required) + Data Safety form | Recommended | P1 | Publish page (Pages) + review |
| Data Safety | Draft mapping in `PRIVACY.md` (no collection) | N/A | BLOCKER (form) | N/A | P1 | Fill at submission from draft |
| Store metadata | Checklist only (§12) | README suffices | BLOCKER (listing + graphics) | BLOCKER (summary/desc) | P1 | Author from one canonical copy |
| Screenshots/assets | None | Optional | BLOCKER | Recommended | P1 | Capture on device (needs gate) |
| LICENSE/NOTICE | LGPL-2.1 + NOTICE present | OK | OK | OK | — | Keep accurate on dep changes |
| Static-link LGPL duties | `.a` → JNI `.so`; no source/object offer shipped yet | Needed at distribution | Needed at distribution | Source build covers it | P1 | Ship build materials + source pointer (§13) |
| F-Droid metadata | Draft block below, not submitted | N/A | N/A | BLOCKER | P1 for F-Droid | Needs final app ID first |
| Source-build native dep | Prebuilt `.a` via bootstrap (pinned, hashed) | OK | OK | BLOCKER (no binary downloads at build) | P0 for F-Droid | Source lane (§14) |
| Release notes/SHA | prerelease.yml generates both per tag | OK | Needs per-release notes | N/A | P2 | Keep; harden trigger (§15) |
| SBOM | None | Useful | Useful | Useful | P3 | Add at first release (syft/cyclonedx) |
| Dependency vulns | AndroidX core/appcompat/material/constraint/preference + junit only; no network libs; versions pinned but aging | Low risk | Review at submission | Review at submission | P2 | Periodic review; no scanner wired yet |
| Device acceptance | BLOCKED-NO-DEVICE (CASE 4/6) | Blocks stable tag | Blocks production | Blocks inclusion request | P0 | Physical-device run |

## 2. Application ID decision (APPROVED and applied in R1)

Locked: `io.github.juhjuhx.kuyin` (option A). Namespace, Kotlin packages, and
JNI class names intentionally unchanged: JNI binds to the Java package, not
the applicationId, so no native changes were required.

Accepted migration consequence: the new ID is a new Android app identity.
Alpha installs under `com.example.androidkeyboard` will not upgrade in place;
their private preferences/userdict are not directly accessible from the new
sandbox (no fake cross-sandbox migration attempted). Accepted because the old
ID never reached stable public release.

Options:

- **A (recommended): `io.github.juhjuhx.kuyin`** — zero cost, permanent,
  collision-free, matches repo ownership. No domain to buy/verify.
- **B: owner-controlled domain** (e.g. a `kuyin.tw`-style reverse domain) —
  only if the user owns the domain; nicer branding, adds DNS/ownership work.
- **C: keep `com.example.*`** — rejected for any public distribution
  (store policy friction, F-Droid collision/impersonation risk).

Implications of any rename: Play package ID is permanent once published;
F-Droid metadata filename equals the ID; default-`SharedPreferences` filename
is package-derived (settings reset unless migrated); `noBackupFilesDir`
user dictionary path is package-derived (user phrases do NOT migrate
automatically — needs explicit migration code or a documented clean break);
existing alpha installs become a *different app* (side-by-side, no upgrade
path). Namespace/package refactor is optional cosmetics — defer it.

## 3. Versioning (applied in R1: 0.2.0-alpha.1 / 20001)

- Scheme: `0.2.0-alpha.1`, `0.2.0-alpha.2`, … → `0.2.0-beta.1` → `0.2.0`
  (SemVer prerelease naming).
- `versionCode`: monotonically increasing integers, currently **20001**
  (jumped from 3 to leave room and to mark the release-engineering epoch;
  codes need not encode SemVer ordering). For future multi-ABI F-Droid
  splits, reserve the lowest digit(s) per ABI via `VercodeOperation` at that
  time — do not renumber now.
- Fixed in this audit: README claimed `0.1.1-alpha` while Gradle/tags/releases
  are `0.1.2-alpha` (docs-only fix committed).
- Rule: version bump + tag + release notes travel together; never bump
  `versionName` on `main` without intending a release (see prerelease guard).

## 4. Target API 36 toolchain plan (plan only, do not execute yet)

Minimum versions for `compileSdk`/`targetSdk` 36 per Android developer docs:

| Component | Current | Minimum for API 36 | Notes |
|---|---|---|---|
| AGP | 7.4.2 | **8.9.1** | Needs Gradle 8.11.1+ |
| Gradle | 7.6.4 | **8.11.1+** | Wrapper jar still gitignored — revisit |
| JDK | 17 | **17 (unchanged)** | No JDK migration needed |
| Kotlin (KGP) | 1.9.22 | 1.9.x expected-compatible; verify, consider 2.0.x in-slice | Confirm during migration |
| NDK | r27.3 | **r28+** (16K default; also serves §6) | Pinned exactly as today |
| CMake | 3.22.1 | keep unless NDK demands newer | Verify |
| minSdk | 24 | **24 preserved** | No reason found to raise |

Migration slice order: toolchain bump → compile → JVM tests → contracts →
native rebuild → APK → AAB → 16K ELF re-verify → runtime/device verification.
Never mixed with M4 feature work. Extension window to 2026-11-01 exists if
needed, via Play Console form.

## 5. 16 KB page-size status (measured, not assumed)

- 2026-09-15 local Release APK, `lib/arm64-v8a/libchewing-jni.so`: all
  `PT_LOAD` segments `p_align = 4096` → **UNALIGNED**, not 16K-ready.
- Routes: (a) NDK r28+ (16K default) + AGP ≥ 8.5.1 — preferred, folds into §4;
  (b) r27 linker flags `-Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384`
  (+ 16K CRT considerations) — fallback only.
- Warning: a 4K-built static `.a` can drag the final `.so` back to 4K; the
  pinned prebuilt must be re-verified after any NDK move — always check the
  **final ELF**, never the `.a`.
- `scripts/check_elf_alignment.py` (added in this audit, CI-unwired until the
  NDK move lands) encodes the check: reports ALIGNED/UNALIGNED per ABI from a
  built APK/AAB, exit 1 on violation.

## 6. Signing design (design only — no keys created)

Constraints: never commit keystores/passwords/material; never print secrets.

- **Debug**: default debug key (unchanged).
- **GitHub direct**: dedicated dev-distribution key, self-managed offline
  (e.g. encrypted USB + paper backup), CI holds nothing until a release lane
  is approved. Unsigned APKs stay clearly labelled developer artifacts.
- **Play**: mandatory Play App Signing for new apps. Upload key (dev-held)
  signs the AAB; Google holds the app-signing key. Consequence: a Play
  install and a GitHub-direct install carry **different certificates and can
  never upgrade each other** — cross-store updates are impossible by design
  for new apps. Decide the primary distribution home accordingly.
- **F-Droid**: signs with its own keys by default (reproducible/upstream
  signing is an advanced opt-in, out of scope for first inclusion).
- Backup/recovery: offline copies in ≥2 locations; loss of the upload key is
  recoverable via Play Console reset, loss discipline still required.

## 7. Privacy & Data Safety (evidence-mapped, see PRIVACY.md)

Processed on device (keystrokes, preedit, candidates, editor context via
`InputConnection`); stored on device (dictionaries + userdict in
`noBackupFilesDir`, preferences + future recents in app-private prefs);
transmitted off device: **none** (no INTERNET permission, no network libs,
no telemetry/clipboard code); shared with third parties: **none**.
Password/`FORCE_ASCII`/no-learning editors get safe sessions
(`EditorPolicy.kt`). Publish path: GitHub Pages (or static project page) +
Data Safety "no data collected" declaration sourced from the mapping table.
Limitations honestly recorded (no app-layer encryption on userdict,
rooted devices out of model, absence-claims bounded by reviewed revision).

## 8. F-Droid draft (NOT submitted; needs final app ID + source lane)

```yaml
Categories:
  - System
License: LGPL-2.1-only
RepoType: git
Repo: https://github.com/juhjuhx/KU-Yin_keyboardforandroid
SourceCode: https://github.com/juhjuhx/KU-Yin_keyboardforandroid
IssueTracker: https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues
Changelog: https://github.com/juhjuhx/KU-Yin_keyboardforandroid/blob/main/CHANGELOG.md
AutoUpdateMode: Version v%v
UpdateCheckMode: Tags
CurrentVersion: 0.2.0-alpha.1
CurrentVersionCode: 20001
Builds:
  - versionName: 0.2.0-alpha.1
    versionCode: 20001
    commit: v0.2.0-alpha.1
    subdir: app
    gradle:
      - yes
    # NDK block + source-built libchewing prebuild step attach here once
    # the source lane (§9) exists. Binary prebuilt download is NOT allowed.
```

Anti-Features expected: none (no ads/tracking/Firebase/non-free deps; all
deps Apache-2.0/EPL-test/JDK-toolchain). Double-check at submission; any new
dependency re-opens this verdict. Multi-ABI splits need `VercodeOperation`
(lowest-digit ordering armeabi-v7a < arm64-v8a < x86 < x86_64) — deferred.

## 9. F-Droid source lane (design; the critical blocker)

Problem: `bootstrap_native_deps.sh` downloads prebuilt `libchewing_capi.a`
binaries — F-Droid builds must compile from source (scanner strips binaries).

Feasibility (researched, firsthand sources): upstream libchewing builds via
CMake (≥3.21/3.24) + **Rust ≥1.88** (corrosion) + `chewing-cli init-database`
for `tsi.dat`/`word.dat` from `tsi.src`/`word.src`; `swkb.dat`/`symbols.dat`
copied; upstream documents Android platform support. fcitx5-android's
prebuilt repo holds the working Android recipe to mirror.

Proposed policy (dual lane): fast lane (pinned prebuilt, dev/CI) stays until
the source lane is proven; reproducible lane
(`scripts/bootstrap_native_from_source.sh`: pinned libchewing source rev →
per-ABI CMake/Rust build → dict generation → same staging layout) for
F-Droid/reproducibility. Drift risk between lanes is real: mitigate with
byte/hash comparison of staged `.a`/dicts in CI once both exist (proposed,
not built).

## 10. Reproducible-build procedure (proposed, unproven)

1. Pin: native commit (done), JDK/Gradle/AGP/NDK/CMake (done), dict sources
   (pending source lane), version injection (static `versionName`, no timestamps
   injected — verify no `BuildConfig` time fields: none found).
2. Two clean builds (separate checkouts/machines) → compare
   `SHA256SUMS` of APK/AAB + `unzip -l` listings + `.so` hashes.
3. Known nondeterminism risks to close: Gradle build timestamps in ZIP
   metadata (normalize or accept-and-document), dictionary generation order,
   toolchain drift. Do not claim reproducibility before step 2 passes twice.

## 11. Prerelease workflow evaluation (no change made)

`prerelease.yml` triggers on `main` push but skips when the tag already
exists. With `versionName` untouched by PR #4, merging today is a safe no-op
(tag `v0.1.2-alpha` exists). Residual hazard: a careless future version bump
on `main` auto-publishes a possibly-unsigned build. Evaluated options:
(A) `workflow_dispatch`-only, (B) tag-driven, (C) protected explicit lane.
Recommendation: keep current guard until the §3 version strategy is approved,
then move to (B) as part of the release-branch cut. **No change applied in
this audit** (release automation needs explicit approval).

## 12. Store metadata & listing checklist (canonical copy TBD)

App name / short + full description / 512×512 icon / feature graphic / phone
screenshots (+ tablet if targeted) / privacy URL (Pages) / support contact /
content rating questionnaire / Data Safety (from §7 draft) / category /
release notes. No marketing assets generated in this audit; screenshots need
the device gate. `fastlane/metadata/android/` (en-US) recommended as the
single source feeding both Play and F-Droid text.

## 13. M4 relationship answer

**Yes — a useful 0.2 alpha can ship before M4.** Symbols/Emoji pages already
exist as navigable scaffolding with static data and tested `InsertText`
boundaries; M4 adds dataset depth (Unicode-generated emoji), recents, and
settings. None of the release blockers (app ID, target API, signing, 16K,
source lane, device acceptance) depend on M4. Product impact of shipping
without M4: emoji coverage is a fixed starter set without recents — acceptable
for an alpha whose headline is the v0.2 shell + continuous composition.
Release engineering and M4 proceed on parallel tracks; do not gate one on
the other beyond shared CI health.

## 14. Open audit findings carried forward

- P0/P1 release-engineering: none open beyond the gated blockers above.
- `executeChewingKey` fallback suspect, fling thresholds, TalkBack virtual
  children: unchanged, still evidence-gated (see WORKLOG).
- Dependency versions are aging but contain no network attack surface;
  schedule periodic review; no scanner wired (deliberately, to avoid CI noise
  without an owner).
