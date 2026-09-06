package com.heaton.funnyvote.ui.create

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heaton.funnyvote.data.repository.UserRepository
import com.heaton.funnyvote.data.repository.VoteRepository
import com.heaton.funnyvote.util.AnalyticsManager
import com.heaton.funnyvote.util.ImageUploadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateVoteViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VoteRepository,
    private val userRepository: UserRepository,
    private val imageUploadManager: ImageUploadManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateVoteUiState())
    val uiState: StateFlow<CreateVoteUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<CreateVoteUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<CreateVoteUiEffect> = _uiEffect.receiveAsFlow()

    init {
        analyticsManager.logScreenView("CreateVoteScreen")
        checkUserStatus()
    }

    private fun checkUserStatus() {
        viewModelScope.launch {
            val isAnon = userRepository.isAnonymous()
            _uiState.update { it.copy(isAnonymous = isAnon) }
        }
    }

    fun handleIntent(intent: CreateVoteIntent) {
        when (intent) {
            is CreateVoteIntent.UpdateTitle -> {
                _uiState.update { it.copy(title = intent.title, titleError = null) }
            }
            is CreateVoteIntent.UpdateDescription -> {
                _uiState.update { it.copy(description = intent.description) }
            }
            is CreateVoteIntent.SelectCoverImage -> {
                if (intent.uri != null && userRepository.isAnonymous()) {
                    viewModelScope.launch {
                        _uiEffect.send(CreateVoteUiEffect.ShowSnackbar("🔒 投票封面圖為 Google 認證會員專屬功能，請先登入或綁定帳號！"))
                    }
                } else {
                    _uiState.update { it.copy(coverUri = intent.uri) }
                }
            }
            is CreateVoteIntent.UpdateExpireDate -> {
                _uiState.update { it.copy(expireDateMillis = intent.expireDateMillis) }
            }
            is CreateVoteIntent.UpdateOption -> {
                val currentOptions = _uiState.value.options.toMutableList()
                if (intent.index in currentOptions.indices) {
                    currentOptions[intent.index] = intent.text
                    _uiState.update { it.copy(options = currentOptions, optionsError = null) }
                }
            }
            is CreateVoteIntent.AddOption -> {
                val currentOptions = _uiState.value.options.toMutableList()
                if (currentOptions.size < 10) {
                    currentOptions.add("")
                    _uiState.update { it.copy(options = currentOptions) }
                } else {
                    viewModelScope.launch {
                        _uiEffect.send(CreateVoteUiEffect.ShowSnackbar("最多只能建立 10 個選項"))
                    }
                }
            }
            is CreateVoteIntent.RemoveOption -> {
                val currentOptions = _uiState.value.options.toMutableList()
                if (currentOptions.size > 2 && intent.index in currentOptions.indices) {
                    currentOptions.removeAt(intent.index)
                    _uiState.update { it.copy(options = currentOptions) }
                }
            }
            is CreateVoteIntent.TogglePrivate -> {
                _uiState.update { it.copy(isPrivate = intent.isPrivate, passwordError = null) }
            }
            is CreateVoteIntent.UpdatePassword -> {
                _uiState.update { it.copy(password = intent.password, passwordError = null) }
            }
            is CreateVoteIntent.ToggleMultiChoice -> {
                _uiState.update { it.copy(isMultiChoice = intent.isMulti) }
            }
            is CreateVoteIntent.Submit -> {
                submitVote()
            }
        }
    }

    private fun submitVote() {
        val state = _uiState.value
        var hasError = false

        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "投票標題不能為空！") }
            hasError = true
        } else if (state.title.length > 100) {
            _uiState.update { it.copy(titleError = "標題長度不能超過 100 字！") }
            hasError = true
        }

        val validOptions = state.options.map { it.trim() }.filter { it.isNotEmpty() }
        if (validOptions.size < 2) {
            _uiState.update { it.copy(optionsError = "請至少填寫 2 個有效選項！") }
            hasError = true
        } else if (validOptions.any { it.length > 50 }) {
            _uiState.update { it.copy(optionsError = "單一選項長度不能超過 50 字！") }
            hasError = true
        }

        if (state.isPrivate) {
            if (state.password.isBlank()) {
                _uiState.update { it.copy(passwordError = "私密投票必須設定密碼！") }
                hasError = true
            } else if (state.password.length > 32) {
                _uiState.update { it.copy(passwordError = "密碼長度不能超過 32 字！") }
                hasError = true
            }
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            // 上傳封面圖 (若有選取)
            var uploadedCoverUrl: String? = null
            if (state.coverUri != null) {
                val tempPollId = "poll_${UUID.randomUUID().toString().replace("-", "").take(10)}"
                val uploadResult = imageUploadManager.compressAndUploadImage(
                    context = context,
                    uri = state.coverUri,
                    storagePath = "polls/$tempPollId/cover.jpg"
                )
                uploadedCoverUrl = uploadResult.getOrNull()
            }

            val result = repository.createNewVote(
                title = state.title.trim(),
                options = validOptions,
                isPrivate = state.isPrivate,
                password = if (state.isPrivate) state.password.trim() else null,
                isMultiChoice = state.isMultiChoice,
                description = state.description.trim().ifEmpty { null },
                imageUrl = uploadedCoverUrl,
                endTime = state.expireDateMillis
            )
            _uiState.update { it.copy(isSubmitting = false) }

            result.onSuccess { voteCode ->
                analyticsManager.logVoteCreate(voteCode, state.isPrivate)
                _uiEffect.send(CreateVoteUiEffect.ShowSnackbar("投票建立成功！"))
                _uiEffect.send(CreateVoteUiEffect.NavigateToDetail(voteCode))
            }.onFailure { e ->
                _uiEffect.send(CreateVoteUiEffect.ShowSnackbar("建立失敗：${e.message}"))
            }
        }
    }
}
