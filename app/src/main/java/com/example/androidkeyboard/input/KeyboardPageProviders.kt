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
