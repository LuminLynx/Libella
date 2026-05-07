package com.example.foss101.data.repository

import android.content.Context
import com.example.foss101.data.auth.AuthApiServiceFactory
import com.example.foss101.data.auth.EncryptedTokenStorage
import com.example.foss101.data.auth.TokenStorage
import com.example.foss101.data.remote.network.ApiConfig
import com.example.foss101.data.remote.network.GlossaryApiServiceFactory
import com.example.foss101.data.remote.network.PathApiServiceFactory

object RepositoryProvider {

    private var tokenStorage: TokenStorage? = null
    private var completionCache: CompletionCache? = null

    fun init(context: Context) {
        if (tokenStorage == null) {
            tokenStorage = EncryptedTokenStorage(context.applicationContext)
        }
        if (completionCache == null) {
            completionCache = SharedPrefsCompletionCache(context.applicationContext)
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
        val config = ApiConfig.fromBuildConfig()
        val storage = requireTokenStorage()
        ApiGlossaryRepository(
            glossaryApiService = GlossaryApiServiceFactory.create(
                config = config,
                tokenProvider = { storage.getToken() }
            )
        )
    }

    val pathRepository: PathRepository by lazy {
        val config = ApiConfig.fromBuildConfig()
        val storage = requireTokenStorage()
        ApiPathRepository(
            pathApiService = PathApiServiceFactory.create(
                config = config,
                tokenProvider = { storage.getToken() }
            ),
            completionCache = requireCompletionCache()
        )
    }

    val completionCacheInstance: CompletionCache
        get() = requireCompletionCache()

    private fun requireTokenStorage(): TokenStorage {
        return tokenStorage
            ?: error("RepositoryProvider.init(context) must be called before accessing repositories.")
    }

    private fun requireCompletionCache(): CompletionCache {
        return completionCache
            ?: error("RepositoryProvider.init(context) must be called before accessing repositories.")
    }
}
