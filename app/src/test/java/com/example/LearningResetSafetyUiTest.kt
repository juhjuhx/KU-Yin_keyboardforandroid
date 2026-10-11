package com.example

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ime.engine.ZhuyinDictionary
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class LearningResetSafetyUiTest {
    @get:Rule val rule = createComposeRule()
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun showSettings() {
        rule.setContent { MyApplicationTheme { KuYinSetupScreen() } }
        rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasTestTag("btn_reset_dictionary"))
    }

    @Test fun `complete learning reset is unavailable rather than falsely successful`() {
        showSettings()
        rule.onNodeWithTag("btn_reset_dictionary").assertIsNotEnabled()
        rule.onNodeWithText("尚未支援安全重設原生與備援學習紀錄；目前不會刪除任何資料。")
            .assertExists()
        rule.onNodeWithTag("btn_reset_dictionary").performClick()
        rule.onNodeWithText("確定重設").assertDoesNotExist()
        rule.onNodeWithText("已成功重設字詞學習紀錄").assertDoesNotExist()
    }

    @Test fun `old reset entry cannot partially erase learning or settings or files`() {
        val fallback = context.getSharedPreferences("kuyin_dict_prefs", Context.MODE_PRIVATE)
        fallback.edit().putInt("freq_我", 7).commit()
        val settings = context.getSharedPreferences("kuyin_settings", Context.MODE_PRIVATE)
        settings.edit().putInt("pref_keyboard_theme_index", 3).commit()
        val native = File(context.noBackupFilesDir, "libchewing/user/userdict.dat")
        native.parentFile!!.mkdirs()
        native.writeText("synthetic-native-fixture")
        val unrelated = File(context.filesDir, "unrelated-reset-fixture").apply { writeText("retain") }
        showSettings()
        rule.onNodeWithTag("btn_reset_dictionary").performClick()
        // Exercise the original destructive UI path if it is still reachable.
        if (rule.onAllNodesWithText("確定重設").fetchSemanticsNodes().isNotEmpty())
            rule.onNodeWithText("確定重設").performClick()
        rule.runOnIdle {
            assertEquals(7, fallback.getInt("freq_我", 0))
            assertEquals(3, settings.getInt("pref_keyboard_theme_index", 0))
            assertEquals("synthetic-native-fixture", native.readText())
            assertEquals("retain", unrelated.readText())
        }
    }

    @Test fun `fallback only reset API still clears its own test store`() {
        val fallback = context.getSharedPreferences("kuyin_dict_prefs", Context.MODE_PRIVATE)
        fallback.edit().putInt("freq_我", 7).commit()
        val settings = context.getSharedPreferences("kuyin_settings", Context.MODE_PRIVATE)
        settings.edit().putInt("pref_keyboard_theme_index", 3).commit()
        ZhuyinDictionary(context).clearLearnedFrequencies()
        assertEquals(0, fallback.getInt("freq_我", 0))
        assertEquals(3, settings.getInt("pref_keyboard_theme_index", 0))
    }
}
