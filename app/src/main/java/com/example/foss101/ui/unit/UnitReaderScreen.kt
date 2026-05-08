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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
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
import androidx.compose.material3.OutlinedTextField
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
import Grade
import RubricCriterion
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
                onAnswerChanged = viewModel::onAnswerChanged,
                onSubmitAnswer = viewModel::submitAnswer,
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
    onAnswerChanged: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
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

            // F3 — open-ended answer input. F4 — submit + render grade output.
            // Decide before Calibrate (Loop step 3 → 4) so the consensus
            // signal doesn't prime the answer.
            DecisionAnswerSection(
                state = state,
                onAnswerChanged = onAnswerChanged,
                onSubmit = onSubmitAnswer
            )

            state.gradeResult?.let { result ->
                GradeOutputSection(
                    grades = result.grades,
                    flagged = result.flagged,
                    rubricCriteriaById = unit.rubric?.criteria.orEmpty().associateBy { it.id }
                )
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

        if (state.isCompleted && state.gradeResult == null) {
            // Completed in a prior session — the grade output isn't loaded
            // (we don't fetch grades on unit open today), so just confirm.
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
                    text = "You completed this unit in a previous session.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
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
private fun DecisionAnswerSection(
    state: UnitReaderUiState.Loaded,
    onAnswerChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.answerDraft,
            onValueChange = onAnswerChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            label = { Text("Your answer") },
            placeholder = {
                Text(
                    "Be specific about what you'd measure, what you'd ignore, " +
                        "and where your estimate might still be wrong."
                )
            },
            enabled = !state.submitInProgress,
            minLines = 4,
            maxLines = 12
        )

        val canSubmit = state.answerDraft.trim().isNotEmpty() && !state.submitInProgress
        val ctaLabel = when {
            state.submitInProgress -> "Grading…"
            state.gradeResult != null -> "Re-submit"
            else -> "Submit answer"
        }
        PrimaryActionButton(
            text = ctaLabel,
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit
        )

        state.submitFailure?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GradeOutputSection(
    grades: List<Grade>,
    flagged: Boolean,
    rubricCriteriaById: Map<Long, RubricCriterion>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = "Your grade")

        if (flagged) {
            // T2-B — flagged means the grader couldn't grade fairly, so
            // the per-criterion pass/fail isn't reliable. Surface the
            // calibration tags + sources below as the canonical answer
            // and tell the user explicitly. v1 doesn't yet store a
            // separate authored canonical answer; tracked for later.
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Review needed",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "We're not confident enough to grade this fairly. " +
                            "Compare your answer to the calibration tags and sources below, then try again.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Sort grades by the criterion's authored position so the UI
        // mirrors the rubric order, not the order grades came back in.
        val ordered = grades.sortedBy { rubricCriteriaById[it.criterionId]?.position ?: Int.MAX_VALUE }
        ordered.forEach { grade ->
            val criterion = rubricCriteriaById[grade.criterionId]
            GradeRow(grade = grade, criterionText = criterion?.text ?: "Criterion ${grade.criterionId}")
        }
    }
}

@Composable
private fun GradeRow(
    grade: Grade,
    criterionText: String
) {
    val tint = if (grade.met) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (grade.met) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = if (grade.met) "Met" else "Not met",
                    tint = tint
                )
                Text(
                    text = criterionText,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = {},
                    label = { Text("${(grade.confidence * 100).toInt()}%") }
                )
            }
            Text(
                text = grade.rationale,
                style = MaterialTheme.typography.bodyMedium
            )
            if (grade.answerQuote.isNotBlank()) {
                Text(
                    text = "“${grade.answerQuote}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
