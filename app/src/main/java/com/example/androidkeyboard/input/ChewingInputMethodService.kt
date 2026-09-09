package com.example.androidkeyboard.input

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.android.EngineUpdate
import com.example.androidkeyboard.engines.core.IMEConfig
import com.example.androidkeyboard.engines.opencc.OpenCCConverter
import com.example.androidkeyboard.ui.CandidateView

class ChewingInputMethodService : InputMethodService() {

    private lateinit var keyboardView: KeyboardView
    private lateinit var candidateView: CandidateView
    private lateinit var symbolPicker: SymbolPicker
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

    override fun onDestroy() {
        chewing.close()
        super.onDestroy()
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
            onPrevPage = {
                chewing.prevPageUpdate()?.let(::applyEngineUpdate)
            }
            onNextPage = {
                chewing.nextPageUpdate()?.let(::applyEngineUpdate)
            }
        }

        symbolPicker = SymbolPicker(this).apply {
            visibility = View.GONE
            onSymbolSelect = ::commitSymbol
            onClose = { symbolPicker.visibility = View.GONE }
        }

        val candidateHeight = (44f * resources.displayMetrics.density).toInt()
        return FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            )
            addView(candidateView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                candidateHeight,
            ).apply { gravity = android.view.Gravity.TOP })
            addView(keyboardView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = candidateHeight
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
        if (::candidateView.isInitialized) candidateView.setCandidates(emptyList())
        if (::keyboardView.isInitialized) keyboardView.refreshLayout()
    }

    override fun onFinishInput() {
        currentInputConnection?.finishComposingText()
        chewing.reset()
        if (::candidateView.isInitialized) candidateView.setCandidates(emptyList())
        super.onFinishInput()
    }

    private fun handleKey(key: KeyDef) {
        val inputConnection = currentInputConnection ?: return

        when (key.action) {
            KeyAction.BACKSPACE -> {
                val update = chewing.backspaceUpdate()
                if (update.consumed) {
                    applyEngineUpdate(update, inputConnection)
                } else {
                    inputConnection.finishComposingText()
                    inputConnection.deleteSurroundingText(1, 0)
                }
            }

            KeyAction.SPACE -> {
                val update = chewing.handleKeyUpdate(key.code)
                if (update.consumed || update.preedit.isNotEmpty() || update.committedText.isNotEmpty()) {
                    applyEngineUpdate(update, inputConnection)
                } else {
                    inputConnection.finishComposingText()
                    inputConnection.commitText(" ", 1)
                    candidateView.setCandidates(emptyList())
                }
            }

            KeyAction.DISMISS -> {
                inputConnection.finishComposingText()
                requestHideSelf(0)
            }

            KeyAction.INPUT -> {
                val libchewingKey = key.code.takeIf { it > 0 }
                    ?: KeyMapping.getLibchewingKeyCode(key.label)

                if (libchewingKey > 0 && chewing.isReady) {
                    applyEngineUpdate(chewing.handleKeyUpdate(libchewingKey), inputConnection)
                } else {
                    inputConnection.finishComposingText()
                    inputConnection.commitText(key.label, 1)
                    candidateView.setCandidates(emptyList())
                }
            }
        }
    }

    private fun applyEngineUpdate(
        update: EngineUpdate,
        inputConnection: InputConnection = currentInputConnection ?: return,
    ) {
        if (update.committedText.isNotEmpty()) {
            val committed = convertForOutput(update.committedText)
            inputConnection.commitText(committed, 1)
        }

        if (update.preedit.isNotEmpty()) {
            inputConnection.setComposingText(update.preedit, 1)
        } else {
            inputConnection.finishComposingText()
        }

        candidateView.setCandidates(update.candidates)
    }

    private fun commitCandidate(index: Int, candidate: String) {
        // The rendered text is intentionally not committed directly. The native
        // decoder must receive the selected index so its composition state stays
        // authoritative. `candidate` remains useful for accessibility/debug UI.
        @Suppress("UNUSED_VARIABLE")
        val renderedCandidate = candidate
        applyEngineUpdate(chewing.selectCandidateUpdate(index))
    }

    private fun commitSymbol(symbol: String) {
        currentInputConnection?.let { inputConnection ->
            inputConnection.finishComposingText()
            inputConnection.commitText(symbol, 1)
        }
    }

    private fun convertForOutput(text: String): String = if (converter.enabled) {
        converter.simplifyToTraditional(text)
    } else {
        text
    }
}
