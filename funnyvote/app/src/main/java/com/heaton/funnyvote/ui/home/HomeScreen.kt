package com.heaton.funnyvote.ui.home

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToTutorial: () -> Unit,
    onNavigateToAuthor: (String, String, String?) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000L) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "再按一次退出 FunnyVote", Toast.LENGTH_SHORT).show()
        }
    }

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
        onAuthorClick = onNavigateToAuthor,
        snackbarHostState = snackbarHostState
    )
}
