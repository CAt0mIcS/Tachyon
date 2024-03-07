package com.tachyonmusic.presentation.player

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerLayout(
    navController: NavController,
    onMiniPlayerHeight: (Dp) -> Unit,
    miniPlayerHeight: Dp,
) {
    val scope = rememberCoroutineScope()

    // TODO: Different layout in landscape mode

    MiniPlayerScreen()

//    /**
//     * If the bottom sheet is collapsed we show the MiniPlayer in the HomeScreen through
//     * the bottom sheet peak height.
//     */
//    if (sheetFraction < 1f) {
//        MiniPlayerScreen(sheetState, onMiniPlayerHeight, sheetFraction, onTargetSheetFraction)
//    }
//
//    /**
//     * The [MiniPlayer] Layout cannot be in the LazyColumn. Thus we set the top padding of
//     * the LazyColumn to be the [miniPlayerHeight] and animate it with the current fraction of the
//     * bottom sheet swipe
//     */
//    if (sheetFraction > 0f) {
//        BackHandler {
//            scope.launch {
//                sheetState.partialExpand()
//            }
//            onTargetSheetFraction(0f)
//        }
//
//        PlayerScreen(sheetState, miniPlayerHeight, sheetFraction, navController)
//    }
}
