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

    override suspend fun askGlossary(
        question: String,
        termId: String?
    ): RemoteAskGlossaryResponse = withContext(Dispatchers.IO) {
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
            .put("seeAlso", JSONArray(draft.seeAlso))
            .put("tags", JSONArray(draft.tags))
            .put("categoryId", draft.categoryId)
            .put("controversyLevel", draft.controversyLevel)
            .put("status", "submitted")

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

    private fun parseAskGlossary(envelope: JSONObject): RemoteAskGlossaryResponse {
        val data = envelope.optJSONObject("data")
            ?: throw GlossaryApiException(message = "Ask Glossary response was empty.")
        return RemoteAskGlossaryResponse(
            answer = data.optString("answer"),
            summary = data.optString("summary"),
            relatedTermIds = data.optJSONArray("relatedTermIds").toStringList()
        )
    }

    private fun parseGeneratedScenario(envelope: JSONObject): RemoteGeneratedArtifactResult<RemoteLearningScenario> {
        val data = envelope.optJSONObject("data")
            ?: throw GlossaryApiException(message = "Scenario response was empty.")
        val artifact = data.optJSONObject("artifact")
            ?: throw GlossaryApiException(message = "Scenario artifact is missing.")

        return RemoteGeneratedArtifactResult(
            artifact = RemoteLearningScenario(
                title = artifact.optString("title"),
                difficulty = artifact.optString("difficulty"),
                context = artifact.optString("context"),
                objective = artifact.optString("objective"),
                tasks = artifact.optJSONArray("tasks").toStringList(),
                reflectionQuestions = artifact.optJSONArray("reflectionQuestions").toStringList()
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
                title = artifact.optString("title"),
                difficulty = artifact.optString("difficulty"),
                prompt = artifact.optString("prompt"),
                successCriteria = artifact.optJSONArray("successCriteria").toStringList(),
                hint = artifact.optString("hint")
            ),
            cached = data.optBoolean("cached", false)
        )
    }

    private fun parseTermDraftSubmission(envelope: JSONObject): RemoteTermDraftSubmissionResult {
        val data = envelope.optJSONObject("data")
            ?: throw GlossaryApiException(message = "Draft submission response was empty.")

        return RemoteTermDraftSubmissionResult(
            id = data.opt("id")?.toString()
                ?: data.opt("draft_id")?.toString()
                ?: throw GlossaryApiException(message = "Server returned no draft id."),
            status = data.optString("status", "submitted")
        )
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
