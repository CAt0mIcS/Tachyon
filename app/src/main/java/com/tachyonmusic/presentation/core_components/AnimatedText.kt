package com.tachyonmusic.presentation.core_components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.tachyonmusic.presentation.theme.Theme


/**
 * First measures the width of the [Text] composable and if that width exceeds bounds it will
 * add the gradient and [Modifier.basicMarquee] to the text. Otherwise a default [Text] composable
 * will be drawn.
 */
@Composable
fun AnimatedText(
    text: String,
    modifier: Modifier = Modifier,
    gradientEdgeColor: Color = MaterialTheme.colorScheme.primary,
    gradientWidth: Dp = 12.dp,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = 1,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {

    if (Theme.settings.animateText)
        MarqueeTextInternal(
            text,
            modifier,
            gradientEdgeColor,
            gradientWidth,
            color,
            fontSize,
            fontStyle,
            fontWeight,
            fontFamily,
            letterSpacing,
            textDecoration,
            textAlign,
            lineHeight,
            maxLines,
            minLines,
            onTextLayout,
            style
        )
    else
        Text(
            text,
            modifier,
            color,
            fontSize,
            fontStyle,
            fontWeight,
            fontFamily,
            letterSpacing,
            textDecoration,
            textAlign,
            lineHeight,
            TextOverflow.Ellipsis,
            true,
            maxLines,
            minLines,
            onTextLayout,
            style
        )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MarqueeTextInternal(
    text: String,
    modifier: Modifier,
    gradientEdgeColor: Color,
    gradientWidth: Dp,
    color: Color,
    fontSize: TextUnit,
    fontStyle: FontStyle?,
    fontWeight: FontWeight?,
    fontFamily: FontFamily?,
    letterSpacing: TextUnit,
    textDecoration: TextDecoration?,
    textAlign: TextAlign?,
    lineHeight: TextUnit,
    maxLines: Int,
    minLines: Int,
    onTextLayout: (TextLayoutResult) -> Unit,
    style: TextStyle,
) {
    var gradientHeight by remember { mutableStateOf<Dp?>(null) }

    val createText: @Composable (Modifier) -> Unit = {
        Text(
            text,
            it,
            color,
            fontSize,
            fontStyle,
            fontWeight,
            fontFamily,
            letterSpacing,
            textDecoration,
            textAlign,
            lineHeight,
            TextOverflow.Clip,
            softWrap = true,
            maxLines,
            minLines,
            onTextLayout,
            style
        )
    }

    SubcomposeLayout(
        modifier = Modifier.clipToBounds()
    ) { constraints ->
        val infiniteWidthConstraints = constraints.copy(maxWidth = Int.MAX_VALUE)

        val placeable = subcompose(0) {
            createText(Modifier)
        }.first().measure(infiniteWidthConstraints)
        gradientHeight = if (placeable.width >= constraints.maxWidth) {
            // requires gradient
            placeable.height.toDp()
        } else null

        layout(
            width = constraints.maxWidth,
            height = 0
        ) {}
    }

    if (gradientHeight != null) {
        Box(modifier) {
            createText(Modifier.basicMarquee(Int.MAX_VALUE))
            Gradient(gradientHeight ?: return@Box, gradientEdgeColor, gradientWidth)
        }
    } else
        createText(modifier)
}

@Composable
private fun Gradient(height: Dp, gradientEdgeColor: Color, width: Dp) {
    Row(Modifier.height(height)) {
        GradientEdge(startColor = gradientEdgeColor, endColor = Color.Transparent, width = width)
        Spacer(modifier = Modifier.weight(1f))
        GradientEdge(startColor = Color.Transparent, endColor = gradientEdgeColor, width = width)
    }
}

@Composable
private fun GradientEdge(
    startColor: Color,
    endColor: Color,
    width: Dp
) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(
                brush = Brush.horizontalGradient(
                    0f to startColor,
                    1f to endColor
                )
            )
    )
}