package com.example.foss101.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

/**
 * Bundle 0 proof screen: a hand-authored, rich learning page for "Tokenization".
 *
 * No data model, no AI generation, no migration. Pure static UI proof of what a
 * canonical term page should *feel* like when populated by the future content
 * pipeline (curated sources + grounded generation).
 *
 * Section structure mirrors the schema we will adopt in Bundle A:
 *  - TL;DR
 *  - Why this matters
 *  - How it works
 *  - Concrete example  (with embedded interactive widget)
 *  - Pitfalls
 *  - Sources
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenizationProofScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tokenization", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Concept preview · Bundle 0",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TldrCard()
            SectionCard(title = "Why this matters", body = WHY_BODY)
            SectionCard(title = "How it works", body = HOW_BODY)
            ExampleCard()
            PitfallsCard()
            SourcesCard()
            ProofFooter()
        }
    }
}

@Composable
private fun TldrCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "TL;DR",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Tokenization converts raw text into a sequence of discrete units (tokens) " +
                    "that a language model actually operates on. Modern LLMs use subword " +
                    "tokenization — like Byte-Pair Encoding (BPE) or SentencePiece — which " +
                    "balances vocabulary size against sequence length.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ExampleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "Concrete example", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Type any sentence below to see how it breaks into tokens. " +
                    "Each chip is one token; the small number under it is its (synthetic) id.",
                style = MaterialTheme.typography.bodyMedium
            )
            TokenizationWidget()
        }
    }
}

@Composable
private fun PitfallsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Pitfalls / when this fails",
                style = MaterialTheme.typography.titleMedium
            )
            Pitfall(
                title = "Token boundaries shape outputs.",
                body = "A model rarely sees \"JavaScript\" as a single token; it sees \"Java\" + " +
                    "\"Script\". This can subtly leak associations and biases learned from each piece."
            )
            Pitfall(
                title = "Counting tokens ≠ counting words.",
                body = "A 100-word English sentence is typically 130–200 tokens. Non-English " +
                    "languages and code can be 2–3× more, because the tokenizer's vocabulary " +
                    "was learned mostly on English."
            )
            Pitfall(
                title = "Special characters and emojis are expensive.",
                body = "A single emoji often takes 3–4 tokens. A long unicode-heavy string " +
                    "can blow up token counts dramatically."
            )
            Pitfall(
                title = "Different models, different tokenizers.",
                body = "GPT-4's tokenizer is not LLaMA's, which is not Claude's. The same " +
                    "prompt has different costs and different boundary behaviour across models."
            )
        }
    }
}

@Composable
private fun Pitfall(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = "• $title", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun SourcesCard() {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "Sources", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "This page synthesises material from the following sources. Future " +
                    "auto-generated pages will cite them inline per section.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SourceRow(
                label = "Wikipedia — Lexical analysis",
                url = "https://en.wikipedia.org/wiki/Lexical_analysis",
                kind = "Wikipedia",
                onOpen = { uriHandler.openUri(it) }
            )
            SourceRow(
                label = "Sennrich, Haddow & Birch (2016) — Neural Machine Translation of Rare Words with Subword Units",
                url = "https://arxiv.org/abs/1508.07909",
                kind = "arXiv · BPE paper",
                onOpen = { uriHandler.openUri(it) }
            )
            SourceRow(
                label = "Karpathy — Let's build the GPT Tokenizer",
                url = "https://www.youtube.com/watch?v=zduSFxRajkE",
                kind = "Video",
                onOpen = { uriHandler.openUri(it) }
            )
            SourceRow(
                label = "Hugging Face — Tokenizers chapter",
                url = "https://huggingface.co/learn/nlp-course/chapter6/1",
                kind = "Course",
                onOpen = { uriHandler.openUri(it) }
            )
        }
    }
}

@Composable
private fun SourceRow(
    label: String,
    url: String,
    kind: String,
    onOpen: (String) -> Unit
) {
    Card(
        onClick = { onOpen(url) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = kind,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProofFooter() {
    Text(
        text = "Bundle 0 proof — static, hand-authored, no data model. " +
            "If this depth feels right, Bundle A introduces the schema that lets every term page look like this.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private const val WHY_BODY =
    "Models can't process raw characters or whole words efficiently. Character-level input " +
    "produces sequences too long to be practical; word-level input can't handle words that " +
    "weren't seen in training. Tokenization is the bridge: it determines vocabulary size, " +
    "sequence length, multilingual coverage, and even what the model can emit. A bad " +
    "tokenizer caps what the model can do — no architecture or training data can fully " +
    "compensate for it."

private const val HOW_BODY =
    "1. The tokenizer learns a vocabulary of subword pieces from a training corpus, " +
    "typically via Byte-Pair Encoding (BPE) — repeatedly merging the most frequent " +
    "adjacent symbol pairs.\n\n" +
    "2. At inference time, input text is scanned left-to-right and split into the longest " +
    "matching pieces from that vocabulary. Common words become a single token; rare words " +
    "split into multiple subword pieces; truly unseen characters fall through to a " +
    "byte-level fallback.\n\n" +
    "3. Each piece maps to a fixed integer id.\n\n" +
    "4. The model operates on the integer sequence — embeddings → transformer layers → " +
    "output logits. The output is also a sequence of token ids that get converted back " +
    "into text by the same vocabulary."
