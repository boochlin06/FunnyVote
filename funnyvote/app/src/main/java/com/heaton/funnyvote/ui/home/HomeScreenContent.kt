package com.heaton.funnyvote.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heaton.funnyvote.data.local.entity.OptionEntity
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import com.heaton.funnyvote.ui.common.ShareBottomSheet
import com.heaton.funnyvote.ui.theme.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onVoteClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAboutClick: () -> Unit = {},
    onTutorialClick: () -> Unit = {},
    onAuthorClick: (String, String, String?) -> Unit = { _, _, _ -> },
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var shareTargetVote by remember { mutableStateOf<VoteEntity?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                // 原版風格之 navigation_header.xml
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(FunnyVoteBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                onProfileClick()
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "FunnyVote 熱血會員",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "dev@funnyvote.org",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 原版選單清單
                NavigationDrawerItem(
                    label = { Text("熱門排行 (HOT)", fontWeight = FontWeight.Medium) },
                    selected = uiState.selectedTab == "hot",
                    icon = { Icon(Icons.Default.Whatshot, contentDescription = null, tint = if (uiState.selectedTab == "hot") FunnyVoteBlue else Color.Gray) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        onIntent(HomeIntent.SelectTab("hot"))
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("最新上架 (NEW)", fontWeight = FontWeight.Medium) },
                    selected = uiState.selectedTab == "new",
                    icon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = if (uiState.selectedTab == "new") FunnyVoteBlue else Color.Gray) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        onIntent(HomeIntent.SelectTab("new"))
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("我的收藏 (FAVORITE)", fontWeight = FontWeight.Medium) },
                    selected = uiState.selectedTab == "favorite",
                    icon = { Icon(Icons.Default.Star, contentDescription = null, tint = if (uiState.selectedTab == "favorite") StarGold else Color.Gray) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        onIntent(HomeIntent.SelectTab("favorite"))
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("發起投票", fontWeight = FontWeight.Medium) },
                    selected = false,
                    icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = FunnyVoteBlue) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCreateClick()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("個人帳號", fontWeight = FontWeight.Medium) },
                    selected = false,
                    icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        onProfileClick()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("功能導覽教學", fontWeight = FontWeight.Medium) },
                    selected = false,
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuOpen, contentDescription = null, tint = Color.Gray) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        onTutorialClick()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("關於 FunnyVote", fontWeight = FontWeight.Medium) },
                    selected = false,
                    icon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        onAboutClick()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = FunnyVoteWindowBg,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = FunnyVoteBlue,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "選單")
                            }
                        },
                        title = {
                            if (uiState.isSearchActive) {
                                TextField(
                                    value = uiState.searchQuery,
                                    onValueChange = { onIntent(HomeIntent.UpdateSearchQuery(it)) },
                                    placeholder = { Text("搜尋投票標題...", color = Color.White.copy(alpha = 0.7f)) },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "FunnyVote",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { onIntent(HomeIntent.ToggleSearch(!uiState.isSearchActive)) }) {
                                Icon(
                                    imageVector = if (uiState.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "搜尋"
                                )
                            }
                            IconButton(onClick = onProfileClick) {
                                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "個人")
                            }
                        }
                    )

                    // 原版風格之整合式藍底分頁欄
                    TabRow(
                        selectedTabIndex = when (uiState.selectedTab) {
                            "new" -> 1
                            "favorite" -> 2
                            else -> 0
                        },
                        containerColor = FunnyVoteBlue,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[when (uiState.selectedTab) {
                                    "new" -> 1
                                    "favorite" -> 2
                                    else -> 0
                                }]),
                                color = TabIndicatorWhite,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = uiState.selectedTab == "hot",
                            onClick = { onIntent(HomeIntent.SelectTab("hot")) },
                            text = { Text("熱門排行", fontWeight = FontWeight.Medium) }
                        )
                        Tab(
                            selected = uiState.selectedTab == "new",
                            onClick = { onIntent(HomeIntent.SelectTab("new")) },
                            text = { Text("最新上架", fontWeight = FontWeight.Medium) }
                        )
                        Tab(
                            selected = uiState.selectedTab == "favorite",
                            onClick = { onIntent(HomeIntent.SelectTab("favorite")) },
                            text = { Text("我的收藏", fontWeight = FontWeight.Medium) }
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onCreateClick,
                    containerColor = FunnyVoteBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "發起投票")
                }
            }
        ) { paddingValues ->
            val pullRefreshState = rememberPullToRefreshState()
            if (pullRefreshState.isRefreshing) {
                LaunchedEffect(true) {
                    onIntent(HomeIntent.Refresh)
                }
            }

            LaunchedEffect(uiState.isLoading) {
                if (!uiState.isLoading) {
                    pullRefreshState.endRefresh()
                }
            }

            val listState = rememberLazyListState()

            val shouldLoadMore = remember {
                derivedStateOf {
                    val totalItems = listState.layoutInfo.totalItemsCount
                    val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    totalItems > 0 && lastVisibleItem >= totalItems - 3
                }
            }

            LaunchedEffect(shouldLoadMore.value) {
                if (shouldLoadMore.value && !uiState.isLoading && !uiState.isLoadingMore && uiState.hasMore && !uiState.isSearchActive) {
                    onIntent(HomeIntent.LoadMore)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(pullRefreshState.nestedScrollConnection)
            ) {
                if (uiState.isLoading && uiState.votes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FunnyVoteBlue)
                    }
                } else if (uiState.votes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty()) "沒有找到符合的投票" else "目前暫無投票內容",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. 焦點推薦輪播卡片 (Promotion Carousel)
                        if (uiState.searchQuery.isEmpty() && uiState.votes.isNotEmpty()) {
                            item {
                                PromotionCarousel(
                                    items = uiState.votes.take(3),
                                    onVoteClick = onVoteClick
                                )
                            }
                        }

                        // 2. 投票清單
                        items(
                            items = uiState.votes,
                            key = { it.vote.voteCode }
                        ) { item ->
                            ClassicVoteCard(
                                item = item,
                                onClick = { onVoteClick(item.vote.voteCode) },
                                onFavoriteToggle = {
                                    onIntent(
                                        HomeIntent.ToggleFavorite(
                                            voteCode = item.vote.voteCode,
                                            currentFavorite = item.vote.isFavorite
                                        )
                                    )
                                },
                                onShareClick = {
                                    shareTargetVote = item.vote
                                },
                                onAuthorClick = onAuthorClick
                            )
                        }

                        // 3. 加載更多指示器
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = FunnyVoteBlue,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }

                PullToRefreshContainer(
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = Color.White,
                    contentColor = FunnyVoteBlue
                )
            }

            // 社群分享彈窗 (Share Bottom Sheet)
            shareTargetVote?.let { target ->
                ShareBottomSheet(
                    title = target.title,
                    voteUrl = "https://www.funny-vote.com/vote/${target.voteCode}",
                    onDismiss = { shareTargetVote = null }
                )
            }
        }
    }
}

/**
 * 焦點推薦輪播橫幅 (Promotion Carousel)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromotionCarousel(
    items: List<VoteWithDetails>,
    onVoteClick: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { items.size })

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val item = items[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVoteClick(item.vote.voteCode) },
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = FunnyVoteBlue.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = StarGold,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "熱門焦點推薦",
                                color = FunnyVoteBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${item.vote.totalVotedCount} 人參與",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = item.vote.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Pager Indicator dots
        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(items.size) { index ->
                    val color = if (pagerState.currentPage == index) FunnyVoteBlue else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

/**
 * 完美還原原版 card_view_wall_item.xml 經典視覺
 */
@Composable
fun ClassicVoteCard(
    item: VoteWithDetails,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onShareClick: () -> Unit = {},
    onAuthorClick: (String, String, String?) -> Unit = { _, _, _ -> }
) {
    val totalCount = item.vote.totalVotedCount.coerceAtLeast(1)
    val maxVoteCount = item.options.maxOfOrNull { it.count } ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // 1. Author Bar (include_author.xml)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onAuthorClick(item.vote.authorId, item.vote.authorName, item.vote.authorIcon) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color(0xFFE0E0E0)
                ) {
                    if (!item.vote.authorIcon.isNullOrBlank()) {
                        AsyncImage(
                            model = item.vote.authorIcon,
                            contentDescription = "作者頭像",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.vote.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "2026/09/05",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                if (item.vote.isNeedPassword) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "私密投票",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (item.vote.isVoted) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FunnyVoteBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "已投票",
                            color = FunnyVoteBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Title (txtTitle)
            Text(
                text = item.vote.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // 2.1 封面圖 (若有)
            if (!item.vote.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = item.vote.imageUrl,
                    contentDescription = "投票封面",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Function Bar (include_function_bar.xml)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp))
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 投票數
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Poll,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = FunnyVoteBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Vote: ${item.vote.totalVotedCount}", fontSize = 12.sp, color = TextPrimary)
                }

                // 收藏
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onFavoriteToggle)
                ) {
                    Icon(
                        imageVector = if (item.vote.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (item.vote.isFavorite) StarGold else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.vote.isFavorite) "已收藏" else "收藏",
                        fontSize = 12.sp,
                        color = if (item.vote.isFavorite) StarGold else TextPrimary
                    )
                }

                // 分享
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onShareClick)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = FunnyVoteBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "分享", fontSize = 12.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Options List (btnFirstOption, btnSecondOption, btnThirdOption MORE)
            item.options.take(2).forEachIndexed { index, option ->
                val isFirst = index == 0
                val bgColor = if (isFirst) Option1Background else Option2Background
                val numBgColor = if (isFirst) Option1NumberBg else Option2NumberBg
                val ratio = (option.count.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
                val isChampion = option.count > 0 && option.count == maxVoteCount

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = CircleShape,
                                color = numBgColor
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = option.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (option.isUserChoiced) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "已投此項",
                                    tint = FunnyVoteBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Champion Icon + Progress Bar + Percent
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isChampion) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "最高票",
                                    tint = StarGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            } else {
                                Spacer(modifier = Modifier.width(22.dp))
                            }

                            val animatedRatio by animateFloatAsState(
                                targetValue = ratio,
                                label = "wall_ratio_anim"
                            )

                            LinearProgressIndicator(
                                progress = animatedRatio,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp)),
                                color = if (isChampion) ProgressBarChampion else ProgressBarNormal,
                                trackColor = ProgressBarTrack
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            val percent = String.format(Locale.getDefault(), "%.1f%%", ratio * 100)
                            Text(
                                text = percent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // MORE 按鈕 (btnThirdOption)
            if (item.options.size > 2) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Option2Background),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MORE (${item.options.size} 個選項) +",
                            fontWeight = FontWeight.Bold,
                            color = FunnyVoteBlue,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ----------------- Previews -----------------

@Preview(showBackground = true)
@Composable
fun HomeScreenClassicPreview() {
    val sample = VoteWithDetails(
        vote = VoteEntity(
            voteCode = "v1",
            title = "Do your mother know what you do in front of computer?",
            authorName = "Heaton Lin",
            totalVotedCount = 120,
            isFavorite = true
        ),
        options = listOf(
            OptionEntity(voteCode = "v1", optionCode = "o1", title = "Yes, I have told her", count = 90),
            OptionEntity(voteCode = "v1", optionCode = "o2", title = "No, absolutely a secret", count = 30),
            OptionEntity(voteCode = "v1", optionCode = "o3", title = "She doesn't care", count = 0)
        )
    )
    HomeScreenContent(
        uiState = HomeUiState(votes = listOf(sample)),
        onIntent = {},
        onVoteClick = {},
        onCreateClick = {},
        onProfileClick = {}
    )
}
