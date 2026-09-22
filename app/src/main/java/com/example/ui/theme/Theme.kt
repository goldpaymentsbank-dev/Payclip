package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00363D),
    onPrimaryContainer = NeonCyan,
    secondary = WarmGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF422F00),
    onSecondaryContainer = WarmGold,
    tertiary = NeonPurple,
    onTertiary = Color.White,
    background = FuturisticBlack,
    onBackground = TextWhitePrimary,
    surface = FuturisticDarkSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = Color(0xFF1E1E28),
    onSurfaceVariant = TextWhiteSecondary,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
