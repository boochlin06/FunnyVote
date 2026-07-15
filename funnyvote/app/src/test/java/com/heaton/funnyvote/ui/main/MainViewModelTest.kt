package com.heaton.funnyvote.ui.main

import com.heaton.funnyvote.data.local.entity.VoteData
import com.heaton.funnyvote.data.repository.VoteRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private val voteRepository: VoteRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading and transitions to Success when fetch completes`() = runTest {
        // Arrange
        val mockData = listOf(VoteData(voteCode = "testCode", title = "Test Title"))
        coEvery { voteRepository.getHotVotes() } returns flowOf(Result.success(mockData))

        // Act
        viewModel = MainViewModel(voteRepository)
        
        // Assert Loading State initially (since the flow is collected asynchronously)
        assertTrue(viewModel.uiState.value is MainUiState.Loading)

        // Advance coroutines to execute the collect block
        advanceUntilIdle()

        // Assert Success State
        val state = viewModel.uiState.value
        assertTrue(state is MainUiState.Success)
        assertEquals("testCode", (state as MainUiState.Success).hotVotes[0].voteCode)
    }

    @Test
    fun `transitions to Error state when fetch fails`() = runTest {
        // Arrange
        coEvery { voteRepository.getHotVotes() } returns flowOf(Result.failure(Exception("Network Timeout")))

        // Act
        viewModel = MainViewModel(voteRepository)
        advanceUntilIdle()

        // Assert Error State
        val state = viewModel.uiState.value
        assertTrue(state is MainUiState.Error)
        assertEquals("Network Timeout", (state as MainUiState.Error).message)
    }
}
