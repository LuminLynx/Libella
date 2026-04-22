package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.GlossaryTerm
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<GlossaryTerm> = emptyList(),
    val isLoading: Boolean = false,
    val hasExactMatch: Boolean = false,
    val errorMessage: String? = null
)

class SearchViewModel(
    private val repository: GlossaryRepository
) : ViewModel() {

    var uiState by mutableStateOf(SearchUiState())
        private set

    private var activeSearchJob: Job? = null

    fun onQueryChanged(query: String) {
        if (query.isBlank()) {
            activeSearchJob?.cancel()
            uiState = SearchUiState(query = query)
            return
        }

        activeSearchJob?.cancel()
        uiState = uiState.copy(
            query = query,
            isLoading = true,
            errorMessage = null
        )

        activeSearchJob = viewModelScope.launch {
            uiState = try {
                val results = repository.searchTerms(query)
                SearchUiState(
                    query = query,
                    results = results,
                    isLoading = false,
                    hasExactMatch = hasExactMatch(query = query, results = results)
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                SearchUiState(
                    query = query,
                    isLoading = false,
                    errorMessage = "Unable to load search results."
                )
            }
        }
    }

    private fun hasExactMatch(query: String, results: List<GlossaryTerm>): Boolean {
        val normalizedQuery = query.trim().lowercase()
        return results.any { result ->
            result.term.trim().lowercase() == normalizedQuery ||
                result.id.trim().lowercase() == normalizedQuery
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
