package com.heaton.funnyvote.ui.personal

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.heaton.funnyvote.MainDispatcherRule
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import com.heaton.funnyvote.data.repository.VoteRepository
import com.heaton.funnyvote.util.AnalyticsManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val voteRepository: VoteRepository = mockk(relaxed = true)
    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)
    private val savedStateHandle = SavedStateHandle(
        mapOf(
            "authorId" to "author_999",
            "authorName" to "賈伯斯",
            "authorIcon" to null
        )
    )
    private lateinit var viewModel: PersonalViewModel

    @Before
    fun setUp() {
        val testVotes = listOf(
            VoteWithDetails(
                vote = VoteEntity(voteCode = "p1", title = "賈伯斯之投票", authorId = "author_999"),
                options = emptyList()
            )
        )
        coEvery { voteRepository.getVotesByAuthor("author_999") } returns flowOf(testVotes)

        viewModel = PersonalViewModel(savedStateHandle, voteRepository, analyticsManager)
    }

    @Test
    fun `initial state initializes with author info and loads votes`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("author_999", state.authorId)
            assertEquals("賈伯斯", state.authorName)
            assertEquals(1, state.votes.size)
            assertEquals("賈伯斯之投票", state.votes.first().vote.title)
        }
    }
}
