package com.example.foss101.model

data class UnitManifestEntry(
    val id: String,
    val slug: String,
    val title: String,
    val position: Int,
    val status: String
)

data class Path(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val units: List<UnitManifestEntry>
)

data class UnitSource(
    val id: Long,
    val url: String,
    val title: String,
    val date: String,
    val primarySource: Boolean
)

data class CalibrationTag(
    val id: Long,
    val claim: String,
    val tier: String
)

data class DecisionPrompt(
    val id: Long,
    val promptMd: String
)

data class RubricCriterion(
    val id: Long,
    val position: Int,
    val text: String
)

data class Rubric(
    val id: Long,
    val version: Int,
    val criteria: List<RubricCriterion>
)

data class UnitDetail(
    val id: String,
    val pathId: String,
    val slug: String,
    val position: Int,
    val title: String,
    val definition: String,
    val tradeOffFraming: String,
    val biteMd: String,
    val depthMd: String,
    val prereqUnitIds: List<String>,
    val status: String,
    val sources: List<UnitSource>,
    val calibrationTags: List<CalibrationTag>,
    val decisionPrompt: DecisionPrompt?,
    val rubric: Rubric?
)

data class CompletionRecord(
    val id: Long,
    val userId: String,
    val pathId: String,
    val unitId: String,
    val completedAt: String
)
