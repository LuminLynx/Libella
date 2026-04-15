package com.example.foss101.data.repository

import com.example.foss101.data.remote.network.ApiConfig
import com.example.foss101.data.remote.network.GlossaryApiServiceFactory

object RepositoryProvider {

    private enum class RepositoryMode {
        MOCK,
        API
    }

    private val repositoryMode = RepositoryMode.API

    val glossaryRepository: GlossaryRepository by lazy {
        when (repositoryMode) {
            RepositoryMode.MOCK -> MockGlossaryRepository()
            RepositoryMode.API -> {
                val config = ApiConfig.fromBuildConfig()
                ApiGlossaryRepository(
                    glossaryApiService = GlossaryApiServiceFactory.create(config)
                )
            }
        }
    }
}
