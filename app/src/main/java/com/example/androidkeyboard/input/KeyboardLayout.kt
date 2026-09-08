package com.example.androidkeyboard.input

data class KeyDef(
    val label: String,
    val widthPct: Float = 10f,
    val isSpecial: Boolean = false,
)

data class KeyboardRow(val keys: List<KeyDef>)

enum class KeyboardLayout(val rows: List<KeyboardRow>) {

    Dachen(
        rows = listOf(
            KeyboardRow(listOf(
                KeyDef(chr(0x3105), 10f),
                KeyDef(chr(0x3106), 10f),
                KeyDef(chr(0x3107), 10f),
                KeyDef(chr(0x3108), 10f),
                KeyDef(chr(0x3109), 10f),
                KeyDef(chr(0x310A), 10f),
                KeyDef(chr(0x310B), 10f),
                KeyDef(chr(0x310C), 10f),
                KeyDef(chr(0x310D), 10f),
                KeyDef(chr(0x310E), 10f),
            )),
            KeyboardRow(listOf(
                KeyDef(chr(0x310F), 10f),
                KeyDef(chr(0x3110), 10f),
                KeyDef(chr(0x3111), 10f),
                KeyDef(chr(0x3112), 10f),
                KeyDef(chr(0x3113), 10f),
                KeyDef(chr(0x3114), 10f),
                KeyDef(chr(0x3115), 10f),
                KeyDef(chr(0x3116), 10f),
                KeyDef(chr(0x3117), 10f),
                KeyDef(chr(0x3118), 10f),
            )),
            KeyboardRow(listOf(
                KeyDef(chr(0x3119), 10f),
                KeyDef(chr(0x311A), 10f),
                KeyDef(chr(0x311B), 10f),
                KeyDef(chr(0x311C), 10f),
                KeyDef(chr(0x311D), 10f),
                KeyDef(chr(0x311E), 10f),
                KeyDef(chr(0x311F), 10f),
                KeyDef(chr(0x3120), 10f),
                KeyDef(chr(0x3121), 10f),
                KeyDef(chr(0x3122), 10f),
            )),
            KeyboardRow(listOf(
                KeyDef(chr(0x3123), 10f), KeyDef(chr(0x3124), 10f), KeyDef(chr(0x3125), 10f), KeyDef(chr(45), 10f),
                KeyDef(chr(32), 10f), KeyDef(chr(124), 10f), KeyDef(chr(0x3001), 10f), KeyDef(chr(0x3002), 10f),
                KeyDef(chr(63), 10f), KeyDef(chr(33), 10f)
            ))
        )
    ),

    BopomofoSecond(
        rows = listOf(
            KeyboardRow(listOf(
                KeyDef(chr(0x02CA), 10f), KeyDef(chr(0x02C7), 10f), KeyDef(chr(0x02CB), 10f), KeyDef(chr(0x02D9), 10f),
                KeyDef(chr(48), 10f), KeyDef(chr(49), 10f), KeyDef(chr(50), 10f), KeyDef(chr(51), 10f),
                KeyDef(chr(52), 10f), KeyDef(chr(53), 10f)
            )),
            KeyboardRow(listOf(
                KeyDef(chr(54), 10f), KeyDef(chr(55), 10f), KeyDef(chr(56), 10f), KeyDef(chr(57), 10f),
                KeyDef(chr(0xFF0D), 10f), KeyDef(chr(61), 10f), KeyDef(chr(40), 10f), KeyDef(chr(41), 10f),
                KeyDef(chr(91), 10f), KeyDef(chr(93), 10f)
            )),
            KeyboardRow(listOf(
                KeyDef(chr(0x300A), 10f), KeyDef(chr(0x300B), 10f), KeyDef(chr(0x300C), 10f), KeyDef(chr(0x300D), 10f),
                KeyDef(chr(0x201C), 10f), KeyDef(chr(0x201D), 10f), KeyDef(chr(0x300E), 10f), KeyDef(chr(0x300F), 10f),
                KeyDef(chr(58), 10f), KeyDef(chr(59), 10f)
            )),
            KeyboardRow(listOf(
                KeyDef(chr(126), 10f), KeyDef(chr(64), 10f), KeyDef(chr(35), 10f), KeyDef(chr(36), 10f),
                KeyDef(chr(37), 10f), KeyDef(chr(94), 10f), KeyDef(chr(38), 10f), KeyDef(chr(42), 10f),
                KeyDef(chr(95), 10f), KeyDef(chr(43), 10f)
            ))
        )
    );

    val allKeys: List<KeyDef>
        get() = rows.flatten().flatMap { it.keys }

    val rowCount: Int get() = rows.size
}
