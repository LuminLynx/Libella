package com.example.foss101.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foss101.ui.components.SectionHeader

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "MVP app preferences and project info.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionHeader(title = "Appearance")
        SettingsInfoCard(
            title = "Theme",
            description = "Theme is currently managed by system defaults for MVP."
        )

        SectionHeader(title = "About")
        SettingsInfoCard(
            title = "Product",
            description = "AI-101 is an AI Terms Glossary designed for beginner-friendly learning."
        )
        SettingsInfoCard(
            title = "MVP Scope",
            description = "Browse, Categories, Search, Details, and Settings are active. Non-MVP features stay isolated from the main flow."
        )
    }
}

@Composable
private fun SettingsInfoCard(
    title: String,
    description: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
