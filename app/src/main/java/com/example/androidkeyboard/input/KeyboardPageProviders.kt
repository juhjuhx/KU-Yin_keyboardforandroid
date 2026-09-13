package com.example.androidkeyboard.input

/**
 * UI-facing keyboard model used by the v0.2 shell.
 *
 * It intentionally adapts the already validated legacy [KeyboardLayout] definitions instead of
 * duplicating the Dachen/ASCII key tables. This keeps libchewing physical key codes and the
 * existing sizing metadata as the source of truth while the view layer is migrated incrementally.
 */
data class ResolvedKey(
    val label: String,
    val command: ImeCommand,
    val widthPct: Float,
    val isSpecial: Boolean,
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
        KeyboardLayout.Dachen.rows.toResolvedLayout()
}

class EnglishPageProvider : KeyboardPageProvider {
    override fun resolve(state: KeyboardRuntimeState): ResolvedKeyboardLayout =
        KeyboardLayout.asciiRows(shifted = state.shifted).toResolvedLayout()
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
            includeSecondarySwitch = true,
        )
        KeyboardPage.SYMBOLS_SECONDARY -> resolveLiteralPage(
            rows = SECONDARY_SYMBOL_ROWS,
            includeSecondarySwitch = false,
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
                    keys = row.keys.filterNot { key ->
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
        val configured = profile.left + profile.center + profile.right
        val keys = configured.mapNotNull { bottomKey ->
            when {
                bottomKey == BottomKey.NONE -> null
                bottomKey == BottomKey.LANGUAGE && !preferences.showLanguageKey -> null
                bottomKey == BottomKey.EMOJI && !preferences.showEmojiKey -> null
                bottomKey == BottomKey.NEXT_IME && !preferences.showNextImeKey -> null
                else -> bottomKey.toResolvedKey(state)
            }
        }
        return ResolvedKeyboardRow(keys = keys)
    }

    private fun resolveLiteralPage(
        rows: List<List<String>>,
        includeSecondarySwitch: Boolean,
    ): ResolvedKeyboardLayout {
        val body = rows.map { labels ->
            ResolvedKeyboardRow(
                keys = labels.map { label ->
                    ResolvedKey(
                        label = label,
                        command = ImeCommand.InsertText(label),
                        widthPct = 1f,
                        isSpecial = false,
                    )
                },
            )
        }
        val navigation = mutableListOf(
            ResolvedKey("ABC", ImeCommand.ReturnToLetters, 1.35f, true),
        )
        if (includeSecondarySwitch) {
            navigation += ResolvedKey("#+=", ImeCommand.InsertText(""), 1.1f, true)
        }
        navigation += listOf(
            ResolvedKey("⌫", ImeCommand.Backspace, 1.1f, true),
            ResolvedKey("空白", ImeCommand.Space, 3f, true),
            ResolvedKey("↵", ImeCommand.Enter, 1.35f, true),
        )
        return ResolvedKeyboardLayout(rows = body + ResolvedKeyboardRow(navigation))
    }

    private fun resolveEmojiPage(): ResolvedKeyboardLayout {
        val body = EMOJI_ROWS.map { labels ->
            ResolvedKeyboardRow(
                keys = labels.map { label ->
                    ResolvedKey(
                        label = label,
                        command = ImeCommand.InsertText(label),
                        widthPct = 1f,
                        isSpecial = false,
                    )
                },
            )
        }
        val navigation = ResolvedKeyboardRow(
            keys = listOf(
                ResolvedKey("ABC", ImeCommand.ReturnToLetters, 1.35f, true),
                ResolvedKey("⌫", ImeCommand.Backspace, 1.1f, true),
                ResolvedKey("空白", ImeCommand.Space, 3f, true),
                ResolvedKey("↵", ImeCommand.Enter, 1.35f, true),
            ),
        )
        return ResolvedKeyboardLayout(rows = body + navigation)
    }

    private fun BottomKey.toResolvedKey(state: KeyboardRuntimeState): ResolvedKey = when (this) {
        BottomKey.SYMBOLS -> ResolvedKey("?123", ImeCommand.OpenSymbols, 1.25f, true)
        BottomKey.EMOJI -> ResolvedKey("☺", ImeCommand.OpenEmoji, 1.05f, true)
        BottomKey.COMMA -> ResolvedKey(",", ImeCommand.InsertText(","), 1f, false)
        BottomKey.PERIOD -> ResolvedKey(".", ImeCommand.InsertText("."), 1f, false)
        BottomKey.LANGUAGE -> ResolvedKey(
            label = if (state.inputMode == InputMode.ZHUYIN) "中/英" else "英/中",
            command = ImeCommand.ToggleLanguage,
            widthPct = 1.25f,
            isSpecial = true,
        )
        BottomKey.SPACE -> ResolvedKey("空白", ImeCommand.Space, 3f, true)
        BottomKey.NEXT_IME -> ResolvedKey("🌐", ImeCommand.NextInputMethod, 1.05f, true)
        BottomKey.ENTER -> ResolvedKey("↵", ImeCommand.Enter, 1.35f, true)
        BottomKey.DISMISS -> ResolvedKey("⌄", ImeCommand.Dismiss, 1.05f, true)
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

private fun List<KeyboardRow>.toResolvedLayout(): ResolvedKeyboardLayout =
    ResolvedKeyboardLayout(
        rows = map { row ->
            ResolvedKeyboardRow(
                keys = row.keys.map(KeyDef::toResolvedKey),
            )
        },
    )

private fun KeyDef.toResolvedKey(): ResolvedKey = ResolvedKey(
    label = label,
    command = when (action) {
        KeyAction.INPUT -> ImeCommand.Input(code)
        KeyAction.SPACE -> ImeCommand.Space
        KeyAction.BACKSPACE -> ImeCommand.Backspace
        KeyAction.ENTER -> ImeCommand.Enter
        KeyAction.SHIFT -> ImeCommand.Shift
        KeyAction.DISMISS -> ImeCommand.Dismiss
    },
    widthPct = widthPct,
    isSpecial = isSpecial,
)
