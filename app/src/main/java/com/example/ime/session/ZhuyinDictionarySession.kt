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

    private fun completeComposition(): String? {
        if (_state.value.preedit.isEmpty()) return null
        val first = _state.value.candidates.firstOrNull()
        return if (first != null) {
            var flushed: String? = null
            engine.selectCandidate(first) { flushed = it }
            flushed
        } else {
            val raw = engine.composingZhuyin.value
            engine.clearComposing()
            raw
        }
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
            is ImeCommand.Punctuation -> {
                if (_state.value.preedit.isEmpty()) {
                    return DispatchResult(command.text, true)
                }
                val first = _state.value.candidates.firstOrNull()
                return if (first != null) {
                    var flushed: String? = null
                    engine.selectCandidate(first) { flushed = it }
                    refresh()
                    DispatchResult(flushed, true, listOf(command.text))
                } else {
                    val raw = engine.composingZhuyin.value
                    engine.clearComposing()
                    refresh()
                    DispatchResult(raw, true, listOf(command.text))
                }
            }
            ImeCommand.Enter, ImeCommand.Complete -> {
                val done = completeComposition() ?: return DispatchResult(null, false)
                refresh()
                return DispatchResult(done, true)
            }
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
