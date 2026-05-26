package com.yogi.quotebattleroyal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C59),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F3DD),
    onPrimaryContainer = Color(0xFF002117),
    secondary = Color(0xFF006A61),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA5F0E4),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFF7651A6),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEBDDFF),
    onTertiaryContainer = Color(0xFF2D104F),
    background = Color(0xFFF7FBF8),
    onBackground = Color(0xFF181D1A),
    surface = Color(0xFFF7FBF8),
    onSurface = Color(0xFF181D1A),
    surfaceContainerHigh = Color(0xFFE7EFEB),
    surfaceContainerHighest = Color(0xFFDDE7E2),
    outline = Color(0xFF62706A),
    outlineVariant = Color(0xFFC0CBC6),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8FF0B0),
    onPrimary = Color(0xFF062112),
    primaryContainer = Color(0xFF1F4633),
    onPrimaryContainer = Color(0xFFC6F8D5),
    secondary = Color(0xFF2BC7B3),
    onSecondary = Color(0xFF00201C),
    secondaryContainer = Color(0xFF005B50),
    onSecondaryContainer = Color(0xFFB4F2E7),
    tertiary = Color(0xFFD7A6FF),
    onTertiary = Color(0xFF341056),
    tertiaryContainer = Color(0xFF4F2A73),
    onTertiaryContainer = Color(0xFFEBDDFF),
    background = Color(0xFF111314),
    onBackground = Color(0xFFF2F4F3),
    surface = Color(0xFF111314),
    onSurface = Color(0xFFF2F4F3),
    surfaceContainerLow = Color(0xFF15191A),
    surfaceContainer = Color(0xFF181D1F),
    surfaceContainerHigh = Color(0xFF1B1F22),
    surfaceContainerHighest = Color(0xFF242A2E),
    outline = Color(0xFF6F7B76),
    outlineVariant = Color(0xFF333B40),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
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
