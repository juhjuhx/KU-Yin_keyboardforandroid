package com.example.androidkeyboard.input

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
) {
    companion object {
        fun defaultZhuyin(): KeyboardRuntimeState = KeyboardRuntimeState(
            inputMode = InputMode.ZHUYIN,
            page = KeyboardPage.LETTERS,
            shifted = false,
            candidateExpanded = false,
        )
    }
}

sealed interface ImeCommand {
    data class Input(val codePoint: Int) : ImeCommand
    object Backspace : ImeCommand
    object Space : ImeCommand
    object Enter : ImeCommand
    object Shift : ImeCommand
    object ToggleLanguage : ImeCommand
    object OpenSymbols : ImeCommand
    object OpenEmoji : ImeCommand
    object ReturnToLetters : ImeCommand
    object NextInputMethod : ImeCommand
    object Dismiss : ImeCommand
    object ToggleCandidateExpanded : ImeCommand
}

sealed interface ImeEffect {
    data class SendChewingKey(val code: Int) : ImeEffect
    object BackspaceChewing : ImeEffect
    object CommitComposition : ImeEffect
    data class CommitText(val text: String) : ImeEffect
    object PerformEditorAction : ImeEffect
    object HideKeyboard : ImeEffect
    object ShowNextInputMethod : ImeEffect
}

data class ControllerContext(
    val hasActiveComposition: Boolean,
)

data class ControllerResult(
    val state: KeyboardRuntimeState,
    val effects: List<ImeEffect>,
)

class KeyboardController {
    fun reduce(
        state: KeyboardRuntimeState,
        command: ImeCommand,
        context: ControllerContext,
    ): ControllerResult = when (command) {
        ImeCommand.ToggleLanguage -> toggleLanguage(state, context)
        ImeCommand.OpenSymbols -> ControllerResult(
            state = state.copy(page = KeyboardPage.SYMBOLS_PRIMARY),
            effects = emptyList(),
        )
        ImeCommand.OpenEmoji -> ControllerResult(
            state = state.copy(page = KeyboardPage.EMOJI),
            effects = emptyList(),
        )
        ImeCommand.ReturnToLetters -> ControllerResult(
            state = state.copy(page = KeyboardPage.LETTERS),
            effects = emptyList(),
        )
        ImeCommand.Shift -> ControllerResult(
            state = state.copy(shifted = !state.shifted),
            effects = emptyList(),
        )
        ImeCommand.ToggleCandidateExpanded -> ControllerResult(
            state = state.copy(candidateExpanded = !state.candidateExpanded),
            effects = emptyList(),
        )
        else -> ControllerResult(state = state, effects = emptyList())
    }

    private fun toggleLanguage(
        state: KeyboardRuntimeState,
        context: ControllerContext,
    ): ControllerResult {
        val nextMode = when (state.inputMode) {
            InputMode.ZHUYIN -> InputMode.ENGLISH
            InputMode.ENGLISH -> InputMode.ZHUYIN
        }
        val effects = if (
            state.inputMode == InputMode.ZHUYIN && context.hasActiveComposition
        ) {
            listOf(ImeEffect.CommitComposition)
        } else {
            emptyList()
        }
        return ControllerResult(
            state = state.copy(
                inputMode = nextMode,
                page = KeyboardPage.LETTERS,
                shifted = false,
                candidateExpanded = false,
            ),
            effects = effects,
        )
    }
}

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
) {
    companion object {
        fun default(): BottomRowProfile = BottomRowProfile(
            left = listOf(BottomKey.SYMBOLS, BottomKey.COMMA, BottomKey.LANGUAGE),
            center = BottomKey.SPACE,
            right = listOf(BottomKey.ENTER),
        )
    }
}

object BottomRowProfileValidator {
    fun sanitize(profile: BottomRowProfile): BottomRowProfile {
        val all = profile.left + profile.center + profile.right
        val valid = all.count { it == BottomKey.SPACE } == 1 &&
            all.count { it == BottomKey.ENTER } <= 1 &&
            all.count { it == BottomKey.NEXT_IME } <= 1
        return if (valid) profile else BottomRowProfile.default()
    }
}
