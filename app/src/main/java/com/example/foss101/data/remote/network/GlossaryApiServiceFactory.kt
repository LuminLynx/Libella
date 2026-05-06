package com.example.foss101.data.remote.network

import com.example.foss101.data.remote.api.GlossaryApiService
import com.example.foss101.data.remote.model.RemoteCategory
import com.example.foss101.data.remote.model.RemoteGlossaryTerm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GlossaryApiServiceFactory {
    fun create(
        config: ApiConfig,
        tokenProvider: () -> String? = { null }
    ): GlossaryApiService {
        return HttpGlossaryApiService(config, tokenProvider)
    }
}

private class HttpGlossaryApiService(
    private val config: ApiConfig,
    private val tokenProvider: () -> String?
) : GlossaryApiService {

    override suspend fun getTerms(): List<RemoteGlossaryTerm> = withContext(Dispatchers.IO) {
        val response = get(path = "api/v1/terms")
        parseTerms(response)
    }

    override suspend fun getTermDetails(termId: String): RemoteGlossaryTerm = withContext(Dispatchers.IO) {
        val encodedId = URLEncoder.encode(termId, Charsets.UTF_8.name())
        val response = get(path = "api/v1/terms/$encodedId")
        parseTerm(response)
    }

    override suspend fun getCategories(): List<RemoteCategory> = withContext(Dispatchers.IO) {
        val response = get(path = "api/v1/categories")
        parseCategories(response)
    }

    override suspend fun getTermsByCategory(categoryId: String): List<RemoteGlossaryTerm> = withContext(Dispatchers.IO) {
        val encodedCategoryId = URLEncoder.encode(categoryId, Charsets.UTF_8.name())
        val response = get(path = "api/v1/categories/$encodedCategoryId/terms")
        parseTerms(response)
    }

    override suspend fun searchTerms(query: String): List<RemoteGlossaryTerm> = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
        val response = get(path = "api/v1/search/terms?q=$encodedQuery")
        parseTerms(response)
    }

    private fun get(path: String): JSONObject = request("GET", path, null)

    private fun request(method: String, path: String, payload: JSONObject?): JSONObject {
        val requestUrl = URL(config.baseUrl.trimEnd('/') + "/" + path)
        val connection = (requestUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = config.connectTimeoutMillis.toInt()
            readTimeout = config.readTimeoutMillis.toInt()
            setRequestProperty("Accept", "application/json")
            tokenProvider()?.takeIf { it.isNotBlank() }?.let { token ->
                setRequestProperty("Authorization", "Bearer $token")
            }
            if (payload != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }

        return try {
            if (payload != null) {
                connection.outputStream.bufferedWriter().use { it.write(payload.toString()) }
            }
            val responseCode = connection.responseCode
            val responseText = when {
                responseCode in 200..299 -> connection.inputStream.bufferedReader().use { it.readText() }
                else -> connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            parseEnvelope(responseText, responseCode)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseEnvelope(responseText: String, responseCode: Int): JSONObject {
        val envelope = runCatching { JSONObject(responseText) }
            .getOrElse {
                throw GlossaryApiException(
                    message = "Unexpected server response (HTTP $responseCode).",
                    statusCode = responseCode
                )
            }

        val errorObject = envelope.optJSONObject("error")
        if (errorObject != null && !errorObject.isNull("code")) {
            throw GlossaryApiException(
                message = errorObject.optString("message", "Request failed."),
                code = errorObject.optString("code", "UNKNOWN_ERROR"),
                statusCode = responseCode
            )
        }

        if (!envelope.has("data") || envelope.isNull("data")) {
            throw GlossaryApiException(
                message = "Server returned no data.",
                statusCode = responseCode
            )
        }

        return envelope
    }

    private fun parseTerms(envelope: JSONObject): List<RemoteGlossaryTerm> {
        val data = envelope.optJSONArray("data") ?: JSONArray()
        return buildList {
            for (index in 0 until data.length()) {
                add(parseRemoteGlossaryTerm(data.getJSONObject(index)))
            }
        }
    }

    private fun parseTerm(envelope: JSONObject): RemoteGlossaryTerm {
        val data = envelope.optJSONObject("data")
            ?: throw GlossaryApiException(message = "Term response was empty.")
        return parseRemoteGlossaryTerm(data)
    }

    private fun parseCategories(envelope: JSONObject): List<RemoteCategory> {
        val data = envelope.optJSONArray("data") ?: JSONArray()
        return buildList {
            for (index in 0 until data.length()) {
                val item = data.getJSONObject(index)
                add(
                    RemoteCategory(
                        id = item.optString("id"),
                        name = item.optString("name"),
                        description = item.optString("description")
                    )
                )
            }
        }
    }

    private fun parseRemoteGlossaryTerm(item: JSONObject): RemoteGlossaryTerm {
        val slug = item.optString("slug").takeIf { it.isNotBlank() } ?: item.optString("id")
        val definition = item.optString("definition").takeIf { it.isNotBlank() }
            ?: item.optString("shortDefinition")
        val explanation = item.optString("explanation").takeIf { it.isNotBlank() }
            ?: item.optString("fullExplanation")
        val humor = item.optString("humor").takeIf { it.isNotBlank() }

        val categoryId = item.optString("categoryId").takeIf { it.isNotBlank() }
            ?: item.optString("category_id")

        val seeAlso = item.optJSONArray("seeAlso").toStringList().ifEmpty {
            item.optJSONArray("see_also").toStringList().ifEmpty {
                item.optJSONArray("relatedTerms").toStringList()
            }
        }

        val controversyLevel = when {
            item.has("controversyLevel") -> item.optInt("controversyLevel", 0)
            item.has("controversy_level") -> item.optInt("controversy_level", 0)
            else -> 0
        }

        return RemoteGlossaryTerm(
            id = item.optString("id"),
            slug = slug,
            term = item.optString("term"),
            definition = definition,
            explanation = explanation,
            humor = humor,
            categoryId = categoryId,
            tags = item.optJSONArray("tags").toStringList(),
            seeAlso = seeAlso,
            controversyLevel = controversyLevel,
            exampleUsage = item.optString("exampleUsage").takeIf { it.isNotBlank() },
            source = item.optString("source").takeIf { it.isNotBlank() }
        )
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            add(optString(index))
        }
    }.filter { it.isNotBlank() }
}

internal class GlossaryApiException(
    override val message: String,
    val code: String? = null,
    val statusCode: Int? = null
) : RuntimeException(message)
