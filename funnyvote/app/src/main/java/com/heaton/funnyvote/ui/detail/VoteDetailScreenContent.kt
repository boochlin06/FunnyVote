package com.heaton.funnyvote.ui.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.data.local.entity.OptionEntity
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteDetailScreenContent(
    uiState: VoteDetailUiState,
    onIntent: (VoteDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    val vote = uiState.voteWithDetails?.vote
    val options = uiState.voteWithDetails?.options ?: emptyList()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "投票詳情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (vote != null) {
                        IconButton(onClick = { onIntent(VoteDetailIntent.ToggleFavorite) }) {
                            Icon(
                                imageVector = if (vote.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "收藏",
                                tint = if (vote.isFavorite) Color.Red else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (vote != null && !vote.isVoted && uiState.isUnlocked) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { onIntent(VoteDetailIntent.SubmitVote) },
                            enabled = uiState.selectedOptionCodes.isNotEmpty() && !uiState.isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = "確認送出投票 (${uiState.selectedOptionCodes.size}/${vote.maxOption})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (vote == null) {
                Text(
                    text = uiState.errorMessage ?: "查無此投票資訊",
                    color = MaterialTheme.colorScheme.error
                )
            } else if (!uiState.isUnlocked) {
                // 密碼鎖定卡片
                PasswordLockCard(
                    passwordInput = uiState.passwordInput,
                    passwordError = uiState.passwordError,
                    onPasswordChange = { onIntent(VoteDetailIntent.UpdatePasswordInput(it)) },
                    onUnlock = { onIntent(VoteDetailIntent.UnlockWithPassword) }
                )
            } else {
                // 正常投票內容
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        VoteHeaderSection(vote = vote)
                    }

                    items(options, key = { it.optionCode }) { option ->
                        val isSelected = uiState.selectedOptionCodes.contains(option.optionCode)
                        val totalVotes = options.sumOf { it.count }.coerceAtLeast(1)
                        val ratio = option.count.toFloat() / totalVotes.toFloat()
                        val percentage = ratio * 100f

                        OptionCard(
                            option = option,
                            isVoted = vote.isVoted,
                            isSelected = isSelected,
                            percentage = percentage,
                            ratio = ratio,
                            isMultiChoice = vote.maxOption > 1,
                            onClick = {
                                onIntent(VoteDetailIntent.SelectOption(option.optionCode))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoteHeaderSection(vote: VoteEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (vote.maxOption > 1) "複選投票 (最多可選 ${vote.maxOption} 項)" else "單選投票"
                        )
                    }
                )
                if (vote.isVoted) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("你已完成投票", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = vote.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "發起人：${vote.authorName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "累積投票數：${vote.totalVotedCount} 票",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun OptionCard(
    option: OptionEntity,
    isVoted: Boolean,
    isSelected: Boolean,
    percentage: Float,
    ratio: Float,
    isMultiChoice: Boolean,
    onClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVoted) ratio else 0f,
        label = "progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isVoted, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(width = 2.dp) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isVoted) {
                        if (isMultiChoice) {
                            Checkbox(checked = isSelected, onCheckedChange = { onClick() })
                        } else {
                            RadioButton(selected = isSelected, onClick = { onClick() })
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    } else if (option.isUserChoiced) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "你的選擇",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = option.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected || option.isUserChoiced) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (isVoted) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%% (%d 票)", percentage, option.count),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isVoted) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (option.isUserChoiced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun PasswordLockCard(
    passwordInput: String,
    passwordError: String?,
    onPasswordChange: (String) -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "此為私密保護投票",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "請輸入密碼以解鎖檢視內容並參與投票",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = onPasswordChange,
                label = { Text("請輸入投票密碼") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onUnlock,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("解鎖投票")
            }
        }
    }
}

// ----------------- Previews -----------------

@Preview(showBackground = true)
@Composable
fun VoteDetailUnvotedPreview() {
    val sample = VoteWithDetails(
        vote = VoteEntity(
            voteCode = "v1",
            title = "今天中午要訂哪一家外送？",
            authorName = "同事A",
            maxOption = 1,
            totalVotedCount = 10
        ),
        options = listOf(
            OptionEntity(voteCode = "v1", optionCode = "o1", title = "大戶屋定食", count = 6),
            OptionEntity(voteCode = "v1", optionCode = "o2", title = "摩斯漢堡", count = 4)
        )
    )
    VoteDetailScreenContent(
        uiState = VoteDetailUiState(
            isLoading = false,
            voteWithDetails = sample,
            selectedOptionCodes = setOf("o1"),
            isUnlocked = true
        ),
        onIntent = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true)
@Composable
fun VoteDetailVotedPreview() {
    val sample = VoteWithDetails(
        vote = VoteEntity(
            voteCode = "v1",
            title = "今天中午要訂哪一家外送？",
            authorName = "同事A",
            maxOption = 1,
            isVoted = true,
            totalVotedCount = 10
        ),
        options = listOf(
            OptionEntity(voteCode = "v1", optionCode = "o1", title = "大戶屋定食", count = 6, isUserChoiced = true),
            OptionEntity(voteCode = "v1", optionCode = "o2", title = "摩斯漢堡", count = 4)
        )
    )
    VoteDetailScreenContent(
        uiState = VoteDetailUiState(
            isLoading = false,
            voteWithDetails = sample,
            isUnlocked = true
        ),
        onIntent = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true)
@Composable
fun VoteDetailLockedPreview() {
    val sample = VoteWithDetails(
        vote = VoteEntity(
            voteCode = "v2",
            title = "機密年度考核投票",
            authorName = "管理部",
            isNeedPassword = true
        ),
        options = emptyList()
    )
    VoteDetailScreenContent(
        uiState = VoteDetailUiState(
            isLoading = false,
            voteWithDetails = sample,
            isUnlocked = false
        ),
        onIntent = {},
        onNavigateBack = {}
    )
}
