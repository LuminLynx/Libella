package com.example.foss101.data.remote.model

import com.example.foss101.model.AskGlossaryResponse
import com.example.foss101.model.GeneratedArtifactResult
import com.example.foss101.model.LearningChallenge
import com.example.foss101.model.LearningScenario

data class RemoteAskGlossaryResponse(
    val answer: String,
    val summary: String,
    val relatedTermIds: List<String>
)

data class RemoteLearningScenario(
    val title: String,
    val difficulty: String,
    val context: String,
    val objective: String,
    val tasks: List<String>,
    val reflectionQuestions: List<String>
)

data class RemoteLearningChallenge(
    val title: String,
    val difficulty: String,
    val prompt: String,
    val successCriteria: List<String>,
    val hint: String
)

data class RemoteGeneratedArtifactResult<T>(
    val artifact: T,
    val cached: Boolean
)

fun RemoteAskGlossaryResponse.toDomain(): AskGlossaryResponse = AskGlossaryResponse(
    answer = answer,
    summary = summary,
    relatedTermIds = relatedTermIds
)

fun RemoteLearningScenario.toDomain(): LearningScenario = LearningScenario(
    title = title,
    difficulty = difficulty,
    context = context,
    objective = objective,
    tasks = tasks,
    reflectionQuestions = reflectionQuestions
)

fun RemoteLearningChallenge.toDomain(): LearningChallenge = LearningChallenge(
    title = title,
    difficulty = difficulty,
    prompt = prompt,
    successCriteria = successCriteria,
    hint = hint
)

fun <T, R> RemoteGeneratedArtifactResult<T>.toDomain(mapper: (T) -> R): GeneratedArtifactResult<R> {
    return GeneratedArtifactResult(
        artifact = mapper(artifact),
        cached = cached
    )
}
