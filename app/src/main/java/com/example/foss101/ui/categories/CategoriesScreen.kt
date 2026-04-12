package com.example.foss101.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.model.Category
import com.example.foss101.ui.browse.GlossaryTermItem

@Composable
fun CategoriesScreen(
    onNavigate: (String) -> Unit,
    repository: GlossaryRepository
) {
    val categories = remember(repository) { repository.getAllCategories() }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    if (categories.isEmpty()) {
        EmptyState(message = "No categories available.")
        return
    }

    if (selectedCategoryId == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Explore terms by category.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        onClick = { selectedCategoryId = category.id }
                    )
                }
            }
        }
        return
    }

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    val filteredTerms = remember(selectedCategoryId, repository) {
        selectedCategoryId?.let(repository::getTermsByCategory).orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = selectedCategory?.name ?: "Category",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = selectedCategory?.description.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        Text(
            text = "Back to categories",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .clickable { selectedCategoryId = null }
        )

        if (filteredTerms.isEmpty()) {
            EmptyState(message = "No terms found for this category.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTerms) { term ->
                    GlossaryTermItem(
                        term = term,
                        onClick = { onNavigate("details/${term.id}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
