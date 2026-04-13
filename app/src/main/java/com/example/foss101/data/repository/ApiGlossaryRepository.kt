package com.example.foss101.data.repository

import com.example.foss101.data.remote.api.GlossaryApiService
import com.example.foss101.data.remote.model.toDomain
import com.example.foss101.model.Category
import com.example.foss101.model.GlossaryTerm

class ApiGlossaryRepository(
    private val glossaryApiService: GlossaryApiService
) : GlossaryRepository {

    override suspend fun getAllTerms(): List<GlossaryTerm> {
        return glossaryApiService.getTerms().map { it.toDomain() }
    }

    override suspend fun getTermById(id: String): GlossaryTerm? {
        return glossaryApiService.getTermDetails(id).toDomain()
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
}
