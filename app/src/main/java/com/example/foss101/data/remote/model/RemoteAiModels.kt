package com.example.foss101.data.remote.model

import com.example.foss101.model.ArtifactKind
import com.example.foss101.model.AskGlossaryResponse
import com.example.foss101.model.CompletionConfidence
import com.example.foss101.model.CriterionGrade
import com.example.foss101.model.GeneratedArtifactResult
import com.example.foss101.model.LearningChallenge
import com.example.foss101.model.LearningCompletion
import com.example.foss101.model.LearningCompletionResult
import com.example.foss101.model.LearningScenario
import com.example.foss101.model.TaskState

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

data class RemoteTaskState(
    val index: Int,
    val checked: Boolean,
    val note: String?
)

data class RemoteCriterionGrade(
    val index: Int,
    val met: Boolean,
    val note: String?
)

data class RemoteLearningCompletion(
    val id: Long,
    val userId: String,
    val termId: String,
    val artifactType: String,
    val confidence: String,
    val reflectionNotes: String?,
    val taskStates: List<RemoteTaskState>?,
    val challengeResponse: String?,
    val criteriaGrades: List<RemoteCriterionGrade>?,
    val earnedPoints: Int,
    val completedAt: String
)

data class RemoteLearningCompletionResult(
    val completion: RemoteLearningCompletion,
    val pointsAwarded: Int,
    val alreadyCompleted: Boolean
)

fun RemoteTaskState.toDomain(): TaskState = TaskState(
    index = index,
    checked = checked,
    note = note?.takeIf { it.isNotBlank() }
)

fun RemoteCriterionGrade.toDomain(): CriterionGrade = CriterionGrade(
    index = index,
    met = met,
    note = note?.takeIf { it.isNotBlank() }
)

fun RemoteLearningCompletion.toDomain(): LearningCompletion = LearningCompletion(
    id = id,
    userId = userId,
    termId = termId,
    artifactType = if (artifactType == "challenge") ArtifactKind.Challenge else ArtifactKind.Scenario,
    confidence = CompletionConfidence.fromKey(confidence),
    reflectionNotes = reflectionNotes?.takeIf { it.isNotBlank() },
    taskStates = taskStates?.map { it.toDomain() },
    challengeResponse = challengeResponse?.takeIf { it.isNotBlank() },
    criteriaGrades = criteriaGrades?.map { it.toDomain() },
    earnedPoints = earnedPoints,
    completedAt = completedAt
)

fun RemoteLearningCompletionResult.toDomain(): LearningCompletionResult = LearningCompletionResult(
    completion = completion.toDomain(),
    pointsAwarded = pointsAwarded,
    alreadyCompleted = alreadyCompleted
)
