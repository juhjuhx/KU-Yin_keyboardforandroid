package com.example.ime.engine

import java.io.Reader
import java.util.Locale

/** Offline unigram completion; callers supply the normalized current token. */
fun interface EnglishPrefixProvider {
    fun suggest(prefix: String, limit: Int): List<String>
}

/** Small, immutable frequency-ordered list; no editor, Android, native or learning dependency. */
class EnglishLexicon private constructor(private val words: List<String>) : EnglishPrefixProvider {
    override fun suggest(prefix: String, limit: Int): List<String> {
        if (prefix.isEmpty() || limit <= 0 || !prefix.all { it in 'a'..'z' || it in 'A'..'Z' }) {
            return emptyList()
        }
        val normalized = prefix.lowercase(Locale.ROOT)
        return words.asSequence().filter { it.startsWith(normalized) }.take(limit).toList()
    }

    companion object {
        const val ASSET_PATH = "english/word_frequency.tsv"
        private const val MAX_ENTRIES = 10_000

        /** Caller owns the Reader. Invalid bundled data fails as a whole, never partially loads. */
        fun fromTsv(reader: Reader): EnglishLexicon {
            val frequencies = mutableMapOf<String, Int>()
            reader.buffered().lineSequence().forEachIndexed { index, line ->
                require(index < MAX_ENTRIES) { "English lexicon exceeds its entry bound" }
                val columns = line.split('\t')
                require(columns.size == 2) { "Invalid English lexicon row ${index + 1}" }
                val word = columns[0]
                val frequency = columns[1].toIntOrNull()
                require(word.isNotEmpty() && word.length <= 64 && word.all { it in 'a'..'z' } &&
                    frequency != null && frequency in 1..255) {
                    "Invalid English lexicon row ${index + 1}"
                }
                frequencies[word] = maxOf(frequency, frequencies[word] ?: 0)
            }
            return EnglishLexicon(frequencies.entries
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .map { it.key })
        }
    }
}
