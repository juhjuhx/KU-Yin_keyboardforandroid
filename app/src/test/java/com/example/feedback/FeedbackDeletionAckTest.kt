package com.example.feedback

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import com.example.feedback.data.FeedbackDao
import com.example.feedback.data.FeedbackEntity
import com.example.feedback.data.FeedbackRepository
import com.example.feedback.ui.FeedbackViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FeedbackDeletionAckTest {
    private val dispatcher = StandardTestDispatcher()
    private val models = mutableListOf<FeedbackViewModel>()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() {
        models.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    private class FaultDao : FeedbackDao {
        val rows = MutableStateFlow(listOf(1, 2))
        val release = CompletableDeferred<Unit>()
        var failure: Exception? = null
        var calls = 0
        override fun getAllFeedback(): Flow<List<FeedbackEntity>> = rows.map { emptyList() }
        override fun getFeedbackByCategory(category: String) = getAllFeedback()
        override suspend fun insertFeedback(feedback: FeedbackEntity): Long = error("not used")
        private suspend fun awaitResult() {
            calls++
            release.await()
            failure?.let { throw it }
        }
        override suspend fun deleteFeedbackById(id: Int) {
            awaitResult()
            rows.value = rows.value.filterNot { it == id }
        }
        override suspend fun clearAllFeedback() {
            awaitResult()
            rows.value = emptyList()
        }
    }

    private fun model(dao: FeedbackDao) = FeedbackViewModel(
        ApplicationProvider.getApplicationContext(), FeedbackRepository(dao)
    ).also { models += it }

    @Test fun `single success follows completion once and duplicate pending taps are ignored`() = runTest(dispatcher) {
        val dao = FaultDao(); val vm = model(dao); val events = mutableListOf<String>()
        repeat(2) { vm.deleteFeedback(1, { events += "success" }, { events += "failure" }) }
        runCurrent()
        assertEquals(1, dao.calls); assertTrue(events.isEmpty()); assertEquals(listOf(1, 2), dao.rows.value)
        dao.release.complete(Unit); runCurrent()
        assertEquals(listOf("success"), events); assertEquals(listOf(2), dao.rows.value)
    }

    @Test fun `clear success follows completion once and does not overlap single deletion`() = runTest(dispatcher) {
        val dao = FaultDao(); val vm = model(dao); val events = mutableListOf<String>()
        repeat(2) { vm.clearAllFeedback({ events += "clear" }, { events += "failure" }) }
        vm.deleteFeedback(1, { events += "single" }, { events += "failure" })
        runCurrent(); assertTrue(events.isEmpty()); assertEquals(1, dao.calls)
        dao.release.complete(Unit); runCurrent()
        assertEquals(listOf("clear"), events); assertTrue(dao.rows.value.isEmpty())
    }

    @Test fun `single failure preserves list reports no success and permits explicit retry`() = runTest(dispatcher) {
        val dao = FaultDao().apply { failure = IllegalStateException("synthetic-private-detail") }
        val vm = model(dao); val events = mutableListOf<String>()
        dao.release.complete(Unit)
        vm.deleteFeedback(1, { events += "success" }, { events += it }); runCurrent()
        assertEquals(listOf("刪除失敗，請稍後再試"), events); assertEquals(listOf(1, 2), dao.rows.value)
        dao.failure = null
        vm.deleteFeedback(1, { events += "success" }, { events += it }); runCurrent()
        assertEquals(listOf("刪除失敗，請稍後再試", "success"), events); assertEquals(listOf(2), dao.rows.value)
    }

    @Test fun `clear failure preserves list and never announces success`() = runTest(dispatcher) {
        val dao = FaultDao().apply { failure = IllegalArgumentException("synthetic error") }
        val vm = model(dao); var successes = 0; val errors = mutableListOf<String>()
        vm.clearAllFeedback({ successes++ }, { errors += it }); runCurrent()
        assertTrue(errors.isEmpty()); assertEquals(listOf(1, 2), dao.rows.value)
        dao.release.complete(Unit); runCurrent()
        assertEquals(0, successes); assertEquals(1, errors.size); assertEquals(listOf(1, 2), dao.rows.value)
    }

    @Test fun `cancellation is not a database error or success and releases pending guard`() = runTest(dispatcher) {
        val dao = FaultDao().apply { failure = CancellationException("synthetic cancellation") }
        val vm = model(dao); val events = mutableListOf<String>(); dao.release.complete(Unit)
        vm.deleteFeedback(1, { events += "success" }, { events += "failure" }); runCurrent()
        assertTrue(events.isEmpty()); assertEquals(listOf(1, 2), dao.rows.value)
        dao.failure = null
        vm.deleteFeedback(1, { events += "success" }, { events += "failure" }); runCurrent()
        assertEquals(listOf("success"), events)
    }

    @Test fun `Application only constructor remains available to Android factory`() {
        assertNotNull(FeedbackViewModel::class.java.getConstructor(Application::class.java))
    }
}
