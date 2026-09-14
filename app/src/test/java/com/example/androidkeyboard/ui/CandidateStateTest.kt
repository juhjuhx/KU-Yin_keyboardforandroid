package com.example.androidkeyboard.ui

import com.example.androidkeyboard.engines.core.EngineUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CandidateStateTest {

    private fun update(candidates: List<String>) = EngineUpdate(
        consumed = true,
        preedit = "preedit",
        candidates = candidates,
        committedText = "",
    )

    @Test
    fun collapsedStateKeepsDecoderOrdering() {
        val items = listOf("我", "你", "是", "的")

        val state = candidateStateOf(
            update = update(items),
            expanded = false,
            canPageBackward = false,
            canPageForward = true,
        )

        assertEquals(items, state.items)
        assertFalse(state.expanded)
        assertFalse(state.canPageBackward)
        assertTrue(state.canPageForward)
    }

    @Test
    fun expandCollapsePreservesOrderingUnchanged() {
        val items = listOf("這", "他", "在", "人", "了")

        val collapsed = candidateStateOf(update(items), expanded = false, canPageBackward = true, canPageForward = true)
        val expanded = collapsed.copy(expanded = true)
        val collapsedAgain = expanded.copy(expanded = false)

        assertEquals(items, expanded.items)
        assertEquals(items, collapsedAgain.items)
        assertEquals(collapsed.items, expanded.items)
    }

    @Test
    fun emptyCandidateStateCollapsesSafely() {
        val state = candidateStateOf(
            update = update(emptyList()),
            expanded = true,
            canPageBackward = false,
            canPageForward = false,
        )

        assertTrue(state.items.isEmpty())
        assertFalse(state.canPageBackward)
        assertFalse(state.canPageForward)
    }

    @Test
    fun itemsAreDefensivelyCopied() {
        val source = mutableListOf("我", "你")

        val state = candidateStateOf(update(source), expanded = false, canPageBackward = false, canPageForward = false)
        source.add("是")

        assertEquals(listOf("我", "你"), state.items)
    }
}
