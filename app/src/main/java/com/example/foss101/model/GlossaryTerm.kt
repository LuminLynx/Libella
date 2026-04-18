package com.example.foss101.model

data class GlossaryTerm(
    val id: String,
    val slug: String,
    val term: String,
    val definition: String,
    val explanation: String,
    val humor: String? = null,
    val seeAlso: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val controversyLevel: Int = 0,
    val categoryId: String? = null,
    val relatedTermIds: List<String> = emptyList()
) {
    constructor(
        id: String,
        term: String,
        shortDefinition: String,
        fullExplanation: String,
        categoryId: String,
        tags: List<String> = emptyList(),
        relatedTerms: List<String> = emptyList(),
        exampleUsage: String? = null,
        source: String? = null
    ) : this(
        id = id,
        slug = id,
        term = term,
        definition = shortDefinition,
        explanation = fullExplanation,
        humor = source ?: exampleUsage,
        seeAlso = relatedTerms,
        tags = tags,
        controversyLevel = 0,
        categoryId = categoryId,
        relatedTermIds = relatedTerms
    )

    val shortDefinition: String get() = definition
    val fullExplanation: String get() = explanation
    val relatedTerms: List<String> get() = relatedTermIds
}
