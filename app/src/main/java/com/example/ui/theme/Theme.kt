package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SteelBlue,
    secondary = EmeraldGreen,
    tertiary = SlateNavy,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = LightBackground,
    onSecondary = LightBackground,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onTertiary = DarkTextPrimary,
    primaryContainer = DarkSurface,
    secondaryContainer = SlateNavy
)

private val LightColorScheme = lightColorScheme(
    primary = SlateNavy,
    secondary = SteelBlue,
    tertiary = EmeraldGreen,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightBackground,
    onSecondary = LightBackground,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onTertiary = LightTextPrimary,
    primaryContainer = LightSurface,
    secondaryContainer = LightBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
