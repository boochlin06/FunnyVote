package com.heaton.funnyvote.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.data.local.entity.UserEntity
import com.heaton.funnyvote.ui.home.ClassicVoteCard
import com.heaton.funnyvote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onVoteClick: (String) -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    Scaffold(
        containerColor = FunnyVoteWindowBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FunnyVoteBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                title = { Text("個人中心", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. 個人資料卡片 (包含折疊底圖與圓形頭像)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 視差橫幅
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(FunnyVoteBlue.copy(alpha = 0.85f))
                        )

                        // 圓形頭像 (支援點擊選圖上傳與 Coil 快取展示)
                        Surface(
                            modifier = Modifier
                                .size(76.dp)
                                .offset(y = (-38).dp)
                                .clip(CircleShape)
                                .clickable { onAvatarClick() },
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(FunnyVoteBlue)
                            ) {
                                if (uiState.isUploadingAvatar) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = Color.White,
                                        strokeWidth = 3.dp
                                    )
                                } else if (!uiState.user?.userIcon.isNullOrBlank()) {
                                    coil.compose.AsyncImage(
                                        model = uiState.user?.userIcon,
                                        contentDescription = "個人頭像",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(50.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // 姓名與修改按鈕
                        Column(
                            modifier = Modifier
                                .offset(y = (-28).dp)
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.user?.userName ?: "訪客",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                IconButton(
                                    onClick = { onIntent(ProfileIntent.EditName(true)) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "編輯暱稱",
                                        modifier = Modifier.size(18.dp),
                                        tint = FunnyVoteBlue
                                    )
                                }
                            }

                            Text(
                                text = uiState.user?.email ?: "dev@funnyvote.org",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        // 3 欄數據統計小卡
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-14).dp)
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "發起投票",
                                count = uiState.totalCreatedVotes,
                                unit = "則",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "已收藏",
                                count = uiState.totalFavoriteCount,
                                unit = "則",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "已參與",
                                count = uiState.totalVotedCount,
                                unit = "次",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Google 登入與帳號綁定狀態小卡
            item {
                if (uiState.isAnonymous) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = FunnyVoteBlue.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "目前為訪客身份",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "綁定 Google 帳號，永久保存發起與投票紀錄",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onGoogleSignInClick,
                                colors = ButtonDefaults.buttonColors(containerColor = FunnyVoteBlue),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                if (uiState.isGoogleSigningIn) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("連結 Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "已連結永久 Google 帳號",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2E7D32),
                                    fontSize = 12.sp
                                )
                            }
                            TextButton(onClick = { onIntent(ProfileIntent.SignOut) }) {
                                Text("登出", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 3. 三 Tab 分頁選單 (1. 我發起的投票, 2. 我的收藏, 3. 我參與的投票)
            item {
                TabRow(
                    selectedTabIndex = uiState.selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = FunnyVoteBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTabIndex]),
                            color = FunnyVoteBlue,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {
                    Tab(
                        selected = uiState.selectedTabIndex == 0,
                        onClick = { onIntent(ProfileIntent.SelectTab(0)) },
                        text = {
                            Text(
                                text = "我發起 (${uiState.createdVotes.size})",
                                fontWeight = if (uiState.selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.selectedTabIndex == 0) FunnyVoteBlue else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTabIndex == 1,
                        onClick = { onIntent(ProfileIntent.SelectTab(1)) },
                        text = {
                            Text(
                                text = "我的收藏 (${uiState.favoriteVotes.size})",
                                fontWeight = if (uiState.selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.selectedTabIndex == 1) FunnyVoteBlue else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTabIndex == 2,
                        onClick = { onIntent(ProfileIntent.SelectTab(2)) },
                        text = {
                            Text(
                                text = "我參與 (${uiState.participatedVotes.size})",
                                fontWeight = if (uiState.selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.selectedTabIndex == 2) FunnyVoteBlue else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // 4. 渲染投票列表或空狀態
            val currentList = when (uiState.selectedTabIndex) {
                0 -> uiState.createdVotes
                1 -> uiState.favoriteVotes
                else -> uiState.participatedVotes
            }
            if (currentList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (uiState.selectedTabIndex) {
                                    0 -> Icons.Default.HowToVote
                                    1 -> Icons.Default.StarBorder
                                    else -> Icons.Default.Poll
                                },
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = when (uiState.selectedTabIndex) {
                                    0 -> "你尚未發起過任何投票"
                                    1 -> "尚未加入任何收藏投票"
                                    else -> "尚未參與過任何投票"
                                },
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(currentList, key = { it.vote.voteCode }) { voteItem ->
                    ClassicVoteCard(
                        item = voteItem,
                        onClick = { onVoteClick(voteItem.vote.voteCode) },
                        onFavoriteToggle = { onIntent(ProfileIntent.ToggleFavorite(voteItem.vote.voteCode)) }
                    )
                }
            }
        }

        // 4. 專屬暱稱修改對話框 (dialog_account_name_edit)
        if (uiState.isEditingName) {
            AlertDialog(
                onDismissRequest = { onIntent(ProfileIntent.EditName(false)) },
                title = { Text("修改暱稱 (Change Name)", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("請輸入你希望在 FunnyVote 顯示的新暱稱：", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = uiState.nameInput,
                            onValueChange = { onIntent(ProfileIntent.UpdateNameInput(it)) },
                            label = { Text("個人暱稱 (最多 20 字)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onIntent(ProfileIntent.SaveName) },
                        colors = ButtonDefaults.buttonColors(containerColor = FunnyVoteBlue)
                    ) {
                        Text("儲存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(ProfileIntent.EditName(false)) }) {
                        Text("取消", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$count",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FunnyVoteBlue
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = unit, fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}
