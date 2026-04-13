package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.GlossaryTerm
import kotlinx.coroutines.launch

data class TermDetailsUiState(
    val isLoading: Boolean = true,
    val term: GlossaryTerm? = null,
    val errorMessage: String? = null
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
            } catch (error: Exception) {
                TermDetailsUiState(
                    isLoading = false,
                    errorMessage = "Unable to load term details."
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
