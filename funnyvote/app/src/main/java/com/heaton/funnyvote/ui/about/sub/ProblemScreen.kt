package com.heaton.funnyvote.ui.about.sub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.ui.theme.FunnyVoteBlue
import com.heaton.funnyvote.ui.theme.FunnyVoteWindowBg
import com.heaton.funnyvote.ui.theme.TextPrimary
import com.heaton.funnyvote.ui.theme.TextSecondary

data class ProblemItem(val question: String, val answer: String)

val problemList = listOf(
    ProblemItem(
        "Q: 為什麼沒有留言板？",
        "A: 求助最怕有疑惑時想尋求大眾智慧，卻被酸民指指點點。FunnyVote 堅持「沒有留言板」，讓表決結果純粹、乾淨，不被多餘口水戰干擾。"
    ),
    ProblemItem(
        "Q: 投票可以重複投票或修改嗎？",
        "A: 每個投票一旦送出確認後便計入總票數，無法重複投票或收回，請謹慎且真誠地投下你寶貴的一票。"
    ),
    ProblemItem(
        "Q: 私密投票的密碼如何取得？",
        "A: 私密保護投票是由發起人設定存取密碼，參與者必須向發起人索取並輸入正確密碼後方能解鎖檢視內容並進行表決。"
    ),
    ProblemItem(
        "Q: 什麼是「允許自由新增選項」？",
        "A: 發起人在建立投票時若開啟此權限，參與者若覺得既有選項不足，可自行擴充新選項讓大家一起投票選擇。"
    ),
    ProblemItem(
        "Q: 如何發起全新的投票活動？",
        "A: 點選主畫面右下角的「+」藍色浮動按鈕，即可輸入投票標題、設計選項內容與自訂單選/複選等進階規則。"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemScreen(
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
                title = { Text("常見問題 (FAQ)", fontWeight = FontWeight.Bold) },
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
            items(problemList) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = item.question,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FunnyVoteBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.answer,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
