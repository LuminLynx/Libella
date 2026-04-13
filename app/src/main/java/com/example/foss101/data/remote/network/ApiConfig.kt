package com.example.foss101.data.remote.network

data class ApiConfig(
    val baseUrl: String,
    val connectTimeoutMillis: Long = 15_000,
    val readTimeoutMillis: Long = 15_000
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://api.ai101.example/"
    }
}
