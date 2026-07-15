package com.heaton.funnyvote.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import com.heaton.funnyvote.data.local.entity.VoteData

@Composable
fun VoteDetailScreen(
    voteCode: String,
    viewModel: VoteDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    VoteDetailScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRefresh = { viewModel.handleIntent(VoteDetailIntent.RefreshVoteDetail) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteDetailScreenContent(
    uiState: VoteDetailUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vote Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is VoteDetailUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is VoteDetailUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = state.vote.title ?: "Untitled",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vote Code: ${state.vote.voteCode}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Author: ${state.vote.authorName ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is VoteDetailUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRefresh) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VoteDetailScreenLoadingPreview() {
    VoteDetailScreenContent(
        uiState = VoteDetailUiState.Loading,
        onNavigateBack = {},
        onRefresh = {}
    )
}

@Preview(showBackground = true)
@Composable
fun VoteDetailScreenSuccessPreview() {
    val mockData = VoteData(voteCode = "123", title = "Favorite Programming Language?", authorName = "John Doe")
    VoteDetailScreenContent(
        uiState = VoteDetailUiState.Success(mockData),
        onNavigateBack = {},
        onRefresh = {}
    )
}

@Preview(showBackground = true)
@Composable
fun VoteDetailScreenErrorPreview() {
    VoteDetailScreenContent(
        uiState = VoteDetailUiState.Error("Failed to fetch vote details"),
        onNavigateBack = {},
        onRefresh = {}
    )
}
