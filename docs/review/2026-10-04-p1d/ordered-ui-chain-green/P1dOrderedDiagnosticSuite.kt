package com.example.ime

import com.example.GreetingScreenshotTest
import com.example.androidkeyboard.input.KeyboardHitboxContractTest
import com.example.ime.ui.DachenKeyboardLayoutTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/** 暫時診斷用；完成後移除。 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    GreetingScreenshotTest::class,
    KeyboardHitboxContractTest::class,
    EnterPolicyServiceTest::class,
    EnterUiWiringTest::class,
    NonTextEditorServiceTest::class,
    DachenKeyboardLayoutTest::class,
)
class P1dOrderedDiagnosticSuite
