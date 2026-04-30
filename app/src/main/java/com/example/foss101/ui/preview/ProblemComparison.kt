package com.example.foss101.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Interactive "why this matters" section.
 *
 * Three tabs walk the user through three ways to break a sentence into model input
 * (characters / words / subword tokens). Each tab visualises the result and gives
 * a verdict so the user feels why the previous approaches fail and why subword
 * tokenization is the practical answer.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProblemComparison() {
    val sentence = "Tokenize JavaScript! 🙂"
    val tabs = listOf("As characters", "As words", "As tokens")
    var selected by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = sentence,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }

        PrimaryTabRow(selectedTabIndex = selected) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selected == index,
                    onClick = { selected = index },
                    text = { Text(label) }
                )
            }
        }

        when (selected) {
            0 -> CharactersView(sentence)
            1 -> WordsView(sentence)
            else -> TokensView(sentence)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CharactersView(sentence: String) {
    val units = sentence.toList().map { it.toString() }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            units.forEach { ch ->
                MiniChip(text = if (ch == " ") "·" else ch)
            }
        }
        VerdictRow(
            ok = false,
            count = units.size,
            unit = "characters",
            note = "Sequence is much longer than the model would need to process. Every step costs a forward pass."
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordsView(sentence: String) {
    val knownVocab = setOf("Tokenize", "Hello", "world", "the", "and")
    val pieces = sentence
        .replace("!", " !")
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            pieces.forEach { word ->
                val ok = word in knownVocab
                MiniChip(
                    text = word,
                    container = if (ok) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    onContainer = if (ok) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        val unknown = pieces.filter { it !in knownVocab }
        VerdictRow(
            ok = false,
            count = pieces.size,
            unit = "words",
            note = if (unknown.isEmpty()) {
                "All words known — but most real text contains rare or compound words that aren't in the vocabulary."
            } else {
                "Words like ${unknown.joinToString(", ")} aren't in the model's vocabulary at all. Word-level fails on rare words, names, and code."
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TokensView(sentence: String) {
    val tokens = SimpleTokenizer.tokenize(sentence)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            tokens.forEach { token ->
                MiniChip(
                    text = token.text,
                    container = MaterialTheme.colorScheme.primaryContainer,
                    onContainer = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        VerdictRow(
            ok = true,
            count = tokens.size,
            unit = "tokens",
            note = "Every piece comes from a fixed vocabulary. Long or unseen words split into known subwords. This is what real LLMs do."
        )
    }
}

@Composable
private fun MiniChip(
    text: String,
    container: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    onContainer: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(color = container, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = onContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun VerdictRow(ok: Boolean, count: Int, unit: String, note: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (ok) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "$count $unit",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
