package com.heaton.funnyvote.ui.home

import com.heaton.funnyvote.data.local.entity.VoteWithDetails

data class HomeUiState(
    val selectedTab: String = "hot", // "hot", "new", "favorite"
    val votes: List<VoteWithDetails> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val message: String? = null
)

sealed interface HomeIntent {
    data class SelectTab(val tab: String) : HomeIntent
    data class UpdateSearchQuery(val query: String) : HomeIntent
    data class ToggleSearch(val active: Boolean) : HomeIntent
    data class ToggleFavorite(val voteCode: String, val currentFavorite: Boolean) : HomeIntent
    data class QuickVote(val voteCode: String, val optionCode: String) : HomeIntent
    data object Refresh : HomeIntent
    data object LoadMore : HomeIntent
}

sealed interface HomeUiEffect {
    data class ShowSnackbar(val message: String) : HomeUiEffect
    data class NavigateToDetail(val voteCode: String) : HomeUiEffect
}
