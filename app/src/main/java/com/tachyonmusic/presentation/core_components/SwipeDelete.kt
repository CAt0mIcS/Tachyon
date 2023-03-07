package com.tachyonmusic.presentation.core_components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.tachyonmusic.presentation.theme.Theme

/**
 * https://medium.com/mobile-app-development-publication/lessons-learned-after-3-days-debugging-jetpack-compose-swipetodismiss-e058d71f7374
 *
 * For any problems with [SwipeToDismiss]
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeDelete(
    state: DismissState,
    modifier: Modifier = Modifier,
    primaryBackgroundColor: Color = Theme.colors.primary,
    shape: Shape = RectangleShape,
    content: @Composable RowScope.() -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        if (state.isDismissed(DismissDirection.EndToStart)
            || state.isDismissed(DismissDirection.StartToEnd)
        ) 0f
        else 1f,
        tween(Theme.animation.short)
    )

    SwipeToDismiss(
        state = state,
        modifier = modifier,
        directions = setOf(
            DismissDirection.EndToStart
        ),
        dismissThresholds = {
            FractionalThreshold(.05f)
        },
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
                    .padding(horizontal = Dp(20f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Icon",
                    modifier = Modifier.scale(scale)
                )
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