package com.heaton.funnyvote.ui.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import com.heaton.funnyvote.ui.theme.FunnyVoteBlue
import com.heaton.funnyvote.ui.theme.FunnyVoteWindowBg
import com.heaton.funnyvote.ui.theme.TextPrimary
import com.heaton.funnyvote.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScreenContent(
    uiState: PersonalUiState,
    onNavigateBack: () -> Unit,
    onVoteClick: (String) -> Unit
) {
    Scaffold(
        containerColor = FunnyVoteWindowBg,
        topBar = {
            TopAppBar(
                title = { Text("個人公開主頁", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FunnyVoteBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 作者資訊小卡
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(FunnyVoteBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!uiState.authorIcon.isNullOrBlank()) {
                                AsyncImage(
                                    model = uiState.authorIcon,
                                    contentDescription = "作者頭像",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = FunnyVoteBlue,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = uiState.authorName.ifBlank { "FunnyVote 使用者" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "用戶識別碼: ${uiState.authorId.take(14)}...",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text("已發起 ${uiState.votes.size} 個公開投票")
                            }
                        )
                    }
                }
            }

            // 標題
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HowToVote,
                        contentDescription = null,
                        tint = FunnyVoteBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "發起的投票清單",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // 投票項目
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = FunnyVoteBlue)
                    }
                }
            } else if (uiState.votes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "該使用者目前尚未發起任何公開投票",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(uiState.votes, key = { it.vote.voteCode }) { voteItem ->
                    AuthorVoteCard(
                        voteItem = voteItem,
                        onClick = { onVoteClick(voteItem.vote.voteCode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthorVoteCard(
    voteItem: VoteWithDetails,
    onClick: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val formattedDate = if (voteItem.vote.createdAt > 0) {
        dateFormatter.format(Date(voteItem.vote.createdAt))
    } else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = voteItem.vote.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (!voteItem.vote.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = voteItem.vote.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "累計 ${voteItem.vote.totalVotedCount} 人投票",
                    fontSize = 12.sp,
                    color = FunnyVoteBlue,
                    fontWeight = FontWeight.Medium
                )
                if (formattedDate.isNotBlank()) {
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
