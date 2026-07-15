package com.heaton.funnyvote.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.heaton.funnyvote.data.local.entity.VoteData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FunnyVote") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Text("Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is MainUiState.Success -> {
                    VoteList(
                        votes = state.hotVotes,
                        onVoteClick = { vote ->
                            onNavigateToDetail(vote.voteCode)
                        }
                    )
                }
                is MainUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchHotVotes() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoteList(votes: List<VoteData>, onVoteClick: (VoteData) -> Unit) {
    if (votes.isEmpty()) {
        Text("No votes available.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(votes) { vote ->
                VoteItemCard(vote = vote, onClick = { onVoteClick(vote) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteItemCard(vote: VoteData, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = vote.title ?: "Untitled", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Author: ${vote.authorName ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
