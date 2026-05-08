package com.example.foss101.data.remote.api

import com.example.foss101.model.CompletionRecord
import com.example.foss101.model.GradeResult
import com.example.foss101.model.Path
import com.example.foss101.model.UnitDetail

interface PathApiService {
    suspend fun getPath(pathId: String): Path
    suspend fun getUnit(unitId: String): UnitDetail
    suspend fun postCompletion(unitId: String): CompletionRecord
    /** Returns every completion for the authenticated user, newest first. */
    suspend fun listCompletions(): List<CompletionRecord>
    /**
     * F4 — submit the user's open-ended decision-prompt answer for grading.
     * Server records a completion + grades on success. See
     * backend/app/main.py POST /api/v1/units/{unit_id}/grade.
     */
    suspend fun submitGrade(unitId: String, answer: String): GradeResult
}

class PathApiException(
    override val message: String,
    val code: String? = null,
    val statusCode: Int? = null
) : RuntimeException(message)
