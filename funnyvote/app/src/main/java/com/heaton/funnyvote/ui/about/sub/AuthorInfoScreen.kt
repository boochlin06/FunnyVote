package com.heaton.funnyvote.ui.about.sub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.ui.theme.FunnyVoteBlue
import com.heaton.funnyvote.ui.theme.FunnyVoteWindowBg
import com.heaton.funnyvote.ui.theme.TextPrimary
import com.heaton.funnyvote.ui.theme.TextSecondary

data class TeamMember(
    val name: String,
    val role: String,
    val description: String,
    val contact: String
)

val teamMembers = listOf(
    TeamMember(
        name = "Heaton Lin",
        role = "全端架構與 Android 開發",
        description = "負責 FunnyVote 核心功能設計、資料流架構及 2026 現代化 Compose/MVI 全面重構。",
        contact = "heaton@funnyvote.org"
    ),
    TeamMember(
        name = "Jim",
        role = "後端服務工程師",
        description = "負責高併發投票結算引擎、分散式資料庫快取架構與防刷票風控設計。",
        contact = "jim@funnyvote.org"
    ),
    TeamMember(
        name = "Nick",
        role = "UI / UX 體驗設計師",
        description = "打造純淨、專注於決策本質的互動介面，堅持「沒有留言板」的極簡設計語彙。",
        contact = "nick@funnyvote.org"
    ),
    TeamMember(
        name = "Eason",
        role = "品質工程與社群運營",
        description = "負責全平台自動化測試驗證、使用者真實場景反饋收集與產品體驗優化。",
        contact = "eason@funnyvote.org"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorInfoScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = FunnyVoteWindowBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FunnyVoteBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                title = { Text("開發團隊 (Author Info)", fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(teamMembers) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = FunnyVoteBlue.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = FunnyVoteBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = member.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = member.role,
                                    fontSize = 12.sp,
                                    color = FunnyVoteBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = member.description,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = member.contact,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
