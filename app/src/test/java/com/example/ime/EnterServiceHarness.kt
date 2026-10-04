package com.example.ime

import android.text.InputType
import android.os.Looper
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.ime.engine.KuYinEngine
import com.example.ime.service.ImeLifecycleOwner
import com.example.ime.session.ChewingEngineSession
import com.example.ime.session.ComposeDecoderSession
import com.example.ime.session.ImeCommand
import com.example.ime.settings.KeyboardSettings
import com.example.ime.util.FeedbackHelper
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.util.ReflectionHelpers

/** 真實 service callbacks/UI，只有 Android connection 與 JNI seam 使用 recording fake。 */
internal class EnterServiceHarness {
    val service = Robolectric.buildService(KuYinInputMethodService::class.java).get()
    val engine = KuYinEngine(service)
    val native = RecordingNativeEngine()
    val commands = mutableListOf<ImeCommand>()
    private val decoder = ChewingEngineSession(native)
    val session = object : ComposeDecoderSession {
        override val state = decoder.state
        override fun dispatch(command: ImeCommand) = decoder.dispatch(command).also { commands.add(command) }
    }
    val connection = RecordingConnection(View(service))
    private val lifecycle = ImeLifecycleOwner().apply { onCreate() }

    init {
        val settings = KeyboardSettings(service)
        setField("lifecycleOwner", lifecycle)
        setField("engine", engine)
        setField("nativeEngine", native)
        setField("session", session)
        setField("settings", settings)
        setField("feedbackHelper", FeedbackHelper(service, settings))
        // API36 InputMethodService.getCurrentInputConnection 讀取此欄位。
        ReflectionHelpers.setField(service, "mStartedInputConnection", connection)
    }

    fun setField(name: String, value: Any) = ReflectionHelpers.setField(service, name, value)

    fun close() {
        // 內層ComposeView使用service-owned lifecycle；外層ComposeRule結束不足以銷毀它。
        ReflectionHelpers.getField<ComposeView?>(service, "composeView")?.disposeComposition()
        lifecycle.onStop()
        lifecycle.onDestroy()
        // 真實service callback寫入Compose state；先送出通知，避免Robolectric reset
        // 清掉Handler callback卻保留GlobalSnapshotManager/dispatcher的已排程旗標。
        shadowOf(Looper.getMainLooper()).idle()
    }

    fun start(
        options: Int,
        label: CharSequence? = null,
        id: Int = 0,
        type: Int = InputType.TYPE_CLASS_TEXT,
    ): EditorInfo = EditorInfo().apply {
        inputType = type
        imeOptions = options
        actionLabel = label
        actionId = id
        ReflectionHelpers.setField(service, "mInputEditorInfo", this)
        service.onStartInput(this, false)
        service.onStartInputView(this, false)
        commands.clear()
        connection.effects.clear()
    }

    fun enterEditor() {
        KuYinInputMethodService::class.java.getDeclaredMethod("handleEditorAction").apply {
            isAccessible = true
            invoke(service)
        }
    }

    sealed interface Effect {
        data class Commit(val text: String, val cursor: Int = 1) : Effect
        data class Action(val id: Int) : Effect
    }

    class RecordingConnection(view: View) : BaseInputConnection(view, true) {
        val effects = mutableListOf<Effect>()
        var actionResult = true
        override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
            effects.add(Effect.Commit(text.toString(), newCursorPosition))
            return true
        }
        override fun performEditorAction(actionCode: Int): Boolean {
            effects.add(Effect.Action(actionCode))
            return actionResult
        }
    }

    class RecordingNativeEngine : ChewingEngine {
        override val isReady = true
        override var personalizedLearningEnabled = false
            private set
        var commitCalls = 0
        private val idle = EngineUpdate(true, "", emptyList(), "")
        override fun init(layout: ChewingEngine.Layout) {}
        override fun setPersonalizedLearningEnabled(enabled: Boolean) { personalizedLearningEnabled = enabled }
        override fun reset() {}
        override fun handleKeyUpdate(keyCode: Int) = EngineUpdate(true, "ㄋ", listOf("你"), "")
        override fun commitUpdate(): EngineUpdate {
            commitCalls++
            return idle.copy(committedText = "你")
        }
        override fun backspaceUpdate() = idle
        override fun selectCandidateUpdate(index: Int) = idle
        override fun nextPageUpdate(): EngineUpdate? = null
        override fun prevPageUpdate(): EngineUpdate? = null
        override fun canPageCandidatesBackward() = false
        override fun canPageCandidatesForward() = false
        override fun close() {}
    }
}
