package com.example.foss101.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.ui.components.SectionHeader
import com.example.foss101.viewmodel.TermDetailsViewModel

@Composable
fun TermDetailsScreen(
    termId: String? = null,
    repository: GlossaryRepository
) {
    val viewModel: TermDetailsViewModel = viewModel(
        key = termId,
        factory = TermDetailsViewModel.factory(termId = termId, repository = repository)
    )
    val uiState = viewModel.uiState

    if (uiState.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Loading term details...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    if (uiState.errorMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = uiState.errorMessage,
                style = MaterialTheme.typography.headlineMedium
            )
        }
        return
    }

    val term = uiState.term
    if (term == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Term not found",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "The requested term could not be located.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = term.term,
            style = MaterialTheme.typography.headlineLarge
        )

        SectionHeader(
            title = "Short Definition",
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = term.shortDefinition,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )

        SectionHeader(
            title = "Full Explanation",
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            text = term.fullExplanation,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (term.exampleUsage != null) {
            SectionHeader(
                title = "Example Usage",
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = term.exampleUsage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
