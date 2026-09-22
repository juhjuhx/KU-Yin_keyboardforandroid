package com.example.feedback.data

import kotlinx.coroutines.flow.Flow

class FeedbackRepository(private val feedbackDao: FeedbackDao) {

    val allFeedback: Flow<List<FeedbackEntity>> = feedbackDao.getAllFeedback()

    fun getFeedbackByCategory(category: String): Flow<List<FeedbackEntity>> {
        return feedbackDao.getFeedbackByCategory(category)
    }

    suspend fun insertFeedback(feedback: FeedbackEntity): Long {
        return feedbackDao.insertFeedback(feedback)
    }

    suspend fun deleteFeedbackById(id: Int) {
        feedbackDao.deleteFeedbackById(id)
    }

    suspend fun clearAllFeedback() {
        feedbackDao.clearAllFeedback()
    }
}
