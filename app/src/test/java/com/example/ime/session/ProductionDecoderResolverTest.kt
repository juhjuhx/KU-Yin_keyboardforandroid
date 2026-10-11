package com.example.ime.session

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.ime.engine.KuYinEngine
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class ResolverFakeEngine : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false

    private fun empty(consumed: Boolean) = EngineUpdate(
        consumed = consumed,
        preedit = "",
        candidates = emptyList(),
        committedText = "",
    )

    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() {}
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate = empty(true)
    override fun backspaceUpdate(): EngineUpdate = empty(false)
    override fun selectCandidateUpdate(index: Int): EngineUpdate = empty(false)
    override fun commitUpdate(): EngineUpdate = empty(false)
    override fun nextPageUpdate(): EngineUpdate? = null
    override fun prevPageUpdate(): EngineUpdate? = null
    override fun canPageCandidatesBackward(): Boolean = false
    override fun canPageCandidatesForward(): Boolean = false
    override fun close() {}
}

/**
 * D3d RED: production resolves ChewingEngineSession when native is ready,
 * DictionarySession only as fallback. Fails now because the resolver
 * does not exist and production still hardwires DictionarySession.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductionDecoderResolverTest {

    @Test
    fun `ready native engine resolves chewing session`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val session = ProductionDecoderResolver.resolve(
            ResolverFakeEngine(),
            KuYinEngine(context),
        )
        assertTrue(session is ChewingEngineSession)
    }

    @Test
    fun `missing native engine falls back to dictionary session`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val session = ProductionDecoderResolver.resolve(
            null,
            KuYinEngine(context),
        )
        assertTrue(session is ZhuyinDictionarySession)
    }
}
