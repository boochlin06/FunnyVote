package com.heaton.funnyvote.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToTutorial: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HomeUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is HomeUiEffect.NavigateToDetail -> {
                    onNavigateToDetail(effect.voteCode)
                }
            }
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onVoteClick = onNavigateToDetail,
        onCreateClick = onNavigateToCreate,
        onProfileClick = onNavigateToProfile,
        onAboutClick = onNavigateToAbout,
        onTutorialClick = onNavigateToTutorial,
        snackbarHostState = snackbarHostState
    )
}
