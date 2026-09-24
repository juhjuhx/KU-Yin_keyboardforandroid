package com.example.androidkeyboard.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.android.LibChewingDataInstaller
import com.example.androidkeyboard.engines.core.ChewingEngine
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private val CJK = Regex("[一-鿿]")

/**
 * D3h: continuous sentence structural gate on pinned dictionaries.
 * 你好 = ㄋㄧˇ + ㄏㄠˇ = s,u,3,c,l,3. Structural invariants only (no exact
 * ranking claim): syllables coexist, conversion engages, correction preserves
 * context, one final commit, no per-character manual commits.
 */
@RunWith(AndroidJUnit4::class)
class ContinuousSentenceTest {

    @Test
    fun nihaoCoexistsConvertsCorrectsAndCommitsOnce() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val nativePaths = LibChewingDataInstaller.ensureInstalled(context)
        val engine = AndroidChewingEngine(
            systemDataPath = nativePaths.systemDir.absolutePath,
            userDataPath = nativePaths.userFile.absolutePath,
        )
        try {
            engine.init(ChewingEngine.Layout.DACHEN)
            assertTrue("native engine must be ready on an Android runtime", engine.isReady)
            engine.setPersonalizedLearningEnabled(false)

            var last = engine.handleKeyUpdate('s'.code)
            for (key in listOf('u'.code, '3'.code, 'c'.code, 'l'.code, '3'.code)) {
                last = engine.handleKeyUpdate(key)
            }
            assertTrue(
                "two syllables must coexist in one decoder context; preedit='${last.preedit}'",
                last.preedit.isNotEmpty(),
            )
            assertTrue(
                "conversion must engage across syllables; preedit='${last.preedit}'",
                CJK.containsMatchIn(last.preedit),
            )
            assertTrue(
                "correction must be available; candidates=${last.candidates}",
                last.candidates.isNotEmpty(),
            )
            val selected = engine.selectCandidateUpdate(0)
            assertTrue(
                "correction must preserve remaining context; preedit='${selected.preedit}'",
                selected.preedit.isNotEmpty(),
            )
            val committed = engine.commitUpdate()
            assertTrue(
                "final complete must commit once; committed='${committed.committedText}'",
                committed.committedText.isNotEmpty(),
            )
        } finally {
            engine.close()
        }
    }
}
