package com.heaton.funnyvote.ui.create

data class CreateVoteUiState(
    val title: String = "",
    val options: List<String> = listOf("", ""),
    val isPrivate: Boolean = false,
    val password: String = "",
    val isMultiChoice: Boolean = false,
    val isSubmitting: Boolean = false,
    val titleError: String? = null,
    val optionsError: String? = null,
    val passwordError: String? = null
)

sealed interface CreateVoteIntent {
    data class UpdateTitle(val title: String) : CreateVoteIntent
    data class UpdateOption(val index: Int, val text: String) : CreateVoteIntent
    data object AddOption : CreateVoteIntent
    data class RemoveOption(val index: Int) : CreateVoteIntent
    data class TogglePrivate(val isPrivate: Boolean) : CreateVoteIntent
    data class UpdatePassword(val password: String) : CreateVoteIntent
    data class ToggleMultiChoice(val isMulti: Boolean) : CreateVoteIntent
    data object Submit : CreateVoteIntent
}

sealed interface CreateVoteUiEffect {
    data class ShowSnackbar(val message: String) : CreateVoteUiEffect
    data class NavigateToDetail(val voteCode: String) : CreateVoteUiEffect
}
