package com.example.androidkeyboard.runtime

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.util.Log
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputConnection
import android.widget.EditText
import org.json.JSONObject

class ImeHostActivity : Activity() {
    private var connectionCount = 0
    private var eventCount = 0
    private val diagnosticsEnabled: Boolean
        get() = intent.getBooleanExtra("KUYIN_R2_DIAGNOSTICS", false)

    lateinit var editor: EditText
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editor = (if (diagnosticsEnabled) object : EditText(this) {
            override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
                val connection = super.onCreateInputConnection(outAttrs)
                connectionCount++
                trace("INPUT_CONNECTION_CREATED", JSONObject()
                    .put("connection_present", connection != null)
                    .put("input_type", outAttrs.inputType)
                    .put("ime_options", outAttrs.imeOptions)
                    .put("action_id", outAttrs.actionId)
                    .put("editor_package", outAttrs.packageName ?: JSONObject.NULL))
                return connection
            }

            override fun onAttachedToWindow() {
                super.onAttachedToWindow()
                trace("EDITOR_ATTACHED")
            }

            override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
                super.onWindowFocusChanged(hasWindowFocus)
                trace("EDITOR_WINDOW_FOCUS")
            }
        } else EditText(this)).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            imeOptions = EditorInfo.IME_ACTION_DONE
            hint = "KU-Yin runtime smoke editor"
            minLines = 3
            requestFocus()
        }

        setContentView(
            editor,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        trace("ACTIVITY_CREATED")
    }

    fun requestIme() {
        val focused = editor.requestFocus()
        trace("VIEW_FOCUS_REQUEST", JSONObject().put("accepted", focused))
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            val controller = window.insetsController
            trace("INSETS_SHOW_REQUEST", JSONObject().put("controller_present", controller != null))
            controller?.show(WindowInsets.Type.ime())
        }
        editor.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val accepted = imm.showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT)
            trace("IMM_SHOW_RESULT", JSONObject().put("accepted", accepted))
        }
    }

    /** TEST_ONLY passive metadata; never reads text or calls IMM.checkFocus APIs. */
    fun recordR2State(event: String) = trace(event)

    private fun trace(event: String, details: JSONObject = JSONObject()) {
        if (!diagnosticsEnabled || !::editor.isInitialized || eventCount >= 200) return
        val insets = editor.rootWindowInsets
        details.put("event", event)
            .put("event_id", ++eventCount)
            .put("elapsed_ms", SystemClock.elapsedRealtime())
            .put("activity_generation", System.identityHashCode(this))
            .put("view_focused", editor.hasFocus())
            .put("window_focused", editor.hasWindowFocus())
            .put("attached", editor.isAttachedToWindow)
            .put("token_present", editor.windowToken != null)
            .put("root_insets_present", insets != null)
            .put("input_connection_count", connectionCount)
            .put("ime_visible", if (android.os.Build.VERSION.SDK_INT >= 30 && insets != null)
                insets.isVisible(WindowInsets.Type.ime()) else JSONObject.NULL)
        Log.i("KUYIN_R2_TEST_ONLY", details.toString())
    }
}
