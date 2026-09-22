package com.example.feedback.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.checkIsImeEnabled
import com.example.checkIsImeSelected
import com.example.feedback.data.AppDatabase
import com.example.feedback.data.FeedbackEntity
import com.example.feedback.data.FeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeedbackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FeedbackRepository
    val filterCategory = MutableStateFlow("ALL")

    val allFeedback: StateFlow<List<FeedbackEntity>>
    val filteredFeedback: StateFlow<List<FeedbackEntity>>

    init {
        val dao = AppDatabase.getDatabase(application).feedbackDao()
        repository = FeedbackRepository(dao)

        allFeedback = repository.allFeedback.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredFeedback = combine(allFeedback, filterCategory) { list, category ->
            if (category == "ALL") {
                list
            } else {
                list.filter { it.category == category }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun setFilter(category: String) {
        filterCategory.value = category
    }

    fun submitFeedback(
        category: String,
        title: String,
        description: String,
        userEmail: String,
        rating: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (title.isBlank()) {
            onError("請輸入回饋或問題主旨")
            return
        }
        if (description.isBlank()) {
            onError("請輸入詳細描述或問題重現步驟")
            return
        }

        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val isEnabled = checkIsImeEnabled(context)
                val isSelected = checkIsImeSelected(context)
                val imeStatus = when {
                    isEnabled && isSelected -> "已啟用且為預設輸入法"
                    isEnabled -> "已啟用但非預設輸入法"
                    else -> "尚未啟用"
                }

                val entity = FeedbackEntity(
                    category = category,
                    title = title.trim(),
                    description = description.trim(),
                    userEmail = userEmail.trim(),
                    rating = rating.coerceIn(1, 5),
                    appVersion = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                    deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                    imeStatus = imeStatus,
                    timestamp = System.currentTimeMillis()
                )

                repository.insertFeedback(entity)
                onSuccess()
            } catch (e: Exception) {
                onError("儲存失敗: ${e.localizedMessage}")
            }
        }
    }

    fun deleteFeedback(id: Int) {
        viewModelScope.launch {
            repository.deleteFeedbackById(id)
        }
    }

    fun clearAllFeedback() {
        viewModelScope.launch {
            repository.clearAllFeedback()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "KU-Yin Feedback") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
    }

    fun createShareIntent(feedback: FeedbackEntity): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "[酷音回饋] ${feedback.getCategoryDisplayName()}: ${feedback.title}")
            putExtra(Intent.EXTRA_TEXT, feedback.toStructuredReport())
        }
    }
}
