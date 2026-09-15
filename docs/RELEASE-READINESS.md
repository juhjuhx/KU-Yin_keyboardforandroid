# KU-Yin Release Readiness (v0.2 line)

> Living release gate for PR #4 (`feat/keyboard-shell-v0.2`).
> Audit 2.0 reference baseline: `main` = `653b4a3`, PR branch = `5b0f967` before the Audit 2.0 repair commits. Subsequent repair commits intentionally advance the PR head. Always verify the current PR head before release or merge.

## 1. Current release state

| Requirement | Current state | Severity / gate | Required action |
|---|---|---|---|
| applicationId | `io.github.juhjuhx.kuyin`; namespace/Kotlin/JNI package remains `com.example.androidkeyboard` | Done | Keep stable after public release |
| version | `versionName=0.2.0-alpha.1`, `versionCode=20001` | Done for alpha | Tag only after device/release gates pass |
| compileSdk / targetSdk | 36 / 36 | Done | Keep API 35/36 behavior in device matrix |
| minSdk | 24 | Done | Preserve unless evidence requires change |
| AGP / Gradle / Kotlin / JDK | 8.10.1 / 8.11.1 / 1.9.22 / 17 | Done | Re-verify on future toolchain changes |
| NDK / CMake | r28.2.13676358 / 3.22.1 | Done | Keep pinned |
| 16 KB page size | Blocking CI gate passes for the current native build; arm64/x86_64 final JNI ELF verified aligned on Audit 2.0 baseline | Done for current branch | Re-run final ELF gate after any native/toolchain change |
| Debug / Release APK | Built in CI | Done | Public release APK must be signed |
| Release AAB | Built in CI | Done | Sign with upload key only when Play release lane is approved |
| Settings edge-to-edge | targetSdk 36 requires inset-safe UI; Audit 2.0 added an explicit P2 contract and SettingsActivity system-bar/display-cutout handling | Code-fixed, runtime visual gate remains | Verify Android 15/16 + gesture/3-button + font/display scaling on device/emulator |
| APK / Play signing | Architecture corrected and approved; **no production keys created** | P0 release gate | Generate offline only at release cut |
| Privacy policy | `PRIVACY.md` is code-mapped draft | P1 release gate | Publish canonical URL and re-review release artifact |
| Data Safety | Draft mapping says no collection/share | P1 Play gate | Complete Play form from current release evidence |
| F-Droid source build | Dev/CI lane still consumes pinned prebuilt native artifacts | P0 F-Droid gate | Build libchewing from source in reproducible lane |
| Store metadata/assets | Not final | P1 distribution gate | Capture after device gate |
| LGPL static-link distribution duties | JNI links libchewing static archive; distribution materials/source pointer still required | P1 distribution gate | Ship compliant source/build materials |
| Device acceptance | CASE 4 / CASE 6 and full physical-device matrix not yet recorded as passed | P0 product gate | Run device gate before merge/stable tag |
| Dependency review | Dependencies pinned; several AndroidX versions are aging | P2 maintenance | Periodic review; no nightly scanner required yet |
| SBOM | Not generated | P3 | Add at first public release if useful |

## 2. Evidence identity and CI history

Audit 2.0 corrects two earlier bookkeeping mistakes:

- `5b0f967` was the remote head of **PR branch `feat/keyboard-shell-v0.2`**, not `main`.
- `main` remained `653b4a3` during the audit.
- CI run `34959243434` / Build #142 belonged to earlier head `3249425`.
- CI run `34966710551` / Build #143 is the verified full green run for head `5b0f967`: static contracts, pinned native bootstrap, 85/85 JVM tests, Debug APK, Release APK, Release AAB, final 16 KB ELF gate, and artifact uploads all passed. The experimental runtime-smoke retained its known headless IME-window assertion limitation.
- Audit 2.0 repair uses a deliberate RED/GREEN sequence for Settings insets. RED commit `61de1ae` makes the P2 contract require target-36 system-bar/display-cutout handling; Build #144 fails at that P2 contract as intended. GREEN commit `4666e58` adds the bounded SettingsActivity fix; final CI status must be read from the current PR head before any release decision.

A green historical run proves only the revision it exercised. Do not use an older run as evidence for a newer branch head.

## 3. Signing architecture (corrected; approved design, no keys created)

Constraints:

- never commit keystores, passwords, private keys, encoded key material, or signing secrets;
- never print signing secrets in CI logs;
- do not generate production keys until the release cut is explicitly approved.

Key roles:

1. **APP SIGNING KEY**
   - long-lived, user-controlled, stored offline with at least two recovery copies;
   - used to sign GitHub/direct public release APKs;
   - the same app-signing key is enrolled/uploaded into Google Play App Signing for this application.

2. **PLAY UPLOAD KEY**
   - separate from the app-signing key;
   - used only to sign/authenticate AAB uploads to Play;
   - resettable through Play procedures if compromised or lost;
   - not used to sign the APK installed on end-user devices.

3. **DEBUG KEY**
   - Android debug signing only;
   - never treated as a public release identity.

### Cross-source update semantics

The earlier statement that GitHub and Play installs must always use different certificates was incorrect.

If GitHub release APKs are signed with the same app-signing certificate/lineage that Play App Signing uses for generated APKs, Android can accept updates across those sources when the normal update invariants are also satisfied, including the same `applicationId` and a compatible `versionCode`/signing lineage.

This is the intended KU-Yin design. It preserves one long-lived application identity while keeping the Play upload key operationally separate.

F-Droid is different: the default F-Droid signing model uses F-Droid-managed keys. Cross-source updates with GitHub/Play therefore must not be promised unless reproducible/upstream signing is deliberately configured and verified.

Official references used for this decision:

- Android app signing: https://developer.android.com/studio/publish/app-signing
- Play App Signing: https://support.google.com/googleplay/android-developer/answer/9842756
- Android update identity requirements: https://developer.android.com/google/play/app-updates

## 4. Android 15 / 16 edge-to-edge gate

Current branch targets API 36. Android 15 enforces edge-to-edge for apps targeting API 35+, and Android 16 removes the target-36 opt-out path. A launcher/settings Activity therefore must handle system UI insets instead of relying on legacy non-edge-to-edge defaults.

Audit 2.0 evidence:

- `SettingsActivity` previously called `setContentView()` without explicit window-inset handling.
- RED commit `61de1ae` added a P2 contract requiring `ViewCompat.setOnApplyWindowInsetsListener`, system-bar insets, display-cutout insets, and an explicit `requestApplyInsets`.
- Build #144 fails exactly at this new P2 requirement while earlier contracts pass.
- GREEN commit `4666e58` applies system-bar + display-cutout safe padding to `settings_container` and requests initial insets.

The static/API requirement is now encoded. Visual/runtime acceptance is still required because CI compilation cannot prove OEM layout behavior.

Device/emulator matrix before merge:

| Case | Required result |
|---|---|
| Android 15 / API 35, gesture navigation | No title/preference clipping under status or gesture areas |
| Android 15 / API 35, 3-button navigation | Bottom content remains reachable and unobscured |
| Android 16 / API 36, gesture navigation | Same |
| Android 16 / API 36, 3-button navigation | Same |
| Large font | Preference text remains readable/reachable |
| Increased display size | No clipped controls; scrolling remains usable |
| Rotation / multi-window where available | No stale/doubled inset padding |

Official references:

- Android 15 target behavior: https://developer.android.com/about/versions/15/behavior-changes-15
- Android 16 target behavior: https://developer.android.com/about/versions/16/behavior-changes-16
- View edge-to-edge guidance: https://developer.android.com/develop/ui/views/layout/edge-to-edge

## 5. Audit 2.0 architecture decisions

These are closed unless new evidence appears:

### R1 — Do not split `ChewingInputMethodService` now

The service is the Android effect boundary: lifecycle, `InputConnection`, engine lifecycle, candidate rendering bridge, and surface refresh belong together at this stage. File length alone is not evidence of harmful coupling.

Re-open only if profiling or change-amplification shows a real problem, for example measurable key-to-visible latency in render coordination or small features repeatedly requiring unrelated edits.

### R2 — Do not add a nightly full CI lane now

The existing PR CI already covers contracts, bootstrap, JVM tests, APK/AAB, and 16 KB verification. `.github/workflows/upstream-watch.yml` performs a scheduled upstream check. A daily duplicate full build would add noise/cost without a demonstrated detection gap.

Revisit near stable release if a scheduled fresh-environment emulator/security lane has a defined owner and actionable failure policy.

### R3 — Do not reorganize `IMEConfig` now

Audit 2.0 retracts the earlier claim that `IMEConfig` initializes the converter. It does not. Converter initialization remains in `ChewingInputMethodService`; `IMEConfig` is mainly a compatibility/preferences facade and delegates typed keyboard settings to `KeyboardPreferencesRepository`.

Any cleanup is P3 and should wait for a real OpenCC/settings expansion or another concrete change-amplification signal.

## 6. F-Droid source lane

Current dev/CI bootstrap downloads pinned prebuilt libchewing artifacts. That is acceptable for the current development lane but is not the intended F-Droid build path.

Required F-Droid/reproducible lane:

`pinned libchewing source -> per-ABI CMake/Rust build -> dictionary generation -> same staging layout -> Gradle build -> final ELF verification`

Do not auto-switch the existing fast lane until the source lane produces equivalent behavior and passes the same decoder/privacy/candidate tests.

Draft metadata remains conceptually:

```yaml
Categories:
  - System
License: LGPL-2.1-only
RepoType: git
Repo: https://github.com/juhjuhx/KU-Yin_keyboardforandroid
SourceCode: https://github.com/juhjuhx/KU-Yin_keyboardforandroid
IssueTracker: https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues
AutoUpdateMode: Version v%v
UpdateCheckMode: Tags
CurrentVersion: 0.2.0-alpha.1
CurrentVersionCode: 20001
```

Submission remains blocked until the source-build lane and final metadata are proven.

## 7. Device gate and M4 gate

PR #4 remains **Draft** until real-device acceptance is recorded. Do not merge `main`, publish a stable tag, submit Play/F-Droid, or start M4 production merely because CI is green.

Minimum product gate:

- CASE 4 passes on a real device;
- CASE 6 passes on a real device;
- continuous Chinese composition/candidate flow is exercised through the real JNI/libchewing path;
- password / `FORCE_ASCII` / no-personalized-learning editor behavior remains safe;
- editor switching and selection changes do not leak stale composition/candidates;
- Settings edge-to-edge matrix in §4 is visually accepted.

M4 remains blocked until the previously approved CASE 4 + CASE 6 gate passes. Its design approval does not authorize production implementation before that gate.

## 8. Release cut checklist

Only after the device gate:

1. Re-run current-head CI and verify every blocking step against the exact release candidate SHA.
2. Re-review dependencies, permissions, `PRIVACY.md`, and Data Safety claims against the release artifact.
3. Generate the APP SIGNING KEY offline and record its public certificate fingerprints; keep private material out of the repository and ordinary CI.
4. Enroll that same APP SIGNING KEY in Play App Signing.
5. Generate/register the separate PLAY UPLOAD KEY.
6. Produce and verify a signed GitHub release APK and Play AAB path.
7. Verify update continuity with a disposable test installation before public distribution.
8. Publish privacy URL, release notes, screenshots/assets, source/build materials required by licensing, and hashes.
9. Keep PR/release status truthful: CI green alone is not physical-device or distribution readiness.

## 9. Remaining known non-blockers

- Performance matrix is still **UNMEASURED**. Do not add Baseline Profiles or perform speculative micro-optimization before measurement.
- Small per-key allocations and `scaledDensity` deprecation are maintenance items, not release blockers without measured/user-visible impact.
- Legacy compatibility surfaces may be removed in a later bounded cleanup after call-site proof.
- The experimental headless emulator IME-window-visible assertion is not a substitute for physical-device IME acceptance.
