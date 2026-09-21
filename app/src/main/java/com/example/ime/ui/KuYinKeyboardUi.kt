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
    settings: KeyboardSettings,
    actionLabel: String,
    onCommitText: (String) -> Unit,
    onDeleteSurroundingText: () -> Unit,
    onPerformEditorAction: () -> Unit,
    onHideKeyboard: () -> Unit,
    onFeedback: () -> Unit,
    onOpenFeedback: () -> Unit = {}
) {
    val mode by engine.mode.collectAsState()
    val shiftState by engine.shiftState.collectAsState()
    val composingZhuyin by engine.composingZhuyin.collectAsState()
    val candidates by engine.candidates.collectAsState()

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
            // 頂部候選字 / 狀態列
            CandidateBar(
                composingZhuyin = composingZhuyin,
                candidates = candidates,
                candidateFontSizeSp = settings.candidateFontSizeSp,
                isClipboardBarEnabled = settings.isClipboardBarEnabled,
                theme = currentTheme,
                mode = mode,
                onCandidateClick = { candidate ->
                    onFeedback()
                    engine.selectCandidate(candidate, onCommitText)
                },
                onClearComposing = {
                    onFeedback()
                    engine.clearComposing()
                },
                onQuickPunctuation = { punct ->
                    onFeedback()
                    onCommitText(punct)
                },
                onPasteClipboard = { text ->
                    onFeedback()
                    onCommitText(text)
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
            when (mode) {
                KeyboardMode.ZHUYIN -> {
                    ZhuyinKeyboardLayout(
                        keyBgNormal = keyBgNormal,
                        keyBgAction = keyBgAction,
                        keyBgAccent = keyBgAccent,
                        keyTextPrimary = keyTextPrimary,
                        keyTextSub = keyTextSub,
                        actionLabel = actionLabel,
                        onZhuyinPress = { key ->
                            onFeedback()
                            engine.onZhuyinKey(key, onCommitText)
                        },
                        onSpacePress = {
                            onFeedback()
                            engine.onSpace(onCommitText)
                        },
                        onBackspacePress = {
                            onFeedback()
                            engine.onBackspace(onDeleteSurroundingText)
                        },
                        onEnterPress = {
                            onFeedback()
                            if (composingZhuyin.isNotEmpty()) {
                                // 如果正在拼音，按 Enter 直接送出拼音本身
                                onCommitText(composingZhuyin)
                                engine.clearComposing()
                            } else {
                                onPerformEditorAction()
                            }
                        },
                        onSwitchToEnglish = {
                            onFeedback()
                            engine.switchMode(KeyboardMode.ENGLISH, onCommitText)
                        },
                        onSwitchToSymbols = {
                            onFeedback()
                            engine.switchMode(KeyboardMode.SYMBOLS, onCommitText)
                        },
                        onSwitchToEmoji = {
                            onFeedback()
                            engine.switchMode(KeyboardMode.EMOJI, onCommitText)
                        },
                        onPunctuation = { p ->
                            onFeedback()
                            onCommitText(p)
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
                        onKeyPress = { char ->
                            onFeedback()
                            engine.onEnglishKey(char, onCommitText)
                        },
                        onShiftPress = {
                            onFeedback()
                            engine.toggleShift()
                        },
                        onSpacePress = {
                            onFeedback()
                            onCommitText(" ")
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
                            engine.switchMode(KeyboardMode.ZHUYIN, onCommitText)
                        },
                        onSwitchToSymbols = {
                            onFeedback()
                            engine.switchMode(KeyboardMode.SYMBOLS, onCommitText)
                        },
                        onPunctuation = { p ->
                            onFeedback()
                            onCommitText(p)
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
                        onSymbolPress = { symbol ->
                            onFeedback()
                            onCommitText(symbol)
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
                            engine.switchMode(KeyboardMode.ZHUYIN, onCommitText)
                        },
                        onSwitchToEnglish = {
                            onFeedback()
                            engine.switchMode(KeyboardMode.ENGLISH, onCommitText)
                        },
                        onSpacePress = {
                            onFeedback()
                            onCommitText(" ")
                        }
                    )
                }
                KeyboardMode.EMOJI -> {
                    EmojiKeyboardLayout(
                        keyBgAction = keyBgAction,
                        keyTextPrimary = keyTextPrimary,
                        onEmojiPress = { emoji ->
                            onFeedback()
                            onCommitText(emoji)
                        },
                        onBackspacePress = {
                            onFeedback()
                            onDeleteSurroundingText()
                        },
                        onSwitchToZhuyin = {
                            onFeedback()
                            engine.switchMode(KeyboardMode.ZHUYIN, onCommitText)
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
    onCandidateClick: (String) -> Unit,
    onClearComposing: () -> Unit,
    onQuickPunctuation: (String) -> Unit,
    onPasteClipboard: (String) -> Unit = {},
    onHideKeyboard: () -> Unit,
    onOpenFeedback: () -> Unit
) {
    val candidateBg = theme?.candidateBg ?: Color(0xFF0F172A)
    val accentColor = theme?.keyBgAccent ?: Color(0xFF0284C7)
    val textColor = theme?.keyTextPrimary ?: Color.White
    val subTextColor = theme?.keyTextSub ?: Color(0xFF94A3B8)
    val candidateCardBg = theme?.keyBgNormal ?: Color(0xFF1E293B)

    // 零網路/無剪貼簿讀取政策：已移除剪貼簿自動讀取，不再存取系統剪貼簿

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(candidateBg)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 拼音暫存標籤 (若有輸入)
        AnimatedVisibility(
            visible = composingZhuyin.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = composingZhuyin,
                    color = accentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "清除",
                    tint = accentColor,
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .clickable { onClearComposing() }
                )
            }
        }

        if (composingZhuyin.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
        }

        // 候選字列表
        if (candidates.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(candidates) { candidate ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onCandidateClick(candidate) },
                        color = candidateCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = candidate,
                            color = textColor,
                            fontSize = candidateFontSizeSp.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        } else {
            // 無候選字時：呈現常用標點
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val quickPunct = listOf("，", "。", "！", "？", "、", "～", "…", "「", "」")
                items(quickPunct) { punct ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onQuickPunctuation(punct) },
                        color = candidateCardBg
                    ) {
                        Text(
                            text = punct,
                            color = subTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 意見回饋 / 問題回報快捷鍵
        IconButton(
            onClick = onOpenFeedback,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.RateReview,
                contentDescription = "意見回饋與問題回報",
                tint = subTextColor
            )
        }

        // 收起鍵盤圖示
        IconButton(
            onClick = onHideKeyboard,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardHide,
                contentDescription = "收起鍵盤",
                tint = subTextColor
            )
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
    onPunctuation: (String) -> Unit
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
            ZhuyinConstants.ROW_1.forEach { keyDef ->
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
            ZhuyinConstants.ROW_2.forEach { keyDef ->
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
            ZhuyinConstants.ROW_3.forEach { keyDef ->
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

        // 第四排 (8 個注音鍵 + Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZhuyinConstants.ROW_4.forEach { keyDef ->
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
                icon = if (actionLabel == "換行") Icons.Default.KeyboardReturn else null,
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
    onPunctuation: (String) -> Unit
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
                icon = if (actionLabel == "換行") Icons.Default.KeyboardReturn else null,
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
    onSpacePress: () -> Unit
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
                icon = if (actionLabel == "換行") Icons.Default.KeyboardReturn else null,
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
