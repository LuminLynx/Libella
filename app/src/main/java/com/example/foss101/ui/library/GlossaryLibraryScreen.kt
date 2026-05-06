package com.example.foss101.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.Category
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.EmptyState
import com.example.foss101.ui.components.ErrorState
import com.example.foss101.ui.components.GlossaryTermCard
import com.example.foss101.ui.components.LoadingState
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.viewmodel.GlossaryLibraryViewModel

@Composable
fun GlossaryLibraryScreen(
    repository: GlossaryRepository,
) {
    val viewModel: GlossaryLibraryViewModel = viewModel(
        factory = GlossaryLibraryViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppScreenScaffold(
        title = "Glossary",
        subtitle = "Search, filter, and browse terms",
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 960.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SearchField(
                    query = uiState.query,
                    onQueryChange = viewModel::onQueryChanged,
                )

                if (uiState.categories.isNotEmpty()) {
                    CategoryChips(
                        categories = uiState.categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onCategorySelected = viewModel::onCategorySelected,
                    )
                }

                ResultsBlock(
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage,
                    terms = uiState.terms,
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
        },
        label = { Text("Search glossary") },
        placeholder = { Text("Transformer, embedding, RLHF...") },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        categories.forEach { category ->
            val selected = category.id == selectedCategoryId
            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}

@Composable
private fun ResultsBlock(
    isLoading: Boolean,
    errorMessage: String?,
    terms: List<com.example.foss101.model.GlossaryTerm>,
) {
    when {
        isLoading -> LoadingState("Loading terms...")
        errorMessage != null -> ErrorState(errorMessage)
        terms.isEmpty() -> EmptyState("No terms match this filter.")
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(terms) { term ->
                    GlossaryTermCard(
                        term = term,
                        onClick = {},
                    )
                }
            }
        }
    }
}
