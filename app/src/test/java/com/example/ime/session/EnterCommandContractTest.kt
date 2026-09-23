package com.example.ime.session

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.ime.engine.KuYinEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A1: Enter must go through the session (C4-D2.5).
 *
 * RED: both sessions currently answer `DispatchResult(null, false)` for Enter
 * (ZhuyinDictionarySession.kt:43, ChewingEngineSession.kt:50), so every
 * "commits" assertion below fails against HEAD.
 *
 * Run (SDK machine): ./gradlew :app:testDebugUnitTest
 *   --tests "com.example.ime.session.EnterCommandContractTest"
 */
private class EnterFakeChewingEngine : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false
    var commitCalls = 0

    private fun tapped(): EngineUpdate = EngineUpdate(
        consumed = true,
        preedit = "ㄋ",
        candidates = listOf("你", "尼"),
        committedText = ""
    )

    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() {}
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate = tapped()
    override fun backspaceUpdate(): EngineUpdate = tapped()
    override fun selectCandidateUpdate(index: Int): EngineUpdate =
        tapped().copy(committedText = "你")

    override fun commitUpdate(): EngineUpdate {
        commitCalls++
        return tapped().copy(preedit = "", candidates = emptyList(), committedText = "ㄋ")
    }

    override fun nextPageUpdate(): EngineUpdate? = null
    override fun prevPageUpdate(): EngineUpdate? = null
    override fun canPageCandidatesBackward(): Boolean = false
    override fun canPageCandidatesForward(): Boolean = false
    override fun close() {}
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EnterCommandContractTest {

    private fun dictionarySession(): ZhuyinDictionarySession {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return ZhuyinDictionarySession(KuYinEngine(context))
    }

    @Test
    fun `dictionary enter with active preedit commits first candidate exactly once`() {
        val session = dictionarySession()
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        session.dispatch(ImeCommand.TapZhuyinKey("ㄧ"))
        session.dispatch(ImeCommand.TapZhuyinKey("ˇ"))
        val expected = session.state.value.candidates.first()
        val first = session.dispatch(ImeCommand.Enter)
        assertEquals(expected, first.commitText)
        assertTrue(first.consumed)
        assertEquals("", session.state.value.preedit)
        assertTrue(session.state.value.candidates.isEmpty())
        val second = session.dispatch(ImeCommand.Enter)
        assertNull(second.commitText)
        assertFalse(second.consumed)
    }

    @Test
    fun `dictionary enter on empty preedit is unconsumed`() {
        val session = dictionarySession()
        val result = session.dispatch(ImeCommand.Enter)
        assertNull(result.commitText)
        assertFalse(result.consumed)
    }

    @Test
    fun `chewing enter with active preedit commits via engine exactly once`() {
        val engine = EnterFakeChewingEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        val first = session.dispatch(ImeCommand.Enter)
        assertEquals("ㄋ", first.commitText)
        assertTrue(first.consumed)
        assertEquals(1, engine.commitCalls)
        assertEquals("", session.state.value.preedit)
        val second = session.dispatch(ImeCommand.Enter)
        assertNull(second.commitText)
        assertFalse(second.consumed)
        assertEquals(1, engine.commitCalls)
    }

    @Test
    fun `chewing enter on empty preedit is unconsumed without touching engine`() {
        val engine = EnterFakeChewingEngine()
        val session = ChewingEngineSession(engine)
        val result = session.dispatch(ImeCommand.Enter)
        assertNull(result.commitText)
        assertFalse(result.consumed)
        assertEquals(0, engine.commitCalls)
    }
}
