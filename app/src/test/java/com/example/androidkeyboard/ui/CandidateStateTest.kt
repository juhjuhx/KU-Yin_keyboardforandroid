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

    @Test
    fun flowAssignsSingleRowWhenEverythingFits() {
        val rows = assignFlowRows(listOf(40f, 50f, 60f), maxRowWidth = 200f)

        assertEquals(listOf(0, 0, 0), rows)
        assertEquals(1, flowRowCount(listOf(40f, 50f, 60f), maxRowWidth = 200f))
    }

    @Test
    fun flowWrapsOverflowOntoNextRow() {
        val rows = assignFlowRows(listOf(80f, 80f, 80f), maxRowWidth = 200f)

        assertEquals(listOf(0, 0, 1), rows)
        assertEquals(2, flowRowCount(listOf(80f, 80f, 80f), maxRowWidth = 200f))
    }

    @Test
    fun narrowerWidthProducesMoreRowsForSameCells() {
        val widths = listOf(80f, 80f, 80f)

        assertEquals(1, flowRowCount(widths, maxRowWidth = 300f))
        assertEquals(2, flowRowCount(widths, maxRowWidth = 200f))
        assertEquals(3, flowRowCount(widths, maxRowWidth = 100f))
    }

    @Test
    fun oversizedCellOccupiesItsOwnRow() {
        val rows = assignFlowRows(listOf(500f, 40f), maxRowWidth = 200f)

        assertEquals(listOf(0, 1), rows)
    }

    @Test
    fun emptyCellsNeedNoRows() {
        assertEquals(emptyList<Int>(), assignFlowRows(emptyList(), maxRowWidth = 200f))
        assertEquals(0, flowRowCount(emptyList(), maxRowWidth = 200f))
    }

    @Test
    fun collapsedContainerIsOneRow() {
        assertEquals(44, candidateContainerHeightPx(expanded = false, rows = 1, rowHeightPx = 44, maxRows = 4))
    }

    @Test
    fun expandedContainerGrowsWithRowsUpToCap() {
        assertEquals(88, candidateContainerHeightPx(expanded = true, rows = 2, rowHeightPx = 44, maxRows = 4))
        assertEquals(176, candidateContainerHeightPx(expanded = true, rows = 9, rowHeightPx = 44, maxRows = 4))
    }
}
