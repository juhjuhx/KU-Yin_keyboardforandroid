package com.example.androidkeyboard.runtime

import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.android.LibChewingDataInstaller
import com.example.androidkeyboard.engines.core.ChewingEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImeRuntimeSmokeTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun nativeEngineInitializesAndLearningGateWorks() {
        val context = instrumentation.targetContext
        val nativePaths = LibChewingDataInstaller.ensureInstalled(context)
        val engine = AndroidChewingEngine(
            systemDataPath = nativePaths.systemDir.absolutePath,
            userDataPath = nativePaths.userFile.absolutePath,
        )

        try {
            engine.init(ChewingEngine.Layout.DACHEN)
            assertTrue("libchewing JNI session should be ready on an Android runtime", engine.isReady)

            engine.setPersonalizedLearningEnabled(false)
            assertFalse("no-learning policy should reach libchewing", engine.personalizedLearningEnabled)

            val update = engine.handleKeyUpdate('5'.code)
            assertTrue(
                "a valid Dachen key should produce a decoder transition",
                update.consumed || update.preedit.isNotEmpty() || update.candidates.isNotEmpty(),
            )
        } finally {
            engine.close()
        }
    }

    @Test
    fun imeRegistersEnablesBindsAndSurvivesEditorRecreate() {
        val listed = shell("ime list -s")
        assertTrue("KU-Yin must be registered as an IME", listed.contains(APP_PACKAGE))

        shell("ime enable $IME_COMPONENT")
        shell("ime set $IME_COMPONENT")

        val selected = shell("settings get secure default_input_method").trim()
        assertTrue(
            "KU-Yin must become the selected IME, got: $selected",
            selected.contains(APP_PACKAGE) && selected.contains("ChewingInputMethodService"),
        )

        ActivityScenario.launch(ImeHostActivity::class.java).use { scenario ->
            scenario.onActivity { it.requestIme() }
            assertTrue("KU-Yin input window should become visible", waitForImeVisible(scenario))

            scenario.recreate()
            scenario.onActivity { it.requestIme() }
            assertTrue("KU-Yin should survive editor recreation", waitForImeVisible(scenario))
        }

        val dump = shell("dumpsys input_method")
        assertTrue(
            "InputMethodManager should still reference KU-Yin after the smoke flow",
            dump.contains(APP_PACKAGE) && dump.contains("ChewingInputMethodService"),
        )
    }

    private fun waitForImeVisible(scenario: ActivityScenario<ImeHostActivity>): Boolean {
        repeat(40) {
            var visible = false
            scenario.onActivity { activity ->
                if (android.os.Build.VERSION.SDK_INT >= 30) {
                    visible = activity.window.decorView.rootWindowInsets
                        ?.isVisible(WindowInsets.Type.ime()) == true
                } else {
                    val imm = activity.getSystemService(InputMethodManager::class.java)
                    visible = imm.isAcceptingText
                }
            }
            if (visible) return true
            SystemClock.sleep(250)
        }
        return false
    }

    private fun shell(command: String): String {
        val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor)
            .bufferedReader()
            .use { it.readText() }
    }

    private companion object {
        const val APP_PACKAGE = "com.example.androidkeyboard"
        const val IME_COMPONENT =
            "com.example.androidkeyboard/com.example.androidkeyboard.input.ChewingInputMethodService"
    }
}
