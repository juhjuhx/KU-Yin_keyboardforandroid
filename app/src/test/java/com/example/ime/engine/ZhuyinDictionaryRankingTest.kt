package com.example.ime.engine

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ZhuyinDictionaryRankingTest {

    private lateinit var dict: ZhuyinDictionary

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dict = ZhuyinDictionary(context)
    }

    @Test
    fun `exact match beats prefix prediction`() {
        val results = dict.query("ㄋㄧˇ")
        assertTrue(results.contains("你"))
        assertEquals("你", results.first())
    }

    @Test
    fun `short key does not leak into longer unrelated input`() {
        val results = dict.query("ㄋㄧˇㄏㄠˇㄋㄧˇ")
        assertFalse(results.contains("你好"))
    }

    @Test
    fun `valid continuation still appears`() {
        val results = dict.query("ㄋㄧ")
        assertTrue(results.contains("你"))
        assertTrue(results.contains("你好"))
    }

    @Test
    fun `learned continuation cannot beat exact match`() {
        dict.recordWordSelection("你好")
        val results = dict.query("ㄋㄧˇ")
        assertEquals("你", results.first())
    }

    @Test
    fun `learning moves word up within its tier`() {
        val before = dict.query("ㄋㄧˇ")
        assertTrue(before.size >= 3)
        val tail = before.last()
        dict.recordWordSelection(tail)
        val after = dict.query("ㄋㄧˇ")
        assertTrue(after.indexOf(tail) < before.indexOf(tail))
    }

    @Test
    fun `tie order is deterministic`() {
        assertEquals(dict.query("ㄋㄧˇ"), dict.query("ㄋㄧˇ"))
    }

    @Test
    fun `candidates contain no duplicates`() {
        val results = dict.query("ㄋㄧˇ")
        assertEquals(results.toSet().size, results.size)
    }

    @Test
    fun `selecting candidate commits once and clears composing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = KuYinEngine(context)
        engine.onZhuyinKey("ㄋ") {}
        engine.onZhuyinKey("ㄧ") {}
        engine.onZhuyinKey("ˇ") {}
        var commits = 0
        var committed = ""
        engine.selectCandidate("你") {
            commits++
            committed = it
        }
        assertEquals(1, commits)
        assertEquals("你", committed)
        assertEquals("", engine.composingZhuyin.value)
        assertTrue(engine.candidates.value.isEmpty())
    }
}
