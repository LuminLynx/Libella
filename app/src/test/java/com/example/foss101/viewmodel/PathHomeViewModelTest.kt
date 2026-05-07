package com.example.foss101.viewmodel

import com.example.foss101.data.remote.api.PathApiException
import com.example.foss101.data.repository.CompletionCache
import com.example.foss101.data.repository.PathRepository
import com.example.foss101.model.CompletionRecord
import com.example.foss101.model.Path
import com.example.foss101.model.UnitDetail
import com.example.foss101.model.UnitManifestEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    fun `401 emits a single AuthExpired event (no re-emit while state lingers)`() = runTest(dispatcher) {
        val viewModel = PathHomeViewModel(
            pathRepository = FakePathRepository(
                error = PathApiException("expired", statusCode = 401)
            ),
            completionCache = FakeCompletionCache()
        )

        advanceUntilIdle()
        val first = withTimeoutOrNull(1_000) { viewModel.events.first() }
        assertNotNull("expected one AuthExpired event", first)

        // No second event should arrive while the screen sits on the same Error state.
        advanceUntilIdle()
        val second = withTimeoutOrNull(50) { viewModel.events.first() }
        assertNull("AuthExpired must be one-shot, not re-emitted from stale state", second)
    }

    @Test
    fun `load syncs server completions into the local cache before computing nextUnit`() = runTest(dispatcher) {
        // Cache starts empty (e.g. fresh install on a new device); the
        // server reports the user already completed u-1. After load we
        // expect u-1 to appear in the cache so "Continue" advances to u-2.
        val path = samplePath(
            UnitManifestEntry("u-1", "a", "A", 1, "draft"),
            UnitManifestEntry("u-2", "b", "B", 2, "draft")
        )
        val cache = FakeCompletionCache()
        val repository = FakePathRepository(
            path = path,
            cacheToSeed = cache,
            syncedUnitIds = setOf("u-1")
        )
        val viewModel = PathHomeViewModel(repository, cache)

        advanceUntilIdle()

        val state = viewModel.uiState as PathHomeUiState.Loaded
        assertTrue("u-1" in state.completedUnitIds)
        assertEquals("u-2", state.nextUnit?.id)
        assertEquals(1, repository.syncCalls)
    }

    @Test
    fun `sync failure does not break load (best-effort)`() = runTest(dispatcher) {
        // If the sync call fails (offline, transient 500, etc.) the load
        // must still succeed using whatever is already cached locally.
        val path = samplePath(
            UnitManifestEntry("u-1", "a", "A", 1, "draft")
        )
        val cache = FakeCompletionCache(initial = emptySet())
        val repository = FakePathRepository(
            path = path,
            syncError = PathApiException("offline", statusCode = null)
        )
        val viewModel = PathHomeViewModel(repository, cache)

        advanceUntilIdle()

        val state = viewModel.uiState as PathHomeUiState.Loaded
        assertEquals("u-1", state.nextUnit?.id)
        assertEquals(1, repository.syncCalls)
        assertEquals(1, repository.getPathCalls)
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
    private val error: Throwable? = null,
    private val cacheToSeed: FakeCompletionCache? = null,
    private val syncedUnitIds: Set<String> = emptySet(),
    private val syncError: Throwable? = null
) : PathRepository {
    var getPathCalls = 0
        private set
    var syncCalls = 0
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

    override suspend fun syncCompletedUnits() {
        syncCalls++
        syncError?.let { throw it }
        cacheToSeed?.replaceAll(syncedUnitIds)
    }
}

private class FakeCompletionCache(initial: Set<String> = emptySet()) : CompletionCache {
    private val store = initial.toMutableSet()
    override fun completedUnitIds(): Set<String> = store.toSet()
    override fun add(unitId: String) { store.add(unitId) }
    override fun replaceAll(unitIds: Set<String>) {
        store.clear()
        store.addAll(unitIds)
    }
    override fun clear() { store.clear() }
}
