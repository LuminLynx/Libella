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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
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
        route = "glossary",
        title = "Glossary",
        description = "Search, filter, and browse terms",
        icon = Icons.Filled.MenuBook
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
    }
}
