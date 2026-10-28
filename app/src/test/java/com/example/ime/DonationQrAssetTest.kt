package com.example.ime

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.MessageDigest

/** Independent decoder, deliberately separate from the offline OpenCV encoder. */
internal object DonationQrOracle {
    val urls = linkedMapOf(
        "fsf" to "https://my.fsf.org/donate",
        "asf" to "https://donate.apache.org/",
        "osi" to "https://opensource.org/donate",
        "eff" to "https://eff.org/donate"
    )

    fun decode(bitmap: Bitmap): Result {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return QRCodeReader().decode(BinaryBitmap(HybridBinarizer(
            RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        )))
    }

    fun assertDisplayedQuietZone(bitmap: Bitmap, id: String) {
        val black = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            if (bitmap.getPixel(x, y) == -0x1000000) black += x to y
        }
        assertTrue("Rendered symbol must contain black modules", black.isNotEmpty())
        val minX = black.minOf { it.first }
        val minY = black.minOf { it.second }
        val maxX = black.maxOf { it.first }
        val maxY = black.maxOf { it.second }
        val modules = if (id != "osi") 25 else 29
        val scale = (maxX - minX + 1) / modules
        assertTrue("Each module needs integral display pixels", scale > 0)
        assertEquals(modules * scale, maxX - minX + 1)
        assertEquals(modules * scale, maxY - minY + 1)
        val left = minX - 4 * scale
        val top = minY - 4 * scale
        val right = maxX + 4 * scale
        val bottom = maxY + 4 * scale
        assertTrue(left >= 0 && top >= 0 && right < bitmap.width && bottom < bitmap.height)
        for (y in top..bottom) for (x in left..right) {
            if (x < minX || y < minY || x > maxX || y > maxY) {
                assertEquals("Quiet-zone corner/edge must remain fully white ($id at $x,$y)", -1,
                    bitmap.getPixel(x, y))
            }
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DonationQrAssetTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun `four packaged drawables roundtrip with M correction and four module quiet zone`() {
        DonationQrOracle.urls.forEach { (id, url) ->
            val resource = context.resources.getIdentifier("foundation_qr_$id", "drawable", context.packageName)
            assertNotEquals("Missing offline standard QR for $id", 0, resource)
            val bitmap = BitmapFactory.decodeResource(context.resources, resource)
            assertEquals(bitmap.width, bitmap.height)
            val expectedModules = if (id != "osi") 25 else 29
            assertEquals((expectedModules + 8) * 8, bitmap.width)
            for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                assertTrue("Opaque black or white only", pixel == -1 || pixel == -0x1000000)
                if (x < 32 || y < 32 || x >= bitmap.width - 32 || y >= bitmap.height - 32)
                    assertEquals("Four-module white quiet zone", -1, pixel)
            }
            val result = DonationQrOracle.decode(bitmap)
            assertEquals(url, result.text)
            assertEquals("M", result.resultMetadata[ResultMetadataType.ERROR_CORRECTION_LEVEL])
            // One damaged bottom-right data module, away from finder/timing patterns.
            val damaged = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val origin = 32 + (expectedModules - 1) * 8
            val inverse = if (damaged.getPixel(origin, origin) == -1) -0x1000000 else -1
            for (y in origin until origin + 8) for (x in origin until origin + 8)
                damaged.setPixel(x, y, inverse)
            assertEquals("Isolated one-module error remains decodable", url, DonationQrOracle.decode(damaged).text)
        }
    }

    @Test fun `provenance manifest pins exact packaged PNG source hashes and official identities`() {
        val manifest = JSONObject(context.assets.open("foundation_qr_manifest.json").bufferedReader().use { it.readText() })
        assertEquals(1, manifest.getInt("schema_version"))
        assertEquals(4, manifest.getInt("quiet_zone_modules"))
        val entries = manifest.getJSONArray("foundations")
        assertEquals(4, entries.length())
        val seen = mutableSetOf<String>()
        for (index in 0 until entries.length()) {
            val entry = entries.getJSONObject(index)
            val id = entry.getString("id")
            assertTrue("Duplicate manifest id", seen.add(id))
            assertEquals(DonationQrOracle.urls[id], entry.getString("url"))
            assertEquals("foundation_qr_$id", entry.getString("drawable"))
            val resource = context.resources.getIdentifier(entry.getString("drawable"), "drawable", context.packageName)
            val bytes = context.resources.openRawResource(resource).use { it.readBytes() }
            assertEquals(entry.getString("sha256"), MessageDigest.getInstance("SHA-256").digest(bytes)
                .joinToString("") { "%02x".format(it) })
        }
        assertEquals(DonationQrOracle.urls.keys, seen)
    }
}
