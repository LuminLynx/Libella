package com.example.foss101.ui.browse

import androidx.compose.foundation.clickable
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
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.ui.components.EmptyState
import com.example.foss101.ui.components.ErrorState
import com.example.foss101.ui.components.LoadingState
import com.example.foss101.viewmodel.BrowseTermsViewModel

@Composable
fun BrowseTermsScreen(
    onNavigate: (String) -> Unit,
    repository: GlossaryRepository
) {
    val viewModel: BrowseTermsViewModel = viewModel(
        factory = BrowseTermsViewModel.factory(repository)
    )
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Browse Terms",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "All glossary terms in one list.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        when {
            uiState.isLoading -> LoadingState("Loading terms...")

            uiState.errorMessage != null -> ErrorState(uiState.errorMessage)

            uiState.terms.isEmpty() -> EmptyState("No glossary terms available.")

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(uiState.terms) { term ->
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

@Composable
fun GlossaryTermItem(
    term: GlossaryTerm,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = term.term,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = term.shortDefinition,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
