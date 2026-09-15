package com.example.androidkeyboard.input

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.preference.PreferenceManager
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.android.LibChewingDataInstaller
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.androidkeyboard.engines.core.IMEConfig
import com.example.androidkeyboard.engines.opencc.OpenCCConverter
import com.example.androidkeyboard.ui.CandidateState
import com.example.androidkeyboard.ui.CandidateView
import com.example.androidkeyboard.ui.ImePalette
import com.example.androidkeyboard.ui.candidateContainerHeightPx
import com.example.androidkeyboard.ui.candidateStateOf

class ChewingInputMethodService : InputMethodService() {

    private lateinit var keyboardView: KeyboardView
    private lateinit var candidateView: CandidateView
    private lateinit var symbolPicker: SymbolPicker
    private lateinit var chewing: ChewingEngine
    private lateinit var converter: OpenCCConverter
    private lateinit var config: IMEConfig
    private lateinit var keyboardPreferencesRepository: SharedPreferencesKeyboardPreferencesRepository

    private val sessionController = ImeSessionController()
    private val keyboardController = KeyboardController()
    private val shellLayoutResolver = KeyboardShellLayoutResolver()

    private var activeSession = sessionController.current()
    private var activeLayout = ChewingEngine.Layout.DACHEN
    private var keyboardPreferences = KeyboardPreferences()
    private var runtimeState = KeyboardRuntimeState.defaultZhuyin()
    private var rememberedMode: InputMode? = null
    private var hasActiveComposition = false
    private var lastCandidateState = CandidateState(
        items = emptyList(),
        canPageBackward = false,
        canPageForward = false,
        expanded = false,
    )

    override fun onCreate() {
        super.onCreate()
        config = IMEConfig(this)
        keyboardPreferencesRepository = SharedPreferencesKeyboardPreferencesRepository(
            PreferenceManager.getDefaultSharedPreferences(applicationContext),
        )
        keyboardPreferences = keyboardPreferencesRepository.load()
        runtimeState = createRuntimeState(activeSession)

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
            setResolvedLayout(shellLayoutResolver.resolve(runtimeState, keyboardPreferences))
            setHaptic(keyboardPreferences.hapticEnabled)
            setProximityTolerance(keyboardPreferences.proximityTolerance)
            onCommand = ::dispatchCommand
        }

        candidateView = CandidateView(this).apply {
            renderCandidateState(
                CandidateState(
                    items = emptyList(),
                    canPageBackward = false,
                    canPageForward = false,
                    expanded = runtimeState.candidateExpanded,
                ),
            )
            visibility = View.GONE
            onItemClick = { index, _ -> dispatchCommand(ImeCommand.SelectCandidate(index)) }
            onToggleExpand = { dispatchCommand(ImeCommand.ToggleCandidateExpanded) }
            onRequiredRowsChanged = { rows -> applyCandidateGeometry(lastCandidateState, rows) }
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

        // Retained as a compatibility surface while the v0.2 shell absorbs the old picker.
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
            EditorPolicy.from(attribute.inputType, attribute.imeOptions),
        )
        keyboardPreferences = keyboardPreferencesRepository.load()
        runtimeState = createRuntimeState(activeSession)
        hasActiveComposition = false

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

        if (::keyboardView.isInitialized) refreshKeyboardSurface()
        clearCandidates()
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        keyboardPreferences = keyboardPreferencesRepository.load()
        if (::keyboardView.isInitialized) {
            keyboardView.setHaptic(keyboardPreferences.hapticEnabled)
            keyboardView.setProximityTolerance(keyboardPreferences.proximityTolerance)
            refreshKeyboardSurface()
        }
        if (::candidateView.isInitialized) {
            candidateView.refreshAppearance()
            clearCandidates()
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
            hasActiveComposition = false
            clearCandidates()
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        runtimeState = runtimeState.copy(
            page = KeyboardPage.LETTERS,
            shifted = false,
            candidateExpanded = false,
        )
        hasActiveComposition = false
        if (::symbolPicker.isInitialized) symbolPicker.visibility = View.GONE
        clearCandidates()
        super.onFinishInputView(finishingInput)
    }

    override fun onFinishInput() {
        currentInputConnection?.finishComposingText()
        chewing.reset()
        runtimeState = runtimeState.copy(
            page = KeyboardPage.LETTERS,
            shifted = false,
            candidateExpanded = false,
        )
        hasActiveComposition = false
        clearCandidates()
        super.onFinishInput()
    }

    private fun dispatchCommand(command: ImeCommand) {
        val previousState = runtimeState
        val result = keyboardController.reduce(
            state = runtimeState,
            command = command,
            context = ControllerContext(
                hasActiveComposition = hasActiveComposition,
                allowComposition = activeSession.allowComposition,
            ),
        )
        runtimeState = result.state

        if (
            keyboardPreferences.rememberLastMode &&
            previousState.inputMode != runtimeState.inputMode
        ) {
            rememberedMode = runtimeState.inputMode
        }

        result.effects.forEach(::executeEffect)

        if (runtimeState != previousState) {
            refreshKeyboardSurface()
            renderCandidateState(lastCandidateState.copy(expanded = runtimeState.candidateExpanded))
        }
    }

    private fun executeEffect(effect: ImeEffect) {
        when (effect) {
            is ImeEffect.SendChewingKey -> executeChewingKey(effect.code)
            ImeEffect.BackspaceChewing -> executeChewingBackspace()
            ImeEffect.DeleteBackward -> deleteBackward()
            ImeEffect.CommitComposition -> commitComposition()
            is ImeEffect.CommitText -> commitLiteralText(effect.text)
            ImeEffect.PerformEditorAction -> performEditorAction()
            ImeEffect.HideKeyboard -> hideKeyboard()
            ImeEffect.ShowNextInputMethod -> showNextInputMethod()
            is ImeEffect.SelectCandidate -> selectCandidate(effect.index)
        }
    }

    private fun executeChewingKey(code: Int) {
        val inputConnection = currentInputConnection ?: return
        if (activeSession.allowComposition && chewing.isReady) {
            val update = chewing.handleKeyUpdate(code)
            if (update.hasEngineState()) {
                applyEngineUpdate(update, inputConnection)
                return
            }
        }

        inputConnection.finishComposingText()
        val fallback = if (code == ' '.code) {
            " "
        } else {
            dachenLabelForCode(code)
        }
        if (!fallback.isNullOrEmpty()) {
            inputConnection.commitText(fallback, 1)
        }
        chewing.reset()
        hasActiveComposition = false
        clearCandidates()
    }

    private fun executeChewingBackspace() {
        val inputConnection = currentInputConnection ?: return
        if (activeSession.allowComposition && chewing.isReady) {
            val update = chewing.backspaceUpdate()
            if (update.hasEngineState()) {
                applyEngineUpdate(update, inputConnection)
                return
            }
        }
        inputConnection.finishComposingText()
        inputConnection.deleteSurroundingText(1, 0)
        chewing.reset()
        hasActiveComposition = false
        clearCandidates()
    }

    private fun deleteBackward() {
        currentInputConnection?.let { inputConnection ->
            inputConnection.finishComposingText()
            inputConnection.deleteSurroundingText(1, 0)
        }
        chewing.reset()
        hasActiveComposition = false
        clearCandidates()
    }

    private fun commitComposition() {
        val inputConnection = currentInputConnection ?: return
        if (
            activeSession.allowComposition &&
            chewing.isReady &&
            hasActiveComposition
        ) {
            val update = chewing.commitUpdate()
            if (update.hasEngineState()) {
                applyEngineUpdate(update, inputConnection)
                if (!hasActiveComposition) clearCandidates()
                return
            }
        }
        inputConnection.finishComposingText()
        chewing.reset()
        hasActiveComposition = false
        clearCandidates()
    }

    private fun commitLiteralText(text: String) {
        currentInputConnection?.let { inputConnection ->
            inputConnection.finishComposingText()
            inputConnection.commitText(text, 1)
        }
        chewing.reset()
        hasActiveComposition = false
        clearCandidates()
    }

    private fun performEditorAction() {
        val inputConnection = currentInputConnection ?: return
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
        hasActiveComposition = false
        clearCandidates()
    }

    private fun hideKeyboard() {
        currentInputConnection?.finishComposingText()
        chewing.reset()
        hasActiveComposition = false
        clearCandidates()
        requestHideSelf(0)
    }

    private fun showNextInputMethod() {
        currentInputConnection?.finishComposingText()
        chewing.reset()
        hasActiveComposition = false
        clearCandidates()
        switchToNextInputMethod(false)
    }

    private fun createRuntimeState(session: ImeSession): KeyboardRuntimeState {
        val inputMode = if (!session.allowComposition) {
            InputMode.ENGLISH
        } else if (keyboardPreferences.rememberLastMode && rememberedMode != null) {
            rememberedMode!!
        } else {
            when (keyboardPreferences.defaultMode) {
                DefaultInputMode.ZHUYIN -> InputMode.ZHUYIN
                DefaultInputMode.ENGLISH -> InputMode.ENGLISH
            }
        }

        return KeyboardRuntimeState(
            inputMode = inputMode,
            page = KeyboardPage.LETTERS,
            shifted = false,
            candidateExpanded = keyboardPreferences.candidateExpandedByDefault,
        )
    }

    private fun refreshKeyboardSurface() {
        if (!::keyboardView.isInitialized) return
        keyboardView.setResolvedLayout(
            shellLayoutResolver.resolve(runtimeState, keyboardPreferences),
        )
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

        val visibleCandidates = if (activeSession.allowCandidates) {
            update.candidates
        } else {
            emptyList()
        }
        val canBackward = activeSession.allowCandidates && chewing.canPageCandidatesBackward()
        val canForward = activeSession.allowCandidates && chewing.canPageCandidatesForward()
        renderCandidateState(
            candidateStateOf(
                update.copy(candidates = visibleCandidates),
                expanded = runtimeState.candidateExpanded,
                canPageBackward = canBackward,
                canPageForward = canForward,
            ),
        )

        hasActiveComposition = activeSession.allowComposition &&
            (update.preedit.isNotEmpty() || visibleCandidates.isNotEmpty())
    }

    private fun renderCandidateState(state: CandidateState) {
        lastCandidateState = state
        if (!::candidateView.isInitialized) return
        candidateView.setCandidateState(state)
        applyCandidateGeometry(state, candidateView.contentRowCount())
    }

    private fun applyCandidateGeometry(state: CandidateState, rows: Int) {
        if (!::candidateView.isInitialized) return
        if (state.items.isEmpty()) {
            if (candidateView.visibility != View.GONE) candidateView.visibility = View.GONE
            return
        }
        if (candidateView.visibility != View.VISIBLE) candidateView.visibility = View.VISIBLE
        val rowH = (CandidateView.ROW_HEIGHT_DP * resources.displayMetrics.density).toInt()
        val newHeight = candidateContainerHeightPx(
            expanded = state.expanded,
            rows = rows,
            rowHeightPx = rowH,
            maxRows = CandidateView.MAX_EXPANDED_ROWS,
        )
        val lp = candidateView.layoutParams
        if (lp != null && lp.height != newHeight) {
            lp.height = newHeight
            candidateView.requestLayout()
        }
    }

    private fun selectCandidate(index: Int) {
        if (!activeSession.allowCandidates) return
        applyEngineUpdate(chewing.selectCandidateUpdate(index))
    }

    private fun commitSymbol(symbol: String) {
        dispatchCommand(ImeCommand.InsertText(symbol))
    }

    private fun clearCandidates() {
        renderCandidateState(
            lastCandidateState.copy(
                items = emptyList(),
                canPageBackward = false,
                canPageForward = false,
            ),
        )
    }

    private fun EngineUpdate.hasEngineState(): Boolean =
        consumed ||
            preedit.isNotEmpty() ||
            committedText.isNotEmpty() ||
            candidates.isNotEmpty()

    private fun dachenLabelForCode(code: Int): String? =
        KeyboardLayout.Dachen.rows
            .asSequence()
            .flatMap { it.keys.asSequence() }
            .firstOrNull { it.code == code }
            ?.label

    private fun convertForOutput(text: String): String = if (converter.isReady && converter.enabled) {
        converter.simplifyToTraditional(text)
    } else {
        text
    }
}
