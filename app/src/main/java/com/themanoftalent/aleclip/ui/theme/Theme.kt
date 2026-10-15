package com.themanoftalent.aleclip.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SpectrumColors.Azure,
    secondary = SpectrumColors.Cyan,
    tertiary = SpectrumColors.Mint,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceCard
)

private val LightColorScheme = lightColorScheme(
    primary = SpectrumColors.Azure,
    secondary = SpectrumColors.Cyan,
    tertiary = SpectrumColors.Emerald,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceCard
)

@Composable
fun AleClipTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
