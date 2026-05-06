package com.example.foss101.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.Category
import com.example.foss101.model.GlossaryTerm
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GlossaryLibraryUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedCategoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val terms: List<GlossaryTerm> = emptyList(),
    val errorMessage: String? = null,
)

class GlossaryLibraryViewModel(
    private val repository: GlossaryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GlossaryLibraryUiState())
    val uiState: StateFlow<GlossaryLibraryUiState> = _uiState.asStateFlow()

    private var activeTermsJob: Job? = null

    init {
        bootstrap()
    }

    private fun bootstrap() {
        viewModelScope.launch {
            val categories = runCatching { repository.getAllCategories() }
                .getOrDefault(emptyList())
            _uiState.update { it.copy(categories = categories) }
        }
        refreshTerms()
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        refreshTerms()
    }

    fun onCategorySelected(categoryId: String?) {
        val current = _uiState.value.selectedCategoryId
        val next = if (current == categoryId) null else categoryId
        _uiState.update { it.copy(selectedCategoryId = next) }
        refreshTerms()
    }

    private fun refreshTerms() {
        activeTermsJob?.cancel()

        val state = _uiState.value
        val query = state.query.trim()
        val categoryId = state.selectedCategoryId

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        activeTermsJob = viewModelScope.launch {
            val result = runCatching {
                when {
                    query.isNotEmpty() -> {
                        val matches = repository.searchTerms(query)
                        if (categoryId != null) matches.filter { it.categoryId == categoryId } else matches
                    }
                    categoryId != null -> repository.getTermsByCategory(categoryId)
                    else -> repository.getAllTerms()
                }
            }

            _uiState.update { current ->
                result.fold(
                    onSuccess = { terms ->
                        current.copy(
                            isLoading = false,
                            terms = terms,
                            errorMessage = null,
                        )
                    },
                    onFailure = { error ->
                        if (error is CancellationException) throw error
                        current.copy(
                            isLoading = false,
                            terms = emptyList(),
                            errorMessage = "Unable to load glossary terms.",
                        )
                    },
                )
            }
        }
    }

    companion object {
        fun factory(repository: GlossaryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GlossaryLibraryViewModel(repository) as T
                }
            }
    }
}
