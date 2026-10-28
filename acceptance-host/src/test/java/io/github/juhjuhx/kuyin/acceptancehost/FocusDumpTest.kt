package io.github.juhjuhx.kuyin.acceptancehost

import android.widget.EditText
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
class FocusDumpTest {
    private fun dump(activity: AcceptanceEditorActivity): String {
        val output = StringWriter()
        activity.dump("", null, PrintWriter(output), arrayOf("--kuyin-focus"))
        return output.toString()
    }

    @Test fun reportsActualViewAndAccessibilityFocusWithoutChangingThem() {
        val controller = Robolectric.buildActivity(AcceptanceEditorActivity::class.java).setup()
        try {
            val activity = controller.get()
            val editor = activity.findViewById<EditText>(R.id.editor)
            val before = editor.isFocused
            val node = editor.createAccessibilityNodeInfo()
            val output = dump(activity)
            assertTrue(output.contains("KUYIN_FOCUS_TEST_ONLY"))
            assertTrue(output.contains("editor_is_focused=$before"))
            assertTrue(output.contains("node_input_focused=${node.isFocused}"))
            assertTrue(output.contains("window_has_focus=${activity.hasWindowFocus()}"))
            assertEquals(before, editor.isFocused)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun observationDoesNotContainSyntheticTextOrCreateEditorEffects() {
        val controller = Robolectric.buildActivity(AcceptanceEditorActivity::class.java).setup()
        try {
            val activity = controller.get()
            val editor = activity.findViewById<EditText>(R.id.editor)
            editor.setText("F4_SYNTHETIC_CONTENT_MUST_NOT_BE_DUMPED")
            val before = activity.editorSnapshot
            val output = dump(activity)
            assertTrue(output.contains("KUYIN_FOCUS_TEST_ONLY"))
            assertFalse(output.contains("F4_SYNTHETIC_CONTENT_MUST_NOT_BE_DUMPED"))
            assertEquals(before, activity.editorSnapshot)
            assertEquals("F4_SYNTHETIC_CONTENT_MUST_NOT_BE_DUMPED", editor.text.toString())
        } finally { controller.pause().stop().destroy() }
    }
}
