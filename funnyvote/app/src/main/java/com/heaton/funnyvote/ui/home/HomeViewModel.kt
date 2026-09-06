package com.heaton.funnyvote.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heaton.funnyvote.data.repository.VoteRepository
import com.heaton.funnyvote.util.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VoteRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<HomeUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<HomeUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadVotes()
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SelectTab -> {
                _uiState.update { it.copy(selectedTab = intent.tab) }
                analyticsManager.logTabSelect(intent.tab)
                loadVotes()
            }
            is HomeIntent.UpdateSearchQuery -> {
                _uiState.update { it.copy(searchQuery = intent.query) }
                if (intent.query.isNotBlank()) {
                    analyticsManager.logSearch(intent.query, 0)
                    performSearch(intent.query)
                } else {
                    loadVotes()
                }
            }
            is HomeIntent.ToggleSearch -> {
                _uiState.update { it.copy(isSearchActive = intent.active, searchQuery = if (!intent.active) "" else it.searchQuery) }
                if (!intent.active) {
                    loadVotes()
                }
            }
            is HomeIntent.ToggleFavorite -> {
                viewModelScope.launch {
                    repository.toggleFavorite(intent.voteCode, intent.currentFavorite)
                    val msg = if (intent.currentFavorite) "已取消收藏" else "已加入收藏"
                    _uiEffect.send(HomeUiEffect.ShowSnackbar(msg))
                }
            }
            is HomeIntent.QuickVote -> {
                viewModelScope.launch {
                    val result = repository.submitVote(intent.voteCode, listOf(intent.optionCode))
                    result.onSuccess {
                        analyticsManager.logQuickVote(intent.voteCode, intent.optionCode)
                        _uiEffect.send(HomeUiEffect.ShowSnackbar("已快速完成投票！"))
                        loadVotes()
                    }.onFailure { e ->
                        _uiEffect.send(HomeUiEffect.ShowSnackbar("投票失敗：${e.message ?: "未知錯誤"}"))
                    }
                }
            }
            is HomeIntent.Refresh -> {
                loadVotes()
            }
            is HomeIntent.LoadMore -> {
                loadMoreVotes()
            }
        }
    }

    private var loadVotesJob: kotlinx.coroutines.Job? = null
    private var searchJob: kotlinx.coroutines.Job? = null
    private var loadMoreJob: kotlinx.coroutines.Job? = null

    private fun loadVotes() {
        val currentTab = _uiState.value.selectedTab
        searchJob?.cancel()
        loadVotesJob?.cancel()
        loadVotesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasMore = true, isLoadingMore = false) }
            repository.getVotesByCategory(currentTab)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, message = e.message) }
                }
                .collect { list ->
                    _uiState.update { it.copy(votes = list, isLoading = false) }
                }
        }
    }

    private fun loadMoreVotes() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore || state.isSearchActive) return
        val lastVote = state.votes.lastOrNull() ?: return

        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val result = repository.loadMoreVotes(state.selectedTab, lastVote.vote.voteCode, 20)
            result.onSuccess { newVotes ->
                if (newVotes.isEmpty()) {
                    _uiState.update { it.copy(isLoadingMore = false, hasMore = false) }
                } else {
                    val existingCodes = state.votes.map { it.vote.voteCode }.toSet()
                    val filtered = newVotes.filter { !existingCodes.contains(it.vote.voteCode) }
                    _uiState.update {
                        it.copy(
                            votes = it.votes + filtered,
                            isLoadingMore = false,
                            hasMore = filtered.isNotEmpty()
                        )
                    }
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    private fun performSearch(query: String) {
        loadVotesJob?.cancel()
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            repository.searchVotes(query)
                .catch { e ->
                    _uiState.update { it.copy(message = e.message) }
                }
                .collect { list ->
                    _uiState.update { it.copy(votes = list) }
                }
        }
    }
}
