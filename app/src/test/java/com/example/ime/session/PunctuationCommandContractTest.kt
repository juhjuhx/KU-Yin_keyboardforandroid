package com.example.ime.session

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.ime.engine.KuYinEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A2: punctuation must go through the session (C4-D2.5).
 *
 * RED: `ImeCommand.Punctuation` does not exist at HEAD, so this file does not
 * even compile — the strongest possible RED. It also pins the required order:
 * flush composition, commit Chinese, commit punctuation, clean state.
 *
 * Run (SDK machine): ./gradlew :app:testDebugUnitTest
 *   --tests "com.example.ime.session.PunctuationCommandContractTest"
 */
private class PunctFakeChewingEngine : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false
    var commitCalls = 0

    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() {}
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate = EngineUpdate(
        consumed = true, preedit = "ㄋㄧ", candidates = listOf("你"), committedText = ""
    )
    override fun backspaceUpdate(): EngineUpdate = EngineUpdate(
        consumed = true, preedit = "", candidates = emptyList(), committedText = ""
    )
    override fun selectCandidateUpdate(index: Int): EngineUpdate = EngineUpdate(
        consumed = true, preedit = "", candidates = emptyList(), committedText = "你"
    )
    override fun commitUpdate(): EngineUpdate {
        commitCalls++
        return EngineUpdate(
            consumed = true, preedit = "", candidates = emptyList(), committedText = "你"
        )
    }
    override fun nextPageUpdate(): EngineUpdate? = null
    override fun prevPageUpdate(): EngineUpdate? = null
    override fun canPageCandidatesBackward(): Boolean = false
    override fun canPageCandidatesForward(): Boolean = false
    override fun close() {}
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PunctuationCommandContractTest {

    private fun dictionarySession(): ZhuyinDictionarySession {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return ZhuyinDictionarySession(KuYinEngine(context))
    }

    private fun appliedCommits(session: ComposeDecoderSession, text: String): List<String> {
        val result = session.dispatch(ImeCommand.Punctuation(text))
        val out = mutableListOf<String>()
        result.commitText?.let(out::add)
        out.addAll(result.additionalCommits)
        return out
    }

    @Test
    fun `dictionary punct flushes composition then punctuation with clean state`() {
        val session = dictionarySession()
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        session.dispatch(ImeCommand.TapZhuyinKey("ㄧ"))
        session.dispatch(ImeCommand.TapZhuyinKey("ˇ"))
        val first = session.state.value.candidates.first()
        assertEquals(listOf(first, "，"), appliedCommits(session, "，"))
        assertEquals("", session.state.value.preedit)
        assertTrue(session.state.value.candidates.isEmpty())
    }

    @Test
    fun `dictionary punct on empty commits once`() {
        val session = dictionarySession()
        assertEquals(listOf("。"), appliedCommits(session, "。"))
        assertEquals("", session.state.value.preedit)
    }

    @Test
    fun `chewing punct flushes native preedit then punctuation`() {
        val engine = PunctFakeChewingEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        assertEquals(listOf("你", "，"), appliedCommits(session, "，"))
        assertEquals(1, engine.commitCalls)
        assertEquals("", session.state.value.preedit)
        assertTrue(session.state.value.candidates.isEmpty())
    }

    @Test
    fun `chewing punct on empty commits once without touching engine`() {
        val engine = PunctFakeChewingEngine()
        val session = ChewingEngineSession(engine)
        assertEquals(listOf("。"), appliedCommits(session, "。"))
        assertEquals(0, engine.commitCalls)
    }
}
