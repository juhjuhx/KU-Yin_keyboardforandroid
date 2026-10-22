package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CandidateAnchorTest {
    private val zhuyinWithHints = mapOf(
        "ㄅ" to listOf(CandidateRegionBounds(0, 196, 40, 244)),
        "ㄉ" to listOf(CandidateRegionBounds(40, 196, 80, 244)),
        // Visible q/w are ASCII hints on ㄆ/ㄊ in the second Dachen row.
        "q" to listOf(CandidateRegionBounds(0, 244, 40, 292)),
        "w" to listOf(CandidateRegionBounds(40, 244, 80, 292)),
    )

    @Test fun zhuyinAsciiHintsDoNotMakeFirstRowAmbiguous() {
        assertEquals(196, firstKeyboardRowTop(CandidateKeyboardLayout.ZHUYIN) { zhuyinWithHints[it].orEmpty() })
    }
    @Test fun unknownVisibleModeRefusesEvenWhenAnchorsExist() {
        assertNull(firstKeyboardRowTop(null) { if (it == "ㄅ" || it == "ㄉ") zhuyinWithHints[it].orEmpty() else emptyList() })
    }
    @Test fun englishUsesItsFirstRow() {
        val english = zhuyinWithHints.filterKeys { it == "q" || it == "w" }
        assertEquals(244, firstKeyboardRowTop(CandidateKeyboardLayout.ENGLISH) { english[it].orEmpty() })
    }
    @Test fun englishCapsUsesUppercaseAnchors() {
        val caps = zhuyinWithHints.filterKeys { it == "q" || it == "w" }.mapKeys { it.key.uppercase() }
        assertEquals(244, firstKeyboardRowTop(CandidateKeyboardLayout.ENGLISH) { caps[it].orEmpty() })
    }
    @Test fun missingOrDuplicateZhuyinAnchorIsRejected() {
        assertNull(firstKeyboardRowTop(CandidateKeyboardLayout.ZHUYIN) {
            if (it == "ㄅ") zhuyinWithHints[it].orEmpty() + zhuyinWithHints[it].orEmpty() else zhuyinWithHints[it].orEmpty()
        })
    }
    @Test fun wrongLayoutAnchorsCannotSubstituteForMissingZhuyinKeys() {
        val hintsOnly = zhuyinWithHints.filterKeys { it == "q" || it == "w" }
        assertNull(firstKeyboardRowTop(CandidateKeyboardLayout.ZHUYIN) { hintsOnly[it].orEmpty() })
    }
}
