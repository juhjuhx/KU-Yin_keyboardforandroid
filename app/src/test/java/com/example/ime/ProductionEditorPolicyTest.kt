package com.example.ime

import android.content.Context
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.test.core.app.ApplicationProvider
import com.example.androidkeyboard.input.EditorPolicy
import com.example.ime.engine.KeyboardMode
import com.example.ime.engine.KuYinEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductionEditorPolicyTest {

    private lateinit var context: Context
    private lateinit var engine: KuYinEngine

    private fun normalPolicy(): EditorPolicy = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
        EditorInfo.IME_ACTION_DONE
    )

    private fun noLearningPolicy(): EditorPolicy = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
        EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
    )

    private fun passwordPolicy(): EditorPolicy = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
        EditorInfo.IME_ACTION_DONE
    )

    private fun visiblePasswordPolicy(): EditorPolicy = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
        EditorInfo.IME_ACTION_DONE
    )

    private fun webPasswordPolicy(): EditorPolicy = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
        EditorInfo.IME_ACTION_DONE
    )

    private fun numericPasswordPolicy(): EditorPolicy = EditorPolicy.from(
        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD,
        EditorInfo.IME_ACTION_DONE
    )

    private fun forceAsciiPolicy(): EditorPolicy = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
        EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_FORCE_ASCII
    )

    private fun learnedKeys(): Set<String> {
        val prefs = context.getSharedPreferences("kuyin_dict_prefs", Context.MODE_PRIVATE)
        return prefs.all.keys.filter { it.startsWith("freq_") }.toSet()
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        engine = KuYinEngine(context)
    }

    @Test
    fun `normal editor allows learning`() {
        engine.applyPolicy(normalPolicy())
        engine.onZhuyinKey("ㄋ") {}
        engine.onZhuyinKey("ㄧ") {}
        engine.onZhuyinKey("ˇ") {}
        val first = engine.candidates.value.first()
        engine.selectCandidate(first) {}
        assertTrue(learnedKeys().isNotEmpty())
    }

    @Test
    fun `no-learning editor selection does not mutate frequency`() {
        engine.applyPolicy(noLearningPolicy())
        engine.onZhuyinKey("ㄋ") {}
        engine.onZhuyinKey("ㄧ") {}
        engine.onZhuyinKey("ˇ") {}
        val before = engine.candidates.value
        assertTrue(before.isNotEmpty())
        engine.selectCandidate(before.first()) {}
        assertTrue(learnedKeys().isEmpty())
        assertTrue(engine.candidates.value.isEmpty())
    }

    @Test
    fun `text password disables learning`() {
        engine.applyPolicy(passwordPolicy())
        engine.onZhuyinKey("ㄋ") {}
        assertEquals("", engine.composingZhuyin.value)
        assertTrue(engine.candidates.value.isEmpty())
        assertTrue(learnedKeys().isEmpty())
    }

    @Test
    fun `visible password disables learning`() {
        engine.applyPolicy(visiblePasswordPolicy())
        engine.onZhuyinKey("ㄋ") {}
        assertEquals("", engine.composingZhuyin.value)
        assertTrue(learnedKeys().isEmpty())
    }

    @Test
    fun `web password disables learning`() {
        engine.applyPolicy(webPasswordPolicy())
        engine.onZhuyinKey("ㄋ") {}
        assertEquals("", engine.composingZhuyin.value)
        assertTrue(learnedKeys().isEmpty())
    }

    @Test
    fun `numeric password disables learning`() {
        engine.applyPolicy(numericPasswordPolicy())
        engine.onZhuyinKey("ㄋ") {}
        assertEquals("", engine.composingZhuyin.value)
        assertTrue(learnedKeys().isEmpty())
    }

    @Test
    fun `force ASCII cannot return to Zhuyin composition`() {
        engine.applyPolicy(forceAsciiPolicy())
        engine.setMode(KeyboardMode.ZHUYIN)
        engine.onZhuyinKey("ㄋ") {}
        assertEquals("", engine.composingZhuyin.value)
        assertTrue(engine.candidates.value.isEmpty())
    }
}
