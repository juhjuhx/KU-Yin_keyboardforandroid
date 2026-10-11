package com.example.androidkeyboard.input

import android.view.inputmethod.EditorInfo

/**
 * Inline Enter 的唯一決策；presentation 與 dispatch 使用同一個 immutable snapshot。
 * NO_ENTER_ACTION 優先於 standard/custom action，session consumed 的 Enter 不委派至此。
 * https://developer.android.com/reference/android/view/inputmethod/EditorInfo#IME_FLAG_NO_ENTER_ACTION
 */
class EditorEnterPolicy private constructor(
    val label: String,
    val behavior: Behavior,
) {
    val isActionPresentation: Boolean get() = behavior != Behavior.InsertNewline

    sealed interface Behavior {
        data object InsertNewline : Behavior
        data class PerformStandardAction(val actionCode: Int) : Behavior
        data class PerformCustomAction(val actionId: Int) : Behavior
    }

    companion object {
        private val newline = EditorEnterPolicy("換行", Behavior.InsertNewline)

        fun from(imeOptions: Int, actionLabel: String? = null, actionId: Int = 0): EditorEnterPolicy {
            if (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) return newline
            if (actionLabel != null) {
                return EditorEnterPolicy(actionLabel, Behavior.PerformCustomAction(actionId))
            }
            val action = imeOptions and EditorInfo.IME_MASK_ACTION
            val label = when (action) {
                EditorInfo.IME_ACTION_GO -> "前往"
                EditorInfo.IME_ACTION_SEARCH -> "搜尋"
                EditorInfo.IME_ACTION_SEND -> "傳送"
                EditorInfo.IME_ACTION_NEXT -> "下一步"
                EditorInfo.IME_ACTION_DONE -> "完成"
                EditorInfo.IME_ACTION_PREVIOUS -> "上一步"
                else -> return newline
            }
            return EditorEnterPolicy(label, Behavior.PerformStandardAction(action))
        }
    }
}
