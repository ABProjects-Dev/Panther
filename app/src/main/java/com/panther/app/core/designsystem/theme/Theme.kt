package com.panther.app.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = PantherLime,
    onPrimary = PantherBackground,
    background = PantherBackground,
    onBackground = PantherWhite,
    surface = PantherSurface,
    onSurface = PantherWhite,
    onSurfaceVariant = PantherGray,
)
private val LightColors = lightColorScheme(
    primary = PantherLightPrimary,
    onPrimary = PantherWhite,
    background = PantherLightBackground,
    onBackground = PantherLightText,
    surface = PantherLightSurface,
    onSurface = PantherLightText,
)

@Composable
fun PantherTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PantherTypography,
        shapes = PantherShapes,
        content = content,
    )
}
