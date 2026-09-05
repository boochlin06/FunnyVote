package com.heaton.funnyvote.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.data.local.entity.OptionEntity
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onVoteClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isSearchActive) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = { onIntent(HomeIntent.UpdateSearchQuery(it)) },
                            placeholder = { Text("搜尋投票標題...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "FunnyVote 趣味投票",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onIntent(HomeIntent.ToggleSearch(!uiState.isSearchActive))
                        }
                    ) {
                        Icon(
                            imageVector = if (uiState.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "搜尋"
                        )
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "個人檔案"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("發起投票") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 分頁切換
            TabRow(
                selectedTabIndex = when (uiState.selectedTab) {
                    "new" -> 1
                    "favorite" -> 2
                    else -> 0
                }
            ) {
                Tab(
                    selected = uiState.selectedTab == "hot",
                    onClick = { onIntent(HomeIntent.SelectTab("hot")) },
                    text = { Text("熱門排行") }
                )
                Tab(
                    selected = uiState.selectedTab == "new",
                    onClick = { onIntent(HomeIntent.SelectTab("new")) },
                    text = { Text("最新上架") }
                )
                Tab(
                    selected = uiState.selectedTab == "favorite",
                    onClick = { onIntent(HomeIntent.SelectTab("favorite")) },
                    text = { Text("我的收藏") }
                )
            }

            // 內容列表
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading && uiState.votes.isEmpty()) {
                    CircularProgressIndicator()
                } else if (uiState.votes.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty()) "沒有找到符合的投票" else "目前暫無投票內容",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.votes,
                            key = { it.vote.voteCode }
                        ) { item ->
                            VoteCard(
                                item = item,
                                onClick = { onVoteClick(item.vote.voteCode) },
                                onFavoriteToggle = {
                                    onIntent(
                                        HomeIntent.ToggleFavorite(
                                            voteCode = item.vote.voteCode,
                                            currentFavorite = item.vote.isFavorite
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoteCard(
    item: VoteWithDetails,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = if (item.vote.maxOption > 1) "複選(最多${item.vote.maxOption}項)" else "單選",
                                fontSize = 12.sp
                            )
                        }
                    )
                    if (item.vote.isNeedPassword) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "私密投票",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    if (item.vote.isVoted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("已投票", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp) }
                        )
                    }
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (item.vote.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (item.vote.isFavorite) Color.Red else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.vote.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 預覽前兩個選項
            item.options.take(2).forEachIndexed { index, option ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}. ${option.title}",
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${option.count} 票",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "發起人：${item.vote.authorName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "總參與：${item.vote.totalVotedCount} 人",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ----------------- Previews -----------------

@Preview(showBackground = true)
@Composable
fun HomeScreenLoadingPreview() {
    HomeScreenContent(
        uiState = HomeUiState(isLoading = true),
        onIntent = {},
        onVoteClick = {},
        onCreateClick = {},
        onProfileClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenSuccessPreview() {
    val sampleVote = VoteWithDetails(
        vote = VoteEntity(
            voteCode = "v001",
            title = "2026 年 Android 最熱門開發架構是？",
            authorName = "資深架構師",
            maxOption = 1,
            totalVotedCount = 138,
            isFavorite = true,
            isVoted = true
        ),
        options = listOf(
            OptionEntity(voteCode = "v001", optionCode = "opt1", title = "MVI + Jetpack Compose", count = 98),
            OptionEntity(voteCode = "v001", optionCode = "opt2", title = "傳統 MVVM + XML", count = 40)
        )
    )
    HomeScreenContent(
        uiState = HomeUiState(votes = listOf(sampleVote)),
        onIntent = {},
        onVoteClick = {},
        onCreateClick = {},
        onProfileClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyPreview() {
    HomeScreenContent(
        uiState = HomeUiState(votes = emptyList()),
        onIntent = {},
        onVoteClick = {},
        onCreateClick = {},
        onProfileClick = {}
    )
}
