package com.themanoftalent.aleclip.model

import androidx.compose.ui.graphics.Color
import com.themanoftalent.aleclip.ui.theme.SpectrumColors

enum class ClipType(val idName: String) {
    TEXT("text"),
    LINK("link"),
    COLOR("color"),
    IMAGE("image"),
    FILE("file");

    val accentColor: Color
        get() = when (this) {
            TEXT -> SpectrumColors.Azure
            LINK -> SpectrumColors.Cyan
            COLOR -> SpectrumColors.Tangerine // Vibrant Tangerine Orange - ZERO PINK!
            IMAGE -> SpectrumColors.Lime
            FILE -> SpectrumColors.Gold
        }

    val displayName: String
        get() = when (this) {
            TEXT -> "Text"
            LINK -> "Link"
            COLOR -> "Color"
            IMAGE -> "Image"
            FILE -> "File"
        }

    companion object {
        fun fromString(type: String?): ClipType {
            return entries.firstOrNull { it.idName.equals(type, ignoreCase = true) } ?: TEXT
        }
    }
}
