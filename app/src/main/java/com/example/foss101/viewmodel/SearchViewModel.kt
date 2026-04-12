package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.GlossaryTerm

data class SearchUiState(
    val query: String = "",
    val results: List<GlossaryTerm> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SearchViewModel(
    private val repository: GlossaryRepository
) : ViewModel() {

    var uiState by mutableStateOf(SearchUiState())
        private set

    fun onQueryChanged(query: String) {
        if (query.isBlank()) {
            uiState = SearchUiState(query = query)
            return
        }

        uiState = try {
            SearchUiState(
                query = query,
                results = repository.searchTerms(query),
                isLoading = false
            )
        } catch (error: Exception) {
            SearchUiState(
                query = query,
                isLoading = false,
                errorMessage = "Unable to load search results."
            )
        }
    }

    companion object {
        fun factory(repository: GlossaryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchViewModel(repository) as T
                }
            }
    }
}
