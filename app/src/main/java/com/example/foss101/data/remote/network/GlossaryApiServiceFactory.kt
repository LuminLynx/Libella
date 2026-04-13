package com.example.foss101.data.remote.network

import com.example.foss101.data.remote.api.GlossaryApiService

object GlossaryApiServiceFactory {
    fun create(config: ApiConfig): GlossaryApiService {
        return UnsupportedGlossaryApiService(config)
    }
}

private class UnsupportedGlossaryApiService(
    private val config: ApiConfig
) : GlossaryApiService {

    private fun error(): Nothing {
        throw UnsupportedOperationException(
            "API client not wired yet. Configure a real HTTP client for ${config.baseUrl}."
        )
    }

    override suspend fun getTerms() = error()

    override suspend fun getTermDetails(termId: String) = error()

    override suspend fun getCategories() = error()

    override suspend fun getTermsByCategory(categoryId: String) = error()

    override suspend fun searchTerms(query: String) = error()
}
