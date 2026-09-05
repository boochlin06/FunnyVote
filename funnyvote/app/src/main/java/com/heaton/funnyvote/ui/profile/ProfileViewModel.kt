package com.heaton.funnyvote.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heaton.funnyvote.data.local.entity.UserEntity
import com.heaton.funnyvote.data.repository.UserRepository
import com.heaton.funnyvote.data.repository.VoteRepository
import com.heaton.funnyvote.util.AnalyticsManager
import com.heaton.funnyvote.util.ImageUploadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val voteRepository: VoteRepository,
    private val imageUploadManager: ImageUploadManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ProfileUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<ProfileUiEffect> = _uiEffect.receiveAsFlow()

    init {
        analyticsManager.logScreenView("ProfileScreen")
        observeUserData()
        observeVoteStats()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userRepository.getUser().collect { user ->
                val isAnon = userRepository.isAnonymous()
                if (user == null) {
                    val defaultUser = UserEntity(
                        userId = "user_default",
                        userName = "熱血投票員",
                        email = "dev@funnyvote.org"
                    )
                    userRepository.saveUser(defaultUser)
                    _uiState.update { it.copy(user = defaultUser, nameInput = defaultUser.userName, isAnonymous = isAnon) }
                } else {
                    _uiState.update { it.copy(user = user, nameInput = user.userName, isAnonymous = isAnon) }
                }
            }
        }
    }

    private fun observeVoteStats() {
        viewModelScope.launch {
            val uid = userRepository.ensureAuthenticated()
            launch {
                voteRepository.getUserParticipatedVotes(uid).collect { participated ->
                    _uiState.update {
                        it.copy(
                            participatedVotes = participated,
                            totalVotedCount = if (participated.isNotEmpty()) participated.size else it.totalVotedCount
                        )
                    }
                }
            }
            launch {
                voteRepository.getAllVotes().collect { allVotes ->
                    val currentUserName = _uiState.value.user?.userName ?: "熱血投票員"
                    val votedCount = allVotes.count { it.vote.isVoted }
                    val favVotes = allVotes.filter { it.vote.isFavorite }
                    val createdVotes = allVotes.filter { it.vote.authorName == currentUserName || it.vote.authorName == "熱血投票員" || it.vote.authorName == "Heaton Lin" }

                    _uiState.update {
                        it.copy(
                            totalVotedCount = if (it.participatedVotes.isNotEmpty()) it.participatedVotes.size else votedCount,
                            totalFavoriteCount = favVotes.size,
                            totalCreatedVotes = createdVotes.size,
                            createdVotes = createdVotes,
                            favoriteVotes = favVotes
                        )
                    }
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
            is ProfileIntent.SelectTab -> {
                _uiState.update { it.copy(selectedTabIndex = intent.index) }
            }
            is ProfileIntent.ToggleFavorite -> {
                viewModelScope.launch {
                    voteRepository.toggleFavorite(intent.voteCode)
                }
            }
            is ProfileIntent.SignInWithGoogle -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isGoogleSigningIn = true) }
                    val result = userRepository.linkOrSignInWithGoogle(intent.idToken)
                    result.onSuccess { user ->
                        _uiState.update {
                            it.copy(
                                isGoogleSigningIn = false,
                                user = user,
                                isAnonymous = false
                            )
                        }
                        _uiEffect.send(ProfileUiEffect.ShowSnackbar("已成功連結 Google 帳號：${user.userName}"))
                    }.onFailure { err ->
                        _uiState.update { it.copy(isGoogleSigningIn = false) }
                        _uiEffect.send(ProfileUiEffect.ShowSnackbar("Google 登入失敗：${err.message ?: "未知錯誤"}"))
                    }
                }
            }
            is ProfileIntent.SignOut -> {
                viewModelScope.launch {
                    userRepository.signOut()
                    _uiState.update { it.copy(isAnonymous = true) }
                    _uiEffect.send(ProfileUiEffect.ShowSnackbar("已登出帳號"))
                }
            }
            is ProfileIntent.UploadAvatar -> {
                viewModelScope.launch {
                    val current = _uiState.value.user ?: return@launch
                    _uiState.update { it.copy(isUploadingAvatar = true) }
                    val path = "users/${current.userId}/avatar.jpg"
                    val result = imageUploadManager.compressAndUploadImage(context, intent.uri, path)
                    result.onSuccess { downloadUrl ->
                        userRepository.saveUser(current.copy(userIcon = downloadUrl))
                        _uiState.update { it.copy(isUploadingAvatar = false) }
                        _uiEffect.send(ProfileUiEffect.ShowSnackbar("個人頭像更新成功！"))
                    }.onFailure { err ->
                        _uiState.update { it.copy(isUploadingAvatar = false) }
                        _uiEffect.send(ProfileUiEffect.ShowSnackbar("頭像上傳失敗：${err.message ?: "未知錯誤"}"))
                    }
                }
            }
        }
    }
}
