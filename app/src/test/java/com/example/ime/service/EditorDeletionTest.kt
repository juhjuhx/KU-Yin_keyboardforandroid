package com.example.ime.service

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorDeletionTest {

    @Test
    fun `bmp text deletes one code point`() {
        assertEquals(
            EditorDeletion.Plan.DeleteCodePointBeforeCursor,
            EditorDeletion.plan(hasSelection = false)
        )
    }

    @Test
    fun `supplementary text still deletes one code point`() {
        assertEquals(
            EditorDeletion.Plan.DeleteCodePointBeforeCursor,
            EditorDeletion.plan(hasSelection = false)
        )
    }

    @Test
    fun `active selection deletes selection instead`() {
        assertEquals(
            EditorDeletion.Plan.DeleteSelection,
            EditorDeletion.plan(hasSelection = true)
        )
    }
}
