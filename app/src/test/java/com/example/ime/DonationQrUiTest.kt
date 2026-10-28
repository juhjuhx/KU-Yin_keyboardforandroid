package com.example.ime

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import com.example.ime.ui.OPEN_SOURCE_FOUNDATIONS
import com.example.ime.ui.OpenSourceDonationCard
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.nio.file.Files

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h900dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class DonationQrUiTest {
    @get:Rule val rule = createComposeRule()

    private fun show(density: Float = 1f, fontScale: Float = 1f) {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                MaterialTheme {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        OpenSourceDonationCard()
                    }
                }
            }
        }
    }

    private fun select(index: Int) {
        val selected = rule.onNodeWithTag("chip_foundation_${DonationQrOracle.urls.keys.elementAt(index)}")
        try {
            // Bring the LazyRow into the outer vertical viewport before moving its horizontal page.
            // performScrollTo on a chip only reaches its nearest (horizontal) scrollable ancestor.
            rule.onNode(hasScrollToIndexAction()).performScrollTo().assertIsDisplayed()
                .performScrollToIndex(index)
            selected.assertIsDisplayed().performClick().assertIsSelected()
        } catch (error: AssertionError) {
            System.getenv("KUYIN_QR_EVIDENCE_DIR")?.let { directory ->
                File(directory).mkdirs()
                rule.onRoot().captureRoboImage(
                    filePath = File(directory, "selection-failure-$index.png").absolutePath,
                    roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record)
                )
                File(directory, "selection-failure-$index.txt").writeText(rule.onRoot().printToString())
            }
            throw error
        }
    }

    private fun capture(id: String, label: String): Bitmap {
        val external = System.getenv("KUYIN_QR_EVIDENCE_DIR")?.let(::File)
        val directory = external ?: Files.createTempDirectory("kuyin-qr-").toFile()
        directory.mkdirs()
        val file = File(directory, "$label-$id.png")
        rule.onNodeWithTag("qr_code_$id").performScrollTo().captureRoboImage(
            filePath = file.absolutePath,
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record)
        )
        val bitmap = requireNotNull(BitmapFactory.decodeFile(file.absolutePath))
        if (external == null) { file.delete(); directory.delete() }
        return bitmap
    }

    private fun assertAllDisplayed(density: Float = 1f, fontScale: Float = 1f, label: String) {
        show(density, fontScale)
        DonationQrOracle.urls.entries.forEachIndexed { index, (id, url) ->
            select(index)
            val bitmap = capture(id, label)
            assertTrue("Capture must contain real modules", bitmap.width > 100 && bitmap.height > 100)
            DonationQrOracle.assertDisplayedQuietZone(bitmap, id)
            assertEquals(url, DonationQrOracle.decode(bitmap).text)
            rule.onNodeWithContentDescription("${OPEN_SOURCE_FOUNDATIONS[index].name}官方捐款網址 QR Code")
                .assertExists()
        }
    }

    @Test fun `displayed foundation QR actually decodes to its official URL`() =
        assertAllDisplayed(label = "360-mdpi")

    @Test @Config(qualifiers = "w320dp-h900dp-xhdpi")
    fun `four displayed codes decode at narrow width density two and large font`() =
        assertAllDisplayed(density = 2f, fontScale = 1.3f, label = "320-xhdpi-font130")

    @Test @Config(qualifiers = "w600dp-h360dp-land-xxhdpi")
    fun `four displayed codes decode in landscape density three and double font`() =
        assertAllDisplayed(density = 3f, fontScale = 2f, label = "600-land-xxhdpi-font200")

    @Test fun `foundation destinations are the four canonical official endpoints`() {
        assertEquals(DonationQrOracle.urls, OPEN_SOURCE_FOUNDATIONS.associate { it.id to it.donateUrl })
        assertEquals(4, OPEN_SOURCE_FOUNDATIONS.size)
    }

    @Test fun `selected foundation copy and browser actions use the same exact URL`() {
        show()
        val app = ApplicationProvider.getApplicationContext<Application>()
        DonationQrOracle.urls.entries.forEachIndexed { index, (_, url) ->
            select(index)
            rule.onNodeWithTag("btn_copy_donate_url").performScrollTo().performClick()
            val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            assertEquals(url, clipboard.primaryClip?.getItemAt(0)?.text.toString())
            rule.onNodeWithTag("btn_open_donate_url").performScrollTo().performClick()
            val intent = shadowOf(app).nextStartedActivity
            assertNotNull("User action must actually launch the browser intent", intent)
            assertEquals(Intent.ACTION_VIEW, intent.action)
            assertEquals(url, intent.dataString)
            assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        }
    }

    @Test fun `unverified phone scanning and foundation endorsement are not claimed`() {
        show()
        rule.onNodeWithText("手機相機掃描尚未驗證；可複製或開啟官方網址。")
            .performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("本輸入法作者不收取任何個人贊助。若您肯定開源軟體價值，請點擊或掃描下方 QR Code 捐款至國際開源組織！")
            .assertExists()
        rule.onNodeWithText("📱 可使用另一支手機相機掃描上述 QR Code").assertDoesNotExist()
    }
}
