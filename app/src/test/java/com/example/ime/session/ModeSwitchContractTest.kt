package com.example.ime.session

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ime.engine.KeyboardMode
import com.example.ime.engine.KuYinEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A3: mode switch completes composition via session, engine only flips
 * presentation KeyboardMode (C4-D2.5).
 *
 * RED: `ImeCommand.Complete` does not exist at HEAD, so this file does not
 * even compile. Mirrors the production UI order: Complete → apply commit →
 * setMode, asserting exactly-once commit and clean state.
 *
 * Run (SDK machine): ./gradlew :app:testDebugUnitTest
 *   --tests "com.example.ime.session.ModeSwitchContractTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ModeSwitchContractTest {

    private fun engine(): KuYinEngine {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return KuYinEngine(context)
    }

    private fun switchPresentationMode(
        engine: KuYinEngine,
        session: ZhuyinDictionarySession,
        mode: KeyboardMode,
        committed: MutableList<String>
    ) {
        val result = session.dispatch(ImeCommand.Complete)
        result.commitText?.let(committed::add)
        engine.setMode(mode)
    }

    @Test
    fun `zhuyin active to english commits once and flips mode`() {
        val engine = engine()
        val session = ZhuyinDictionarySession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        session.dispatch(ImeCommand.TapZhuyinKey("ㄧ"))
        session.dispatch(ImeCommand.TapZhuyinKey("ˇ"))
        val expected = session.state.value.candidates.first()
        val committed = mutableListOf<String>()
        switchPresentationMode(engine, session, KeyboardMode.ENGLISH, committed)
        assertEquals(listOf(expected), committed)
        assertEquals("", session.state.value.preedit)
        assertTrue(session.state.value.candidates.isEmpty())
        assertEquals(KeyboardMode.ENGLISH, engine.mode.value)
    }

    @Test
    fun `zhuyin active to symbols and emoji commit without ghost state`() {
        for (mode in listOf(KeyboardMode.SYMBOLS, KeyboardMode.EMOJI)) {
            val engine = engine()
            val session = ZhuyinDictionarySession(engine)
            session.dispatch(ImeCommand.TapZhuyinKey("ㄎ"))
            session.dispatch(ImeCommand.TapZhuyinKey("ㄜ"))
            val committed = mutableListOf<String>()
            switchPresentationMode(engine, session, mode, committed)
            assertEquals(1, committed.size)
            assertEquals("", session.state.value.preedit)
            assertTrue(session.state.value.candidates.isEmpty())
            assertEquals(mode, engine.mode.value)
        }
    }

    @Test
    fun `empty to english switches with no commit`() {
        val engine = engine()
        val session = ZhuyinDictionarySession(engine)
        val committed = mutableListOf<String>()
        switchPresentationMode(engine, session, KeyboardMode.ENGLISH, committed)
        assertTrue(committed.isEmpty())
        assertEquals(KeyboardMode.ENGLISH, engine.mode.value)
    }
}
