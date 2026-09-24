package com.example.ime.service

import com.example.androidkeyboard.engines.core.ChewingEngine

/**
 * D3a: one service lifetime owns ONE native [ChewingEngine].
 *
 * Narrow seam: the service calls [ensureStarted] (init-once), [resetSession]
 * (never recreates), and [close] (close-once, defensive repeats are no-ops).
 * No DI framework; the factory is injected once at construction.
 */
class NativeChewingEngineOwner(
    private val factory: () -> ChewingEngine,
) {
    private var engine: ChewingEngine? = null
    private var closed = false

    fun ensureStarted(): ChewingEngine {
        if (closed) error("NativeChewingEngineOwner is closed")
        val existing = engine
        if (existing != null) return existing
        val created = factory()
        created.init(ChewingEngine.Layout.DACHEN)
        engine = created
        return created
    }

    fun resetSession() {
        engine?.reset()
    }

    fun close() {
        if (closed) return
        closed = true
        engine?.close()
        engine = null
    }
}
