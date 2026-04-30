package com.example.foss101.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.foss101.ui.components.NavigationTile
import com.example.foss101.ui.components.SectionHeader

private data class HomeDestination(
    val route: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

private val HomeDestinations = listOf(
    HomeDestination(
        route = "ai_tools",
        title = "AI Learning Layer",
        description = "Interactive scenarios and challenges",
        icon = Icons.Filled.AutoAwesome
    ),
    HomeDestination(
        route = "ask_glossary",
        title = "Ask Glossary",
        description = "Glossary-grounded AI answers",
        icon = Icons.Filled.Chat
    ),
    HomeDestination(
        route = "browse",
        title = "Browse Terms",
        description = "All glossary terms in one place",
        icon = Icons.Filled.MenuBook
    ),
    HomeDestination(
        route = "categories",
        title = "Categories",
        description = "Explore terms by topic",
        icon = Icons.Filled.Category
    ),
    HomeDestination(
        route = "search",
        title = "Search",
        description = "Find AI terms by keyword",
        icon = Icons.Filled.Search
    ),
    HomeDestination(
        route = "settings",
        title = "Settings",
        description = "App preferences and product info",
        icon = Icons.Filled.Settings
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("AI-101", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { innerPadding ->
        HomeScreenContent(
            destinations = HomeDestinations,
            onNavigate = onNavigate,
            contentPadding = innerPadding
        )
    }
}

@Composable
private fun HomeScreenContent(
    destinations: List<HomeDestination>,
    onNavigate: (String) -> Unit,
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Learn AI terms with glossary-backed AI tutoring, scenarios, and challenges.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionHeader(
            title = "Explore",
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )

        destinations.forEach { destination ->
            NavigationTile(
                title = destination.title,
                description = destination.description,
                leadingIcon = destination.icon,
                onClick = { onNavigate(destination.route) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        SectionHeader(
            title = "Concept previews",
            modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
        )

        NavigationTile(
            title = "Tokenization",
            description = "Bundle 0 proof — rich learning page + try-it widget",
            leadingIcon = Icons.Filled.Science,
            onClick = { onNavigate("preview_tokenization") },
            modifier = Modifier.fillMaxWidth()
        )

        NavigationTile(
            title = "Tokenization · bite feed",
            description = "Bundle 0 v3 — TikTok-shaped vertical swipe of 6 interactions",
            leadingIcon = Icons.Filled.Science,
            onClick = { onNavigate("preview_tokenization_bite") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
