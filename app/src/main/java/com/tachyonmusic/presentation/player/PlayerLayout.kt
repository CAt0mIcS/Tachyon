package com.tachyonmusic.presentation.player

import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.tachyonmusic.presentation.entry.SwipingStates
import com.tachyonmusic.presentation.entry.absoluteFraction
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun PlayerLayout(
    navController: NavController,
    miniPlayerHeight: Dp,
    onMiniPlayerHeight: (Dp) -> Unit,
    swipe: SwipeableState<SwipingStates>
) {
    val scope = rememberCoroutineScope()

    // TODO: Different layout in landscape mode

    /**
     * If the bottom sheet is collapsed we show the MiniPlayer in the HomeScreen through
     * the bottom sheet peak height.
     */
    if (swipe.absoluteFraction < 1f) {
        println("PLL: Should show miniplayer")
        MiniPlayerScreen(swipe, onMiniPlayerHeight)
    }

    /**
     * The [MiniPlayer] Layout cannot be in the LazyColumn. Thus we set the top padding of
     * the LazyColumn to be the [miniPlayerHeight] and animate it with the current fraction of the
     * bottom sheet swipe
     */
    if (swipe.absoluteFraction > 0f) {
        BackHandler {
            scope.launch {
                swipe.animateTo(SwipingStates.COLLAPSED)
            }
        }

        PlayerScreen(swipe, miniPlayerHeight, navController)
        println("PLL: Should show player")
    }
}
