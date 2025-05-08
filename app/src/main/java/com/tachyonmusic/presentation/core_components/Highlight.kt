package com.tachyonmusic.presentation.core_components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize

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

fun Modifier.onHighlightPositioned(
    rootOffset: Offset,
    condition: Boolean,
    onHighlightPositioned: (Rect, Int) -> Unit
) = then(
    Modifier.onGloballyPositioned {
        if (condition) {
            // get the child’s window position...
            val childWindowPos = it.localToWindow(Offset.Zero)
            // ...then subtract the root’s window offset to get a position
            // relative to the Box canvas:
            val topLeft = childWindowPos - rootOffset
            val size = it.size.toSize()
            onHighlightPositioned(Rect(topLeft, size), it.size.height)
        }
    }
)

fun Rect.inflate(horizontalPx: Float = 0f, verticalPx: Float = 0f) = copy(
    left = left - horizontalPx,
    top = top - verticalPx,
    bottom = bottom + verticalPx,
    right = right + horizontalPx
)

private fun Rect.toRoundRect(radius: CornerRadius, padding: Float = 0f) =
    RoundRect(left + padding, top + padding, right + padding, bottom + padding, radius)