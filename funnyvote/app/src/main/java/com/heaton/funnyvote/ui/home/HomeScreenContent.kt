package com.heaton.funnyvote.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.data.local.entity.OptionEntity
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import com.heaton.funnyvote.ui.theme.*
import java.util.Locale

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
        containerColor = FunnyVoteWindowBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = FunnyVoteBlue,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    title = {
                        if (uiState.isSearchActive) {
                            TextField(
                                value = uiState.searchQuery,
                                onValueChange = { onIntent(HomeIntent.UpdateSearchQuery(it)) },
                                placeholder = { Text("搜尋投票標題...", color = Color.White.copy(alpha = 0.7f)) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = "FunnyVote",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onIntent(HomeIntent.ToggleSearch(!uiState.isSearchActive)) }) {
                            Icon(
                                imageVector = if (uiState.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "搜尋"
                            )
                        }
                        IconButton(onClick = onProfileClick) {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "個人")
                        }
                    }
                )

                // 原版風格之整合式藍底分頁欄
                TabRow(
                    selectedTabIndex = when (uiState.selectedTab) {
                        "new" -> 1
                        "favorite" -> 2
                        else -> 0
                    },
                    containerColor = FunnyVoteBlue,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        val index = when (uiState.selectedTab) {
                            "new" -> 1
                            "favorite" -> 2
                            else -> 0
                        }
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[index]),
                            height = 3.dp,
                            color = FunnyVoteBlueLight
                        )
                    }
                ) {
                    Tab(
                        selected = uiState.selectedTab == "hot",
                        onClick = { onIntent(HomeIntent.SelectTab("hot")) },
                        text = { Text("熱門排行", fontWeight = FontWeight.Medium) }
                    )
                    Tab(
                        selected = uiState.selectedTab == "new",
                        onClick = { onIntent(HomeIntent.SelectTab("new")) },
                        text = { Text("最新上架", fontWeight = FontWeight.Medium) }
                    )
                    Tab(
                        selected = uiState.selectedTab == "favorite",
                        onClick = { onIntent(HomeIntent.SelectTab("favorite")) },
                        text = { Text("我的收藏", fontWeight = FontWeight.Medium) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = FunnyVoteBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "發起投票")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading && uiState.votes.isEmpty()) {
                CircularProgressIndicator(color = FunnyVoteBlue)
            } else if (uiState.votes.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty()) "沒有找到符合的投票" else "目前暫無投票內容",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.votes,
                        key = { it.vote.voteCode }
                    ) { item ->
                        ClassicVoteCard(
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

/**
 * 完美還原原版 card_view_wall_item.xml 經典視覺
 */
@Composable
fun ClassicVoteCard(
    item: VoteWithDetails,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val totalCount = item.vote.totalVotedCount.coerceAtLeast(1)
    val maxVoteCount = item.options.maxOfOrNull { it.count } ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // 1. Author Bar (include_author.xml)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color(0xFFE0E0E0)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.vote.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "2026/09/05",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                if (item.vote.isNeedPassword) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "私密投票",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (item.vote.isVoted) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FunnyVoteBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "已投票",
                            color = FunnyVoteBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Title (txtTitle)
            Text(
                text = item.vote.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Function Bar (include_function_bar.xml)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp))
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 投票數
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Poll,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = FunnyVoteBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Vote: ${item.vote.totalVotedCount}", fontSize = 12.sp, color = TextPrimary)
                }

                // 收藏
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onFavoriteToggle)
                ) {
                    Icon(
                        imageVector = if (item.vote.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (item.vote.isFavorite) StarGold else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.vote.isFavorite) "已收藏" else "收藏",
                        fontSize = 12.sp,
                        color = if (item.vote.isFavorite) StarGold else TextPrimary
                    )
                }

                // 分享
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "分享", fontSize = 12.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Options List (btnFirstOption, btnSecondOption, btnThirdOption MORE)
            item.options.take(2).forEachIndexed { index, option ->
                val isFirst = index == 0
                val bgColor = if (isFirst) Option1Background else Option2Background
                val numBgColor = if (isFirst) Option1NumberBg else Option2NumberBg
                val ratio = (option.count.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
                val isChampion = option.count > 0 && option.count == maxVoteCount

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 圓形編號 (txtOptionNumber)
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = CircleShape,
                                color = numBgColor
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = option.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isChampion) {
                                Text("🏆", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", ratio * 100f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 金黃色進度條 (RoundCornerProgressBar)
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ProgressAmber,
                            trackColor = ProgressAmberTrack
                        )
                    }
                }
            }

            // MORE 按鈕 (btnThirdOption)
            if (item.options.size > 2) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Option2Background),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MORE (${item.options.size} 個選項) +",
                            fontWeight = FontWeight.Bold,
                            color = FunnyVoteBlue,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ----------------- Previews -----------------

@Preview(showBackground = true)
@Composable
fun HomeScreenClassicPreview() {
    val sample = VoteWithDetails(
        vote = VoteEntity(
            voteCode = "v1",
            title = "Do your mother know what you do in front of computer?",
            authorName = "Heaton Lin",
            totalVotedCount = 120,
            isFavorite = true
        ),
        options = listOf(
            OptionEntity(voteCode = "v1", optionCode = "o1", title = "Yes, I have told her", count = 90),
            OptionEntity(voteCode = "v1", optionCode = "o2", title = "No, absolutely a secret", count = 30),
            OptionEntity(voteCode = "v1", optionCode = "o3", title = "She doesn't care", count = 0)
        )
    )
    HomeScreenContent(
        uiState = HomeUiState(votes = listOf(sample)),
        onIntent = {},
        onVoteClick = {},
        onCreateClick = {},
        onProfileClick = {}
    )
}
