package com.example.foss101.data.remote.api

import com.example.foss101.data.remote.model.RemoteAskGlossaryResponse
import com.example.foss101.data.remote.model.RemoteCategory
import com.example.foss101.data.remote.model.RemoteGeneratedArtifactResult
import com.example.foss101.data.remote.model.RemoteGlossaryTerm
import com.example.foss101.data.remote.model.RemoteLearningChallenge
import com.example.foss101.data.remote.model.RemoteLearningCompletionResult
import com.example.foss101.data.remote.model.RemoteLearningScenario
import com.example.foss101.data.remote.model.RemoteTermDraftSubmission
import com.example.foss101.data.remote.model.RemoteTermDraftSubmissionResult

interface GlossaryApiService {
    suspend fun getTerms(): List<RemoteGlossaryTerm>
    suspend fun getTermDetails(termId: String): RemoteGlossaryTerm
    suspend fun getCategories(): List<RemoteCategory>
    suspend fun getTermsByCategory(categoryId: String): List<RemoteGlossaryTerm>
    suspend fun searchTerms(query: String): List<RemoteGlossaryTerm>
    suspend fun askGlossary(question: String, termId: String? = null): RemoteAskGlossaryResponse
    suspend fun generateScenario(
        termId: String,
        forceRefresh: Boolean = false,
        preset: String? = null
    ): RemoteGeneratedArtifactResult<RemoteLearningScenario>
    suspend fun generateChallenge(
        termId: String,
        forceRefresh: Boolean = false,
        preset: String? = null
    ): RemoteGeneratedArtifactResult<RemoteLearningChallenge>
    suspend fun submitTermDraft(draft: RemoteTermDraftSubmission): RemoteTermDraftSubmissionResult
    suspend fun submitLearningCompletion(
        termId: String,
        artifactType: String,
        confidence: String,
        reflectionNotes: String?
    ): RemoteLearningCompletionResult
}
