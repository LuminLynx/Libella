package com.example.foss101.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private val TabletContentMaxWidth = 960.dp

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
        title = uiState.term?.term ?: "Loading...",
        subtitle = uiState.term?.slug ?: ""
    ) { contentPadding ->
        when {
            uiState.isLoading -> {
                CenteredDetailsContainer(contentPadding) {
                    LoadingState(message = "Loading term details...")
                }
            }

            uiState.errorMessage != null -> {
                CenteredDetailsContainer(contentPadding) {
                    ErrorState(message = uiState.errorMessage)
                }
            }

            uiState.term == null -> {
                CenteredDetailsContainer(contentPadding) {
                    EmptyState(message = "The requested term could not be located.")
                }
            }

            else -> {
                TermDetailsContent(
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
}

@Composable
private fun CenteredDetailsContainer(
    contentPadding: PaddingValues,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .screenContentPadding(contentPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = TabletContentMaxWidth)
        ) {
            content()
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
    val explanation = term.explanation
    val humor = term.humor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .screenContentPadding(contentPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = TabletContentMaxWidth)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Definition",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = term.definition,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = "Category: ${displayCategoryName(term.categoryId)}",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )

                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = "Controversy: ${displayControversyLevel(term.controversyLevel)}",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

            if (!explanation.isNullOrBlank()) {
                DetailSectionCard(
                    title = "Explanation",
                    content = explanation
                )
            }

            if (!humor.isNullOrBlank()) {
                DetailSectionCard(
                    title = "Humor",
                    content = humor
                )
            }

            if (term.seeAlso.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(title = "See Also")

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        term.seeAlso.forEach { related ->
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = related,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
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
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = tag,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }
            }

            SecondaryLearningModulesSection(
                scenarioState = scenarioState,
                challengeState = challengeState,
                onGenerateScenario = onGenerateScenario,
                onRefreshScenario = onRefreshScenario,
                onGenerateChallenge = onGenerateChallenge,
                onRefreshChallenge = onRefreshChallenge
            )
        }
    }
}

@Composable
private fun SecondaryLearningModulesSection(
    scenarioState: ArtifactUiState<LearningScenario>,
    challengeState: ArtifactUiState<LearningChallenge>,
    onGenerateScenario: () -> Unit,
    onRefreshScenario: () -> Unit,
    onGenerateChallenge: () -> Unit,
    onRefreshChallenge: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Optional AI Learning Modules",
                style = MaterialTheme.typography.titleMedium,
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
        PrimaryActionButton(
            text = "Generate Scenario",
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth()
        )
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
        PrimaryActionButton(
            text = "Generate Challenge",
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GeneratedScenarioCard(
    result: GeneratedArtifactResult<LearningScenario>,
    onRefresh: () -> Unit
) {
    val artifact = result.artifact
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = artifact.title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(text = "Difficulty: ${artifact.difficulty}")
            Text(text = artifact.context)
            Text(text = "Objective: ${artifact.objective}")
            Text(text = "Tasks:\n- ${artifact.tasks.joinToString("\n- ")}")
            Text(text = "Reflect:\n- ${artifact.reflectionQuestions.joinToString("\n- ")}")
            Text(text = if (result.cached) "Loaded from cache" else "Freshly generated")
            PrimaryActionButton(
                text = "Regenerate Scenario",
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GeneratedChallengeCard(
    result: GeneratedArtifactResult<LearningChallenge>,
    onRefresh: () -> Unit
) {
    val artifact = result.artifact
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = artifact.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "Difficulty: ${artifact.difficulty}")
            Text(text = artifact.prompt)
            Text(text = "Success criteria:\n- ${artifact.successCriteria.joinToString("\n- ")}")
            Text(text = "Hint: ${artifact.hint}")
            Text(text = if (result.cached) "Loaded from cache" else "Freshly generated")
            PrimaryActionButton(
                text = "Regenerate Challenge",
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth()
            )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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

private fun displayCategoryName(categoryId: String): String {
    return when (categoryId) {
        "ai_fundamentals" -> "AI Fundamentals"
        "llm_prompting" -> "LLM Concepts"
        "deployment_ops" -> "Inference & Serving"
        "ml_training" -> "Data & Training"
        "safety_eval" -> "AI Safety"
        "cat-ml-foundations" -> "ML Foundations"
        "cat-llm-concepts" -> "LLM Concepts"
        "cat-inference-serving" -> "Inference & Serving"
        "cat-data-training" -> "Data & Training"
        "cat-ai-safety" -> "AI Safety"
        else -> categoryId
    }
}

private fun displayControversyLevel(level: Int): String {
    return when (level) {
        0 -> "Low"
        1 -> "Moderate"
        2 -> "High"
        3 -> "Very High"
        else -> "Unknown"
    }
}