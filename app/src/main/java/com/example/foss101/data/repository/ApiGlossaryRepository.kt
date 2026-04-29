package com.example.foss101.data.repository

import com.example.foss101.data.remote.api.GlossaryApiService
import com.example.foss101.data.remote.model.toDomain
import com.example.foss101.data.remote.model.toRemote
import com.example.foss101.data.remote.network.GlossaryApiException
import com.example.foss101.model.ArtifactKind
import com.example.foss101.model.AskGlossaryResponse
import com.example.foss101.model.Category
import com.example.foss101.model.CompletionConfidence
import com.example.foss101.model.GeneratedArtifactResult
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.model.LearningChallenge
import com.example.foss101.model.LearningCompletionResult
import com.example.foss101.model.LearningPreset
import com.example.foss101.model.LearningScenario
import com.example.foss101.model.TermDraftSubmission
import com.example.foss101.model.TermDraftSubmissionResult

class ApiGlossaryRepository(
    private val glossaryApiService: GlossaryApiService
) : GlossaryRepository {

    override suspend fun getAllTerms(): List<GlossaryTerm> {
        return glossaryApiService.getTerms().map { it.toDomain() }
    }

    override suspend fun getTermById(id: String): GlossaryTerm? {
        return try {
            glossaryApiService.getTermDetails(id).toDomain()
        } catch (error: GlossaryApiException) {
            if (error.code == "TERM_NOT_FOUND") {
                null
            } else {
                throw error
            }
        }
    }

    override suspend fun getAllCategories(): List<Category> {
        return glossaryApiService.getCategories().map { it.toDomain() }
    }

    override suspend fun searchTerms(query: String): List<GlossaryTerm> {
        return glossaryApiService.searchTerms(query).map { it.toDomain() }
    }

    override suspend fun getTermsByCategory(categoryId: String): List<GlossaryTerm> {
        return glossaryApiService.getTermsByCategory(categoryId).map { it.toDomain() }
    }

    override suspend fun askGlossary(question: String, termId: String?): AskGlossaryResponse {
        return glossaryApiService.askGlossary(question = question, termId = termId).toDomain()
    }

    override suspend fun generateScenario(
        termId: String,
        forceRefresh: Boolean,
        preset: LearningPreset?
    ): GeneratedArtifactResult<LearningScenario> {
        return glossaryApiService.generateScenario(
            termId = termId,
            forceRefresh = forceRefresh,
            preset = preset?.key
        ).toDomain { it.toDomain() }
    }

    override suspend fun generateChallenge(
        termId: String,
        forceRefresh: Boolean,
        preset: LearningPreset?
    ): GeneratedArtifactResult<LearningChallenge> {
        return glossaryApiService.generateChallenge(
            termId = termId,
            forceRefresh = forceRefresh,
            preset = preset?.key
        ).toDomain { it.toDomain() }
    }

    override suspend fun submitTermDraft(draft: TermDraftSubmission): TermDraftSubmissionResult {
        return glossaryApiService.submitTermDraft(draft.toRemote()).toDomain()
    }

    override suspend fun submitLearningCompletion(
        termId: String,
        artifactType: ArtifactKind,
        confidence: CompletionConfidence,
        reflectionNotes: String?
    ): LearningCompletionResult {
        return glossaryApiService.submitLearningCompletion(
            termId = termId,
            artifactType = artifactType.key,
            confidence = confidence.key,
            reflectionNotes = reflectionNotes
        ).toDomain()
    }
}
