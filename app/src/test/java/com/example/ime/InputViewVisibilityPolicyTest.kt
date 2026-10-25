package com.example.ime

import android.content.Context
import android.content.res.Configuration
import android.os.Looper
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

/** Existing always-show policy, not proof of Android window visibility or binding. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33, 36])
class InputViewVisibilityPolicyTest {
    private lateinit var controller: ServiceController<KuYinInputMethodService>

    @Before
    fun startIsolatedService() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Robolectric sandbox only; never writes settings on a device.
        Settings.Secure.putInt(context.contentResolver, "show_ime_with_hard_keyboard", 0)
        controller = Robolectric.buildService(KuYinInputMethodService::class.java).create()
    }

    @After
    fun closeIsolatedService() {
        controller.destroy()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `exposed hardware keyboard does not suppress existing software input policy`() {
        val resources = controller.get().resources
        val config = Configuration(resources.configuration).apply {
            keyboard = Configuration.KEYBOARD_QWERTY
            hardKeyboardHidden = Configuration.HARDKEYBOARDHIDDEN_NO
        }
        resources.updateConfiguration(config, resources.displayMetrics)
        assertEquals(Configuration.KEYBOARD_QWERTY, config.keyboard)
        assertEquals(Configuration.HARDKEYBOARDHIDDEN_NO, config.hardKeyboardHidden)
        assertTrue(controller.get().onEvaluateInputViewShown())
    }

    @Test
    fun `no hardware keyboard preserves existing software input policy`() {
        val resources = controller.get().resources
        val config = Configuration(resources.configuration).apply {
            keyboard = Configuration.KEYBOARD_NOKEYS
            hardKeyboardHidden = Configuration.HARDKEYBOARDHIDDEN_YES
        }
        resources.updateConfiguration(config, resources.displayMetrics)
        assertTrue(controller.get().onEvaluateInputViewShown())
    }
}
