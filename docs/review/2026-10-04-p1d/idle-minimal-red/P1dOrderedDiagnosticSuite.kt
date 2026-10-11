package com.example.ime

import com.example.GreetingScreenshotTest
import com.example.androidkeyboard.input.KeyboardHitboxContractTest
import com.example.ime.ui.DachenKeyboardLayoutTest
import org.junit.runner.RunWith
import org.junit.runner.Description
import org.junit.runner.manipulation.Filter
import org.junit.runners.Suite

/** 暫時診斷用：明確驗證新 service UI 後執行 P1-B 的方向。完成後移除。 */
@RunWith(P1dOrderedDiagnosticRunner::class)
class P1dOrderedDiagnosticSuite

class P1dOrderedDiagnosticRunner(testClass: Class<*>) : Suite(testClass, arrayOf(
    IdleAInitializeTest::class.java,
    IdleBWriteTest::class.java,
    IdleCDisplayTest::class.java,
)) {
    init {
        filter(object : Filter() {
            override fun describe() = "只診斷 Dachen native→legacy 邊界，前置測試完整保留"
            override fun shouldRun(description: Description): Boolean {
                if (description.className != DachenKeyboardLayoutTest::class.java.name || !description.isTest) return true
                return description.methodName in listOf(
                    "four phonetic rows keep key order with backspace at fourth row end",
                    "function keys use dedicated callbacks without phonetic dispatch",
                )
            }
        })
    }
}
