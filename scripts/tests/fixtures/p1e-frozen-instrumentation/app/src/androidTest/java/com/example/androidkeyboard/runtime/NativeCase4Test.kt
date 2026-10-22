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
 * D3f: real native CASE-4 on pinned libchewing a6a8fa4.
 *
 * Pinned-version truth (verified on-device): choose_by_index selects/replaces
 * the interval but does NOT immediately commit (commit lands via Enter/Space/
 * auto-commit). CASE-4 therefore proves: select applies the replacement into
 * the SAME native context (no reset), and the next key keeps composing there.
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

            val stepUpdates = listOf('s'.code, 'u'.code, '3'.code).map { key ->
                key to engine.handleKeyUpdate(key)
            }
            val last = stepUpdates.last().second
            assertTrue(
                "s/u/3 must build an active native composition; " +
                    stepUpdates.joinToString { (k, u) ->
                        "$k consumed=${u.consumed} preedit='${u.preedit}' cands=${u.candidates}"
                    },
                last.preedit.isNotEmpty() && last.candidates.isNotEmpty(),
            )
            val selected = engine.selectCandidateUpdate(0)
            assertTrue(
                "select must apply into the live composition without resetting it; " +
                    "consumed=${selected.consumed} preedit='${selected.preedit}'",
                selected.consumed && selected.preedit.isNotEmpty(),
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
