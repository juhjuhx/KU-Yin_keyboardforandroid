package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.*
import org.junit.Test

class MatrixDefinitionTest {
    @Test fun allEnterCasesAreUniqueAndOrdered() {
        assertEquals((1..18).map { "D%02d".format(it) }, AcceptanceMatrix.enterCases.map { it.config.caseId })
    }
    @Test fun standardActionIdsAndLabelsAreDeclaredTogether() {
        val cases = AcceptanceMatrix.enterCases.associateBy { it.config.caseId }
        val expected = listOf("前往" to 2, "搜尋" to 3, "傳送" to 4, "下一步" to 5, "完成" to 6, "上一步" to 7)
        expected.forEachIndexed { index, pair ->
            val case = cases.getValue("D%02d".format(index + 3))
            assertEquals(pair.first, case.expectedLabel)
            assertEquals(pair.second, case.expectedActionId)
            assertEquals(pair.second, case.config.imeOptions)
        }
    }
    @Test fun noEnterCasesActuallyDeclareTheFlag() {
        val cases = AcceptanceMatrix.enterCases.associateBy { it.config.caseId }
        listOf("D09", "D10", "D12", "D14", "D15").forEach { id ->
            val case = cases.getValue(id)
            assertTrue(case.config.imeOptions and AcceptanceMatrix.NO_ENTER_ACTION != 0)
            assertNull(case.expectedActionId)
            assertEquals("換行", case.expectedLabel)
        }
    }
    @Test fun customCasesCarryRealIdAndAmbiguousLabel() {
        val cases = AcceptanceMatrix.enterCases.associateBy { it.config.caseId }
        for (id in listOf("D11", "D12", "D16")) assertEquals(73, cases.getValue(id).config.customActionId)
        assertEquals("確認", cases.getValue("D11").config.customActionLabel)
        assertEquals("換行", cases.getValue("D16").config.customActionLabel)
        assertTrue(cases.getValue("D16").customPresentation)
        assertEquals(73, cases.getValue("D16").expectedActionId)
    }
    @Test fun falseReturnAndCompositionCasesAreExplicit() {
        val cases = AcceptanceMatrix.enterCases.associateBy { it.config.caseId }
        assertFalse(cases.getValue("D17").config.performEditorActionReturnValue)
        assertEquals(4, cases.getValue("D17").expectedActionId)
        assertEquals(listOf("D13", "D14"), AcceptanceMatrix.enterCases.filter { it.compositionFirst }.map { it.config.caseId })
    }
    @Test fun nonTextFixturesIncludeRealTypeNull() {
        assertEquals(listOf("C01", "C02", "C03", "C04"), AcceptanceMatrix.nonTextCases.map { it.caseId })
        assertEquals(listOf(2, 3, 4, 0), AcceptanceMatrix.nonTextCases.map { it.inputType })
    }
}
