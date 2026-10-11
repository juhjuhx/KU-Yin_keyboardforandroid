package com.example.ime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.engine.*
import com.example.ime.session.ComposeDecoderSession
import com.example.ime.session.ImeCommand
import com.example.ime.session.DispatchResult
import com.example.ime.session.EnglishWordSession
import com.example.ime.session.orderedCommits
import com.example.ime.session.CandidatePageSnapshot
import com.example.ime.settings.KeyboardSettings

data class KeyboardThemeColors(
    val id: Int,
    val name: String,
    val keyboardBg: Color,
    val keyBgNormal: Color,
    val keyBgAction: Color,
    val keyBgAccent: Color,
    val keyTextPrimary: Color,
    val keyTextSub: Color,
    val candidateBg: Color,
    val dividerColor: Color
)

val KEYBOARD_THEMES = listOf(
    KeyboardThemeColors(
        id = 0,
        name = "經典深灰",
        keyboardBg = Color(0xFF1E293B),
        keyBgNormal = Color(0xFF334155),
        keyBgAction = Color(0xFF475569),
        keyBgAccent = Color(0xFF0284C7),
        keyTextPrimary = Color(0xFFF8FAFC),
        keyTextSub = Color(0xFF94A3B8),
        candidateBg = Color(0xFF0F172A),
        dividerColor = Color(0xFF334155)
    ),
    KeyboardThemeColors(
        id = 1,
        name = "曜石極黑",
        keyboardBg = Color(0xFF000000),
        keyBgNormal = Color(0xFF1C1C1E),
        keyBgAction = Color(0xFF2C2C2E),
        keyBgAccent = Color(0xFF0A84FF),
        keyTextPrimary = Color(0xFFFFFFFF),
        keyTextSub = Color(0xFF8E8E93),
        candidateBg = Color(0xFF121212),
        dividerColor = Color(0xFF2C2C2E)
    ),
    KeyboardThemeColors(
        id = 2,
        name = "皓雪純白",
        keyboardBg = Color(0xFFECEFF1),
        keyBgNormal = Color(0xFFFFFFFF),
        keyBgAction = Color(0xFFCFD8DC),
        keyBgAccent = Color(0xFF0288D1),
        keyTextPrimary = Color(0xFF263238),
        keyTextSub = Color(0xFF78909C),
        candidateBg = Color(0xFFF5F5F5),
        dividerColor = Color(0xFFCFD8DC)
    ),
    KeyboardThemeColors(
        id = 3,
        name = "莫蘭迪綠",
        keyboardBg = Color(0xFF1F2E2B),
        keyBgNormal = Color(0xFF2F4540),
        keyBgAction = Color(0xFF3B5650),
        keyBgAccent = Color(0xFF2E7D6A),
        keyTextPrimary = Color(0xFFE8F5E9),
        keyTextSub = Color(0xFFA3C2BA),
        candidateBg = Color(0xFF172421),
        dividerColor = Color(0xFF2F4540)
    ),
    KeyboardThemeColors(
        id = 4,
        name = "櫻花粉霞",
        keyboardBg = Color(0xFF2D1F29),
        keyBgNormal = Color(0xFF452B3C),
        keyBgAction = Color(0xFF5A384F),
        keyBgAccent = Color(0xFFD81B60),
        keyTextPrimary = Color(0xFFFCE4EC),
        keyTextSub = Color(0xFFCE93D8),
        candidateBg = Color(0xFF22161F),
        dividerColor = Color(0xFF452B3C)
    ),
    KeyboardThemeColors(
        id = 5,
        name = "賽博霓虹",
        keyboardBg = Color(0xFF130E2E),
        keyBgNormal = Color(0xFF241C52),
        keyBgAction = Color(0xFF372D75),
        keyBgAccent = Color(0xFF7C3AED),
        keyTextPrimary = Color(0xFFF5F3FF),
        keyTextSub = Color(0xFFA78BFA),
        candidateBg = Color(0xFF0C0820),
        dividerColor = Color(0xFF241C52)
    )
)

@Composable
fun KuYinKeyboardUi(
    engine: KuYinEngine,
    session: ComposeDecoderSession,
    settings: KeyboardSettings,
    actionLabel: String,
    onCommitText: (String) -> Unit,
    onDeleteSurroundingText: () -> Unit,
    onPerformEditorAction: () -> Unit,
    onHideKeyboard: () -> Unit,
    onFeedback: () -> Unit,
    onOpenFeedback: () -> Unit = {},
    onUserModeSelected: (KeyboardMode) -> Unit = {},
    isActionPresentation: Boolean = actionLabel != "換行",
    englishSession: EnglishWordSession? = null,
    onComposingText: (String) -> Unit = {},
    editorWriteFailure: String? = null,
    onDismissEditorWriteFailure: () -> Unit = {},
    shouldAcceptInput: () -> Boolean = { true },
) {
    val mode by engine.mode.collectAsState()
    val shiftState by engine.shiftState.collectAsState()
    val sessionState by session.state.collectAsState()
    val englishState = englishSession?.state?.collectAsState()?.value
    val composingZhuyin = if (mode == KeyboardMode.ENGLISH && englishState != null)
        englishState.wordBuffer else sessionState.preedit
    val expanded = mode == KeyboardMode.ZHUYIN && sessionState.candidatesExpanded && sessionState.candidatePage != null
    val candidates = if (mode == KeyboardMode.ENGLISH && englishState != null)
        englishState.candidates else sessionState.candidates

    fun applyResult(result: DispatchResult, preedit: String) {
        orderedCommits(result).forEach(onCommitText)
        onComposingText(preedit)
    }
    fun dispatchChinese(command: ImeCommand): DispatchResult {
        if (!shouldAcceptInput()) return DispatchResult(null, true)
        return session.dispatch(command).also { applyResult(it, session.state.value.preedit) }
    }
    fun dispatchEnglish(action: () -> DispatchResult): DispatchResult {
        if (!shouldAcceptInput()) return DispatchResult(null, true)
        return action().also { applyResult(it, englishSession?.state?.value?.wordBuffer.orEmpty()) }
    }
    fun commitLiteral(text: String) {
        if (shouldAcceptInput()) onCommitText(text)
    }
    val switchPresentationMode: (KeyboardMode) -> Unit = switch@ { selectedMode ->
        if (!shouldAcceptInput()) return@switch
        if (mode == KeyboardMode.ENGLISH && englishSession != null)
            dispatchEnglish { englishSession.complete() }
        else {
            dispatchChinese(ImeCommand.Complete)
            if (session.state.value.preedit.isNotEmpty() || session.state.value.unavailableReason != null)
                return@switch
        }
        if (shouldAcceptInput()) {
            onUserModeSelected(selectedMode)
            engine.setMode(selectedMode)
        }
    }
    val applyPunctuation: (String) -> Unit = { punctuation ->
        if (mode == KeyboardMode.ENGLISH && englishSession != null)
            dispatchEnglish { englishSession.punctuation(punctuation) }
        else dispatchChinese(ImeCommand.Punctuation(punctuation))
    }

    val onCandidateSelect: (Int) -> Unit = { index ->
        onFeedback()
        if (mode == KeyboardMode.ENGLISH && englishSession != null) {
            if (englishSession.state.value === englishState)
                dispatchEnglish { englishSession.select(index) }
        } else if (session.state.value === sessionState) {
            dispatchChinese(ImeCommand.SelectCandidate(index, sessionState.candidatePage?.ref(index)))
        }
    }
    fun browse(command: ImeCommand) {
        // Presentation/page operations produce no editor effects.
        if (shouldAcceptInput() && session.state.value === sessionState) session.dispatch(command)
    }
    val currentTheme = KEYBOARD_THEMES.getOrElse(settings.keyboardThemeIndex) { KEYBOARD_THEMES[0] }
    val keyboardBg = currentTheme.keyboardBg
    val keyBgNormal = currentTheme.keyBgNormal
    val keyBgAction = currentTheme.keyBgAction
    val keyBgAccent = currentTheme.keyBgAccent
    val keyTextPrimary = currentTheme.keyTextPrimary
    val keyTextSub = currentTheme.keyTextSub

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = keyboardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            if (editorWriteFailure != null) {
                Text("編輯器未確認送出", color = keyTextPrimary)
                Text(editorWriteFailure, color = keyTextPrimary, maxLines = 2)
                TextButton(onClick = onDismissEditorWriteFailure) { Text("捨棄未確認文字") }
            }
            if (mode == KeyboardMode.ZHUYIN && sessionState.unavailableReason != null) {
                Text(sessionState.unavailableReason.orEmpty(), color = keyTextPrimary)
            }
            // 頂部候選字 / 狀態列
            CandidateBar(
                composingZhuyin = composingZhuyin,
                candidates = candidates,
                candidateFontSizeSp = settings.candidateFontSizeSp,
                isClipboardBarEnabled = settings.isClipboardBarEnabled,
                theme = currentTheme,
                mode = mode,
                onCandidateClick = onCandidateSelect,
                pageSnapshot = sessionState.candidatePage,
                expanded = expanded,
                onExpandCandidates = { browse(ImeCommand.OpenExpandedCandidates) },
                onCollapseCandidates = { browse(ImeCommand.CloseExpandedCandidates) },
                onClearComposing = {
                    onFeedback()
                    if (shouldAcceptInput()) {
                        if (mode == KeyboardMode.ENGLISH && englishSession != null) {
                            englishSession.reset()
                            onComposingText("")
                        } else dispatchChinese(ImeCommand.ClearComposing)
                    }
                },
                onQuickPunctuation = { punct ->
                    onFeedback()
                    applyPunctuation(punct)
                },
                onPasteClipboard = { text ->
                    onFeedback()
                    commitLiteral(text)
                },
                onHideKeyboard = onHideKeyboard,
                onOpenFeedback = onOpenFeedback
            )

            HorizontalDivider(
                color = currentTheme.dividerColor,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 鍵盤主體區
            if (expanded) {
                val page = sessionState.candidatePage!!
                ExpandedCandidateGrid(page, settings.candidateFontSizeSp, currentTheme,
                    onSelect = onCandidateSelect,
                    onPrevious = { browse(ImeCommand.PreviousCandidatePage(page)) },
                    onNext = { browse(ImeCommand.NextCandidatePage(page)) },
                )
            } else when (mode) {
                KeyboardMode.ZHUYIN -> {
                    ZhuyinKeyboardLayout(
                        keyBgNormal = keyBgNormal,
                        keyBgAction = keyBgAction,
                        keyBgAccent = keyBgAccent,
                        keyTextPrimary = keyTextPrimary,
                        keyTextSub = keyTextSub,
                        actionLabel = actionLabel,
                        isActionPresentation = isActionPresentation,
                        onZhuyinPress = { key ->
                            onFeedback()
                            dispatchChinese(ImeCommand.TapZhuyinKey(key))
                        },
                        onSpacePress = {
                            onFeedback()
                            dispatchChinese(ImeCommand.Space)
                        },
                        onBackspacePress = {
                            onFeedback()
                            val result = dispatchChinese(ImeCommand.Backspace)
                            if (!result.consumed) onDeleteSurroundingText()
                        },
                        onEnterPress = {
                            onFeedback()
                            val result = dispatchChinese(ImeCommand.Enter)
                            if (!result.consumed) onPerformEditorAction()
                        },
                        onSwitchToEnglish = {
                            onFeedback()
                            switchPresentationMode(KeyboardMode.ENGLISH)
                        },
                        onSwitchToSymbols = {
                            onFeedback()
                            switchPresentationMode(KeyboardMode.SYMBOLS)
                        },
                        onSwitchToEmoji = {
                            onFeedback()
                            switchPresentationMode(KeyboardMode.EMOJI)
                        },
                        onPunctuation = { p ->
                            onFeedback()
                            applyPunctuation(p)
                        }
                    )
                }
                KeyboardMode.ENGLISH -> {
                    EnglishKeyboardLayout(
                        shiftState = shiftState,
                        keyBgNormal = keyBgNormal,
                        keyBgAction = keyBgAction,
                        keyBgAccent = keyBgAccent,
                        keyTextPrimary = keyTextPrimary,
                        actionLabel = actionLabel,
                        isActionPresentation = isActionPresentation,
                        onKeyPress = { char ->
                            onFeedback()
                            if (shouldAcceptInput()) engine.onEnglishKey(char) { typed ->
                                if (englishSession == null) commitLiteral(typed)
                                else dispatchEnglish { englishSession.typeText(typed) }
                            }
                        },
                        onShiftPress = {
                            onFeedback()
                            engine.toggleShift()
                        },
                        onSpacePress = {
                            onFeedback()
                            if (englishSession == null) commitLiteral(" ")
                            else dispatchEnglish { englishSession.space() }
                        },
                        onBackspacePress = {
                            onFeedback()
                            if (englishSession == null) {
                                if (shouldAcceptInput()) onDeleteSurroundingText()
                            } else if (!dispatchEnglish { englishSession.backspace() }.consumed)
                                onDeleteSurroundingText()
                        },
                        onEnterPress = {
                            onFeedback()
                            if (englishSession == null) {
                                if (shouldAcceptInput()) onPerformEditorAction()
                            } else if (!dispatchEnglish { englishSession.complete() }.consumed)
                                onPerformEditorAction()
                        },
                        onSwitchToZhuyin = {
                            onFeedback()
                            switchPresentationMode(KeyboardMode.ZHUYIN)
                        },
                        onSwitchToSymbols = {
                            onFeedback()
                            switchPresentationMode(KeyboardMode.SYMBOLS)
                        },
                        onPunctuation = { p ->
                            onFeedback()
                            applyPunctuation(p)
                        }
                    )
                }
                KeyboardMode.SYMBOLS -> {
                    SymbolsKeyboardLayout(
                        keyBgNormal = keyBgNormal,
                        keyBgAction = keyBgAction,
                        keyBgAccent = keyBgAccent,
                        keyTextPrimary = keyTextPrimary,
                        actionLabel = actionLabel,
                        isActionPresentation = isActionPresentation,
                        onSymbolPress = { symbol ->
                            onFeedback()
                            commitLiteral(symbol)
                        },
                        onBackspacePress = {
                            onFeedback()
                            onDeleteSurroundingText()
                        },
                        onEnterPress = {
                            onFeedback()
                            onPerformEditorAction()
                        },
                        onSwitchToZhuyin = {
                            onFeedback()
                            switchPresentationMode(KeyboardMode.ZHUYIN)
                        },
                        onSwitchToEnglish = {
                            onFeedback()
                            switchPresentationMode(KeyboardMode.ENGLISH)
                        },
                        onSpacePress = {
                            onFeedback()
                            commitLiteral(" ")
                        }
                    )
                }
                KeyboardMode.EMOJI -> {
                    EmojiKeyboardLayout(
                        keyBgAction = keyBgAction,
                        keyTextPrimary = keyTextPrimary,
                        onEmojiPress = { emoji ->
                            onFeedback()
                            commitLiteral(emoji)
                        },
                        onBackspacePress = {
                            onFeedback()
                            onDeleteSurroundingText()
                        },
                        onSwitchToZhuyin = {
                            onFeedback()
                            switchPresentationMode(KeyboardMode.ZHUYIN)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CandidateBar(
    composingZhuyin: String,
    candidates: List<String>,
    candidateFontSizeSp: Int,
    isClipboardBarEnabled: Boolean = true,
    theme: KeyboardThemeColors? = null,
    mode: KeyboardMode,
    onCandidateClick: (Int) -> Unit,
    onClearComposing: () -> Unit,
    onQuickPunctuation: (String) -> Unit,
    onPasteClipboard: (String) -> Unit = {},
    onHideKeyboard: () -> Unit,
    onOpenFeedback: () -> Unit,
    pageSnapshot: CandidatePageSnapshot? = null,
    expanded: Boolean = false,
    onExpandCandidates: () -> Unit = {},
    onCollapseCandidates: () -> Unit = {},
) {
    val colors = theme ?: KEYBOARD_THEMES[0]
    Column(Modifier.fillMaxWidth().background(colors.candidateBg)) {
        if (composingZhuyin.isNotEmpty() || expanded) {
            Row(Modifier.fillMaxWidth().height(48.dp).padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(composingZhuyin, color = colors.keyBgAccent, fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f))
                IconButton(onClick = onClearComposing, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Close, "清除", tint = colors.keyBgAccent)
                }
                if (expanded) IconButton(onClick = onCollapseCandidates, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ExpandLess, "收合候選", tint = colors.keyTextSub)
                }
                IconButton(onClick = onOpenFeedback, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.RateReview, "意見回饋與問題回報", tint = colors.keyTextSub)
                }
                IconButton(onClick = onHideKeyboard, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.KeyboardHide, "收起鍵盤", tint = colors.keyTextSub)
                }
            }
        }
        if (!expanded) {
            if (candidates.isNotEmpty() || (mode == KeyboardMode.ZHUYIN &&
                    pageSnapshot != null && (pageSnapshot.hasPrevious || pageSnapshot.hasNext))) {
                CandidateStrip(candidates, pageSnapshot, candidateFontSizeSp, colors,
                    showExpand = mode == KeyboardMode.ZHUYIN && composingZhuyin.isNotEmpty(),
                    onSelect = onCandidateClick, onExpand = onExpandCandidates)
            } else {
                Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                    LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)) {
                        items(listOf("，", "。", "！", "？", "、", "～", "…", "「", "」")) { punct ->
                            Surface(Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                .clickable { onQuickPunctuation(punct) },
                                color = colors.keyBgNormal, shape = RoundedCornerShape(6.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(punct, color = colors.keyTextSub, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                    IconButton(onClick = onOpenFeedback, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.RateReview, "意見回饋與問題回報", tint = colors.keyTextSub)
                    }
                    if (composingZhuyin.isEmpty()) IconButton(onClick = onHideKeyboard, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.KeyboardHide, "收起鍵盤", tint = colors.keyTextSub)
                    }
                }
            }
        }
    }
}

@Composable
fun ZhuyinKeyboardLayout(
    keyBgNormal: Color,
    keyBgAction: Color,
    keyBgAccent: Color,
    keyTextPrimary: Color,
    keyTextSub: Color,
    actionLabel: String,
    onZhuyinPress: (String) -> Unit,
    onSpacePress: () -> Unit,
    onBackspacePress: () -> Unit,
    onEnterPress: () -> Unit,
    onSwitchToEnglish: () -> Unit,
    onSwitchToSymbols: () -> Unit,
    onSwitchToEmoji: () -> Unit,
    onPunctuation: (String) -> Unit,
    isActionPresentation: Boolean = actionLabel != "換行",
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 第一排 (11 個鍵)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            DachenKeyboardRows.rows[0].forEach { keyDef ->
                ZhuyinKeyView(
                    modifier = Modifier.weight(1f),
                    keyDef = keyDef,
                    bg = keyBgNormal,
                    primaryColor = keyTextPrimary,
                    subColor = keyTextSub,
                    onClick = { onZhuyinPress(keyDef.main) }
                )
            }
        }

        // 第二排 (10 個鍵)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            DachenKeyboardRows.rows[1].forEach { keyDef ->
                ZhuyinKeyView(
                    modifier = Modifier.weight(1f),
                    keyDef = keyDef,
                    bg = keyBgNormal,
                    primaryColor = keyTextPrimary,
                    subColor = keyTextSub,
                    onClick = { onZhuyinPress(keyDef.main) }
                )
            }
        }

        // 第三排 (10 個鍵)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            DachenKeyboardRows.rows[2].forEach { keyDef ->
                ZhuyinKeyView(
                    modifier = Modifier.weight(1f),
                    keyDef = keyDef,
                    bg = keyBgNormal,
                    primaryColor = keyTextPrimary,
                    subColor = keyTextSub,
                    onClick = { onZhuyinPress(keyDef.main) }
                )
            }
        }

        // 第四排 (10 個注音鍵 + Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DachenKeyboardRows.rows[3].forEach { keyDef ->
                ZhuyinKeyView(
                    modifier = Modifier.weight(1f),
                    keyDef = keyDef,
                    bg = keyBgNormal,
                    primaryColor = keyTextPrimary,
                    subColor = keyTextSub,
                    onClick = { onZhuyinPress(keyDef.main) }
                )
            }

            // Backspace 鍵
            ActionKeyView(
                modifier = Modifier.weight(1.3f),
                bg = keyBgAction,
                icon = Icons.Default.Backspace,
                onClick = onBackspacePress
            )
        }

        // 第五排 (功能鍵 + 空白鍵 + 送出)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 切換英文
            ActionKeyView(
                modifier = Modifier.weight(1.1f),
                bg = keyBgAction,
                text = "EN",
                onClick = onSwitchToEnglish
            )

            // 切換符號
            ActionKeyView(
                modifier = Modifier.weight(1.1f),
                bg = keyBgAction,
                text = "?123",
                onClick = onSwitchToSymbols
            )

            // 表情符號
            ActionKeyView(
                modifier = Modifier.weight(0.9f),
                bg = keyBgAction,
                text = "😊",
                onClick = onSwitchToEmoji
            )

            // 逗號
            ActionKeyView(
                modifier = Modifier.weight(0.8f),
                bg = keyBgNormal,
                text = "，",
                onClick = { onPunctuation("，") }
            )

            // 空白鍵
            Box(
                modifier = Modifier
                    .weight(3.0f)
                    .height(48.dp)
                    .clickable { onSpacePress() }
                    .padding(horizontal = 1.5.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(keyBgNormal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "空白 (一聲)",
                    color = keyTextSub,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 句號
            ActionKeyView(
                modifier = Modifier.weight(0.8f),
                bg = keyBgNormal,
                text = "。",
                onClick = { onPunctuation("。") }
            )

            // Enter 鍵 (主色調)
            ActionKeyView(
                modifier = Modifier.weight(1.5f),
                bg = keyBgAccent,
                text = actionLabel,
                icon = if (!isActionPresentation) Icons.Default.KeyboardReturn else null,
                onClick = onEnterPress
            )
        }
    }
}

@Composable
fun EnglishKeyboardLayout(
    shiftState: ShiftState,
    keyBgNormal: Color,
    keyBgAction: Color,
    keyBgAccent: Color,
    keyTextPrimary: Color,
    actionLabel: String,
    onKeyPress: (String) -> Unit,
    onShiftPress: () -> Unit,
    onSpacePress: () -> Unit,
    onBackspacePress: () -> Unit,
    onEnterPress: () -> Unit,
    onSwitchToZhuyin: () -> Unit,
    onSwitchToSymbols: () -> Unit,
    onPunctuation: (String) -> Unit,
    isActionPresentation: Boolean = actionLabel != "換行",
) {
    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")

    val isUppercase = shiftState != ShiftState.OFF

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 第一排
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            row1.forEach { char ->
                val display = if (isUppercase) char.uppercase() else char
                SingleKeyView(
                    modifier = Modifier.weight(1f),
                    text = display,
                    bg = keyBgNormal,
                    textColor = keyTextPrimary,
                    onClick = { onKeyPress(char) }
                )
            }
        }

        // 第二排
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            row2.forEach { char ->
                val display = if (isUppercase) char.uppercase() else char
                SingleKeyView(
                    modifier = Modifier.weight(1f),
                    text = display,
                    bg = keyBgNormal,
                    textColor = keyTextPrimary,
                    onClick = { onKeyPress(char) }
                )
            }
        }

        // 第三排 (Shift + 字母 + Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shift
            val shiftBg = if (shiftState == ShiftState.CAPS_LOCK) keyBgAccent else keyBgAction
            ActionKeyView(
                modifier = Modifier.weight(1.5f),
                bg = shiftBg,
                icon = Icons.Default.ArrowUpward,
                onClick = onShiftPress
            )

            row3.forEach { char ->
                val display = if (isUppercase) char.uppercase() else char
                SingleKeyView(
                    modifier = Modifier.weight(1f),
                    text = display,
                    bg = keyBgNormal,
                    textColor = keyTextPrimary,
                    onClick = { onKeyPress(char) }
                )
            }

            // Backspace
            ActionKeyView(
                modifier = Modifier.weight(1.5f),
                bg = keyBgAction,
                icon = Icons.Default.Backspace,
                onClick = onBackspacePress
            )
        }

        // 第四排 (注音切換 + 符號 + 空格 + Enter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionKeyView(
                modifier = Modifier.weight(1.2f),
                bg = keyBgAction,
                text = "注音",
                onClick = onSwitchToZhuyin
            )

            ActionKeyView(
                modifier = Modifier.weight(1.1f),
                bg = keyBgAction,
                text = "?123",
                onClick = onSwitchToSymbols
            )

            ActionKeyView(
                modifier = Modifier.weight(0.9f),
                bg = keyBgNormal,
                text = ",",
                onClick = { onPunctuation(",") }
            )

            // Space bar
            Box(
                modifier = Modifier
                    .weight(3.4f)
                    .height(48.dp)
                    .clickable { onSpacePress() }
                    .padding(horizontal = 1.5.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(keyBgNormal),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Space", color = Color(0xFF94A3B8), fontSize = 13.sp)
            }

            ActionKeyView(
                modifier = Modifier.weight(0.9f),
                bg = keyBgNormal,
                text = ".",
                onClick = { onPunctuation(".") }
            )

            ActionKeyView(
                modifier = Modifier.weight(1.5f),
                bg = keyBgAccent,
                text = actionLabel,
                icon = if (!isActionPresentation) Icons.Default.KeyboardReturn else null,
                onClick = onEnterPress
            )
        }
    }
}

@Composable
fun SymbolsKeyboardLayout(
    keyBgNormal: Color,
    keyBgAction: Color,
    keyBgAccent: Color,
    keyTextPrimary: Color,
    actionLabel: String,
    onSymbolPress: (String) -> Unit,
    onBackspacePress: () -> Unit,
    onEnterPress: () -> Unit,
    onSwitchToZhuyin: () -> Unit,
    onSwitchToEnglish: () -> Unit,
    onSpacePress: () -> Unit,
    isActionPresentation: Boolean = actionLabel != "換行",
) {
    val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val row2 = listOf("，", "。", "！", "？", "：", "；", "「", "」", "『", "』")
    val row3 = listOf("（", "）", "【", "】", "《", "》", "～", "…", "、", "@")
    val row4 = listOf("+", "-", "*", "/", "=", "%", "#", "$", "&")

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 數字排
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            row1.forEach { sym ->
                SingleKeyView(
                    modifier = Modifier.weight(1f),
                    text = sym,
                    bg = keyBgNormal,
                    textColor = keyTextPrimary,
                    onClick = { onSymbolPress(sym) }
                )
            }
        }

        // 常用全形標點 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            row2.forEach { sym ->
                SingleKeyView(
                    modifier = Modifier.weight(1f),
                    text = sym,
                    bg = keyBgNormal,
                    textColor = keyTextPrimary,
                    onClick = { onSymbolPress(sym) }
                )
            }
        }

        // 常用全形標點 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            row3.forEach { sym ->
                SingleKeyView(
                    modifier = Modifier.weight(1f),
                    text = sym,
                    bg = keyBgNormal,
                    textColor = keyTextPrimary,
                    onClick = { onSymbolPress(sym) }
                )
            }
        }

        // 運算符號 + Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            row4.forEach { sym ->
                SingleKeyView(
                    modifier = Modifier.weight(1f),
                    text = sym,
                    bg = keyBgNormal,
                    textColor = keyTextPrimary,
                    onClick = { onSymbolPress(sym) }
                )
            }

            ActionKeyView(
                modifier = Modifier.weight(1.3f),
                bg = keyBgAction,
                icon = Icons.Default.Backspace,
                onClick = onBackspacePress
            )
        }

        // 底部導航
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionKeyView(
                modifier = Modifier.weight(1.3f),
                bg = keyBgAction,
                text = "注音",
                onClick = onSwitchToZhuyin
            )

            ActionKeyView(
                modifier = Modifier.weight(1.3f),
                bg = keyBgAction,
                text = "ABC",
                onClick = onSwitchToEnglish
            )

            Box(
                modifier = Modifier
                    .weight(3.4f)
                    .height(48.dp)
                    .clickable { onSpacePress() }
                    .padding(horizontal = 1.5.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(keyBgNormal),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "空白", color = Color(0xFF94A3B8), fontSize = 13.sp)
            }

            ActionKeyView(
                modifier = Modifier.weight(1.5f),
                bg = keyBgAccent,
                text = actionLabel,
                icon = if (!isActionPresentation) Icons.Default.KeyboardReturn else null,
                onClick = onEnterPress
            )
        }
    }
}

@Composable
fun EmojiKeyboardLayout(
    keyBgAction: Color,
    keyTextPrimary: Color,
    onEmojiPress: (String) -> Unit,
    onBackspacePress: () -> Unit,
    onSwitchToZhuyin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 表情網格
        val emojis = ZhuyinConstants.EMOJI_LIST.chunked(8)
        emojis.forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                chunk.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { onEmojiPress(emoji) }
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 22.sp)
                    }
                }
            }
        }

        // 底部導航
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionKeyView(
                modifier = Modifier.weight(1.5f),
                bg = keyBgAction,
                text = "返回注音",
                onClick = onSwitchToZhuyin
            )


            ActionKeyView(
                modifier = Modifier.weight(1.2f),
                bg = keyBgAction,
                icon = Icons.Default.Backspace,
                onClick = onBackspacePress
            )
        }
    }
}

// 注音鍵 (顯示大注音與右上角小字)
@Composable
fun ZhuyinKeyView(
    modifier: Modifier = Modifier,
    keyDef: KeyDef,
    bg: Color,
    primaryColor: Color,
    subColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentBg = if (isPressed) bg.copy(alpha = 0.6f) else bg

    Box(
        modifier = modifier
            .height(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple()
            ) { onClick() }
            .padding(horizontal = 1.5.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(currentBg)
    ) {
        // 右上角英數對照 (大千鍵位提示)
        if (keyDef.sub.isNotEmpty()) {
            Text(
                text = keyDef.sub,
                color = subColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 4.dp)
            )
        }

        // 主要注音符號
        Text(
            text = keyDef.main,
            color = primaryColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// 單一文字鍵
@Composable
fun SingleKeyView(
    modifier: Modifier = Modifier,
    text: String,
    bg: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentBg = if (isPressed) bg.copy(alpha = 0.6f) else bg

    Box(
        modifier = modifier
            .height(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple()
            ) { onClick() }
            .padding(horizontal = 1.5.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(currentBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// 功能/動作鍵
@Composable
fun ActionKeyView(
    modifier: Modifier = Modifier,
    bg: Color,
    text: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentBg = if (isPressed) bg.copy(alpha = 0.6f) else bg

    Box(
        modifier = modifier
            .height(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple()
            ) { onClick() }
            .padding(horizontal = 1.5.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(currentBg),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text ?: "操作",
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
