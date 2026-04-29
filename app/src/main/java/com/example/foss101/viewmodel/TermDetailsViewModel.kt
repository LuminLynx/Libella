package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foss101.data.repository.AuthRepository
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.ArtifactKind
import com.example.foss101.model.CompletionConfidence
import com.example.foss101.model.CriterionGrade
import com.example.foss101.model.GeneratedArtifactResult
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.model.LearningChallenge
import com.example.foss101.model.LearningCompletion
import com.example.foss101.model.LearningPreset
import com.example.foss101.model.LearningScenario
import com.example.foss101.model.TaskState
import kotlinx.coroutines.launch

/**
 * Per-task editing state during the scenario completion workflow.
 */
data class ScenarioEditState(
    val taskChecked: Map<Int, Boolean> = emptyMap(),
    val taskNotes: Map<Int, String> = emptyMap(),
    val reflection: String = "",
    val confidence: CompletionConfidence = CompletionConfidence.Medium
) {
    fun checkedCount(): Int = taskChecked.count { it.value }
    fun isSubmittable(): Boolean = checkedCount() > 0
}

enum class ChallengePhase { Writing, Grading }

/**
 * Per-criterion editing state during the challenge completion workflow.
 */
data class ChallengeEditState(
    val response: String = "",
    val phase: ChallengePhase = ChallengePhase.Writing,
    val criterionMet: Map<Int, Boolean> = emptyMap(),
    val criterionNotes: Map<Int, String> = emptyMap(),
    val confidence: CompletionConfidence = CompletionConfidence.Medium
) {
    fun isResponseReady(): Boolean = response.trim().isNotEmpty()
    fun isSubmittable(totalCriteria: Int): Boolean =
        phase == ChallengePhase.Grading && totalCriteria > 0
}

data class ArtifactUiState<T>(
    val isLoading: Boolean = false,
    val data: GeneratedArtifactResult<T>? = null,
    val errorMessage: String? = null,
    val completion: LearningCompletion? = null,
    val pointsAwarded: Int = 0,
    val isSubmittingCompletion: Boolean = false,
    val completionErrorMessage: String? = null
)

data class TermDetailsUiState(
    val isLoading: Boolean = true,
    val term: GlossaryTerm? = null,
    val errorMessage: String? = null,
    val isSignedIn: Boolean = false,
    val scenarioPreset: LearningPreset = LearningPreset.Default,
    val challengePreset: LearningPreset = LearningPreset.Default,
    val scenarioState: ArtifactUiState<LearningScenario> = ArtifactUiState(),
    val challengeState: ArtifactUiState<LearningChallenge> = ArtifactUiState(),
    val scenarioEdit: ScenarioEditState = ScenarioEditState(),
    val challengeEdit: ChallengeEditState = ChallengeEditState()
)

class TermDetailsViewModel(
    private val termId: String?,
    private val repository: GlossaryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(
        TermDetailsUiState(isSignedIn = authRepository.isLoggedIn())
    )
        private set

    init {
        loadTerm()
    }

    fun refreshAuthState() {
        uiState = uiState.copy(isSignedIn = authRepository.isLoggedIn())
    }

    fun loadTerm() {
        if (termId.isNullOrBlank()) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "Unable to load term details."
            )
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            uiState = try {
                uiState.copy(
                    isLoading = false,
                    term = repository.getTermById(termId),
                    errorMessage = null
                )
            } catch (_: Exception) {
                uiState.copy(
                    isLoading = false,
                    errorMessage = "Unable to load term details."
                )
            }
        }
    }

    fun setScenarioPreset(preset: LearningPreset) {
        uiState = uiState.copy(scenarioPreset = preset)
    }

    fun setChallengePreset(preset: LearningPreset) {
        uiState = uiState.copy(challengePreset = preset)
    }

    fun generateScenario(forceRefresh: Boolean = false) {
        val resolvedTermId = termId ?: return
        if (!authRepository.isLoggedIn()) {
            uiState = uiState.copy(isSignedIn = false)
            return
        }
        val preset = uiState.scenarioPreset
        uiState = uiState.copy(
            scenarioState = uiState.scenarioState.copy(
                isLoading = true,
                errorMessage = null,
                completion = null,
                pointsAwarded = 0,
                completionErrorMessage = null
            ),
            scenarioEdit = ScenarioEditState()
        )

        viewModelScope.launch {
            uiState = try {
                uiState.copy(
                    scenarioState = uiState.scenarioState.copy(
                        isLoading = false,
                        data = repository.generateScenario(resolvedTermId, forceRefresh, preset),
                        errorMessage = null
                    )
                )
            } catch (_: Exception) {
                uiState.copy(
                    scenarioState = uiState.scenarioState.copy(
                        isLoading = false,
                        errorMessage = "Scenario generation is unavailable right now."
                    )
                )
            }
        }
    }

    fun generateChallenge(forceRefresh: Boolean = false) {
        val resolvedTermId = termId ?: return
        if (!authRepository.isLoggedIn()) {
            uiState = uiState.copy(isSignedIn = false)
            return
        }
        val preset = uiState.challengePreset
        uiState = uiState.copy(
            challengeState = uiState.challengeState.copy(
                isLoading = true,
                errorMessage = null,
                completion = null,
                pointsAwarded = 0,
                completionErrorMessage = null
            ),
            challengeEdit = ChallengeEditState()
        )

        viewModelScope.launch {
            uiState = try {
                uiState.copy(
                    challengeState = uiState.challengeState.copy(
                        isLoading = false,
                        data = repository.generateChallenge(resolvedTermId, forceRefresh, preset),
                        errorMessage = null
                    )
                )
            } catch (_: Exception) {
                uiState.copy(
                    challengeState = uiState.challengeState.copy(
                        isLoading = false,
                        errorMessage = "Challenge generation is unavailable right now."
                    )
                )
            }
        }
    }

    // ----- Scenario edit handlers -----

    fun toggleScenarioTask(index: Int, checked: Boolean) {
        val current = uiState.scenarioEdit
        uiState = uiState.copy(
            scenarioEdit = current.copy(
                taskChecked = current.taskChecked + (index to checked)
            )
        )
    }

    fun setScenarioTaskNote(index: Int, note: String) {
        val current = uiState.scenarioEdit
        uiState = uiState.copy(
            scenarioEdit = current.copy(
                taskNotes = current.taskNotes + (index to note)
            )
        )
    }

    fun setScenarioReflection(text: String) {
        uiState = uiState.copy(
            scenarioEdit = uiState.scenarioEdit.copy(reflection = text)
        )
    }

    fun setScenarioConfidence(confidence: CompletionConfidence) {
        uiState = uiState.copy(
            scenarioEdit = uiState.scenarioEdit.copy(confidence = confidence)
        )
    }

    fun submitScenarioCompletion() {
        val resolvedTermId = termId ?: return
        val scenario = uiState.scenarioState.data?.artifact ?: return
        val edit = uiState.scenarioEdit
        if (!edit.isSubmittable()) return

        val taskStates = scenario.tasks.indices.map { index ->
            TaskState(
                index = index,
                checked = edit.taskChecked[index] == true,
                note = edit.taskNotes[index]?.trim()?.ifBlank { null }
            )
        }
        val reflection = edit.reflection.trim().ifBlank { null }
        val confidence = edit.confidence

        uiState = uiState.copy(
            scenarioState = uiState.scenarioState.copy(
                isSubmittingCompletion = true,
                completionErrorMessage = null
            )
        )

        viewModelScope.launch {
            try {
                val result = repository.submitLearningCompletion(
                    termId = resolvedTermId,
                    artifactType = ArtifactKind.Scenario,
                    confidence = confidence,
                    reflectionNotes = reflection,
                    taskStates = taskStates,
                    challengeResponse = null,
                    criteriaGrades = null
                )
                uiState = uiState.copy(
                    scenarioState = uiState.scenarioState.copy(
                        isSubmittingCompletion = false,
                        completion = result.completion,
                        pointsAwarded = result.pointsAwarded,
                        completionErrorMessage = null
                    )
                )
            } catch (_: Exception) {
                uiState = uiState.copy(
                    scenarioState = uiState.scenarioState.copy(
                        isSubmittingCompletion = false,
                        completionErrorMessage = "Could not submit completion. Try again."
                    )
                )
            }
        }
    }

    // ----- Challenge edit handlers -----

    fun setChallengeResponse(text: String) {
        uiState = uiState.copy(
            challengeEdit = uiState.challengeEdit.copy(response = text)
        )
    }

    fun continueToChallengeGrading() {
        val current = uiState.challengeEdit
        if (!current.isResponseReady()) return
        uiState = uiState.copy(
            challengeEdit = current.copy(phase = ChallengePhase.Grading)
        )
    }

    fun returnToChallengeWriting() {
        uiState = uiState.copy(
            challengeEdit = uiState.challengeEdit.copy(phase = ChallengePhase.Writing)
        )
    }

    fun toggleCriterionMet(index: Int, met: Boolean) {
        val current = uiState.challengeEdit
        uiState = uiState.copy(
            challengeEdit = current.copy(
                criterionMet = current.criterionMet + (index to met)
            )
        )
    }

    fun setCriterionNote(index: Int, note: String) {
        val current = uiState.challengeEdit
        uiState = uiState.copy(
            challengeEdit = current.copy(
                criterionNotes = current.criterionNotes + (index to note)
            )
        )
    }

    fun setChallengeConfidence(confidence: CompletionConfidence) {
        uiState = uiState.copy(
            challengeEdit = uiState.challengeEdit.copy(confidence = confidence)
        )
    }

    fun submitChallengeCompletion() {
        val resolvedTermId = termId ?: return
        val challenge = uiState.challengeState.data?.artifact ?: return
        val edit = uiState.challengeEdit
        if (!edit.isSubmittable(challenge.successCriteria.size)) return

        val criteriaGrades = challenge.successCriteria.indices.map { index ->
            CriterionGrade(
                index = index,
                met = edit.criterionMet[index] == true,
                note = edit.criterionNotes[index]?.trim()?.ifBlank { null }
            )
        }
        val response = edit.response.trim()
        val confidence = edit.confidence

        uiState = uiState.copy(
            challengeState = uiState.challengeState.copy(
                isSubmittingCompletion = true,
                completionErrorMessage = null
            )
        )

        viewModelScope.launch {
            try {
                val result = repository.submitLearningCompletion(
                    termId = resolvedTermId,
                    artifactType = ArtifactKind.Challenge,
                    confidence = confidence,
                    reflectionNotes = null,
                    taskStates = null,
                    challengeResponse = response,
                    criteriaGrades = criteriaGrades
                )
                uiState = uiState.copy(
                    challengeState = uiState.challengeState.copy(
                        isSubmittingCompletion = false,
                        completion = result.completion,
                        pointsAwarded = result.pointsAwarded,
                        completionErrorMessage = null
                    )
                )
            } catch (_: Exception) {
                uiState = uiState.copy(
                    challengeState = uiState.challengeState.copy(
                        isSubmittingCompletion = false,
                        completionErrorMessage = "Could not submit completion. Try again."
                    )
                )
            }
        }
    }

    companion object {
        fun factory(
            termId: String?,
            repository: GlossaryRepository,
            authRepository: AuthRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TermDetailsViewModel(
                        termId = termId,
                        repository = repository,
                        authRepository = authRepository
                    ) as T
                }
            }
    }
}
