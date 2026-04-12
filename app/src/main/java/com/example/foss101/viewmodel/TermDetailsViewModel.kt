package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.GlossaryTerm

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
        uiState = try {
            TermDetailsUiState(
                isLoading = false,
                term = termId?.let(repository::getTermById),
                errorMessage = null
            )
        } catch (error: Exception) {
            TermDetailsUiState(
                isLoading = false,
                errorMessage = "Unable to load term details."
            )
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
