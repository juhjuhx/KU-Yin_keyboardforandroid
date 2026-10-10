package com.example.ime.session

import com.example.ime.engine.EnglishPrefixProvider
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EnglishWordState(
    val wordBuffer: String = "",
    val candidates: List<String> = emptyList(),
)

/**
 * Owns only the current English token. UI/service apply preedit and ordered commits.
 * Typing, space and punctuation preserve the user's word; only explicit selection completes it.
 * Restricted editors use literal commits and never retain or query their text.
 */
class EnglishWordSession(private val provider: EnglishPrefixProvider) {
    private val mutableState = MutableStateFlow(EnglishWordState())
    val state: StateFlow<EnglishWordState> = mutableState.asStateFlow()
    private var suggestionsEnabled = true

    fun configureSuggestions(enabled: Boolean) {
        suggestionsEnabled = enabled
        reset()
    }

    fun typeText(text: String): DispatchResult {
        if (text.isEmpty()) return DispatchResult(null, false)
        if (!suggestionsEnabled) return DispatchResult(text, true)
        val previous = state.value.wordBuffer
        if (!text.all { it in 'a'..'z' || it in 'A'..'Z' }) return punctuation(text)
        val word = previous + text
        if (word.length > MAX_WORD_LENGTH) {
            reset()
            return DispatchResult(word, true)
        }
        updateWord(word)
        return DispatchResult(null, true)
    }

    fun select(index: Int): DispatchResult {
        val candidate = state.value.candidates.getOrNull(index) ?: return DispatchResult(null, false)
        if (!suggestionsEnabled || state.value.wordBuffer.isEmpty()) return DispatchResult(null, false)
        reset()
        return DispatchResult(candidate, true)
    }

    fun space(): DispatchResult {
        val word = state.value.wordBuffer
        reset()
        return if (word.isEmpty()) DispatchResult(" ", true)
        else DispatchResult(word, true, listOf(" "))
    }

    /** Enter/mode completion consumes a nonempty token once; the next Enter may dispatch action. */
    fun complete(): DispatchResult {
        val word = state.value.wordBuffer
        reset()
        return DispatchResult(word.takeIf { it.isNotEmpty() }, word.isNotEmpty())
    }

    fun punctuation(text: String): DispatchResult {
        if (text.isEmpty()) return complete()
        val word = state.value.wordBuffer
        reset()
        return if (word.isEmpty()) DispatchResult(text, true)
        else DispatchResult(word, true, listOf(text))
    }

    fun backspace(): DispatchResult {
        val word = state.value.wordBuffer
        if (word.isEmpty()) return DispatchResult(null, false)
        updateWord(word.dropLast(1))
        return DispatchResult(null, true)
    }

    fun reset() {
        mutableState.value = EnglishWordState()
    }

    private fun updateWord(word: String) {
        val normalized = word.lowercase(Locale.ROOT)
        val candidates = if (word.isEmpty() || !suggestionsEnabled) emptyList() else {
            provider.suggest(normalized, CANDIDATE_LIMIT).map { candidate ->
                when {
                    word.all { it in 'A'..'Z' } -> candidate.uppercase(Locale.ROOT)
                    word.first() in 'A'..'Z' && word.drop(1).all { it in 'a'..'z' } ->
                        candidate.replaceFirstChar { it.uppercaseChar() }
                    else -> word + candidate.drop(word.length)
                }
            }
        }
        mutableState.value = EnglishWordState(word, candidates)
    }

    companion object {
        private const val MAX_WORD_LENGTH = 64
        private const val CANDIDATE_LIMIT = 8
    }
}
