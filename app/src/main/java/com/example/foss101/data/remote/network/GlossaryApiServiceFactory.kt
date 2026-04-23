package com.example.foss101.data.remote.network

import com.example.foss101.data.remote.api.GlossaryApiService
import com.example.foss101.data.remote.model.RemoteAskGlossaryResponse
import com.example.foss101.data.remote.model.RemoteCategory
import com.example.foss101.data.remote.model.RemoteGeneratedArtifactResult
import com.example.foss101.data.remote.model.RemoteGlossaryTerm
import com.example.foss101.data.remote.model.RemoteLearningChallenge
import com.example.foss101.data.remote.model.RemoteLearningScenario
import com.example.foss101.data.remote.model.RemoteTermDraftSubmission
import com.example.foss101.data.remote.model.RemoteTermDraftSubmissionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GlossaryApiServiceFactory {
    fun create(config: ApiConfig): GlossaryApiService {
        return HttpGlossaryApiService(config)
    }
}

private class HttpGlossaryApiService(
    private val config: ApiConfig
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

    override suspend fun askGlossary(question: String, termId: String?): RemoteAskGlossaryResponse = withContext(Dispatchers.IO) {
        val payload = JSONObject().put("question", question)
        if (!termId.isNullOrBlank()) {
            payload.put("termId", termId)
        }
        val response = post(path = "api/v1/ai/ask-glossary", payload = payload)
        parseAskGlossary(response)
    }

    override suspend fun generateScenario(
        termId: String,
        forceRefresh: Boolean
    ): RemoteGeneratedArtifactResult<RemoteLearningScenario> = withContext(Dispatchers.IO) {
        val encodedId = URLEncoder.encode(termId, Charsets.UTF_8.name())
        val response = post(
            path = "api/v1/ai/terms/$encodedId/scenario",
            payload = JSONObject().put("forceRefresh", forceRefresh)
        )
        parseGeneratedScenario(response)
    }

    override suspend fun generateChallenge(
        termId: String,
        forceRefresh: Boolean
    ): RemoteGeneratedArtifactResult<RemoteLearningChallenge> = withContext(Dispatchers.IO) {
        val encodedId = URLEncoder.encode(termId, Charsets.UTF_8.name())
        val response = post(
            path = "api/v1/ai/terms/$encodedId/challenge",
            payload = JSONObject().put("forceRefresh", forceRefresh)
        )
        parseGeneratedChallenge(response)
    }

    override suspend fun submitTermDraft(
        draft: RemoteTermDraftSubmission
    ): RemoteTermDraftSubmissionResult = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("slug", draft.slug)
            .put("term", draft.term)
            .put("definition", draft.definition)
            .put("explanation", draft.explanation)
            .put("humor", draft.humor)
            .put("see_also", JSONArray(draft.seeAlso))
            .put("tags", JSONArray(draft.tags))
            .put("category_id", draft.categoryId)

        draft.controversyLevel?.let { payload.put("controversy_level", it) }

        val response = post(path = "api/v1/term-drafts", payload = payload)
        parseTermDraftSubmission(response)
    }

    private fun get(path: String): JSONObject = request("GET", path, null)

    private fun post(path: String, payload: JSONObject): JSONObject = request("POST", path, payload)

    private fun request(method: String, path: String, payload: JSONObject?): JSONObject {
        val requestUrl = URL(config.baseUrl.trimEnd('/') + "/" + path)
        val connection = (requestUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = config.connectTimeoutMillis.toInt()
            readTimeout = config.readTimeoutMillis.toInt()
            setRequestProperty("Accept", "application/json")
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
        return List(data.length()) { index ->
            data.getJSONObject(index).toRemoteGlossaryTerm()
        }
    }

    private fun parseTerm(envelope: JSONObject): RemoteGlossaryTerm {
        val data = envelope.optJSONObject("data")
            ?: throw GlossaryApiException(message = "Term response was empty.")
        return data.toRemoteGlossaryTerm()
    }

    private fun parseCategories(envelope: JSONObject): List<RemoteCategory> {
        val data = envelope.optJSONArray("data") ?: JSONArray()
        return List(data.length()) { index ->
            data.getJSONObject(index).toRemoteCategory()
        }
    }

    private fun parseAskGlossary(envelope: JSONObject): RemoteAskGlossaryResponse {
        val data = envelope.optJSONObject("data")
            ?: throw GlossaryApiException(message = "Ask Glossary response was empty.")
        return RemoteAskGlossaryResponse(
            answer = data.getString("answer"),
            summary = data.getString("summary"),
            relatedTermIds = data.optJsonArray("relatedTermIds")
        )
    }

    private fun parseGeneratedScenario(envelope: JSONObject): RemoteGeneratedArtifactResult<RemoteLearningScenario> {
        val data = envelope.optJSONObject("data")
            ?: throw GlossaryApiException(message = "Scenario response was empty.")
        val artifact = data.optJSONObject("artifact")
            ?: throw GlossaryApiException(message = "Scenario artifact is missing.")
        return RemoteGeneratedArtifactResult(
            artifact = RemoteLearningScenario(
                title = artifact.getString("title"),
                difficulty = artifact.getString("difficulty"),
                context = artifact.getString("context"),
                objective = artifact.getString("objective"),
                tasks = artifact.optJsonArray("tasks"),
                reflectionQuestions = artifact.optJsonArray("reflectionQuestions")
            ),
            cached = data.optBoolean("cached", false)
        )
    }

    private fun parseGeneratedChallenge(envelope: JSONObject): RemoteGeneratedArtifactResult<RemoteLearningChallenge> {
        val data = envelope.optJSONObject("data")
            ?: throw GlossaryApiException(message = "Challenge response was empty.")
        val artifact = data.optJSONObject("artifact")
            ?: throw GlossaryApiException(message = "Challenge artifact is missing.")
        return RemoteGeneratedArtifactResult(
            artifact = RemoteLearningChallenge(
                title = artifact.getString("title"),
                difficulty = artifact.getString("difficulty"),
                prompt = artifact.getString("prompt"),
                successCriteria = artifact.optJsonArray("successCriteria"),
                hint = artifact.getString("hint")
            ),
            cached = data.optBoolean("cached", false)
        )
    }

    private fun parseTermDraftSubmission(envelope: JSONObject): RemoteTermDraftSubmissionResult {
        val data = envelope.optJSONObject("data")
            ?: throw GlossaryApiException(message = "Draft submission response was empty.")
        return RemoteTermDraftSubmissionResult(
            id = data.optString("id").ifBlank { data.optString("draft_id") },
            status = data.optString("status", "draft")
        )
    }
}

private fun JSONObject.toRemoteGlossaryTerm(): RemoteGlossaryTerm {
    return RemoteGlossaryTerm(
        id = getString("id"),
        term = getString("term"),
        shortDefinition = getString("shortDefinition"),
        fullExplanation = getString("fullExplanation"),
        categoryId = getString("categoryId"),
        tags = optJsonArray("tags"),
        relatedTerms = optJsonArray("relatedTerms"),
        exampleUsage = optNullableString("exampleUsage"),
        source = optNullableString("source"),
        createdAt = getString("createdAt"),
        updatedAt = getString("updatedAt")
    )
}

private fun JSONObject.toRemoteCategory(): RemoteCategory {
    return RemoteCategory(
        id = getString("id"),
        name = getString("name"),
        description = getString("description")
    )
}

private fun JSONObject.optJsonArray(field: String): List<String> {
    val array = optJSONArray(field) ?: return emptyList()
    return List(array.length()) { index -> array.optString(index) }.filter { it.isNotBlank() }
}

private fun JSONObject.optNullableString(field: String): String? {
    if (isNull(field)) return null
    return optString(field).takeIf { it.isNotBlank() }
}

internal class GlossaryApiException(
    override val message: String,
    val code: String? = null,
    val statusCode: Int? = null
) : RuntimeException(message)