package com.example.foss101.ui.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.EmptyState
import com.example.foss101.ui.components.ErrorState
import com.example.foss101.ui.components.GlossaryTermCard
import com.example.foss101.ui.components.LoadingState
import com.example.foss101.ui.components.screenContentPadding
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

    AppScreenScaffold(
        title = "Browse Terms",
        subtitle = "All glossary terms in one place"
    ) { contentPadding ->
        when {
            uiState.isLoading -> LoadingState(
                "Loading terms...",
                modifier = Modifier.screenContentPadding(contentPadding)
            )

            uiState.errorMessage != null -> ErrorState(
                uiState.errorMessage,
                modifier = Modifier.screenContentPadding(contentPadding)
            )

            uiState.terms.isEmpty() -> EmptyState(
                "No glossary terms available right now.",
                modifier = Modifier.screenContentPadding(contentPadding)
            )

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .screenContentPadding(contentPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 960.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
}

@Composable
fun GlossaryTermItem(
    term: GlossaryTerm,
    onClick: () -> Unit
) {
    GlossaryTermCard(term = term, onClick = onClick)
}