package com.example.ime.session

import com.example.ime.engine.EnglishLexicon
import com.example.ime.engine.EnglishPrefixProvider
import java.io.File
import java.io.StringReader
import org.junit.Assert.*
import org.junit.Test

class EnglishWordSessionTest {
    private fun session(): EnglishWordSession = EnglishWordSession(
        EnglishLexicon.fromTsv(StringReader("held\t158\nhelp\t149\nhello\t110\nworld\t155\n"))
    )

    @Test fun realTenThousandWordLexiconOffersHelloForHelWithinEightCandidates() {
        val asset = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
            .map { File(it, "app/src/main/assets/${EnglishLexicon.ASSET_PATH}") }
            .first { it.isFile }
        val lexicon = asset.bufferedReader().use { EnglishLexicon.fromTsv(it) }
        val session = EnglishWordSession(lexicon)
        session.typeText("hel")
        assertTrue(session.state.value.candidates.contains("hello"))
        assertEquals(listOf("held", "help", "helped"), session.state.value.candidates.take(3))
        assertTrue(session.state.value.candidates.size <= 8)
    }

    @Test fun typingKeepsOriginalWordInBufferUntilExplicitCompletion() {
        val session = session()
        assertEquals(emptyList<String>(), orderedCommits(session.typeText("hel")))
        assertEquals("hel", session.state.value.wordBuffer)
        assertEquals(listOf("held", "help", "hello"), session.state.value.candidates)
        assertEquals(listOf("hel"), orderedCommits(session.complete()))
        assertTrue(session.state.value.wordBuffer.isEmpty())
        assertFalse(session.complete().consumed)
    }

    @Test fun selectingCandidateCommitsWholeWordWithoutAutomaticSpace() {
        val session = session()
        session.typeText("hel")
        assertEquals(listOf("hello"), orderedCommits(session.select(2)))
        assertEquals(listOf(" "), orderedCommits(session.space()))
        assertTrue(session.state.value.candidates.isEmpty())
    }

    @Test fun spaceFlushesTypedWordThenExactlyOneSeparator() {
        val session = session()
        session.typeText("hel")
        assertEquals(listOf("hel", " "), orderedCommits(session.space()))
        assertEquals(listOf(" "), orderedCommits(session.space()))
    }

    @Test fun punctuationFlushesOriginalWordBeforePunctuation() {
        val session = session()
        session.typeText("hel")
        assertEquals(listOf("hel", "."), orderedCommits(session.punctuation(".")))
        assertEquals(listOf(","), orderedCommits(session.punctuation(",")))
        assertTrue(session.state.value.wordBuffer.isEmpty())
    }

    @Test fun completionConsumesBufferedEnterOnlyOnce() {
        val session = session()
        session.typeText("world")
        val first = session.complete()
        assertTrue(first.consumed)
        assertEquals(listOf("world"), orderedCommits(first))
        assertFalse(session.complete().consumed)
        assertEquals(emptyList<String>(), orderedCommits(session.complete()))
    }

    @Test fun backspaceEditsWordBeforeDelegatingToEditor() {
        val session = session()
        session.typeText("hel")
        assertTrue(session.backspace().consumed)
        assertEquals("he", session.state.value.wordBuffer)
        session.backspace()
        session.backspace()
        assertFalse(session.backspace().consumed)
        assertTrue(session.state.value.candidates.isEmpty())
    }

    @Test fun suggestionsPreserveTitleUpperAndMixedTypedCasing() {
        val session = session()
        session.typeText("Hel")
        assertEquals(listOf("Held", "Help", "Hello"), session.state.value.candidates)
        session.reset()
        session.typeText("HEL")
        assertEquals(listOf("HELD", "HELP", "HELLO"), session.state.value.candidates)
        session.reset()
        session.typeText("hEl")
        assertEquals(listOf("hEld", "hElp", "hEllo"), session.state.value.candidates)
    }

    @Test fun disablingSuggestionsClearsStateAndNeverQueriesProvider() {
        var allowed = true
        val provider = object : EnglishPrefixProvider {
            override fun suggest(prefix: String, limit: Int): List<String> {
                check(allowed) { "Restricted editor must never query a lexicon" }
                return listOf("hello")
            }
        }
        val session = EnglishWordSession(provider)
        session.typeText("he")
        allowed = false
        session.configureSuggestions(false)
        assertTrue(session.state.value.wordBuffer.isEmpty())
        assertTrue(session.state.value.candidates.isEmpty())
        assertEquals(listOf("SeCrEt"), orderedCommits(session.typeText("SeCrEt")))
        assertEquals(emptyList<String>(), orderedCommits(session.select(0)))
        assertFalse(session.backspace().consumed)
        assertFalse(session.complete().consumed)
    }

    @Test fun reEnablingSuggestionsDoesNotRestorePreviousEditorWord() {
        val session = session()
        session.typeText("hel")
        session.configureSuggestions(false)
        session.typeText("secret")
        session.configureSuggestions(true)
        assertTrue(session.state.value.wordBuffer.isEmpty())
        session.typeText("wo")
        assertEquals(listOf("world"), session.state.value.candidates)
    }

    @Test fun resetDropsStaleCompositionAndCandidatesWithoutCommit() {
        val session = session()
        session.typeText("hel")
        session.reset()
        assertEquals(emptyList<String>(), orderedCommits(session.complete()))
        assertTrue(session.state.value.candidates.isEmpty())
    }

    @Test fun invalidSelectionHasNoEditorEffectOrStateMutation() {
        val session = session()
        session.typeText("hel")
        val before = session.state.value
        assertFalse(session.select(-1).consumed)
        assertFalse(session.select(30).consumed)
        assertEquals(before, session.state.value)
    }

    @Test fun nonLettersFlushCurrentTokenAndCommitLiterally() {
        val session = session()
        session.typeText("hel")
        assertEquals(listOf("hel", "123"), orderedCommits(session.typeText("123")))
        assertTrue(session.state.value.wordBuffer.isEmpty())
    }

    @Test fun overlongWordCommitsLiterallyWithoutUnboundedComposition() {
        val session = session()
        val text = "a".repeat(65)
        assertEquals(listOf(text), orderedCommits(session.typeText(text)))
        assertTrue(session.state.value.wordBuffer.isEmpty())
        assertTrue(session.state.value.candidates.isEmpty())
    }
}
