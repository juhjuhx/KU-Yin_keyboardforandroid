package com.example.ime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33, 36], qualifiers = "night")
class NightThemeCompatibilityTest {
    @Test
    fun `night style retains dark system bars and surface after version qualification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val theme = context.resources.newTheme().apply {
            applyStyle(R.style.Theme_AndroidKeyboard, true)
        }
        val attributes = theme.obtainStyledAttributes(intArrayOf(
            android.R.attr.navigationBarColor,
            android.R.attr.windowLightNavigationBar,
            android.R.attr.windowLightStatusBar,
            com.google.android.material.R.attr.colorSurface,
        ))
        try {
            val darkBackground = context.getColor(R.color.keyboard_bg_dark)
            assertEquals(darkBackground, attributes.getColor(0, 0))
            assertFalse(attributes.getBoolean(1, true))
            assertFalse(attributes.getBoolean(2, true))
            assertEquals(darkBackground, attributes.getColor(3, 0))
        } finally {
            attributes.recycle()
        }
    }
}
