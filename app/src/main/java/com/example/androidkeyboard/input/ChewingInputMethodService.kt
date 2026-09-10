package com.example.androidkeyboard.input

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
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

    private val sessionController = ImeSessionController()
    private var activeSession = sessionController.current()
    private var activeLayout = ChewingEngine.Layout.DACHEN
    private var asciiShifted = false

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
            enabled = isReady && config.conversionEnabled
        }
    }

    override fun onDestroy() {
        chewing.close()
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        keyboardView = KeyboardView(this).apply {
            setLayout(rowsForActiveSession())
            setHaptic(config.hapticEnabled)
            setProximityTolerance(config.proximityTolerance)
            onKeyPress = ::handleKey
        }

        candidateView = CandidateView(this).apply {
            setCandidates(emptyList())
            onItemClick = ::commitCandidate
            onPrevPage = {
                if (activeSession.allowCandidates) {
                    chewing.prevPageUpdate()?.let(::applyEngineUpdate)
                }
            }
            onNextPage = {
                if (activeSession.allowCandidates) {
                    chewing.nextPageUpdate()?.let(::applyEngineUpdate)
                }
            }
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

        activeSession = sessionController.begin(
            EditorPolicy.from(attribute.inputType, attribute.imeOptions)
        )
        asciiShifted = false

        val desiredLayout = config.layout
        if (!chewing.isReady || desiredLayout != activeLayout) {
            chewing.init(desiredLayout)
            activeLayout = desiredLayout
        } else {
            chewing.reset()
        }
        if (chewing.isReady) {
            chewing.setPersonalizedLearningEnabled(activeSession.personalizedLearningEnabled)
        }

        converter.init(config.s2tProfile, config.t2sProfile)
        converter.enabled = converter.isReady && config.conversionEnabled

        if (::keyboardView.isInitialized) refreshKeyboardForSession()
        if (::candidateView.isInitialized) candidateView.setCandidates(emptyList())
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (::keyboardView.isInitialized) {
            keyboardView.setHaptic(config.hapticEnabled)
            keyboardView.setProximityTolerance(config.proximityTolerance)
            refreshKeyboardForSession()
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

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int,
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd,
        )

        if (
            sessionController.shouldResetComposition(
                newSelStart = newSelStart,
                newSelEnd = newSelEnd,
                candidatesStart = candidatesStart,
                candidatesEnd = candidatesEnd,
            )
        ) {
            currentInputConnection?.finishComposingText()
            chewing.reset()
            if (::candidateView.isInitialized) candidateView.setCandidates(emptyList())
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        asciiShifted = false
        if (::symbolPicker.isInitialized) symbolPicker.visibility = View.GONE
        if (::candidateView.isInitialized) candidateView.setCandidates(emptyList())
        super.onFinishInputView(finishingInput)
    }

    override fun onFinishInput() {
        currentInputConnection?.finishComposingText()
        chewing.reset()
        asciiShifted = false
        if (::candidateView.isInitialized) candidateView.setCandidates(emptyList())
        super.onFinishInput()
    }

    private fun handleKey(key: KeyDef) {
        val inputConnection = currentInputConnection ?: return

        when (key.action) {
            KeyAction.BACKSPACE -> handleBackspace(inputConnection)
            KeyAction.SPACE -> handleSpace(inputConnection, key)
            KeyAction.ENTER -> handleEnter(inputConnection)
            KeyAction.SHIFT -> toggleAsciiShift()
            KeyAction.DISMISS -> {
                inputConnection.finishComposingText()
                requestHideSelf(0)
            }
            KeyAction.INPUT -> handleInput(inputConnection, key)
        }
    }

    private fun handleBackspace(inputConnection: InputConnection) {
        if (!activeSession.allowComposition) {
            inputConnection.finishComposingText()
            inputConnection.deleteSurroundingText(1, 0)
            clearCandidates()
            return
        }

        val update = chewing.backspaceUpdate()
        if (update.consumed) {
            applyEngineUpdate(update, inputConnection)
        } else {
            inputConnection.finishComposingText()
            inputConnection.deleteSurroundingText(1, 0)
            clearCandidates()
        }
    }

    private fun handleSpace(inputConnection: InputConnection, key: KeyDef) {
        if (!activeSession.allowComposition) {
            inputConnection.finishComposingText()
            inputConnection.commitText(" ", 1)
            clearCandidates()
            return
        }

        val update = chewing.handleKeyUpdate(key.code)
        if (update.consumed || update.preedit.isNotEmpty() || update.committedText.isNotEmpty()) {
            applyEngineUpdate(update, inputConnection)
        } else {
            inputConnection.finishComposingText()
            inputConnection.commitText(" ", 1)
            clearCandidates()
        }
    }

    private fun handleEnter(inputConnection: InputConnection) {
        if (activeSession.allowComposition && chewing.isReady) {
            val update = chewing.commitUpdate()
            if (
                update.consumed ||
                update.preedit.isNotEmpty() ||
                update.committedText.isNotEmpty() ||
                update.candidates.isNotEmpty()
            ) {
                applyEngineUpdate(update, inputConnection)
            } else {
                inputConnection.finishComposingText()
            }
        } else {
            inputConnection.finishComposingText()
        }

        val imeAction = when (activeSession.editorAction) {
            EditorAction.GO -> EditorInfo.IME_ACTION_GO
            EditorAction.SEARCH -> EditorInfo.IME_ACTION_SEARCH
            EditorAction.SEND -> EditorInfo.IME_ACTION_SEND
            EditorAction.NEXT -> EditorInfo.IME_ACTION_NEXT
            EditorAction.DONE -> EditorInfo.IME_ACTION_DONE
            EditorAction.PREVIOUS -> EditorInfo.IME_ACTION_PREVIOUS
            EditorAction.NONE,
            EditorAction.UNSPECIFIED -> null
        }

        if (imeAction != null) {
            inputConnection.performEditorAction(imeAction)
        } else {
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
        clearCandidates()
    }

    private fun handleInput(inputConnection: InputConnection, key: KeyDef) {
        if (!activeSession.allowComposition) {
            inputConnection.finishComposingText()
            inputConnection.commitText(key.label, 1)
            clearCandidates()

            if (asciiShifted && key.label.singleOrNull()?.isLetter() == true) {
                asciiShifted = false
                refreshKeyboardForSession()
            }
            return
        }

        val libchewingKey = key.code.takeIf { it > 0 }
            ?: KeyMapping.getLibchewingKeyCode(key.label)

        if (libchewingKey > 0 && chewing.isReady) {
            applyEngineUpdate(chewing.handleKeyUpdate(libchewingKey), inputConnection)
        } else {
            inputConnection.finishComposingText()
            inputConnection.commitText(key.label, 1)
            clearCandidates()
        }
    }

    private fun toggleAsciiShift() {
        if (activeSession.keyboard != SessionKeyboard.ASCII) return
        asciiShifted = !asciiShifted
        refreshKeyboardForSession()
    }

    private fun rowsForActiveSession(): List<KeyboardRow> = when (activeSession.keyboard) {
        SessionKeyboard.DACHEN -> KeyboardLayout.Dachen.rows
        SessionKeyboard.ASCII -> KeyboardLayout.asciiRows(asciiShifted)
    }

    private fun refreshKeyboardForSession() {
        if (!::keyboardView.isInitialized) return
        keyboardView.setLayout(rowsForActiveSession())
        keyboardView.refreshLayout()
    }

    private fun applyEngineUpdate(
        update: EngineUpdate,
        inputConnection: InputConnection? = currentInputConnection,
    ) {
        val editor = inputConnection ?: return

        if (update.committedText.isNotEmpty()) {
            editor.commitText(convertForOutput(update.committedText), 1)
        }

        if (activeSession.allowComposition && update.preedit.isNotEmpty()) {
            editor.setComposingText(update.preedit, 1)
        } else {
            editor.finishComposingText()
        }

        if (::candidateView.isInitialized) {
            candidateView.setCandidates(
                if (activeSession.allowCandidates) update.candidates else emptyList()
            )
        }
    }

    private fun commitCandidate(index: Int, candidate: String) {
        @Suppress("UNUSED_VARIABLE")
        val renderedCandidate = candidate
        if (!activeSession.allowCandidates) return
        applyEngineUpdate(chewing.selectCandidateUpdate(index))
    }

    private fun commitSymbol(symbol: String) {
        currentInputConnection?.let { inputConnection ->
            inputConnection.finishComposingText()
            inputConnection.commitText(symbol, 1)
        }
        clearCandidates()
    }

    private fun clearCandidates() {
        if (::candidateView.isInitialized) candidateView.setCandidates(emptyList())
    }

    private fun convertForOutput(text: String): String = if (converter.isReady && converter.enabled) {
        converter.simplifyToTraditional(text)
    } else {
        text
    }
}
