package com.example.ime

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.MainActivity
import com.example.ime.engine.KuYinEngine
import com.example.ime.service.ImeLifecycleOwner
import com.example.ime.settings.KeyboardSettings
import com.example.ime.ui.KuYinKeyboardUi
import com.example.ime.util.FeedbackHelper

class KuYinInputMethodService : InputMethodService() {

    private lateinit var lifecycleOwner: ImeLifecycleOwner
    private lateinit var engine: KuYinEngine
    private lateinit var settings: KeyboardSettings
    private lateinit var feedbackHelper: FeedbackHelper

    private val currentActionLabel = mutableStateOf("換行")
    private var composeView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner = ImeLifecycleOwner()
        lifecycleOwner.onCreate()

        settings = KeyboardSettings(this)
        engine = KuYinEngine(this)
        feedbackHelper = FeedbackHelper(this, settings)
    }

    /**
     * 強制回傳 true，防止 Android 系統在偵測到模擬器或實體電腦鍵盤時自動隱藏軟體虛擬鍵盤。
     */
    override fun onEvaluateInputViewShown(): Boolean {
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
                    settings = settings,
                    actionLabel = currentActionLabel.value,
                    onCommitText = { text ->
                        currentInputConnection?.commitText(text, 1)
                    },
                    onDeleteSurroundingText = {
                        currentInputConnection?.deleteSurroundingText(1, 0)
                    },
                    onPerformEditorAction = {
                        handleEditorAction()
                    },
                    onHideKeyboard = {
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
                    }
                )
            }
        }
        composeView = view
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        window?.window?.decorView?.let { decor ->
            lifecycleOwner.attachToView(decor)
        }
        lifecycleOwner.onStart()

        // 判定 Enter 鍵動作文字
        currentActionLabel.value = when (info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)) {
            EditorInfo.IME_ACTION_GO -> "前往"
            EditorInfo.IME_ACTION_SEARCH -> "搜尋"
            EditorInfo.IME_ACTION_SEND -> "傳送"
            EditorInfo.IME_ACTION_NEXT -> "下一步"
            EditorInfo.IME_ACTION_DONE -> "完成"
            else -> "換行"
        }

        engine.clearComposing()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        engine.clearComposing()
        lifecycleOwner.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner.onDestroy()
    }

    private fun handleEditorAction() {
        val ic = currentInputConnection ?: return
        val currentInfo = currentInputEditorInfo
        val action = currentInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        if (action != null && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action)
        } else {
            // 預設換行
            ic.commitText("\n", 1)
        }
    }
}
