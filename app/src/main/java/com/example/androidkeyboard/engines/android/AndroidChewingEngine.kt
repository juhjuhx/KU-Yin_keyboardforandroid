package com.example.androidkeyboard.engines.android

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.ChewingEngine.Layout

/**
 * Wave 4 T21: Android-specific ChewingEngine implementation.
 *
 * STUB — JNI binding to libchewing will be added in a follow-up commit.
 * Current behavior: no-op decoder that passes keys through unchanged.
 * Replace body with actual chewing_* C API calls when libchewing native
 * library is available (see DECODER-PIN.md for pin information).
 */
class AndroidChewingEngine : ChewingEngine {

    // TODO T21b: Replace stub with actual libchewing JNI binding
    // See: fcitx5-chewing submodule at 07eddb1696...
    // Prebuilt: lib/fcitx5/src/main/cpp/prebuilt/libchewing/<ABI>/lib/libchewing_capi.a

    private var _ready = false
    private var _layout = Layout.DACHEN
    private var _preedit = ""
    private var _candidates = emptyList<String>()

    override val isReady: Boolean get() = _ready

    override fun init(layout: Layout) {
        _layout = layout
        _ready = true
        _preedit = ""
        _candidates = emptyList()
        // TODO: chewing_initialize(), chewing_set_KBType(ctx, ...)
    }

    override fun reset() {
        _ready = false
        _preedit = ""
        _candidates = emptyList()
        // TODO: chewing_Reset(ctx) — see DECODER-PIN.md §3 NO-FALLBACK rule
    }

    override fun handleKeyEvent(keyCode: Int): Boolean {
        // TODO: chewing_handle_Default(ctx, keyCode)
        return false // stub: key not consumed
    }

    override fun getPreedit(): String = _preedit

    override fun getCandidates(): List<String> = _candidates

    override fun selectCandidate(index: Int) {
        // TODO: chewing_cand_ChoiceByIndex(ctx, index)
    }

    override fun commit() {
        // TODO: chewing_commit_Commit(ctx)
        _preedit = ""
        _candidates = emptyList()
    }

    override fun backspace(): Boolean {
        // TODO: chewing_handle_Backspace(ctx)
        return false
    }

    override fun toggleFullHalf(): Boolean {
        // TODO: chewing_handle_FullHalf(ctx)
        return false
    }

    override fun loadUserDict(path: String): Boolean {
        // TODO: chewing_add_userphrase etc.
        return false
    }

    override fun saveUserDict(path: String) {
        // TODO: chewing_store_userphrase etc.
    }
}
