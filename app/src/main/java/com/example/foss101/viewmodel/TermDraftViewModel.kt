package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.TermDraftSubmission
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class TermDraftFormState(
    val term: String = "",
    val definition: String = "",
    val explanation: String = "",
    val humor: String = "",
    val tagsInput: String = ""
)

data class TermDraftUiState(
    val form: TermDraftFormState = TermDraftFormState(),
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val submitErrorMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap()
)

class TermDraftViewModel(
    private val repository: GlossaryRepository,
    initialTerm: String
) : ViewModel() {

    var uiState by mutableStateOf(
        TermDraftUiState(
            form = TermDraftFormState(term = initialTerm)
        )
    )
        private set

    fun onTermChanged(value: String) {
        updateForm { copy(term = value) }
    }

    fun onDefinitionChanged(value: String) {
        updateForm { copy(definition = value) }
    }

    fun onExplanationChanged(value: String) {
        updateForm { copy(explanation = value) }
    }

    fun onHumorChanged(value: String) {
        updateForm { copy(humor = value) }
    }

    fun onTagsInputChanged(value: String) {
        updateForm { copy(tagsInput = value) }
    }

    fun submitDraft() {
        val validationErrors = validate(uiState.form)
        if (validationErrors.isNotEmpty()) {
            uiState = uiState.copy(validationErrors = validationErrors, submitErrorMessage = null)
            return
        }

        uiState = uiState.copy(
            isSubmitting = true,
            submitErrorMessage = null,
            successMessage = null,
            validationErrors = emptyMap()
        )

        viewModelScope.launch {
            uiState = try {
                val form = uiState.form
                val draft = TermDraftSubmission(
                    slug = form.term.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                        .ifBlank { null },
                    term = form.term.trim(),
                    definition = form.definition.trim(),
                    explanation = form.explanation.trim(),
                    humor = form.humor.trim().ifBlank { null },
                    tags = parseTags(form.tagsInput)
                )
                repository.submitTermDraft(draft)
                uiState.copy(
                    isSubmitting = false,
                    successMessage = "Draft submitted for review. It will not appear in the live glossary until approved."
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                uiState.copy(
                    isSubmitting = false,
                    submitErrorMessage = error.message ?: "Unable to submit draft. Please try again."
                )
            }
        }
    }

    private fun parseTags(tagsInput: String): List<String> {
        return tagsInput
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun validate(form: TermDraftFormState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (form.term.trim().length < 2) {
            errors["term"] = "Term name must be at least 2 characters."
        }
        if (form.definition.trim().length < 10) {
            errors["definition"] = "Definition should be at least 10 characters."
        }
        if (form.explanation.trim().length < 10) {
            errors["explanation"] = "Explanation should be at least 10 characters."
        }

        return errors
    }

    private fun updateForm(update: TermDraftFormState.() -> TermDraftFormState) {
        val updatedForm = uiState.form.update()
        uiState = uiState.copy(
            form = updatedForm,
            successMessage = null,
            submitErrorMessage = null,
            validationErrors = uiState.validationErrors
                .filterKeys { it != "term" && it != "definition" && it != "explanation" }
        )
    }

    companion object {
        fun factory(repository: GlossaryRepository, initialTerm: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TermDraftViewModel(repository, initialTerm) as T
                }
            }
    }
}
