package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.GeneratedArtifactResult
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.model.LearningChallenge
import com.example.foss101.model.LearningScenario
import kotlinx.coroutines.launch

data class ArtifactUiState<T>(
    val isLoading: Boolean = false,
    val data: GeneratedArtifactResult<T>? = null,
    val errorMessage: String? = null
)

data class TermDetailsUiState(
    val isLoading: Boolean = true,
    val term: GlossaryTerm? = null,
    val errorMessage: String? = null,
    val scenarioState: ArtifactUiState<LearningScenario> = ArtifactUiState(),
    val challengeState: ArtifactUiState<LearningChallenge> = ArtifactUiState()
)

class TermDetailsViewModel(
    private val termId: String?,
    private val repository: GlossaryRepository
) : ViewModel() {

    var uiState by mutableStateOf(TermDetailsUiState())
        private set

    init {
        loadTerm()
    }

    fun loadTerm() {
        if (termId.isNullOrBlank()) {
            uiState = TermDetailsUiState(
                isLoading = false,
                errorMessage = "Unable to load term details."
            )
            return
        }

        uiState = TermDetailsUiState(isLoading = true)

        viewModelScope.launch {
            uiState = try {
                TermDetailsUiState(
                    isLoading = false,
                    term = repository.getTermById(termId),
                    errorMessage = null
                )
            } catch (_: Exception) {
                TermDetailsUiState(
                    isLoading = false,
                    errorMessage = "Unable to load term details."
                )
            }
        }
    }

    fun generateScenario(forceRefresh: Boolean = false) {
        val resolvedTermId = termId ?: return
        uiState = uiState.copy(
            scenarioState = uiState.scenarioState.copy(isLoading = true, errorMessage = null)
        )

        viewModelScope.launch {
            uiState = try {
                uiState.copy(
                    scenarioState = ArtifactUiState(
                        isLoading = false,
                        data = repository.generateScenario(resolvedTermId, forceRefresh),
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
        uiState = uiState.copy(
            challengeState = uiState.challengeState.copy(isLoading = true, errorMessage = null)
        )

        viewModelScope.launch {
            uiState = try {
                uiState.copy(
                    challengeState = ArtifactUiState(
                        isLoading = false,
                        data = repository.generateChallenge(resolvedTermId, forceRefresh),
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

    companion object {
        fun factory(termId: String?, repository: GlossaryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TermDetailsViewModel(termId = termId, repository = repository) as T
                }
            }
    }
}
