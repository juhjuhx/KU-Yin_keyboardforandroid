package com.example.ime.service

import com.example.androidkeyboard.engines.core.ChewingEngine

/**
 * D3b: safe native startup (P1-A: single init ownership).
 *
 * Runs dictionary install, then opens the engine. The [openEngine] block owns
 * creation AND first init (via [NativeChewingEngineOwner.ensureStarted]); the gate
 * only validates readiness. Any installer/open/init failure or unready engine
 * falls back to null so the service keeps
 * [com.example.ime.session.ZhuyinDictionarySession] as production.
 * No user-facing decoder toggle.
 *
 * Pure logic (no android.util.Log) so plain JUnit covers the fallback paths.
 */
object NativeStartupGate {

    fun start(
        install: () -> Unit,
        openEngine: () -> ChewingEngine,
    ): ChewingEngine? {
        try {
            install()
        } catch (error: Exception) {
            return null
        }
        val engine = try {
            openEngine()
        } catch (error: Exception) {
            return null
        }
        return try {
            if (!engine.isReady) {
                runCatching { engine.close() }
                null
            } else {
                engine
            }
        } catch (error: Exception) {
            runCatching { engine.close() }
            null
        }
    }
}
