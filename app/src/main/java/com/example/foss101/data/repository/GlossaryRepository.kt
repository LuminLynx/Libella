package com.example.foss101.data.repository

import com.example.foss101.model.Category
import com.example.foss101.model.GlossaryTerm

interface GlossaryRepository {
    fun getAllTerms(): List<GlossaryTerm>
    fun getTermById(id: String): GlossaryTerm?
    fun getAllCategories(): List<Category>
    fun searchTerms(query: String): List<GlossaryTerm>
    fun getTermsByCategory(categoryId: String): List<GlossaryTerm>
}