package com.example.foss101.data.repository

import com.example.foss101.data.remote.api.PathApiService
import com.example.foss101.model.CompletionRecord
import com.example.foss101.model.Path
import com.example.foss101.model.UnitDetail

interface PathRepository {
    suspend fun getPath(pathId: String): Path
    suspend fun getUnit(unitId: String): UnitDetail
    suspend fun markComplete(unitId: String): CompletionRecord
}

class ApiPathRepository(
    private val pathApiService: PathApiService,
    private val completionCache: CompletionCache
) : PathRepository {

    override suspend fun getPath(pathId: String): Path = pathApiService.getPath(pathId)

    override suspend fun getUnit(unitId: String): UnitDetail = pathApiService.getUnit(unitId)

    override suspend fun markComplete(unitId: String): CompletionRecord {
        val record = pathApiService.postCompletion(unitId)
        completionCache.add(record.unitId)
        return record
    }
}
