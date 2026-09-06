package com.heaton.funnyvote.ui.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.heaton.funnyvote.MainDispatcherRule
import com.heaton.funnyvote.data.local.entity.OptionEntity
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import com.heaton.funnyvote.data.repository.VoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoteDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: VoteRepository = mockk(relaxed = true)
    private lateinit var viewModel: VoteDetailViewModel

    private val sampleVote = VoteWithDetails(
        vote = VoteEntity(
            voteCode = "vote_101",
            title = "測試單選題目",
            maxOption = 1,
            isNeedPassword = true,
            password = "secret_password"
        ),
        options = listOf(
            OptionEntity(voteCode = "vote_101", optionCode = "opt_1", title = "選項一", count = 5),
            OptionEntity(voteCode = "vote_101", optionCode = "opt_2", title = "選項二", count = 3)
        )
    )

    @Before
    fun setUp() {
        coEvery { repository.getVoteDetail("vote_101") } returns flowOf(sampleVote)
        val savedStateHandle = SavedStateHandle(mapOf("voteCode" to "vote_101"))
        viewModel = VoteDetailViewModel(repository, savedStateHandle)
    }

    @Test
    fun `password-protected vote is initially locked`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.voteWithDetails)
            assertFalse(state.isUnlocked)
        }
    }

    @Test
    fun `wrong password fails unlock`() = runTest {
        viewModel.handleIntent(VoteDetailIntent.UpdatePasswordInput("wrong"))
        viewModel.handleIntent(VoteDetailIntent.UnlockWithPassword)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isUnlocked)
            assertEquals("密碼錯誤，請重新輸入", state.passwordError)
        }
    }

    @Test
    fun `correct password unlocks vote`() = runTest {
        viewModel.handleIntent(VoteDetailIntent.UpdatePasswordInput("secret_password"))
        viewModel.handleIntent(VoteDetailIntent.UnlockWithPassword)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isUnlocked)
            assertNull(state.passwordError)
        }
    }

    @Test
    fun `single choice option selection replaces previous option`() = runTest {
        viewModel.handleIntent(VoteDetailIntent.SelectOption("opt_1"))
        viewModel.handleIntent(VoteDetailIntent.SelectOption("opt_2"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(setOf("opt_2"), state.selectedOptionCodes)
        }
    }

    @Test
    fun `submitting vote calls repository`() = runTest {
        coEvery { repository.submitVote(any(), any()) } returns Result.success(Unit)

        viewModel.handleIntent(VoteDetailIntent.SelectOption("opt_1"))
        viewModel.uiEffect.test {
            viewModel.handleIntent(VoteDetailIntent.SubmitVote)
            val effect = awaitItem()
            assert(effect is VoteDetailUiEffect.ShowSnackbar)
            assertEquals("投票成功！", (effect as VoteDetailUiEffect.ShowSnackbar).message)
        }

        coVerify { repository.submitVote("vote_101", listOf("opt_1")) }
    }

    @Test
    fun `cloud zero-trust password verification success unlocks vote`() = runTest {
        val cloudVote = sampleVote.copy(
            vote = sampleVote.vote.copy(voteCode = "vote_cloud", password = null)
        )
        coEvery { repository.getVoteDetail("vote_cloud") } returns flowOf(cloudVote)
        coEvery { repository.verifyPollPassword("vote_cloud", "cloud_secret") } returns true

        val vm = VoteDetailViewModel(repository, SavedStateHandle(mapOf("voteCode" to "vote_cloud")))
        vm.uiState.test {
            var state = awaitItem()
            while (state.voteWithDetails == null) {
                state = awaitItem()
            }
            assertFalse(state.isUnlocked)

            vm.handleIntent(VoteDetailIntent.UpdatePasswordInput("cloud_secret"))
            state = awaitItem()
            assertEquals("cloud_secret", state.passwordInput)

            vm.handleIntent(VoteDetailIntent.UnlockWithPassword)
            state = awaitItem()
            assertTrue(state.isUnlocked)
            assertNull(state.passwordError)
        }
    }

    @Test
    fun `cloud zero-trust password verification failure shows error`() = runTest {
        val cloudVote = sampleVote.copy(
            vote = sampleVote.vote.copy(voteCode = "vote_cloud", password = null)
        )
        coEvery { repository.getVoteDetail("vote_cloud") } returns flowOf(cloudVote)
        coEvery { repository.verifyPollPassword("vote_cloud", "wrong") } returns false

        val vm = VoteDetailViewModel(repository, SavedStateHandle(mapOf("voteCode" to "vote_cloud")))
        vm.uiState.test {
            var state = awaitItem()
            while (state.voteWithDetails == null) {
                state = awaitItem()
            }
            assertFalse(state.isUnlocked)

            vm.handleIntent(VoteDetailIntent.UpdatePasswordInput("wrong"))
            state = awaitItem()
            assertEquals("wrong", state.passwordInput)

            vm.handleIntent(VoteDetailIntent.UnlockWithPassword)
            state = awaitItem()
            assertFalse(state.isUnlocked)
            assertEquals("密碼錯誤，請重新輸入", state.passwordError)
        }
    }

    @Test
    fun `invalid voteCode format rejects query and displays error`() = runTest {
        val vm = VoteDetailViewModel(repository, SavedStateHandle(mapOf("voteCode" to "invalid!code@#$%^&*()")))
        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("無效的投票代碼格式", state.errorMessage)
        }
    }

    @Test
    fun `exceeding password attempts locks out user`() = runTest {
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.voteWithDetails == null) {
                state = awaitItem()
            }

            repeat(5) {
                viewModel.handleIntent(VoteDetailIntent.UpdatePasswordInput("wrong_pass"))
                state = awaitItem()
                viewModel.handleIntent(VoteDetailIntent.UnlockWithPassword)
                state = awaitItem()
            }

            assertTrue(state.isPasswordLockedOut)
            assertEquals(5, state.passwordFailedAttempts)
            assertTrue(state.passwordError?.contains("密碼錯誤已達上限") == true)
        }
    }
}
