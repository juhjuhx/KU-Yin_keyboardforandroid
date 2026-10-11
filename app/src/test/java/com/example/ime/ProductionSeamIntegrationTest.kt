package com.example.ime

import android.content.Context
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.test.core.app.ApplicationProvider
import com.example.androidkeyboard.input.EditorPolicy
import com.example.ime.engine.KeyboardMode
import com.example.ime.engine.KuYinEngine
import com.example.ime.settings.KeyboardSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductionSeamIntegrationTest {

    private lateinit var context: Context
    private lateinit var engine: KuYinEngine
    private lateinit var settings: KeyboardSettings

    private fun policyFor(inputType: Int, imeOptions: Int): EditorPolicy =
        EditorPolicy.from(inputType, imeOptions)

    private fun learnedKeys(): Set<String> {
        val prefs = context.getSharedPreferences("kuyin_dict_prefs", Context.MODE_PRIVATE)
        return prefs.all.keys.filter { it.startsWith("freq_") }.toSet()
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        engine = KuYinEngine(context)
        settings = KeyboardSettings(context)
    }

    @Test
    fun `normal editor full cycle types selects and learns`() {
        engine.applyPolicy(policyFor(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
            EditorInfo.IME_ACTION_DONE
        ))
        engine.onZhuyinKey("ㄋ") {}
        engine.onZhuyinKey("ㄧ") {}
        engine.onZhuyinKey("ˇ") {}
        assertEquals("ㄋㄧˇ", engine.composingZhuyin.value)
        val first = engine.candidates.value.first()
        var committed = ""
        engine.selectCandidate(first) { committed = it }
        assertEquals(first, committed)
        assertEquals("", engine.composingZhuyin.value)
        assertTrue(learnedKeys().isNotEmpty())
        assertTrue(settings.isVibrateEnabled)
    }

    @Test
    fun `password editor stays silent end to end`() {
        engine.applyPolicy(policyFor(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            EditorInfo.IME_ACTION_DONE
        ))
        engine.setMode(KeyboardMode.ENGLISH)
        engine.onZhuyinKey("ㄋ") {}
        engine.onZhuyinKey("ㄧ") {}
        assertEquals("", engine.composingZhuyin.value)
        assertTrue(engine.candidates.value.isEmpty())
        assertTrue(learnedKeys().isEmpty())
    }

    @Test
    fun `force ascii editor never accumulates composing`() {
        engine.applyPolicy(policyFor(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
            EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_FORCE_ASCII
        ))
        engine.setMode(KeyboardMode.ENGLISH)
        engine.onZhuyinKey("ㄈ") {}
        engine.onBackspace {}
        assertEquals("", engine.composingZhuyin.value)
        assertTrue(learnedKeys().isEmpty())
    }
}
