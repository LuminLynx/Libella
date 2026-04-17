package com.example.foss101.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.SectionHeader
import com.example.foss101.ui.components.screenContentPadding

@Composable
fun SettingsScreen() {
    AppScreenScaffold(
        title = "Settings",
        subtitle = "App preferences and product information"
    ) { contentPadding ->
        Column(
            modifier = Modifier.screenContentPadding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Manage the app appearance and review product information.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionHeader(title = "Appearance")
            SettingsInfoCard(
                title = "Theme",
                description = "The app currently follows the system theme automatically."
            )

            SectionHeader(title = "About")
            SettingsInfoCard(
                title = "Product",
                description = "AI-101 is an AI Terms Glossary designed for clear, beginner-friendly learning."
            )
            SettingsInfoCard(
                title = "Core Experience",
                description = "Browse, Categories, Search, Details, and Settings are available in the live app."
            )
        }
    }
}

@Composable
private fun SettingsInfoCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}