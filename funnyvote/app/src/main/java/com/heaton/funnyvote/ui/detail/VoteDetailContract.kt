package com.heaton.funnyvote.ui.detail

import com.heaton.funnyvote.data.local.entity.VoteWithDetails

data class VoteDetailUiState(
    val isLoading: Boolean = true,
    val voteWithDetails: VoteWithDetails? = null,
    val selectedOptionCodes: Set<String> = emptySet(),
    val isSubmitting: Boolean = false,
    val passwordInput: String = "",
    val isUnlocked: Boolean = false,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val showInfoDialog: Boolean = false,
    val showAddOptionDialog: Boolean = false,
    val newOptionInput: String = ""
)

sealed interface VoteDetailIntent {
    data class InitWithVoteCode(val code: String) : VoteDetailIntent
    data class SelectOption(val optionCode: String) : VoteDetailIntent
    data class UpdatePasswordInput(val input: String) : VoteDetailIntent
    data object UnlockWithPassword : VoteDetailIntent
    data object SubmitVote : VoteDetailIntent
    data object ToggleFavorite : VoteDetailIntent
    data class SetShowInfoDialog(val show: Boolean) : VoteDetailIntent
    data class SetShowAddOptionDialog(val show: Boolean) : VoteDetailIntent
    data class UpdateNewOptionInput(val input: String) : VoteDetailIntent
    data object SubmitNewOption : VoteDetailIntent
}

sealed interface VoteDetailUiEffect {
    data class ShowSnackbar(val message: String) : VoteDetailUiEffect
    data object NavigateBack : VoteDetailUiEffect
}
