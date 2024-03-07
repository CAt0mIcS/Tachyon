package com.tachyonmusic.presentation.player

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.presentation.player.component.MiniPlayer
import com.tachyonmusic.util.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniPlayerScreen(
    viewModel: MiniPlayerViewModel = hiltViewModel()
) {
    val playback by viewModel.playback.collectAsState()

    if (playback == null)
        return

    val isPlaying by viewModel.isPlaying.collectAsState()

    var currentPositionNormalized by remember { mutableStateOf(0f) }

    val scope = rememberCoroutineScope()

    var miniPlayerHeight by remember { mutableStateOf(0.dp) }

    LaunchedEffect(Unit) {
        while (true) {
            currentPositionNormalized = viewModel.getCurrentPositionNormalized()
            delay(viewModel.audioUpdateInterval)
        }
    }

    MiniPlayer(
        playback = playback,
        artwork = playback?.artwork ?: PlaceholderArtwork,
        currentPosition = currentPositionNormalized,
        isPlaying = isPlaying,
        onPlayPauseClicked = viewModel::pauseResume,
        onClick = {
            TODO("EXPAND SHEET")
        }
    )

    /**
     * TODO
     *   The MiniPlayer - if shown - recomposes every frame, but it should only recompose the
     *   ProgressIndicator line
     */
//    Layout(
//        modifier = Modifier.graphicsLayer(alpha = 1f - sheetFraction),
//        content = {
//            MiniPlayer(
//                playback = playback,
//                artwork = playback?.artwork ?: PlaceholderArtwork,
//                currentPosition = currentPositionNormalized,
//                isPlaying = isPlaying,
//                onPlayPauseClicked = viewModel::pauseResume,
//                onClick = {
//                    scope.launch {
//                        sheetState.expand()
//                    }
//                    onTargetSheetFraction(1f)
//                }
//            )
//        }
//    ) { measurables, constraints ->
//        val looseConstraints = constraints.copy(
//            minWidth = 0,
//            maxWidth = constraints.maxWidth,
//            minHeight = 0,
//            maxHeight = constraints.maxHeight
//        )
//
//        // Measure each child
//        val placeables = measurables.map { measurable ->
//            measurable.measure(looseConstraints)
//        }
//
//        layout(constraints.maxWidth, constraints.maxHeight) {
//            // Place children in the parent layout
//            placeables.forEach { placeable ->
//                // This applies bottom content padding to the LazyColumn handling the entire other screen
//                // so that we can scroll down far enough
//                if (miniPlayerHeight == 0.dp && placeable.height != 0) {
//                    miniPlayerHeight = placeable.height.toDp()
//                    onMiniPlayerHeight(miniPlayerHeight)
//                }
//
//                // Position items
//                placeable.placeRelative(
//                    x = 0,
//                    y = 0
//                )
//            }
//        }
}
