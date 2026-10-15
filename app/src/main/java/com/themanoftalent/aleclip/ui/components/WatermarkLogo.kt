package com.themanoftalent.aleclip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.themanoftalent.aleclip.ui.theme.SpectrumColors

@Composable
fun SpectrumLogoBadge(size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(
                color = SpectrumColors.Azure.copy(alpha = 0.15f),
                shape = RoundedCornerShape((size * 0.28).dp)
            )
            .border(
                width = 2.dp,
                brush = SpectrumColors.HorizontalGradient,
                shape = RoundedCornerShape((size * 0.28).dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AttachFile,
            contentDescription = "AleClip Logo",
            tint = SpectrumColors.Cyan,
            modifier = Modifier
                .size((size * 0.55).dp)
                .rotate(-45f)
        )
    }
}

@Composable
fun WatermarkLogo(compact: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 16.dp)
    ) {
        SpectrumLogoBadge(size = if (compact) 36 else 52)

        Column(verticalArrangement = Arrangement.spacedBy((-2).dp)) {
            Text(
                text = "ALE",
                fontSize = if (compact) 26.sp else 40.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = SpectrumColors.Cyan
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "CLIP",
                    fontSize = if (compact) 9.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    color = SpectrumColors.Azure
                )
                Box(
                    modifier = Modifier
                        .width(if (compact) 18.dp else 24.dp)
                        .height(2.dp)
                        .background(
                            brush = SpectrumColors.HorizontalGradient,
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}
