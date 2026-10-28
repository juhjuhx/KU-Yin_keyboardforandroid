package com.example.feedback

import android.app.Application
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.KuYinSetupScreen
import com.example.feedback.data.AppDatabase
import com.example.feedback.data.FeedbackDao
import com.example.feedback.data.FeedbackEntity
import com.example.feedback.data.FeedbackRepository
import com.example.feedback.ui.FeedbackViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class FeedbackDeletionUiTest {
    @get:Rule val rule = createComposeRule()

    private fun showHistory(failure: Exception? = null): CompletableDeferred<Unit> {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val dao = AppDatabase.getDatabase(app).feedbackDao()
        runBlocking {
            dao.clearAllFeedback()
            dao.insertFeedback(FeedbackEntity(
                category = "BUG", title = "synthetic-delete", description = "synthetic only",
                userEmail = "", rating = 3, appVersion = "test", androidVersion = "test",
                deviceModel = "Robolectric", imeStatus = "test"
            ))
        }
        val release = CompletableDeferred<Unit>()
        val delayedDao = object : FeedbackDao by dao {
            override suspend fun deleteFeedbackById(id: Int) {
                release.await()
                failure?.let { throw it }
                dao.deleteFeedbackById(id)
            }
            override suspend fun clearAllFeedback() {
                release.await()
                failure?.let { throw it }
                dao.clearAllFeedback()
            }
        }
        val vm = FeedbackViewModel(app, FeedbackRepository(delayedDao))
        rule.setContent { MyApplicationTheme { KuYinSetupScreen(feedbackViewModel = vm) } }
        rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasTestTag("btn_open_feedback_history"))
        rule.onNodeWithTag("btn_open_feedback_history").performClick()
        rule.waitUntil { rule.onAllNodesWithText("synthetic-delete").fetchSemanticsNodes().isNotEmpty() }
        return release
    }

    @Test fun `single delete does not announce success before DAO completion`() {
        val release = showHistory()
        try {
            rule.onNodeWithContentDescription("刪除此筆回饋").performClick()
            rule.onNodeWithText("已刪除該筆回饋").assertDoesNotExist()
            rule.onNodeWithText("synthetic-delete").assertExists()
        } finally {
            release.complete(Unit)
        }
        rule.waitUntil { rule.onAllNodesWithText("synthetic-delete").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithText("已刪除該筆回饋").assertExists()
    }

    @Test fun `clear all does not announce success before DAO completion`() {
        val release = showHistory()
        try {
            rule.onNodeWithText("清空全部").performClick()
            rule.onNodeWithText("確定清空").performClick()
            rule.onNodeWithText("已清空所有回饋紀錄").assertDoesNotExist()
            rule.onNodeWithText("synthetic-delete").assertExists()
        } finally {
            release.complete(Unit)
        }
        rule.waitUntil { rule.onAllNodesWithText("synthetic-delete").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithText("已清空所有回饋紀錄").assertExists()
    }
    @Test fun `single database failure keeps list and never displays success`() {
        val release = showHistory(IllegalStateException("synthetic fault"))
        rule.onNodeWithContentDescription("刪除此筆回饋").performClick()
        release.complete(Unit)
        rule.waitUntil { rule.onAllNodesWithText("刪除失敗，請稍後再試").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("已刪除該筆回饋").assertDoesNotExist()
        rule.onNodeWithText("synthetic-delete").assertExists()
    }

    @Test fun `clear database failure keeps list and never displays success`() {
        val release = showHistory(IllegalStateException("synthetic fault"))
        rule.onNodeWithText("清空全部").performClick()
        rule.onNodeWithText("確定清空").performClick()
        release.complete(Unit)
        rule.waitUntil { rule.onAllNodesWithText("刪除失敗，請稍後再試").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("已清空所有回饋紀錄").assertDoesNotExist()
        rule.onNodeWithText("synthetic-delete").assertExists()
    }

}
