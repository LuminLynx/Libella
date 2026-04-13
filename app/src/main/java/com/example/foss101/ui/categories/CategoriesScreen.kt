package com.example.foss101.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.Category
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.ui.browse.GlossaryTermItem
import com.example.foss101.ui.components.EmptyState
import com.example.foss101.ui.components.ErrorState
import com.example.foss101.ui.components.LoadingState
import com.example.foss101.viewmodel.CategoriesViewModel

@Composable
fun CategoriesScreen(
    onNavigate: (String) -> Unit,
    repository: GlossaryRepository
) {
    val viewModel: CategoriesViewModel = viewModel(
        factory = CategoriesViewModel.factory(repository)
    )
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Explore AI terms by category.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        when {
            uiState.isLoading -> LoadingState("Loading categories...")
            uiState.categoriesLoadError != null && uiState.selectedCategoryId == null -> ErrorState(uiState.categoriesLoadError)
            uiState.categories.isEmpty() -> EmptyState("No categories available.")
            uiState.selectedCategoryId == null -> CategoriesList(
                categories = uiState.categories,
                onCategorySelected = viewModel::selectCategory
            )
            else -> SelectedCategoryTerms(
                category = uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId },
                termsEmpty = uiState.filteredTerms.isEmpty(),
                isLoadingTerms = uiState.isSelectedCategoryLoading,
                termsError = uiState.selectedCategoryTermsError,
                onBackToCategories = viewModel::clearSelection,
                onNavigate = onNavigate,
                terms = uiState.filteredTerms
            )
        }
    }
}

@Composable
private fun CategoriesList(
    categories: List<Category>,
    onCategorySelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryItem(
                category = category,
                onClick = { onCategorySelected(category.id) }
            )
        }
    }
}

@Composable
private fun SelectedCategoryTerms(
    category: Category?,
    terms: List<GlossaryTerm>,
    termsEmpty: Boolean,
    isLoadingTerms: Boolean,
    termsError: String?,
    onBackToCategories: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Text(
        text = category?.name ?: "Category",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 4.dp)
    )
    Text(
        text = category?.description.orEmpty(),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
    )
    Text(
        text = "Back to categories",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(bottom = 12.dp)
            .clickable { onBackToCategories() }
    )

    if (isLoadingTerms) {
        LoadingState("Loading terms...")
    } else if (termsError != null) {
        ErrorState(termsError)
    } else if (category != null && termsEmpty) {
        EmptyState("No terms found for this category.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(terms) { term ->
                GlossaryTermItem(
                    term = term,
                    onClick = { onNavigate("details/${term.id}") }
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
