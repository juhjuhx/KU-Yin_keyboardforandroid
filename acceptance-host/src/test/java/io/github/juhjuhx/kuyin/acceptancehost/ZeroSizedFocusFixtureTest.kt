package io.github.juhjuhx.kuyin.acceptancehost

import android.view.View
import android.view.ViewGroup
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Offline Android View mechanism characterization, not a Runtime repair verdict. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ZeroSizedFocusFixtureTest {
    @Test fun sameSizeLayoutKeepsRealEditorFocusAndDoesNotRecordLoss() {
        val controller = Robolectric.buildActivity(AcceptanceEditorActivity::class.java).setup()
        try {
            val activity = controller.get()
            val editor = activity.findViewById<RecordingEditText>(R.id.editor)
            val root = editor.parent as ViewGroup
            root.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY))
            root.layout(0, 0, 1080, 1000)
            assertTrue(editor.requestFocus())
            val losses = editor.focusLossCount
            root.layout(0, 0, 1080, 1000)
            assertTrue(editor.isFocused)
            assertEquals(losses, editor.focusLossCount)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun transientZeroHeightClearsThenRestoresFocusInThisOfflineFixture() {
        val controller = Robolectric.buildActivity(AcceptanceEditorActivity::class.java).setup()
        try {
            val activity = controller.get()
            val editor = activity.findViewById<RecordingEditText>(R.id.editor)
            val root = editor.parent as ViewGroup
            fun layout(height: Int) {
                root.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
                root.layout(0, 0, 1080, height)
            }
            layout(1000)
            editor.setText("F4_SYNTHETIC_LAYOUT_PROBE")
            layout(1000)
            assertTrue(editor.requestFocus())
            assertTrue(editor.isFocused)
            val before = activity.editorSnapshot
            val initialLosses = editor.focusLossCount
            layout(0)
            assertFalse("A zero-size ancestor must exercise real framework focus loss", editor.isFocused)
            assertTrue(editor.focusLossCount > initialLosses)
            assertTrue(editor.lastFocusLoss!!.frames.any { it.startsWith("android.view.View#sizeChange:") })
            layout(1000)
            assertTrue(editor.width > 0 && editor.height > 0)
            assertTrue("This synchronous fixture reacquires focus; it does not reproduce the observed CI focus-after=false state", editor.isFocused)
            assertEquals("F4_SYNTHETIC_LAYOUT_PROBE", editor.text.toString())
            assertEquals(before.commitCount, activity.editorSnapshot.commitCount)
            assertEquals(before.editorActionCount, activity.editorSnapshot.editorActionCount)
        } finally { controller.pause().stop().destroy() }
    }
}
