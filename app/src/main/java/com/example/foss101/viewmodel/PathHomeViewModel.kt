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

class PathHomeViewModel(
    private val pathRepository: PathRepository,
    private val completionCache: CompletionCache,
    private val pathId: String = CANONICAL_PATH_ID
) : ViewModel() {

    var uiState: PathHomeUiState by mutableStateOf(PathHomeUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = PathHomeUiState.Loading
        viewModelScope.launch {
            uiState = try {
                val path = pathRepository.getPath(pathId)
                val completed = completionCache.completedUnitIds()
                PathHomeUiState.Loaded(
                    path = path,
                    completedUnitIds = completed,
                    nextUnit = path.units.firstOrNull { it.id !in completed }
                )
            } catch (error: PathApiException) {
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
