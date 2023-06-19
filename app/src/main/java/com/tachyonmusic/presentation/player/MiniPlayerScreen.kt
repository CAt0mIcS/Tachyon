package com.tachyonmusic.presentation.player

import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.presentation.player.component.MiniPlayer
import com.tachyonmusic.util.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MiniPlayerScreen(
    sheetState: BottomSheetState,
    onMiniPlayerHeight: (Dp) -> Unit,
    sheetFraction: Float,
    onTargetSheetFraction: (Float) -> Unit,
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

    /**
     * TODO
     *   The MiniPlayer - if shown - recomposes every frame, but it should only recompose the
     *   ProgressIndicator line
     */
    Layout(
        modifier = Modifier.graphicsLayer(alpha = 1f - sheetFraction),
        content = {
            MiniPlayer(
                playback = playback,
                artwork = playback?.artwork ?: PlaceholderArtwork,
                isArtworkLoading = playback?.isArtworkLoading ?: false,
                currentPosition = currentPositionNormalized,
                isPlaying = isPlaying,
                onPlayPauseClicked = viewModel::pauseResume,
                onClick = {
                    scope.launch {
                        sheetState.expand()
                    }
                    onTargetSheetFraction(1f)
                }
            )
        }
    ) { measurables, constraints ->
        val looseConstraints = constraints.copy(
            minWidth = 0,
            maxWidth = constraints.maxWidth,
            minHeight = 0,
            maxHeight = constraints.maxHeight
        )

        // Measure each child
        val placeables = measurables.map { measurable ->
            measurable.measure(looseConstraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            // Place children in the parent layout
            placeables.forEach { placeable ->
                // This applies bottom content padding to the LazyColumn handling the entire other screen
                // so that we can scroll down far enough
                if (miniPlayerHeight == 0.dp && placeable.height != 0) {
                    miniPlayerHeight = placeable.height.toDp()
                    onMiniPlayerHeight(miniPlayerHeight)
                }

                // Position items
                placeable.placeRelative(
                    x = 0,
                    y = 0
                )
            }
        }
    }
}