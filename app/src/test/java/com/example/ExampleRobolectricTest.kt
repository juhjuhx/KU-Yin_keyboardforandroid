package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ime.engine.KuYinEngine
import com.example.ime.engine.ZhuyinDictionary
import androidx.room.Room
import com.example.feedback.data.AppDatabase
import com.example.feedback.data.FeedbackEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("KU-Yin 酷音鍵盤", appName)
  }

  @Test
  fun `test zhuyin dictionary query`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dict = ZhuyinDictionary(context)

    // 測試詞庫查詢
    val niHaoResults = dict.query("ㄋㄧˇㄏㄠˇ")
    assertTrue(niHaoResults.contains("你好"))

    val kuYinResults = dict.query("ㄎㄨˋㄧㄣ")
    assertTrue(kuYinResults.contains("酷音"))

    // 測試單字查詢 (例如 ㄋㄧˇ -> 你)
    val niResults = dict.query("ㄋㄧˇ")
    assertTrue(niResults.contains("你"))
  }

  @Test
  fun `test kuyin engine composing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = KuYinEngine(context)

    var committed = ""
    engine.onZhuyinKey("ㄋ") { committed = it }
    engine.onZhuyinKey("ㄧ") { committed = it }
    engine.onZhuyinKey("ˇ") { committed = it }

    assertEquals("ㄋㄧˇ", engine.composingZhuyin.value)
    assertTrue(engine.candidates.value.contains("你"))

    // 按空白鍵選取第一候選字 (例如智慧前綴聯想詞或高頻候選字)
    val expectedFirst = engine.candidates.value.first()
    engine.onSpace { committed = it }
    assertEquals(expectedFirst, committed)
    assertEquals("", engine.composingZhuyin.value)

    // 測試精確選取指定候選字
    engine.onZhuyinKey("ㄋ") { committed = it }
    engine.onZhuyinKey("ㄧ") { committed = it }
    engine.onZhuyinKey("ˇ") { committed = it }
    engine.selectCandidate("你") { committed = it }
    assertEquals("你", committed)
    assertEquals("", engine.composingZhuyin.value)
  }

  @Test
  fun `test feedback entity structured output and room database`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val dao = db.feedbackDao()

    val item = FeedbackEntity(
      category = "BUG",
      title = "無法輸入輕聲",
      description = "在某些特定輸入框中，輕聲符號無法正常組字",
      userEmail = "tester@example.com",
      rating = 4,
      appVersion = "v1.0 (1)",
      androidVersion = "Android 14 (API 34)",
      deviceModel = "Google Pixel 8",
      imeStatus = "已啟用且為預設輸入法"
    )

    // 驗證結構化 JSON 與報表
    val json = item.toStructuredJson()
    assertTrue(json.contains("\"category\": \"BUG\""))
    assertTrue(json.contains("無法輸入輕聲"))
    assertTrue(json.contains("Google Pixel 8"))

    val report = item.toStructuredReport()
    assertTrue(report.contains("KU-YIN 酷音輸入法 - 使用者回饋"))
    assertTrue(report.contains("類別: 問題回報 (Bug)"))
    assertTrue(report.contains("4 ★"))

    // 驗證 Room 資料庫操作
    val id = dao.insertFeedback(item)
    assertTrue(id > 0)

    val list = dao.getAllFeedback().first()
    assertEquals(1, list.size)
    assertEquals("無法輸入輕聲", list[0].title)

    val bugList = dao.getFeedbackByCategory("BUG").first()
    assertEquals(1, bugList.size)

    val featureList = dao.getFeedbackByCategory("FEATURE").first()
    assertEquals(0, featureList.size)

    dao.deleteFeedbackById(list[0].id)
    assertEquals(0, dao.getAllFeedback().first().size)

    db.close()
  }

  @Test
  fun `test keyboard themes and settings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val settings = com.example.ime.settings.KeyboardSettings(context)

    assertEquals(6, com.example.ime.ui.KEYBOARD_THEMES.size)
    assertEquals("經典深灰", com.example.ime.ui.KEYBOARD_THEMES[0].name)
    assertEquals("曜石極黑", com.example.ime.ui.KEYBOARD_THEMES[1].name)
    assertEquals("皓雪純白", com.example.ime.ui.KEYBOARD_THEMES[2].name)

    settings.keyboardThemeIndex = 3
    assertEquals(3, settings.keyboardThemeIndex)

    settings.isClipboardBarEnabled = false
    assertEquals(false, settings.isClipboardBarEnabled)
    settings.isClipboardBarEnabled = true
    assertEquals(true, settings.isClipboardBarEnabled)
  }

  @Test
  fun `test open source foundation donation list`() {
    val foundations = com.example.ime.ui.OPEN_SOURCE_FOUNDATIONS
    assertTrue(foundations.isNotEmpty())
    val fsf = foundations.find { it.id == "fsf" }
    assertTrue(fsf != null)
    assertTrue(fsf!!.donateUrl.startsWith("https://"))
    val asf = foundations.find { it.id == "asf" }
    assertTrue(asf != null)
    assertTrue(asf!!.donateUrl.startsWith("https://"))
  }

  @Test
  fun `test upstream sync manager URLs and state`() {
    assertEquals("juhjuhx/KU-Yin_keyboardforandroid", com.example.ime.sync.UpstreamSyncManager.GITHUB_REPO)
    assertEquals("https://github.com/juhjuhx/KU-Yin_keyboardforandroid", com.example.ime.sync.UpstreamSyncManager.REPO_URL)
    assertEquals("https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues", com.example.ime.sync.UpstreamSyncManager.ISSUES_URL)

    val state = com.example.ime.sync.UpstreamSyncManager.syncState.value
    assertEquals("1.0.0", state.currentVersion)
    assertTrue(state.totalLexiconWords >= 1250)
    assertTrue(state.lexiconIntegrityVerified)
  }

  @Test
  fun `test decoupled IKuYinEngine interface`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine: com.example.ime.api.IKuYinEngine = com.example.ime.engine.KuYinEngine(context)

    assertEquals(com.example.ime.engine.KeyboardMode.ZHUYIN, engine.mode.value)
    engine.setMode(com.example.ime.engine.KeyboardMode.ENGLISH)
    assertEquals(com.example.ime.engine.KeyboardMode.ENGLISH, engine.mode.value)

    var committed = ""
    engine.onEnglishKey("h") { committed = it }
    assertEquals("h", committed)
  }
}

