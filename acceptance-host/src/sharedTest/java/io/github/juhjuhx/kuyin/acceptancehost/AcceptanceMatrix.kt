package io.github.juhjuhx.kuyin.acceptancehost

/** 只在 test/androidTest 中打包，不屬於 editor oracle 的產品邏輯。 */
data class EnterCase(
    val config: CaseConfig,
    val expectedLabel: String,
    val expectedActionId: Int?,
    val compositionFirst: Boolean = false,
    val customPresentation: Boolean = false,
)

object AcceptanceMatrix {
    const val NO_ENTER_ACTION: Int = 0x40000000
    const val MULTILINE_TEXT: Int = 0x00020001
    val enterCases: List<EnterCase> = listOf(
        EnterCase(CaseConfig("D01", imeOptions = 1), "換行", null),
        EnterCase(CaseConfig("D02", imeOptions = 0), "換行", null),
        EnterCase(CaseConfig("D03", imeOptions = 2), "前往", 2),
        EnterCase(CaseConfig("D04", imeOptions = 3), "搜尋", 3),
        EnterCase(CaseConfig("D05", imeOptions = 4), "傳送", 4),
        EnterCase(CaseConfig("D06", imeOptions = 5), "下一步", 5),
        EnterCase(CaseConfig("D07", imeOptions = 6), "完成", 6),
        EnterCase(CaseConfig("D08", imeOptions = 7), "上一步", 7),
        EnterCase(CaseConfig("D09", imeOptions = 4 or NO_ENTER_ACTION), "換行", null),
        EnterCase(CaseConfig("D10", imeOptions = 2 or NO_ENTER_ACTION), "換行", null),
        EnterCase(CaseConfig("D11", imeOptions = 4, customActionLabel = "確認", customActionId = 73),
            "確認", 73, customPresentation = true),
        EnterCase(CaseConfig("D12", imeOptions = 4 or NO_ENTER_ACTION, customActionLabel = "確認", customActionId = 73),
            "換行", null),
        EnterCase(CaseConfig("D13", imeOptions = 4), "傳送", 4, compositionFirst = true),
        EnterCase(CaseConfig("D14", imeOptions = 4 or NO_ENTER_ACTION), "換行", null, compositionFirst = true),
        EnterCase(CaseConfig("D15", inputType = MULTILINE_TEXT, imeOptions = 4 or NO_ENTER_ACTION, minLines = 3),
            "換行", null),
        EnterCase(CaseConfig("D16", imeOptions = 4, customActionLabel = "換行", customActionId = 73),
            "換行", 73, customPresentation = true),
        EnterCase(CaseConfig("D17", imeOptions = 4, performEditorActionReturnValue = false), "傳送", 4),
        EnterCase(CaseConfig("D18", imeOptions = 4), "傳送", 4),
    )
    val nonTextCases: List<CaseConfig> = listOf(
        CaseConfig("C01", inputType = 2, imeOptions = 1),
        CaseConfig("C02", inputType = 3, imeOptions = 1),
        CaseConfig("C03", inputType = 4, imeOptions = 1),
        CaseConfig("C04", inputType = 0, imeOptions = 1),
    )
}
