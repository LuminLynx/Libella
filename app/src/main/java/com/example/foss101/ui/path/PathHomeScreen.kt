package com.example.foss101.ui.path

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.CompletionCache
import com.example.foss101.data.repository.PathRepository
import com.example.foss101.model.UnitManifestEntry
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.PrimaryActionButton
import com.example.foss101.ui.components.SectionHeader
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.viewmodel.PathHomeEvent
import com.example.foss101.viewmodel.PathHomeUiState
import com.example.foss101.viewmodel.PathHomeViewModel

@Composable
fun PathHomeScreen(
    pathRepository: PathRepository,
    completionCache: CompletionCache,
    onOpenUnit: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onAuthExpired: () -> Unit
) {
    val viewModel: PathHomeViewModel = viewModel(
        factory = PathHomeViewModel.factory(pathRepository, completionCache)
    )

    // The VM's init already loads on first composition. On subsequent
    // resumes (returning from the unit reader, from settings, or from
    // a successful sign-in via auth_login), trigger a fresh load() so
    // the cross-device completion sync picks up server-side state and
    // any auth changes are reflected. The cheap path-only refresh from
    // the local cache stays as the default to avoid unnecessary network
    // — load() is the strict superset that also re-syncs from the API.
    var initialMount by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        if (initialMount) {
            initialMount = false
            viewModel.refreshFromCache()
        } else {
            viewModel.load()
        }
        onPauseOrDispose { }
    }

    val state = viewModel.uiState

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PathHomeEvent.AuthExpired -> onAuthExpired()
            }
        }
    }

    AppScreenScaffold(
        title = "LLM Systems for PMs",
        subtitle = "Path home",
        actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    ) { contentPadding ->
        when (val current = state) {
            is PathHomeUiState.Loading -> LoadingBox(modifier = Modifier.screenContentPadding(contentPadding))
            is PathHomeUiState.Error -> ErrorBox(
                message = if (current.authExpired) "Sign in to continue." else current.message,
                onRetry = viewModel::load,
                modifier = Modifier.screenContentPadding(contentPadding)
            )
            is PathHomeUiState.Loaded -> LoadedBody(
                state = current,
                onOpenUnit = onOpenUnit,
                modifier = Modifier.screenContentPadding(contentPadding)
            )
        }
    }
}

@Composable
private fun LoadedBody(
    state: PathHomeUiState.Loaded,
    onOpenUnit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (state.path.description.isNotBlank()) {
            Text(
                text = state.path.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        SectionHeader(title = "Units")

        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = state.path.units, key = { it.id }) { unit ->
                UnitRow(
                    unit = unit,
                    completed = unit.id in state.completedUnitIds,
                    onClick = { onOpenUnit(unit.id) }
                )
            }
        }

        val nextUnit = state.nextUnit
        if (nextUnit != null) {
            PrimaryActionButton(
                text = "Continue · ${nextUnit.title}",
                onClick = { onOpenUnit(nextUnit.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
        } else {
            Text(
                text = "Path complete. Phase 2 (the grader) opens next.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun UnitRow(
    unit: UnitManifestEntry,
    completed: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (completed) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = if (completed) "Completed" else "Not started",
                tint = if (completed) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = unit.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Unit ${unit.position} · ${unit.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Open"
            )
        }
    }
}

@Composable
private fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        PrimaryActionButton(
            text = "Retry",
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
