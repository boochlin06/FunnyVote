package com.heaton.funnyvote.ui.main

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

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val hotVotes: List<VoteData>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val voteRepository: VoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        fetchHotVotes()
    }

    fun fetchHotVotes() {
        _uiState.update { MainUiState.Loading }
        viewModelScope.launch {
            voteRepository.getHotVotes().collect { result ->
                result.onSuccess { votes ->
                    _uiState.update { MainUiState.Success(votes) }
                }.onFailure { error ->
                    _uiState.update { MainUiState.Error(error.message ?: "Unknown Error") }
                }
            }
        }
    }
}
