package com.example.foss101.data.remote.model

import com.example.foss101.model.TermDraftSubmission
import com.example.foss101.model.TermDraftSubmissionResult

data class RemoteTermDraftSubmission(
    val slug: String? = null,
    val term: String,
    val definition: String,
    val explanation: String,
    val humor: String = "",
    val seeAlso: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val controversyLevel: Int? = null,
    val categoryId: String
)

data class RemoteTermDraftSubmissionResult(
    val id: String,
    val status: String
)

fun TermDraftSubmission.toRemote(): RemoteTermDraftSubmission = RemoteTermDraftSubmission(
    slug = slug,
    term = term,
    definition = definition,
    explanation = explanation,
    humor = humor ?: "",
    seeAlso = seeAlso,
    tags = tags,
    controversyLevel = controversyLevel,
    categoryId = categoryId
)

fun RemoteTermDraftSubmissionResult.toDomain(): TermDraftSubmissionResult = TermDraftSubmissionResult(
    id = id,
    status = status
)
