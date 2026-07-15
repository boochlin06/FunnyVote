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

sealed class CreateVoteIntent {
    data class UpdateTitle(val title: String) : CreateVoteIntent()
    data class UpdateOption1(val option: String) : CreateVoteIntent()
    data class UpdateOption2(val option: String) : CreateVoteIntent()
    object SubmitVote : CreateVoteIntent()
}

@HiltViewModel
class CreateVoteViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(CreateVoteUiState())
    val uiState: StateFlow<CreateVoteUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: CreateVoteIntent) {
        when (intent) {
            is CreateVoteIntent.UpdateTitle -> updateTitle(intent.title)
            is CreateVoteIntent.UpdateOption1 -> updateOption1(intent.option)
            is CreateVoteIntent.UpdateOption2 -> updateOption2(intent.option)
            is CreateVoteIntent.SubmitVote -> submitVote()
        }
    }

    private fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    private fun updateOption1(option: String) {
        _uiState.update { it.copy(option1 = option) }
    }

    private fun updateOption2(option: String) {
        _uiState.update { it.copy(option2 = option) }
    }

    private fun submitVote() {
        // Mock implementation for creation
        _uiState.update { it.copy(isLoading = true) }
        // TODO: Call repository
    }
}
