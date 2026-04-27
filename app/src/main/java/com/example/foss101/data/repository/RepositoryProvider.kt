package com.example.foss101.data.repository

import android.content.Context
import com.example.foss101.data.auth.AuthApiServiceFactory
import com.example.foss101.data.auth.EncryptedTokenStorage
import com.example.foss101.data.auth.TokenStorage
import com.example.foss101.data.remote.network.ApiConfig
import com.example.foss101.data.remote.network.GlossaryApiServiceFactory

object RepositoryProvider {

    private enum class RepositoryMode {
        MOCK,
        API
    }

    private val repositoryMode = RepositoryMode.API

    private var tokenStorage: TokenStorage? = null

    fun init(context: Context) {
        if (tokenStorage == null) {
            tokenStorage = EncryptedTokenStorage(context.applicationContext)
        }
    }

    val authRepository: AuthRepository by lazy {
        val storage = requireTokenStorage()
        val config = ApiConfig.fromBuildConfig()
        ApiAuthRepository(
            authApiService = AuthApiServiceFactory.create(config),
            tokenStorage = storage
        )
    }

    val glossaryRepository: GlossaryRepository by lazy {
        when (repositoryMode) {
            RepositoryMode.MOCK -> MockGlossaryRepository()
            RepositoryMode.API -> {
                val config = ApiConfig.fromBuildConfig()
                val storage = requireTokenStorage()
                ApiGlossaryRepository(
                    glossaryApiService = GlossaryApiServiceFactory.create(
                        config = config,
                        tokenProvider = { storage.getToken() }
                    )
                )
            }
        }
    }

    private fun requireTokenStorage(): TokenStorage {
        return tokenStorage
            ?: error("RepositoryProvider.init(context) must be called before accessing repositories.")
    }
}
