package com.example.androidkeyboard.input

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.IMEConfig
import com.example.androidkeyboard.engines.opencc.OpenCCConverter
import com.example.androidkeyboard.ui.CandidateView

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
            onPrevPage = {
                if (chewing.prevPage()) updateCandidates()
            }
            onNextPage = {
                if (chewing.nextPage()) updateCandidates()
            }
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
        config.applyTo(chewing)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        clearComposingText()
        chewing.reset()
    }

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
                    val committed = converter.simplifyToTraditional(chewing.getPreedit())
                    ic.commitText(committed, 1)
                    chewing.commit()
                } else {
                    ic.commitText(" ", 1)
                }
                updateCandidates()
            }
            "cand" -> {
                // Show candidates if there are any
                if (chewing.getCandidates().isNotEmpty()) {
                    updateCandidates()
                } else {
                    ic.commitText(" ", 1)
                }
            }
            else -> {
                val keyCode = KeyMapping.getKeyEventForChar(key)
                if (keyCode >= 0) {
                    val consumed = chewing.handleKeyEvent(keyCode)
                    if (!consumed) {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
                    }
                } else {
                    ic.commitText(key, 1)
                }
            }
        }
    }

    private fun updateCandidates() {
        val candidates = chewing.getCandidates()
        candidateView.setCandidates(candidates)
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
