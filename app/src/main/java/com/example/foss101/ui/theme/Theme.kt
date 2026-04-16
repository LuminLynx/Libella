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
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    error = Error,
    onError = OnError
)

private val DarkColors = darkColorScheme(
    primary = PrimaryContainer,
    onPrimary = OnPrimaryContainer,
    primaryContainer = Primary,
    onPrimaryContainer = OnPrimary,
    secondary = SecondaryContainer,
    onSecondary = OnSecondaryContainer,
    secondaryContainer = Secondary,
    onSecondaryContainer = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = ColorDarkBackground,
    onBackground = ColorDarkOnBackground,
    surface = ColorDarkSurface,
    surfaceVariant = ColorDarkSurfaceVariant,
    onSurface = ColorDarkOnSurface,
    onSurfaceVariant = ColorDarkOnSurfaceVariant,
    outline = ColorDarkOutline,
    error = Error,
    onError = OnError
)

@Composable
fun Foss101Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

private val ColorDarkBackground = androidx.compose.ui.graphics.Color(0xFF10131C)
private val ColorDarkOnBackground = androidx.compose.ui.graphics.Color(0xFFE2E8F6)
private val ColorDarkSurface = androidx.compose.ui.graphics.Color(0xFF10131C)
private val ColorDarkSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF2A3040)
private val ColorDarkOnSurface = androidx.compose.ui.graphics.Color(0xFFE2E8F6)
private val ColorDarkOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC1C8DB)
private val ColorDarkOutline = androidx.compose.ui.graphics.Color(0xFF8A92A8)
