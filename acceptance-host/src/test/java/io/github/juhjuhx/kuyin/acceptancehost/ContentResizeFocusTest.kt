package io.github.juhjuhx.kuyin.acceptancehost

import android.view.View
import android.view.ViewGroup
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Offline content-receiver control; Robolectric alone did not reproduce the CI final-focus loss. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ContentResizeFocusTest {
    @Test fun editorRestoresFocusAfterTransientContentCollapseWithoutExtraEditorEffects() {
        val controller = Robolectric.buildActivity(AcceptanceEditorActivity::class.java).setup()
        try {
            val activity = controller.get()
            val editor = activity.findViewById<RecordingEditText>(R.id.editor)
            val content = activity.findViewById<ViewGroup>(android.R.id.content)
            fun layout(h: Int) {
                content.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY))
                content.layout(0, 63, 1080, 63 + h)
            }
            layout(1731)
            assertTrue(editor.requestFocus())
            val before = activity.editorSnapshot
            layout(0)
            assertFalse(editor.isFocused)
            layout(956)
            assertTrue("Real content recovery must restore editor focus, without loop/repeated taps", editor.isFocused)
            assertEquals(before.commitCount, activity.editorSnapshot.commitCount)
            assertEquals(before.editorActionCount, activity.editorSnapshot.editorActionCount)
        } finally { controller.pause().stop().destroy() }
    }
}
