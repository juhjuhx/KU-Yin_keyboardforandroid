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
    fun nativeEngineSurfacesVisibleDachenPhoneticPreedit() {
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

            val update = engine.handleKeyUpdate('1'.code)
            assertTrue(
                "Dachen key '1' (ㄅ) must surface visible phonetic preedit; " +
                    "consumed=${update.consumed}, preedit='${update.preedit}', candidates=${update.candidates}",
                update.preedit.contains("ㄅ"),
            )
        } finally {
            engine.close()
        }
    }

    @Test
    fun imeBindsAndSurvivesEditorRecreate() {
        val listed = waitForRegisteredIme()
        assertTrue(
            "KU-Yin must be installed as an IME. Installed IMEs:\n$listed",
            listed.contains(APP_PACKAGE),
        )

        val enabled = waitForEnabledIme()
        assertTrue(
            "runtime fixture must pre-enable KU-Yin. Enabled IMEs:\n$enabled",
            enabled.contains(APP_PACKAGE),
        )

        val selected = waitForSelectedIme()
        assertTrue(
            "runtime fixture must pre-select KU-Yin. default_input_method: '$selected'",
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

    private fun waitForRegisteredIme(): String {
        var lastOutput = ""
        repeat(POLL_ATTEMPTS) {
            lastOutput = shell("ime list -s -a")
            if (lastOutput.contains(APP_PACKAGE)) return lastOutput
            SystemClock.sleep(POLL_INTERVAL_MS)
        }
        return lastOutput
    }

    private fun waitForEnabledIme(): String {
        var lastOutput = ""
        repeat(POLL_ATTEMPTS) {
            lastOutput = shell("ime list -s")
            if (lastOutput.contains(APP_PACKAGE)) return lastOutput
            SystemClock.sleep(POLL_INTERVAL_MS)
        }
        return lastOutput
    }

    private fun waitForSelectedIme(): String {
        var lastOutput = ""
        repeat(POLL_ATTEMPTS) {
            lastOutput = shell("settings get secure default_input_method").trim()
            if (lastOutput.contains(APP_PACKAGE) && lastOutput.contains("ChewingInputMethodService")) {
                return lastOutput
            }
            SystemClock.sleep(POLL_INTERVAL_MS)
        }
        return lastOutput
    }

    private fun waitForImeVisible(scenario: ActivityScenario<ImeHostActivity>): Boolean {
        repeat(POLL_ATTEMPTS) {
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
            SystemClock.sleep(POLL_INTERVAL_MS)
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
        const val POLL_ATTEMPTS = 40
        const val POLL_INTERVAL_MS = 250L
    }
}
