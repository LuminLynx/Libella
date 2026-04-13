package com.example.foss101.data.remote.model

import com.example.foss101.model.GlossaryTerm

data class RemoteGlossaryTerm(
    val id: String,
    val term: String,
    val shortDefinition: String,
    val fullExplanation: String,
    val categoryId: String,
    val tags: List<String> = emptyList(),
    val relatedTerms: List<String> = emptyList(),
    val exampleUsage: String? = null,
    val source: String? = null,
    val createdAt: String,
    val updatedAt: String
)

fun RemoteGlossaryTerm.toDomain(): GlossaryTerm {
    return GlossaryTerm(
        id = id,
        term = term,
        shortDefinition = shortDefinition,
        fullExplanation = fullExplanation,
        categoryId = categoryId,
        tags = tags,
        relatedTerms = relatedTerms,
        exampleUsage = exampleUsage,
        source = source
    )
}
