package com.example.androidkeyboard.engines.opencc

import com.example.androidkeyboard.engines.core.ChineseConverter.Profile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenCCConverterTest {

    @Test
    fun passThroughStubDoesNotReportReadyAfterInit() {
        val converter = OpenCCConverter()

        converter.init(Profile.S2TW, Profile.TW2S)

        assertFalse("pass-through OpenCC stub must not advertise a working backend", converter.isReady)
    }

    @Test
    fun unavailableBackendCannotBecomeEnabled() {
        val converter = OpenCCConverter()
        converter.init(Profile.S2TW, Profile.TW2S)

        converter.enabled = true

        assertFalse("an unavailable conversion backend cannot be enabled", converter.enabled)
        assertEquals("台灣", converter.simplifyToTraditional("台灣"))
    }
}
