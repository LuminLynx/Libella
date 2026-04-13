package com.example.foss101.data.repository

import com.example.foss101.data.remote.network.ApiConfig
import com.example.foss101.data.remote.network.GlossaryApiServiceFactory

object RepositoryProvider {

    private enum class RepositoryMode {
        MOCK,
        API
    }

    private val repositoryMode = RepositoryMode.MOCK

    val glossaryRepository: GlossaryRepository by lazy {
        when (repositoryMode) {
            RepositoryMode.MOCK -> MockGlossaryRepository()
            RepositoryMode.API -> {
                val config = ApiConfig(baseUrl = ApiConfig.DEFAULT_BASE_URL)
                ApiGlossaryRepository(
                    glossaryApiService = GlossaryApiServiceFactory.create(config)
                )
            }
        }
    }
}
