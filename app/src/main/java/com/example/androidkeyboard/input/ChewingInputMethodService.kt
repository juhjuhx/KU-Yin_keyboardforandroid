package com.example.androidkeyboard.input

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
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
        Log.d(TAG, "onCreate: initializing")
        config = IMEConfig(this)
        chewing = AndroidChewingEngine().apply { init(config.layout) }
        converter = OpenCCConverter().apply {
            init(config.s2tProfile, config.t2sProfile)
            enabled = config.conversionEnabled
        }
    }

    override fun onCreateInputView(): View {
        keyboardView = KeyboardView(this).apply {
            setLayout(KeyboardLayout.Dachen.rows)
            setHaptic(config.hapticEnabled)
            setProximityTolerance(config.proximityTolerance)
            onKeyPress = ::handleKey
        }

        candidateView = CandidateView(this).apply {
            setCandidates(emptyList())
            onItemClick = ::commitCandidate
            onPrevPage = { if (chewing.prevPage()) updateCandidates() }
            onNextPage = { if (chewing.nextPage()) updateCandidates() }
        }

        symbolPicker = SymbolPicker(this).apply {
            visibility = View.GONE
            onSymbolSelect = ::commitSymbol
            onClose = { symbolPicker.visibility = View.GONE }
        }

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val candH = (44f * resources.displayMetrics.density).toInt()
        return FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            )
            addView(candidateView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                candH,
            ).apply { gravity = android.view.Gravity.TOP })
            addView(keyboardView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = candH
                gravity = android.view.Gravity.TOP
            })
            addView(symbolPicker)
        }
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        config.applyTo(chewing)
        converter.enabled = config.conversionEnabled
        converter.init(config.s2tProfile, config.t2sProfile)
        if (::keyboardView.isInitialized) keyboardView.refreshLayout()
    }

    override fun onFinishInput() {
        currentInputConnection?.commitText("", 0)
        chewing.reset()
        super.onFinishInput()
    }

    private fun handleKey(key: KeyDef) {
        val ic = currentInputConnection ?: return

        when (key.action) {
            KeyAction.BACKSPACE -> {
                if (chewing.backspace()) {
                    updateCandidates()
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
            }
            KeyAction.SPACE -> {
                val consumed = chewing.handleKeyEvent(key.code)
                if (!consumed) ic.commitText(" ", 1)
                updateCandidates()
            }
            KeyAction.DISMISS -> requestHideSelf(0)
            KeyAction.INPUT -> {
                val libchewingKey = if (key.code > 0) {
                    key.code
                } else {
                    KeyMapping.getLibchewingKeyCode(key.label)
                }

                if (libchewingKey > 0) {
                    chewing.handleKeyEvent(libchewingKey)
                    updateCandidates()
                } else {
                    ic.commitText(key.label, 1)
                }
            }
        }
    }

    private fun updateCandidates() {
        candidateView.setCandidates(chewing.getCandidates())
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

    companion object {
        private const val TAG = "ChewingIMEService"
    }
}
