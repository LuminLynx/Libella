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

data class BrowseTermsUiState(
    val isLoading: Boolean = true,
    val terms: List<GlossaryTerm> = emptyList(),
    val errorMessage: String? = null
)

class BrowseTermsViewModel(
    private val repository: GlossaryRepository
) : ViewModel() {

    var uiState by mutableStateOf(BrowseTermsUiState())
        private set

    init {
        loadTerms()
    }

    fun loadTerms() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            uiState = try {
                BrowseTermsUiState(
                    isLoading = false,
                    terms = repository.getAllTerms()
                )
            } catch (error: Exception) {
                BrowseTermsUiState(
                    isLoading = false,
                    errorMessage = "Unable to load terms."
                )
            }
        }
    }

    companion object {
        fun factory(repository: GlossaryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BrowseTermsViewModel(repository) as T
                }
            }
    }
}
