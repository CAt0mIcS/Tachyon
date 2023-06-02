package com.tachyonmusic.presentation.player

import androidx.activity.compose.BackHandler
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.tachyonmusic.presentation.player.component.MiniPlayer
import com.tachyonmusic.presentation.util.isAtBottom
import com.tachyonmusic.presentation.util.isAtTop
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlayerLayout(
    navController: NavController,
    sheetState: BottomSheetState,
    onMiniPlayerHeight: (Dp) -> Unit,
    miniPlayerHeight: Dp
) {
    val scope = rememberCoroutineScope()

    // TODO: Different layout in landscape mode

    /**
     * If the bottom sheet is collapsed we show the MiniPlayer in the HomeScreen through
     * the bottom sheet peak height.
     */
    if (!sheetState.isAtTop) {
        MiniPlayerScreen(sheetState, onMiniPlayerHeight = onMiniPlayerHeight)
    }

    /**
     * The [MiniPlayer] Layout cannot be in the LazyColumn. Thus we set the top padding of
     * the LazyColumn to be the [miniPlayerHeight] and animate it with the current fraction of the
     * bottom sheet swipe
     */
    if (!sheetState.isAtBottom) {
        BackHandler {
            scope.launch {
                sheetState.collapse()
            }
        }

        PlayerScreen(sheetState, miniPlayerHeight, navController)
    }
}
