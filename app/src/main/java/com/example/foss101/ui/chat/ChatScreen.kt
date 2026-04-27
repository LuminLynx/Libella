package com.example.foss101.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.EmptyState
import com.example.foss101.ui.components.ErrorState
import com.example.foss101.ui.components.LoadingState
import com.example.foss101.ui.components.PrimaryActionButton
import com.example.foss101.ui.components.SectionHeader
import com.example.foss101.ui.components.TagChip
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.viewmodel.AskGlossaryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(repository: GlossaryRepository) {
    val viewModel: AskGlossaryViewModel = viewModel(factory = AskGlossaryViewModel.factory(repository))
    val uiState = viewModel.uiState

    AppScreenScaffold(
        title = "Ask Glossary",
        subtitle = "Glossary-grounded answers"
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .screenContentPadding(contentPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChanged,
                label = { Text("Your question") },
                placeholder = { Text("e.g. What is RLHF used for?") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.selectedTermId,
                onValueChange = viewModel::onSelectedTermChanged,
                label = { Text("Optional term id focus") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryActionButton(
                text = "Ask Glossary",
                onClick = viewModel::askGlossary,
                modifier = Modifier.fillMaxWidth()
            )

            when {
                uiState.isLoading -> LoadingState(message = "Finding the best glossary answer...")
                uiState.errorMessage != null -> ErrorState(message = uiState.errorMessage)
                uiState.response == null -> EmptyState(message = "Ask a question to get an AI-guided glossary answer.")
                else -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionHeader(title = "Answer")
                        Text(uiState.response.answer, style = MaterialTheme.typography.bodyMedium)
                        SectionHeader(title = "Summary")
                        Text(uiState.response.summary, style = MaterialTheme.typography.bodyMedium)
                        if (uiState.response.relatedTermIds.isNotEmpty()) {
                            SectionHeader(title = "Related Terms")
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                uiState.response.relatedTermIds.forEach { id ->
                                    TagChip(label = id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
