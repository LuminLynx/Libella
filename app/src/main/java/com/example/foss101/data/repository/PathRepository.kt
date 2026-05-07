package com.example.foss101.data.repository

import com.example.foss101.data.remote.api.PathApiService
import com.example.foss101.model.CompletionRecord
import com.example.foss101.model.Path
import com.example.foss101.model.UnitDetail

interface PathRepository {
    suspend fun getPath(pathId: String): Path
    suspend fun getUnit(unitId: String): UnitDetail
    suspend fun markComplete(unitId: String): CompletionRecord
    /**
     * Pull the authenticated user's completion list from the server and
     * replace the local cache with it. Used to seed the cache after sign-in
     * or on a fresh install so completion state syncs across devices for
     * the same account.
     */
    suspend fun syncCompletedUnits()
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

    override suspend fun syncCompletedUnits() {
        val records = pathApiService.listCompletions()
        completionCache.replaceAll(records.map { it.unitId }.toSet())
    }
}
