package com.yogi.chucknorris.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB59D),
    onPrimary = Color(0xFF5E1600),
    primaryContainer = Color(0xFF872A10),
    onPrimaryContainer = Color(0xFFFFDBCD),
    secondary = Color(0xFF9FD0C9),
    onSecondary = Color(0xFF003733),
    secondaryContainer = Color(0xFF064F4A),
    onSecondaryContainer = Color(0xFFC7ECE6),
    tertiary = Color(0xFFD8C974),
    onTertiary = Color(0xFF393000),
    tertiaryContainer = Color(0xFF524700),
    onTertiaryContainer = Color(0xFFF5E58E),
    background = Color(0xFF181210),
    onBackground = Color(0xFFEDE0DA),
    surface = Color(0xFF181210),
    onSurface = Color(0xFFEDE0DA),
    surfaceContainerHigh = Color(0xFF2A211E),
    surfaceContainerHighest = Color(0xFF352B27),
    outlineVariant = Color(0xFF56433C)
)

@Composable
fun ChuckNorrisTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
