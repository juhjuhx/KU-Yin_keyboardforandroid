package com.example.ime.session

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.ime.engine.KuYinEngine

/** D3d: normal production resolves the native session; dictionary is fallback-only. */
object ProductionDecoderResolver {
    fun resolve(
        nativeEngine: ChewingEngine?,
        dictionaryEngine: KuYinEngine,
    ): ComposeDecoderSession =
        if (nativeEngine != null) ChewingEngineSession(nativeEngine)
        else ZhuyinDictionarySession(dictionaryEngine)
}
