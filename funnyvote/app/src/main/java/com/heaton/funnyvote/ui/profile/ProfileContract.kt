package com.heaton.funnyvote.ui.profile

import com.heaton.funnyvote.data.local.entity.UserEntity

data class ProfileUiState(
    val user: UserEntity? = null,
    val totalCreatedVotes: Int = 0,
    val totalVotedCount: Int = 0,
    val totalFavoriteCount: Int = 0,
    val isEditingName: Boolean = false,
    val nameInput: String = ""
)

sealed interface ProfileIntent {
    data class EditName(val editing: Boolean) : ProfileIntent
    data class UpdateNameInput(val name: String) : ProfileIntent
    data object SaveName : ProfileIntent
}

sealed interface ProfileUiEffect {
    data class ShowSnackbar(val message: String) : ProfileUiEffect
}
