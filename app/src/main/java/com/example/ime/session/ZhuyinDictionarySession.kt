package com.example.ime.session

import android.content.Context
import com.example.ime.engine.KuYinEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ZhuyinDictionarySession(context: Context) : ComposeDecoderSession {

    private val engine = KuYinEngine(context)
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

    override fun dispatch(command: ImeCommand): String? {
        var commit: String? = null
        when (command) {
            is ImeCommand.TapZhuyinKey -> engine.onZhuyinKey(command.char) { commit = it }
            is ImeCommand.SelectCandidate -> {
                val candidate = _state.value.candidates.getOrNull(command.index) ?: return null
                engine.selectCandidate(candidate) { commit = it }
            }
            ImeCommand.Backspace -> engine.onBackspace { }
            ImeCommand.Space -> engine.onSpace { commit = it }
            ImeCommand.Enter -> return null
            ImeCommand.PageForward -> return null
            ImeCommand.PageBackward -> return null
            ImeCommand.ToggleCandidatesExpanded -> {
                _state.value = _state.value.copy(
                    candidatesExpanded = !_state.value.candidatesExpanded
                )
                return null
            }
            ImeCommand.ClearComposing, ImeCommand.Reset -> engine.clearComposing()
        }
        refresh()
        return commit
    }
}
