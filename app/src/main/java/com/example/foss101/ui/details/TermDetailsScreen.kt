package com.example.foss101.ui.details

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.AuthRepository
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.CompletionConfidence
import com.example.foss101.model.GeneratedArtifactResult
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.model.LearningChallenge
import com.example.foss101.model.LearningCompletion
import com.example.foss101.model.LearningPreset
import com.example.foss101.model.LearningScenario
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.EmptyState
import com.example.foss101.ui.components.ErrorState
import com.example.foss101.ui.components.LoadingState
import com.example.foss101.ui.components.PresetSelector
import com.example.foss101.ui.components.PrimaryActionButton
import com.example.foss101.ui.components.SecondaryActionButton
import com.example.foss101.ui.components.SectionHeader
import com.example.foss101.ui.components.TagChip
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.ui.theme.OnSuccessContainerDark
import com.example.foss101.ui.theme.OnSuccessContainerLight
import com.example.foss101.ui.theme.SuccessContainerDark
import com.example.foss101.ui.theme.SuccessContainerLight
import com.example.foss101.viewmodel.ArtifactUiState
import com.example.foss101.viewmodel.ChallengeEditState
import com.example.foss101.viewmodel.ChallengePhase
import com.example.foss101.viewmodel.ScenarioEditState
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
                    scenarioEdit = uiState.scenarioEdit,
                    challengeEdit = uiState.challengeEdit,
                    onSignInClick = { onNavigate("auth_login") },
                    onScenarioPresetSelected = viewModel::setScenarioPreset,
                    onChallengePresetSelected = viewModel::setChallengePreset,
                    onGenerateScenario = { viewModel.generateScenario() },
                    onRefreshScenario = { viewModel.generateScenario(forceRefresh = true) },
                    onGenerateChallenge = { viewModel.generateChallenge() },
                    onRefreshChallenge = { viewModel.generateChallenge(forceRefresh = true) },
                    onToggleScenarioTask = viewModel::toggleScenarioTask,
                    onScenarioTaskNoteChanged = viewModel::setScenarioTaskNote,
                    onScenarioReflectionChanged = viewModel::setScenarioReflection,
                    onScenarioConfidenceChanged = viewModel::setScenarioConfidence,
                    onSubmitScenario = viewModel::submitScenarioCompletion,
                    onChallengeResponseChanged = viewModel::setChallengeResponse,
                    onContinueToChallengeGrading = viewModel::continueToChallengeGrading,
                    onReturnToChallengeWriting = viewModel::returnToChallengeWriting,
                    onToggleCriterionMet = viewModel::toggleCriterionMet,
                    onCriterionNoteChanged = viewModel::setCriterionNote,
                    onChallengeConfidenceChanged = viewModel::setChallengeConfidence,
                    onSubmitChallenge = viewModel::submitChallengeCompletion
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
    isSignedIn: Boolean,
    scenarioPreset: LearningPreset,
    challengePreset: LearningPreset,
    scenarioState: ArtifactUiState<LearningScenario>,
    challengeState: ArtifactUiState<LearningChallenge>,
    scenarioEdit: ScenarioEditState,
    challengeEdit: ChallengeEditState,
    onSignInClick: () -> Unit,
    onScenarioPresetSelected: (LearningPreset) -> Unit,
    onChallengePresetSelected: (LearningPreset) -> Unit,
    onGenerateScenario: () -> Unit,
    onRefreshScenario: () -> Unit,
    onGenerateChallenge: () -> Unit,
    onRefreshChallenge: () -> Unit,
    onToggleScenarioTask: (Int, Boolean) -> Unit,
    onScenarioTaskNoteChanged: (Int, String) -> Unit,
    onScenarioReflectionChanged: (String) -> Unit,
    onScenarioConfidenceChanged: (CompletionConfidence) -> Unit,
    onSubmitScenario: () -> Unit,
    onChallengeResponseChanged: (String) -> Unit,
    onContinueToChallengeGrading: () -> Unit,
    onReturnToChallengeWriting: () -> Unit,
    onToggleCriterionMet: (Int, Boolean) -> Unit,
    onCriterionNoteChanged: (Int, String) -> Unit,
    onChallengeConfidenceChanged: (CompletionConfidence) -> Unit,
    onSubmitChallenge: () -> Unit
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
                scenarioEdit = scenarioEdit,
                challengeEdit = challengeEdit,
                onSignInClick = onSignInClick,
                onScenarioPresetSelected = onScenarioPresetSelected,
                onChallengePresetSelected = onChallengePresetSelected,
                onGenerateScenario = onGenerateScenario,
                onRefreshScenario = onRefreshScenario,
                onGenerateChallenge = onGenerateChallenge,
                onRefreshChallenge = onRefreshChallenge,
                onToggleScenarioTask = onToggleScenarioTask,
                onScenarioTaskNoteChanged = onScenarioTaskNoteChanged,
                onScenarioReflectionChanged = onScenarioReflectionChanged,
                onScenarioConfidenceChanged = onScenarioConfidenceChanged,
                onSubmitScenario = onSubmitScenario,
                onChallengeResponseChanged = onChallengeResponseChanged,
                onContinueToChallengeGrading = onContinueToChallengeGrading,
                onReturnToChallengeWriting = onReturnToChallengeWriting,
                onToggleCriterionMet = onToggleCriterionMet,
                onCriterionNoteChanged = onCriterionNoteChanged,
                onChallengeConfidenceChanged = onChallengeConfidenceChanged,
                onSubmitChallenge = onSubmitChallenge
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
    scenarioEdit: ScenarioEditState,
    challengeEdit: ChallengeEditState,
    onSignInClick: () -> Unit,
    onScenarioPresetSelected: (LearningPreset) -> Unit,
    onChallengePresetSelected: (LearningPreset) -> Unit,
    onGenerateScenario: () -> Unit,
    onRefreshScenario: () -> Unit,
    onGenerateChallenge: () -> Unit,
    onRefreshChallenge: () -> Unit,
    onToggleScenarioTask: (Int, Boolean) -> Unit,
    onScenarioTaskNoteChanged: (Int, String) -> Unit,
    onScenarioReflectionChanged: (String) -> Unit,
    onScenarioConfidenceChanged: (CompletionConfidence) -> Unit,
    onSubmitScenario: () -> Unit,
    onChallengeResponseChanged: (String) -> Unit,
    onContinueToChallengeGrading: () -> Unit,
    onReturnToChallengeWriting: () -> Unit,
    onToggleCriterionMet: (Int, Boolean) -> Unit,
    onCriterionNoteChanged: (Int, String) -> Unit,
    onChallengeConfidenceChanged: (CompletionConfidence) -> Unit,
    onSubmitChallenge: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "AI Learning Modules",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Pick a style, generate a scenario or challenge, then work through it to earn points.",
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
            edit = scenarioEdit,
            onPresetSelected = onScenarioPresetSelected,
            onGenerate = onGenerateScenario,
            onRefresh = onRefreshScenario,
            onToggleTask = onToggleScenarioTask,
            onTaskNoteChanged = onScenarioTaskNoteChanged,
            onReflectionChanged = onScenarioReflectionChanged,
            onConfidenceChanged = onScenarioConfidenceChanged,
            onSubmit = onSubmitScenario
        )

        ChallengeSection(
            preset = challengePreset,
            state = challengeState,
            edit = challengeEdit,
            onPresetSelected = onChallengePresetSelected,
            onGenerate = onGenerateChallenge,
            onRefresh = onRefreshChallenge,
            onResponseChanged = onChallengeResponseChanged,
            onContinueToGrading = onContinueToChallengeGrading,
            onReturnToWriting = onReturnToChallengeWriting,
            onToggleCriterion = onToggleCriterionMet,
            onCriterionNoteChanged = onCriterionNoteChanged,
            onConfidenceChanged = onChallengeConfidenceChanged,
            onSubmit = onSubmitChallenge
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
    edit: ScenarioEditState,
    onPresetSelected: (LearningPreset) -> Unit,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit,
    onToggleTask: (Int, Boolean) -> Unit,
    onTaskNoteChanged: (Int, String) -> Unit,
    onReflectionChanged: (String) -> Unit,
    onConfidenceChanged: (CompletionConfidence) -> Unit,
    onSubmit: () -> Unit
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
            else -> ScenarioCard(
                result = state.data,
                completion = state.completion,
                pointsAwarded = state.pointsAwarded,
                isSubmitting = state.isSubmittingCompletion,
                completionErrorMessage = state.completionErrorMessage,
                edit = edit,
                onRefresh = onRefresh,
                onToggleTask = onToggleTask,
                onTaskNoteChanged = onTaskNoteChanged,
                onReflectionChanged = onReflectionChanged,
                onConfidenceChanged = onConfidenceChanged,
                onSubmit = onSubmit
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
    edit: ChallengeEditState,
    onPresetSelected: (LearningPreset) -> Unit,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit,
    onResponseChanged: (String) -> Unit,
    onContinueToGrading: () -> Unit,
    onReturnToWriting: () -> Unit,
    onToggleCriterion: (Int, Boolean) -> Unit,
    onCriterionNoteChanged: (Int, String) -> Unit,
    onConfidenceChanged: (CompletionConfidence) -> Unit,
    onSubmit: () -> Unit
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
            else -> ChallengeCard(
                result = state.data,
                completion = state.completion,
                pointsAwarded = state.pointsAwarded,
                isSubmitting = state.isSubmittingCompletion,
                completionErrorMessage = state.completionErrorMessage,
                edit = edit,
                onRefresh = onRefresh,
                onResponseChanged = onResponseChanged,
                onContinueToGrading = onContinueToGrading,
                onReturnToWriting = onReturnToWriting,
                onToggleCriterion = onToggleCriterion,
                onCriterionNoteChanged = onCriterionNoteChanged,
                onConfidenceChanged = onConfidenceChanged,
                onSubmit = onSubmit
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
private fun ScenarioCard(
    result: GeneratedArtifactResult<LearningScenario>,
    completion: LearningCompletion?,
    pointsAwarded: Int,
    isSubmitting: Boolean,
    completionErrorMessage: String?,
    edit: ScenarioEditState,
    onRefresh: () -> Unit,
    onToggleTask: (Int, Boolean) -> Unit,
    onTaskNoteChanged: (Int, String) -> Unit,
    onReflectionChanged: (String) -> Unit,
    onConfidenceChanged: (CompletionConfidence) -> Unit,
    onSubmit: () -> Unit
) {
    val artifact = result.artifact
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = artifact.title, style = MaterialTheme.typography.titleMedium)
            MetadataLine(text = "Difficulty: ${artifact.difficulty}")
            Text(text = artifact.context, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Objective: ${artifact.objective}", style = MaterialTheme.typography.bodyMedium)

            SectionHeader(title = "Tasks")
            if (completion == null) {
                artifact.tasks.forEachIndexed { index, taskText ->
                    EditableTaskRow(
                        index = index,
                        text = taskText,
                        checked = edit.taskChecked[index] == true,
                        note = edit.taskNotes[index].orEmpty(),
                        onToggle = { onToggleTask(index, it) },
                        onNoteChanged = { onTaskNoteChanged(index, it) }
                    )
                }
            } else {
                val checkedSet = completion.taskStates.orEmpty().filter { it.checked }.map { it.index }.toSet()
                val notesByIndex = completion.taskStates.orEmpty().associateBy({ it.index }, { it.note })
                artifact.tasks.forEachIndexed { index, taskText ->
                    ReadOnlyTaskRow(
                        text = taskText,
                        checked = index in checkedSet,
                        note = notesByIndex[index]
                    )
                }
            }

            if (artifact.reflectionQuestions.isNotEmpty()) {
                SectionHeader(title = "Reflect")
                artifact.reflectionQuestions.forEach { question ->
                    Text(
                        text = "• $question",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (completion == null) {
                    OutlinedTextField(
                        value = edit.reflection,
                        onValueChange = onReflectionChanged,
                        label = { Text("Your reflection (optional, private)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 96.dp, max = 160.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                } else {
                    completion.reflectionNotes?.let { notes ->
                        Text(
                            text = "Your reflection: $notes",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            MetadataLine(text = if (result.cached) "Loaded from cache" else "Freshly generated")

            if (completion == null) {
                ConfidenceSelector(
                    selected = edit.confidence,
                    onSelect = onConfidenceChanged
                )
                if (completionErrorMessage != null) {
                    Text(
                        text = completionErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                PrimaryActionButton(
                    text = if (isSubmitting) "Submitting..." else "Submit completion",
                    onClick = onSubmit,
                    enabled = !isSubmitting && edit.isSubmittable(),
                    modifier = Modifier.fillMaxWidth()
                )
                SecondaryActionButton(
                    text = "Regenerate",
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                CompletedFooter(completion = completion, pointsAwarded = pointsAwarded)
                SecondaryActionButton(
                    text = "Regenerate",
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    result: GeneratedArtifactResult<LearningChallenge>,
    completion: LearningCompletion?,
    pointsAwarded: Int,
    isSubmitting: Boolean,
    completionErrorMessage: String?,
    edit: ChallengeEditState,
    onRefresh: () -> Unit,
    onResponseChanged: (String) -> Unit,
    onContinueToGrading: () -> Unit,
    onReturnToWriting: () -> Unit,
    onToggleCriterion: (Int, Boolean) -> Unit,
    onCriterionNoteChanged: (Int, String) -> Unit,
    onConfidenceChanged: (CompletionConfidence) -> Unit,
    onSubmit: () -> Unit
) {
    val artifact = result.artifact
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = artifact.title, style = MaterialTheme.typography.titleMedium)
            MetadataLine(text = "Difficulty: ${artifact.difficulty}")
            Text(text = artifact.prompt, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Hint: ${artifact.hint}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MetadataLine(text = if (result.cached) "Loaded from cache" else "Freshly generated")

            if (completion != null) {
                SectionHeader(title = "Your response")
                Text(
                    text = completion.challengeResponse.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium
                )
                SectionHeader(title = "Self-grade")
                val gradesByIndex = completion.criteriaGrades.orEmpty().associateBy { it.index }
                artifact.successCriteria.forEachIndexed { index, criterionText ->
                    val grade = gradesByIndex[index]
                    ReadOnlyCriterionRow(
                        text = criterionText,
                        met = grade?.met == true,
                        note = grade?.note
                    )
                }
                CompletedFooter(completion = completion, pointsAwarded = pointsAwarded)
                SecondaryActionButton(
                    text = "Regenerate",
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                when (edit.phase) {
                    ChallengePhase.Writing -> {
                        SectionHeader(title = "Your response")
                        OutlinedTextField(
                            value = edit.response,
                            onValueChange = onResponseChanged,
                            label = { Text("Write your answer") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 240.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                        PrimaryActionButton(
                            text = "Continue to self-grade",
                            onClick = onContinueToGrading,
                            enabled = edit.isResponseReady(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        SecondaryActionButton(
                            text = "Regenerate",
                            onClick = onRefresh,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ChallengePhase.Grading -> {
                        SectionHeader(title = "Self-grade against each criterion")
                        Text(
                            text = "For each criterion, mark whether your response satisfied it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        artifact.successCriteria.forEachIndexed { index, criterionText ->
                            EditableCriterionRow(
                                index = index,
                                text = criterionText,
                                met = edit.criterionMet[index] == true,
                                note = edit.criterionNotes[index].orEmpty(),
                                onToggle = { onToggleCriterion(index, it) },
                                onNoteChanged = { onCriterionNoteChanged(index, it) }
                            )
                        }
                        ConfidenceSelector(
                            selected = edit.confidence,
                            onSelect = onConfidenceChanged
                        )
                        if (completionErrorMessage != null) {
                            Text(
                                text = completionErrorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        PrimaryActionButton(
                            text = if (isSubmitting) "Submitting..." else "Submit completion",
                            onClick = onSubmit,
                            enabled = !isSubmitting && edit.isSubmittable(artifact.successCriteria.size),
                            modifier = Modifier.fillMaxWidth()
                        )
                        SecondaryActionButton(
                            text = "Back to response",
                            onClick = onReturnToWriting,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedFooter(
    completion: LearningCompletion,
    pointsAwarded: Int
) {
    val displayedPoints = if (pointsAwarded > 0) pointsAwarded else completion.earnedPoints
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) SuccessContainerDark else SuccessContainerLight
    val onContainerColor = if (isDark) OnSuccessContainerDark else OnSuccessContainerLight

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = onContainerColor
                )
                Text(
                    text = "Completed (${completion.confidence.label} confidence) · +$displayedPoints points",
                    style = MaterialTheme.typography.titleSmall,
                    color = onContainerColor
                )
            }
        }
    }
}

@Composable
private fun EditableTaskRow(
    index: Int,
    text: String,
    checked: Boolean,
    note: String,
    onToggle: (Boolean) -> Unit,
    onNoteChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(checked = checked, onCheckedChange = onToggle)
            TaskBody(
                text = text,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
            )
        }
        if (checked) {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChanged,
                label = { Text("Note (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
        }
    }
}

@Composable
private fun ReadOnlyTaskRow(
    text: String,
    checked: Boolean,
    note: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (checked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (checked) "Done" else "Not done",
                tint = if (checked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
            )
            TaskBody(
                text = text,
                color = if (checked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        if (!note.isNullOrBlank()) {
            Text(
                text = "Note: $note",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 30.dp)
            )
        }
    }
}

@Composable
private fun EditableCriterionRow(
    index: Int,
    text: String,
    met: Boolean,
    note: String,
    onToggle: (Boolean) -> Unit,
    onNoteChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = met,
                onClick = { onToggle(true) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Met") }
            SegmentedButton(
                selected = !met,
                onClick = { onToggle(false) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Not met") }
        }
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChanged,
            label = { Text("How did your answer satisfy this? (optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
    }
}

@Composable
private fun ReadOnlyCriterionRow(
    text: String,
    met: Boolean,
    note: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (met) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = if (met) "Met" else "Not met",
                tint = if (met) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
        if (!note.isNullOrBlank()) {
            Text(
                text = "Note: $note",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 30.dp)
            )
        }
    }
}

@Composable
private fun ConfidenceSelector(
    selected: CompletionConfidence,
    onSelect: (CompletionConfidence) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "How confident did you feel?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            CompletionConfidence.values().forEachIndexed { index, level ->
                SegmentedButton(
                    selected = level == selected,
                    onClick = { onSelect(level) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = CompletionConfidence.values().size
                    )
                ) {
                    Text(level.label)
                }
            }
        }
    }
}

/**
 * Renders a task line. If the task content looks like a code block (multi-line or starts
 * with a recognisable code keyword) we switch to a monospace font and a subtle
 * surfaceVariant background so the snippet stays readable. Plain prose tasks render
 * unchanged.
 */
@Composable
private fun TaskBody(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    if (looksLikeCode(text)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = modifier
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = color,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    } else {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            modifier = modifier
        )
    }
}

private fun looksLikeCode(text: String): Boolean {
    if ('\n' in text) return true
    val trimmed = text.trimStart()
    val codePrefixes = listOf(
        "import ", "from ", "def ", "class ", ">>> ", "$ ", "# ",
        "for ", "while ", "if __name__", "return "
    )
    return codePrefixes.any { trimmed.startsWith(it) }
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
