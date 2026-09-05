package com.heaton.funnyvote.ui.profile

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.data.local.entity.UserEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 用戶頭像與名稱卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                                Icon(Icons.Default.Check, contentDescription = "儲存", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.user?.userName ?: "訪客",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onIntent(ProfileIntent.EditName(true)) }) {
                                Icon(Icons.Default.Edit, contentDescription = "編輯暱稱", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Text(
                        text = uiState.user?.email ?: "未綁定電子郵件",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // 數據統計列
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "架構與技術亮點",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TechInfoRow(label = "UI 系統", value = "Jetpack Compose + Material 3")
                    TechInfoRow(label = "應用架構", value = "MVI (StateFlow + Channel UDF)")
                    TechInfoRow(label = "導航架構", value = "Navigation Compose 2.8+ Type-Safe")
                    TechInfoRow(label = "持久化儲存", value = "Room 2.6+ (Local-First SSOT)")
                    TechInfoRow(label = "依賴注入", value = "Dagger Hilt + KSP")
                    TechInfoRow(label = "單元測試", value = "Turbine + MockK + CoroutineTest")
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$count",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = unit, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
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
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ----------------- Previews -----------------

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreenContent(
        uiState = ProfileUiState(
            user = UserEntity(userId = "1", userName = "Android資深開發者", email = "test@dev.com"),
            totalCreatedVotes = 3,
            totalVotedCount = 18,
            totalFavoriteCount = 5
        ),
        onIntent = {},
        onNavigateBack = {}
    )
}
