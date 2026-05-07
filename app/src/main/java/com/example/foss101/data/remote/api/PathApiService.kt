package com.example.foss101.data.remote.api

import com.example.foss101.model.CompletionRecord
import com.example.foss101.model.Path
import com.example.foss101.model.UnitDetail

interface PathApiService {
    suspend fun getPath(pathId: String): Path
    suspend fun getUnit(unitId: String): UnitDetail
    suspend fun postCompletion(unitId: String): CompletionRecord
    /** Returns every completion for the authenticated user, newest first. */
    suspend fun listCompletions(): List<CompletionRecord>
}

class PathApiException(
    override val message: String,
    val code: String? = null,
    val statusCode: Int? = null
) : RuntimeException(message)
