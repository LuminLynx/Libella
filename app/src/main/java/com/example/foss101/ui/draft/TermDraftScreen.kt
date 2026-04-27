package com.example.foss101.ui.draft

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.example.foss101.ui.components.PrimaryActionButton
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.viewmodel.TermDraftViewModel

private val MultiLineFieldMinHeight = 96.dp
private val MultiLineFieldMaxHeight = 140.dp

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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Contribute a missing glossary term. Submissions are saved as drafts and reviewed before publishing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                FormField(
                    value = uiState.form.term,
                    onValueChange = viewModel::onTermChanged,
                    label = "Term name",
                    error = uiState.validationErrors["term"],
                    singleLine = true
                )
            }

            item {
                FormField(
                    value = uiState.form.definition,
                    onValueChange = viewModel::onDefinitionChanged,
                    label = "Definition",
                    placeholder = "Short, clear glossary definition",
                    error = uiState.validationErrors["definition"],
                    multiLine = true,
                    capitalization = KeyboardCapitalization.Sentences
                )
            }

            item {
                FormField(
                    value = uiState.form.explanation,
                    onValueChange = viewModel::onExplanationChanged,
                    label = "Explanation",
                    error = uiState.validationErrors["explanation"],
                    multiLine = true,
                    capitalization = KeyboardCapitalization.Sentences
                )
            }

            item {
                FormField(
                    value = uiState.form.categoryId,
                    onValueChange = viewModel::onCategoryChanged,
                    label = "Category",
                    placeholder = "cat-llm-concepts",
                    error = uiState.validationErrors["categoryId"],
                    singleLine = true
                )
            }

            item {
                FormField(
                    value = uiState.form.humor,
                    onValueChange = viewModel::onHumorChanged,
                    label = "Humor (optional)",
                    multiLine = true,
                    capitalization = KeyboardCapitalization.Sentences
                )
            }

            item {
                FormField(
                    value = uiState.form.tagsInput,
                    onValueChange = viewModel::onTagsInputChanged,
                    label = "Tags (optional)",
                    placeholder = "llm, training, safety",
                    helperText = "Separate tags with commas",
                    singleLine = true
                )
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
                PrimaryActionButton(
                    text = if (uiState.isSubmitting) "Submitting draft..." else "Submit Draft",
                    onClick = viewModel::submitDraft,
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(18.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    singleLine: Boolean = false,
    multiLine: Boolean = false,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    val fieldModifier = if (multiLine) {
        Modifier
            .fillMaxWidth()
            .heightIn(min = MultiLineFieldMinHeight, max = MultiLineFieldMaxHeight)
    } else {
        Modifier.fillMaxWidth()
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder != null) {
            { Text(placeholder) }
        } else {
            null
        },
        modifier = fieldModifier,
        singleLine = singleLine,
        isError = error != null,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            capitalization = capitalization
        ),
        supportingText = {
            when {
                error != null -> Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                helperText != null -> Text(
                    text = helperText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
private fun SuccessMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
