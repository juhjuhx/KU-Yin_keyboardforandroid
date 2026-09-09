# KU-Yin P0–P4 IME Recovery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore KU-Yin to a reproducible Android IME baseline that can accept Dachen input, synchronize composition/candidates with Android editors, rebuild native dependencies from pins, pass CI, and accurately document its real state.

**Architecture:** Preserve the current Kotlin View/Canvas UI and `ChewingEngine` abstraction. Repair behavior at explicit boundaries: `KeyAction` → `ChewingEngine` → native libchewing → editor synchronization. Native dependencies are generated from a pinned upstream revision and never treated as optional at package time.

**Tech Stack:** Kotlin, Android `InputMethodService`, `InputConnection`, JNI/C++17, libchewing 0.12 alpha Rust/C API, CMake, Gradle/AGP 7.4.2, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-09-p0-p4-ime-recovery-design.md`

## Global Constraints

- Preserve the existing View/Canvas keyboard stack; no Compose migration.
- Keep AGP 7.4.2 / Kotlin 1.9.22 during recovery unless a build blocker proves unavoidable.
- No telemetry, remote typing or plaintext keystroke/candidate logging.
- Native dependency absence must fail the build, not silently create a runtime-broken APK.
- OpenCC remains explicitly pass-through until a tested converter is wired.
- Build success is not documented as runtime success without device/emulator evidence.

---

### Task 1: P0 regression harness and logical key actions

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/com/example/androidkeyboard/input/KeyboardLayout.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/input/KeyMapping.kt`
- Create: `app/src/test/java/com/example/androidkeyboard/input/KeyboardLayoutTest.kt`

**Interfaces:**
- Produces: `enum class KeyAction { INPUT, SPACE, BACKSPACE, DISMISS }`
- Produces: `KeyDef.action: KeyAction`

- [ ] Add JUnit dependency.
- [ ] Add failing assertions for Dachen physical ASCII mapping: `x->ㄌ`, `c->ㄏ`, `v->ㄒ`, `b->ㄖ`, `n->ㄙ`, `m->ㄩ`, `,->ㄝ`, `.->ㄡ`, `/->ㄥ`.
- [ ] Add failing assertions that Space uses ASCII 32 and Backspace/Dismiss are semantic actions rather than label comparisons.
- [ ] Run `./gradlew testDebugUnitTest`; expected RED on current mapping.
- [ ] Implement corrected mapping and `KeyAction`.
- [ ] Run the same test; expected GREEN.

### Task 2: P0 keyboard geometry regression

**Files:**
- Modify: `app/src/main/java/com/example/androidkeyboard/input/KeyboardView.kt`

**Interfaces:**
- Consumes: `KeyboardLayout.rows`
- Produces: key slots rebuilt when measured width changes.

- [ ] Add `onSizeChanged()` and rebuild slots when width becomes non-zero.
- [ ] Guard `rebuildKeySlots()` from caching zero-width geometry.
- [ ] Ensure `refreshLayout()` calls `requestLayout()` before rebuilding.
- [ ] Verify via build plus deterministic geometry reasoning; device smoke remains P3/P4 release gate.

### Task 3: P0 editor synchronization state machine

**Files:**
- Create: `app/src/main/java/com/example/androidkeyboard/input/EditorSync.kt`
- Create: `app/src/test/java/com/example/androidkeyboard/input/EditorSyncTest.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/engines/core/ChewingEngine.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/engines/android/AndroidChewingEngine.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt`

**Interfaces:**
- Produces: `EngineUpdate(preedit, candidates, committedText, consumed)`.
- Produces: `ChewingEngine.handleKeyEvent(...) : EngineUpdate`.
- Produces: `ChewingEngine.backspace() : EngineUpdate`.

- [ ] Write RED unit tests for editor decisions: preedit → composing; non-empty commit → commit + finish; empty preedit after prior composition → finish composing.
- [ ] Extend engine contract to return immutable state snapshots instead of forcing UI to pull partially synchronized fields.
- [ ] Read libchewing commit buffer when keystroke commit bit is set.
- [ ] Update service to call `setComposingText`, `commitText`, `finishComposingText` through a small editor-sync helper.
- [ ] Ensure null `InputConnection` is a no-op and not a crash.
- [ ] Run unit tests GREEN.

### Task 4: P0 candidate selection integrity

**Files:**
- Modify: `app/src/main/java/com/example/androidkeyboard/ui/CandidateView.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/engines/android/AndroidChewingEngine.kt`

**Interfaces:**
- Produces: `CandidateView.onItemClick: ((Int, String) -> Unit)?`
- Consumes: candidate index within engine's current page.

- [ ] Return candidate index with rendered string.
- [ ] Select candidate in libchewing before updating editor state.
- [ ] Do not directly commit rendered candidate text independently of native engine state.
- [ ] Synchronize resulting preedit/commit/candidates after selection.

### Task 5: P1 deterministic native dependency pin

**Files:**
- Create: `native/libchewing.lock`
- Create: `scripts/bootstrap-libchewing.sh`
- Create: `scripts/bootstrap-libchewing.ps1`
- Modify: `.gitignore`
- Modify: `app/src/main/cpp/CMakeLists.txt`
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Pin: `https://github.com/chewing/libchewing.git` at `3c4a93aa03d574c7f011ff84e8a2437c2f79b2cf`.
- Generated root: `.native/libchewing/` and `.native/out/<ABI>/`.

- [ ] Bootstrap script clones/fetches exact revision recursively.
- [ ] Install/check Rust Android targets for four ABIs.
- [ ] Build `chewing_capi` static library for each ABI using Android NDK/cargo-ndk-compatible target configuration.
- [ ] Stage `chewing.h` and archives into `.native/out`.
- [ ] Make CMake fail with `FATAL_ERROR` if staged header/archive is absent.
- [ ] Remove warning-only runtime-broken path.
- [ ] Wire Gradle native build to the generated root without committing binaries.

### Task 6: P1 dictionary/data initialization

**Files:**
- Create: `app/src/main/java/com/example/androidkeyboard/engines/android/ChewingDataInstaller.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/engines/android/AndroidChewingEngine.kt`
- Modify: `app/src/main/cpp/chewing_jni.cpp`
- Modify: build/bootstrap scripts as required.

**Interfaces:**
- Produces: app-private system dictionary directory and user dictionary directory.
- Native constructor uses explicit paths (`chewing_new3`/equivalent pinned C API) rather than process-global implicit search.

- [ ] Stage required libchewing data files into Android assets during bootstrap/build.
- [ ] Copy immutable system dictionaries from assets to app-private storage on version/hash change.
- [ ] Keep user dictionary in a separate writable directory.
- [ ] Initialize native context with explicit system/user paths.
- [ ] Fail readiness when dictionaries cannot be initialized.

### Task 7: P2 lifecycle and privacy hardening

**Files:**
- Modify: `app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/engines/android/AndroidChewingEngine.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `SECURITY.md`

- [ ] Remove per-key and candidate-content logs.
- [ ] Track native library availability explicitly; never call JNI after failed load.
- [ ] Reset/finish composition coherently in `onFinishInput`.
- [ ] Reapply selected keyboard layout after reset.
- [ ] Set `android:allowBackup="false"` for the IME baseline.
- [ ] Document no-network/no-telemetry baseline and sensitive-input logging rules.

### Task 8: P2 OpenCC truthfulness

**Files:**
- Modify: `app/src/main/java/com/example/androidkeyboard/engines/opencc/OpenCCConverter.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/SettingsActivity.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] Expose converter readiness separately from user preference.
- [ ] Disable or clearly label conversion controls when real OpenCC backend is unavailable.
- [ ] Never claim conversion occurred when implementation is pass-through.

### Task 9: P3 CI reconstruction and gates

**Files:**
- Modify: `.github/workflows/build.yml`
- Modify: `BUILD.md`

- [ ] Checkout with submodules/repository history sufficient for pins.
- [ ] Setup JDK 17, Android SDK/NDK, Rust and Android Rust targets.
- [ ] Run bootstrap script.
- [ ] Run `./gradlew testDebugUnitTest`.
- [ ] Run `./gradlew :app:externalNativeBuildDebug`.
- [ ] Run `./gradlew :app:assembleDebug`.
- [ ] Upload APK with `if-no-files-found: error`.
- [ ] Remove stale placeholder comments and false release/AAB claims.

### Task 10: P4 project documentation and attribution

**Files:**
- Modify: `README.md`
- Create: `CONTRIBUTORS.md`
- Modify: `NOTICE`
- Modify: `docs/UPSTREAM.md`
- Modify: `docs/ARCHITECTURE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `CHANGELOG.md`
- Create: `docs/RECOVERY-2026-09-09.md`

- [ ] Replace scaffold-era README with current state, build/install instructions, architecture, known limitations and verification status.
- [ ] Record human ownership and AI-assisted contribution ledger exactly as requested by project owner: OpenCode + Mules Spark 1.3; Hermes + Agnes 2.0 Dash Flash; ChatGPT / GPT-5.6 Sol recovery pass.
- [ ] Preserve upstream attribution for fcitx5-android, fcitx5-chewing, libchewing, OpenCC and any source actually reused.
- [ ] Document P0–P4 status and future work without marking unverified runtime behavior as complete.

### Task 11: P4 architecture/project visuals

**Files:**
- Modify: `docs/diagram/architecture.svg`
- Create: `docs/assets/ku-yin-project-mark.svg`
- Modify: `README.md`

- [ ] Replace stale architecture graphic with the current input/data/native flow.
- [ ] Add a clean repository-native project mark using keyboard/Bopomofo geometry and no third-party brand assets.
- [ ] Reference both assets from README.
- [ ] If ChatGPT image-generation runtime is unavailable, keep the SVG as the deterministic fallback and disclose that limitation in the recovery report.

### Task 12: Verification and PR

- [ ] Fetch branch head and inspect all changed files.
- [ ] Check GitHub Actions status/logs.
- [ ] Fix any CI failures caused by this branch.
- [ ] Open PR `fix/p0-p4-ime-recovery -> main` with P0–P4 checklist and remaining device-smoke gate.
- [ ] Do not merge automatically unless all repository-side checks available to this environment are green.
