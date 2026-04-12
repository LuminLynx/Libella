package com.example.foss101.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foss101.data.repository.GlossaryRepository
import com.example.foss101.ui.components.SectionHeader

@Composable
fun TermDetailsScreen(
    termId: String? = null,
    repository: GlossaryRepository
) {
    val term = remember(termId, repository) {
        termId?.let { repository.getTermById(it) }
    }

    if (term == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Term not found",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "The requested term could not be located.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = term.term,
                style = MaterialTheme.typography.headlineLarge
            )

            SectionHeader(
                title = "Short Definition",
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = term.shortDefinition,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )

            SectionHeader(
                title = "Full Explanation",
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = term.fullExplanation,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (term.exampleUsage != null) {
                SectionHeader(
                    title = "Example Usage",
                    modifier = Modifier.padding(top = 24.dp)
                )
                Text(
                    text = term.exampleUsage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
