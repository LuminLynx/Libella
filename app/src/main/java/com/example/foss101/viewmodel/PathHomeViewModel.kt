package com.example.foss101.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foss101.data.remote.api.PathApiException
import com.example.foss101.data.repository.CompletionCache
import com.example.foss101.data.repository.PathRepository
import com.example.foss101.model.Path
import com.example.foss101.model.UnitManifestEntry
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

const val CANONICAL_PATH_ID: String = "llm-systems-for-pms"

sealed interface PathHomeUiState {
    object Loading : PathHomeUiState
    data class Error(val message: String, val authExpired: Boolean = false) : PathHomeUiState
    data class Loaded(
        val path: Path,
        val completedUnitIds: Set<String>,
        val nextUnit: UnitManifestEntry?
    ) : PathHomeUiState
}

/** One-shot UI event emitted at most once per occurrence (Channel-backed). */
sealed interface PathHomeEvent {
    object AuthExpired : PathHomeEvent
}

class PathHomeViewModel(
    private val pathRepository: PathRepository,
    private val completionCache: CompletionCache,
    private val pathId: String = CANONICAL_PATH_ID
) : ViewModel() {

    var uiState: PathHomeUiState by mutableStateOf(PathHomeUiState.Loading)
        private set

    private val _events = Channel<PathHomeEvent>(Channel.BUFFERED)
    val events: Flow<PathHomeEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        uiState = PathHomeUiState.Loading
        viewModelScope.launch {
            uiState = try {
                // Best-effort: pull completion state from the server before
                // we read the local cache, so completion syncs across devices
                // for the same account. Failures (offline, 401, server down)
                // are intentionally swallowed — we fall through to whatever
                // is already cached locally, which is the v1 baseline.
                runCatching { pathRepository.syncCompletedUnits() }

                val path = pathRepository.getPath(pathId)
                val completed = completionCache.completedUnitIds()
                PathHomeUiState.Loaded(
                    path = path,
                    completedUnitIds = completed,
                    nextUnit = path.units.firstOrNull { it.id !in completed }
                )
            } catch (error: PathApiException) {
                if (error.statusCode == 401) {
                    _events.send(PathHomeEvent.AuthExpired)
                }
                PathHomeUiState.Error(
                    message = error.message.ifBlank { "Couldn't load the path." },
                    authExpired = error.statusCode == 401
                )
            } catch (error: Exception) {
                PathHomeUiState.Error(message = "Network error. Pull to retry.")
            }
        }
    }

    /** Re-evaluate "next unit" using the current cache; cheap, no network. */
    fun refreshFromCache() {
        val current = uiState
        if (current !is PathHomeUiState.Loaded) return
        val completed = completionCache.completedUnitIds()
        uiState = current.copy(
            completedUnitIds = completed,
            nextUnit = current.path.units.firstOrNull { it.id !in completed }
        )
    }

    companion object {
        fun factory(
            pathRepository: PathRepository,
            completionCache: CompletionCache,
            pathId: String = CANONICAL_PATH_ID
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PathHomeViewModel(pathRepository, completionCache, pathId) as T
            }
        }
    }
}
