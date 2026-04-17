package com.example.foss101.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.PrimaryActionButton
import com.example.foss101.ui.components.screenContentPadding

@Composable
fun AiToolsScreen(onNavigate: (String) -> Unit) {
    AppScreenScaffold(title = "AI Learning Layer", subtitle = "Interactive learning tools") { padding ->
        Column(
            modifier = Modifier.screenContentPadding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryActionButton(
                text = "Ask Glossary",
                onClick = { onNavigate("ask_glossary") },
                modifier = Modifier.fillMaxWidth()
            )
            PrimaryActionButton(
                text = "Browse Terms for Scenario/Challenge",
                onClick = { onNavigate("browse") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
