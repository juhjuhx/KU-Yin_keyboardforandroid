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
    object DeleteBackward : ImeEffect
    object CommitComposition : ImeEffect
    data class CommitText(val text: String) : ImeEffect
    object PerformEditorAction : ImeEffect
    object HideKeyboard : ImeEffect
    object ShowNextInputMethod : ImeEffect
}

data class ControllerContext(
    val hasActiveComposition: Boolean,
    val allowComposition: Boolean = true,
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
    ): ControllerResult {
        val effectiveState = normalizeForEditorPolicy(state, context)
        return when (command) {
            is ImeCommand.Input -> input(effectiveState, command)
            ImeCommand.Backspace -> backspace(effectiveState)
            ImeCommand.Space -> space(effectiveState)
            ImeCommand.Enter -> enter(effectiveState, context)
            ImeCommand.ToggleLanguage -> toggleLanguage(effectiveState, context)
            ImeCommand.OpenSymbols -> ControllerResult(
                state = effectiveState.copy(page = KeyboardPage.SYMBOLS_PRIMARY),
                effects = emptyList(),
            )
            ImeCommand.OpenEmoji -> ControllerResult(
                state = effectiveState.copy(page = KeyboardPage.EMOJI),
                effects = emptyList(),
            )
            ImeCommand.ReturnToLetters -> ControllerResult(
                state = effectiveState.copy(page = KeyboardPage.LETTERS),
                effects = emptyList(),
            )
            ImeCommand.Shift -> ControllerResult(
                state = effectiveState.copy(shifted = !effectiveState.shifted),
                effects = emptyList(),
            )
            ImeCommand.NextInputMethod -> ControllerResult(
                state = effectiveState,
                effects = compositionBoundaryEffects(
                    effectiveState,
                    context,
                    ImeEffect.ShowNextInputMethod,
                ),
            )
            ImeCommand.Dismiss -> ControllerResult(
                state = effectiveState,
                effects = compositionBoundaryEffects(
                    effectiveState,
                    context,
                    ImeEffect.HideKeyboard,
                ),
            )
            ImeCommand.ToggleCandidateExpanded -> ControllerResult(
                state = effectiveState.copy(
                    candidateExpanded = !effectiveState.candidateExpanded,
                ),
                effects = emptyList(),
            )
        }
    }

    private fun normalizeForEditorPolicy(
        state: KeyboardRuntimeState,
        context: ControllerContext,
    ): KeyboardRuntimeState {
        if (context.allowComposition || state.inputMode == InputMode.ENGLISH) return state
        return state.copy(
            inputMode = InputMode.ENGLISH,
            page = KeyboardPage.LETTERS,
            shifted = false,
            candidateExpanded = false,
        )
    }

    private fun input(
        state: KeyboardRuntimeState,
        command: ImeCommand.Input,
    ): ControllerResult = when (state.inputMode) {
        InputMode.ZHUYIN -> ControllerResult(
            state = state,
            effects = listOf(ImeEffect.SendChewingKey(command.codePoint)),
        )
        InputMode.ENGLISH -> {
            val char = command.codePoint.toChar()
            val text = if (state.shifted && char.isLetter()) {
                char.uppercaseChar().toString()
            } else {
                char.toString()
            }
            ControllerResult(
                state = state.copy(shifted = false),
                effects = listOf(ImeEffect.CommitText(text)),
            )
        }
    }

    private fun backspace(state: KeyboardRuntimeState): ControllerResult = when (state.inputMode) {
        InputMode.ZHUYIN -> ControllerResult(
            state = state,
            effects = listOf(ImeEffect.BackspaceChewing),
        )
        InputMode.ENGLISH -> ControllerResult(
            state = state,
            effects = listOf(ImeEffect.DeleteBackward),
        )
    }

    private fun space(state: KeyboardRuntimeState): ControllerResult = when (state.inputMode) {
        InputMode.ZHUYIN -> ControllerResult(
            state = state,
            effects = listOf(ImeEffect.SendChewingKey(' '.code)),
        )
        InputMode.ENGLISH -> ControllerResult(
            state = state,
            effects = listOf(ImeEffect.CommitText(" ")),
        )
    }

    private fun enter(
        state: KeyboardRuntimeState,
        context: ControllerContext,
    ): ControllerResult = ControllerResult(
        state = state,
        effects = compositionBoundaryEffects(
            state,
            context,
            ImeEffect.PerformEditorAction,
        ),
    )

    private fun toggleLanguage(
        state: KeyboardRuntimeState,
        context: ControllerContext,
    ): ControllerResult {
        if (!context.allowComposition) {
            return ControllerResult(
                state = state.copy(
                    inputMode = InputMode.ENGLISH,
                    page = KeyboardPage.LETTERS,
                    shifted = false,
                    candidateExpanded = false,
                ),
                effects = emptyList(),
            )
        }

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

    private fun compositionBoundaryEffects(
        state: KeyboardRuntimeState,
        context: ControllerContext,
        terminalEffect: ImeEffect,
    ): List<ImeEffect> = if (
        state.inputMode == InputMode.ZHUYIN && context.hasActiveComposition
    ) {
        listOf(ImeEffect.CommitComposition, terminalEffect)
    } else {
        listOf(terminalEffect)
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
