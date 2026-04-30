package com.example.foss101.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Tokenizer playground.
 *
 * Free-text input + a row of language preset chips. Tapping a preset replaces the
 * input with a phrase in that language and the tokenization updates live, so the
 * user feels how token count varies wildly across languages and content types
 * (English vs Spanish vs Japanese vs Python vs emoji).
 *
 * Token chips show the token text (monospace) and the synthetic id below.
 * Continuation pieces (▁-prefixed) get a different colour to mimic the BPE
 * "this is a piece, not a whole word" cue.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TokenizerPlayground() {
    val presets = listOf(
        Preset("English", "Tokenization splits text into pieces."),
        Preset("Spanish", "La tokenización divide el texto en piezas."),
        Preset("Japanese", "トークン化はテキストを断片に分割します。"),
        Preset("Python", "for token in tokens: print(token.id)"),
        Preset("Emoji", "I love AI 🤖🧠✨ — it works! 🎉")
    )
    var input by remember { mutableStateOf(presets[0].text) }
    val tokens = remember(input) { SimpleTokenizer.tokenize(input) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Tap a preset to load a sample, or edit the field directly. The token list and counters update as you type.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { preset ->
                AssistChip(
                    onClick = { input = preset.text },
                    label = { Text(preset.label) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Input") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Counter(label = "tokens", value = tokens.size.toString())
            Counter(label = "characters", value = input.length.toString())
            Counter(
                label = "tokens / 100 chars",
                value = if (input.isEmpty()) "—"
                else String.format("%.1f", tokens.size.toFloat() / input.length * 100)
            )
        }

        if (tokens.isEmpty()) {
            Text(
                text = "Empty input.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tokens.forEach { TokenChip(it) }
            }
        }
    }
}

private data class Preset(val label: String, val text: String)

@Composable
private fun Counter(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TokenChip(token: SimpleTokenizer.Token) {
    val isContinuation = token.text.startsWith("▁")
    val container = if (isContinuation) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val onContainer = if (isContinuation) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    Surface(color = container, shape = RoundedCornerShape(8.dp)) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = token.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = onContainer
            )
            Text(
                text = "id ${token.id}",
                style = MaterialTheme.typography.labelSmall,
                color = onContainer
            )
        }
    }
}
