package com.heaton.funnyvote.ui.about

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreenContent(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = FunnyVoteWindowBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FunnyVoteBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                title = { Text("關於 FunnyVote", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. App 介紹卡片 (對應 activity_about_app.xml)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        color = FunnyVoteBlue
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "全台最大投票軟體上線啦",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "版本 3.0.0 (Modern Android Edition)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "【快速發起投票】\n會想發起投票就是因為心中有猶豫、或是覺得有趣，趣投票讓發起投票變得超級方便！\n\n" +
                                "【快速投票】\n完全不需要繁複過程，只要一鍵就可以快速投票。\n\n" +
                                "【沒有廢話】\n求助時最怕想尋求眾人智慧卻一堆人指指點點，趣投票堅持「沒有留言板」乾淨純粹。\n\n" +
                                "【大數據與共享】\n投票本身就是一種智慧共享，快來拯救猶豫不決的眾生！",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 一鍵分享按鈕 (btnShareApp)
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "FunnyVote 趣投票")
                                putExtra(Intent.EXTRA_TEXT, "最有趣的投票社群 FunnyVote！快來下載體驗：https://www.funny-vote.com")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "分享 FunnyVote"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FunnyVoteBlue),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("分享 FunnyVote 給朋友", color = Color.White)
                    }
                }
            }

            // 2. 開發團隊資訊 (about_author_info)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "開發團隊 (Author Information)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AuthorItem(name = "Heaton", role = "全端架構與 Android", desc = "PM, Android RD, UI, QA 樣樣兼修，負責現代化 Compose 重構。")
                    AuthorItem(name = "Jim", role = "後端架構", desc = "負責後端資料服務與雲端部署。")
                    AuthorItem(name = "Nick", role = "文案設計", desc = "文案主要擔當，幽默風格。")
                    AuthorItem(name = "Eason", role = "顧問團隊", desc = "社群與運營顧問。")
                }
            }

            // 3. 開源許可證 (activity_licence.xml)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "開源許可 (Open Source Licences)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LicenceItem(name = "Jetpack Compose & Material 3", licence = "Apache License 2.0 (Google LLC)")
                    LicenceItem(name = "Kotlin Coroutines & Flow", licence = "Apache License 2.0 (JetBrains s.r.o.)")
                    LicenceItem(name = "AndroidX Room & Navigation", licence = "Apache License 2.0 (Google LLC)")
                    LicenceItem(name = "Hilt Dependency Injection", licence = "Apache License 2.0 (Google LLC)")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AuthorItem(name: String, role: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.width(6.dp))
            Surface(shape = RoundedCornerShape(4.dp), color = FunnyVoteBlue.copy(alpha = 0.1f)) {
                Text(text = role, fontSize = 11.sp, color = FunnyVoteBlue, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
            }
        }
        Text(text = desc, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun LicenceItem(name: String, licence: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
        Text(text = licence, fontSize = 11.sp, color = TextSecondary)
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    AboutScreenContent(onNavigateBack = {})
}
