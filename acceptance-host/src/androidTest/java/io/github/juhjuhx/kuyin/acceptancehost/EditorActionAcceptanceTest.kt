package io.github.juhjuhx.kuyin.acceptancehost

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/** 每個 editor case 獨立回報；真 IME 操作只走跨視窗可見軟鍵。 */
@RunWith(AndroidJUnit4::class)
class EditorActionAcceptanceTest {
    private fun verify(id: String) = acceptanceCase(id) {
        val case = AcceptanceMatrix.enterCases.single { it.config.caseId == id }
        launch(case.config)
        selectMode(VisibleKeyboardMode.ZHUYIN)
        assertEmptyComposition()
        if (case.compositionFirst) {
            compose(listOf("ㄋ", "ㄧ", "ˇ"))
            assertCompositionEnter(case)
        } else if (id == "D18") {
            assertThreeLayoutEnter(case)
            val newlineCase = EnterCase(
                config = case.config.copy(caseId = "D18-NO_ENTER_ACTION", imeOptions = 4 or AcceptanceMatrix.NO_ENTER_ACTION),
                expectedLabel = "換行",
                expectedActionId = null,
            )
            launch(newlineCase.config)
            assertThreeLayoutEnter(newlineCase)
            scope = "已測 ZHUYIN／ENGLISH／SYMBOLS 的真模式切換及 SEND action／NO_ENTER_ACTION newline 兩條軟鍵接線。"
        } else {
            assertEmptyEnter(case)
        }
        if (id == "D17") {
            scope = "已測 SEND 一次、host 回 false、無換行；未證明 Android oneway IPC 傳回 boolean 至 IME。"
            inconclusive("HOST_FALSE_NOT_PROPAGATED_BY_ANDROID_IPC")
        }
    }

    @Test fun d01NoneInsertsOneNewline() = verify("D01")
    @Test fun d02UnspecifiedInsertsOneNewline() = verify("D02")
    @Test fun d03GoDispatchesOneAction() = verify("D03")
    @Test fun d04SearchDispatchesOneAction() = verify("D04")
    @Test fun d05SendDispatchesOneAction() = verify("D05")
    @Test fun d06NextDispatchesOneAction() = verify("D06")
    @Test fun d07DoneDispatchesOneAction() = verify("D07")
    @Test fun d08PreviousDispatchesOneAction() = verify("D08")
    @Test fun d09SendWithNoEnterActionInsertsNewline() = verify("D09")
    @Test fun d10GoWithNoEnterActionInsertsNewline() = verify("D10")
    @Test fun d11CustomActionUsesCustomId() = verify("D11")
    @Test fun d12NoEnterActionOverridesCustomAction() = verify("D12")
    @Test fun d13CompositionCompletesBeforeSend() = verify("D13")
    @Test fun d14CompositionCompletesBeforeNewline() = verify("D14")
    @Test fun d15ExplicitMultilineInsertsNewline() = verify("D15")
    @Test fun d16CustomNewlineLabelRemainsTextAction() = verify("D16")
    @Test fun d17HostFalseHasObservableActionButIpcScopeRemainsOpen() = verify("D17")
    @Test fun d18AllThreeLayoutsWireRealSoftEnter() = verify("D18")
}
