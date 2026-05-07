package com.example.foss101.viewmodel

import com.example.foss101.data.remote.api.PathApiException
import com.example.foss101.data.repository.CompletionCache
import com.example.foss101.data.repository.PathRepository
import com.example.foss101.model.CompletionRecord
import com.example.foss101.model.Path
import com.example.foss101.model.UnitDetail
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnitReaderViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `loaded state exposes the unit`() = runTest(dispatcher) {
        val viewModel = newViewModel(
            repo = FakeRepo(unit = sampleUnit("u-1")),
            cache = FakeCache()
        )
        advanceUntilIdle()

        val state = viewModel.uiState as UnitReaderUiState.Loaded
        assertEquals("u-1", state.unit.id)
        assertFalse(state.depthExpanded)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `loaded state reads completion from cache so prior-session completions show as complete`() = runTest(dispatcher) {
        val viewModel = newViewModel(
            repo = FakeRepo(unit = sampleUnit("u-1")),
            cache = FakeCache(initial = setOf("u-1"))
        )
        advanceUntilIdle()

        val state = viewModel.uiState as UnitReaderUiState.Loaded
        assertTrue("isCompleted must reflect the local cache on load", state.isCompleted)
    }

    @Test
    fun `toggleDepth flips the disclosure`() = runTest(dispatcher) {
        val viewModel = newViewModel(repo = FakeRepo(unit = sampleUnit("u-1")), cache = FakeCache())
        advanceUntilIdle()

        viewModel.toggleDepth()
        assertTrue((viewModel.uiState as UnitReaderUiState.Loaded).depthExpanded)
        viewModel.toggleDepth()
        assertFalse((viewModel.uiState as UnitReaderUiState.Loaded).depthExpanded)
    }

    @Test
    fun `markComplete sets isCompleted and clears in-progress`() = runTest(dispatcher) {
        val repo = FakeRepo(unit = sampleUnit("u-1"))
        val viewModel = newViewModel(repo = repo, cache = FakeCache())
        advanceUntilIdle()

        viewModel.markComplete()
        advanceUntilIdle()

        val state = viewModel.uiState as UnitReaderUiState.Loaded
        assertTrue(state.isCompleted)
        assertFalse(state.markCompleteInProgress)
        assertNull(state.markCompleteFailure)
        assertEquals(1, repo.markCompleteCalls)
    }

    @Test
    fun `401 on markComplete surfaces a session-expired message`() = runTest(dispatcher) {
        val repo = FakeRepo(
            unit = sampleUnit("u-1"),
            markCompleteError = PathApiException("expired", statusCode = 401)
        )
        val viewModel = newViewModel(repo = repo, cache = FakeCache())
        advanceUntilIdle()

        viewModel.markComplete()
        advanceUntilIdle()

        val state = viewModel.uiState as UnitReaderUiState.Loaded
        assertNotNull(state.markCompleteFailure)
        assertTrue(state.markCompleteFailure!!.contains("session", ignoreCase = true))
    }

    @Test
    fun `error state surfaces authExpired on initial 401`() = runTest(dispatcher) {
        val repo = FakeRepo(
            getUnitError = PathApiException("expired", statusCode = 401)
        )
        val viewModel = newViewModel(repo = repo, cache = FakeCache())
        advanceUntilIdle()

        val state = viewModel.uiState as UnitReaderUiState.Error
        assertTrue(state.authExpired)
    }

    @Test
    fun `401 emits a single AuthExpired event (one-shot)`() = runTest(dispatcher) {
        val viewModel = newViewModel(
            repo = FakeRepo(getUnitError = PathApiException("expired", statusCode = 401)),
            cache = FakeCache()
        )
        advanceUntilIdle()

        val first = withTimeoutOrNull(1_000) { viewModel.events.first() }
        assertNotNull("expected one AuthExpired event", first)

        advanceUntilIdle()
        val second = withTimeoutOrNull(50) { viewModel.events.first() }
        assertNull("AuthExpired must not re-emit while state lingers", second)
    }

    private fun newViewModel(
        repo: PathRepository,
        cache: CompletionCache,
        unitId: String = "u-1"
    ): UnitReaderViewModel = UnitReaderViewModel(repo, cache, unitId)

    private fun sampleUnit(id: String): UnitDetail = UnitDetail(
        id = id,
        pathId = "llm-systems-for-pms",
        slug = "tokenization",
        position = 1,
        title = "Tokenization",
        definition = "def",
        tradeOffFraming = "framing",
        biteMd = "bite",
        depthMd = "depth",
        prereqUnitIds = emptyList(),
        status = "draft",
        sources = emptyList(),
        calibrationTags = emptyList(),
        decisionPrompt = null,
        rubric = null
    )
}

private class FakeRepo(
    private val unit: UnitDetail? = null,
    private val getUnitError: Throwable? = null,
    private val markCompleteError: Throwable? = null
) : PathRepository {
    var markCompleteCalls = 0
        private set

    override suspend fun getPath(pathId: String): Path = error("not used")

    override suspend fun getUnit(unitId: String): UnitDetail {
        getUnitError?.let { throw it }
        return unit ?: error("no unit stub set")
    }

    override suspend fun markComplete(unitId: String): CompletionRecord {
        markCompleteCalls++
        markCompleteError?.let { throw it }
        return CompletionRecord(1L, "u", "p", unitId, "now")
    }

    override suspend fun syncCompletedUnits() { /* no-op for these tests */ }
}

private class FakeCache(initial: Set<String> = emptySet()) : CompletionCache {
    private val store = initial.toMutableSet()
    override fun completedUnitIds(): Set<String> = store.toSet()
    override fun add(unitId: String) { store.add(unitId) }
    override fun replaceAll(unitIds: Set<String>) {
        store.clear()
        store.addAll(unitIds)
    }
    override fun clear() { store.clear() }
}
