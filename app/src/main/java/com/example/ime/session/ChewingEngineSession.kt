package com.example.ime.session

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.androidkeyboard.input.KeyboardLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChewingEngineSession(private val engine: ChewingEngine) : ComposeDecoderSession {

    private val keyCodeByLabel: Map<String, Int> =
        KeyboardLayout.Dachen.allKeys.associate { it.label to it.code }

    private val _state = MutableStateFlow(ComposeImeState())
    override val state: StateFlow<ComposeImeState> = _state.asStateFlow()

    private fun project(update: EngineUpdate): DispatchResult {
        _state.value = ComposeImeState(
            preedit = update.preedit,
            candidates = update.candidates,
            canPageBackward = engine.canPageCandidatesBackward(),
            canPageForward = engine.canPageCandidatesForward(),
            candidatesExpanded = _state.value.candidatesExpanded
        )
        return DispatchResult(update.committedText.takeIf { it.isNotEmpty() }, update.consumed)
    }

    private fun resetState() {
        _state.value = ComposeImeState(candidatesExpanded = _state.value.candidatesExpanded)
    }

    private fun completeComposition(): DispatchResult {
        if (_state.value.preedit.isEmpty()) return DispatchResult(null, false)
        return project(engine.commitUpdate())
    }

    override fun dispatch(command: ImeCommand): DispatchResult {
        when (command) {
            is ImeCommand.TapZhuyinKey -> {
                val keyCode = keyCodeByLabel[command.char] ?: return DispatchResult(null, false)
                return project(engine.handleKeyUpdate(keyCode))
            }
            is ImeCommand.SelectCandidate -> {
                if (command.index !in _state.value.candidates.indices) {
                    return DispatchResult(null, false)
                }
                return project(engine.selectCandidateUpdate(command.index))
            }
            ImeCommand.Backspace -> return project(engine.backspaceUpdate())
            ImeCommand.Space -> {
                if (_state.value.preedit.isEmpty()) return DispatchResult(" ", true)
                return project(engine.commitUpdate())
            }
            is ImeCommand.Punctuation -> {
                if (_state.value.preedit.isEmpty()) return DispatchResult(command.text, true)
                val flushed = project(engine.commitUpdate())
                return DispatchResult(flushed.commitText, true, listOf(command.text))
            }
            ImeCommand.Enter, ImeCommand.Complete -> return completeComposition()
            ImeCommand.PageForward -> {
                val update = engine.nextPageUpdate() ?: return DispatchResult(null, false)
                return project(update)
            }
            ImeCommand.PageBackward -> {
                val update = engine.prevPageUpdate() ?: return DispatchResult(null, false)
                return project(update)
            }
            ImeCommand.ToggleCandidatesExpanded -> {
                _state.value = _state.value.copy(
                    candidatesExpanded = !_state.value.candidatesExpanded
                )
                return DispatchResult(null, false)
            }
            ImeCommand.ClearComposing, ImeCommand.Reset -> {
                engine.reset()
                resetState()
                return DispatchResult(null, true)
            }
        }
    }
}
