# KU-Yin Recovery v2 Stable Kernel Design

> Branch: `fix/p0-p4-ime-recovery`
> Supersedes the architecture assumptions in `2026-09-09-p0-p4-ime-recovery-design.md` where they conflict with this document.
> Fresh-audit baseline PR head: `39ab7a9ee9b34b4028cc120246285619921067b4`
> Date: 2026-09-09

## Goal

Recover KU-Yin as a privacy-conscious, testable Android IME by stabilizing the existing native Android + Canvas + libchewing stack before any broad toolchain modernization.

## Architecture decision

Adopt **Stable Kernel**:

```text
Android Framework
EditorInfo / InputConnection / lifecycle / selection
                    |
                    v
            EditorPolicyFactory
                    |
                    v
            ImeSessionController
       composition / candidates / actions
          privacy / selection sync
           /                    \
          v                      v
   ChewingEngine             UI State
          |                      |
          v                      v
 AndroidChewingEngine     KeyboardSurface
          |              geometry / touch / a11y
          v
 JNI -> pinned libchewing
```

Keep the existing `ChewingEngine -> AndroidChewingEngine -> JNI` boundary. Do not migrate to Compose or transplant a full third-party keyboard shell during recovery.

## Fresh audit corrections

The previous recovery pass contained useful fixes but several status claims were too optimistic or imprecise. Recovery v2 treats the following as authoritative:

1. Current CI failure occurs in Android resource processing before JVM tests run because `AndroidManifest.xml` references missing `@mipmap/ic_launcher_round`.
2. `OpenCCConverter` is a pass-through stub while Settings exposes conversion as if operational. This is a deceptive-feature defect and must be removed or clearly disabled until a real converter exists.
3. `ChewingInputMethodService` has no editor privacy policy for password variants or `IME_FLAG_NO_PERSONALIZED_LEARNING`.
4. Composition state is not reconciled in `onUpdateSelection()`, leaving native/editor state vulnerable to cursor-selection drift.
5. The current Dachen keyboard lacks an editor-action key, IME switching, and a complete symbol/action layer.
6. `KeyboardView` and `CandidateView` are single Canvas views without per-key/per-candidate accessibility virtual nodes.
7. Native dependencies are pinned to immutable upstream commits, but artifact hashes and reproducibility verification are not yet enforced.
8. `targetSdk=33` is a release migration debt. Toolchain modernization must be isolated from functional recovery.
9. PR #1 has no formal review submission or inline review evidence yet.

## Global constraints

- No network-backed typing, telemetry, keystroke logging, or cloud personalization.
- No broad AGP/Kotlin/Gradle migration until functional recovery gates are green.
- No OpenCC feature claim until a tested backend is connected.
- No merge-to-main claim of runtime readiness without emulator or physical-device evidence.
- Every production behavior change follows RED -> GREEN -> REFACTOR.
- Third-party code may be reused only after per-file license/provenance review; architectural ideas may be referenced without copying implementation.
- Existing libchewing/fcitx5-android pins remain immutable until an explicit dependency-upgrade task.

## Editor policy

Introduce a pure decision layer derived from `EditorInfo`:

```text
EditorPolicy
- isSensitive: Boolean
- allowPersonalizedLearning: Boolean
- allowCandidates: Boolean
- allowComposition: Boolean
- requestedAction: EditorAction
- forceAscii: Boolean
```

Sensitive editor classes include text password, visible password, web password, number password and any additional Android password variations supported by the current SDK. `IME_FLAG_NO_PERSONALIZED_LEARNING` must disable user-model learning even when the field is not a password.

The first recovery implementation may conservatively disable candidates and native user-dictionary learning in sensitive/no-learning contexts. It must not invent unsupported libchewing APIs; libchewing dictionary/session configuration is handled as a separate verified adapter task.

## Session controller

`ImeSessionController` becomes the state owner between Android framework callbacks and the decoder. `ChewingInputMethodService` remains an Android adapter.

Responsibilities:

- reset decoder/editor state on start/finish/restart;
- apply `EditorPolicy`;
- route key actions;
- produce editor operations (`setComposingText`, `commitText`, `finishComposingText`, delete, editor action);
- reconcile selection changes;
- suppress candidates/composition where policy requires;
- expose a small immutable UI state.

No Android `View` logic belongs in the controller.

## UI boundary

Keep Canvas rendering for performance and low dependency cost. Refactor only enough to create separable responsibilities:

```text
KeyboardSurface
  -> LayoutGeometry
  -> TouchInterpreter
  -> CanvasRenderer
  -> AccessibilityVirtualKeys
```

Use `ExploreByTouchHelper` or an equivalent Android accessibility virtual-view mechanism so each key/candidate is independently discoverable and actionable.

Visual direction remains restrained: high legibility, compact candidate density, minimal decoration, explicit pressed states, dark/light support, robust landscape/tablet geometry. Web UI libraries are visual references only and must not become Android runtime dependencies.

## Donor/reference policy

Preferred references in order:

1. Android Developers / AOSP IME contracts.
2. HeliBoard for editor variation, functional-key and IME-switch patterns.
3. FlorisBoard for privacy, state, theming and accessibility patterns.
4. fcitx5-android / fcitx5-chewing for libchewing integration and native packaging.
5. Alibaba OpenCodeReview as a second-pass AI review layer after deterministic CI.

Do not copy GPL or other incompatible code into KU-Yin without an explicit licensing decision.

## Recovery gates

```text
R0 Fresh audit                         DONE
 |
 v
R1 Resource/build baseline
 |
 v
R2 Remove deceptive/dead features
 |
 v
R3 EditorPolicy + ImeSessionController
 |
 v
R4 Complete core keyboard actions
 |
 v
R5 Accessibility + geometry
 |
 v
R6 libchewing privacy-session strategy
 |
 v
R7 Isolated API36/toolchain migration
 |
 v
R8 Native provenance hashes/rebuild checks
 |
 v
R9 Documentation reconciliation
 |
 v
R10 OpenCodeReview + formal PR review
 |
 v
R11 Emulator/device/release verification
```

## Definition of done for this recovery branch

The branch is not merge-ready until all of the following are evidenced:

```text
clean checkout
  -> deterministic native bootstrap
  -> contract checks
  -> JVM unit tests
  -> debug build
  -> release build (signing-independent)
  -> Android recognizes KU-Yin as IME
  -> enable/switch succeeds
  -> typing, space, backspace, editor action work
  -> composition/candidate selection works
  -> sensitive/no-learning editor policy verified
  -> app switching/restart/selection movement do not corrupt composition
  -> accessibility smoke passes
  -> structured second-pass review recorded
```

Device/runtime items must remain explicitly unverified until an emulator or physical-device run actually proves them.
