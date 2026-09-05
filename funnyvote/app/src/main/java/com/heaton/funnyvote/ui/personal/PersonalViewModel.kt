package com.heaton.funnyvote.ui.personal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.heaton.funnyvote.data.repository.VoteRepository
import com.heaton.funnyvote.ui.navigation.PersonalRoute
import com.heaton.funnyvote.util.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val voteRepository: VoteRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val authorId: String = savedStateHandle.get<String>("authorId")
        ?: runCatching { savedStateHandle.toRoute<PersonalRoute>().authorId }.getOrDefault("")
    private val authorName: String = savedStateHandle.get<String>("authorName")
        ?: runCatching { savedStateHandle.toRoute<PersonalRoute>().authorName }.getOrDefault("FunnyVote 使用者")
    private val authorIcon: String? = savedStateHandle.get<String>("authorIcon")
        ?: runCatching { savedStateHandle.toRoute<PersonalRoute>().authorIcon }.getOrNull()

    private val _uiState = MutableStateFlow(
        PersonalUiState(
            authorId = authorId,
            authorName = authorName,
            authorIcon = authorIcon
        )
    )
    val uiState: StateFlow<PersonalUiState> = _uiState.asStateFlow()

    init {
        analyticsManager.logScreenView("PersonalScreen_$authorId")
        loadVotes(authorId)
    }

    fun handleIntent(intent: PersonalIntent) {
        when (intent) {
            is PersonalIntent.LoadAuthorVotes -> loadVotes(intent.authorId)
        }
    }

    private fun loadVotes(authorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            voteRepository.getVotesByAuthor(authorId).collect { list ->
                _uiState.update { it.copy(votes = list, isLoading = false) }
            }
        }
    }
}
