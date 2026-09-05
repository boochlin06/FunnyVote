package com.heaton.funnyvote.ui.profile

import com.heaton.funnyvote.data.local.entity.UserEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails

data class ProfileUiState(
    val user: UserEntity? = null,
    val totalCreatedVotes: Int = 0,
    val totalVotedCount: Int = 0,
    val totalFavoriteCount: Int = 0,
    val isEditingName: Boolean = false,
    val nameInput: String = "",
    val selectedTabIndex: Int = 0, // 0: 發起投票 (Created), 1: 我的收藏 (Favorite)
    val createdVotes: List<VoteWithDetails> = emptyList(),
    val favoriteVotes: List<VoteWithDetails> = emptyList()
)

sealed interface ProfileIntent {
    data class EditName(val editing: Boolean) : ProfileIntent
    data class UpdateNameInput(val name: String) : ProfileIntent
    data object SaveName : ProfileIntent
    data class SelectTab(val index: Int) : ProfileIntent
    data class ToggleFavorite(val voteCode: String) : ProfileIntent
}

sealed interface ProfileUiEffect {
    data class ShowSnackbar(val message: String) : ProfileUiEffect
}
