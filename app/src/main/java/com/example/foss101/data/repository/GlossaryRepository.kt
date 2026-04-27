package com.example.foss101.data.repository

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

interface GlossaryRepository {
    suspend fun getAllTerms(): List<GlossaryTerm>
    suspend fun getTermById(id: String): GlossaryTerm?
    suspend fun getAllCategories(): List<Category>
    suspend fun searchTerms(query: String): List<GlossaryTerm>
    suspend fun getTermsByCategory(categoryId: String): List<GlossaryTerm>
    suspend fun askGlossary(question: String, termId: String? = null): AskGlossaryResponse
    suspend fun generateScenario(
        termId: String,
        forceRefresh: Boolean = false,
        preset: LearningPreset? = null
    ): GeneratedArtifactResult<LearningScenario>
    suspend fun generateChallenge(
        termId: String,
        forceRefresh: Boolean = false,
        preset: LearningPreset? = null
    ): GeneratedArtifactResult<LearningChallenge>
    suspend fun submitTermDraft(draft: TermDraftSubmission): TermDraftSubmissionResult
    suspend fun submitLearningCompletion(
        termId: String,
        artifactType: ArtifactKind,
        confidence: CompletionConfidence,
        reflectionNotes: String?
    ): LearningCompletionResult
}
