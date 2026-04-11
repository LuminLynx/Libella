package com.example.foss101.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foss101.data.repository.MockGlossaryRepository
import com.example.foss101.ui.browse.GlossaryTermItem

@Composable
fun SearchScreen(onNavigate: (String) -> Unit) {
    val repository = remember { MockGlossaryRepository() }
    var query by remember { mutableStateOf("") }
    val results = remember(query) {
        if (query.isBlank()) {
            emptyList()
        } else {
            repository.searchTerms(query)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search terms") },
            placeholder = { Text("Type a term or keyword") }
        )

        if (query.isBlank()) {
            Text(
                text = "Enter a search query to see results.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            return@Column
        }

        if (results.isEmpty()) {
            Text(
                text = "No search results found.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { term ->
                GlossaryTermItem(
                    term = term,
                    onClick = { onNavigate("details/${term.id}") }
                )
            }
        }
    }
}
