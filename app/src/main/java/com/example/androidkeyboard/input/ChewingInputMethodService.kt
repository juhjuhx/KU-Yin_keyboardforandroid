package com.example.androidkeyboard.input

import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.example.androidkeyboard.ui.CandidateView

class ChewingInputMethodService : InputMethodService() {

    private lateinit var keyboardView: KeyboardView
    private lateinit var candidateView: CandidateView

    override fun onCreateInputView(): View = with(LayoutInflater.from(this)) {
        keyboardView = KeyboardView(this@ChewingInputMethodService).apply {
            setLayout(KeyboardLayout.Dachen.rows)
            setKeyHeight(60f)
            onKeyPress = ::handleKey
        }
        candidateView = CandidateView(this@ChewingInputMethodService).apply {
            setCandidates(emptyList())
            onItemClick = ::commitCandidate
        }

        val container = LinearLayout(this@ChewingInputMethodService).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            addView(keyboardView)
            addView(candidateView)
        }
        container
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        // TODO T10: init libchewing editor, load user dict
    }

    override fun onFinishInput() {
        super.onFinishInput()
        clearComposingText()
        // TODO T10: reset libchewing editor
    }

    private fun handleKey(key: String) {
        val ic = currentInputConnection ?: return
        when (key) {
"back" -> ic.deleteSurroundingText(1, 0)
" " -> ic.commitText(" ", 1)
            else -> ic.commitText(key, 1)
        }
    }

    private fun commitCandidate(candidate: String) {
        currentInputConnection?.commitText(candidate, 1)
    }
}
