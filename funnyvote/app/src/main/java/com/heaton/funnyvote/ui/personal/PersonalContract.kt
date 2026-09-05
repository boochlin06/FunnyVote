package com.heaton.funnyvote.ui.personal

import com.heaton.funnyvote.data.local.entity.VoteWithDetails

data class PersonalUiState(
    val authorId: String = "",
    val authorName: String = "",
    val authorIcon: String? = null,
    val votes: List<VoteWithDetails> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface PersonalIntent {
    data class LoadAuthorVotes(val authorId: String) : PersonalIntent
}

sealed interface PersonalUiEffect {
    data class ShowSnackbar(val message: String) : PersonalUiEffect
}
