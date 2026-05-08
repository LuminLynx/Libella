package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foss101.data.remote.api.PathApiException
import com.example.foss101.data.repository.CompletionCache
import com.example.foss101.data.repository.PathRepository
import com.example.foss101.model.GradeResult
import com.example.foss101.model.UnitDetail
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface UnitReaderUiState {
    object Loading : UnitReaderUiState
    data class Error(val message: String, val authExpired: Boolean = false) : UnitReaderUiState
    data class Loaded(
        val unit: UnitDetail,
        val tradeOffExpanded: Boolean = false,
        val depthExpanded: Boolean = false,
        /** Current value of the open-ended decision-prompt answer field (F3). */
        val answerDraft: String = "",
        /** True while a grade submission is in flight. */
        val submitInProgress: Boolean = false,
        /** Non-null when the most recent grade submission failed. */
        val submitFailure: String? = null,
        /** Non-null after a successful grade submission. Drives the F4 grade UI. */
        val gradeResult: GradeResult? = null,
        /**
         * True if this unit is already completed for the current user — either
         * because the user just submitted an answer (which records a completion
         * server-side under T2), or because the local CompletionCache already
         * had the unit id when the screen loaded.
         */
        val isCompleted: Boolean = false
    ) : UnitReaderUiState
}

sealed interface UnitReaderEvent {
    object AuthExpired : UnitReaderEvent
}

class UnitReaderViewModel(
    private val pathRepository: PathRepository,
    private val completionCache: CompletionCache,
    private val unitId: String
) : ViewModel() {

    var uiState: UnitReaderUiState by mutableStateOf(UnitReaderUiState.Loading)
        private set

    private val _events = Channel<UnitReaderEvent>(Channel.BUFFERED)
    val events: Flow<UnitReaderEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        uiState = UnitReaderUiState.Loading
        viewModelScope.launch {
            uiState = try {
                val unit = pathRepository.getUnit(unitId)
                UnitReaderUiState.Loaded(
                    unit = unit,
                    isCompleted = unit.id in completionCache.completedUnitIds()
                )
            } catch (error: PathApiException) {
                if (error.statusCode == 401) {
                    _events.send(UnitReaderEvent.AuthExpired)
                }
                UnitReaderUiState.Error(
                    message = error.message.ifBlank { "Couldn't load this unit." },
                    authExpired = error.statusCode == 401
                )
            } catch (error: Exception) {
                UnitReaderUiState.Error(message = "Network error. Pull to retry.")
            }
        }
    }

    fun toggleTradeOff() {
        val current = uiState
        if (current is UnitReaderUiState.Loaded) {
            uiState = current.copy(tradeOffExpanded = !current.tradeOffExpanded)
        }
    }

    fun toggleDepth() {
        val current = uiState
        if (current is UnitReaderUiState.Loaded) {
            uiState = current.copy(depthExpanded = !current.depthExpanded)
        }
    }

    fun onAnswerChanged(answer: String) {
        val current = uiState
        if (current is UnitReaderUiState.Loaded) {
            uiState = current.copy(answerDraft = answer, submitFailure = null)
        }
    }

    /**
     * F4 — submit the user's open-ended answer to the grader. On success
     * the per-criterion grade payload populates `gradeResult` and
     * `isCompleted` flips to true (grading IS completion under T2).
     * Re-submitting replaces the prior grade output.
     */
    fun submitAnswer() {
        val current = uiState
        if (current !is UnitReaderUiState.Loaded || current.submitInProgress) return
        val answer = current.answerDraft.trim()
        if (answer.isBlank()) return

        uiState = current.copy(submitInProgress = true, submitFailure = null)
        viewModelScope.launch {
            uiState = try {
                val result = pathRepository.submitGrade(current.unit.id, answer)
                current.copy(
                    submitInProgress = false,
                    submitFailure = null,
                    gradeResult = result,
                    isCompleted = true
                )
            } catch (error: PathApiException) {
                if (error.statusCode == 401) {
                    _events.send(UnitReaderEvent.AuthExpired)
                }
                current.copy(
                    submitInProgress = false,
                    submitFailure = when (error.statusCode) {
                        401 -> "Your session expired. Sign in again to submit your answer."
                        409 -> "This unit doesn't have a graded decision prompt yet."
                        502 -> "The grader is temporarily unavailable. Try again in a moment."
                        else -> error.message.ifBlank { "Couldn't grade your answer." }
                    }
                )
            } catch (error: Exception) {
                current.copy(
                    submitInProgress = false,
                    submitFailure = "Network error. Try again."
                )
            }
        }
    }

    companion object {
        fun factory(
            pathRepository: PathRepository,
            completionCache: CompletionCache,
            unitId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UnitReaderViewModel(pathRepository, completionCache, unitId) as T
            }
        }
    }
}
