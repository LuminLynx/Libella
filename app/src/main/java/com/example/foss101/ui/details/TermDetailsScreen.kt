package com.example.foss101.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.GeneratedArtifactResult
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.model.LearningChallenge
import com.example.foss101.model.LearningScenario
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.EmptyState
import com.example.foss101.ui.components.ErrorState
import com.example.foss101.ui.components.LoadingState
import com.example.foss101.ui.components.PrimaryActionButton
import com.example.foss101.ui.components.SectionHeader
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.viewmodel.ArtifactUiState
import com.example.foss101.viewmodel.TermDetailsViewModel

@Composable
fun TermDetailsScreen(
    termId: String? = null,
    repository: GlossaryRepository
) {
    val viewModel: TermDetailsViewModel = viewModel(
        key = termId,
        factory = TermDetailsViewModel.factory(termId = termId, repository = repository)
    )
    val uiState = viewModel.uiState

    AppScreenScaffold(
        title = uiState.term?.term ?: "AI Terms Glossary",
        subtitle = if (uiState.term == null) "Glossary term" else "Canonical term entry"
    ) { contentPadding ->
        when {
            uiState.isLoading -> LoadingState(
                message = "Loading term details...",
                modifier = Modifier.screenContentPadding(contentPadding)
            )

            uiState.errorMessage != null -> ErrorState(
                message = uiState.errorMessage,
                modifier = Modifier.screenContentPadding(contentPadding)
            )

            uiState.term == null -> EmptyState(
                message = "The requested term could not be located.",
                modifier = Modifier.screenContentPadding(contentPadding)
            )

            else -> TermDetailsContent(
                contentPadding = contentPadding,
                term = uiState.term,
                scenarioState = uiState.scenarioState,
                challengeState = uiState.challengeState,
                onGenerateScenario = { viewModel.generateScenario() },
                onRefreshScenario = { viewModel.generateScenario(forceRefresh = true) },
                onGenerateChallenge = { viewModel.generateChallenge() },
                onRefreshChallenge = { viewModel.generateChallenge(forceRefresh = true) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TermDetailsContent(
    contentPadding: PaddingValues,
    term: GlossaryTerm,
    scenarioState: ArtifactUiState<LearningScenario>,
    challengeState: ArtifactUiState<LearningChallenge>,
    onGenerateScenario: () -> Unit,
    onRefreshScenario: () -> Unit,
    onGenerateChallenge: () -> Unit,
    onRefreshChallenge: () -> Unit
) {
    Column(
        modifier = Modifier
            .screenContentPadding(contentPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = term.term,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Slug: ${term.slug}",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
                AssistChip(
                    onClick = { },
                    label = { Text(text = controversyLabel(term.controversyLevel)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = controversyContainerColor(term.controversyLevel),
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }

        DetailSectionCard(title = "Definition", content = term.definition)

        term.humor?.takeIf { it.isNotBlank() }?.let { humorText ->
            DetailSectionCard(title = "Humor", content = humorText)
        }

        if (term.seeAlso.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "See Also")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    term.seeAlso.forEach { relatedSlug ->
                        MetadataPill(text = relatedSlug)
                    }
                }
            }
        }

        if (term.tags.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "Tags")

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    term.tags.forEach { tag ->
                        MetadataPill(text = tag)
                    }
                }
            }
        }

        AuxiliaryLearningSection(
            scenarioState = scenarioState,
            challengeState = challengeState,
            onGenerateScenario = onGenerateScenario,
            onRefreshScenario = onRefreshScenario,
            onGenerateChallenge = onGenerateChallenge,
            onRefreshChallenge = onRefreshChallenge
        )
    }
}

@Composable
private fun AuxiliaryLearningSection(
    scenarioState: ArtifactUiState<LearningScenario>,
    challengeState: ArtifactUiState<LearningChallenge>,
    onGenerateScenario: () -> Unit,
    onRefreshScenario: () -> Unit,
    onGenerateChallenge: () -> Unit,
    onRefreshChallenge: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "AI Learning (Optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "These tools help you practice the term. They are separate from the canonical glossary definition.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ScenarioSection(
                state = scenarioState,
                onGenerate = onGenerateScenario,
                onRefresh = onRefreshScenario
            )

            ChallengeSection(
                state = challengeState,
                onGenerate = onGenerateChallenge,
                onRefresh = onRefreshChallenge
            )
        }
    }
}

@Composable
private fun ScenarioSection(
    state: ArtifactUiState<LearningScenario>,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit
) {
    SectionHeader(title = "AI Learning Scenario")
    when {
        state.isLoading -> LoadingState(message = "Generating scenario...")
        state.errorMessage != null -> ErrorState(message = state.errorMessage)
        state.data == null -> EmptyState(message = "No scenario yet. Generate one to practice this term.")
        else -> GeneratedScenarioCard(result = state.data, onRefresh = onRefresh)
    }
    if (state.data == null && !state.isLoading) {
        PrimaryActionButton(text = "Generate Scenario", onClick = onGenerate, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ChallengeSection(
    state: ArtifactUiState<LearningChallenge>,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit
) {
    SectionHeader(title = "AI Learning Challenge")
    when {
        state.isLoading -> LoadingState(message = "Generating challenge...")
        state.errorMessage != null -> ErrorState(message = state.errorMessage)
        state.data == null -> EmptyState(message = "No challenge yet. Generate one to test your understanding.")
        else -> GeneratedChallengeCard(result = state.data, onRefresh = onRefresh)
    }
    if (state.data == null && !state.isLoading) {
        PrimaryActionButton(text = "Generate Challenge", onClick = onGenerate, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun GeneratedScenarioCard(result: GeneratedArtifactResult<LearningScenario>, onRefresh: () -> Unit) {
    val artifact = result.artifact
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = artifact.title, style = MaterialTheme.typography.titleMedium)
            }
            Text(text = "Difficulty: ${artifact.difficulty}")
            Text(text = artifact.context)
            Text(text = "Objective: ${artifact.objective}")
            Text(text = "Tasks:\n- ${artifact.tasks.joinToString("\n- ")}")
            Text(text = "Reflect:\n- ${artifact.reflectionQuestions.joinToString("\n- ")}")
            Text(text = if (result.cached) "Loaded from cache" else "Freshly generated")
            PrimaryActionButton(text = "Regenerate Scenario", onClick = onRefresh, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun GeneratedChallengeCard(result: GeneratedArtifactResult<LearningChallenge>, onRefresh: () -> Unit) {
    val artifact = result.artifact
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = artifact.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Difficulty: ${artifact.difficulty}")
            Text(text = artifact.prompt)
            Text(text = "Success criteria:\n- ${artifact.successCriteria.joinToString("\n- ")}")
            Text(text = "Hint: ${artifact.hint}")
            Text(text = if (result.cached) "Loaded from cache" else "Freshly generated")
            PrimaryActionButton(text = "Regenerate Challenge", onClick = onRefresh, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(title = title)
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetadataPill(text: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            softWrap = true,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun controversyContainerColor(level: Int) = when (level) {
    0 -> MaterialTheme.colorScheme.secondaryContainer
    1 -> MaterialTheme.colorScheme.tertiaryContainer
    2 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
    else -> MaterialTheme.colorScheme.errorContainer
}

private fun controversyLabel(level: Int): String = when (level) {
    0 -> "Controversy: Low"
    1 -> "Controversy: Moderate"
    2 -> "Controversy: Elevated"
    3 -> "Controversy: High"
    else -> "Controversy: Unknown"
}
