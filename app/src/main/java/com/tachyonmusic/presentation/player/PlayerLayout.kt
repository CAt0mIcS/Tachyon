package com.tachyonmusic.presentation.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.tachyonmusic.presentation.entry.SwipingStates
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerLayout(
    navController: NavController,
    miniPlayerHeight: Dp,
    onMiniPlayerHeight: (Dp) -> Unit,
    draggable: AnchoredDraggableState<SwipingStates>,
    motionLayoutProgress: Float
) {
    val scope = rememberCoroutineScope()

    // TODO: Different layout in landscape mode

    /**
     * If the bottom sheet is collapsed we show the MiniPlayer in the HomeScreen through
     * the bottom sheet peak height.
     */
    if (motionLayoutProgress < 1f) {
        MiniPlayerScreen(draggable, motionLayoutProgress, onMiniPlayerHeight)
    }

    /**
     * The [MiniPlayer] Layout cannot be in the LazyColumn. Thus we set the top padding of
     * the LazyColumn to be the [miniPlayerHeight] and animate it with the current fraction of the
     * bottom sheet swipe
     */
    if (motionLayoutProgress > 0.1f) {
        BackHandler {
            scope.launch {
                draggable.animateTo(SwipingStates.COLLAPSED)
            }
        }

        PlayerScreen(motionLayoutProgress, miniPlayerHeight, navController)
    }
}
