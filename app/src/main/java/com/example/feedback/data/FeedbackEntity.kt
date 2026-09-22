package com.example.feedback.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "feedback_entries")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String, // "BUG", "FEATURE", "DICTIONARY", "OTHER"
    val title: String,
    val description: String,
    val userEmail: String = "",
    val rating: Int = 5, // 1-5
    val appVersion: String = "",
    val androidVersion: String = "",
    val deviceModel: String = "",
    val imeStatus: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getCategoryDisplayName(): String {
        return when (category) {
            "BUG" -> "問題回報 (Bug)"
            "FEATURE" -> "功能建議 (Feature)"
            "DICTIONARY" -> "詞庫補充 (Dictionary)"
            else -> "其他回饋 (Other)"
        }
    }

    /**
     * 將回饋資料轉換成結構化 JSON 字串，便於導出與結構化檢閱
     */
    fun toStructuredJson(): String {
        return """
        {
          "id": $id,
          "category": "$category",
          "categoryName": "${getCategoryDisplayName()}",
          "title": "${escapeJson(title)}",
          "description": "${escapeJson(description)}",
          "userEmail": "${escapeJson(userEmail)}",
          "rating": $rating,
          "timestamp": $timestamp,
          "dateFormatted": "${getFormattedDate()}",
          "diagnostics": {
            "appVersion": "${escapeJson(appVersion)}",
            "androidVersion": "${escapeJson(androidVersion)}",
            "deviceModel": "${escapeJson(deviceModel)}",
            "imeStatus": "${escapeJson(imeStatus)}"
          }
        }
        """.trimIndent()
    }

    /**
     * 將回饋資料轉換成格式化摘要報表，方便檢閱、複製或透過 Email/分享發送
     */
    fun toStructuredReport(): String {
        return """
        ==============================
        KU-YIN 酷音輸入法 - 使用者回饋
        ==============================
        類別: ${getCategoryDisplayName()}
        標題: $title
        評分: $rating ★
        時間: ${getFormattedDate()}
        聯絡信箱: ${if (userEmail.isNotBlank()) userEmail else "未提供"}

        【詳細說明】
        $description

        【系統與裝置環境診斷】
        - 鍵盤版本: $appVersion
        - 裝置型號: $deviceModel
        - Android 系統: $androidVersion
        - 輸入法狀態: $imeStatus
        ==============================
        """.trimIndent()
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
