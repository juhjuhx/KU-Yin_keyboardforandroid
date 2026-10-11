package com.example.ime.engine

import java.io.StringReader
import org.junit.Assert.*
import org.junit.Test

class EnglishLexiconTest {
    @Test fun prefixSuggestionsUseFrequencyThenAlphabeticalTieBreakAndLimit() {
        val lexicon = EnglishLexicon.fromTsv(StringReader("help\t149\nheld\t158\nhello\t110\nhelen\t149\nworld\t200\n"))
        assertEquals(listOf("held", "helen", "help"), lexicon.suggest("HE", 3))
        assertEquals(emptyList<String>(), lexicon.suggest("", 5))
        assertEquals(emptyList<String>(), lexicon.suggest("zzzz", 5))
        assertEquals(emptyList<String>(), lexicon.suggest("he", 0))
    }

    @Test fun repeatedWordsUseHighestScoreWithoutDuplicateCandidates() {
        val lexicon = EnglishLexicon.fromTsv(StringReader("hello\t110\nhelp\t149\nhello\t160\n"))
        assertEquals(listOf("hello", "help"), lexicon.suggest("hel", 5))
    }

    @Test fun malformedAssetIsRejectedInsteadOfPartlyLoaded() {
        for (line in listOf("Hello\t90", "hello\t0", "hello\t256", "hello\tNaN", "hello", "hello-world\t90")) {
            try {
                EnglishLexicon.fromTsv(StringReader("help\t149\n$line\n"))
                fail("Expected malformed lexicon row to be rejected")
            } catch (_: IllegalArgumentException) {
                // Intentional asset boundary validation.
            }
        }
    }
}
