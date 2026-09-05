package com.heaton.funnyvote.ui.about.sub

import androidx.compose.foundation.background
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

data class LicenceItem(val title: String, val desc: String)

val licenceList = listOf(
    LicenceItem(
        "Jetpack Compose & Material 3",
        "Copyright 2024 The Android Open Source Project\nLicensed under the Apache License, Version 2.0."
    ),
    LicenceItem(
        "Dagger Hilt",
        "Copyright 2024 Google Inc.\nLicensed under the Apache License, Version 2.0."
    ),
    LicenceItem(
        "Room Persistence Library",
        "Copyright 2024 The Android Open Source Project\nLicensed under the Apache License, Version 2.0."
    ),
    LicenceItem(
        "Kotlin Coroutines & Flow",
        "Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors."
    ),
    LicenceItem(
        "Retrofit & OkHttp",
        "Copyright 2013-2024 Square, Inc.\nLicensed under the Apache License, Version 2.0."
    ),
    LicenceItem(
        "CircleImageView",
        "Copyright 2014 - 2020 Henning Dodenhof\nLicensed under the Apache License, Version 2.0."
    ),
    LicenceItem(
        "RoundCornerProgressBar",
        "Copyright 2015 Akexorcist\nLicensed under the Apache License, Version 2.0."
    ),
    LicenceItem(
        "AutoScrollViewPager",
        "Copyright 2014 angeldevil.me\nLicensed under the Apache License, Version 2.0."
    ),
    LicenceItem(
        "ShowcaseView",
        "Copyright Alex Curran (@amlcurran) © 2012-2014. All rights reserved."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenceScreen(
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
                title = { Text("開放原始碼授權 (Licence)", fontWeight = FontWeight.Bold) },
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
            items(licenceList) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FunnyVoteBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.desc,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
