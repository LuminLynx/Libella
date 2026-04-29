package com.example.foss101.model

/**
 * Curated input bundles the user picks before generating a Scenario or Challenge.
 * Each preset maps server-side to a (difficulty, tone, focus) prompt bundle.
 * Add presets here in lockstep with backend [PRESETS] in ai_service.py.
 */
enum class LearningPreset(val key: String, val label: String, val tagline: String) {
    QuickRecap(
        key = "quick_recap",
        label = "Quick recap",
        tagline = "5 min · friendly · core idea"
    ),
    InterviewPrep(
        key = "interview_prep",
        label = "Interview prep",
        tagline = "Probing · trade-offs · failure modes"
    ),
    HandsOnCoding(
        key = "hands_on_coding",
        label = "Hands-on coding",
        tagline = "Code snippets · runnable in a notebook"
    ),
    ConceptualDeepDive(
        key = "conceptual_deep_dive",
        label = "Conceptual deep-dive",
        tagline = "Why · when · contrasted with related ideas"
    );

    companion object {
        val Default: LearningPreset = QuickRecap

        fun fromKey(key: String?): LearningPreset =
            values().firstOrNull { it.key == key } ?: Default
    }
}

enum class CompletionConfidence(val key: String, val label: String) {
    Low("low", "Low"),
    Medium("medium", "Medium"),
    High("high", "High");

    companion object {
        fun fromKey(key: String?): CompletionConfidence =
            values().firstOrNull { it.key == key } ?: Medium
    }
}

enum class ArtifactKind(val key: String) {
    Scenario("scenario"),
    Challenge("challenge")
}

data class TaskState(
    val index: Int,
    val checked: Boolean,
    val note: String? = null
)

data class CriterionGrade(
    val index: Int,
    val met: Boolean,
    val note: String? = null
)

data class LearningCompletion(
    val id: Long,
    val userId: String,
    val termId: String,
    val artifactType: ArtifactKind,
    val confidence: CompletionConfidence,
    val reflectionNotes: String?,
    val taskStates: List<TaskState>?,
    val challengeResponse: String?,
    val criteriaGrades: List<CriterionGrade>?,
    val earnedPoints: Int,
    val completedAt: String
)

data class LearningCompletionResult(
    val completion: LearningCompletion,
    val pointsAwarded: Int,
    val alreadyCompleted: Boolean
)
