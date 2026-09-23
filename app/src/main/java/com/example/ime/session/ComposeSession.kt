package com.example.ime.session

import kotlinx.coroutines.flow.StateFlow

data class ComposeImeState(
    val preedit: String = "",
    val candidates: List<String> = emptyList(),
    val canPageBackward: Boolean = false,
    val canPageForward: Boolean = false,
    val candidatesExpanded: Boolean = false
)

sealed interface ImeCommand {
    data class TapZhuyinKey(val char: String) : ImeCommand
    data class SelectCandidate(val index: Int) : ImeCommand
    data object Backspace : ImeCommand
    data object Space : ImeCommand
    data object Enter : ImeCommand
    data class Punctuation(val text: String) : ImeCommand
    data object Complete : ImeCommand
    data object PageForward : ImeCommand
    data object PageBackward : ImeCommand
    data object ToggleCandidatesExpanded : ImeCommand
    data object ClearComposing : ImeCommand
    data object Reset : ImeCommand
}

data class DispatchResult(
    val commitText: String?,
    val consumed: Boolean,
    val additionalCommits: List<String> = emptyList()
)

interface ComposeDecoderSession {
    val state: StateFlow<ComposeImeState>
    fun dispatch(command: ImeCommand): DispatchResult
}
