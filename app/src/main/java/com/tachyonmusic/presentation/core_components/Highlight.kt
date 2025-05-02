package com.tachyonmusic.presentation.core_components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Fill

fun Modifier.highlight(holeBounds: Rect?) =
    then(
        Modifier.drawWithContent {
            drawContent()
            holeBounds?.let { rect ->
                val path = Path().apply {
                    // full screen
                    addRect(Rect(0f, 0f, size.width, size.height))
                    // the “hole”
                    addRoundRect(rect.toRoundRect(CornerRadius(25f)))
                    // even-odd means “subtract” the second rect from the first
                    fillType = PathFillType.EvenOdd
                }
                drawPath(
                    path = path,
                    color = Color(0x99000000),
                    style = Fill
                )
            }
        }
    )

private fun Rect.toRoundRect(radius: CornerRadius, padding: Float = 0f) =
    RoundRect(left + padding, top + padding, right + padding, bottom + padding, radius)