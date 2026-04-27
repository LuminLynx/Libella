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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.AuthRepository
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.ArtifactKind
import com.example.foss101.model.GeneratedArtifactResult
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.model.LearningChallenge
import com.example.foss101.model.LearningCompletion
import com.example.foss101.model.LearningPreset
import com.example.foss101.model.LearningScenario
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.CompletionSheet
import com.example.foss101.ui.components.EmptyState
import com.example.foss101.ui.components.ErrorState
import com.example.foss101.ui.components.LoadingState
import com.example.foss101.ui.components.PresetSelector
import com.example.foss101.ui.components.PrimaryActionButton
import com.example.foss101.ui.components.SecondaryActionButton
import com.example.foss101.ui.components.SectionHeader
import com.example.foss101.ui.components.TagChip
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.viewmodel.ArtifactUiState
import com.example.foss101.viewmodel.TermDetailsViewModel

private val TabletContentMaxWidth = 960.dp

@Composable
fun TermDetailsScreen(
    termId: String? = null,
    repository: GlossaryRepository,
    authRepository: AuthRepository,
    onNavigate: (String) -> Unit
) {
    val viewModel: TermDetailsViewModel = viewModel(
        key = termId,
        factory = TermDetailsViewModel.factory(
            termId = termId,
            repository = repository,
            authRepository = authRepository
        )
    )
    val uiState = viewModel.uiState

    LifecycleResumeEffect(Unit) {
        viewModel.refreshAuthState()
        onPauseOrDispose { }
    }

    AppScreenScaffold(
        title = uiState.term?.term ?: "Loading...",
        subtitle = uiState.term?.categoryId?.let(::displayCategoryName) ?: ""
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
                    isSignedIn = uiState.isSignedIn,
                    scenarioPreset = uiState.scenarioPreset,
                    challengePreset = uiState.challengePreset,
                    scenarioState = uiState.scenarioState,
                    challengeState = uiState.challengeState,
                    onSignInClick = { onNavigate("auth_login") },
                    onScenarioPresetSelected = viewModel::setScenarioPreset,
                    onChallengePresetSelected = viewModel::setChallengePreset,
                    onGenerateScenario = { viewModel.generateScenario() },
                    onRefreshScenario = { viewModel.generateScenario(forceRefresh = true) },
                    onGenerateChallenge = { viewModel.generateChallenge() },
                    onRefreshChallenge = { viewModel.generateChallenge(forceRefresh = true) },
                    onMarkScenarioComplete = { viewModel.openCompletionSheet(ArtifactKind.Scenario) },
                    onMarkChallengeComplete = { viewModel.openCompletionSheet(ArtifactKind.Challenge) }
                )
            }
        }
    }

    val activeSheet = uiState.activeCompletionSheet
    if (activeSheet != null) {
        val artifactState = when (activeSheet) {
            ArtifactKind.Scenario -> uiState.scenarioState
            ArtifactKind.Challenge -> uiState.challengeState
        }
        CompletionSheet(
            kind = activeSheet,
            isSubmitting = artifactState.isSubmittingCompletion,
            errorMessage = artifactState.completionErrorMessage,
            onDismiss = viewModel::dismissCompletionSheet,
            onSubmit = { confidence, notes ->
                viewModel.submitCompletion(activeSheet, confidence, notes)
            }
        )
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
    isSignedIn: Boolean,
    scenarioPreset: LearningPreset,
    challengePreset: LearningPreset,
    scenarioState: ArtifactUiState<LearningScenario>,
    challengeState: ArtifactUiState<LearningChallenge>,
    onSignInClick: () -> Unit,
    onScenarioPresetSelected: (LearningPreset) -> Unit,
    onChallengePresetSelected: (LearningPreset) -> Unit,
    onGenerateScenario: () -> Unit,
    onRefreshScenario: () -> Unit,
    onGenerateChallenge: () -> Unit,
    onRefreshChallenge: () -> Unit,
    onMarkScenarioComplete: () -> Unit,
    onMarkChallengeComplete: () -> Unit
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
                .fillMaxWidth()
                .widthIn(max = TabletContentMaxWidth)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Definition",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = term.definition,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TagChip(label = displayCategoryName(term.categoryId))
                        TagChip(label = "Controversy: ${displayControversyLevel(term.controversyLevel)}")
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
                ChipSection(title = "See Also", labels = term.seeAlso)
            }

            if (term.tags.isNotEmpty()) {
                ChipSection(title = "Tags", labels = term.tags)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LearningModulesSection(
                isSignedIn = isSignedIn,
                scenarioPreset = scenarioPreset,
                challengePreset = challengePreset,
                scenarioState = scenarioState,
                challengeState = challengeState,
                onSignInClick = onSignInClick,
                onScenarioPresetSelected = onScenarioPresetSelected,
                onChallengePresetSelected = onChallengePresetSelected,
                onGenerateScenario = onGenerateScenario,
                onRefreshScenario = onRefreshScenario,
                onGenerateChallenge = onGenerateChallenge,
                onRefreshChallenge = onRefreshChallenge,
                onMarkScenarioComplete = onMarkScenarioComplete,
                onMarkChallengeComplete = onMarkChallengeComplete
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSection(title: String, labels: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = title)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            labels.forEach { label ->
                TagChip(label = label)
            }
        }
    }
}

@Composable
private fun LearningModulesSection(
    isSignedIn: Boolean,
    scenarioPreset: LearningPreset,
    challengePreset: LearningPreset,
    scenarioState: ArtifactUiState<LearningScenario>,
    challengeState: ArtifactUiState<LearningChallenge>,
    onSignInClick: () -> Unit,
    onScenarioPresetSelected: (LearningPreset) -> Unit,
    onChallengePresetSelected: (LearningPreset) -> Unit,
    onGenerateScenario: () -> Unit,
    onRefreshScenario: () -> Unit,
    onGenerateChallenge: () -> Unit,
    onRefreshChallenge: () -> Unit,
    onMarkScenarioComplete: () -> Unit,
    onMarkChallengeComplete: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "AI Learning Modules",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Pick a style, generate a scenario or challenge, then mark it complete to earn points.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!isSignedIn) {
            SignInGate(onSignInClick = onSignInClick)
            return@Column
        }

        ScenarioSection(
            preset = scenarioPreset,
            state = scenarioState,
            onPresetSelected = onScenarioPresetSelected,
            onGenerate = onGenerateScenario,
            onRefresh = onRefreshScenario,
            onMarkComplete = onMarkScenarioComplete
        )

        ChallengeSection(
            preset = challengePreset,
            state = challengeState,
            onPresetSelected = onChallengePresetSelected,
            onGenerate = onGenerateChallenge,
            onRefresh = onRefreshChallenge,
            onMarkComplete = onMarkChallengeComplete
        )
    }
}

@Composable
private fun SignInGate(onSignInClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Sign in to generate scenarios and challenges",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Generated artifacts and the points you earn for completing them are tied to your account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PrimaryActionButton(
                text = "Sign in",
                onClick = onSignInClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScenarioSection(
    preset: LearningPreset,
    state: ArtifactUiState<LearningScenario>,
    onPresetSelected: (LearningPreset) -> Unit,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit,
    onMarkComplete: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Scenario")
        if (state.data == null && !state.isLoading) {
            PresetSelector(
                selected = preset,
                onSelect = onPresetSelected
            )
        }
        when {
            state.isLoading -> LoadingState(message = "Generating scenario...")
            state.errorMessage != null -> ErrorState(message = state.errorMessage)
            state.data == null -> EmptyState(message = "No scenario yet. Pick a style and generate one.")
            else -> GeneratedScenarioCard(
                result = state.data,
                completion = state.completion,
                pointsAwarded = state.pointsAwarded,
                onRefresh = onRefresh,
                onMarkComplete = onMarkComplete
            )
        }
        if (state.data == null && !state.isLoading) {
            PrimaryActionButton(
                text = "Generate Scenario",
                onClick = onGenerate,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ChallengeSection(
    preset: LearningPreset,
    state: ArtifactUiState<LearningChallenge>,
    onPresetSelected: (LearningPreset) -> Unit,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit,
    onMarkComplete: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Challenge")
        if (state.data == null && !state.isLoading) {
            PresetSelector(
                selected = preset,
                onSelect = onPresetSelected
            )
        }
        when {
            state.isLoading -> LoadingState(message = "Generating challenge...")
            state.errorMessage != null -> ErrorState(message = state.errorMessage)
            state.data == null -> EmptyState(message = "No challenge yet. Pick a style and generate one.")
            else -> GeneratedChallengeCard(
                result = state.data,
                completion = state.completion,
                pointsAwarded = state.pointsAwarded,
                onRefresh = onRefresh,
                onMarkComplete = onMarkComplete
            )
        }
        if (state.data == null && !state.isLoading) {
            PrimaryActionButton(
                text = "Generate Challenge",
                onClick = onGenerate,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GeneratedScenarioCard(
    result: GeneratedArtifactResult<LearningScenario>,
    completion: LearningCompletion?,
    pointsAwarded: Int,
    onRefresh: () -> Unit,
    onMarkComplete: () -> Unit
) {
    val artifact = result.artifact
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = artifact.title,
                style = MaterialTheme.typography.titleMedium
            )
            MetadataLine(text = "Difficulty: ${artifact.difficulty}")
            Text(text = artifact.context, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Objective: ${artifact.objective}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Tasks:\n- ${artifact.tasks.joinToString("\n- ")}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Reflect:\n- ${artifact.reflectionQuestions.joinToString("\n- ")}",
                style = MaterialTheme.typography.bodyMedium
            )
            MetadataLine(text = if (result.cached) "Loaded from cache" else "Freshly generated")

            CompletionFooter(
                completion = completion,
                pointsAwarded = pointsAwarded,
                onMarkComplete = onMarkComplete,
                onRefresh = onRefresh
            )
        }
    }
}

@Composable
private fun GeneratedChallengeCard(
    result: GeneratedArtifactResult<LearningChallenge>,
    completion: LearningCompletion?,
    pointsAwarded: Int,
    onRefresh: () -> Unit,
    onMarkComplete: () -> Unit
) {
    val artifact = result.artifact
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = artifact.title,
                style = MaterialTheme.typography.titleMedium
            )
            MetadataLine(text = "Difficulty: ${artifact.difficulty}")
            Text(text = artifact.prompt, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Success criteria:\n- ${artifact.successCriteria.joinToString("\n- ")}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(text = "Hint: ${artifact.hint}", style = MaterialTheme.typography.bodyMedium)
            MetadataLine(text = if (result.cached) "Loaded from cache" else "Freshly generated")

            CompletionFooter(
                completion = completion,
                pointsAwarded = pointsAwarded,
                onMarkComplete = onMarkComplete,
                onRefresh = onRefresh
            )
        }
    }
}

@Composable
private fun CompletionFooter(
    completion: LearningCompletion?,
    pointsAwarded: Int,
    onMarkComplete: () -> Unit,
    onRefresh: () -> Unit
) {
    if (completion != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    val pointsText = if (pointsAwarded > 0) " · +$pointsAwarded points" else ""
                    Text(
                        text = "Completed (${completion.confidence.label} confidence)$pointsText",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                completion.reflectionNotes?.let { notes ->
                    Text(
                        text = "Reflection: $notes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        SecondaryActionButton(
            text = "Regenerate",
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        PrimaryActionButton(
            text = "Mark complete",
            onClick = onMarkComplete,
            modifier = Modifier.fillMaxWidth()
        )
        SecondaryActionButton(
            text = "Regenerate",
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MetadataLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
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
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(title = title)

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
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
            .replace('-', ' ')
            .replace('_', ' ')
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { token ->
                token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
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
