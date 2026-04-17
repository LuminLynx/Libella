package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.AskGlossaryResponse
import kotlinx.coroutines.launch

data class AskGlossaryUiState(
    val query: String = "",
    val selectedTermId: String = "",
    val isLoading: Boolean = false,
    val response: AskGlossaryResponse? = null,
    val errorMessage: String? = null
)

class AskGlossaryViewModel(
    private val repository: GlossaryRepository
) : ViewModel() {

    var uiState by mutableStateOf(AskGlossaryUiState())
        private set

    fun onQueryChanged(value: String) {
        uiState = uiState.copy(query = value)
    }

    fun onSelectedTermChanged(value: String) {
        uiState = uiState.copy(selectedTermId = value)
    }

    fun askGlossary() {
        val question = uiState.query.trim()
        if (question.isBlank()) {
            uiState = uiState.copy(
                response = null,
                errorMessage = "Enter a question to ask the glossary."
            )
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                uiState.copy(
                    isLoading = false,
                    response = repository.askGlossary(
                        question = question,
                        termId = uiState.selectedTermId.trim().takeIf { it.isNotBlank() }
                    ),
                    errorMessage = null
                )
            } catch (_: Exception) {
                uiState.copy(
                    isLoading = false,
                    response = null,
                    errorMessage = "Ask Glossary is unavailable right now."
                )
            }
        }
    }

    companion object {
        fun factory(repository: GlossaryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AskGlossaryViewModel(repository) as T
                }
            }
    }
}
