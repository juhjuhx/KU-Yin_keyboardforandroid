package io.github.juhjuhx.kuyin.acceptancehost

import android.content.Intent

object IntentCaseConfig {
    private val keys = listOf("caseId", "inputType", "imeOptions", "customActionLabel",
        "customActionId", "performEditorActionReturnValue", "initialText", "minLines")

    @Suppress("DEPRECATION")
    fun from(intent: Intent): CaseConfig {
        val extras = intent.extras ?: return CaseConfig()
        return CaseConfig.fromExtras(keys.filter(extras::containsKey).associateWith { extras.get(it) })
    }
}
