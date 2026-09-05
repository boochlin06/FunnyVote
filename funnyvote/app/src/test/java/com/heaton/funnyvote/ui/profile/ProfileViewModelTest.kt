package com.heaton.funnyvote.ui.profile

import app.cash.turbine.test
import com.heaton.funnyvote.MainDispatcherRule
import com.heaton.funnyvote.data.local.entity.UserEntity
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import com.heaton.funnyvote.data.repository.UserRepository
import com.heaton.funnyvote.data.repository.VoteRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: android.content.Context = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val voteRepository: VoteRepository = mockk(relaxed = true)
    private val imageUploadManager: com.heaton.funnyvote.util.ImageUploadManager = mockk(relaxed = true)
    private val analyticsManager: com.heaton.funnyvote.util.AnalyticsManager = mockk(relaxed = true)
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        val testUser = UserEntity(
            userId = "user_123",
            userName = "測試玩家",
            email = "test@funnyvote.org"
        )
        val testVotes = listOf(
            VoteWithDetails(
                vote = VoteEntity(voteCode = "p1", title = "參與投票1", isVoted = true),
                options = emptyList()
            )
        )
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { userRepository.isAnonymous() } returns true
        coEvery { userRepository.ensureAuthenticated() } returns "user_123"
        coEvery { voteRepository.getAllVotes() } returns flowOf(emptyList())
        coEvery { voteRepository.getUserParticipatedVotes("user_123") } returns flowOf(testVotes)

        viewModel = ProfileViewModel(context, userRepository, voteRepository, imageUploadManager, analyticsManager)
    }

    @Test
    fun `initial state loads user data and anonymous flag correctly`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("測試玩家", state.user?.userName)
            assertTrue(state.isAnonymous)
        }
    }

    @Test
    fun `intent SelectTab updates selectedTabIndex for 3 tabs`() = runTest {
        viewModel.handleIntent(ProfileIntent.SelectTab(2))
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.selectedTabIndex)
        }
    }

    @Test
    fun `intent SignInWithGoogle calls repository and updates state`() = runTest {
        val googleUser = UserEntity(
            userId = "user_123",
            userName = "Google 會員",
            email = "google@funnyvote.org"
        )
        coEvery { userRepository.linkOrSignInWithGoogle("fake_id_token") } returns Result.success(googleUser)

        viewModel.handleIntent(ProfileIntent.SignInWithGoogle("fake_id_token"))
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Google 會員", state.user?.userName)
            assertEquals(false, state.isAnonymous)
        }
    }
}
