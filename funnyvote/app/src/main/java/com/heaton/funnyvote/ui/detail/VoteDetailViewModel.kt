package com.heaton.funnyvote.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heaton.funnyvote.data.local.entity.VoteData
import com.heaton.funnyvote.data.repository.VoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VoteDetailUiState {
    object Loading : VoteDetailUiState()
    data class Success(val vote: VoteData) : VoteDetailUiState()
    data class Error(val message: String) : VoteDetailUiState()
}

@HiltViewModel
class VoteDetailViewModel @Inject constructor(
    private val voteRepository: VoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val voteCode: String = checkNotNull(savedStateHandle["voteCode"])

    private val _uiState = MutableStateFlow<VoteDetailUiState>(VoteDetailUiState.Loading)
    val uiState: StateFlow<VoteDetailUiState> = _uiState.asStateFlow()

    init {
        fetchVoteDetail()
    }

    fun fetchVoteDetail() {
        _uiState.update { VoteDetailUiState.Loading }
        viewModelScope.launch {
            val result = voteRepository.getVoteDetail(voteCode)
            result.onSuccess { vote ->
                _uiState.update { VoteDetailUiState.Success(vote) }
            }.onFailure { error ->
                _uiState.update { VoteDetailUiState.Error(error.message ?: "Unknown error") }
            }
        }
    }
}
