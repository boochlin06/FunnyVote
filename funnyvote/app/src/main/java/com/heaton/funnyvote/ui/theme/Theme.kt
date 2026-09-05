package com.heaton.funnyvote.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FunnyVoteColorScheme = lightColorScheme(
    primary = FunnyVoteBlue,
    onPrimary = Color.White,
    primaryContainer = FunnyVoteBlueDark,
    onPrimaryContainer = Color.White,
    secondary = FunnyVoteBlueLight,
    onSecondary = Color.Black,
    background = FunnyVoteWindowBg,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = TextSecondary
)

@Composable
fun FunnyVoteTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = FunnyVoteBlueDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = FunnyVoteColorScheme,
        typography = Typography,
        content = content
    )
}
