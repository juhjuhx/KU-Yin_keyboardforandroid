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

    private fun showHistory(): CompletableDeferred<Unit> {
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
                dao.deleteFeedbackById(id)
            }
            override suspend fun clearAllFeedback() {
                release.await()
                dao.clearAllFeedback()
            }
        }
        val vm = FeedbackViewModel(app)
        // Baseline has no injection constructor. Test-only seam to reproduce its actual UI flow.
        FeedbackViewModel::class.java.getDeclaredField("repository").apply {
            isAccessible = true
            set(vm, FeedbackRepository(delayedDao))
        }
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
    }
}
