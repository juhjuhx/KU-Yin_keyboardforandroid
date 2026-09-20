package com.example.ime.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeSession : ComposeDecoderSession {
    private val _state = MutableStateFlow(
        ComposeImeState(
            preedit = "",
            candidates = emptyList(),
            canPageBackward = false,
            canPageForward = false,
            candidatesExpanded = false
        )
    )
    override val state: StateFlow<ComposeImeState> = _state.asStateFlow()

    override fun dispatch(command: ImeCommand): String? {
        val current = _state.value
        when (command) {
            is ImeCommand.ToggleCandidatesExpanded -> {
                _state.value = current.copy(candidatesExpanded = !current.candidatesExpanded)
            }
            is ImeCommand.SelectCandidate -> {
                if (command.index !in current.candidates.indices) return null
                return current.candidates[command.index]
            }
            is ImeCommand.ClearComposing, ImeCommand.Reset -> {
                _state.value = current.copy(preedit = "", candidates = emptyList())
            }
            else -> {}
        }
        return null
    }
}

class ComposeDecoderSessionContractTest {

    @Test
    fun `fresh session exposes empty preedit with no pages and collapsed panel`() {
        val state = FakeSession().state.value
        assertEquals("", state.preedit)
        assertTrue(state.candidates.isEmpty())
        assertFalse(state.canPageBackward)
        assertFalse(state.canPageForward)
        assertFalse(state.candidatesExpanded)
    }

    @Test
    fun `selecting from empty candidates commits nothing`() {
        val session = FakeSession()
        assertNull(session.dispatch(ImeCommand.SelectCandidate(0)))
        assertTrue(session.state.value.candidates.isEmpty())
    }

    @Test
    fun `toggle flips expanded flag without touching content`() {
        val session = FakeSession()
        session.dispatch(ImeCommand.ToggleCandidatesExpanded)
        assertTrue(session.state.value.candidatesExpanded)
        assertEquals("", session.state.value.preedit)
        session.dispatch(ImeCommand.ToggleCandidatesExpanded)
        assertFalse(session.state.value.candidatesExpanded)
    }
}
