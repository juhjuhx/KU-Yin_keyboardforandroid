package com.example.ime

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.text.Spanned
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.MainActivity
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.android.LibChewingDataInstaller
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.input.EditorEnterPolicy
import com.example.androidkeyboard.input.EditorPolicy
import com.example.ime.engine.KeyboardMode
import com.example.ime.engine.KuYinEngine
import com.example.ime.engine.EnglishLexicon
import com.example.ime.service.EditorEffectTracker
import com.example.ime.service.CompositionResetPolicy
import com.example.ime.service.EffectiveModePolicy
import com.example.ime.service.NativeChewingEngineOwner
import com.example.ime.service.NativeLearningPolicy
import com.example.ime.service.NativeStartupGate
import com.example.ime.session.ComposeDecoderSession
import com.example.ime.session.ImeCommand
import com.example.ime.session.EnglishWordSession
import com.example.ime.session.ProductionDecoderResolver
import com.example.ime.session.ZhuyinDictionarySession
import com.example.ime.service.EditorDeletion
import com.example.ime.service.ImeLifecycleOwner
import com.example.ime.settings.KeyboardSettings
import com.example.ime.ui.KuYinKeyboardUi
import com.example.ime.util.FeedbackHelper

class KuYinInputMethodService : InputMethodService() {

    private lateinit var lifecycleOwner: ImeLifecycleOwner
    private lateinit var engine: KuYinEngine
    private lateinit var session: ComposeDecoderSession
    private lateinit var settings: KeyboardSettings
    private lateinit var feedbackHelper: FeedbackHelper
    private var inputViewAccepting = false
    private var userPreferredMode: KeyboardMode = KeyboardMode.ZHUYIN
    private var nativeOwner: NativeChewingEngineOwner? = null
    private var nativeEngine: ChewingEngine? = null
    private val englishSession by lazy {
        EnglishWordSession(assets.open(EnglishLexicon.ASSET_PATH).bufferedReader().use(EnglishLexicon::fromTsv))
    }
    private val editorEffects = EditorEffectTracker()
    private val editorWriteFailure = mutableStateOf<String?>(null)
    private var failedTextIsSensitive = false

    private val currentEnterPolicy = mutableStateOf(EditorEnterPolicy.from(EditorInfo.IME_ACTION_UNSPECIFIED))
    private val currentEditorPolicy = mutableStateOf(EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT, EditorInfo.IME_ACTION_UNSPECIFIED))
    private var composeView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner = ImeLifecycleOwner()
        lifecycleOwner.onCreate()

        settings = KeyboardSettings(this)
        engine = KuYinEngine(this)
        var nativePaths: LibChewingDataInstaller.Paths? = null
        nativeEngine = NativeStartupGate.start(
            install = { nativePaths = LibChewingDataInstaller.ensureInstalled(this) },
            openEngine = {
                val owner = NativeChewingEngineOwner {
                    val paths = nativePaths ?: error("libchewing paths missing")
                    AndroidChewingEngine(
                        paths.systemDir.absolutePath,
                        paths.userFile.absolutePath,
                    )
                }
                nativeOwner = owner
                owner.ensureStarted()
            },
        )
        session = ProductionDecoderResolver.resolve(nativeEngine, engine)
        feedbackHelper = FeedbackHelper(this, settings)
    }

    /**
     * 強制回傳 true，防止 Android 系統在偵測到模擬器或實體電腦鍵盤時自動隱藏軟體虛擬鍵盤。
     */
    override fun onEvaluateInputViewShown(): Boolean {
        // Honor the platform callback contract while retaining KU-Yin's
        // existing always-show policy, including with a hardware keyboard.
        super.onEvaluateInputViewShown()
        return true
    }

    /**
     * 強制回傳 true，確保在模擬器及真機環境中皆能順暢響應輸入框焦點並彈出鍵盤。
     */
    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        return true
    }

    override fun onCreateInputView(): View {
        // 同步將生命週期綁定至 Window DecorView，以支援 Compose 內部視窗測量與對話框
        window?.window?.decorView?.let { decor ->
            lifecycleOwner.attachToView(decor)
        }

        val view = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner))
            lifecycleOwner.attachToView(this)
            setContent {
                KuYinKeyboardUi(
                    engine = engine,
                    session = session,
                    englishSession = englishSession,
                    settings = settings,
                    actionLabel = currentEnterPolicy.value.label,
                    isActionPresentation = currentEnterPolicy.value.isActionPresentation,
                    onCommitText = ::commitToEditor,
                    onComposingText = ::syncEditorComposition,
                    editorWriteFailure = editorWriteFailure.value?.let {
                        if (failedTextIsSensitive || currentEditorPolicy.value.isSensitive)
                            "敏感欄位的未確認文字已遮蔽，請自行核對。" else it
                    },
                    onDismissEditorWriteFailure = ::discardUnconfirmedText,
                    shouldAcceptInput = { inputViewAccepting && editorWriteFailure.value == null },
                    onDeleteSurroundingText = {
                        performEditorBackspace()
                    },
                    onPerformEditorAction = {
                        handleEditorAction()
                    },
                    onHideKeyboard = {
                        inputViewAccepting = false
                        session.dispatch(ImeCommand.CloseExpandedCandidates)
                        requestHideSelf(0)
                    },
                    onFeedback = {
                        feedbackHelper.performKeyPressFeedback(this)
                    },
                    onOpenFeedback = {
                        feedbackHelper.performKeyPressFeedback(this)
                        val intent = Intent(this@KuYinInputMethodService, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra("EXTRA_OPEN_FEEDBACK", true)
                        }
                        startActivity(intent)
                    },
                    onUserModeSelected = { userPreferredMode = it }
                )
            }
        }
        composeView = view
        return view
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        inputViewAccepting = false
        finishOwnedComposition()
        editorEffects.startEditor(currentInputConnection,
            attribute?.initialSelStart ?: -1, attribute?.initialSelEnd ?: -1)
        updateEnterPolicy(attribute)
        val policy = EditorPolicy.from(
            attribute?.inputType ?: InputType.TYPE_CLASS_TEXT,
            attribute?.imeOptions ?: EditorInfo.IME_ACTION_UNSPECIFIED
        )
        currentEditorPolicy.value = policy
        engine.applyPolicy(policy)
        englishSession.configureSuggestions(policy.allowEnglishSuggestions)
        nativeEngine?.setPersonalizedLearningEnabled(
            NativeLearningPolicy.shouldEnableLearning(policy),
        )
        session.dispatch(ImeCommand.Reset)
        engine.setMode(EffectiveModePolicy.effectiveMode(userPreferredMode, policy))
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        inputViewAccepting = true
        window?.window?.decorView?.let { decor ->
            lifecycleOwner.attachToView(decor)
        }
        lifecycleOwner.onStart()

        updateEnterPolicy(info)

        finishOwnedComposition()
        session.dispatch(ImeCommand.Reset)
        englishSession.reset()
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )
        if (editorEffects.consumeOwnSelection(currentInputConnection,
                EditorEffectTracker.Selection(newSelStart, newSelEnd, candidatesStart, candidatesEnd))) return
        val composingRangeChanged = editorEffects.compositionWasReplaced(currentInputConnection,
            EditorEffectTracker.Selection(newSelStart, newSelEnd, candidatesStart, candidatesEnd))
        if (composingRangeChanged || CompositionResetPolicy.shouldReset(
                hasComposing = session.state.value.preedit.isNotEmpty() || englishSession.state.value.wordBuffer.isNotEmpty(),
                oldSelStart = oldSelStart,
                oldSelEnd = oldSelEnd,
                newSelStart = newSelStart,
                newSelEnd = newSelEnd
            )
        ) {
            finishOwnedComposition()
            session.dispatch(ImeCommand.Reset)
            englishSession.reset()
        }
        editorEffects.observeExternalSelection(newSelStart, newSelEnd)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        inputViewAccepting = false
        finishOwnedComposition()
        super.onFinishInputView(finishingInput)
        session.dispatch(ImeCommand.Reset)
        englishSession.reset()
        lifecycleOwner.onStop()
    }

    override fun onFinishInput() {
        inputViewAccepting = false
        finishOwnedComposition()
        session.dispatch(ImeCommand.Reset)
        englishSession.reset()
        editorEffects.startEditor(null, -1, -1)
        super.onFinishInput()
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeOwner?.close()
        nativeOwner = null
        nativeEngine = null
        editorWriteFailure.value = null
        failedTextIsSensitive = false
        lifecycleOwner.onDestroy()
    }

    private fun performEditorBackspace() {
        if (!inputViewAccepting) return
        if (editorWriteFailure.value != null) return
        val ic = currentInputConnection ?: return
        val hasSelection = !ic.getSelectedText(0).isNullOrEmpty()
        when (EditorDeletion.plan(hasSelection)) {
            EditorDeletion.Plan.DeleteSelection -> commitToEditor("")
            EditorDeletion.Plan.DeleteCodePointBeforeCursor ->
                ic.deleteSurroundingTextInCodePoints(1, 0)
        }
    }

    private fun updateEnterPolicy(info: EditorInfo?) {
        currentEnterPolicy.value = EditorEnterPolicy.from(
            imeOptions = info?.imeOptions ?: EditorInfo.IME_ACTION_UNSPECIFIED,
            actionLabel = info?.actionLabel?.toString(),
            actionId = info?.actionId ?: 0,
        )
    }

    private fun handleEditorAction() {
        if (!inputViewAccepting) return
        if (editorWriteFailure.value != null) return
        val ic = currentInputConnection ?: return
        when (val behavior = currentEnterPolicy.value.behavior) {
            EditorEnterPolicy.Behavior.InsertNewline -> commitToEditor("\n")
            is EditorEnterPolicy.Behavior.PerformStandardAction -> ic.performEditorAction(behavior.actionCode)
            is EditorEnterPolicy.Behavior.PerformCustomAction -> ic.performEditorAction(behavior.actionId)
        }
    }

    /**
     * A false commit ACK may arrive after the editor already mutated. Preserve the text and
     * ordered tail, block further input, and never replay it automatically.
     * https://developer.android.com/reference/android/view/inputmethod/InputConnection#commitText(java.lang.CharSequence,int)
     */
    private fun commitToEditor(text: String) {
        if (editorWriteFailure.value != null) {
            retainUnconfirmedText(text)
            return
        }
        val ic = currentInputConnection
        val before = ic?.let(::readEditorSelection)
        if (ic == null || !ic.commitText(text, 1)) {
            retainUnconfirmedText(text)
            return
        }
        editorEffects.recordWrite(ic, text.length, false, before, readEditorSelection(ic))
    }

    private fun retainUnconfirmedText(text: String) {
        if (editorWriteFailure.value == null) failedTextIsSensitive = currentEditorPolicy.value.isSensitive
        editorWriteFailure.value = editorWriteFailure.value.orEmpty() + text
    }

    /** Composing replaces the owned range; a commit already replaces/finishes it once. */
    private fun syncEditorComposition(preedit: String) {
        if (editorWriteFailure.value != null) return
        val ic = currentInputConnection ?: run {
            if (preedit.isNotEmpty()) retainUnconfirmedText(preedit)
            return
        }
        val before = readEditorSelection(ic)
        if (preedit.isEmpty() && !editorEffects.ownsComposition(ic)) return
        if (preedit.isEmpty() && !editorEffects.ownsLiveRange(ic, before)) {
            finishOwnedComposition()
            return
        }
        if (!ic.setComposingText(preedit, 1)) {
            retainUnconfirmedText(preedit)
            return
        }
        editorEffects.recordWrite(ic, preedit.length, preedit.isNotEmpty(), before, readEditorSelection(ic))
        if (preedit.isEmpty()) ic.finishComposingText()
    }

    private fun finishOwnedComposition() {
        val ic = currentInputConnection
        if (editorWriteFailure.value == null && editorEffects.ownsComposition(ic)) ic?.finishComposingText()
        editorEffects.finishComposition()
    }

    /** Explicit UI discard permits cancellation; this does not send the failed commit again. */
    private fun discardUnconfirmedText() {
        editorWriteFailure.value = null
        failedTextIsSensitive = false
        session.dispatch(ImeCommand.Reset)
        englishSession.reset()
        syncEditorComposition("")
        editorEffects.finishComposition()
    }

    /** Only offsets survive this read; sensitive/restricted editor text is never extracted. */
    private fun readEditorSelection(ic: InputConnection): EditorEffectTracker.Selection? {
        val policy = currentEditorPolicy.value
        if (policy.isSensitive || !policy.allowComposition || policy.forceAscii) return null
        val request = ExtractedTextRequest().apply {
            flags = InputConnection.GET_TEXT_WITH_STYLES
            hintMaxChars = 256
            hintMaxLines = 2
        }
        val extracted = ic.getExtractedText(request, 0) ?: return null
        if (extracted.selectionStart < 0 || extracted.selectionEnd < 0) return null
        var composingStart = -1
        var composingEnd = -1
        val styled = extracted.text as? Spanned
        styled?.getSpans(0, styled.length, Any::class.java)?.forEach { span ->
            if (styled.getSpanFlags(span) and Spanned.SPAN_COMPOSING != 0) {
                val start = styled.getSpanStart(span) + extracted.startOffset
                val end = styled.getSpanEnd(span) + extracted.startOffset
                composingStart = if (composingStart < 0) start else minOf(composingStart, start)
                composingEnd = maxOf(composingEnd, end)
            }
        }
        return EditorEffectTracker.Selection(extracted.startOffset + extracted.selectionStart,
            extracted.startOffset + extracted.selectionEnd, composingStart, composingEnd)
    }
}
