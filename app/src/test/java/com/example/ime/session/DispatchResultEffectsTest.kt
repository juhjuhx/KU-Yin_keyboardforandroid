package com.example.ime.session

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * D3e RED: one centralized ordered editor-effect projection.
 * Fails now because orderedCommits does not exist yet.
 */
class DispatchResultEffectsTest {

    @Test
    fun `punctuation active shape applies composition before punctuation exactly once each`() {
        val result = DispatchResult("你", true, listOf("，"))
        assertEquals(listOf("你", "，"), orderedCommits(result))
    }

    @Test
    fun `commit only shape applies once`() {
        assertEquals(listOf("ㄋㄧ"), orderedCommits(DispatchResult("ㄋㄧ", true)))
    }

    @Test
    fun `null commit with additional applies additional in order`() {
        val result = DispatchResult(null, true, listOf("，"))
        assertEquals(listOf("，"), orderedCommits(result))
    }

    @Test
    fun `empty result applies nothing`() {
        assertEquals(
            emptyList<String>(),
            orderedCommits(DispatchResult(null, false)),
        )
    }
}
