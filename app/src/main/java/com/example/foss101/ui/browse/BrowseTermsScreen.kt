package com.example.foss101.ui.browse

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foss101.data.repository.MockGlossaryRepository
import com.example.foss101.model.GlossaryTerm

@Composable
fun BrowseTermsScreen(onNavigate: (String) -> Unit) {
    val repository = remember { MockGlossaryRepository() }
    val terms = remember { repository.getAllTerms() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(terms) { term ->
            GlossaryTermItem(
                term = term,
                onClick = { onNavigate("details/${term.id}") }
            )
        }
    }
}

@Composable
fun GlossaryTermItem(
    term: GlossaryTerm,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = term.term,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = term.shortDefinition,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
