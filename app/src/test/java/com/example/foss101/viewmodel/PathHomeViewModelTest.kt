package com.example.foss101.viewmodel

import com.example.foss101.data.remote.api.PathApiException
import com.example.foss101.data.repository.CompletionCache
import com.example.foss101.data.repository.PathRepository
import com.example.foss101.model.CompletionRecord
import com.example.foss101.model.Path
import com.example.foss101.model.UnitDetail
import com.example.foss101.model.UnitManifestEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PathHomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `next unit is the lowest-position uncompleted unit`() = runTest(dispatcher) {
        val path = samplePath(
            UnitManifestEntry("u-1", "tokenization", "Tokenization", 1, "draft"),
            UnitManifestEntry("u-2", "context-windows", "Context Windows", 2, "draft"),
            UnitManifestEntry("u-3", "latency", "Latency", 3, "draft")
        )
        val viewModel = PathHomeViewModel(
            pathRepository = FakePathRepository(path = path),
            completionCache = FakeCompletionCache(initial = setOf("u-1"))
        )

        advanceUntilIdle()

        val state = viewModel.uiState as PathHomeUiState.Loaded
        assertEquals("u-2", state.nextUnit?.id)
        assertTrue(state.completedUnitIds.contains("u-1"))
    }

    @Test
    fun `nextUnit is null when every unit is completed`() = runTest(dispatcher) {
        val path = samplePath(
            UnitManifestEntry("u-1", "a", "A", 1, "draft"),
            UnitManifestEntry("u-2", "b", "B", 2, "draft")
        )
        val viewModel = PathHomeViewModel(
            pathRepository = FakePathRepository(path = path),
            completionCache = FakeCompletionCache(initial = setOf("u-1", "u-2"))
        )

        advanceUntilIdle()

        val state = viewModel.uiState as PathHomeUiState.Loaded
        assertNull(state.nextUnit)
    }

    @Test
    fun `error state surfaces with authExpired flag on 401`() = runTest(dispatcher) {
        val viewModel = PathHomeViewModel(
            pathRepository = FakePathRepository(
                error = PathApiException("expired", code = "TOKEN_EXPIRED", statusCode = 401)
            ),
            completionCache = FakeCompletionCache()
        )

        advanceUntilIdle()

        val state = viewModel.uiState as PathHomeUiState.Error
        assertTrue(state.authExpired)
    }

    @Test
    fun `refreshFromCache recomputes nextUnit without re-loading`() = runTest(dispatcher) {
        val path = samplePath(
            UnitManifestEntry("u-1", "a", "A", 1, "draft"),
            UnitManifestEntry("u-2", "b", "B", 2, "draft")
        )
        val cache = FakeCompletionCache()
        val repository = FakePathRepository(path = path)
        val viewModel = PathHomeViewModel(repository, cache)
        advanceUntilIdle()
        assertEquals("u-1", (viewModel.uiState as PathHomeUiState.Loaded).nextUnit?.id)

        cache.add("u-1")
        viewModel.refreshFromCache()
        assertEquals("u-2", (viewModel.uiState as PathHomeUiState.Loaded).nextUnit?.id)
        assertEquals("expected exactly one network call", 1, repository.getPathCalls)
    }

    private fun samplePath(vararg units: UnitManifestEntry): Path = Path(
        id = "llm-systems-for-pms",
        slug = "llm-systems-for-pms",
        title = "LLM Systems for PMs",
        description = "desc",
        units = units.toList()
    )
}

private class FakePathRepository(
    private val path: Path? = null,
    private val unit: UnitDetail? = null,
    private val error: Throwable? = null
) : PathRepository {
    var getPathCalls = 0
        private set

    override suspend fun getPath(pathId: String): Path {
        getPathCalls++
        error?.let { throw it }
        return path ?: error("no path stub set")
    }

    override suspend fun getUnit(unitId: String): UnitDetail {
        error?.let { throw it }
        return unit ?: error("no unit stub set")
    }

    override suspend fun markComplete(unitId: String): CompletionRecord {
        error?.let { throw it }
        return CompletionRecord(1L, "u", "p", unitId, "now")
    }
}

private class FakeCompletionCache(initial: Set<String> = emptySet()) : CompletionCache {
    private val store = initial.toMutableSet()
    override fun completedUnitIds(): Set<String> = store.toSet()
    override fun add(unitId: String) { store.add(unitId) }
    override fun clear() { store.clear() }
}
