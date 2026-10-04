package com.example.ime

import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.ime.engine.KeyboardMode
import com.example.ime.engine.KuYinEngine
import com.example.ime.session.ChewingEngineSession
import com.example.ime.session.ImeCommand
import com.example.ime.settings.KeyboardSettings
import com.example.ime.ui.KuYinKeyboardUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class NonTextEditorServiceTest {
    @get:Rule val composeRule = createComposeRule()

    private lateinit var service: KuYinInputMethodService
    private lateinit var engine: KuYinEngine
    private lateinit var native: RecordingEngine
    private lateinit var session: ChewingEngineSession

    @Before
    fun setUp() {
        // 只驗證真正的 onStartInput 與 UI；注入 native seam，避免啟動 JNI／安裝詞庫。
        service = Robolectric.buildService(KuYinInputMethodService::class.java).get()
        engine = KuYinEngine(service)
        native = RecordingEngine()
        session = ChewingEngineSession(native)
        setField("engine", engine)
        setField("nativeEngine", native)
        setField("session", session)
    }

    private fun setField(name: String, value: Any) {
        KuYinInputMethodService::class.java.getDeclaredField(name).apply {
            isAccessible = true
            set(service, value)
        }
    }

    private fun start(type: Int, restarting: Boolean = false) {
        service.onStartInput(EditorInfo().apply {
            inputType = type
            imeOptions = EditorInfo.IME_ACTION_NEXT
        }, restarting)
    }

    @Test
    fun `text to non text clears native composition and restores Zhuyin on return`() {
        listOf(InputType.TYPE_NULL, InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_DATETIME)
            .forEach { type ->
                start(InputType.TYPE_CLASS_TEXT)
                session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
                assertEquals("ㄋ", session.state.value.preedit)
                assertTrue(session.state.value.candidates.isNotEmpty())

                start(type)
                assertEquals(KeyboardMode.ENGLISH, engine.mode.value)
                assertEquals("", session.state.value.preedit)
                assertTrue(session.state.value.candidates.isEmpty())
                engine.setMode(KeyboardMode.ZHUYIN)
                assertEquals(KeyboardMode.ENGLISH, engine.mode.value)

                start(InputType.TYPE_CLASS_TEXT)
                assertEquals(KeyboardMode.ZHUYIN, engine.mode.value)
            }
    }

    @Test
    fun `non text start turns native learning off and text return restores it`() {
        start(InputType.TYPE_CLASS_TEXT)
        assertTrue(native.personalizedLearningEnabled)
        start(InputType.TYPE_CLASS_PHONE)
        assertFalse(native.personalizedLearningEnabled)
        start(InputType.TYPE_CLASS_TEXT)
        assertTrue(native.personalizedLearningEnabled)
    }

    @Test
    fun `service preserves every user preference across numeric editor`() {
        KeyboardMode.entries.forEach { preferred ->
            setField("userPreferredMode", preferred)
            start(InputType.TYPE_CLASS_TEXT)
            assertEquals(preferred, engine.mode.value)
            start(InputType.TYPE_CLASS_NUMBER)
            assertEquals(KeyboardMode.ENGLISH, engine.mode.value)
            start(InputType.TYPE_CLASS_TEXT)
            assertEquals(preferred, engine.mode.value)
        }
    }

    @Test
    fun `restarting non text input resets native state and keeps English`() {
        start(InputType.TYPE_CLASS_PHONE)
        val before = native.resetCalls
        start(InputType.TYPE_CLASS_PHONE, restarting = true)
        assertEquals(before + 1, native.resetCalls)
        assertEquals(KeyboardMode.ENGLISH, engine.mode.value)
        assertFalse(native.personalizedLearningEnabled)
    }

    @Test
    fun `numeric editor blocks Zhuyin and symbol digit commits ASCII without native phonetic input`() {
        start(InputType.TYPE_CLASS_NUMBER)
        val commits = mutableListOf<String>()
        composeRule.setContent {
            KuYinKeyboardUi(
                engine = engine,
                session = session,
                settings = KeyboardSettings(service),
                actionLabel = "下一步",
                onCommitText = { commits.add(it) },
                onDeleteSurroundingText = {},
                onPerformEditorAction = {},
                onHideKeyboard = {},
                onFeedback = {},
                onUserModeSelected = { setField("userPreferredMode", it) },
            )
        }
        composeRule.onNodeWithText("ㄕ").assertDoesNotExist()
        composeRule.onNodeWithText("注音").performClick()
        composeRule.runOnIdle { assertEquals(KeyboardMode.ENGLISH, engine.mode.value) }
        composeRule.onNodeWithText("ㄕ").assertDoesNotExist()
        composeRule.onNodeWithText("?123").performClick()
        composeRule.onNodeWithText("1").performClick()
        composeRule.runOnIdle {
            assertEquals(listOf("1"), commits)
            assertTrue(native.keyCodes.isEmpty())
            assertEquals("", session.state.value.preedit)
        }
    }

    private class RecordingEngine : ChewingEngine {
        val keyCodes = mutableListOf<Int>()
        var resetCalls = 0
        override val isReady = true
        override var personalizedLearningEnabled = false
            private set
        private val idle = EngineUpdate(true, "", emptyList(), "")
        override fun init(layout: ChewingEngine.Layout) {}
        override fun setPersonalizedLearningEnabled(enabled: Boolean) {
            personalizedLearningEnabled = enabled
        }
        override fun reset() { resetCalls++ }
        override fun handleKeyUpdate(keyCode: Int): EngineUpdate {
            keyCodes.add(keyCode)
            return EngineUpdate(true, "ㄋ", listOf("你"), "")
        }
        override fun backspaceUpdate() = idle
        override fun selectCandidateUpdate(index: Int) = idle
        override fun commitUpdate() = idle
        override fun nextPageUpdate(): EngineUpdate? = null
        override fun prevPageUpdate(): EngineUpdate? = null
        override fun canPageCandidatesBackward() = false
        override fun canPageCandidatesForward() = false
        override fun close() {}
    }
}
