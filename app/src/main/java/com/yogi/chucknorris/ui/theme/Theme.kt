package com.yogi.chucknorris.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB34322),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCD),
    onPrimaryContainer = Color(0xFF3A0B00),
    secondary = Color(0xFF236A64),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7ECE6),
    onSecondaryContainer = Color(0xFF00201D),
    tertiary = Color(0xFF6C5F18),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5E58E),
    onTertiaryContainer = Color(0xFF211B00),
    background = Color(0xFFFFFBF8),
    onBackground = Color(0xFF211A17),
    surface = Color(0xFFFFFBF8),
    onSurface = Color(0xFF211A17),
    surfaceContainerHigh = Color(0xFFF5EAE4),
    surfaceContainerHighest = Color(0xFFECE0DA),
    outlineVariant = Color(0xFFD8C2B9)
)

@Composable
fun ChuckNorrisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
