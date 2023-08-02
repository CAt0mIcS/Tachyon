package com.tachyonmusic.presentation.core_components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.tachyonmusic.presentation.theme.Theme
import kotlin.math.roundToInt

/**
 * DIFFERENT IMPLEMENTATION:
 *  https://medium.com/mobile-app-development-publication/lessons-learned-after-3-days-debugging-jetpack-compose-swipetodismiss-e058d71f7374
 */


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeDelete(
    modifier: Modifier = Modifier,
    primaryBackgroundColor: Color = Theme.colors.primary,
    shape: Shape = RectangleShape,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    var freezeOffset by remember { mutableStateOf(false) }
    val state = rememberDismissState {
        freezeOffset = it == DismissValue.DismissedToStart
        true
    }

    val animatedAlpha by animateFloatAsState(
        if (state.isDismissed(DismissDirection.EndToStart)
            || state.isDismissed(DismissDirection.StartToEnd)
        ) 1f // 0f
        else 1f,
        tween(Theme.animation.short)
    )

    SwipeToDismiss(
        state = state,
        modifier = modifier,
        directions = setOf(
            DismissDirection.EndToStart
        ),
        dismissThreshold = .25f,
        freezeOffset = freezeOffset,
        background = {
            state.dismissDirection ?: return@SwipeToDismiss

            val color by animateColorAsState(
                when (state.targetValue) {
                    DismissValue.Default -> primaryBackgroundColor
                    else -> Color.Red
                },
                animationSpec = tween(Theme.animation.long)
            )

            val scale by animateFloatAsState(
                if (state.targetValue == DismissValue.Default) 1f else 1.4f,
                tween(Theme.animation.long)
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, shape)
                    .padding(horizontal = Dp(15f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Icon",
                        modifier = Modifier.scale(scale)
                    )
                }
            }
        },
        dismissContent = {
            val scope = this
            Box(modifier = Modifier.graphicsLayer { alpha = animatedAlpha }) {
                scope.content()
            }
        }
    )
}

/**
 * A composable that can be dismissed by swiping left or right.
 *
 * @sample androidx.compose.material.samples.SwipeToDismissListItems
 *
 * @param state The state of this component.
 * @param modifier Optional [Modifier] for this component.
 * @param directions The set of directions in which the component can be dismissed.
 * @param dismissThresholds The thresholds the item needs to be swiped in order to be dismissed.
 * @param background A composable that is stacked behind the content and is exposed when the
 * content is swiped. You can/should use the [state] to have different backgrounds on each side.
 * @param dismissContent The content that can be dismissed.
 */
@Composable
@ExperimentalMaterialApi
private fun SwipeToDismiss(
    state: DismissState,
    modifier: Modifier = Modifier,
    directions: Set<DismissDirection> = setOf(
        DismissDirection.EndToStart,
        DismissDirection.StartToEnd
    ),
    dismissThreshold: Float,
    freezeOffset: Boolean,
    background: @Composable RowScope.() -> Unit,
    dismissContent: @Composable RowScope.() -> Unit
) = BoxWithConstraints(modifier) {
    val width = constraints.maxWidth.toFloat()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var lastOffset by remember { mutableStateOf(Offset.Zero) }

    val anchors = mutableMapOf(0f to DismissValue.Default)
    if (DismissDirection.StartToEnd in directions) anchors += width to DismissValue.DismissedToEnd
    if (DismissDirection.EndToStart in directions) anchors += -width to DismissValue.DismissedToStart

    val thresholds = { from: DismissValue, to: DismissValue ->
        FractionalThreshold(dismissThreshold)
    }
    val minFactor =
        if (DismissDirection.EndToStart in directions) SwipeableDefaults.StandardResistanceFactor else SwipeableDefaults.StiffResistanceFactor
    val maxFactor =
        if (DismissDirection.StartToEnd in directions) SwipeableDefaults.StandardResistanceFactor else SwipeableDefaults.StiffResistanceFactor
    Box(
        Modifier.swipeable(
            state = state,
            anchors = anchors,
            thresholds = thresholds,
            orientation = Orientation.Horizontal,
            enabled = true, //state.currentValue == DismissValue.Default
            reverseDirection = isRtl,
            resistance = ResistanceConfig(
                basis = width,
                factorAtMin = minFactor,
                factorAtMax = maxFactor
            )
        )
    ) {
        Row(
            content = background,
            modifier = Modifier.matchParentSize()
        )
        Row(
            content = dismissContent,
            modifier = Modifier.offset {
                if (freezeOffset)
                    IntOffset(-(width * dismissThreshold).roundToInt(), 0)
                else {
                    lastOffset = Offset(
                        if (-(width * dismissThreshold) >= state.offset.value)
                            -(width * dismissThreshold)
                        else
                            state.offset.value,
                        y = 0f
                    )
                    IntOffset(lastOffset.x.roundToInt(), lastOffset.y.roundToInt())
                }
            }
        )
    }
}