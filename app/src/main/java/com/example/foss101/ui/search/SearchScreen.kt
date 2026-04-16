package com.example.foss101.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.ui.browse.GlossaryTermItem
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.EmptyState
import com.example.foss101.ui.components.ErrorState
import com.example.foss101.ui.components.LoadingState
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onNavigate: (String) -> Unit,
    repository: GlossaryRepository
) {
    val viewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.factory(repository)
    )
    val uiState = viewModel.uiState

    AppScreenScaffold(
        title = "Search",
        subtitle = "Find AI terms by keyword"
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.screenContentPadding(contentPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Search glossary") },
                    placeholder = { Text("Transformer, embedding, RLHF...") }
                )
            }

            when {
                uiState.query.isBlank() -> item {
                    EmptyState(message = "Enter a term or keyword to search.")
                }

                uiState.isLoading -> item {
                    LoadingState(message = "Searching terms...")
                }

                uiState.errorMessage != null -> item {
                    ErrorState(message = uiState.errorMessage)
                }

                uiState.results.isEmpty() -> item {
                    EmptyState(message = "No results for \"${uiState.query}\". Try broader terms.")
                }

                else -> {
                    item {
                        Text(
                            text = "${uiState.results.size} results",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    items(uiState.results) { term ->
                        GlossaryTermItem(
                            term = term,
                            onClick = { onNavigate("details/${term.id}") }
                        )
                    }
                }
            }
        }
    }
}
