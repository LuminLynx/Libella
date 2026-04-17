package com.example.foss101.model

data class AskGlossaryResponse(
    val answer: String,
    val summary: String,
    val relatedTermIds: List<String>
)

data class LearningScenario(
    val title: String,
    val difficulty: String,
    val context: String,
    val objective: String,
    val tasks: List<String>,
    val reflectionQuestions: List<String>
)

data class LearningChallenge(
    val title: String,
    val difficulty: String,
    val prompt: String,
    val successCriteria: List<String>,
    val hint: String
)

data class GeneratedArtifactResult<T>(
    val artifact: T,
    val cached: Boolean
)
