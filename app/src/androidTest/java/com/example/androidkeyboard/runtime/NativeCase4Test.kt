package com.example.androidkeyboard.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.android.LibChewingDataInstaller
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private class ResetCountingEngine(private val delegate: ChewingEngine) : ChewingEngine by delegate {
    var resetCalls = 0
    override fun reset() {
        resetCalls++
        delegate.reset()
    }
}

/**
 * D3f: real native CASE-4. Candidate selection commits a segment while the
 * remaining context continues in the SAME native context: the next key is
 * consumed without any reset.
 */
@RunWith(AndroidJUnit4::class)
class NativeCase4Test {

    @Test
    fun selectionCommitsSegmentAndNextKeyContinuesSameContext() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val nativePaths = LibChewingDataInstaller.ensureInstalled(context)
        val raw = AndroidChewingEngine(
            systemDataPath = nativePaths.systemDir.absolutePath,
            userDataPath = nativePaths.userFile.absolutePath,
        )
        val engine = ResetCountingEngine(raw)
        try {
            engine.init(ChewingEngine.Layout.DACHEN)
            assertTrue("native engine must be ready on an Android runtime", engine.isReady)
            engine.setPersonalizedLearningEnabled(false)

            for (key in listOf('s'.code, 'u'.code, '3'.code)) {
                engine.handleKeyUpdate(key)
            }
            val selected = engine.selectCandidateUpdate(0)
            assertTrue(
                "selecting candidate 0 must commit a segment; " +
                    "committed='${selected.committedText}' preedit='${selected.preedit}'",
                selected.committedText.isNotEmpty(),
            )
            val resetsBeforeNextKey = engine.resetCalls
            val continued = engine.handleKeyUpdate('1'.code)
            assertTrue(
                "next key after selection must continue the same native context; " +
                    "consumed=${continued.consumed} preedit='${continued.preedit}'",
                continued.consumed,
            )
            assertEquals(
                "CASE-4 must not reset the native context",
                resetsBeforeNextKey,
                engine.resetCalls,
            )
        } finally {
            raw.close()
        }
    }
}
