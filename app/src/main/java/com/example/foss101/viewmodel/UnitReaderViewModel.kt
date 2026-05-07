package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foss101.data.remote.api.PathApiException
import com.example.foss101.data.repository.PathRepository
import com.example.foss101.model.UnitDetail
import kotlinx.coroutines.launch

sealed interface UnitReaderUiState {
    object Loading : UnitReaderUiState
    data class Error(val message: String, val authExpired: Boolean = false) : UnitReaderUiState
    data class Loaded(
        val unit: UnitDetail,
        val depthExpanded: Boolean = false,
        val markCompleteInProgress: Boolean = false,
        val markCompleteFailure: String? = null,
        val completedAt: String? = null
    ) : UnitReaderUiState
}

class UnitReaderViewModel(
    private val pathRepository: PathRepository,
    private val unitId: String
) : ViewModel() {

    var uiState: UnitReaderUiState by mutableStateOf(UnitReaderUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = UnitReaderUiState.Loading
        viewModelScope.launch {
            uiState = try {
                val unit = pathRepository.getUnit(unitId)
                UnitReaderUiState.Loaded(unit = unit)
            } catch (error: PathApiException) {
                UnitReaderUiState.Error(
                    message = error.message.ifBlank { "Couldn't load this unit." },
                    authExpired = error.statusCode == 401
                )
            } catch (error: Exception) {
                UnitReaderUiState.Error(message = "Network error. Pull to retry.")
            }
        }
    }

    fun toggleDepth() {
        val current = uiState
        if (current is UnitReaderUiState.Loaded) {
            uiState = current.copy(depthExpanded = !current.depthExpanded)
        }
    }

    fun markComplete() {
        val current = uiState
        if (current !is UnitReaderUiState.Loaded || current.markCompleteInProgress) return

        uiState = current.copy(markCompleteInProgress = true, markCompleteFailure = null)
        viewModelScope.launch {
            uiState = try {
                val record = pathRepository.markComplete(current.unit.id)
                current.copy(
                    markCompleteInProgress = false,
                    completedAt = record.completedAt
                )
            } catch (error: PathApiException) {
                current.copy(
                    markCompleteInProgress = false,
                    markCompleteFailure = if (error.statusCode == 401) {
                        "Your session expired. Sign in again to mark this complete."
                    } else {
                        error.message.ifBlank { "Couldn't save your completion." }
                    }
                )
            } catch (error: Exception) {
                current.copy(
                    markCompleteInProgress = false,
                    markCompleteFailure = "Network error. Try again."
                )
            }
        }
    }

    companion object {
        fun factory(
            pathRepository: PathRepository,
            unitId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UnitReaderViewModel(pathRepository, unitId) as T
            }
        }
    }
}
