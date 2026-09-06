package com.heaton.funnyvote.ui.home

import app.cash.turbine.test
import com.heaton.funnyvote.MainDispatcherRule
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import com.heaton.funnyvote.data.repository.VoteRepository
import com.heaton.funnyvote.util.AnalyticsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: VoteRepository = mockk(relaxed = true)
    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        val testVotes = listOf(
            VoteWithDetails(
                vote = VoteEntity(voteCode = "code1", title = "測試投票1", isFavorite = false),
                options = emptyList()
            )
        )
        coEvery { repository.getVotesByCategory(any()) } returns flowOf(testVotes)
        viewModel = HomeViewModel(repository, analyticsManager)
    }

    @Test
    fun `initial state loads votes successfully`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("hot", state.selectedTab)
            assertEquals(1, state.votes.size)
            assertEquals("測試投票1", state.votes.first().vote.title)
        }
    }

    @Test
    fun `intent SelectTab updates selectedTab and reloads votes`() = runTest {
        viewModel.handleIntent(HomeIntent.SelectTab("new"))
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("new", state.selectedTab)
        }
    }

    @Test
    fun `intent ToggleFavorite calls repository and emits effect`() = runTest {
        viewModel.uiEffect.test {
            viewModel.handleIntent(HomeIntent.ToggleFavorite("code1", false))
            val effect = awaitItem()
            assert(effect is HomeUiEffect.ShowSnackbar)
            assertEquals("已加入收藏", (effect as HomeUiEffect.ShowSnackbar).message)
        }
        coVerify { repository.toggleFavorite("code1", false) }
    }

    @Test
    fun `intent LoadMore appends new votes to list`() = runTest {
        val nextVotes = listOf(
            VoteWithDetails(
                vote = VoteEntity(voteCode = "code2", title = "分頁投票2", isFavorite = false),
                options = emptyList()
            )
        )
        coEvery { repository.loadMoreVotes("hot", "code1", 20) } returns Result.success(nextVotes)

        viewModel.handleIntent(HomeIntent.LoadMore)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.votes.size)
            assertEquals("code2", state.votes[1].vote.voteCode)
        }
    }

    @Test
    fun `intent QuickVote calls repository submitVote and logs analytics`() = runTest {
        coEvery { repository.submitVote(any(), any()) } returns Result.success(Unit)

        viewModel.uiEffect.test {
            viewModel.handleIntent(HomeIntent.QuickVote("code1", "opt1"))
            val effect = awaitItem()
            assert(effect is HomeUiEffect.ShowSnackbar)
            assertEquals("已快速完成投票！", (effect as HomeUiEffect.ShowSnackbar).message)
        }
        coVerify { repository.submitVote("code1", listOf("opt1")) }
        io.mockk.verify { analyticsManager.logQuickVote("code1", "opt1") }
    }
}
