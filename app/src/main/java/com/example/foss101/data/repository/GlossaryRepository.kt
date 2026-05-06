package com.example.foss101.data.repository

import com.example.foss101.model.Category
import com.example.foss101.model.GlossaryTerm

interface GlossaryRepository {
    suspend fun getAllTerms(): List<GlossaryTerm>
    suspend fun getTermById(id: String): GlossaryTerm?
    suspend fun getAllCategories(): List<Category>
    suspend fun searchTerms(query: String): List<GlossaryTerm>
    suspend fun getTermsByCategory(categoryId: String): List<GlossaryTerm>
}
