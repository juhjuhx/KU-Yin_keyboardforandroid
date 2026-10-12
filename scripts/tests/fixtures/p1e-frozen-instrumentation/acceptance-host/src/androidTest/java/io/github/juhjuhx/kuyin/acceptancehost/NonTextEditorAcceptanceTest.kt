package io.github.juhjuhx.kuyin.acceptancehost

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NonTextEditorAcceptanceTest {
    private fun verify(id: String) = acceptanceCase(id) {
        scope = "可觀測 non-text metadata／temporary ENGLISH／注音切換阻擋；TYPE_NULL 若無真 callback 或可見 IME 則保留接線缺口，native learning 未有 observer。"
        val config = AcceptanceMatrix.nonTextCases.single { it.caseId == id }
        launch(config)
        assertNonTextPresentation(attemptZhuyin = true)
        scope = "已測實際 EditorInfo、temporary ENGLISH、可見注音切換遭阻擋與無 preedit；未取得 native learning observer。"
        inconclusive("NATIVE_LEARNING_OBSERVER_UNAVAILABLE")
    }

    @Test fun c01NumberTemporarilyUsesEnglish() = verify("C01")
    @Test fun c02PhoneTemporarilyUsesEnglish() = verify("C02")
    @Test fun c03DatetimeTemporarilyUsesEnglish() = verify("C03")
    @Test fun c04NullConnectionIsRealOrExplicitlyInconclusive() = verify("C04")

    @Test fun c05ReturningToTextRestoresBothPriorPreferredModes() = acceptanceCase("C05") {
        assertPreferredModeRestoration()
        scope = "已測 preferred ZHUYIN／ENGLISH 經 C01–C04 後返回普通 text 的可見模式；TYPE_NULL 接線缺口另記 evidence；未取得 native learning observer。"
        inconclusive("NATIVE_LEARNING_OBSERVER_UNAVAILABLE")
    }
}
