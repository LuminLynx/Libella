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
 * Five bites in order:
 *  1. The problem (3-tab compare)
 *  2. How BPE merges (stepper)
 *  3. Try it (playground)
 *  4. Pitfall: compound words (live demo)
 *  5. Pitfall: emoji cost (live demo)
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
    )
)

@Composable
private fun keepAvailable() {
    // Touchpoint to keep IDE imports happy if a bite ever drops a widget reference.
    // Compose-no-op.
}
