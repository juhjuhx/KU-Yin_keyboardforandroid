package io.github.juhjuhx.kuyin.acceptancehost

import android.view.View
import android.view.ViewGroup
import org.json.JSONObject
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
class LayoutTraceTest {
    private fun dump(activity: AcceptanceEditorActivity): Pair<String, JSONObject> {
        val out = StringWriter()
        activity.dump("", null, PrintWriter(out), arrayOf("--kuyin-layout-trace"))
        val text = out.toString()
        assertTrue("Bounded layout evidence must have explicit identity", text.startsWith("KUYIN_LAYOUT_BEGIN\n"))
        assertTrue(text.endsWith("\nKUYIN_LAYOUT_END\n"))
        return text to JSONObject(text.substringAfter("KUYIN_LAYOUT_BEGIN\n").substringBefore("\nKUYIN_LAYOUT_END"))
    }

    @Test fun recordsZeroSizedAncestorAtFocusLossWithoutChangingEditorState() {
        val controller = Robolectric.buildActivity(AcceptanceEditorActivity::class.java).setup()
        try {
            val activity = controller.get()
            val editor = activity.findViewById<RecordingEditText>(R.id.editor)
            val root = editor.parent as ViewGroup
            fun layout(h: Int) {
                root.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY))
                root.layout(0, 0, 1080, h)
            }
            editor.setText("P0_SYNTHETIC_TEXT_EXCLUDED")
            layout(1000)
            assertTrue(editor.requestFocus())
            layout(0)
            assertFalse(editor.isFocused)
            val before = activity.editorSnapshot
            val (raw, state) = dump(activity)
            val loss = state.getJSONArray("last_loss_ancestry")
            val ancestor = (0 until loss.length()).map { loss.getJSONObject(it) }
                .single { it.getInt("identity") == System.identityHashCode(root) }
            assertEquals(0, ancestor.getInt("height"))
            assertTrue(state.getJSONArray("layout_events").length() > 0)
            assertFalse(raw.contains("P0_SYNTHETIC_TEXT_EXCLUDED"))
            assertFalse(editor.isFocused)
            assertEquals(before, activity.editorSnapshot)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun historyIsBoundedAndListenersAreReleasedOnDetach() {
        val controller = Robolectric.buildActivity(AcceptanceEditorActivity::class.java).setup()
        val activity = controller.get()
        val editor = activity.findViewById<RecordingEditText>(R.id.editor)
        val root = editor.parent as ViewGroup
        repeat(80) { root.layout(0, 0, 1000, 1000 + it) }
        val state = dump(activity).second
        assertTrue(state.getJSONArray("layout_events").length() <= 32)
        assertTrue(state.getInt("observer_count") in 1..12)
        controller.pause().stop().destroy()
        assertEquals(0, dump(activity).second.getInt("observer_count"))
    }
}
