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
import com.example.foss101.model.GeneratedArtifactResult
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.model.LearningChallenge
import com.example.foss101.model.LearningCompletion
import com.example.foss101.model.LearningPreset
import com.example.foss101.model.LearningScenario
import kotlinx.coroutines.launch

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
    val activeCompletionSheet: ArtifactKind? = null
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
            )
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
            )
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

    fun openCompletionSheet(kind: ArtifactKind) {
        uiState = uiState.copy(activeCompletionSheet = kind)
    }

    fun dismissCompletionSheet() {
        uiState = uiState.copy(activeCompletionSheet = null)
    }

    fun submitCompletion(
        kind: ArtifactKind,
        confidence: CompletionConfidence,
        reflectionNotes: String?
    ) {
        val resolvedTermId = termId ?: return

        when (kind) {
            ArtifactKind.Scenario -> {
                uiState = uiState.copy(
                    scenarioState = uiState.scenarioState.copy(
                        isSubmittingCompletion = true,
                        completionErrorMessage = null
                    )
                )
            }
            ArtifactKind.Challenge -> {
                uiState = uiState.copy(
                    challengeState = uiState.challengeState.copy(
                        isSubmittingCompletion = true,
                        completionErrorMessage = null
                    )
                )
            }
        }

        viewModelScope.launch {
            try {
                val result = repository.submitLearningCompletion(
                    termId = resolvedTermId,
                    artifactType = kind,
                    confidence = confidence,
                    reflectionNotes = reflectionNotes
                )
                uiState = when (kind) {
                    ArtifactKind.Scenario -> uiState.copy(
                        activeCompletionSheet = null,
                        scenarioState = uiState.scenarioState.copy(
                            isSubmittingCompletion = false,
                            completion = result.completion,
                            pointsAwarded = result.pointsAwarded,
                            completionErrorMessage = null
                        )
                    )
                    ArtifactKind.Challenge -> uiState.copy(
                        activeCompletionSheet = null,
                        challengeState = uiState.challengeState.copy(
                            isSubmittingCompletion = false,
                            completion = result.completion,
                            pointsAwarded = result.pointsAwarded,
                            completionErrorMessage = null
                        )
                    )
                }
            } catch (_: Exception) {
                uiState = when (kind) {
                    ArtifactKind.Scenario -> uiState.copy(
                        scenarioState = uiState.scenarioState.copy(
                            isSubmittingCompletion = false,
                            completionErrorMessage = "Could not submit completion. Try again."
                        )
                    )
                    ArtifactKind.Challenge -> uiState.copy(
                        challengeState = uiState.challengeState.copy(
                            isSubmittingCompletion = false,
                            completionErrorMessage = "Could not submit completion. Try again."
                        )
                    )
                }
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
