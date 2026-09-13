# KU-Yin Keyboard Shell v0.2 Design

Date: 2026-09-13
Status: Design approved in chat, pending written-spec review before implementation
Target: post-v0.1.2-alpha architectural refactor

## 1. Goals

Build a configurable Android keyboard shell around the existing libchewing backend without replacing the decoder stack.

The architecture must support a future full per-key layout editor, while the first shipped customization scope is intentionally limited to:

- configurable bottom row and function keys;
- Chinese / English mode switching;
- secondary legends and keyboard height;
- haptic preferences;
- candidate presentation behavior;
- symbol and emoji page architecture;
- continuous Chinese composition with explicit commit boundaries.

The first release does **not** expose arbitrary remapping of the Dachen Zhuyin main character keys.

## 2. Non-goals for the first release

- Arbitrary drag-and-drop remapping of every Zhuyin key.
- Replacing libchewing.
- Adding network services.
- Cloud prediction or telemetry.
- Replacing Android InputMethodService with another IME framework.
- Importing GPL-licensed keyboard implementation code into production source.
- Import/export of full keyboard profiles.
- Long-press space customization.

## 3. Design choice

Adopt a profile-driven keyboard shell with explicit runtime state, semantic IME commands, and external effects.

The model leaves room for a future Layout Editor, while the first UI only exposes a safe subset of customization.

### Why this architecture

The current service owns Android lifecycle, editor policy, layout selection, shift state, candidate state, symbol overlay, libchewing calls and InputConnection writes in one class. Adding language switching and customization directly there would create a new coupling center.

The refactor separates:

1. user preferences;
2. runtime keyboard state;
3. resolved visual layout;
4. semantic input commands;
5. decoder/editor effects;
6. Android lifecycle execution.

## 4. High-level architecture

```text
Settings UI
    |
    v
KeyboardPreferencesRepository
    |
    v
KeyboardPreferences
    |
    +-----------------------+
    |                       |
EditorInfo -> EditorPolicy  |
    |                       |
    v                       v
ImeSessionController -> KeyboardRuntimeState
                            |
                            v
                    KeyboardProfileResolver
                            |
                            v
                    ResolvedKeyboardLayout
                            |
                            v
                       KeyboardView
                            |
                            v
                       ImeCommand
                            |
                            v
                    KeyboardController
                       |          |
                       |          +--> new KeyboardRuntimeState
                       |
                       +--------------> List<ImeEffect>
                                           |
                                           v
                              ChewingInputMethodService
                                    |              |
                                    v              v
                               ChewingEngine  InputConnection
```

`KeyboardController` owns pure state transitions. `ChewingInputMethodService` executes only effects that require libchewing or Android framework access.

## 5. Runtime state

```kotlin
enum class InputMode {
    ZHUYIN,
    ENGLISH,
}

enum class KeyboardPage {
    LETTERS,
    SYMBOLS_PRIMARY,
    SYMBOLS_SECONDARY,
    EMOJI,
}

data class KeyboardRuntimeState(
    val inputMode: InputMode,
    val page: KeyboardPage,
    val shifted: Boolean,
    val candidateExpanded: Boolean,
)
```

Editor policy remains authoritative. Password, FORCE_ASCII and other sensitive editor constraints can force English/ASCII behavior even when the user preference is Chinese.

## 6. Semantic command interface

UI components emit semantic commands instead of calling libchewing or InputConnection directly.

```kotlin
sealed interface ImeCommand {
    data class Input(val codePoint: Int) : ImeCommand
    data object Backspace : ImeCommand
    data object Space : ImeCommand
    data object Enter : ImeCommand
    data object Shift : ImeCommand
    data object ToggleLanguage : ImeCommand
    data object OpenSymbols : ImeCommand
    data object OpenEmoji : ImeCommand
    data object ReturnToLetters : ImeCommand
    data object NextInputMethod : ImeCommand
    data object Dismiss : ImeCommand
    data object ToggleCandidateExpanded : ImeCommand
}
```

The controller returns a state transition plus external effects:

```kotlin
data class ControllerResult(
    val state: KeyboardRuntimeState,
    val effects: List<ImeEffect>,
)
```

Only operations that touch libchewing, Android editor state, or the system IME are effects:

```kotlin
sealed interface ImeEffect {
    data class SendChewingKey(val code: Int) : ImeEffect
    data object BackspaceChewing : ImeEffect
    data object CommitComposition : ImeEffect
    data class CommitText(val text: String) : ImeEffect
    data object PerformEditorAction : ImeEffect
    data object HideKeyboard : ImeEffect
    data object ShowNextInputMethod : ImeEffect
}
```

`SetInputMode`, `SetPage`, Shift and candidate expansion are **not** effects. They are pure `KeyboardRuntimeState` transitions owned by `KeyboardController`.

## 7. Chinese / English transition contract

Default behavior is fixed for the first release:

```text
ZHUYIN with active composition
    -> ToggleLanguage
    -> CommitComposition effect
    -> clear candidates
    -> next runtime state: ENGLISH / LETTERS

ENGLISH
    -> ToggleLanguage
    -> next runtime state: ZHUYIN / LETTERS
```

This is the approved behavior: Chinese preedit is automatically committed before switching to English.

The first release exposes only this AUTO_COMMIT transition in Settings. Alternative transition behavior is deferred.

## 8. Continuous Chinese composition contract

The first release must no longer treat every selected Chinese character as a hard editor commit boundary.

Expected behavior:

```text
ㄨㄛˇ -> 我
ㄧㄥ  -> 我應
ㄍㄞ  -> 我應該
ㄕˋ  -> 我應該是
```

The string remains Android composing text while libchewing still reports an active composition.

Commit boundaries are limited to:

- Enter / editor action when composition is complete;
- language switch to English;
- explicit committed text reported by libchewing;
- editor lifecycle boundary where Android requires composition reset;
- symbol or emoji insertion, which is a hard boundary in v0.2.

Candidate selection updates decoder composition and must not force a final Android commit unless libchewing reports committed text.

## 9. Preferences model

```kotlin
data class KeyboardPreferences(
    val defaultMode: DefaultInputMode,
    val rememberLastMode: Boolean,
    val bottomRowProfile: BottomRowProfile,
    val showSecondaryLabels: Boolean,
    val showLanguageKey: Boolean,
    val showEmojiKey: Boolean,
    val showNextImeKey: Boolean,
    val keyboardHeight: KeyboardHeight,
    val hapticEnabled: Boolean,
    val proximityTolerance: Float,
    val candidateExpandedByDefault: Boolean,
)
```

Persistence is owned by `KeyboardPreferencesRepository`. UI code consumes typed snapshots and does not read SharedPreferences directly outside the repository.

Migration must preserve the existing haptic and proximity tolerance values.

## 10. Bottom-row customization

The first release exposes bottom-row/function-key customization only.

```kotlin
enum class BottomKey {
    SYMBOLS,
    EMOJI,
    COMMA,
    PERIOD,
    LANGUAGE,
    SPACE,
    NEXT_IME,
    ENTER,
    DISMISS,
    NONE,
}

data class BottomRowProfile(
    val left: List<BottomKey>,
    val center: BottomKey = BottomKey.SPACE,
    val right: List<BottomKey>,
)
```

Default profile:

```text
?123 | ， | 中/英 |      空白      | Enter
```

The SPACE slot is flexible-width. The first release lets the user choose and reorder function slots around it.

Validation rules:

- exactly one SPACE slot;
- at least one path from SYMBOLS or EMOJI back to LETTERS;
- no duplicate ENTER key;
- no duplicate NEXT_IME key;
- invalid persisted profiles fall back to the built-in default without crashing the IME.

## 11. Future Layout Editor extension point

The shipped UI keeps the Dachen main character grid fixed, but production models must support future per-key remapping without replacing engine/controller interfaces.

A resolved key separates visual label, decoder code and semantic command:

```kotlin
data class ResolvedKey(
    val id: String,
    val primaryLabel: String,
    val secondaryLabel: String?,
    val weight: Float,
    val command: ImeCommand,
)
```

A future Layout Editor may persist key definitions or overrides against stable `id` values.

The first release only generates these definitions from built-in Dachen and English profiles plus user-configurable function slots.

## 12. Keyboard page providers

Replace the single hard-coded layout enum as the only layout source with providers:

```kotlin
interface KeyboardPageProvider {
    fun resolve(
        state: KeyboardRuntimeState,
        preferences: KeyboardPreferences,
    ): ResolvedKeyboardLayout
}
```

Initial providers:

- `DachenPageProvider`
- `EnglishPageProvider`
- `SymbolPageProvider`
- `EmojiPageProvider`

No network dependency is allowed.

## 13. Symbols and emoji

The current `SymbolPicker` overlay is transitional and is replaced by keyboard pages.

```kotlin
interface SymbolRepository {
    fun categories(): List<SymbolCategory>
    fun symbols(categoryId: String): List<String>
}

interface EmojiRepository {
    fun categories(): List<EmojiCategory>
    fun emoji(categoryId: String): List<EmojiEntry>
    fun recent(): List<EmojiEntry>
}
```

The first implementation uses bundled static data. Recent emoji history is local app-private state.

Selecting a symbol or emoji in v0.2 first commits any active Chinese composition, then commits the selected symbol/emoji text, then remains on the current symbol/emoji page until the user returns to LETTERS.

## 14. Candidate architecture

Decoder paging remains decoder-owned.

Add presentation state independent from the View:

```kotlin
data class CandidateState(
    val items: List<String>,
    val canPageBackward: Boolean,
    val canPageForward: Boolean,
    val expanded: Boolean,
)
```

The first release includes:

- content-width horizontal candidate strip;
- explicit expand/collapse affordance;
- expanded candidate panel/grid;
- candidate selection routed through the controller/engine bridge;
- candidate selection that does not hard-commit unless libchewing reports committed text;
- persisted `candidateExpandedByDefault` preference.

## 15. Settings information architecture

```text
KU-Yin Settings
|
+-- 輸入
|   +-- 預設輸入模式
|   +-- 記住上次模式
|   +-- 中英切換：自動上屏後切換
|   +-- 候選預設展開
|
+-- 鍵盤
|   +-- 配列 Profile
|   +-- 編輯底列
|   +-- 顯示次級 QWERTY
|   +-- 鍵盤高度
|   +-- 震動
|
+-- 符號與 Emoji
|   +-- 顯示 Emoji 鍵
|   +-- 顯示系統輸入法切換鍵
|
+-- 字典
|   +-- reserved for later work
|
+-- 進階
    +-- 恢復預設
```

Import/export and long-press-space customization are deferred beyond v0.2.

## 16. Service responsibility after refactor

`ChewingInputMethodService` retains:

- Android IME lifecycle callbacks;
- EditorInfo -> EditorPolicy construction;
- InputConnection access;
- engine creation/disposal;
- execution of effects that require Android framework or libchewing APIs.

It no longer owns:

- bottom-row policy;
- page transitions;
- Shift state;
- language-mode state;
- candidate expansion state;
- preference interpretation;
- visual layout construction.

## 17. Compatibility and invariants

The refactor must preserve:

- working v0.1.2-alpha Dachen key mapping;
- password / ASCII-only editor policy;
- `IME_FLAG_NO_PERSONALIZED_LEARNING` behavior;
- no INTERNET permission;
- app-private libchewing user data;
- existing native bootstrap and pinned libchewing dependency;
- Dark Mode Settings fix;
- current haptic and proximity user preference values.

## 18. Upstream/reference policy

Reference sources:

- fcitx5-android: Chewing interaction, candidate presentation, symbol/emoji UX, LGPL-compatible architectural reference;
- FlorisBoard: configurable keyboard/profile/settings architecture and theme concepts, Apache-2.0;
- HeliBoard: UX and configuration ideas only unless license compatibility is separately approved, because its production code is GPL-3.0-only.

No code is copied into KU-Yin without per-file license/provenance review.

## 19. Migration sequence

Implementation is incremental:

1. Characterize current v0.1.2 behavior with tests.
2. Add typed preferences repository while preserving old values.
3. Introduce runtime state and semantic `ImeCommand` without changing visible behavior.
4. Extract built-in Dachen/ASCII profiles into providers.
5. Add pure `KeyboardController` and external `ImeEffect` bridge; reduce service responsibilities.
6. Fix continuous composition/candidate commit semantics.
7. Add explicit Chinese/English toggle with auto-commit transition.
8. Add configurable bottom row.
9. Replace symbol overlay with symbol page.
10. Add emoji page/provider and local recent history.
11. Upgrade candidate strip and expanded panel.
12. Expand Settings UI and migration tests.
13. Produce a debug APK and run real-device acceptance.

## 20. Test strategy

### Pure JVM tests

- preference migration;
- invalid-profile fallback;
- bottom-row profile validation;
- layout resolution;
- language transition state machine;
- symbol/emoji page transitions;
- continuous composition command/effect behavior;
- editor-policy overrides;
- candidate-selection commit boundaries.

### Static contracts

- Dachen decoder mapping remains unchanged;
- no INTERNET permission;
- settings keys and defaults remain stable;
- every configurable bottom-row action resolves to a valid command;
- every temporary page has a return path.

### Android instrumentation

- `1` surfaces `ㄅ` preedit;
- a multi-syllable Chinese phrase stays composing across multiple syllables;
- selecting a candidate can continue composition;
- `中/英` commits current Chinese composition and switches to English;
- English QWERTY types ASCII and Shift works;
- switching back to Chinese restores Dachen;
- Settings persist and affect a new IME session;
- symbols and emoji commit text correctly;
- password editor still forces safe ASCII behavior.

### Real-device release gate

A build is not called usable until a real device verifies:

1. continuous Chinese phrase input;
2. candidate selection and continued composition;
3. Chinese -> English auto-commit switch;
4. English -> Chinese switch;
5. configurable bottom row;
6. symbol pages and return-to-letters path;
7. emoji page and recent history;
8. candidate strip expand/collapse;
9. Backspace / Space / Enter/editor actions;
10. Dark Mode Settings;
11. preference persistence after IME restart;
12. reset-to-default recovery.

## 21. Exact v0.2 customization scope

The first release delivers these user-configurable items:

- default input mode;
- remember-last-mode toggle;
- bottom-row function-key composition/order;
- secondary QWERTY labels on Dachen keys;
- keyboard height preset;
- haptic enabled/disabled;
- existing proximity tolerance;
- show/hide language key;
- show/hide emoji key;
- show/hide system next-IME key;
- candidate expanded-by-default;
- reset to default settings.

The first release also delivers these fixed product behaviors:

- architectural support for future Layout Editor overrides;
- continuous Chinese composition;
- Chinese/English auto-commit switching;
- English QWERTY;
- symbol pages;
- emoji pages with local recent history;
- expandable candidate presentation.

It does **not** expose arbitrary Dachen main-key remapping, profile import/export, or long-press-space customization.

## 22. Completion criteria

The architectural work is complete when:

- `ChewingInputMethodService` is no longer the owner of layout/profile policy;
- layout UI resolves entirely from typed preferences + runtime state;
- `KeyboardController` owns pure mode/page/shift/candidate-expanded state transitions;
- backend side effects are explicit `ImeEffect` values executed at the Android/libchewing boundary;
- Dachen and English share the same keyboard-shell interfaces;
- symbol and emoji pages use the same page abstraction;
- continuous Chinese composition passes JVM, instrumentation and real-device tests;
- invalid custom bottom-row profiles safely recover to the default;
- a new debug APK can be installed and used for normal bilingual typing.
