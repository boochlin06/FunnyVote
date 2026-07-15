package com.heaton.funnyvote.ui.create

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

@Composable
fun CreateVoteScreen(
    viewModel: CreateVoteViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CreateVoteScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onIntent = viewModel::handleIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoteScreenContent(
    uiState: CreateVoteUiState,
    onNavigateBack: () -> Unit,
    onIntent: (CreateVoteIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Vote") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { onIntent(CreateVoteIntent.UpdateTitle(it)) },
                label = { Text("Vote Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.option1,
                onValueChange = { onIntent(CreateVoteIntent.UpdateOption1(it)) },
                label = { Text("Option 1") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.option2,
                onValueChange = { onIntent(CreateVoteIntent.UpdateOption2(it)) },
                label = { Text("Option 2") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onIntent(CreateVoteIntent.SubmitVote) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Vote")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateVoteScreenDefaultPreview() {
    CreateVoteScreenContent(
        uiState = CreateVoteUiState(),
        onNavigateBack = {},
        onIntent = {}
    )
}

@Preview(showBackground = true)
@Composable
fun CreateVoteScreenFilledPreview() {
    CreateVoteScreenContent(
        uiState = CreateVoteUiState(
            title = "What's for dinner?",
            option1 = "Pizza",
            option2 = "Burger"
        ),
        onNavigateBack = {},
        onIntent = {}
    )
}

@Preview(showBackground = true)
@Composable
fun CreateVoteScreenLoadingPreview() {
    CreateVoteScreenContent(
        uiState = CreateVoteUiState(isLoading = true),
        onNavigateBack = {},
        onIntent = {}
    )
}
