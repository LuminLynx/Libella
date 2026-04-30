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
 * Pitfall demo: token boundaries fragment compound words.
 *
 * The user types a CamelCase compound (default "JavaScript"). The tokenizer
 * splits it into pieces. The implication — biases learned from each piece can
 * leak into model outputs — is shown right next to the demo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JavaScriptSplitDemo() {
    var input by remember { mutableStateOf("JavaScript") }
    val tokens = remember(input) { SimpleTokenizer.tokenize(input) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Type a compound or rare word and see how it fragments.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Word") },
            modifier = Modifier.fillMaxWidth()
        )

        if (tokens.isEmpty()) {
            Text("(empty)", style = MaterialTheme.typography.bodySmall)
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tokens.forEach { token ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = token.text,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        val implication = if (tokens.size > 1) {
            "The model never sees \"$input\" as a single concept. It sees the pieces. Whatever associations it learned for each piece (tone, language, frequency, biases) leak into how it handles your word."
        } else {
            "This input fits in one token, so the model treats it atomically. Try a compound word like JavaScript, GitHub, or a rare technical term."
        }
        Text(
            text = implication,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Pitfall demo: emoji and unicode blow up token counts.
 *
 * Two side-by-side counters: a baseline plain-text sentence vs. the same
 * sentence with emoji appended. The user toggles the emoji on/off (or types
 * their own) and sees the multiplier.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmojiCostDemo() {
    val plain = "I love AI"
    var withEmoji by remember { mutableStateOf("I love AI 🤖🧠✨🎉") }

    val plainTokens = remember { SimpleTokenizer.tokenize(plain).size }
    val emojiTokens = remember(withEmoji) { SimpleTokenizer.tokenize(withEmoji).size }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "The same sentence with and without emoji. Type to add more.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = withEmoji,
            onValueChange = { withEmoji = it },
            label = { Text("With emoji") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CounterColumn(label = "plain", text = plain, count = plainTokens)
            CounterColumn(label = "with emoji", text = withEmoji, count = emojiTokens)
            val multiplier = if (plainTokens == 0) "—" else String.format("%.1f×", emojiTokens.toFloat() / plainTokens)
            CounterColumn(label = "multiplier", text = "", count = -1, valueOverride = multiplier)
        }

        Text(
            text = "Real tokenizers (BPE / SentencePiece) often spend 2–4 tokens per emoji. " +
                "Long unicode-heavy text — code with special characters, multilingual docs, or " +
                "emoji-rich chats — costs significantly more than English prose to send to a model.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CounterColumn(label: String, text: String, count: Int, valueOverride: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = valueOverride ?: count.toString(),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (text.isNotEmpty()) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
