package com.heaton.funnyvote.ui.about

import androidx.compose.runtime.Composable

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTutorial: () -> Unit = {}
) {
    AboutScreenContent(
        onNavigateBack = onNavigateBack,
        onNavigateToTutorial = onNavigateToTutorial
    )
}
