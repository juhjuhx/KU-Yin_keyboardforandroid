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
    private var compositionGeneration = 0L

    private fun project(update: EngineUpdate, compositionChanged: Boolean = true): DispatchResult {
        if (compositionChanged) compositionGeneration++
        val revision = _state.value.generation + 1
        val backward = engine.canPageCandidatesBackward()
        val forward = engine.canPageCandidatesForward()
        _state.value = ComposeImeState(
            preedit = update.preedit,
            candidates = update.candidates,
            canPageBackward = backward,
            canPageForward = forward,
            candidatesExpanded = _state.value.candidatesExpanded && update.preedit.isNotEmpty(),
            generation = revision,
            unavailableReason = update.unavailableReason,
            candidatePage = if (update.preedit.isNotEmpty()) candidatePageSnapshot(
                "libchewing", revision, compositionGeneration, update.candidates,
                update.candidatePage, backward, forward,
            ) else null,
        )
        return DispatchResult(update.committedText.takeIf { it.isNotEmpty() }, update.consumed)
    }

    private fun resetState() {
        compositionGeneration++
        _state.value = ComposeImeState(generation = _state.value.generation + 1)
    }

    private fun completeComposition(): DispatchResult {
        if (_state.value.preedit.isEmpty()) return DispatchResult(null, false)
        // Once composition exists, failed completion must never fall through
        // to SEND/newline on the same press.
        return project(engine.commitUpdate()).copy(consumed = true)
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
                if (command.reference == null || command.reference != _state.value.candidatePage?.ref(command.index))
                    return DispatchResult(null, false)
                val selected = engine.selectCandidateUpdate(command.index)
                // Native choice replaces the composing interval; it is not yet
                // an Android editor write. A visible candidate tap completes it.
                if (!selected.consumed || selected.unavailableReason != null || selected.committedText.isNotEmpty() ||
                    selected.preedit.isEmpty()) return project(selected)
                return project(engine.commitUpdate())
            }
            ImeCommand.Backspace -> return project(engine.backspaceUpdate())
            ImeCommand.Space -> {
                if (_state.value.preedit.isEmpty()) return DispatchResult(" ", true)
                return project(engine.commitUpdate())
            }
            is ImeCommand.Punctuation -> {
                if (_state.value.preedit.isEmpty()) return DispatchResult(command.text, true)
                val flushed = project(engine.commitUpdate())
                if (_state.value.preedit.isNotEmpty() || _state.value.unavailableReason != null)
                    return flushed.copy(consumed = true)
                return DispatchResult(flushed.commitText, true, listOf(command.text))
            }
            ImeCommand.Enter, ImeCommand.Complete -> return completeComposition()
            is ImeCommand.NextCandidatePage -> {
                if (command.snapshot != _state.value.candidatePage) return DispatchResult(null, false)
                val update = engine.nextPageUpdate() ?: return DispatchResult(null, false)
                return project(update, compositionChanged = false)
            }
            is ImeCommand.PreviousCandidatePage -> {
                if (command.snapshot != _state.value.candidatePage) return DispatchResult(null, false)
                val update = engine.prevPageUpdate() ?: return DispatchResult(null, false)
                return project(update, compositionChanged = false)
            }
            ImeCommand.PageForward -> {
                val update = engine.nextPageUpdate() ?: return DispatchResult(null, false)
                return project(update, compositionChanged = false)
            }
            ImeCommand.PageBackward -> {
                val update = engine.prevPageUpdate() ?: return DispatchResult(null, false)
                return project(update, compositionChanged = false)
            }
            ImeCommand.OpenExpandedCandidates, ImeCommand.ToggleCandidatesExpanded -> {
                if (_state.value.preedit.isEmpty()) return DispatchResult(null, false)
                val expanded = command == ImeCommand.OpenExpandedCandidates || !_state.value.candidatesExpanded
                _state.value = _state.value.withExpanded(expanded)
                return DispatchResult(null, false)
            }
            ImeCommand.CloseExpandedCandidates -> {
                _state.value = _state.value.withExpanded(false, invalidate = true)
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
