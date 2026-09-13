# KU-Yin Keyboard Shell v0.2 Design

Date: 2026-09-13
Status: Design approved in chat, pending written-spec review before implementation
Target: post-v0.1.2-alpha architectural refactor

## 1. Goals

Build a configurable Android keyboard shell around the existing libchewing backend without replacing the decoder stack.

The architecture must support a future full per-key layout editor, but the first shipped customization scope is intentionally smaller:

- configurable bottom row and function keys;
- Chinese / English mode switching;
- configurable visual behavior such as secondary legends and keyboard height;
- haptic preferences;
- candidate behavior preferences;
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

## 3. Design choice

Adopt a profile-driven keyboard shell with explicit runtime state and semantic IME commands.

This corresponds to the long-term extensible option: the model leaves room for a future Layout Editor, while the first UI only exposes a safe subset of customization.

### Why this architecture

The current service owns Android lifecycle, editor policy, layout selection, shift state, candidate state, symbol overlay, libchewing calls and InputConnection writes in one class. Adding language switching and customization directly there would create a new coupling center.

The refactor therefore separates:

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
                            |
                            v
                        ImeEffect
                            |
                            v
              ChewingInputMethodService
                    |               |
                    v               v
              ChewingEngine    InputConnection
```

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

UI components must emit semantic commands instead of calling libchewing or InputConnection directly.

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
}
```

The controller converts commands into effects. Android lifecycle/service code executes the effects.

Representative effects:

```kotlin
sealed interface ImeEffect {
    data class SendChewingKey(val code: Int) : ImeEffect
    data object BackspaceChewing : ImeEffect
    data object CommitComposition : ImeEffect
    data class CommitText(val text: String) : ImeEffect
    data class SetInputMode(val mode: InputMode) : ImeEffect
    data class SetPage(val page: KeyboardPage) : ImeEffect
    data object PerformEditorAction : ImeEffect
    data object HideKeyboard : ImeEffect
}
```

## 7. Chinese / English transition contract

Default behavior is fixed for the first release:

```text
ZHUYIN with active composition
    -> ToggleLanguage
    -> CommitComposition
    -> clear candidates
    -> switch to ENGLISH

ENGLISH
    -> ToggleLanguage
    -> switch to ZHUYIN
```

This is the approved default behavior: Chinese preedit is automatically committed before switching to English.

The data model may keep an enum for future alternatives, but only AUTO_COMMIT is exposed by default in the first release unless implementation cost is negligible.

## 8. Continuous Chinese composition contract

The first release must no longer treat every selected Chinese character as a hard editor commit boundary.

Expected behavior:

```text
ㄨㄛˇ -> 我
ㄧㄥ  -> 我應
ㄍㄞ  -> 我應該
ㄕˋ  -> 我應該是
```

The string remains Android composing text while the decoder still has an active composition.

Commit boundaries include:

- Enter / editor action when composition is complete;
- language switch to English;
- explicit decoder commit;
- editor lifecycle boundary where Android requires composition reset;
- symbol insertion when the symbol action is defined as a hard boundary.

Candidate selection updates decoder composition and must not automatically force a final Android commit unless libchewing reports committed text.

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

The center SPACE slot is flexible-width. Validation must prevent invalid layouts such as multiple SPACE slots or a layout with no path back from symbols/emoji.

## 11. Future Layout Editor extension point

The shipped UI keeps the Dachen main character grid fixed, but production models must not make it impossible to support future per-key remapping.

A resolved key should therefore separate visual label, physical/decoder code and semantic command:

```kotlin
data class ResolvedKey(
    val id: String,
    val primaryLabel: String,
    val secondaryLabel: String?,
    val weight: Float,
    val command: ImeCommand,
)
```

A future Layout Editor can persist key definitions or overrides without changing the command or engine interfaces.

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

The current `SymbolPicker` overlay is transitional and should be replaced by keyboard pages.

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

The first implementation may use bundled static data. Recent emoji history is local app-private state.

## 14. Candidate architecture

Decoder paging remains decoder-owned.

Add a presentation state that is independent from the candidate View:

```kotlin
data class CandidateState(
    val items: List<String>,
    val canPageBackward: Boolean,
    val canPageForward: Boolean,
    val expanded: Boolean,
)
```

Candidate UI targets:

- content-width horizontal candidate strip;
- explicit expand affordance;
- expanded candidate panel/grid;
- candidate selection routed through the controller/engine bridge;
- no hard commit merely because a candidate was selected.

## 15. Settings information architecture

```text
KU-Yin Settings
|
+-- 輸入
|   +-- 預設輸入模式
|   +-- 記住上次模式
|   +-- 中英切換行為
|   +-- 候選行為
|
+-- 鍵盤
|   +-- 配列 Profile
|   +-- 編輯底列
|   +-- 顯示次級 QWERTY
|   +-- 鍵盤高度
|   +-- 震動
|   +-- 長按空白鍵
|
+-- 符號與 Emoji
|   +-- Emoji 鍵
|   +-- 常用符號
|   +-- 最近使用
|
+-- 字典
|   +-- reserved for later work
|
+-- 進階
    +-- 匯出配置
    +-- 匯入配置
    +-- 恢復預設
```

First implementation may defer import/export if needed, but reset-to-default must exist before unrestricted customization is expanded.

## 16. Service responsibility after refactor

`ChewingInputMethodService` should retain:

- Android IME lifecycle callbacks;
- EditorInfo -> EditorPolicy construction;
- InputConnection access;
- engine creation/disposal;
- execution of effects that require Android framework APIs.

It should no longer own the keyboard design, bottom-row policy, page transitions or preference interpretation.

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

No code should be copied into KU-Yin without per-file license/provenance review.

## 19. Migration sequence

Implementation should be incremental:

1. Characterize current v0.1.2 behavior with tests.
2. Add typed preferences repository while preserving old values.
3. Introduce runtime state and semantic `ImeCommand` without changing visible behavior.
4. Extract built-in Dachen/ASCII profiles into providers.
5. Add controller/effect bridge and reduce service responsibilities.
6. Fix continuous composition/candidate commit semantics.
7. Add explicit Chinese/English toggle with auto-commit transition.
8. Add configurable bottom row.
9. Replace symbol overlay with symbol page.
10. Add emoji page/provider.
11. Upgrade candidate strip/expanded panel.
12. Expand Settings UI and migration tests.
13. Produce a debug APK and run real-device acceptance.

## 20. Test strategy

### Pure JVM tests

- preference migration;
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
6. symbol page;
7. emoji page;
8. Backspace / Space / Enter/editor actions;
9. Dark Mode Settings;
10. persistence after IME restart.

## 21. First-release scope

The first release delivers:

- architectural refactor for future Layout Editor support;
- continuous Chinese composition;
- user-visible Chinese/English switch;
- English QWERTY;
- configurable bottom row/function keys;
- keyboard height, secondary-label and haptic preferences;
- candidate behavior setting(s);
- symbols and emoji page infrastructure;
- improved candidate presentation where feasible within the same release.

The first release does **not** expose arbitrary Dachen main-key remapping.

## 22. Completion criteria

The architectural work is complete when:

- `ChewingInputMethodService` is no longer the owner of layout/profile policy;
- layout UI can be resolved entirely from typed preferences + runtime state;
- backend behavior is driven by semantic commands/effects;
- Dachen and English share the same keyboard-shell interfaces;
- symbol and emoji pages use the same page abstraction;
- continuous Chinese composition passes JVM, instrumentation and real-device tests;
- a new debug APK can be installed and used for normal bilingual typing.
