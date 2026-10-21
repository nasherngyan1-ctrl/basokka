package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BasoDarkColorScheme = darkColorScheme(
    primary = BasoCyan,
    onPrimary = BasoDarkOnAccent,
    primaryContainer = BasoCardElevated,
    onPrimaryContainer = BasoTextWhite,
    secondary = BasoGold,
    onSecondary = BasoDarkOnAccent,
    secondaryContainer = BasoCardSurface,
    onSecondaryContainer = BasoGoldBright,
    tertiary = BasoSuccessGreen,
    onTertiary = BasoDarkOnAccent,
    background = BasoBgDark,
    onBackground = BasoTextWhite,
    surface = BasoCardSurface,
    onSurface = BasoTextWhite,
    surfaceVariant = BasoCardElevated,
    onSurfaceVariant = BasoTextMuted,
    outline = BasoBorderCyan,
    error = BasoErrorRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BasoDarkColorScheme,
        typography = Typography,
        content = content
    )
}
