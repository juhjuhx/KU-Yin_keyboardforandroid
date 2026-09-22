package com.example.ime.engine

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PunctuationTransitionTest {

    private lateinit var engine: KuYinEngine

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        engine = KuYinEngine(context)
    }

    @Test
    fun `punctuation during composition flushes then commits punctuation`() {
        engine.onZhuyinKey("ㄋ") {}
        engine.onZhuyinKey("ㄧ") {}
        engine.onZhuyinKey("ˇ") {}
        val first = engine.candidates.value.first()
        val committed = mutableListOf<String>()
        engine.commitPunctuation("，") { committed.add(it) }
        assertEquals(listOf(first, "，"), committed)
        assertEquals("", engine.composingZhuyin.value)
        assertTrue(engine.candidates.value.isEmpty())
    }

    @Test
    fun `punctuation without composition commits once`() {
        val committed = mutableListOf<String>()
        engine.commitPunctuation("。") { committed.add(it) }
        assertEquals(listOf("。"), committed)
        assertEquals("", engine.composingZhuyin.value)
    }
}
