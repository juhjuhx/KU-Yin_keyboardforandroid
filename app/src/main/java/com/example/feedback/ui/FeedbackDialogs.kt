package com.example.feedback.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.feedback.data.FeedbackEntity

fun openGitHubIssue(context: Context, category: String, title: String, description: String, userEmail: String) {
    val categoryLabel = when (category) {
        "BUG" -> "[Bug Report]"
        "FEATURE" -> "[Feature Request]"
        "DICTIONARY" -> "[Dictionary]"
        else -> "[Feedback]"
    }
    val issueTitle = Uri.encode("$categoryLabel $title")
    val issueBody = Uri.encode(
        """
        ### 回饋分類
        $categoryLabel

        ### 詳細說明與重現步驟
        $description

        ### 聯絡信箱
        ${if (userEmail.isNotBlank()) userEmail else "未填寫"}

        ### 系統環境診斷
        - 裝置型號: ${Build.MANUFACTURER} ${Build.MODEL}
        - Android 版本: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
        - 酷音注音輸入法版本: 1.0.0
        """.trimIndent()
    )
    val url = "https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/new?title=$issueTitle&body=$issueBody"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
        Toast.makeText(context, "正在為您開啟 GitHub 提 Issue 頁面…", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "無法開啟瀏覽器: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackCard(
    feedbackCount: Int,
    onOpenSubmit: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("feedback_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "使用者回饋與問題回報",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "遇上打字問題、字詞缺漏或有新功能想法？歡迎回饋",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenSubmit,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_open_feedback_submit"),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("填寫回饋")
                }

                OutlinedButton(
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_open_feedback_history")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (feedbackCount > 0) "紀錄 ($feedbackCount)" else "歷史紀錄")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackSubmissionDialog(
    onDismiss: () -> Unit,
    onSubmit: (category: String, title: String, description: String, email: String, rating: Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("BUG") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("dialog_feedback_submit"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Feedback,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "提交問題或功能建議",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "關閉")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Category Selection
                    item {
                        Text(
                            text = "回饋類型",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val categories = listOf(
                                "BUG" to "🐛 問題回報",
                                "FEATURE" to "💡 功能建議",
                                "DICTIONARY" to "📖 詞庫補充",
                                "OTHER" to "💬 其他"
                            )
                            categories.forEach { (catKey, catLabel) ->
                                FilterChip(
                                    selected = selectedCategory == catKey,
                                    onClick = { selectedCategory = catKey },
                                    label = { Text(catLabel, fontSize = 12.sp) },
                                    modifier = Modifier.testTag("chip_cat_$catKey")
                                )
                            }
                        }
                    }

                    // Rating
                    item {
                        Text(
                            text = "滿意度 / 影響程度",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { rating = star },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("star_rating_$star")
                                ) {
                                    Icon(
                                        imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "$star 星",
                                        tint = if (star <= rating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${rating} 顆星",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Title
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                errorMessage = null
                            },
                            label = { Text("回饋主旨 (必填)") },
                            placeholder = {
                                Text(
                                    when (selectedCategory) {
                                        "BUG" -> "例：打字時某些注音音節未正常出現候選字"
                                        "FEATURE" -> "例：希望增加自訂鍵盤高度功能"
                                        "DICTIONARY" -> "例：建議詞庫加入「酷音」常用詞"
                                        else -> "簡述您的回饋或建議"
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_feedback_title"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Description
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                errorMessage = null
                            },
                            label = { Text("詳細說明與重現步驟 (必填)") },
                            placeholder = {
                                Text(
                                    when (selectedCategory) {
                                        "BUG" -> "請說明：1. 發生問題的具體步驟；2. 預期結果；3. 實際結果"
                                        "FEATURE" -> "請詳細說明您所期望的功能與使用情境"
                                        "DICTIONARY" -> "請列出欲新增的詞彙及對應的注音聲調"
                                        else -> "詳細描述您的寶貴建議或想法"
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp)
                                .testTag("input_feedback_description"),
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // User Email (Optional)
                    item {
                        OutlinedTextField(
                            value = userEmail,
                            onValueChange = { userEmail = it },
                            label = { Text("聯絡信箱 (選填)") },
                            placeholder = { Text("若希望收到處理進度可填寫 email") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_feedback_email"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Diagnostics auto-attached notice
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "系統將自動附帶裝置型號、Android 版本與輸入法啟用狀態等結構化診斷資訊，協助快速釐清問題。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Error Message
                    errorMessage?.let { msg ->
                        item {
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (title.isBlank()) {
                                    errorMessage = "請填寫回饋主旨"
                                    return@OutlinedButton
                                }
                                if (description.isBlank()) {
                                    errorMessage = "請填寫詳細說明內容"
                                    return@OutlinedButton
                                }
                                openGitHubIssue(context, selectedCategory, title, description, userEmail)
                                onSubmit(selectedCategory, title, description, userEmail, rating)
                            },
                            modifier = Modifier.testTag("btn_submit_github_issue")
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GitHub 提 Issue", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                if (title.isBlank()) {
                                    errorMessage = "請填寫回饋主旨"
                                    return@Button
                                }
                                if (description.isBlank()) {
                                    errorMessage = "請填寫詳細說明內容"
                                    return@Button
                                }
                                onSubmit(selectedCategory, title, description, userEmail, rating)
                            },
                            modifier = Modifier.testTag("btn_submit_feedback_confirm")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("儲存送出", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackHistoryDialog(
    feedbackList: List<FeedbackEntity>,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    onSelectFeedback: (FeedbackEntity) -> Unit,
    onDeleteFeedback: (Int) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var showConfirmClear by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("dialog_feedback_history"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "已提交的回饋紀錄",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "關閉")
                    }
                }

                // Filter chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "全部 (${feedbackList.size})",
                        "BUG" to "🐛 問題",
                        "FEATURE" to "💡 建議",
                        "DICTIONARY" to "📖 詞庫",
                        "OTHER" to "💬 其他"
                    )
                    items(filters) { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { onSelectFilter(key) },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                // List
                if (feedbackList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "尚無符合條件的回饋紀錄",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(feedbackList, key = { it.id }) { item ->
                            FeedbackItemCard(
                                item = item,
                                onClick = { onSelectFeedback(item) },
                                onDelete = { onDeleteFeedback(item.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (feedbackList.isNotEmpty()) {
                        TextButton(
                            onClick = { showConfirmClear = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清空全部")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(onClick = onDismiss) {
                        Text("完成")
                    }
                }
            }
        }
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            title = { Text("清空所有回饋紀錄") },
            text = { Text("確定要刪除所有已儲存的使用者回饋紀錄嗎？此動作無法復原。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showConfirmClear = false
                    }
                ) {
                    Text("確定清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun FeedbackItemCard(
    item: FeedbackEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("feedback_item_${item.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (item.category) {
                            "BUG" -> MaterialTheme.colorScheme.errorContainer
                            "FEATURE" -> MaterialTheme.colorScheme.primaryContainer
                            "DICTIONARY" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            text = item.getCategoryDisplayName(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "★ ${item.rating}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = item.getFormattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "刪除此筆回饋",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FeedbackDetailDialog(
    feedback: FeedbackEntity,
    onDismiss: () -> Unit,
    onCopyJson: () -> Unit,
    onCopyReport: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("dialog_feedback_detail"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "回饋結構化明細",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "關閉")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = feedback.getCategoryDisplayName(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "評分: ${feedback.rating} ★",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = feedback.getFormattedDate(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Text(
                            text = feedback.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        Text(
                            text = "【詳細描述】",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = feedback.description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (feedback.userEmail.isNotBlank()) {
                        item {
                            Text(
                                text = "聯絡信箱: ${feedback.userEmail}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    item {
                        Text(
                            text = "【環境與診斷資訊 (自動附加)】",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("• 鍵盤版本: ${feedback.appVersion}", style = MaterialTheme.typography.bodySmall)
                                Text("• 裝置型號: ${feedback.deviceModel}", style = MaterialTheme.typography.bodySmall)
                                Text("• 系統版本: ${feedback.androidVersion}", style = MaterialTheme.typography.bodySmall)
                                Text("• 輸入法狀態: ${feedback.imeStatus}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    item {
                        Text(
                            text = "【結構化 JSON 預覽】",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = feedback.toStructuredJson(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(12.dp),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val context = LocalContext.current
                Button(
                    onClick = {
                        openGitHubIssue(context, feedback.category, feedback.title, feedback.description, feedback.userEmail)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_detail_github_issue"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("前往 GitHub 建立 Issue")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopyJson,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("複製 JSON", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onCopyReport,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("複製報表", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("分享傳送", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
