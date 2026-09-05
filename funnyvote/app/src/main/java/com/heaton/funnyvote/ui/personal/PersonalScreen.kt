package com.heaton.funnyvote.ui.personal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PersonalScreen(
    viewModel: PersonalViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onVoteClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PersonalScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onVoteClick = onVoteClick
    )
}
