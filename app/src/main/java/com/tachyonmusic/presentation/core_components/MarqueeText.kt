package com.tachyonmusic.presentation.core_components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import kotlinx.coroutines.isActive

/**
 * https://github.com/victorbrndls/BlogProjects/blob/marquee/app/src/main/java/com/victorbrandalise/MarqueeText.kt
 */
@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    gradientEdgeColor: Color = Theme.colors.primary,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    linHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val createText = @Composable { localModifier: Modifier ->
        Text(
            text = text,
            textAlign = textAlign,
            modifier = localModifier,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            lineHeight = linHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = 1,
            onTextLayout = onTextLayout,
            style = style
        )
    }

    var xOffset by remember { mutableStateOf(0) }
    val textLayoutInfoState = remember { mutableStateOf<TextLayoutInfo?>(null) }

    LaunchedEffect(textLayoutInfoState.value) {
        val textLayoutInfo = textLayoutInfoState.value ?: return@LaunchedEffect
        if (textLayoutInfo.textWidth <= textLayoutInfo.containerWidth) return@LaunchedEffect

        val duration = 7500 * textLayoutInfo.textWidth / textLayoutInfo.containerWidth

        val animation = TargetBasedAnimation(
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = duration,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            typeConverter = Int.VectorConverter,
            initialValue = 0,
            targetValue = -textLayoutInfo.textWidth
        )

        val startTime = withFrameNanos { it }

        do {
            val playTime = withFrameNanos { it } - startTime
            xOffset = animation.getValueFromNanos(playTime)
            delay(5.ms) // TODO: should be a setting
        } while (isActive)
    }

    SubcomposeLayout(
        modifier = modifier.clipToBounds()
    ) { constraints ->
        val infiniteWidthConstraints = constraints.copy(maxWidth = Int.MAX_VALUE)

        var mainText = subcompose(MarqueeLayers.MainText) {
            createText(Modifier)
        }.first().measure(infiniteWidthConstraints)

        var gradient: Placeable? = null
        var secondPlaceableWithOffset: Pair<Placeable, Int>? = null

        if (mainText.width <= constraints.maxWidth) {
            mainText = subcompose(MarqueeLayers.SecondaryText) {
                createText(Modifier.fillMaxWidth())
            }.first().measure(constraints)
            textLayoutInfoState.value = null
        } else {
            val spacing = constraints.maxWidth * 2 / 3
            textLayoutInfoState.value = TextLayoutInfo(
                textWidth = mainText.width + spacing,
                containerWidth = constraints.maxWidth
            )
            val secondTextOffset = mainText.width + xOffset + spacing
            val secondTextSpace = constraints.maxWidth - secondTextOffset

            if (secondTextSpace > 0) {
                secondPlaceableWithOffset = subcompose(MarqueeLayers.SecondaryText) {
                    createText(Modifier)
                }.first()
                    .measure(infiniteWidthConstraints) to secondTextOffset
            }
            gradient = subcompose(MarqueeLayers.EdgesGradient) {
                Gradient(gradientEdgeColor)
            }.first().measure(constraints = constraints.copy(maxHeight = mainText.height))
        }

        layout(
            width = constraints.maxWidth,
            height = mainText.height
        ) {
            mainText.place(xOffset, 0)
            secondPlaceableWithOffset?.let {
                it.first.place(it.second, 0)
            }
            gradient?.place(0, 0)
        }
    }
}

@Composable
private fun Gradient(gradientEdgeColor: Color) {
    Row {
        GradientEdge(startColor = gradientEdgeColor, endColor = Color.Transparent)
        Spacer(modifier = Modifier.weight(1f))
        GradientEdge(startColor = Color.Transparent, endColor = gradientEdgeColor)
    }
}

@Composable
private fun GradientEdge(
    startColor: Color,
    endColor: Color
) {
    Box(
        modifier = Modifier
            .width(10.dp)
            .fillMaxHeight()
            .background(
                brush = Brush.horizontalGradient(
                    0f to startColor,
                    1f to endColor
                )
            )
    )
}

internal enum class MarqueeLayers {
    MainText,
    SecondaryText,
    EdgesGradient
}

internal data class TextLayoutInfo(
    val textWidth: Int,
    val containerWidth: Int
)