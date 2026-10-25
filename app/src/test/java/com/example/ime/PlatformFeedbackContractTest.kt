package com.example.ime

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.example.ime.settings.KeyboardSettings
import com.example.ime.util.FeedbackHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33, 36])
class PlatformFeedbackContractTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `View feedback needs no additional vibration permission`() {
        val requested = context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_PERMISSIONS
        ).requestedPermissions.orEmpty()
        assertFalse("View-only feedback must preserve the existing permission boundary",
            Manifest.permission.VIBRATE in requested)
    }

    @Test
    fun `enabled feedback requests one keyboard tap on the supplied View`() {
        val view = RecordingView(context)
        val settings = KeyboardSettings(context).apply {
            isVibrateEnabled = true
            isSoundEnabled = false
        }

        FeedbackHelper(context, settings).performKeyPressFeedback(view)

        assertEquals(listOf(HapticFeedbackConstants.KEYBOARD_TAP to
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING), view.requests)
    }

    @Test
    fun `disabled feedback makes no haptic request on the supplied View`() {
        val view = RecordingView(context)
        val settings = KeyboardSettings(context).apply {
            isVibrateEnabled = false
            isSoundEnabled = false
        }

        FeedbackHelper(context, settings).performKeyPressFeedback(view)

        assertTrue(view.requests.isEmpty())
    }

    /** Records the platform call boundary; does not claim physical vibration. */
    private class RecordingView(context: Context) : View(context) {
        val requests = mutableListOf<Pair<Int, Int>>()
        override fun performHapticFeedback(feedbackConstant: Int, flags: Int): Boolean {
            requests.add(feedbackConstant to flags)
            return true
        }
    }
}
