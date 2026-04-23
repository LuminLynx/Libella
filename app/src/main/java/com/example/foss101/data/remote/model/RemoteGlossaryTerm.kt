package com.example.foss101.data.remote.model

import com.example.foss101.model.GlossaryTerm

data class RemoteGlossaryTerm(
    val id: String,
    val slug: String,
    val term: String,
    val definition: String,
    val explanation: String,
    val humor: String? = null,
    val categoryId: String,
    val tags: List<String> = emptyList(),
    val seeAlso: List<String> = emptyList(),
    val controversyLevel: Int = 0,
    val exampleUsage: String? = null,
    val source: String? = null
)

fun RemoteGlossaryTerm.toDomain(): GlossaryTerm {
    return GlossaryTerm(
        id = id,
        slug = slug,
        term = term,
        definition = definition,
        explanation = explanation,
        humor = humor,
        categoryId = categoryId,
        tags = tags,
        seeAlso = seeAlso,
        controversyLevel = controversyLevel,
        exampleUsage = exampleUsage,
        source = source
    )
}