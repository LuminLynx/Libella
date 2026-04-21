package com.example.foss101.model

data class TermDraftSubmission(
    val slug: String? = null,
    val term: String,
    val definition: String,
    val explanation: String? = null,
    val humor: String? = null,
    val seeAlso: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val controversyLevel: Int = 0
)

data class TermDraftSubmissionResult(
    val id: String,
    val status: String
)
