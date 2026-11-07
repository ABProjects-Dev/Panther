package com.panther.app.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PantherExtendedColors(
    val activeGreen: Color,
    val activeGreenContainer: Color,
    val onActiveGreen: Color,
)

val LocalPantherColors = staticCompositionLocalOf {
    PantherExtendedColors(
        activeGreen = PantherActiveGreenDark,
        activeGreenContainer = PantherActiveGreenContainerDark,
        onActiveGreen = PantherOnActiveGreenDark,
    )
}

private val DarkColors = darkColorScheme(
    primary = PantherLime,
    onPrimary = PantherBackground,
    background = PantherBackground,
    onBackground = PantherWhite,
    surface = PantherSurface,
    onSurface = PantherWhite,
    surfaceVariant = PantherSurfaceVariant,
    onSurfaceVariant = PantherGray,
)

private val LightColors = lightColorScheme(
    primary = PantherLightPrimary,
    onPrimary = PantherWhite,
    background = PantherLightBackground,
    onBackground = PantherLightText,
    surface = PantherLightSurface,
    onSurface = PantherLightText,
    surfaceVariant = PantherLightSurfaceVariant,
    onSurfaceVariant = PantherLightSubtleText,
)

private val DarkExtendedColors = PantherExtendedColors(
    activeGreen = PantherActiveGreenDark,
    activeGreenContainer = PantherActiveGreenContainerDark,
    onActiveGreen = PantherOnActiveGreenDark,
)

private val LightExtendedColors = PantherExtendedColors(
    activeGreen = PantherActiveGreenLight,
    activeGreenContainer = PantherActiveGreenContainerLight,
    onActiveGreen = PantherOnActiveGreenLight,
)

@Composable
fun PantherTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
    CompositionLocalProvider(LocalPantherColors provides extendedColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = PantherTypography,
            shapes = PantherShapes,
            content = content,
        )
    }
}
