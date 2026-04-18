package com.example.foss101.data.remote.model

import com.example.foss101.model.GlossaryTerm

data class RemoteGlossaryTerm(
    val id: String,
    val slug: String,
    val term: String,
    val definition: String,
    val explanation: String,
    val humor: String?,
    val seeAlso: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val controversyLevel: Int = 0,
    val categoryId: String? = null,
    val relatedTermIds: List<String> = emptyList()
)

fun RemoteGlossaryTerm.toDomain(): GlossaryTerm {
    return GlossaryTerm(
        id = id,
        slug = slug,
        term = term,
        definition = definition,
        explanation = explanation,
        humor = humor,
        seeAlso = seeAlso,
        tags = tags,
        controversyLevel = controversyLevel,
        categoryId = categoryId,
        relatedTermIds = relatedTermIds
    )
}
