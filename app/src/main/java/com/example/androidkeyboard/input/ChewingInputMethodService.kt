package com.example.androidkeyboard.input

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.android.LibChewingDataInstaller
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.androidkeyboard.engines.core.IMEConfig
import com.example.androidkeyboard.engines.opencc.OpenCCConverter
import com.example.androidkeyboard.ui.CandidateView
import com.example.androidkeyboard.ui.ImePalette

class ChewingInputMethodService : InputMethodService() {

    private lateinit var keyboardView: KeyboardView
    private lateinit var candidateView: CandidateView
    private lateinit var symbolPicker: SymbolPicker
    private lateinit var chewing: ChewingEngine
    private lateinit var converter: OpenCCConverter
    private lateinit var config: IMEConfig
    private var activeLayout = ChewingEngine.Layout.DACHEN

    override fun onCreate() {
        super.onCreate()
        config = IMEConfig(this)

        val nativePaths = LibChewingDataInstaller.ensureInstalled(this)
        activeLayout = config.layout
        chewing = AndroidChewingEngine(
            systemDataPath = nativePaths.systemDir.absolutePath,
            userDataPath = nativePaths.userFile.absolutePath,
        ).apply { init(activeLayout) }

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
            onPrevPage = { chewing.prevPageUpdate()?.let(::applyEngineUpdate) }
            onNextPage = { chewing.nextPageUpdate()?.let(::applyEngineUpdate) }
        }

        symbolPicker = SymbolPicker(this).apply {
            visibility = View.GONE
            onSymbolSelect = ::commitSymbol
            onClose = { symbolPicker.visibility = View.GONE }
        }

        val candidateHeight = (44f * resources.displayMetrics.density).toInt()
        val palette = ImePalette.from(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(palette.surface)
            addView(
                candidateView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    candidateHeight,
                ),
            )
            addView(
                keyboardView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ),
            )
        }

        return FrameLayout(this).apply {
            setBackgroundColor(palette.surface)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            )
            addView(
                content,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                ),
            )
            addView(
                symbolPicker,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    candidateHeight,
                ),
            )
        }
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        val desiredLayout = config.layout
        if (!chewing.isReady || desiredLayout != activeLayout) {
            chewing.init(desiredLayout)
            activeLayout = desiredLayout
        }
        converter.enabled = config.conversionEnabled
        converter.init(config.s2tProfile, config.t2sProfile)
        if (::candidateView.isInitialized) candidateView.setCandidates(emptyList())
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (::keyboardView.isInitialized) {
            keyboardView.setHaptic(config.hapticEnabled)
            keyboardView.setProximityTolerance(config.proximityTolerance)
            keyboardView.refreshLayout()
        }
        if (::candidateView.isInitialized) {
            candidateView.refreshAppearance()
            candidateView.setCandidates(emptyList())
        }
        if (::symbolPicker.isInitialized) {
            symbolPicker.refreshAppearance()
            symbolPicker.visibility = View.GONE
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        if (::symbolPicker.isInitialized) symbolPicker.visibility = View.GONE
        if (::candidateView.isInitialized) candidateView.setCandidates(emptyList())
        super.onFinishInputView(finishingInput)
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
        inputConnection: InputConnection? = currentInputConnection,
    ) {
        val editor = inputConnection ?: return

        if (update.committedText.isNotEmpty()) {
            editor.commitText(convertForOutput(update.committedText), 1)
        }

        if (update.preedit.isNotEmpty()) {
            editor.setComposingText(update.preedit, 1)
        } else {
            editor.finishComposingText()
        }

        if (::candidateView.isInitialized) candidateView.setCandidates(update.candidates)
    }

    private fun commitCandidate(index: Int, candidate: String) {
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
