package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.PrintWriter
import java.io.StringWriter

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FocusLossTraceTest {
    @Test fun firstRealFocusLossHasBoundedCallerFramesWithoutText() {
        val controller = Robolectric.buildActivity(AcceptanceEditorActivity::class.java).setup()
        try {
            val activity = controller.get()
            val editor = activity.findViewById<RecordingEditText>(R.id.editor)
            editor.setText("F4_PRIVATE_TEST_MARKER")
            editor.requestFocus()
            assertTrue(editor.isFocused)
            // This is fixture setup, never an action performed by the observer.
            editor.isFocusable = false
            assertFalse(editor.isFocused)
            val before = activity.editorSnapshot
            val out = StringWriter()
            activity.dump("", null, PrintWriter(out), arrayOf("--kuyin-focus-trace"))
            val text = out.toString()
            assertTrue(text.contains("KUYIN_FOCUS_TRACE_BEGIN"))
            assertTrue(text.contains("event=FOCUS_LOSS_CALLBACK"))
            assertTrue(text.contains("RecordingEditText#onFocusChanged:"))
            assertTrue(text.lines().count { it.startsWith("last_frame=") } in 1..24)
            assertFalse(text.contains("F4_PRIVATE_TEST_MARKER"))
            assertEquals(before, activity.editorSnapshot)
            assertFalse(editor.isFocused)
        } finally { controller.pause().stop().destroy() }
    }
}
