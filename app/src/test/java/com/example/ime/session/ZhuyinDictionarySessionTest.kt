package com.example.ime.session

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
        assertNull(session.dispatch(ImeCommand.TapZhuyinKey("ㄋ")))
        val state = session.state.value
        assertEquals("ㄋ", state.preedit)
        assertTrue(state.candidates.isNotEmpty())
    }

    @Test
    fun `backspace on empty session commits nothing`() {
        assertNull(session.dispatch(ImeCommand.Backspace))
        assertEquals("", session.state.value.preedit)
    }
}
