package com.example.androidkeyboard.input

/**
 * Semantic role of a resolved key. Rendering may use this to distinguish character,
 * function, space, and editor-action keys without inferring meaning from labels.
 */
enum class KeyRole {
    CHARACTER,
    FUNCTION,
    SPACE,
    ACTION,
}

/**
 * UI-facing keyboard model used by the v0.2 shell.
 *
 * [id] is stable across presentation-only changes such as English shift casing, so future
 * per-key overrides can target a semantic key without depending on its visible label.
 * [secondaryLabel] is presentation metadata only; it never changes [command].
 */
data class ResolvedKey(
    val id: String,
    val label: String,
    val secondaryLabel: String? = null,
    val role: KeyRole,
    val command: ImeCommand,
    val widthPct: Float,
)

data class ResolvedKeyboardRow(
    val keys: List<ResolvedKey>,
)

data class ResolvedKeyboardLayout(
    val rows: List<ResolvedKeyboardRow>,
)

fun interface KeyboardPageProvider {
    fun resolve(state: KeyboardRuntimeState): ResolvedKeyboardLayout
}

class DachenPageProvider : KeyboardPageProvider {
    override fun resolve(state: KeyboardRuntimeState): ResolvedKeyboardLayout =
        KeyboardLayout.Dachen.rows.toResolvedLayout(
            profileId = "dachen",
            showPhysicalSecondaryLabels = true,
        )
}

class EnglishPageProvider : KeyboardPageProvider {
    override fun resolve(state: KeyboardRuntimeState): ResolvedKeyboardLayout =
        KeyboardLayout.asciiRows(shifted = state.shifted).toResolvedLayout(
            profileId = "english",
            showPhysicalSecondaryLabels = false,
        )
}

class KeyboardShellLayoutResolver(
    private val dachenProvider: KeyboardPageProvider = DachenPageProvider(),
    private val englishProvider: KeyboardPageProvider = EnglishPageProvider(),
) {
    fun resolve(
        state: KeyboardRuntimeState,
        preferences: KeyboardPreferences,
    ): ResolvedKeyboardLayout = when (state.page) {
        KeyboardPage.LETTERS -> resolveLetters(state, preferences)
        KeyboardPage.SYMBOLS_PRIMARY -> resolveLiteralPage(
            rows = PRIMARY_SYMBOL_ROWS,
            symbolPage = KeyboardPage.SYMBOLS_PRIMARY,
        )
        KeyboardPage.SYMBOLS_SECONDARY -> resolveLiteralPage(
            rows = SECONDARY_SYMBOL_ROWS,
            symbolPage = KeyboardPage.SYMBOLS_SECONDARY,
        )
        KeyboardPage.EMOJI -> resolveEmojiPage()
    }

    private fun resolveLetters(
        state: KeyboardRuntimeState,
        preferences: KeyboardPreferences,
    ): ResolvedKeyboardLayout {
        val provider = when (state.inputMode) {
            InputMode.ZHUYIN -> dachenProvider
            InputMode.ENGLISH -> englishProvider
        }
        val bodyRows = provider.resolve(state).rows
            .map { row ->
                ResolvedKeyboardRow(
                    keys = row.keys
                        .map { key ->
                            if (preferences.showSecondaryLabels) key else key.copy(secondaryLabel = null)
                        }
                        .filterNot { key ->
                            key.command == ImeCommand.Space ||
                                key.command == ImeCommand.Enter ||
                                key.command == ImeCommand.Dismiss
                        },
                )
            }
            .filter { row -> row.keys.isNotEmpty() }
            .filterNot { row ->
                state.inputMode == InputMode.ENGLISH &&
                    row.keys.isNotEmpty() &&
                    row.keys.all { key -> key.label == "." || key.label == "," }
            }

        return ResolvedKeyboardLayout(
            rows = bodyRows + resolveBottomRow(state, preferences),
        )
    }

    private fun resolveBottomRow(
        state: KeyboardRuntimeState,
        preferences: KeyboardPreferences,
    ): ResolvedKeyboardRow {
        val profile = BottomRowProfileValidator.sanitize(preferences.bottomRowProfile)
        val configured = mutableListOf<Pair<String, BottomKey>>()
        profile.left.forEachIndexed { index, key -> configured += "bottom:left:$index" to key }
        configured += "bottom:center" to profile.center
        profile.right.forEachIndexed { index, key -> configured += "bottom:right:$index" to key }

        val keys = configured.mapNotNull { (slotId, bottomKey) ->
            when {
                bottomKey == BottomKey.NONE -> null
                bottomKey == BottomKey.LANGUAGE && !preferences.showLanguageKey -> null
                bottomKey == BottomKey.EMOJI && !preferences.showEmojiKey -> null
                bottomKey == BottomKey.NEXT_IME && !preferences.showNextImeKey -> null
                else -> bottomKey.toResolvedKey(state, slotId)
            }
        }
        return ResolvedKeyboardRow(keys = keys)
    }

    private fun resolveLiteralPage(
        rows: List<List<String>>,
        symbolPage: KeyboardPage,
    ): ResolvedKeyboardLayout {
        val pageId = when (symbolPage) {
            KeyboardPage.SYMBOLS_PRIMARY -> "symbols-primary"
            KeyboardPage.SYMBOLS_SECONDARY -> "symbols-secondary"
            else -> error("resolveLiteralPage only supports symbol pages")
        }
        val body = rows.mapIndexed { rowIndex, labels ->
            ResolvedKeyboardRow(
                keys = labels.mapIndexed { columnIndex, label ->
                    ResolvedKey(
                        id = "$pageId:r$rowIndex:c$columnIndex",
                        label = label,
                        role = KeyRole.CHARACTER,
                        command = ImeCommand.InsertText(label),
                        widthPct = 1f,
                    )
                },
            )
        }
        val pageSwitch = when (symbolPage) {
            KeyboardPage.SYMBOLS_PRIMARY -> ResolvedKey(
                id = "$pageId:page-switch",
                label = "#+=",
                role = KeyRole.FUNCTION,
                command = ImeCommand.OpenSymbolsSecondary,
                widthPct = 1.1f,
            )
            KeyboardPage.SYMBOLS_SECONDARY -> ResolvedKey(
                id = "$pageId:page-switch",
                label = "?123",
                role = KeyRole.FUNCTION,
                command = ImeCommand.OpenSymbolsPrimary,
                widthPct = 1.1f,
            )
            else -> error("resolveLiteralPage only supports symbol pages")
        }
        val navigation = listOf(
            ResolvedKey("$pageId:return", "ABC", role = KeyRole.FUNCTION, command = ImeCommand.ReturnToLetters, widthPct = 1.35f),
            pageSwitch,
            ResolvedKey("$pageId:backspace", "⌫", role = KeyRole.FUNCTION, command = ImeCommand.Backspace, widthPct = 1.1f),
            ResolvedKey("$pageId:space", "空白", role = KeyRole.SPACE, command = ImeCommand.Space, widthPct = 3f),
            ResolvedKey("$pageId:enter", "↵", role = KeyRole.ACTION, command = ImeCommand.Enter, widthPct = 1.35f),
        )
        return ResolvedKeyboardLayout(rows = body + ResolvedKeyboardRow(navigation))
    }

    private fun resolveEmojiPage(): ResolvedKeyboardLayout {
        val body = EMOJI_ROWS.mapIndexed { rowIndex, labels ->
            ResolvedKeyboardRow(
                keys = labels.mapIndexed { columnIndex, label ->
                    ResolvedKey(
                        id = "emoji:r$rowIndex:c$columnIndex",
                        label = label,
                        role = KeyRole.CHARACTER,
                        command = ImeCommand.InsertText(label),
                        widthPct = 1f,
                    )
                },
            )
        }
        val navigation = ResolvedKeyboardRow(
            keys = listOf(
                ResolvedKey("emoji:return", "ABC", role = KeyRole.FUNCTION, command = ImeCommand.ReturnToLetters, widthPct = 1.35f),
                ResolvedKey("emoji:backspace", "⌫", role = KeyRole.FUNCTION, command = ImeCommand.Backspace, widthPct = 1.1f),
                ResolvedKey("emoji:space", "空白", role = KeyRole.SPACE, command = ImeCommand.Space, widthPct = 3f),
                ResolvedKey("emoji:enter", "↵", role = KeyRole.ACTION, command = ImeCommand.Enter, widthPct = 1.35f),
            ),
        )
        return ResolvedKeyboardLayout(rows = body + navigation)
    }

    private fun BottomKey.toResolvedKey(
        state: KeyboardRuntimeState,
        id: String,
    ): ResolvedKey = when (this) {
        BottomKey.SYMBOLS -> ResolvedKey(id, "?123", role = KeyRole.FUNCTION, command = ImeCommand.OpenSymbols, widthPct = 1.25f)
        BottomKey.EMOJI -> ResolvedKey(id, "☺", role = KeyRole.FUNCTION, command = ImeCommand.OpenEmoji, widthPct = 1.05f)
        BottomKey.COMMA -> ResolvedKey(id, ",", role = KeyRole.CHARACTER, command = ImeCommand.InsertText(","), widthPct = 1f)
        BottomKey.PERIOD -> ResolvedKey(id, ".", role = KeyRole.CHARACTER, command = ImeCommand.InsertText("."), widthPct = 1f)
        BottomKey.LANGUAGE -> ResolvedKey(
            id = id,
            label = if (state.inputMode == InputMode.ZHUYIN) "中/英" else "英/中",
            role = KeyRole.FUNCTION,
            command = ImeCommand.ToggleLanguage,
            widthPct = 1.25f,
        )
        BottomKey.SPACE -> ResolvedKey(id, "空白", role = KeyRole.SPACE, command = ImeCommand.Space, widthPct = 3f)
        BottomKey.NEXT_IME -> ResolvedKey(id, "🌐", role = KeyRole.FUNCTION, command = ImeCommand.NextInputMethod, widthPct = 1.05f)
        BottomKey.ENTER -> ResolvedKey(id, "↵", role = KeyRole.ACTION, command = ImeCommand.Enter, widthPct = 1.35f)
        BottomKey.DISMISS -> ResolvedKey(id, "⌄", role = KeyRole.FUNCTION, command = ImeCommand.Dismiss, widthPct = 1.05f)
        BottomKey.NONE -> error("NONE is filtered before key resolution")
    }

    private companion object {
        val PRIMARY_SYMBOL_ROWS = listOf(
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            listOf("!", "@", "#", "$", "%", "&", "*", "(", ")"),
            listOf("-", "+", "=", "/", "?", "：", "；", "、"),
        )
        val SECONDARY_SYMBOL_ROWS = listOf(
            listOf("[", "]", "{", "}", "<", ">", "^", "~"),
            listOf("¥", "$", "€", "£", "₩", "•", "°", "©", "®"),
            listOf("“", "”", "‘", "’", "…", "—", "±", "×", "÷"),
        )
        val EMOJI_ROWS = listOf(
            listOf("😀", "😂", "😊", "😍", "🥰", "😎", "😭", "😡"),
            listOf("👍", "👎", "👌", "🙏", "👏", "❤️", "🔥", "✨"),
            listOf("🎉", "✅", "❌", "⚠️", "💡", "📌", "🚀", "👀"),
        )
    }
}

private fun List<KeyboardRow>.toResolvedLayout(
    profileId: String,
    showPhysicalSecondaryLabels: Boolean,
): ResolvedKeyboardLayout = ResolvedKeyboardLayout(
    rows = map { row ->
        ResolvedKeyboardRow(
            keys = row.keys.map { key ->
                key.toResolvedKey(profileId, showPhysicalSecondaryLabels)
            },
        )
    },
)

private fun KeyDef.toResolvedKey(
    profileId: String,
    showPhysicalSecondaryLabels: Boolean,
): ResolvedKey {
    val command = when (action) {
        KeyAction.INPUT -> ImeCommand.Input(code)
        KeyAction.SPACE -> ImeCommand.Space
        KeyAction.BACKSPACE -> ImeCommand.Backspace
        KeyAction.ENTER -> ImeCommand.Enter
        KeyAction.SHIFT -> ImeCommand.Shift
        KeyAction.DISMISS -> ImeCommand.Dismiss
    }
    val role = when (action) {
        KeyAction.INPUT -> KeyRole.CHARACTER
        KeyAction.SPACE -> KeyRole.SPACE
        KeyAction.ENTER -> KeyRole.ACTION
        KeyAction.BACKSPACE,
        KeyAction.SHIFT,
        KeyAction.DISMISS -> KeyRole.FUNCTION
    }
    val idSuffix = when (action) {
        KeyAction.INPUT -> "input:$code"
        KeyAction.SPACE -> "space"
        KeyAction.BACKSPACE -> "backspace"
        KeyAction.ENTER -> "enter"
        KeyAction.SHIFT -> "shift"
        KeyAction.DISMISS -> "dismiss"
    }
    val secondaryLabel = if (
        showPhysicalSecondaryLabels && action == KeyAction.INPUT && code in 33..126
    ) {
        code.toChar().toString()
    } else {
        null
    }

    return ResolvedKey(
        id = "$profileId:$idSuffix",
        label = label,
        secondaryLabel = secondaryLabel,
        role = role,
        command = command,
        widthPct = widthPct,
    )
}
