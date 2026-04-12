package com.example.foss101.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    surface = Surface,
    onSurface = OnSurface
)

private val DarkColors = darkColorScheme(
    primary = PrimaryContainer,
    onPrimary = OnPrimaryContainer,
    primaryContainer = Primary,
    onPrimaryContainer = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary
)

@Composable
fun Foss101Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
