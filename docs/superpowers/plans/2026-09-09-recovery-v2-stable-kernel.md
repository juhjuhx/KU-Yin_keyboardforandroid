# KU-Yin Recovery v2 Stable Kernel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stabilize KU-Yin's existing native Android + Canvas + libchewing IME into a privacy-correct, testable kernel before API/toolchain modernization.

**Architecture:** Android framework callbacks are adapted into a pure `EditorPolicy` and an `ImeSessionController`; libchewing remains behind `ChewingEngine`, while Canvas UI remains independent. Build/release, privacy, input actions, accessibility and provenance are separate gates so failures remain diagnosable.

**Tech Stack:** Kotlin, Android `InputMethodService`, Android View/Canvas, JUnit 4, JNI/C++17, libchewing, Gradle/AGP existing recovery toolchain, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-09-recovery-v2-stable-kernel-design.md`

## Global Constraints

- No network-backed typing, telemetry, keystroke logging or cloud personalization.
- Keep current AGP/Kotlin/Gradle versions during R1-R6.
- No functional OpenCC claim until a tested backend exists.
- Production behavior changes require RED -> GREEN -> REFACTOR.
- Do not copy third-party implementation without explicit license/provenance review.
- Runtime readiness is not claimed without emulator/device evidence.

---

### Task 1: R1 resource/build baseline

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `scripts/check_p1_build_contract.py`

**Interfaces:**
- Consumes: Android resource names referenced from the manifest.
- Produces: a deterministic contract that rejects dangling app icon resources before Gradle reaches AAPT2.

- [ ] **Step 1: Extend the build contract to assert every manifest `@mipmap/...` application icon reference resolves to a real resource.**
- [ ] **Step 2: Run `python3 scripts/check_p1_build_contract.py`; verify RED on `ic_launcher_round`.**
- [ ] **Step 3: Apply the smallest production fix: remove the unused `android:roundIcon` reference unless a real round icon asset already exists.**
- [ ] **Step 4: Re-run the contract; verify GREEN.**
- [ ] **Step 5: Run `gradle testDebugUnitTest --stacktrace`; verify resource processing now reaches the JVM tests.**
- [ ] **Step 6: Commit as `fix: remove dangling round icon resource`**.

### Task 2: R2 remove deceptive conversion UI

**Files:**
- Modify: `app/src/main/java/com/example/androidkeyboard/SettingsActivity.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/engines/core/IMEConfig.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt`
- Test/Create: `app/src/test/java/com/example/androidkeyboard/engines/core/ConversionAvailabilityTest.kt` or an equivalent pure contract test.

**Interfaces:**
- Consumes: current pass-through `OpenCCConverter` capability.
- Produces: settings/UI that cannot represent conversion as working when no backend is available.

- [ ] **Step 1: Write a failing pure test for an explicit conversion capability model where unavailable conversion cannot be enabled.**
- [ ] **Step 2: Run the targeted test and verify RED because no capability model exists.**
- [ ] **Step 3: Add the smallest capability representation and make Settings hide/disable conversion controls when unavailable.**
- [ ] **Step 4: Ensure the IME output path does not claim conversion semantics while the converter is pass-through.**
- [ ] **Step 5: Re-run the targeted and full JVM tests; verify GREEN.**

### Task 3: R3 EditorPolicy

**Files:**
- Create: `app/src/main/java/com/example/androidkeyboard/input/EditorPolicy.kt`
- Create: `app/src/test/java/com/example/androidkeyboard/input/EditorPolicyTest.kt`

**Interfaces:**
- Consumes: `EditorInfo.inputType`, `EditorInfo.imeOptions`.
- Produces: `EditorPolicy(isSensitive, allowPersonalizedLearning, allowCandidates, allowComposition, forceAscii, action)`.

- [ ] **Step 1: Write failing tests for text password, visible password, web password, numeric password, `IME_FLAG_NO_PERSONALIZED_LEARNING`, normal text and `IME_FLAG_FORCE_ASCII`.**
- [ ] **Step 2: Verify RED because `EditorPolicy` does not exist.**
- [ ] **Step 3: Implement a pure `EditorPolicy.from(EditorInfo)` decision function with conservative privacy defaults.**
- [ ] **Step 4: Re-run targeted tests; verify GREEN.**
- [ ] **Step 5: Refactor only duplicated input-type masking logic and keep tests green.**

### Task 4: R3 session-controller seam

**Files:**
- Create: `app/src/main/java/com/example/androidkeyboard/input/ImeSessionController.kt`
- Create: `app/src/test/java/com/example/androidkeyboard/input/ImeSessionControllerTest.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt`

**Interfaces:**
- Consumes: `EditorPolicy`, `ChewingEngine`, logical `KeyAction`.
- Produces: immutable editor operations and UI state; Android service executes those operations against `InputConnection`.

- [ ] **Step 1: Write failing tests for normal key composition, sensitive-field candidate suppression, backspace fallback, finish/reset and no-learning policy propagation.**
- [ ] **Step 2: Verify RED because the controller does not exist.**
- [ ] **Step 3: Introduce a minimal controller that owns decoder/session state but has no Android `View` dependency.**
- [ ] **Step 4: Adapt `ChewingInputMethodService` to execute controller output against `InputConnection`.**
- [ ] **Step 5: Re-run targeted and full tests; verify GREEN.**

### Task 5: R3 selection reconciliation

**Files:**
- Modify: `app/src/main/java/com/example/androidkeyboard/input/ImeSessionController.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt`
- Modify: `app/src/test/java/com/example/androidkeyboard/input/ImeSessionControllerTest.kt`

**Interfaces:**
- Consumes: `onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)`.
- Produces: deterministic reset/finish behavior when editor selection leaves the active composing region.

- [ ] **Step 1: Write a failing test for cursor movement outside active composition.**
- [ ] **Step 2: Verify RED.**
- [ ] **Step 3: Add minimal reconciliation and wire `onUpdateSelection`.**
- [ ] **Step 4: Verify GREEN plus regression tests.**

### Task 6: R4 core keyboard actions

**Files:**
- Modify: `app/src/main/java/com/example/androidkeyboard/input/KeyboardLayout.kt`
- Modify: `app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt` or controller adapter after Task 4.
- Modify/Create tests under `app/src/test/java/com/example/androidkeyboard/input/`.

**Interfaces:**
- Produces: explicit `EDITOR_ACTION`, `SWITCH_IME`, and symbol-mode actions without overloading display labels.

- [ ] **Step 1: Add failing layout/action tests for editor action and IME switch.**
- [ ] **Step 2: Verify RED.**
- [ ] **Step 3: Add logical actions and derive editor action from `EditorPolicy`.**
- [ ] **Step 4: Wire `switchToNextInputMethod` where supported and a safe fallback to the system picker.**
- [ ] **Step 5: Verify GREEN.**

### Task 7: R5 accessibility seam

**Files:**
- Create: `app/src/main/java/com/example/androidkeyboard/input/KeyboardAccessibilityHelper.kt`
- Create: `app/src/main/java/com/example/androidkeyboard/ui/CandidateAccessibilityHelper.kt`
- Modify: `KeyboardView.kt`, `CandidateView.kt`
- Add instrumentation tests when Android test runtime is available.

**Interfaces:**
- Consumes: current `KeySlot` / candidate geometry.
- Produces: one virtual accessibility node per key/candidate with label, bounds and click action.

- [ ] **Step 1: Add the smallest testable geometry-to-virtual-node contract first.**
- [ ] **Step 2: Verify RED.**
- [ ] **Step 3: Implement with `ExploreByTouchHelper` while preserving Canvas rendering.**
- [ ] **Step 4: Verify JVM geometry contracts and instrumentation accessibility checks where available.**

### Task 8: R6-R8 dependency/privacy provenance research gate

**Files:**
- Modify: `scripts/bootstrap_native_deps.sh`
- Create: `docs/NATIVE_PROVENANCE.md` or update the existing upstream/provenance document.

**Interfaces:**
- Produces: checked SHA-256 manifest for staged libchewing archives/dictionaries and a documented privacy-session strategy based only on verified libchewing APIs.

- [ ] **Step 1: Verify the exact libchewing API/version for dictionary/session selection before production changes.**
- [ ] **Step 2: Add a failing checksum verification contract for staged artifacts.**
- [ ] **Step 3: Add pinned hashes and verification.**
- [ ] **Step 4: Keep privacy-session behavior behind an adapter until API-level tests prove it.**

### Task 9: R7 isolated release toolchain migration

**Files:**
- Root/application Gradle files, wrapper/workflow only after R1-R6 are green.

- [ ] **Step 1: Create a dedicated compatibility matrix for API 36, AGP, Gradle, Kotlin, JDK and NDK.**
- [ ] **Step 2: Upgrade one compatibility axis at a time with clean build/test evidence.**
- [ ] **Step 3: Keep functional behavior unchanged during this task.**

### Task 10: R9-R11 review and release gates

**Files:**
- Reconcile `README.md`, `SECURITY.md`, `ROADMAP.md`, `CHANGELOG.md`, architecture/recovery docs and PR body.

- [ ] **Step 1: Run all deterministic contracts/tests/builds from the PR head.**
- [ ] **Step 2: Run a structured second-pass review (OpenCodeReview when available plus manual/LLM PR review).**
- [ ] **Step 3: Record findings in the PR/repository and resolve all P0/P1 findings.**
- [ ] **Step 4: Run emulator/physical-device IME smoke and accessibility smoke.**
- [ ] **Step 5: Only then mark the draft PR ready for review/merge.**

## Self-review

- Spec coverage: R1-R11 are mapped to independent tasks; toolchain migration is isolated from functional recovery.
- Placeholder scan: no production task relies on an undefined implementation placeholder; research gates explicitly require API verification before code.
- Type consistency: `EditorPolicy` feeds `ImeSessionController`; the Android service remains an adapter; UI/accessibility remains downstream of controller state.
