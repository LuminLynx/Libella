package com.example.foss101.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Interactive BPE walkthrough.
 *
 * Pre-computed snapshots show how Byte-Pair Encoding builds a vocabulary by
 * repeatedly merging the most frequent adjacent pair. The user steps forwards
 * and backwards; each step shows:
 *  - the corpus broken into the current token boundaries
 *  - the new merge highlighted
 *  - the vocabulary growing
 *  - a one-line description of what happened
 *
 * The corpus is tiny on purpose ("low low lower newer wider") so the merges
 * are easy to follow visually.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BpeWalkthrough() {
    val steps = remember { bpeSteps() }
    var index by remember { mutableIntStateOf(0) }
    val step = steps[index]

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Step ${index + 1} of ${steps.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = step.description, style = MaterialTheme.typography.bodyMedium)

        // Corpus rendered as flow-of-tokens with the new-merge token highlighted.
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FlowRow(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                step.tokens.forEach { token ->
                    val isNew = step.highlightToken != null && token == step.highlightToken
                    BpeTokenChip(text = token, highlighted = isNew)
                }
            }
        }

        // Vocabulary state.
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Vocabulary (${step.vocabulary.size})",
                style = MaterialTheme.typography.labelMedium
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                step.vocabulary.forEach { vocabPiece ->
                    val isNew = vocabPiece == step.highlightToken
                    BpeTokenChip(text = vocabPiece, highlighted = isNew, dim = !isNew)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { if (index > 0) index-- },
                enabled = index > 0,
                modifier = Modifier.weight(1f)
            ) { Text("Previous") }
            OutlinedButton(
                onClick = { if (index < steps.lastIndex) index++ },
                enabled = index < steps.lastIndex,
                modifier = Modifier.weight(1f)
            ) { Text(if (index == steps.lastIndex) "Done" else "Next") }
        }
    }
}

@Composable
private fun BpeTokenChip(text: String, highlighted: Boolean, dim: Boolean = false) {
    val container = when {
        highlighted -> MaterialTheme.colorScheme.primary
        dim -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val onContainer = when {
        highlighted -> MaterialTheme.colorScheme.onPrimary
        dim -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    Surface(color = container, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = onContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private data class BpeStep(
    val description: String,
    val tokens: List<String>,
    val vocabulary: List<String>,
    val highlightToken: String?
)

/**
 * A small, hand-tuned BPE story for "low low lower newer wider".
 * Each step's tokens / vocabulary are precomputed so the demo is deterministic
 * and easy to follow. End-of-word markers are omitted for clarity.
 */
private fun bpeSteps(): List<BpeStep> {
    val baseChars = listOf("l", "o", "w", "e", "r", "n", "i", "d")
    val initialTokens = "low low lower newer wider"
        .split(" ")
        .flatMap { word -> word.map { it.toString() } + " " }
        .dropLast(1) // drop trailing space

    val afterLo = initialTokens
        .joinToString("|")
        .replace("l|o", "lo")
        .split("|")

    val afterLow = afterLo
        .joinToString("|")
        .replace("lo|w", "low")
        .split("|")

    val afterEr = afterLow
        .joinToString("|")
        .replace("e|r", "er")
        .split("|")

    val afterNew = afterEr
        .joinToString("|")
        .replace("n|e|w", "new")
        .split("|")

    return listOf(
        BpeStep(
            description = "Start: every character is its own token. Whitespace separates words.",
            tokens = initialTokens,
            vocabulary = baseChars + listOf(" "),
            highlightToken = null
        ),
        BpeStep(
            description = "The most frequent adjacent pair is l + o. Merge it into a new token \"lo\".",
            tokens = afterLo,
            vocabulary = baseChars + listOf(" ", "lo"),
            highlightToken = "lo"
        ),
        BpeStep(
            description = "Now lo + w is the most frequent pair. Merge into \"low\".",
            tokens = afterLow,
            vocabulary = baseChars + listOf(" ", "lo", "low"),
            highlightToken = "low"
        ),
        BpeStep(
            description = "e + r appears in lower, newer, wider. Merge into \"er\".",
            tokens = afterEr,
            vocabulary = baseChars + listOf(" ", "lo", "low", "er"),
            highlightToken = "er"
        ),
        BpeStep(
            description = "n + e + w merges into \"new\". Common subwords keep collapsing.",
            tokens = afterNew,
            vocabulary = baseChars + listOf(" ", "lo", "low", "er", "new"),
            highlightToken = "new"
        ),
        BpeStep(
            description = "After many more merges, common pieces (low, er, new, ing, ed…) are single tokens. " +
                "New text is split into the longest matching pieces from this learned vocabulary.",
            tokens = afterNew,
            vocabulary = baseChars + listOf(" ", "lo", "low", "er", "new", "wid", "ing", "ed"),
            highlightToken = null
        )
    )
}
