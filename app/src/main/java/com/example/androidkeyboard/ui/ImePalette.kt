package com.example.androidkeyboard.ui

import android.content.Context
import android.content.res.Configuration

/** Small color palette shared by the IME's custom-drawn views. */
data class ImePalette(
    val surface: Int,
    val key: Int,
    val keyPressed: Int,
    val text: Int,
    val textPressed: Int,
    val accent: Int,
) {
    companion object {
        fun from(context: Context): ImePalette {
            val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
                ImePalette(
                    surface = 0xFF171717.toInt(),
                    key = 0xFF2A2A2A.toInt(),
                    keyPressed = 0xFF454545.toInt(),
                    text = 0xFFF3F4F6.toInt(),
                    textPressed = 0xFFFFFFFF.toInt(),
                    accent = 0xFF7DD3FC.toInt(),
                )
            } else {
                ImePalette(
                    surface = 0xFFF7F7F8.toInt(),
                    key = 0xFFECEDEF.toInt(),
                    keyPressed = 0xFFD1D5DB.toInt(),
                    text = 0xFF202124.toInt(),
                    textPressed = 0xFF111827.toInt(),
                    accent = 0xFF0369A1.toInt(),
                )
            }
        }
    }
}
