package com.example.foss101.ui.draft

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.ui.components.ErrorState
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.viewmodel.TermDraftViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermDraftScreen(
    repository: GlossaryRepository,
    initialQuery: String,
    onBack: () -> Unit
) {
    val viewModel: TermDraftViewModel = viewModel(
        factory = TermDraftViewModel.factory(repository = repository, initialTerm = initialQuery)
    )
    val uiState = viewModel.uiState
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Term Draft") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.screenContentPadding(padding),
            state = listState,
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Contribute a missing glossary term. Submissions are saved as drafts and reviewed before publishing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.form.term,
                    onValueChange = viewModel::onTermChanged,
                    label = { Text("Term name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.validationErrors.containsKey("term")
                )
                uiState.validationErrors["term"]?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.form.definition,
                    onValueChange = viewModel::onDefinitionChanged,
                    label = { Text("Definition") },
                    placeholder = { Text("Short, clear glossary definition") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = uiState.validationErrors.containsKey("definition")
                )
                uiState.validationErrors["definition"]?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.form.explanation,
                    onValueChange = viewModel::onExplanationChanged,
                    label = { Text("Explanation") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = uiState.validationErrors.containsKey("explanation"),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                uiState.validationErrors["explanation"]?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.form.categoryId,
                    onValueChange = viewModel::onCategoryChanged,
                    label = { Text("Category") },
                    placeholder = { Text("cat-llm-concepts") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.validationErrors.containsKey("categoryId")
                )
                uiState.validationErrors["categoryId"]?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.form.humor,
                    onValueChange = viewModel::onHumorChanged,
                    label = { Text("Humor (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.form.tagsInput,
                    onValueChange = viewModel::onTagsInputChanged,
                    label = { Text("Tags (optional)") },
                    placeholder = { Text("llm, training, safety") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Separate tags with commas") }
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.form.seeAlsoInput,
                    onValueChange = viewModel::onSeeAlsoInputChanged,
                    label = { Text("See also (optional)") },
                    placeholder = { Text("prompt engineering, tokens") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Separate related terms with commas") }
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.form.controversyLevelInput,
                    onValueChange = viewModel::onControversyLevelChanged,
                    label = { Text("Controversy level (optional)") },
                    placeholder = { Text("0-3") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.validationErrors.containsKey("controversyLevel")
                )
                uiState.validationErrors["controversyLevel"]?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                if (uiState.submitErrorMessage != null) {
                    ErrorState(message = uiState.submitErrorMessage)
                }
                if (uiState.successMessage != null) {
                    SuccessMessage(message = uiState.successMessage)
                }
            }

            item {
                Button(
                    onClick = viewModel::submitDraft,
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 10.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text(if (uiState.isSubmitting) "Submitting draft..." else "Submit Draft")
                }
            }
        }
    }
}

@Composable
private fun SuccessMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
