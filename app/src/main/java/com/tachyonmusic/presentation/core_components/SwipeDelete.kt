package com.tachyonmusic.presentation.core_components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.tachyonmusic.presentation.theme.Theme

/**
 * DIFFERENT IMPLEMENTATION:
 *  https://medium.com/mobile-app-development-publication/lessons-learned-after-3-days-debugging-jetpack-compose-swipetodismiss-e058d71f7374
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeDelete(
    modifier: Modifier = Modifier,
    primaryBackgroundColor: Color = Theme.colors.primary,
    shape: Shape = RectangleShape,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    var freezeOffset by remember { mutableStateOf(false) }
    // TODO MAt3
    val state = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        confirmValueChange = { false },
        positionalThreshold = { 128f }
    )

//    val animatedAlpha by animateFloatAsState(
//        if (state.isDismissed(DismissDirection.EndToStart)
//            || state.isDismissed(DismissDirection.StartToEnd)
//        ) 1f // 0f
//        else 1f,
//        tween(Theme.animation.short)
//    )

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        backgroundContent = {
            DeleteBackground(state, shape) {}
        }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBackground(state: SwipeToDismissBoxState, shape: Shape, onClick: () -> Unit) {
    val color by animateColorAsState(
        when (state.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> Color.Red
            else -> Theme.colors.primary // TODO MAT3: Use new color system
        },
        animationSpec = tween(Theme.animation.long)
    )

    val scale by animateFloatAsState(
        if (state.targetValue == SwipeToDismissBoxValue.Settled) 1f else 1.4f,
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
//@Composable
//@ExperimentalMaterialApi
//private fun SwipeToDismiss(
//    state: DismissState,
//    modifier: Modifier = Modifier,
//    directions: Set<DismissDirection> = setOf(
//        DismissDirection.EndToStart,
//        DismissDirection.StartToEnd
//    ),
//    dismissThreshold: Float,
//    freezeOffset: Boolean,
//    background: @Composable RowScope.() -> Unit,
//    dismissContent: @Composable RowScope.() -> Unit
//) = BoxWithConstraints(modifier) {
//    val width = constraints.maxWidth.toFloat()
//    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
//
//    var lastOffset by remember { mutableStateOf(Offset.Zero) }
//
//    val anchors = mutableMapOf(0f to DismissValue.Default)
//    if (DismissDirection.StartToEnd in directions) anchors += width to DismissValue.DismissedToEnd
//    if (DismissDirection.EndToStart in directions) anchors += -width to DismissValue.DismissedToStart
//
//    val thresholds = { from: DismissValue, to: DismissValue ->
//        FractionalThreshold(dismissThreshold)
//    }
//    val minFactor =
//        if (DismissDirection.EndToStart in directions) SwipeableDefaults.StandardResistanceFactor else SwipeableDefaults.StiffResistanceFactor
//    val maxFactor =
//        if (DismissDirection.StartToEnd in directions) SwipeableDefaults.StandardResistanceFactor else SwipeableDefaults.StiffResistanceFactor
//    Box(
//        Modifier.swipeable(
//            state = state,
//            anchors = anchors,
//            thresholds = thresholds,
//            orientation = Orientation.Horizontal,
//            enabled = true, //state.currentValue == DismissValue.Default
//            reverseDirection = isRtl,
//            resistance = ResistanceConfig(
//                basis = width,
//                factorAtMin = minFactor,
//                factorAtMax = maxFactor
//            )
//        )
//    ) {
//        Row(
//            content = background,
//            modifier = Modifier.matchParentSize()
//        )
//        Row(
//            content = dismissContent,
//            modifier = Modifier.offset {
//                if (freezeOffset)
//                    IntOffset(-(width * dismissThreshold).roundToInt(), 0)
//                else {
//                    lastOffset = Offset(
//                        if (-(width * dismissThreshold) >= state.offset.value)
//                            -(width * dismissThreshold)
//                        else
//                            state.offset.value,
//                        y = 0f
//                    )
//                    IntOffset(lastOffset.x.roundToInt(), lastOffset.y.roundToInt())
//                }
//            }
//        )
//    }
//}