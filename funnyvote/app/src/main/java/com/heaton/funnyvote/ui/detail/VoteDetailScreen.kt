package com.heaton.funnyvote.ui.detail

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
import kotlinx.coroutines.delay

@Composable
fun VoteDetailScreen(
    voteCode: String,
    autoShare: Boolean = false,
    viewModel: VoteDetailViewModel = hiltViewModel(key = voteCode),
    onNavigateBack: () -> Unit,
    onNavigateToAuthor: (String, String, String?) -> Unit = { _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    val isSelectingAndUnsubmitted = uiState.selectedOptionCodes.isNotEmpty() && uiState.voteWithDetails?.vote?.isVoted != true

    BackHandler(enabled = isSelectingAndUnsubmitted) {
        showExitConfirmDialog = true
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("離開此頁面？", fontWeight = FontWeight.Bold) },
            text = { Text("您尚未送出投票，若離開將不會記錄您的投票選擇。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("確定離開", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("繼續投票")
                }
            }
        )
    }

    LaunchedEffect(voteCode) {
        viewModel.handleIntent(VoteDetailIntent.InitWithVoteCode(voteCode))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is VoteDetailUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is VoteDetailUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    VoteDetailScreenContent(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onNavigateBack = {
            if (isSelectingAndUnsubmitted) {
                showExitConfirmDialog = true
            } else {
                onNavigateBack()
            }
        },
        onAuthorClick = onNavigateToAuthor,
        autoShare = autoShare,
        snackbarHostState = snackbarHostState
    )
}
