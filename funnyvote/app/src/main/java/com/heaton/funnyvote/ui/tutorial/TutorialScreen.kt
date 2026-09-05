package com.heaton.funnyvote.ui.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.ui.theme.*
import kotlinx.coroutines.launch

data class TutorialPageData(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val backgroundColor: Color
)

val tutorialPages = listOf(
    TutorialPageData(
        title = "Funny Vote 趣投票",
        subtitle = "有趣投票，發起超簡單",
        description = "就是有趣的投票，雖然是這樣說，但是內容有不有趣完全是看你，我們不過只是讓『發起投票』這件事變得超級簡單！",
        icon = Icons.Default.Poll,
        backgroundColor = FunnyVoteBlue
    ),
    TutorialPageData(
        title = "Quick Poll 快速投票",
        subtitle = "一鍵指引明路，隨手做功德",
        description = "為他人指引明路乃是一種功德，但是搞得太麻煩就興致缺缺的，趣投票讓做功德這件事簡單到只要『點選選項』就搞定！",
        icon = Icons.Default.TouchApp,
        backgroundColor = Color(0xFF1E88E5)
    ),
    TutorialPageData(
        title = "No Comment 沒有留言板",
        subtitle = "純粹決策，遠離指指點點",
        description = "求助最怕有疑惑時，想尋求眾人智慧，卻被一堆酸民指指點點，趣投票考慮到這一點，所以我們堅持『完全沒有留言板』！",
        icon = Icons.Default.SpeakerNotesOff,
        backgroundColor = Color(0xFF0288D1)
    ),
    TutorialPageData(
        title = "Big Data 大數據",
        subtitle = "我們完全沒有大數據！",
        description = "關於這個，我們完全沒有大數據，但絕對不是因為覺得很麻煩，純粹回歸人與人之間最真誠的直覺表決！",
        icon = Icons.Default.Storage,
        backgroundColor = Color(0xFFE53935)
    ),
    TutorialPageData(
        title = "Share Finance 共享經濟",
        subtitle = "知識共享與智慧匯聚",
        description = "聽說現在共享經濟很流行，其實投票不也是一種集體智慧的共享嗎？經濟的部分就只能看有沒有廣告收入了！",
        icon = Icons.Default.Share,
        backgroundColor = Color(0xFFD32F2F)
    ),
    TutorialPageData(
        title = "Let's Poll 立即出發",
        subtitle = "拯救猶豫不決的眾生",
        description = "如果你完全搞不懂前面在說啥？沒關係，先來發起一則投票，或者幫大家投票解惑吧！",
        icon = Icons.Default.RocketLaunch,
        backgroundColor = Color(0xFFC62828)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { tutorialPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == tutorialPages.size - 1
    val currentPageData = tutorialPages[pagerState.currentPage]

    Scaffold(
        containerColor = currentPageData.backgroundColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    IconButton(onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上一頁", tint = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                TextButton(onClick = onFinish) {
                    Text("跳過 (SKIP)", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 指示器 (Dots indicator)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(tutorialPages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.4f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 下一步或開始使用按鈕
                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = currentPageData.backgroundColor
                    )
                ) {
                    Text(
                        text = if (isLastPage) "開始使用 FunnyVote" else "下一步",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isLastPage) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { pageIndex ->
            val page = tutorialPages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = page.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = page.subtitle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = page.description,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TutorialScreenPreview() {
    TutorialScreen(onFinish = {})
}
