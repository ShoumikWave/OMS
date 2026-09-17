package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OfsRed,
    onPrimary = Color.White,
    primaryContainer = OfsRedDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF94A3B8),
    onSecondary = OfsDarkBg,
    background = OfsDarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = OfsDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = OfsRed,
    onPrimary = Color.White,
    primaryContainer = OfsRedLight,
    onPrimaryContainer = OfsRedDark,
    secondary = OfsBlack,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDF2F7),
    onSecondaryContainer = OfsBlack,
    background = OfsBgLight,
    onBackground = OfsBlack,
    surface = OfsCardLight,
    onSurface = OfsBlack,
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = OfsSlate,
    outline = OfsLine
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
