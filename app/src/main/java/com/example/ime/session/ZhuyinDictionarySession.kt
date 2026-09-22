package com.example.ime.session

import android.content.Context
import com.example.ime.engine.KuYinEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ZhuyinDictionarySession(private val engine: KuYinEngine) : ComposeDecoderSession {

    constructor(context: Context) : this(KuYinEngine(context))
    private val _state = MutableStateFlow(ComposeImeState())
    override val state: StateFlow<ComposeImeState> = _state.asStateFlow()

    init {
        refresh()
    }

    private fun refresh() {
        _state.value = ComposeImeState(
            preedit = engine.composingZhuyin.value,
            candidates = engine.candidates.value,
            canPageBackward = false,
            canPageForward = false,
            candidatesExpanded = _state.value.candidatesExpanded
        )
    }

    override fun dispatch(command: ImeCommand): DispatchResult {
        var commit: String? = null
        when (command) {
            is ImeCommand.TapZhuyinKey -> engine.onZhuyinKey(command.char) { commit = it }
            is ImeCommand.SelectCandidate -> {
                val candidate = _state.value.candidates.getOrNull(command.index)
                    ?: return DispatchResult(null, false)
                engine.selectCandidate(candidate) { commit = it }
            }
            ImeCommand.Backspace -> {
                if (_state.value.preedit.isEmpty()) return DispatchResult(null, false)
                engine.onBackspace { }
            }
            ImeCommand.Space -> engine.onSpace { commit = it }
            ImeCommand.Enter -> return DispatchResult(null, false)
            ImeCommand.PageForward -> return DispatchResult(null, false)
            ImeCommand.PageBackward -> return DispatchResult(null, false)
            ImeCommand.ToggleCandidatesExpanded -> {
                _state.value = _state.value.copy(
                    candidatesExpanded = !_state.value.candidatesExpanded
                )
                return DispatchResult(null, false)
            }
            ImeCommand.ClearComposing, ImeCommand.Reset -> engine.clearComposing()
        }
        refresh()
        return DispatchResult(commit, true)
    }
}
