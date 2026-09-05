package com.heaton.funnyvote.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heaton.funnyvote.data.repository.VoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateVoteViewModel @Inject constructor(
    private val repository: VoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateVoteUiState())
    val uiState: StateFlow<CreateVoteUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<CreateVoteUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<CreateVoteUiEffect> = _uiEffect.receiveAsFlow()

    fun handleIntent(intent: CreateVoteIntent) {
        when (intent) {
            is CreateVoteIntent.UpdateTitle -> {
                _uiState.update { it.copy(title = intent.title, titleError = null) }
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
        }

        val validOptions = state.options.map { it.trim() }.filter { it.isNotEmpty() }
        if (validOptions.size < 2) {
            _uiState.update { it.copy(optionsError = "請至少填寫 2 個有效選項！") }
            hasError = true
        }

        if (state.isPrivate && state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "私密投票必須設定密碼！") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = repository.createNewVote(
                title = state.title.trim(),
                options = validOptions,
                isPrivate = state.isPrivate,
                password = if (state.isPrivate) state.password.trim() else null,
                isMultiChoice = state.isMultiChoice
            )
            _uiState.update { it.copy(isSubmitting = false) }

            result.onSuccess { voteCode ->
                _uiEffect.send(CreateVoteUiEffect.ShowSnackbar("投票建立成功！"))
                _uiEffect.send(CreateVoteUiEffect.NavigateToDetail(voteCode))
            }.onFailure { e ->
                _uiEffect.send(CreateVoteUiEffect.ShowSnackbar("建立失敗：${e.message}"))
            }
        }
    }
}
