package com.example.foss101.ui.preview

/**
 * A deliberately simplified tokenizer used for the Bundle 0 proof.
 *
 * Rules (in order):
 *  1. Whitespace separates tokens.
 *  2. Punctuation is split off as its own token.
 *  3. Words longer than 6 characters are split into chunks that mimic subword behaviour
 *     (3-char prefix + remaining tail, recursively for very long words).
 *  4. Each token gets a stable, deterministic integer id (positive, < 100_000) derived
 *     from a small hash so the demo feels real without bundling a vocabulary.
 *
 * This is NOT a real BPE tokenizer. It is intentionally approximate: it teaches the
 * concept of tokenization (text → discrete pieces → integer ids) without claiming
 * fidelity to any specific model. The proof page must label it as such.
 */
object SimpleTokenizer {

    private const val MAX_WORD_BEFORE_SPLIT = 6
    private const val SUBWORD_CHUNK = 3

    private val punctuationRegex = Regex("([\\p{Punct}])")

    data class Token(val text: String, val id: Int)

    fun tokenize(input: String): List<Token> {
        if (input.isBlank()) return emptyList()

        val pieces = mutableListOf<String>()
        for (rawChunk in input.trim().split(Regex("\\s+"))) {
            val withSpacedPunct = rawChunk.replace(punctuationRegex, " $1 ")
            withSpacedPunct.split(Regex("\\s+"))
                .filter { it.isNotEmpty() }
                .forEach { pieces += subwordSplit(it) }
        }

        return pieces.map { Token(text = it, id = stableId(it)) }
    }

    private fun subwordSplit(piece: String): List<String> {
        // Punctuation and short pieces are not split.
        if (piece.length <= MAX_WORD_BEFORE_SPLIT) return listOf(piece)
        if (piece.matches(Regex("\\p{Punct}+"))) return listOf(piece)

        val chunks = mutableListOf<String>()
        var remaining = piece
        // First chunk keeps natural casing (no leading marker).
        chunks += remaining.take(SUBWORD_CHUNK)
        remaining = remaining.drop(SUBWORD_CHUNK)
        // Subsequent chunks are prefixed with ▁ to visually mark continuation,
        // mimicking how SentencePiece/BPE display continuation pieces.
        while (remaining.isNotEmpty()) {
            val take = minOf(SUBWORD_CHUNK, remaining.length)
            chunks += "▁" + remaining.take(take)
            remaining = remaining.drop(take)
        }
        return chunks
    }

    private fun stableId(token: String): Int {
        // Small deterministic hash mapped into a believable token-id range.
        var h = 2166136261.toInt()
        for (c in token) {
            h = h xor c.code
            h *= 16777619
        }
        val unsigned = h.toLong() and 0xFFFFFFFFL
        return (unsigned % 99000).toInt() + 1000
    }
}
