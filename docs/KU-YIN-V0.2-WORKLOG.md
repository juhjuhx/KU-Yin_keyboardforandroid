# KU-Yin v0.2 Worklog (keyboard-shell)

Authoritative handoff / context-compression doc for `feat/keyboard-shell-v0.2` (PR #4, Draft).
Spec: `docs/superpowers/specs/2026-09-13-keyboard-shell-v0.2-design.md`.
Production architecture (not README/prototype guesses) is source of truth.

## Current state (2026-09-14, head `a91300a`)

- Branch `feat/keyboard-shell-v0.2` == remote head. Base `main` untouched (no merge).
- This round added 2 commits on top of `ce3b130`:
  - `9dbeadc` fix(ui): restore v0.2 keyboard shell compilation
  - `a91300a` test(ime): lock composition commit boundaries
- CI Build green on the pushed head; PR #4 body updated (no longer "not implemented yet").

## Architecture (as built, not aspirational)

```
┌──────────────────────────────────────┐
│             Keyboard UI              │
│ KeyboardView / CandidateView         │
│ SymbolPicker (compat) / Symbols/Emoji│
└─────────────────┬────────────────────┘
                  │ ImeCommand
                  ▼
┌──────────────────────────────────────┐
│         KeyboardController           │
│ RuntimeState / InputMode / Page      │
│ BottomRowProfile + Validator         │
└─────────────────┬────────────────────┘
                  │ ImeEffect (8 kinds, exhaustive when)
                  ▼
┌──────────────────────────────────────┐
│      ChewingInputMethodService       │
│ dispatchCommand → reduce → execute   │
│ Android lifecycle / InputConnection  │
└───────────┬──────────────────────────┘
            ▼
┌──────────────────────────────────────┐
│   AndroidChewingEngine / libchewing  │
│   (pinned prebuilt 3587ba33)         │
└──────────────────────────────────────┘

Settings UI → KeyboardPreferencesRepository → KeyboardPreferences
→ KeyboardShellLayoutResolver → ResolvedKeyboardLayout → KeyboardView
```

Boundaries enforced: View emits `ImeCommand` only (no InputConnection/Engine/
SharedPreferences access); service executes Android/framework effects only;
`KeyboardController` is the runtime-transition source of truth; libchewing is
the decoder source of truth.

## Decisions

- Compile blocker fixed minimally (`ResolvedKey::toRenderedKey` callable
  reference → `it.toRenderedKey()` lambda). Member-extension refs are illegal
  Kotlin; no KeyboardView rewrite. Verified by local `compileDebugKotlin`.
- R2 bridge audit: `executeEffect` covers all 8 `ImeEffect`s with unique paths
  (exhaustive `when`, compiler-checked). No missing path → no refactor, no
  filler commit.
- Legacy `KeyboardView.onKeyPress`/`setLayout(KeyDef)` surface is dead in the
  v0.2 path (never assigned; resolved keys carry `legacyKey = null`). Left in
  place intentionally — removal is out of scope for this round.
- No engine/composition behavior change: `commitCandidate` already routes via
  `selectCandidateUpdate → applyEngineUpdate` (no hard commit; only
  libchewing-reported `committedText` is committed). Changing JNI-adjacent
  behavior without device reproduction would be speculative.
- Local Mac toolchain installed for fast TDD loop (Homebrew `openjdk@17`,
  Android cmdline-tools + platform-33 + build-tools 33.0.0, Gradle 7.6.4,
  `local.properties` → Homebrew SDK root, gitignored). Wrapper jar is absent
  from the repo (gitignored) so CI/local use system Gradle 7.6.4 + `sh gradlew`
  fallback documented.

## Open questions / suspects for device verification

1. `executeChewingKey` fallback: a libchewing-ignored keystroke mid-composition
   falls through to literal-commit + `chewing.reset()`, wiping composition.
   Suspect for single-char composition drops; needs JNI/device reproduction
   before any fix. Do NOT "fix" blindly.
2. `multiSyllableChineseStaysComposing` /
   `candidateSelectionCanContinueComposition` as service-level tests are not
   JVM-testable (service needs framework; engine needs JNI). Locked at
   controller + engine-helper level instead; full proof requires device pass
   (我應該是 / 我是誰 / 今天晚上要去哪裡).
3. Untracked local build outputs `app/src/main/assets/` (dict `.dat`) and
   `app/src/main/cpp/.deps/` come from `bootstrap_native_deps.sh` (CI runs it
   too). Consider adding both to `.gitignore` in a later hygiene commit.

## Research / donor status

- No open-source donor code copied this round. Donor matrix (fcitx5-android /
  FlorisBoard / HeliBoard / Trime / AOSP LatinIME) not yet built — scheduled
  before Phase D UI work.
- AI Studio prototype: reference only, zero files merged. Banned list
  (KuYinEngine, ZhuyinDictionary, Firebase/OkHttp/Retrofit/Room, network,
  clipboard bar, secrets) untouched — `INTERNET` permission absent.

## Known bugs

- None new. CI `runtime-smoke` (experimental, continue-on-error) fails only on
  the known headless-emulator assertion "KU-Yin input window should become
  visible" (`imeBindsAndSurvivesEditorRecreate`); environment failure, not a
  product regression.

## Security observations

- P3 contract green; manifest/network/dependency audit unchanged this round.
- No logging of keys/preedit/candidates added; test additions assert effect
  routing only, no input content.

## Completed tasks (this round)

- R0 branch sync + spec/diff read (head was already `ce3b130`, clean).
- R1 compile fix + full gate (contracts + JVM + Debug/Release APK).
- R2 bridge audit (complete, no change).
- R3/R5 controller regression tests: InsertText ±composition, ToggleLanguage
  no-commit, NextIME +composition. RED proven by temporary boundary removal
  (1 failure), then restored; local 69/69 green.
- R6 debug APK artifact downloaded (`ku-yin-v0.2-a91300a-debug.apk`).

## Remaining tasks

- M-device: §17 真機 acceptance gate (12 items) — needs physical device.
- M1 architecture + review audit; M2 AI-Studio donor audit (KEEP/ADAPT/DROP).
- M3 candidate architecture (CandidateState model, expand/collapse).
- M4 symbols/emoji pages via resolver; M5 bottom-row settings; M6 Settings UX;
  M7 visual polish; M8 perf/security/a11y audit; M9 device verification;
  M10 release prep (stay Draft until then).

## CI state

- Run 34822913861 (head `9dbeadc`): build SUCCESS; debug-apk + release-apk
  uploaded. Smoke: known headless-window assertion only.
- Run 34825179465 (head `a91300a`): build SUCCESS (69 JVM tests incl. 4 new);
  debug-apk + release-apk uploaded. Smoke: same known assertion.
- Local: 6/6 python contracts OK; `:app:testDebugUnitTest` 69 tests, 0 failures.

## Device verification

Not done (no device in this session). APK ready for install + §17 checklist.

## Next handoff

Branch head: `a91300a` (+ this worklog commit when pushed).
Suggested next slice: M1/M2 audits, then Phase D donor port into
`ResolvedKeyboardLayout → KeyboardView` (no Compose migration in PR #4).
Skills actually used: using-superpowers, systematic-debugging,
test-driven-development, verification-before-completion.
Fallbacks: subagent delegation (`task`/explore/oracle) is broken in this
OpenCode env (ProviderModelNotFoundError) — all exploration done with direct
read/grep/bash instead; UI/design skills deferred (backend round, per §22).
