package com.tachyonmusic.presentation.core_components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.tachyonmusic.presentation.theme.Theme
import kotlin.math.roundToInt

/**
 * DIFFERENT IMPLEMENTATION:
 *  https://medium.com/mobile-app-development-publication/lessons-learned-after-3-days-debugging-jetpack-compose-swipetodismiss-e058d71f7374
 */


@Composable
fun SwipeDelete(
    modifier: Modifier = Modifier,
    primaryBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = RectangleShape,
    onClick: () -> Unit,
    fractionalThreshold: Float = .35f,
    content: @Composable RowScope.() -> Unit
) {
    val density = LocalDensity.current
    val state = remember {
        AnchoredDraggableState(
            SwipeState.COLLAPSED,
            positionalThreshold = { distance -> distance * .5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = tween(),
            confirmValueChange = { true }
        )
    }

    val animatedAlpha by animateFloatAsState(
        if (state.targetValue == SwipeState.EXPANDED
            || state.targetValue == SwipeState.COLLAPSED
        ) 1f // 0f
        else 1f,
        tween(Theme.animation.short)
    )

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        fractionalThreshold = fractionalThreshold,
        backgroundContent = {
            val color by animateColorAsState(
                when (state.targetValue) {
                    SwipeState.COLLAPSED -> primaryBackgroundColor
                    else -> Color.Red
                },
                animationSpec = tween(Theme.animation.long)
            )

            val scale by animateFloatAsState(
                if (state.targetValue == SwipeState.COLLAPSED) 1f else 1.4f,
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
        content = {
            val scope = this
            Box(modifier = Modifier.graphicsLayer { alpha = animatedAlpha }) {
                scope.content()
            }
        }
    )
}

@Composable
private fun SwipeToDismissBox(
    state: AnchoredDraggableState<SwipeState>,
    backgroundContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    enableDismissFromStartToEnd: Boolean = true,
    enableDismissFromEndToStart: Boolean = true,
    fractionalThreshold: Float = 1f,
    content: @Composable RowScope.() -> Unit,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Box(
        modifier
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Horizontal,
                reverseDirection = isRtl,
            ),
        propagateMinConstraints = true
    ) {
        Row(
            content = backgroundContent,
            modifier = Modifier.matchParentSize()
        )
        Row(
            content = content,
            modifier = Modifier.swipeToDismissBoxAnchors(
                state,
                enableDismissFromStartToEnd,
                enableDismissFromEndToStart,
                fractionalThreshold
            )
        )
    }
}

private fun Modifier.swipeToDismissBoxAnchors(
    state: AnchoredDraggableState<SwipeState>,
    enableDismissFromStartToEnd: Boolean,
    enableDismissFromEndToStart: Boolean,
    fractionalThreshold: Float
) = this then SwipeToDismissAnchorsElement(
    state,
    enableDismissFromStartToEnd,
    enableDismissFromEndToStart,
    fractionalThreshold
)

private class SwipeToDismissAnchorsElement(
    private val state: AnchoredDraggableState<SwipeState>,
    private val enableDismissFromStartToEnd: Boolean,
    private val enableDismissFromEndToStart: Boolean,
    private val fractionalThreshold: Float
) : ModifierNodeElement<SwipeToDismissAnchorsNode>() {

    override fun create() = SwipeToDismissAnchorsNode(
        state,
        enableDismissFromStartToEnd,
        enableDismissFromEndToStart,
        fractionalThreshold
    )

    override fun update(node: SwipeToDismissAnchorsNode) {
        node.state = state
        node.enableDismissFromStartToEnd = enableDismissFromStartToEnd
        node.enableDismissFromEndToStart = enableDismissFromEndToStart
        node.fractionalThreshold = fractionalThreshold
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other as SwipeToDismissAnchorsElement
        if (state != other.state) return false
        if (enableDismissFromStartToEnd != other.enableDismissFromStartToEnd) return false
        if (fractionalThreshold != other.fractionalThreshold) return false
        return enableDismissFromEndToStart == other.enableDismissFromEndToStart
    }

    override fun hashCode(): Int {
        var result = state.hashCode()
        result = 31 * result + enableDismissFromStartToEnd.hashCode()
        result = 31 * result + enableDismissFromEndToStart.hashCode()
        result = 31 * result + fractionalThreshold.hashCode()
        return result
    }
}

private class SwipeToDismissAnchorsNode(
    var state: AnchoredDraggableState<SwipeState>,
    var enableDismissFromStartToEnd: Boolean,
    var enableDismissFromEndToStart: Boolean,
    var fractionalThreshold: Float
) : Modifier.Node(), LayoutModifierNode {
    private var didLookahead: Boolean = false

    override fun onDetach() {
        didLookahead = false
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        // If we are in a lookahead pass, we only want to update the anchors here and not in
        // post-lookahead. If there is no lookahead happening (!isLookingAhead && !didLookahead),
        // update the anchors in the main pass.
        if (isLookingAhead || !didLookahead) {
            val width = placeable.width.toFloat() * fractionalThreshold
            val newAnchors = DraggableAnchors {
                SwipeState.COLLAPSED at 0f
                if (enableDismissFromStartToEnd) {
                    SwipeState.EXPANDED at width
                }
                if (enableDismissFromEndToStart) {
                    SwipeState.EXPANDED at -width
                }
            }
            state.updateAnchors(newAnchors)
        }
        didLookahead = isLookingAhead || didLookahead
        return layout(placeable.width, placeable.height) {
            // In a lookahead pass, we use the position of the current target as this is where any
            // ongoing animations would move. If SwipeToDismissBox is in a settled state, lookahead
            // and post-lookahead will converge.
            val xOffset = if (isLookingAhead) {
                state.anchors.positionOf(state.targetValue)
            } else state.requireOffset()
            placeable.place(xOffset.roundToInt(), 0)
        }
    }
}

private enum class SwipeState {
    EXPANDED,
    COLLAPSED
}