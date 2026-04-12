package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.Category
import com.example.foss101.model.GlossaryTerm

data class CategoriesUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val filteredTerms: List<GlossaryTerm> = emptyList(),
    val errorMessage: String? = null
)

class CategoriesViewModel(
    private val repository: GlossaryRepository
) : ViewModel() {

    var uiState by mutableStateOf(CategoriesUiState())
        private set

    init {
        loadCategories()
    }

    fun loadCategories() {
        uiState = try {
            CategoriesUiState(
                isLoading = false,
                categories = repository.getAllCategories()
            )
        } catch (error: Exception) {
            CategoriesUiState(
                isLoading = false,
                errorMessage = "Unable to load categories."
            )
        }
    }

    fun selectCategory(categoryId: String) {
        uiState = try {
            uiState.copy(
                selectedCategoryId = categoryId,
                filteredTerms = repository.getTermsByCategory(categoryId),
                errorMessage = null
            )
        } catch (error: Exception) {
            uiState.copy(
                selectedCategoryId = categoryId,
                filteredTerms = emptyList(),
                errorMessage = "Unable to load terms for this category."
            )
        }
    }

    fun clearSelection() {
        uiState = uiState.copy(
            selectedCategoryId = null,
            filteredTerms = emptyList(),
            errorMessage = null
        )
    }

    companion object {
        fun factory(repository: GlossaryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CategoriesViewModel(repository) as T
                }
            }
    }
}
