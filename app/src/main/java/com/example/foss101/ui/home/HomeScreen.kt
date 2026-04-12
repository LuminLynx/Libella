package com.example.foss101.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foss101.ui.components.PrimaryActionButton
import com.example.foss101.ui.components.SectionHeader

private val HomeRoutes = listOf(
    "browse" to "Browse Terms",
    "categories" to "Categories",
    "search" to "Search",
    "settings" to "Settings"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("FOSS-101") })
        }
    ) { innerPadding ->
        HomeScreenContent(
            routes = HomeRoutes,
            onNavigate = onNavigate,
            contentPadding = innerPadding
        )
    }
}

@Composable
private fun HomeScreenContent(
    routes: List<Pair<String, String>>,
    onNavigate: (String) -> Unit,
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Learn FOSS and AI terms with simple glossary flows.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionHeader(
            title = "Explore",
            modifier = Modifier.padding(top = 8.dp)
        )

        routes.forEach { (route, label) ->
            PrimaryActionButton(
                text = label,
                onClick = { onNavigate(route) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
