package com.example.foss101.model

data class GlossaryTerm(
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