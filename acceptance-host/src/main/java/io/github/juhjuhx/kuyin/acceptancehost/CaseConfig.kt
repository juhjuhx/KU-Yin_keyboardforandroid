package io.github.juhjuhx.kuyin.acceptancehost

/** 純 primitive 協定；不含被測輸入法的任何 Enter 決策。 */
data class CaseConfig(
    val caseId: String = "DEFAULT",
    val inputType: Int = 1,
    val imeOptions: Int = 0,
    val customActionLabel: String? = null,
    val customActionId: Int = 0,
    val performEditorActionReturnValue: Boolean = true,
    val initialText: String = "",
    val minLines: Int = 1,
) {
    init { require(minLines > 0) { "minLines 必須大於 0" } }

    companion object {
        fun fromExtras(extras: Map<String, Any?>): CaseConfig = CaseConfig(
            caseId = extras.primitive("caseId", "DEFAULT"),
            inputType = extras.primitive("inputType", 1),
            imeOptions = extras.primitive("imeOptions", 0),
            customActionLabel = extras["customActionLabel"].let {
                require(it == null || it is String) { "customActionLabel 必須為 String 或 null" }
                it as String?
            },
            customActionId = extras.primitive("customActionId", 0),
            performEditorActionReturnValue = extras.primitive("performEditorActionReturnValue", true),
            initialText = extras.primitive("initialText", ""),
            minLines = extras.primitive("minLines", 1),
        )

        private inline fun <reified T : Any> Map<String, Any?>.primitive(key: String, default: T): T {
            if (!containsKey(key)) return default
            val value = get(key)
            require(value is T) { "$key 必須為 ${T::class.java.simpleName}" }
            return value
        }
    }
}
