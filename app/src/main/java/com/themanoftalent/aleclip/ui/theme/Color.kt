package com.themanoftalent.aleclip.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object SpectrumColors {
    // 100% Spectrum, ZERO PINK!
    val Cyan = Color(0xFF00F0FF)      // Electric Cyan
    val Azure = Color(0xFF0080FF)     // Cobalt Azure
    val Mint = Color(0xFF00F5A0)      // Neon Mint
    val Emerald = Color(0xFF00D26A)   // Emerald Green
    val Lime = Color(0xFF76FF03)      // Electric Lime
    val Gold = Color(0xFFFFD600)      // Sunburst Gold
    val Tangerine = Color(0xFFFF8C00) // Sunset Tangerine

    val GradientColors = listOf(Cyan, Azure, Mint, Emerald, Lime, Gold, Tangerine)

    val HorizontalGradient: Brush
        get() = Brush.horizontalGradient(GradientColors)

    val VerticalGradient: Brush
        get() = Brush.verticalGradient(GradientColors)
}

val DarkBackground = Color(0xFF0B1120)
val DarkSurface = Color(0xFF131D31)
val DarkSurfaceCard = Color(0xFF1A2640)
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceCard = Color(0xFFF1F5F9)
