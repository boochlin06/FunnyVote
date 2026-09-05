package com.heaton.funnyvote.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.R
import com.heaton.funnyvote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToAboutApp: () -> Unit = {},
    onNavigateToTutorial: () -> Unit = {},
    onNavigateToAuthorInfo: () -> Unit = {},
    onNavigateToLicence: () -> Unit = {},
    onNavigateToProblem: () -> Unit = {}
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
                title = { Text("關於", fontWeight = FontWeight.Bold) },
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
            // Card 1: 關於趣投票功能清單
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "關於趣投票",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 經典 Logo 橫幅
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = "FunnyVote Banner",
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AboutNavRow(
                        title = "關於趣投票 APP",
                        icon = Icons.Default.Info,
                        onClick = onNavigateToAboutApp
                    )
                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

                    AboutNavRow(
                        title = "功能導覽介紹",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        onClick = onNavigateToTutorial
                    )
                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

                    AboutNavRow(
                        title = "作者相關資訊",
                        icon = Icons.Default.Person,
                        onClick = onNavigateToAuthorInfo
                    )
                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

                    AboutNavRow(
                        title = "開源許可授權",
                        icon = Icons.Default.Description,
                        onClick = onNavigateToLicence
                    )
                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

                    AboutNavRow(
                        title = "常見問題 FAQ",
                        icon = Icons.Default.HelpOutline,
                        onClick = onNavigateToProblem
                    )
                }
            }

            // Card 2: 版本資訊與分享
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "軟體資訊",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "目前版本", fontSize = 15.sp, color = TextPrimary)
                        Text(text = "3.0.0 (Modern Compose)", fontSize = 14.sp, color = TextSecondary)
                    }

                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

                    AboutNavRow(
                        title = "檢查線上更新",
                        icon = Icons.Default.SystemUpdate,
                        onClick = {
                            val appPackageName = context.packageName
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 4.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("分享趣投票給好友", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AboutNavRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FunnyVoteBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(14.dp)
        )
    }
}
