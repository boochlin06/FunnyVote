package com.heaton.funnyvote.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heaton.funnyvote.data.repository.VoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VoteRepository
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
                loadVotes()
            }
            is HomeIntent.UpdateSearchQuery -> {
                _uiState.update { it.copy(searchQuery = intent.query) }
                if (intent.query.isNotBlank()) {
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
            is HomeIntent.Refresh -> {
                loadVotes()
            }
        }
    }

    private fun loadVotes() {
        val currentTab = _uiState.value.selectedTab
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getVotesByCategory(currentTab)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, message = e.message) }
                }
                .collect { list ->
                    _uiState.update { it.copy(votes = list, isLoading = false) }
                }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
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
