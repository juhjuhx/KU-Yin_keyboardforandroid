package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feedback.data.FeedbackEntity
import com.example.feedback.ui.FeedbackCard
import com.example.feedback.ui.FeedbackDetailDialog
import com.example.feedback.ui.FeedbackHistoryDialog
import com.example.feedback.ui.FeedbackSubmissionDialog
import com.example.feedback.ui.FeedbackViewModel
import com.example.ime.engine.KuYinEngine
import com.example.ime.engine.ZhuyinDictionary
import com.example.ime.settings.KeyboardSettings
import com.example.ime.ui.KEYBOARD_THEMES
import com.example.ime.ui.KuYinKeyboardUi
import com.example.ime.ui.OpenSourceCommunityCard
import com.example.ime.ui.OpenSourceDonationCard
import com.example.ime.ui.UpstreamSyncCard
import com.example.ime.util.FeedbackHelper
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_screen")
                ) { innerPadding ->
                    KuYinSetupScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuYinSetupScreen(
    modifier: Modifier = Modifier,
    feedbackViewModel: FeedbackViewModel = viewModel()
) {
    val context = LocalContext.current
    val settings = remember { KeyboardSettings(context) }
    val dict = remember { ZhuyinDictionary(context) }

    val allFeedback by feedbackViewModel.allFeedback.collectAsStateWithLifecycle()
    val filteredFeedback by feedbackViewModel.filteredFeedback.collectAsStateWithLifecycle()
    val selectedFilter by feedbackViewModel.filterCategory.collectAsStateWithLifecycle()

    var showFeedbackSubmitDialog by remember { mutableStateOf(false) }
    var showFeedbackHistoryDialog by remember { mutableStateOf(false) }
    var selectedDetailFeedback by remember { mutableStateOf<FeedbackEntity?>(null) }

    LaunchedEffect(Unit) {
        val activity = context as? ComponentActivity
        if (activity?.intent?.getBooleanExtra("EXTRA_OPEN_FEEDBACK", false) == true) {
            showFeedbackSubmitDialog = true
            activity.intent.removeExtra("EXTRA_OPEN_FEEDBACK")
        }
    }

    var isEnabled by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }

    // 當 Activity 回到前景時重新整理狀態
    fun refreshStatus() {
        isEnabled = checkIsImeEnabled(context)
        isSelected = checkIsImeSelected(context)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        refreshStatus()
    }

    var testInputText by remember { mutableStateOf("") }
    var vibrateState by remember { mutableStateOf(settings.isVibrateEnabled) }
    var soundState by remember { mutableStateOf(settings.isSoundEnabled) }
    var candidateFontSize by remember { mutableStateOf(settings.candidateFontSizeSp) }
    var themeIndex by remember { mutableIntStateOf(settings.keyboardThemeIndex) }
    var showResetDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val localView = LocalView.current
    val directEngine = remember { KuYinEngine(context) }
    val feedbackHelper = remember { FeedbackHelper(context, settings) }
    var showDirectKeyboard by remember { mutableStateOf(true) }

    val dismissKeyboard: () -> Unit = {
        focusManager.clearFocus()
        keyboardController?.hide()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(localView.windowToken, 0)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    dismissKeyboard()
                })
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Hero Header
            item {
                HeroHeaderCard(
                    isEnabled = isEnabled,
                    isSelected = isSelected
                )
            }

            // 啟用步驟導引
            item {
                Text(
                    text = "設定與啟用步驟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Step 1: 啟用輸入法
            item {
                SetupStepCard(
                    stepNumber = "1",
                    title = "在系統中啟用酷音輸入法",
                    description = "前往 Android「語言與輸入法」設定，開啟「酷音注音輸入法 (KU-Yin)」開關。",
                    isCompleted = isEnabled,
                    buttonText = if (isEnabled) "已啟用 (點擊可查看)" else "前往系統設定啟用",
                    buttonTag = "btn_step_enable",
                    onAction = {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        context.startActivity(intent)
                    }
                )
            }

            // Step 2: 切換輸入法
            item {
                SetupStepCard(
                    stepNumber = "2",
                    title = "切換為酷音輸入法",
                    description = "點擊下方按鈕叫出輸入法切換器，選擇「酷音注音輸入法」。",
                    isCompleted = isSelected,
                    buttonText = if (isSelected) "已設為目前輸入法" else "切換為酷音輸入法",
                    buttonTag = "btn_step_select",
                    onAction = {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.showInputMethodPicker()
                    }
                )
            }

            // Step 3: 即時試用體驗區
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "3",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "即時試用輸入框",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "點擊下方輸入框立即測試酷音注音打字、候選字選取與聲調輸入：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = testInputText,
                            onValueChange = { testInputText = it },
                            placeholder = { Text("點擊此處或使用下方直打鍵盤輸入...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .testTag("test_input_field"),
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (testInputText.isNotEmpty()) {
                                        IconButton(onClick = { testInputText = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "清除")
                                        }
                                    }
                                    IconButton(onClick = { dismissKeyboard() }) {
                                        Icon(Icons.Default.KeyboardHide, contentDescription = "收起鍵盤")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 快捷測試詞彙
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SuggestionChip(
                                onClick = { testInputText += "你好！" },
                                label = { Text("你好！") }
                            )
                            SuggestionChip(
                                onClick = { testInputText += "酷音輸入法" },
                                label = { Text("酷音輸入法") }
                            )
                            SuggestionChip(
                                onClick = { testInputText += "台灣" },
                                label = { Text("台灣") }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 操作與診斷控制列
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                    imm?.showSoftInput(localView, InputMethodManager.SHOW_FORCED)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_call_system_keyboard"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("喚起系統鍵盤", fontSize = 13.sp)
                            }

                            FilledTonalButton(
                                onClick = { showDirectKeyboard = !showDirectKeyboard },
                                modifier = Modifier.testTag("btn_toggle_direct_keyboard")
                            ) {
                                Text(
                                    text = if (showDirectKeyboard) "收起直打鍵盤" else "展開直打鍵盤",
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // 內嵌畫面直打鍵盤
                        AnimatedVisibility(visible = showDirectKeyboard) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "酷音鍵盤即時試打面板",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "支援聲調、注音連打、選字與切換",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("embedded_kuyin_keyboard"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                                ) {
                                    key(themeIndex) {
                                        KuYinKeyboardUi(
                                            engine = directEngine,
                                            settings = settings,
                                            actionLabel = "換行",
                                            onCommitText = { text ->
                                                testInputText += text
                                            },
                                            onDeleteSurroundingText = {
                                                if (testInputText.isNotEmpty()) {
                                                    testInputText = testInputText.dropLast(1)
                                                }
                                            },
                                            onPerformEditorAction = {
                                                testInputText += "\n"
                                            },
                                            onHideKeyboard = {
                                                showDirectKeyboard = false
                                            },
                                            onFeedback = {
                                                feedbackHelper.performKeyPressFeedback(localView)
                                            },
                                            onOpenFeedback = {
                                                showFeedbackSubmitDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 鍵盤個人化設定
            item {
                Text(
                    text = "鍵盤偏好設定",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 按鍵震動回饋
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("按鍵震動回饋", fontWeight = FontWeight.SemiBold)
                                Text("打字時提供觸覺振動回饋", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = vibrateState,
                                onCheckedChange = {
                                    vibrateState = it
                                    settings.isVibrateEnabled = it
                                },
                                modifier = Modifier.testTag("switch_vibrate")
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // 按鍵音效
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("按鍵音效", fontWeight = FontWeight.SemiBold)
                                Text("打字時播放輕脆按鍵點擊音效", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = soundState,
                                onCheckedChange = {
                                    soundState = it
                                    settings.isSoundEnabled = it
                                },
                                modifier = Modifier.testTag("switch_sound")
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // 候選字大小
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("候選字字體大小", fontWeight = FontWeight.SemiBold)
                                Text("目前大小: ${candidateFontSize}sp", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = candidateFontSize == 16,
                                    onClick = {
                                        candidateFontSize = 16
                                        settings.candidateFontSizeSp = 16
                                    },
                                    label = { Text("標準") }
                                )
                                FilterChip(
                                    selected = candidateFontSize == 20,
                                    onClick = {
                                        candidateFontSize = 20
                                        settings.candidateFontSizeSp = 20
                                    },
                                    label = { Text("大字") }
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // 鍵盤配色主題
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("鍵盤色彩主題風格", fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = KEYBOARD_THEMES.getOrElse(themeIndex) { KEYBOARD_THEMES[0] }.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "提供多款精心調色鍵盤面板，自適應個人偏好",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(KEYBOARD_THEMES) { index, theme ->
                                    FilterChip(
                                        selected = themeIndex == index,
                                        onClick = {
                                            themeIndex = index
                                            settings.keyboardThemeIndex = index
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.keyBgAccent)
                                                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                            )
                                        },
                                        label = { Text(theme.name, fontSize = 12.sp) },
                                        modifier = Modifier.testTag("chip_theme_$index")
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // 清除自訂字詞學習紀錄
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_reset_dictionary"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("重設字詞學習紀錄")
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Report a Bug 按鈕 (開啟專案 GitHub Issues 頁面)
                        Button(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/new")))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_settings_report_bug"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Report a Bug (前往 GitHub 回報問題)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/juhjuhx/KU-Yin_keyboardforandroid")))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_settings_open_github")
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("專案 GitHub 倉庫 (Star / Fork / 源碼)")
                        }
                    }
                }
            }

            // 使用者回饋與問題回報區
            item {
                Text(
                    text = "使用者回饋與問題回報",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                FeedbackCard(
                    feedbackCount = allFeedback.size,
                    onOpenSubmit = { showFeedbackSubmitDialog = true },
                    onOpenHistory = { showFeedbackHistoryDialog = true }
                )
            }

            // 注音鍵盤特色與使用秘訣
            item {
                Text(
                    text = "使用技巧與特色",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeatureItem(
                            icon = Icons.Default.Keyboard,
                            title = "標準大千式鍵盤",
                            desc = "鍵盤鍵位完全符合台灣電腦標準鍵盤排列，支援右上角實體鍵位對照。"
                        )
                        FeatureItem(
                            icon = Icons.Default.VolumeUp,
                            title = "完整聲調支援",
                            desc = "支援一聲 (空白鍵)、二聲 (ˊ)、三聲 (ˇ)、四聲 (ˋ) 與輕聲 (˙) 輸入。"
                        )
                        FeatureItem(
                            icon = Icons.Default.AutoFixHigh,
                            title = "智慧候選字與字詞聯想",
                            desc = "輸入常用詞彙（如「你好」、「謝謝」）自動聯想整組詞，選字自適應學習。"
                        )
                        FeatureItem(
                            icon = Icons.Default.Translate,
                            title = "多模式一鍵切換",
                            desc = "底部工具列提供「中 / EN」、「?123」符號以及「😊」表情符號快速切換。"
                        )
                    }
                }
            }

            // 上游版本與詞庫同步
            item {
                UpstreamSyncCard()
            }

            // 開源社群、發行與開放架構
            item {
                OpenSourceCommunityCard()
            }

            // 開源社群與自由軟體基金會捐助
            item {
                OpenSourceDonationCard()
            }
        }

        // 重設對話框
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("重設學習紀錄") },
                text = { Text("這將會清除您打字過程中的候選字常用排序紀錄，是否確定重設？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dict.clearLearnedFrequencies()
                            showResetDialog = false
                            snackbarMessage = "已成功重設字詞學習紀錄"
                        }
                    ) {
                        Text("確定重設", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // 提交回饋對話框
        if (showFeedbackSubmitDialog) {
            FeedbackSubmissionDialog(
                onDismiss = { showFeedbackSubmitDialog = false },
                onSubmit = { category, title, description, email, rating ->
                    feedbackViewModel.submitFeedback(
                        category = category,
                        title = title,
                        description = description,
                        userEmail = email,
                        rating = rating,
                        onSuccess = {
                            showFeedbackSubmitDialog = false
                            snackbarMessage = "已成功送出回饋！感謝您的寶貴意見"
                        },
                        onError = { error ->
                            snackbarMessage = error
                        }
                    )
                }
            )
        }

        // 歷史回饋清單對話框
        if (showFeedbackHistoryDialog) {
            FeedbackHistoryDialog(
                feedbackList = filteredFeedback,
                selectedFilter = selectedFilter,
                onSelectFilter = { feedbackViewModel.setFilter(it) },
                onSelectFeedback = { selectedDetailFeedback = it },
                onDeleteFeedback = { id ->
                    feedbackViewModel.deleteFeedback(id)
                    snackbarMessage = "已刪除該筆回饋"
                },
                onClearAll = {
                    feedbackViewModel.clearAllFeedback()
                    snackbarMessage = "已清空所有回饋紀錄"
                },
                onDismiss = { showFeedbackHistoryDialog = false }
            )
        }

        // 回饋詳細資訊檢閱與分享對話框
        selectedDetailFeedback?.let { feedback ->
            FeedbackDetailDialog(
                feedback = feedback,
                onDismiss = { selectedDetailFeedback = null },
                onCopyJson = {
                    feedbackViewModel.copyToClipboard(context, feedback.toStructuredJson(), "KU-Yin Feedback JSON")
                    snackbarMessage = "已複製結構化 JSON 到剪貼簿"
                },
                onCopyReport = {
                    feedbackViewModel.copyToClipboard(context, feedback.toStructuredReport(), "KU-Yin Feedback Report")
                    snackbarMessage = "已複製格式化回饋報表到剪貼簿"
                },
                onShare = {
                    val shareIntent = Intent.createChooser(
                        feedbackViewModel.createShareIntent(feedback),
                        "分享結構化回饋資料"
                    )
                    context.startActivity(shareIntent)
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun HeroHeaderCard(isEnabled: Boolean, isSelected: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E293B)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "酷",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "酷音注音鍵盤",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "KU-Yin Keyboard for Android",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 狀態徽章
                val statusBg: Color
                val statusText: String
                val statusIcon: androidx.compose.ui.graphics.vector.ImageVector

                if (isEnabled && isSelected) {
                    statusBg = Color(0xFF065F46) // Emerald
                    statusText = "已啟用並正在使用中"
                    statusIcon = Icons.Default.CheckCircle
                } else if (isEnabled) {
                    statusBg = Color(0xFF854D0E) // Amber
                    statusText = "已啟用，請切換為目前輸入法"
                    statusIcon = Icons.Default.Info
                } else {
                    statusBg = Color(0xFF991B1B) // Rose/Red
                    statusText = "尚未啟用輸入法"
                    statusIcon = Icons.Default.Warning
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetupStepCard(
    stepNumber: String,
    title: String,
    description: String,
    isCompleted: Boolean,
    buttonText: String,
    buttonTag: String,
    onAction: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(buttonTag),
                colors = if (isCompleted) {
                    ButtonDefaults.filledTonalButtonColors()
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun checkIsImeEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
    val enabledList = imm.enabledInputMethodList
    val myPackage = context.packageName
    return enabledList.any { it.packageName == myPackage }
}

fun checkIsImeSelected(context: Context): Boolean {
    val currentIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
    return currentIme != null && currentIme.contains(context.packageName)
}
