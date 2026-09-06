package com.heaton.funnyvote.ui.create

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CreateVoteScreen(
    viewModel: CreateVoteViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String, Boolean) -> Unit = { _, _ -> onNavigateBack() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = uiState.title.isNotBlank() || uiState.options.any { it.isNotBlank() }

    BackHandler(enabled = hasUnsavedChanges) {
        showExitConfirmDialog = true
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("放棄建立投票？", fontWeight = FontWeight.Bold) },
            text = { Text("尚未儲存的內容將會遺失，確定要離開嗎？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("確定放棄", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("繼續編輯")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is CreateVoteUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is CreateVoteUiEffect.NavigateToDetail -> {
                    onNavigateToDetail(effect.voteCode, true)
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
        onNavigateBack = {
            if (hasUnsavedChanges) {
                showExitConfirmDialog = true
            } else {
                onNavigateBack()
            }
        },
        onSelectCoverClick = onSelectCoverClick,
        snackbarHostState = snackbarHostState
    )
}
