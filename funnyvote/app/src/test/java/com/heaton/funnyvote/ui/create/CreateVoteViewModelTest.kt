package com.heaton.funnyvote.ui.create

import app.cash.turbine.test
import com.heaton.funnyvote.MainDispatcherRule
import com.heaton.funnyvote.data.repository.VoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateVoteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: VoteRepository = mockk(relaxed = true)
    private lateinit var viewModel: CreateVoteViewModel

    @Before
    fun setUp() {
        viewModel = CreateVoteViewModel(repository)
    }

    @Test
    fun `initial state has empty title and two options`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.title)
            assertEquals(2, state.options.size)
            assertFalse(state.isPrivate)
            assertFalse(state.isMultiChoice)
        }
    }

    @Test
    fun `submit with blank title sets titleError`() = runTest {
        viewModel.handleIntent(CreateVoteIntent.Submit)
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.titleError)
            assertEquals("投票標題不能為空！", state.titleError)
        }
        coVerify(exactly = 0) { repository.createNewVote(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `submit with valid fields calls repository and emits success effect`() = runTest {
        coEvery { repository.createNewVote(any(), any(), any(), any(), any()) } returns Result.success("new_vote_code")

        viewModel.handleIntent(CreateVoteIntent.UpdateTitle("新產品發表投票"))
        viewModel.handleIntent(CreateVoteIntent.UpdateOption(0, "方案 A"))
        viewModel.handleIntent(CreateVoteIntent.UpdateOption(1, "方案 B"))

        viewModel.uiEffect.test {
            viewModel.handleIntent(CreateVoteIntent.Submit)
            val snackbarEffect = awaitItem()
            assert(snackbarEffect is CreateVoteUiEffect.ShowSnackbar)
            val navEffect = awaitItem()
            assert(navEffect is CreateVoteUiEffect.NavigateToDetail)
            assertEquals("new_vote_code", (navEffect as CreateVoteUiEffect.NavigateToDetail).voteCode)
        }

        coVerify {
            repository.createNewVote(
                title = "新產品發表投票",
                options = listOf("方案 A", "方案 B"),
                isPrivate = false,
                password = null,
                isMultiChoice = false
            )
        }
    }

    @Test
    fun `private vote without password fails validation`() = runTest {
        viewModel.handleIntent(CreateVoteIntent.UpdateTitle("機密投票"))
        viewModel.handleIntent(CreateVoteIntent.UpdateOption(0, "A"))
        viewModel.handleIntent(CreateVoteIntent.UpdateOption(1, "B"))
        viewModel.handleIntent(CreateVoteIntent.TogglePrivate(true))

        viewModel.handleIntent(CreateVoteIntent.Submit)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("私密投票必須設定密碼！", state.passwordError)
        }
    }
}
