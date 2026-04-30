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
 * Bundle 0 v2 — interactive Tokenization proof.
 *
 * Every section (except the brief TL;DR and the Sources list) is a stand-alone
 * interactive widget. The text scaffolding around each widget is deliberately
 * minimal — the goal is for the user to learn by doing, not by reading.
 *
 * Section order:
 *   1. TL;DR (single sentence)
 *   2. The problem (3-tab Characters / Words / Tokens comparison)
 *   3. How BPE works (stepper through a tiny corpus)
 *   4. Try it (tokenizer playground with language presets)
 *   5. Pitfall: compound-word fragmentation (live)
 *   6. Pitfall: emoji cost (live)
 *   7. Sources (tappable cards)
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
            InteractionCard(
                title = "The problem",
                subtitle = "Tap each tab to see what breaks."
            ) { ProblemComparison() }
            InteractionCard(
                title = "How BPE builds a vocabulary",
                subtitle = "Step through a tiny corpus and watch tokens merge."
            ) { BpeWalkthrough() }
            InteractionCard(
                title = "Try it",
                subtitle = "Type or pick a preset; tokens update live."
            ) { TokenizerPlayground() }
            InteractionCard(
                title = "Pitfall · compound words split unexpectedly",
                subtitle = null
            ) { JavaScriptSplitDemo() }
            InteractionCard(
                title = "Pitfall · emojis are expensive",
                subtitle = null
            ) { EmojiCostDemo() }
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "TL;DR",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Tokenization turns text into the discrete pieces a language model actually operates on.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun InteractionCard(
    title: String,
    subtitle: String?,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
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
                text = "Each section in a future curated page will cite the specific source it draws from.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SourceRow(
                kind = "Wikipedia",
                label = "Lexical analysis — the broader concept of breaking text into tokens",
                url = "https://en.wikipedia.org/wiki/Lexical_analysis"
            ) { uriHandler.openUri(it) }
            SourceRow(
                kind = "arXiv · BPE paper",
                label = "Sennrich, Haddow & Birch (2016) — Neural Machine Translation of Rare Words with Subword Units",
                url = "https://arxiv.org/abs/1508.07909"
            ) { uriHandler.openUri(it) }
            SourceRow(
                kind = "Video",
                label = "Karpathy — Let's build the GPT Tokenizer",
                url = "https://www.youtube.com/watch?v=zduSFxRajkE"
            ) { uriHandler.openUri(it) }
            SourceRow(
                kind = "Course",
                label = "Hugging Face — Tokenizers chapter",
                url = "https://huggingface.co/learn/nlp-course/chapter6/1"
            ) { uriHandler.openUri(it) }
        }
    }
}

@Composable
private fun SourceRow(
    kind: String,
    label: String,
    url: String,
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
        text = "Bundle 0 v2 — every section is an interaction. If this shape feels right, " +
            "Bundle A introduces a schema where every section is a typed widget kind + payload.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
