package com.tachyonmusic.presentation.player.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun ProgressIndicator(
    progress: Float,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width

        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(x = 0f, y = -10f),
            size = Size(width = width, height = 10f),
            cornerRadius = CornerRadius(100f, 100f)
        )

        drawRoundRect(
            color = color,
            topLeft = Offset(x = 0f, y = -10f),
            size = Size(width = width * progress, height = 10f),
            cornerRadius = CornerRadius(100f, 100f)
        )
    }
}