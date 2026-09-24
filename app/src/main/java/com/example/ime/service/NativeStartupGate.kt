package com.example.ime.service

import com.example.androidkeyboard.engines.core.ChewingEngine

/**
 * D3b: safe native startup.
 *
 * Runs dictionary install, then opens + inits the engine. Any installer
 * failure or unready engine falls back to null so the service keeps
 * [com.example.ime.session.ZhuyinDictionarySession] as production.
 * No user-facing decoder toggle.
 *
 * Pure logic (no android.util.Log) so plain JUnit covers the fallback paths.
 */

/**
 * D3b: safe native startup.
 *
 * Runs dictionary install, then opens + inits the engine. Any installer
 * failure or unready engine falls back to null so the service keeps
 * [com.example.ime.session.ZhuyinDictionarySession] as production.
 * No user-facing decoder toggle.
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
        val engine = openEngine()
        return try {
            engine.init(ChewingEngine.Layout.DACHEN)
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
