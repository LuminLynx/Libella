package com.example.foss101.model

data class GlossaryTerm (
    val id: String,
    val term: String,
    val shortDefinition: String,
    val fullExplanation: String,
    val categoryId: String,
    val tags: List<String> = emptyList(),
    val relatedTerms: List<String> = emptyList(),
    val exampleUsage: String? = null,
    val source: String? = null,
    val slug: String? = null,
    val humor: String? = null,
    val seeAlso: List<String> = emptyList(),
    val controversyLevel: Int = 0
) {
    val definition: String
        get() = shortDefinition
}
