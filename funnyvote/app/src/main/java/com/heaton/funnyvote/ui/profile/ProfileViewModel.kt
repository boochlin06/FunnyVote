package com.heaton.funnyvote.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heaton.funnyvote.data.local.entity.UserEntity
import com.heaton.funnyvote.data.repository.UserRepository
import com.heaton.funnyvote.data.repository.VoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val voteRepository: VoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ProfileUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<ProfileUiEffect> = _uiEffect.receiveAsFlow()

    init {
        observeUserData()
        observeVoteStats()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userRepository.getUser().collect { user ->
                if (user == null) {
                    val defaultUser = UserEntity(
                        userId = "user_default",
                        userName = "熱血投票員",
                        email = "dev@funnyvote.org"
                    )
                    userRepository.saveUser(defaultUser)
                    _uiState.update { it.copy(user = defaultUser, nameInput = defaultUser.userName) }
                } else {
                    _uiState.update { it.copy(user = user, nameInput = user.userName) }
                }
            }
        }
    }

    private fun observeVoteStats() {
        viewModelScope.launch {
            voteRepository.getAllVotes().collect { allVotes ->
                val votedCount = allVotes.count { it.vote.isVoted }
                val favCount = allVotes.count { it.vote.isFavorite }
                val createdCount = allVotes.count { it.vote.authorName == _uiState.value.user?.userName }
                _uiState.update {
                    it.copy(
                        totalVotedCount = votedCount,
                        totalFavoriteCount = favCount,
                        totalCreatedVotes = createdCount
                    )
                }
            }
        }
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.EditName -> {
                _uiState.update { it.copy(isEditingName = intent.editing) }
            }
            is ProfileIntent.UpdateNameInput -> {
                _uiState.update { it.copy(nameInput = intent.name) }
            }
            is ProfileIntent.SaveName -> {
                val current = _uiState.value.user ?: return
                val newName = _uiState.value.nameInput.trim()
                if (newName.isNotEmpty()) {
                    viewModelScope.launch {
                        userRepository.saveUser(current.copy(userName = newName))
                        _uiState.update { it.copy(isEditingName = false) }
                        _uiEffect.send(ProfileUiEffect.ShowSnackbar("暱稱已成功更新為 $newName"))
                    }
                }
            }
        }
    }
}
