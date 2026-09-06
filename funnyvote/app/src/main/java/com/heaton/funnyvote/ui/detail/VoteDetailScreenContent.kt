package com.heaton.funnyvote.ui.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.data.local.entity.OptionEntity
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import com.heaton.funnyvote.ui.theme.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteDetailScreenContent(
    uiState: VoteDetailUiState,
    onIntent: (VoteDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onAuthorClick: (String, String, String?) -> Unit = { _, _, _ -> },
    autoShare: Boolean = false,
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    val vote = uiState.voteWithDetails?.vote
    val rawOptions = uiState.voteWithDetails?.options ?: emptyList()
    var isSortedByCount by remember { mutableStateOf(false) }
    var isPreviewResult by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    LaunchedEffect(autoShare) {
        if (autoShare) {
            delay(800)
            showShareSheet = true
        }
    }
    val options = remember(rawOptions, isSortedByCount) {
        if (isSortedByCount) rawOptions.sortedByDescending { it.count } else rawOptions
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = FunnyVoteWindowBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FunnyVoteBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = { Text(text = "投票詳情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (vote != null) {
                        IconButton(onClick = { showShareSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "分享",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { onIntent(VoteDetailIntent.SetShowInfoDialog(true)) }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "規則詳情",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { onIntent(VoteDetailIntent.ToggleFavorite) }) {
                            Icon(
                                imageVector = if (vote.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "收藏",
                                tint = if (vote.isFavorite) StarGold else Color.White
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (vote != null && uiState.isUnlocked) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. 浮動操作：回到頂部 (fabTop)
                    SmallFloatingActionButton(
                        onClick = { scope.launch { listState.animateScrollToItem(0) } },
                        containerColor = Color.White,
                        contentColor = FunnyVoteBlue
                    ) {
                        Icon(Icons.Default.VerticalAlignTop, contentDescription = "回到頂部", modifier = Modifier.size(20.dp))
                    }

                    // 2. 浮動操作：排序選項 (fabOptionSort)
                    SmallFloatingActionButton(
                        onClick = { isSortedByCount = !isSortedByCount },
                        containerColor = if (isSortedByCount) FunnyVoteBlue else Color.White,
                        contentColor = if (isSortedByCount) Color.White else FunnyVoteBlue
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = "依票數排序", modifier = Modifier.size(20.dp))
                    }

                    // 3. 浮動操作：預覽結果/隱藏結果 (fabPreResult)
                    if (!vote.isVoted) {
                        ExtendedFloatingActionButton(
                            onClick = { isPreviewResult = !isPreviewResult },
                            containerColor = if (isPreviewResult) StarGold else Color.White,
                            contentColor = if (isPreviewResult) Color.White else TextPrimary,
                            icon = { Icon(Icons.Default.Poll, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            text = { Text(if (isPreviewResult) "隱藏結果" else "預覽結果", fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (vote != null && !vote.isVoted && uiState.isUnlocked) {
                Surface(
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { onIntent(VoteDetailIntent.SubmitVote) },
                            enabled = uiState.selectedOptionCodes.isNotEmpty() && !uiState.isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FunnyVoteBlue)
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "確認送出投票 (${uiState.selectedOptionCodes.size}/${vote.maxOption})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = FunnyVoteBlue)
            } else if (vote == null) {
                Text(
                    text = uiState.errorMessage ?: "查無此投票資訊",
                    color = Color.Red
                )
            } else if (!uiState.isUnlocked) {
                PasswordLockCard(
                    passwordInput = uiState.passwordInput,
                    passwordError = uiState.passwordError,
                    onPasswordChange = { onIntent(VoteDetailIntent.UpdatePasswordInput(it)) },
                    onUnlock = { onIntent(VoteDetailIntent.UnlockWithPassword) }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { onAuthorClick(vote.authorId, vote.authorName, vote.authorIcon) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = CircleShape,
                                        color = Color(0xFFE0E0E0)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = vote.authorName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                        Text(text = "2026/09/05", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    if (vote.isVoted) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = FunnyVoteBlue.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "你已投票",
                                                color = FunnyVoteBlue,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = vote.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (vote.maxOption > 1) "規則：複選 (最多 ${vote.maxOption} 項)" else "規則：單選投票",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "總參與：${vote.totalVotedCount} 票",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FunnyVoteBlue
                                    )
                                }
                            }
                        }
                    }

                    items(options, key = { it.optionCode }) { option ->
                        val isSelected = uiState.selectedOptionCodes.contains(option.optionCode)
                        val totalVotes = options.sumOf { it.count }.coerceAtLeast(1)
                        val ratio = (option.count.toFloat() / totalVotes.toFloat()).coerceIn(0f, 1f)
                        val percentage = ratio * 100f
                        val maxCount = options.maxOfOrNull { it.count } ?: 0
                        val isChampion = option.count > 0 && option.count == maxCount

                        ClassicOptionDetailCard(
                            option = option,
                            isVoted = vote.isVoted || isPreviewResult,
                            isSelected = isSelected,
                            percentage = percentage,
                            ratio = ratio,
                            isChampion = isChampion,
                            isMultiChoice = vote.maxOption > 1,
                            onClick = {
                                onIntent(VoteDetailIntent.SelectOption(option.optionCode))
                            }
                        )
                    }

                    // 自由新增選項卡片 (card_view_item_unpoll_create_new_option)
                    if (vote.isUserCanAddOption && !vote.isVoted) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onIntent(VoteDetailIntent.SetShowAddOptionDialog(true)) },
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(containerColor = Option2Background),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "新增選項",
                                        tint = FunnyVoteBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "新增投票選項 (Add New Option)",
                                        fontWeight = FontWeight.Bold,
                                        color = FunnyVoteBlue,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. 規則說明彈窗 (dialog_vote_detail_info)
        if (uiState.showInfoDialog && vote != null) {
            AlertDialog(
                onDismissRequest = { onIntent(VoteDetailIntent.SetShowInfoDialog(false)) },
                title = {
                    Text(
                        text = "投票詳細資訊",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Text(text = "選擇規則", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = if (vote.maxOption > 1) "複選投票 (最多可選 ${vote.maxOption} 項)" else "單選投票 (僅可選擇 1 項)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                        Column {
                            Text(text = "安全與權限", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = buildString {
                                    append(if (vote.isNeedPassword) "🔒 私密保護投票 (需要密碼)" else "🌐 公開投票 (免密碼)")
                                    if (vote.isUserCanAddOption) append(" • 允許參與者自由新增選項")
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                        Column {
                            Text(text = "建立時間與發起者", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "由 ${vote.authorName} 發起於 2026/09/05",
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onIntent(VoteDetailIntent.SetShowInfoDialog(false)) }) {
                        Text("知道了", color = FunnyVoteBlue, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 2. 新增選項彈窗 (Add Option Dialog)
        if (uiState.showAddOptionDialog) {
            AlertDialog(
                onDismissRequest = { onIntent(VoteDetailIntent.SetShowAddOptionDialog(false)) },
                title = {
                    Text(
                        text = "新增投票選項",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "發起人已開放參與者擴充選項，請輸入你想推薦的選項內容：",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.newOptionInput,
                            onValueChange = { onIntent(VoteDetailIntent.UpdateNewOptionInput(it)) },
                            label = { Text("選項名稱 (最多 30 字)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onIntent(VoteDetailIntent.SubmitNewOption) },
                        colors = ButtonDefaults.buttonColors(containerColor = FunnyVoteBlue)
                    ) {
                        Text("送出新增")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(VoteDetailIntent.SetShowAddOptionDialog(false)) }) {
                        Text("取消", color = Color.Gray)
                    }
                }
            )
        }

        // 3. 社群分享彈窗 (Share Bottom Sheet)
        if (showShareSheet && vote != null) {
            com.heaton.funnyvote.ui.common.ShareBottomSheet(
                title = vote.title,
                voteUrl = "https://www.funny-vote.com/vote/${vote.voteCode}",
                onDismiss = { showShareSheet = false }
            )
        }
    }
}

@Composable
fun ClassicOptionDetailCard(
    option: OptionEntity,
    isVoted: Boolean,
    isSelected: Boolean,
    percentage: Float,
    ratio: Float,
    isChampion: Boolean,
    isMultiChoice: Boolean,
    onClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(targetValue = if (isVoted) ratio else 0f, label = "progress")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isVoted, onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Option2Background else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isVoted) {
                    if (isMultiChoice) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onClick() },
                            colors = CheckboxDefaults.colors(checkedColor = FunnyVoteBlue)
                        )
                    } else {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onClick() },
                            colors = RadioButtonDefaults.colors(selectedColor = FunnyVoteBlue)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                } else if (option.isUserChoiced) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "你的選擇",
                        tint = FunnyVoteBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = option.title,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected || option.isUserChoiced) FontWeight.Bold else FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                if (isVoted && isChampion) {
                    Text("🏆", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                }

                if (isVoted) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%% (%d 票)", percentage, option.count),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (option.isUserChoiced) FunnyVoteBlue else TextPrimary
                    )
                }
            }

            if (isVoted) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
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
}

@Composable
fun PasswordLockCard(
    passwordInput: String,
    passwordError: String?,
    onPasswordChange: (String) -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = FunnyVoteBlue
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "此為私密保護投票",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "請輸入密碼以解鎖檢視內容並參與投票",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = onPasswordChange,
                label = { Text("請輸入投票密碼") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = Color.Red) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onUnlock,
                colors = ButtonDefaults.buttonColors(containerColor = FunnyVoteBlue),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("解鎖投票")
            }
        }
    }
}
