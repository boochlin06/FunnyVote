package com.heaton.funnyvote.ui.create

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CreateVoteUiState(
    val title: String = "",
    val option1: String = "",
    val option2: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateVoteViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(CreateVoteUiState())
    val uiState: StateFlow<CreateVoteUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateOption1(option: String) {
        _uiState.update { it.copy(option1 = option) }
    }

    fun updateOption2(option: String) {
        _uiState.update { it.copy(option2 = option) }
    }

    fun submitVote() {
        // Mock implementation for creation
        _uiState.update { it.copy(isLoading = true) }
        // TODO: Call repository
    }
}
