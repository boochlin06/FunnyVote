package com.heaton.funnyvote.ui.about

import androidx.compose.runtime.Composable

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAboutApp: () -> Unit = {},
    onNavigateToTutorial: () -> Unit = {},
    onNavigateToAuthorInfo: () -> Unit = {},
    onNavigateToLicence: () -> Unit = {},
    onNavigateToProblem: () -> Unit = {}
) {
    AboutScreenContent(
        onNavigateBack = onNavigateBack,
        onNavigateToAboutApp = onNavigateToAboutApp,
        onNavigateToTutorial = onNavigateToTutorial,
        onNavigateToAuthorInfo = onNavigateToAuthorInfo,
        onNavigateToLicence = onNavigateToLicence,
        onNavigateToProblem = onNavigateToProblem
    )
}
