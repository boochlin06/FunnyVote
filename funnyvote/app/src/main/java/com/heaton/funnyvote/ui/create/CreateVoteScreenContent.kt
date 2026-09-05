package com.heaton.funnyvote.ui.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoteScreenContent(
    uiState: CreateVoteUiState,
    onIntent: (CreateVoteIntent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("發起全新投票", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 標題輸入卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. 投票主題與標題",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { onIntent(CreateVoteIntent.UpdateTitle(it)) },
                        label = { Text("請輸入吸引人的投票標題...") },
                        placeholder = { Text("例如：大家週末想要去哪裡露營？") },
                        isError = uiState.titleError != null,
                        supportingText = uiState.titleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 選項設定卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. 投票選項 (2-10 項)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { onIntent(CreateVoteIntent.AddOption) },
                            enabled = uiState.options.size < 10
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("新增選項")
                        }
                    }

                    if (uiState.optionsError != null) {
                        Text(
                            text = uiState.optionsError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    uiState.options.forEachIndexed { index, optionText ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = optionText,
                                onValueChange = { onIntent(CreateVoteIntent.UpdateOption(index, it)) },
                                label = { Text("選項 ${index + 1}") },
                                placeholder = { Text("輸入選項內容") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.options.size > 2) {
                                IconButton(
                                    onClick = { onIntent(CreateVoteIntent.RemoveOption(index)) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "刪除選項",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 高級設定卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. 規則與隱私設定",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 複選開關
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("允許多選 (複選投票)", fontWeight = FontWeight.Medium)
                            Text(
                                "開啟後參與者可選擇一個以上的選項",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = uiState.isMultiChoice,
                            onCheckedChange = { onIntent(CreateVoteIntent.ToggleMultiChoice(it)) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // 私密密碼開關
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("私密加密投票", fontWeight = FontWeight.Medium)
                            Text(
                                "開啟後需輸入密碼才可檢視與投票",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = uiState.isPrivate,
                            onCheckedChange = { onIntent(CreateVoteIntent.TogglePrivate(it)) }
                        )
                    }

                    AnimatedVisibility(visible = uiState.isPrivate) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = { onIntent(CreateVoteIntent.UpdatePassword(it)) },
                                label = { Text("設定投票通行密碼") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                isError = uiState.passwordError != null,
                                supportingText = uiState.passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 送出按鈕
            Button(
                onClick = { onIntent(CreateVoteIntent.Submit) },
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("立即發起投票", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ----------------- Previews -----------------

@Preview(showBackground = true)
@Composable
fun CreateVoteScreenDefaultPreview() {
    CreateVoteScreenContent(
        uiState = CreateVoteUiState(),
        onIntent = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true)
@Composable
fun CreateVoteScreenFilledPreview() {
    CreateVoteScreenContent(
        uiState = CreateVoteUiState(
            title = "週末露營地點表決",
            options = listOf("宜蘭礁溪", "新竹尖石", "南投清境"),
            isMultiChoice = true,
            isPrivate = true,
            password = "pass"
        ),
        onIntent = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true)
@Composable
fun CreateVoteScreenErrorPreview() {
    CreateVoteScreenContent(
        uiState = CreateVoteUiState(
            title = "",
            titleError = "投票標題不能為空！",
            options = listOf("", ""),
            optionsError = "請至少填寫 2 個有效選項！"
        ),
        onIntent = {},
        onNavigateBack = {}
    )
}
