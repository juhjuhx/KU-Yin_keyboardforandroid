package com.example.feedback.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackDao {

    @Query("SELECT * FROM feedback_entries ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<FeedbackEntity>>

    @Query("SELECT * FROM feedback_entries WHERE category = :category ORDER BY timestamp DESC")
    fun getFeedbackByCategory(category: String): Flow<List<FeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity): Long

    @Query("DELETE FROM feedback_entries WHERE id = :id")
    suspend fun deleteFeedbackById(id: Int)

    @Query("DELETE FROM feedback_entries")
    suspend fun clearAllFeedback()
}
