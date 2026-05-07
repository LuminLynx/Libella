package com.example.foss101.ui.unit

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.CompletionCache
import com.example.foss101.data.repository.PathRepository
import com.example.foss101.model.CalibrationTag
import com.example.foss101.model.UnitDetail
import com.example.foss101.model.UnitSource
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.MarkdownText
import com.example.foss101.ui.components.PrimaryActionButton
import com.example.foss101.ui.components.SectionHeader
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.viewmodel.UnitReaderEvent
import com.example.foss101.viewmodel.UnitReaderUiState
import com.example.foss101.viewmodel.UnitReaderViewModel

@Composable
fun UnitReaderScreen(
    pathRepository: PathRepository,
    completionCache: CompletionCache,
    unitId: String,
    onAuthExpired: () -> Unit
) {
    val viewModel: UnitReaderViewModel = viewModel(
        key = unitId,
        factory = UnitReaderViewModel.factory(pathRepository, completionCache, unitId)
    )

    val state = viewModel.uiState

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                UnitReaderEvent.AuthExpired -> onAuthExpired()
            }
        }
    }

    val title = when (state) {
        is UnitReaderUiState.Loaded -> state.unit.title
        else -> "Unit"
    }
    val subtitle = when (state) {
        is UnitReaderUiState.Loaded -> "Unit ${state.unit.position}"
        else -> ""
    }

    AppScreenScaffold(title = title, subtitle = subtitle) { contentPadding ->
        when (state) {
            is UnitReaderUiState.Loading -> LoadingBox(modifier = Modifier.screenContentPadding(contentPadding))
            is UnitReaderUiState.Error -> ErrorBox(
                message = if (state.authExpired) "Sign in to continue." else state.message,
                onRetry = viewModel::load,
                modifier = Modifier.screenContentPadding(contentPadding)
            )
            is UnitReaderUiState.Loaded -> LoadedBody(
                state = state,
                onToggleTradeOff = viewModel::toggleTradeOff,
                onToggleDepth = viewModel::toggleDepth,
                onMarkComplete = viewModel::markComplete,
                modifier = Modifier.screenContentPadding(contentPadding)
            )
        }
    }
}

@Composable
private fun LoadedBody(
    state: UnitReaderUiState.Loaded,
    onToggleTradeOff: () -> Unit,
    onToggleDepth: () -> Unit,
    onMarkComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unit = state.unit
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = unit.definition,
            style = MaterialTheme.typography.bodyLarge
        )

        // Order follows the Loop in STRATEGY.md (line 108–115):
        //   2. Bite — read the concept.
        //   3. Decide — answer the prompt before seeing the consensus.
        //   4. Calibrate — settled / contested + sources, after the answer.
        // Trade-off framing and Depth are tap-to-expand reference material
        // (P4 "bite first, depth on tap"). Showing calibration / sources
        // before the decision prompt would prime the answer, which is
        // what P2 ("calibrate, don't bluff") explicitly avoids.

        Section(title = "90-second bite") {
            MarkdownText(markdown = unit.biteMd)
        }

        MarkdownDisclosure(
            label = "Trade-off framing",
            markdown = unit.tradeOffFraming,
            expanded = state.tradeOffExpanded,
            onToggle = onToggleTradeOff
        )

        MarkdownDisclosure(
            label = "Depth",
            markdown = unit.depthMd,
            expanded = state.depthExpanded,
            onToggle = onToggleDepth
        )

        unit.decisionPrompt?.let { prompt ->
            Section(title = "Decision prompt") {
                MarkdownText(markdown = prompt.promptMd)
            }
        }

        if (unit.calibrationTags.isNotEmpty()) {
            Section(title = "Calibration") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    unit.calibrationTags.forEach { tag -> CalibrationTagRow(tag) }
                }
            }
        }

        if (unit.sources.isNotEmpty()) {
            Section(title = "Sources") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    unit.sources.forEach { source -> SourceRow(source) }
                }
            }
        }

        if (state.isCompleted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Marked complete.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            PrimaryActionButton(
                text = if (state.markCompleteInProgress) "Saving…" else "Mark complete",
                onClick = onMarkComplete,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.markCompleteInProgress
            )
        }

        state.markCompleteFailure?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun Section(title: String, body: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionHeader(title = title)
        body()
    }
}

@Composable
private fun MarkdownDisclosure(
    label: String,
    markdown: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    if (markdown.isBlank()) return
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.titleSmall)
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Hide $label" else "Show $label"
            )
        }
        AnimatedVisibility(visible = expanded) {
            MarkdownText(
                markdown = markdown,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun CalibrationTagRow(tag: CalibrationTag) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AssistChip(
            onClick = {},
            label = { Text(tag.tier) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = tierColor(tag.tier)
            )
        )
        Text(
            text = tag.claim,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun tierColor(tier: String): Color {
    val scheme = MaterialTheme.colorScheme
    return when (tier.lowercase()) {
        "settled" -> scheme.primaryContainer
        "contested" -> scheme.tertiaryContainer
        else -> scheme.surfaceVariant
    }
}

@Composable
private fun SourceRow(source: UnitSource) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                runCatching {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = source.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${if (source.primarySource) "Primary · " else ""}${source.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.OpenInNew,
                contentDescription = "Open in browser"
            )
        }
    }
}

@Composable
private fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        PrimaryActionButton(
            text = "Retry",
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
