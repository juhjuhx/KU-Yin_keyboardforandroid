package com.example.androidkeyboard.input

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.example.androidkeyboard.engines.core.IMEConfig
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.opencc.OpenCCConverter
import com.example.androidkeyboard.input.KeyMapping
import com.example.androidkeyboard.ui.CandidateView

/**
 * Wave 4 T21: InputMethodService wired to engines/core abstraction layer.
 *
 * Changes from Wave 3:
 *  - IMEConfig replaces raw SharedPreferences access
 *  - AndroidChewingEngine (stub) replaces TODO libchewing init
 *  - OpenCCConverter (stub) ready for s2tw/tw2s toggle
 *  - handleKey() routes through chewing engine; special keys bypass
 */
class ChewingInputMethodService : InputMethodService() {

    private lateinit var keyboardView: KeyboardView
    private lateinit var candidateView: CandidateView
    private lateinit var symbolPicker: SymbolPicker
    private lateinit var clipboard: ClipboardManager
    private lateinit var chewing: AndroidChewingEngine
    private lateinit var converter: OpenCCConverter
    private lateinit var config: IMEConfig

    override fun onCreate() {
        super.onCreate()
        config = IMEConfig(this)
        chewing = AndroidChewingEngine().apply { init(config.layout) }
        converter = OpenCCConverter().apply {
            init(config.s2tProfile, config.t2sProfile)
            enabled = config.conversionEnabled
        }
    }

    override fun onCreateInputView(): View = with(LayoutInflater.from(this)) {
        keyboardView = KeyboardView(this@ChewingInputMethodService).apply {
            setLayout(KeyboardLayout.Dachen.rows)
            setKeyHeight(60f)
            setHaptic(config.hapticEnabled)
            setProximityTolerance(config.proximityTolerance)
            onKeyPress = ::handleKey
        }
        candidateView = CandidateView(this@ChewingInputMethodService).apply {
            setCandidates(emptyList())
            onItemClick = ::commitCandidate
        }
        symbolPicker = SymbolPicker(this@ChewingInputMethodService).apply {
            visibility = View.GONE
            onSymbolSelect = ::commitSymbol
            onClose = { symbolPicker.visibility = View.GONE }
        }
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val container = LinearLayout(this@ChewingInputMethodService).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            addView(keyboardView)
            addView(candidateView)
            addView(symbolPicker)
        }
        container
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        keyboardView.refreshLayout()
        // Re-apply config from SharedPreferences (in case user changed settings mid-session)
        config.applyTo(chewing)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        clearComposingText()
        chewing.reset()
    }

    /**
     * T18+T21: Route key through ChewingEngine; special keys bypass decoder.
     */
    private fun handleKey(key: String) {
        val ic = currentInputConnection ?: return
        when (key) {
            "back" -> {
                if (chewing.backspace()) {
                    updateCandidates()
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
            }
            " " -> {
                if (chewing.getPreedit().isNotEmpty()) {
                    // Space commits current preedit through converter
                    val committed = converter.simplifyToTraditional(chewing.getPreedit())
                    ic.commitText(committed, 1)
                    chewing.commit()
                } else {
                    ic.commitText(" ", 1)
                }
            }
            else -> {
                val keyCode = KeyMapping.getKeyEventForChar(key)
                if (keyCode >= 0) {
                    val consumed = chewing.handleKeyEvent(keyCode)
                    if (consumed) {
                        updateCandidates()
                    } else {
                        // Fallback: send key directly (for keys not in mapping)
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
                    }
                } else {
                    // Symbol/key not in mapping — commit directly
                    ic.commitText(key, 1)
                }
            }
        }
    }

    private fun updateCandidates() {
        val preedit = chewing.getPreedit()
        val candidates = chewing.getCandidates()
        candidateView.setCandidates(candidates)
        // TODO: show preedit above keyboard (status bar or inline)
    }

    private fun commitSymbol(sym: String) {
        currentInputConnection?.commitText(sym, 1)
    }

    private fun commitCandidate(candidate: String) {
        val converted = if (converter.enabled) {
            converter.simplifyToTraditional(candidate)
        } else {
            candidate
        }
        currentInputConnection?.commitText(converted, 1)
        chewing.commit()
        updateCandidates()
    }
}
