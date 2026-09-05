package com.heaton.funnyvote.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.data.local.entity.UserEntity
import com.heaton.funnyvote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateBack: () -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 原版 activity_personal.xml 之視差橫幅效果 (imgProfileBackdrop)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(FunnyVoteBlue)
                    )

                    // 懸浮重疊大圓頭像
                    Surface(
                        modifier = Modifier
                            .offset(y = (-45).dp)
                            .size(90.dp)
                            .clip(CircleShape),
                        color = Color.White,
                        shadowElevation = 6.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(FunnyVoteBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .offset(y = (-35).dp)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.isEditingName) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                OutlinedTextField(
                                    value = uiState.nameInput,
                                    onValueChange = { onIntent(ProfileIntent.UpdateNameInput(it)) },
                                    label = { Text("修改暱稱") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { onIntent(ProfileIntent.SaveName) }) {
                                    Icon(Icons.Default.Check, contentDescription = "儲存", tint = FunnyVoteBlue)
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.user?.userName ?: "訪客",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                IconButton(onClick = { onIntent(ProfileIntent.EditName(true)) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "編輯暱稱", modifier = Modifier.size(18.dp), tint = FunnyVoteBlue)
                                }
                            }
                        }

                        Text(
                            text = uiState.user?.email ?: "未綁定電子郵件",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 數據統計列
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "已參與",
                    count = uiState.totalVotedCount,
                    unit = "項",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "已收藏",
                    count = uiState.totalFavoriteCount,
                    unit = "項",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "發起投票",
                    count = uiState.totalCreatedVotes,
                    unit = "次",
                    modifier = Modifier.weight(1f)
                )
            }

            // App 資訊卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "關於 FunnyVote",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TechInfoRow(label = "版本號", value = "3.0.0 (Modern Android Edition)")
                    TechInfoRow(label = "UI 系統", value = "Jetpack Compose (經典藍色主題)")
                    TechInfoRow(label = "架構模式", value = "MVI (StateFlow + Channel UDF)")
                    TechInfoRow(label = "資料快取", value = "Room 2.6+ (Local-First SSOT)")
                    TechInfoRow(label = "導航架構", value = "Navigation Compose 2.8+ Type-Safe")
                }
            }
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$count",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FunnyVoteBlue
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = unit, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun TechInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenClassicPreview() {
    ProfileScreenContent(
        uiState = ProfileUiState(
            user = UserEntity(userId = "1", userName = "Heaton Lin", email = "test@dev.com"),
            totalCreatedVotes = 3,
            totalVotedCount = 18,
            totalFavoriteCount = 5
        ),
        onIntent = {},
        onNavigateBack = {}
    )
}
