package com.example.foss101.ui.preview.bite

import androidx.compose.runtime.Composable
import com.example.foss101.ui.preview.BpeWalkthrough
import com.example.foss101.ui.preview.EmojiCostDemo
import com.example.foss101.ui.preview.JavaScriptSplitDemo
import com.example.foss101.ui.preview.ProblemComparison
import com.example.foss101.ui.preview.TokenizerPlayground

/**
 * The Tokenization bite feed.
 *
 * Six bites in order:
 *  1. The problem (3-tab compare)
 *  2. How BPE merges (stepper)
 *  3. Try it (playground)
 *  4. Pitfall: compound words (live demo)
 *  5. Pitfall: emoji cost (live demo)
 *  6. Quick check (3 MCQs)
 *
 * Each bite reuses an existing widget verbatim — the bite-feed shell is just
 * a different consumption shape over the same content units. That's exactly
 * what the future schema is meant to support: a section is a payload + widget
 * kind, and the same payload can render in stacked-page mode or bite-feed mode.
 */
fun tokenizationBites(): List<Bite> = listOf(
    Bite(
        kicker = "Why?",
        title = "Same sentence, three ways",
        hook = "Tap each tab to see what breaks.",
        content = { ProblemComparison() }
    ),
    Bite(
        kicker = "How",
        title = "Watch BPE merge letters into tokens",
        hook = "Tap Next — the corpus retokenizes step by step.",
        content = { BpeWalkthrough() }
    ),
    Bite(
        kicker = "Try",
        title = "Tokenize anything",
        hook = "Pick a preset or type your own. The counters update live.",
        content = { TokenizerPlayground() }
    ),
    Bite(
        kicker = "Gotcha #1",
        title = "Compound words split unexpectedly",
        hook = "Type any CamelCase word — see how it fragments.",
        content = { JavaScriptSplitDemo() }
    ),
    Bite(
        kicker = "Gotcha #2",
        title = "Emojis are surprisingly expensive",
        hook = "Same sentence, with and without emoji. Check the multiplier.",
        content = { EmojiCostDemo() }
    ),
    Bite(
        kicker = "Check",
        title = "Three quick questions",
        hook = "Lock in your understanding before the next concept.",
        content = { McqCheckpoint(tokenizationMcqs()) }
    )
)

@Composable
private fun keepAvailable() {
    // Touchpoint to keep IDE imports happy if a bite ever drops a widget reference.
    // Compose-no-op.
}

private fun tokenizationMcqs(): List<Mcq> = listOf(
    Mcq(
        question = "Why don't modern LLMs tokenize at the character level?",
        options = listOf(
            "Characters can't represent every language.",
            "Sequences would be far too long, making each step too expensive.",
            "Models can't learn from individual characters.",
            "Character vocabularies are too large to store."
        ),
        correctIndex = 1,
        explanation = "Character-level inputs make every short message hundreds of steps long. " +
            "Subword tokenization keeps sequences short while still handling unseen words."
    ),
    Mcq(
        question = "What is BPE doing during training?",
        options = listOf(
            "Counting which words appear most often.",
            "Encrypting text so the model can't memorise it.",
            "Repeatedly merging the most frequent adjacent pair into a single token.",
            "Translating text into a numeric format."
        ),
        correctIndex = 2,
        explanation = "Byte-Pair Encoding starts with characters and grows the vocabulary by " +
            "merging the pair that occurs most often. Common subwords end up as single tokens."
    ),
    Mcq(
        question = "Why does a sentence with emojis cost more tokens?",
        options = listOf(
            "Emojis are stored as images.",
            "The tokenizer's vocabulary was learned mostly on plain text, so emojis fall back to multiple unicode pieces.",
            "Emojis are private characters that bypass the tokenizer.",
            "Models charge extra for non-text inputs."
        ),
        correctIndex = 1,
        explanation = "Emojis aren't in the trained vocabulary as single tokens, so the tokenizer " +
            "splits them into 2–4 byte-level pieces each. Long emoji-heavy text costs noticeably more."
    )
)
