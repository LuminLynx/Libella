package com.example.foss101.data.remote.network

import com.example.foss101.BuildConfig

data class ApiConfig(
    val baseUrl: String,
    val connectTimeoutMillis: Long = 15_000,
    val readTimeoutMillis: Long = 15_000
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://aware-wholeness-production-d771.up.railway.app/"

        fun fromBuildConfig(): ApiConfig {
            val configuredBaseUrl = BuildConfig.API_BASE_URL
                .ifBlank { DEFAULT_BASE_URL }
                .let { if (it.endsWith('/')) it else "$it/" }
            return ApiConfig(baseUrl = configuredBaseUrl)
        }
    }
}
