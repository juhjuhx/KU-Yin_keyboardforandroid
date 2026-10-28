package io.github.juhjuhx.kuyin.acceptancehost

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** 獨立 test-only editor；不持有輸入法、decoder 或 expected policy。 */
class AcceptanceEditorActivity : Activity() {
    private lateinit var recorder: EditorRecorder
    private lateinit var editor: RecordingEditText
    private val diagnostics = mutableMapOf<Int, TextView>()
    private var imeVisibility = "UNAVAILABLE_API_24_29"
    val editorSnapshot: EditorSnapshot get() = recorder.snapshot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = IntentCaseConfig.from(intent)
        recorder = EditorRecorder(config) { runOnUiThread { render() } }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
        }
        root.addView(TextView(this).apply { text = "KU-Yin 受控驗收編輯器（固定測試文字）" })
        editor = RecordingEditText(this, config, recorder)
        root.addView(editor, LinearLayout.LayoutParams(-1, -2))
        val rows = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        fields.forEach { (id, label) ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.addView(TextView(this).apply { text = "$label: "; textSize = 11f })
            val value = TextView(this).apply { this.id = id; textSize = 11f }
            diagnostics[id] = value
            row.addView(value, LinearLayout.LayoutParams(0, -2, 1f))
            rows.addView(row)
        }
        root.addView(ScrollView(this).apply { addView(rows) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener { _: View, insets: WindowInsets ->
                imeVisibility = insets.isVisible(WindowInsets.Type.ime()).toString()
                render()
                insets
            }
        }
        render()
    }

    override fun onResume() {
        super.onResume()
        editor.post {
            if (!isFinishing) {
                editor.requestFocus()
                getSystemService(InputMethodManager::class.java).showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    /** TEST_ONLY read-only booleans; never dump editor text or request/change focus. */
    override fun dump(prefix: String, fd: java.io.FileDescriptor?, writer: java.io.PrintWriter,
                      args: Array<out String>?) {
        if (args?.contentEquals(arrayOf("--kuyin-focus")) != true) {
            super.dump(prefix, fd, writer, args)
            return
        }
        val node = editor.createAccessibilityNodeInfo()
        try {
            writer.println(prefix + "KUYIN_FOCUS_TEST_ONLY")
            writer.println(prefix + "editor_is_focused=${editor.isFocused}")
            writer.println(prefix + "editor_has_focus=${editor.hasFocus()}")
            writer.println(prefix + "window_has_focus=${hasWindowFocus()}")
            writer.println(prefix + "editor_window_has_focus=${editor.hasWindowFocus()}")
            writer.println(prefix + "editor_attached=${editor.isAttachedToWindow}")
            writer.println(prefix + "editor_focusable_in_touch_mode=${editor.isFocusableInTouchMode}")
            writer.println(prefix + "root_focus_is_editor=${window.decorView.findFocus() === editor}")
            writer.println(prefix + "node_input_focused=${node.isFocused}")
            writer.println(prefix + "node_accessibility_focused=${node.isAccessibilityFocused}")
            writer.println(prefix + "last_recorded_connection_present=${recorder.snapshot.finalMetadata?.inputConnectionPresent}")
        } finally {
            @Suppress("DEPRECATION")
            node.recycle()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        render()
    }

    @Suppress("DEPRECATION")
    private fun render() {
        val s = recorder.snapshot
        val final = s.finalMetadata
        val platform = s.platformMetadata
        val values = mapOf(
            R.id.case_id to s.caseId,
            R.id.actual_input_type to final?.inputType,
            R.id.actual_ime_options to final?.imeOptions,
            R.id.actual_action_label to final?.actionLabel,
            R.id.actual_action_id to final?.actionId,
            R.id.input_connection_present to final?.inputConnectionPresent,
            R.id.editor_action_count to s.editorActionCount,
            R.id.last_editor_action_id to s.lastEditorActionId,
            R.id.last_action_return_value to s.lastActionReturnValue,
            R.id.newline_count to s.newlineCount,
            R.id.current_text to s.currentText,
            R.id.commit_count to s.commitCount,
            R.id.last_commit_text to s.lastCommitText,
            R.id.snapshot_revision to s.revision,
            R.id.platform_input_type to platform?.inputType,
            R.id.platform_ime_options to platform?.imeOptions,
            R.id.platform_action_label to platform?.actionLabel,
            R.id.platform_action_id to platform?.actionId,
            R.id.ime_visibility to imeVisibility,
            R.id.host_orientation to resources.configuration.orientation,
            R.id.display_rotation to windowManager.defaultDisplay.rotation,
        )
        values.forEach { (id, value) -> diagnostics[id]?.text = value?.toString() ?: "<null>" }
    }

    companion object {
        private val fields = listOf(
            R.id.case_id to "CASE_ID", R.id.actual_input_type to "ACTUAL_INPUT_TYPE",
            R.id.actual_ime_options to "ACTUAL_IME_OPTIONS", R.id.actual_action_label to "ACTUAL_ACTION_LABEL",
            R.id.actual_action_id to "ACTUAL_ACTION_ID", R.id.input_connection_present to "INPUT_CONNECTION_PRESENT",
            R.id.editor_action_count to "EDITOR_ACTION_COUNT", R.id.last_editor_action_id to "LAST_EDITOR_ACTION_ID",
            R.id.last_action_return_value to "LAST_ACTION_RETURN_VALUE", R.id.newline_count to "NEWLINE_COUNT",
            R.id.current_text to "CURRENT_TEXT", R.id.commit_count to "COMMIT_COUNT",
            R.id.last_commit_text to "LAST_COMMIT_TEXT", R.id.snapshot_revision to "SNAPSHOT_REVISION",
            R.id.platform_input_type to "PLATFORM_INPUT_TYPE", R.id.platform_ime_options to "PLATFORM_IME_OPTIONS",
            R.id.platform_action_label to "PLATFORM_ACTION_LABEL", R.id.platform_action_id to "PLATFORM_ACTION_ID",
            R.id.ime_visibility to "IME_VISIBILITY", R.id.host_orientation to "HOST_ORIENTATION",
            R.id.display_rotation to "DISPLAY_ROTATION",
        )
    }
}
