package com.example.foss101.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.foss101.ui.components.EmptyState
import com.example.foss101.ui.components.ErrorState
import com.example.foss101.ui.components.LoadingState
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Find terms by name or keyword.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search terms") },
            placeholder = { Text("Type a term or keyword") }
        )

        if (uiState.query.isBlank()) {
            EmptyState(
                message = "Enter a search query to see results.",
                modifier = Modifier.padding(top = 8.dp)
            )
            return@Column
        }


        if (uiState.isLoading) {
            LoadingState(
                message = "Searching terms...",
                modifier = Modifier.padding(top = 8.dp)
            )
            return@Column
        }

        if (uiState.errorMessage != null) {
            ErrorState(
                message = uiState.errorMessage,
                modifier = Modifier.padding(top = 8.dp)
            )
            return@Column
        }

        if (uiState.results.isEmpty()) {
            EmptyState(
                message = "No search results found.",
                modifier = Modifier.padding(top = 8.dp)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.results) { term ->
                GlossaryTermItem(
                    term = term,
                    onClick = { onNavigate("details/${term.id}") }
                )
            }
        }
    }
}
