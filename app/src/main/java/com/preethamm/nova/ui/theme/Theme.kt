package com.preethamm.nova.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme: ColorScheme = darkColorScheme(
    primary = NovaTeal,
    secondary = NovaBlue,
    tertiary = NovaAmber,
    background = NovaInk,
    surface = NovaPanel,
    error = NovaRose
)

private val LightScheme: ColorScheme = lightColorScheme(
    primary = ColorTokens.LightPrimary,
    secondary = ColorTokens.LightSecondary,
    tertiary = ColorTokens.LightTertiary,
    background = ColorTokens.LightBackground,
    surface = ColorTokens.LightSurface,
    error = NovaRose
)

@Composable
fun NovaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = Typography,
        content = content
    )
}

private object ColorTokens {
    val LightPrimary = androidx.compose.ui.graphics.Color(0xFF006B5B)
    val LightSecondary = androidx.compose.ui.graphics.Color(0xFF315E9D)
    val LightTertiary = androidx.compose.ui.graphics.Color(0xFF7D5700)
    val LightBackground = androidx.compose.ui.graphics.Color(0xFFF7F9FC)
    val LightSurface = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
}
