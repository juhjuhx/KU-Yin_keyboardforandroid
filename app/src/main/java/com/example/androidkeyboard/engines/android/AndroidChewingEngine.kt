package com.example.androidkeyboard.engines.android

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.ChewingEngine.Layout

/**
 * Wave 4 T21: Android ChewingEngine implementation with JNI bindings.
 *
 * ===== STUB → REAL MODE =====
 * Current: pass-through stub (no-op decoder)
 * When NDK is available: swap System.loadLibrary + external funs activate
 *
 * ===== LIBCHEWING C API REFERENCE (chewing.h 0.13.x) =====
 *
 * Context lifecycle:
 *   ChewingContext *chewing_new(void)
 *   void            chewing_delete(ChewingContext *)
 *   void            chewing_reset(ChewingContext *)
 *
 * Key event:
 *   int chewing_handle_Default(ChewingContext *, int key)
 *     Return: 0=LOOK, 1=COMMIT, 2=PHRASE_CHOICE
 *
 * Preedit:
 *   gchar *chewing_get_composing_str(ChewingContext *)
 *   gint   chewing_get_cursor_rest_pos(ChewingContext *)
 *
 * Candidates:
 *   gint chewing_cand_ChoiceCount(ChewingContext *)
 *   gchar **chewing_cand_choiceString(ChewingContext *, gint index)
 *   void chewing_cand_ChoiceByIndex(ChewingContext *, gint index)
 *   void chewing_cand_close(ChewingContext *)
 *
 * Commit:
 *   gchar *chewing_commit_str(ChewingContext *)
 *
 * Backspace:
 *   void chewing_handle_Backspace(ChewingContext *)
 *
 * Full/Half:
 *   void chewing_handle_FullHalf(ChewingContext *)
 *
 * Layout:
 *   void chewing_set_KBType(ChewingContext *, int kbtype)
 *     KB_DEFAULT=1 (Dachen), KB_HSU=2, KB_ET26=6
 *
 * User dict:
 *   gint chewing_load_userphrase(ChewingContext *, const gchar *path)
 *   void chewing_store_userphrase(ChewingContext *, const gchar *path)
 *
 * ===== JNI ACTIVATION CHECKLIST =====
 * 1. Install Android NDK r25+ via SDK Manager
 * 2. Add app/src/main/cpp/CMakeLists.txt (see docs/JNI-INTEGRATION.md)
 * 3. Add ndkVersion to defaultConfig in build.gradle.kts
 * 4. Run: ./gradlew :app:externalNativeBuildDebug
 * 5. Verify: libchewing-jni.so appears in app/build/outputs/
 */
class AndroidChewingEngine : ChewingEngine {

    private var nativeCtx: Long = 0L
    private var _layout = Layout.DACHEN
    private var _preedit = "
    private var _candidates = emptyList<String>()
    private var _ready = false

    override val isReady: Boolean get() = _ready

    init { loadNativeLibrary() }

    private fun loadNativeLibrary() {
        try { System.loadLibrary( chewing-jni) }
        catch (e: UnsatisfiedLinkError) { /* stub mode */ }
    }

    override fun init(layout: Layout) {
        _layout = layout
        if (nativeCtx == 0L) nativeCtx = chewing_new()
        else chewing_reset(nativeCtx)
        // NO-FALLBACK: caller sets layout, we respect it
        val kbType = when (layout) {
            Layout.DACHEN -> 1
            Layout.HSU -> 2
            Layout.Eten26 -> 6
            else -> 1
        }
        chewing_set_kb_type(nativeCtx, kbType)
        _ready = true
        _preedit = "
        _candidates = emptyList()
    }

    override fun reset() {
        if (nativeCtx != 0L) chewing_reset(nativeCtx)
        _ready = false; _preedit = "; _candidates = emptyList()
    }

    override fun handleKeyEvent(keyCode: Int): Boolean {
        if (nativeCtx == 0L) return false
        val result = chewing_handle_default(nativeCtx, keyCode)
        _preedit = chewing_get_composing_str(nativeCtx)
        _candidates = buildCandidates()
        return result != 0
    }

    override fun getPreedit(): String = _preedit
    override fun getCandidates(): List<String> = _candidates

    override fun selectCandidate(index: Int) {
        if (nativeCtx != 0L) chewing_cand_choice_by_index(nativeCtx, index)
    }

    override fun commit() {
        if (nativeCtx == 0L) { _preedit = "; _candidates = emptyList(); return }
        chewing_commit_str(nativeCtx)
        _preedit = "; _candidates = emptyList()
    }

    override fun backspace(): Boolean {
        if (nativeCtx == 0L) return false
        chewing_handle_backspace(nativeCtx)
        _preedit = chewing_get_composing_str(nativeCtx)
        _candidates = buildCandidates()
        return _preedit.isNotEmpty()
    }

    override fun toggleFullHalf(): Boolean {
        if (nativeCtx != 0L) chewing_handle_full_half(nativeCtx)
        return true
    }

    override fun loadUserDict(path: String): Boolean {
        if (nativeCtx == 0L) return false
        return chewing_load_userphrase(nativeCtx, path) >= 0
    }

    override fun saveUserDict(path: String) {
        if (nativeCtx != 0L) chewing_store_userphrase(nativeCtx, path)
    }

    private fun buildCandidates(): List<String> {
        if (nativeCtx == 0L) return emptyList()
        val count = chewing_cand_choice_count(nativeCtx)
        if (count <= 0) return emptyList()
        return (0 until count).map { i -> chewing_cand_choice_string(nativeCtx, i) ?: " }
            .filter { it.isNotEmpty() }
    }

    override fun finalize() {
        if (nativeCtx != 0L) { chewing_delete(nativeCtx); nativeCtx = 0L }
    }

    // JNI native declarations
    private external fun chewing_new(): Long
    private external fun chewing_delete(ctx: Long)
    private external fun chewing_reset(ctx: Long)
    private external fun chewing_handle_default(ctx: Long, key: Int): Int
    private external fun chewing_get_composing_str(ctx: Long): String
    private external fun chewing_cand_choice_count(ctx: Long): Int
    private external fun chewing_cand_choice_string(ctx: Long, index: Int): String?
    private external fun chewing_cand_choice_by_index(ctx: Long, index: Int)
    private external fun chewing_commit_str(ctx: Long): String
    private external fun chewing_handle_backspace(ctx: Long)
    private external fun chewing_handle_full_half(ctx: Long)
    private external fun chewing_set_kb_type(ctx: Long, kbtype: Int)
    private external fun chewing_load_userphrase(ctx: Long, path: String): Int
    private external fun chewing_store_userphrase(ctx: Long, path: String)
}