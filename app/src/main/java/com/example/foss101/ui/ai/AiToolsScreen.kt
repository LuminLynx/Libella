package com.example.foss101.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.NavigationTile
import com.example.foss101.ui.components.screenContentPadding

@Composable
fun AiToolsScreen(onNavigate: (String) -> Unit) {
    AppScreenScaffold(
        title = "AI Learning Layer",
        subtitle = "Interactive learning tools"
    ) { padding ->
        Column(
            modifier = Modifier.screenContentPadding(padding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NavigationTile(
                title = "Ask Glossary",
                description = "Glossary-grounded AI answers",
                leadingIcon = Icons.Filled.Chat,
                onClick = { onNavigate("ask_glossary") },
                modifier = Modifier.fillMaxWidth()
            )
            NavigationTile(
                title = "Scenarios & Challenges",
                description = "Pick a term to practice",
                leadingIcon = Icons.Filled.School,
                onClick = { onNavigate("browse") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
