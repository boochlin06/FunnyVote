package com.heaton.funnyvote.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.heaton.funnyvote.data.repository.VoteRepository
import com.heaton.funnyvote.ui.navigation.VoteDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoteDetailViewModel @Inject constructor(
    private val repository: VoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val voteCode: String = savedStateHandle.get<String>("voteCode")
        ?: runCatching { savedStateHandle.toRoute<VoteDetailRoute>().voteCode }.getOrDefault("vote_default")

    private val _uiState = MutableStateFlow(VoteDetailUiState())
    val uiState: StateFlow<VoteDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<VoteDetailUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<VoteDetailUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadVoteDetail()
    }

    fun handleIntent(intent: VoteDetailIntent) {
        when (intent) {
            is VoteDetailIntent.SelectOption -> {
                val vote = _uiState.value.voteWithDetails?.vote ?: return
                if (vote.isVoted) return // 已投票不能再選

                val currentSelected = _uiState.value.selectedOptionCodes.toMutableSet()
                if (vote.maxOption == 1) {
                    // 單選
                    currentSelected.clear()
                    currentSelected.add(intent.optionCode)
                } else {
                    // 複選
                    if (currentSelected.contains(intent.optionCode)) {
                        currentSelected.remove(intent.optionCode)
                    } else {
                        if (currentSelected.size < vote.maxOption) {
                            currentSelected.add(intent.optionCode)
                        } else {
                            viewModelScope.launch {
                                _uiEffect.send(VoteDetailUiEffect.ShowSnackbar("最多只能選擇 ${vote.maxOption} 個選項"))
                            }
                        }
                    }
                }
                _uiState.update { it.copy(selectedOptionCodes = currentSelected) }
            }

            is VoteDetailIntent.UpdatePasswordInput -> {
                _uiState.update { it.copy(passwordInput = intent.input, passwordError = null) }
            }

            is VoteDetailIntent.UnlockWithPassword -> {
                val vote = _uiState.value.voteWithDetails?.vote ?: return
                if (vote.password == _uiState.value.passwordInput) {
                    _uiState.update { it.copy(isUnlocked = true, passwordError = null) }
                    viewModelScope.launch {
                        _uiEffect.send(VoteDetailUiEffect.ShowSnackbar("解鎖成功！"))
                    }
                } else {
                    _uiState.update { it.copy(passwordError = "密碼錯誤，請重新輸入") }
                }
            }

            is VoteDetailIntent.SubmitVote -> {
                submitVote()
            }

            is VoteDetailIntent.ToggleFavorite -> {
                val vote = _uiState.value.voteWithDetails?.vote ?: return
                viewModelScope.launch {
                    repository.toggleFavorite(vote.voteCode, vote.isFavorite)
                    val msg = if (vote.isFavorite) "已取消收藏" else "已加入收藏"
                    _uiEffect.send(VoteDetailUiEffect.ShowSnackbar(msg))
                }
            }
        }
    }

    private fun loadVoteDetail() {
        viewModelScope.launch {
            repository.getVoteDetail(voteCode)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { details ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            voteWithDetails = details,
                            isUnlocked = details?.vote?.isNeedPassword != true || current.isUnlocked
                        )
                    }
                }
        }
    }

    private fun submitVote() {
        val selected = _uiState.value.selectedOptionCodes.toList()
        if (selected.isEmpty()) {
            viewModelScope.launch {
                _uiEffect.send(VoteDetailUiEffect.ShowSnackbar("請至少選擇一個選項！"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = repository.submitVote(voteCode, selected)
            _uiState.update { it.copy(isSubmitting = false) }

            result.onSuccess {
                _uiEffect.send(VoteDetailUiEffect.ShowSnackbar("投票成功！"))
            }.onFailure { e ->
                _uiEffect.send(VoteDetailUiEffect.ShowSnackbar("投票失敗：${e.message}"))
            }
        }
    }
}
