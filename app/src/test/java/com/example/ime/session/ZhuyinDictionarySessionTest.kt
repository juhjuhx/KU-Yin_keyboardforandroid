package com.example.ime.session

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ime.engine.KuYinEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ZhuyinDictionarySessionTest {

    private lateinit var session: ComposeDecoderSession

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        session = ZhuyinDictionarySession(context)
    }

    @Test
    fun `fresh session is empty and collapsed`() {
        val state = session.state.value
        assertEquals("", state.preedit)
        assertTrue(state.candidates.isEmpty())
        assertFalse(state.canPageBackward)
        assertFalse(state.canPageForward)
        assertFalse(state.candidatesExpanded)
    }

    @Test
    fun `tapping zhuyin appends preedit and projects candidates`() {
        val result = session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        assertNull(result.commitText)
        assertTrue(result.consumed)
        val state = session.state.value
        assertEquals("ㄋ", state.preedit)
        assertTrue(state.candidates.isNotEmpty())
    }

    @Test
    fun `backspace on empty session commits nothing`() {
        val result = session.dispatch(ImeCommand.Backspace)
        assertNull(result.commitText)
        assertFalse(result.consumed)
        assertEquals("", session.state.value.preedit)
    }

    @Test
    fun `session shares the caller engine instance`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = KuYinEngine(context)
        val shared = ZhuyinDictionarySession(engine)
        shared.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        assertEquals("ㄋ", engine.composingZhuyin.value)
        assertEquals("ㄋ", shared.state.value.preedit)
    }
}
