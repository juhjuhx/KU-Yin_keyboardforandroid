package io.github.juhjuhx.kuyin.acceptancehost

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.widget.EditText

/** 真 EditText；只控制宣告 metadata 與消費 action，文字仍由平台實作。 */
class RecordingEditText(
    context: Context,
    private val config: CaseConfig,
    private val recorder: EditorRecorder,
) : EditText(context) {
    private var layoutTrace: FocusLayoutTrace? = null
    fun layoutTraceSnapshot() = layoutTrace?.snapshot() ?: org.json.JSONObject()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        layoutTrace?.attach()
    }

    override fun onDetachedFromWindow() {
        layoutTrace?.detach()
        super.onDetachedFromWindow()
    }

    data class FocusLoss(val elapsedNanos: Long, val frames: List<String>)
    var firstFocusLoss: FocusLoss? = null
        private set
    var lastFocusLoss: FocusLoss? = null
        private set
    var focusLossCount: Int = 0
        private set

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (!focused) {
            layoutTrace?.loss()
            // TEST_ONLY synthetic editor. Stack symbols only: no Throwable message/editor text.
            val loss = FocusLoss(android.os.SystemClock.elapsedRealtimeNanos(),
                Throwable().stackTrace.take(24).map { "${it.className}#${it.methodName}:${it.lineNumber}" })
            if (firstFocusLoss == null) firstFocusLoss = loss
            lastFocusLoss = loss
            focusLossCount += 1
        }
    }

    init {
        layoutTrace = FocusLayoutTrace(this)
        id = R.id.editor
        inputType = config.inputType
        imeOptions = config.imeOptions
        setImeActionLabel(config.customActionLabel, config.customActionId)
        minLines = config.minLines
        setText(config.initialText)
        setSelection(text.length)
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { recorder.recordText(s?.toString().orEmpty()) }
        })
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val original = super.onCreateInputConnection(outAttrs)
        val platform = outAttrs.snapshot(original != null)
        // TextView 可正規化 action/flags；此處是真正送到 IMF 的受控 editor 宣告。
        outAttrs.inputType = config.inputType
        outAttrs.imeOptions = config.imeOptions
        outAttrs.actionLabel = config.customActionLabel
        outAttrs.actionId = config.customActionId
        recorder.recordConnection(platform, outAttrs.snapshot(original != null))
        if (original == null) return null
        return object : InputConnectionWrapper(original, false) {
            override fun performEditorAction(actionCode: Int): Boolean = recorder.recordAction(actionCode)

            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                val result = super.commitText(text, newCursorPosition)
                recorder.recordCommit(text?.toString().orEmpty())
                return result
            }
        }
    }

    private fun EditorInfo.snapshot(present: Boolean) = EditorInfoSnapshot(
        inputType, imeOptions, actionLabel?.toString(), actionId, present)
}
