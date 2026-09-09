package com.example.androidkeyboard.input

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.core.IMEConfig
import com.example.androidkeyboard.engines.opencc.OpenCCConverter
import com.example.androidkeyboard.ui.CandidateView
import android.util.Log

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
        Log.d(TAG, "onCreateInputView: creating view")
        
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

        // Candidate bar (44dp, TOP) + keyboard below it: explicit bounds so the
        // candidate view can never overlay the keyboard and swallow touches.
        val candH = (44f * resources.displayMetrics.density).toInt()
        val container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            val candLp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, candH
            )
            candLp.gravity = android.view.Gravity.TOP
            addView(candidateView, candLp)
            val kbLp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            kbLp.topMargin = candH
            kbLp.gravity = android.view.Gravity.TOP
            addView(keyboardView, kbLp)
            addView(symbolPicker)
        }
        
        Log.d(TAG, "onCreateInputView: returned view with keyboard=${keyboardView.height}dp")
        return container
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        Log.d(TAG, "onStartInput: attribute=$attribute, restarting=$restarting")
        
        config.applyTo(chewing)

        // Re-sync converter state so toggle/profile changes in Settings
        // take effect without requiring a process kill.
        converter.enabled = config.conversionEnabled
        converter.init(config.s2tProfile, config.t2sProfile)
        
        // Refresh layout after view is created
        if (::keyboardView.isInitialized) {
            keyboardView.refreshLayout()
        }
    }

    override fun onStartInputView(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
        Log.d(TAG, "onStartInputView: keyboard view starting, restarting=$restarting")
    }

    override fun onFinishInput() {
        super.onFinishInput()
        Log.d(TAG, "onFinishInput")
        currentInputConnection?.commitText("", 0)
        chewing.reset()
    }

    override fun onFinishInputView(finishedTyping: Boolean) {
        super.onFinishInputView(finishedTyping)
        Log.d(TAG, "onFinishInputView: finishedTyping=$finishedTyping")
    }

    private fun handleKey(key: KeyDef) {
        val ic = currentInputConnection ?: run {
            Log.e(TAG, "handleKey: no input connection!")
            return
        }
        Log.d(TAG, "handleKey: '${key.label}' code=${key.code}")
        
        when {
            key.label == "back" -> {
                if (chewing.backspace()) {
                    updateCandidates()
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
            }
            key.label == " " -> {
                // Space sends libchewing keycode 65 (KEY_SPACE)
                val consumed = chewing.handleKeyEvent(65)
                if (!consumed) {
                    ic.commitText(" ", 1)
                }
                updateCandidates()
            }
            key.label == "▼" -> requestHideSelf(0)
            key.isSpecial -> {
                // Special keys without a bopomofo code are handled above; fall through
                ic.commitText(key.label, 1)
            }
            else -> {
                // Use the physical key's ASCII code directly (authoritative).
                // KeyDef.code is set for every bopomofo/tone key in KeyboardLayout.
                val libchewingKey = key.code
                Log.d(TAG, "key '${key.label}' -> libchewing keycode $libchewingKey")
                
                if (libchewingKey > 0) {
                    val consumed = chewing.handleKeyEvent(libchewingKey)
                    Log.d(TAG, "handleKeyEvent($libchewingKey) returned: $consumed")
                    // Refresh candidate view after each typed key so candidates stay current.
                    updateCandidates()
                } else {
                    // Fallback for label-only paths: resolve via hint map
                    val hinted = KeyMapping.getLibchewingKeyCode(key.label)
                    if (hinted > 0) {
                        chewing.handleKeyEvent(hinted)
                        updateCandidates()
                    } else {
                        ic.commitText(key.label, 1)
                    }
                }
            }
        }
    }

    private fun updateCandidates() {
        val candidates = chewing.getCandidates()
        Log.d(TAG, "updateCandidates: ${candidates.size} candidates")
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

    companion object {
        private const val TAG = "ChewingIMEService"
    }
}
