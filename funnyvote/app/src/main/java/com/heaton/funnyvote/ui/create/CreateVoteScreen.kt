package com.heaton.funnyvote.ui.create

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CreateVoteScreen(
    viewModel: CreateVoteViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is CreateVoteUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is CreateVoteUiEffect.NavigateToDetail -> {
                    onNavigateBack() // 返回首頁以查看新建立的投票
                }
            }
        }
    }

    val coverPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.handleIntent(CreateVoteIntent.SelectCoverImage(uri))
    }

    val onSelectCoverClick = {
        if (uiState.isAnonymous) {
            viewModel.handleIntent(CreateVoteIntent.SelectCoverImage(android.net.Uri.EMPTY))
        } else {
            coverPickerLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }

    CreateVoteScreenContent(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onNavigateBack = onNavigateBack,
        onSelectCoverClick = onSelectCoverClick,
        snackbarHostState = snackbarHostState
    )
}
