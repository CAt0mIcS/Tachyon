package com.tachyonmusic.presentation.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.krottv.compose.sliders.DefaultThumb
import com.github.krottv.compose.sliders.DefaultTrack
import com.github.krottv.compose.sliders.SliderValueHorizontal
import com.tachyonmusic.app.R
import com.tachyonmusic.data.PlaceholderArtwork
import com.tachyonmusic.logger.Log
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.main.component.MiniPlayer
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.currentFraction
import com.tachyonmusic.presentation.util.isAtBottom
import com.tachyonmusic.presentation.util.isAtTop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Player(
    sheetState: BottomSheetState,
    miniPlayerHeight: MutableState<Dp>,
    viewModel: PlayerViewModel = hiltViewModel(),
    log: Logger = Log()
) {
    var currentPositionNormalized by remember {
        mutableStateOf(
            viewModel.currentPositionNormalized ?: viewModel.recentlyPlayedPositionNormalized
        )
    }
    var currentPosition by remember { mutableStateOf(viewModel.currentPosition) }
    val isPlaying by viewModel.isPlaying

    val playbackState by viewModel.playbackState

    val scope = rememberCoroutineScope()
    val recentlyPlayed by viewModel.recentlyPlayed

    val repeatMode by viewModel.repeatMode

    var isSeeking by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        log.debug("Entered currentPosition update effect composition")
        while (true) {
            if (!isSeeking) {
                currentPositionNormalized = viewModel.currentPositionNormalized
                    ?: viewModel.recentlyPlayedPositionNormalized
                currentPosition = viewModel.currentPosition
            }

            delay(viewModel.getAudioUpdateInterval())
        }
    }

    DisposableEffect(Unit) {
        viewModel.registerMediaListener()
        onDispose {
            viewModel.unregisterMediaListener()
        }
    }

    // TODO: Different layout in landscape mode

    /**
     * If the bottom sheet is collapsed we show the MiniPlayer in the HomeScreen through
     * the bottom sheet peak height.
     */
    if (recentlyPlayed != null && !sheetState.isAtTop) {

        val artwork by recentlyPlayed!!.artwork.collectAsState()

        /**
         * TODO
         *   The MiniPlayer - if shown - recomposes every frame, but it should only recompose the
         *   ProgressIndicator line
         */
        Layout(
            modifier = Modifier.graphicsLayer(alpha = 1f - sheetState.currentFraction),
            content = {
                MiniPlayer(
                    playback = recentlyPlayed,
                    artwork = artwork
                        ?: PlaceholderArtwork(R.drawable.artwork_image_placeholder),
                    currentPosition = currentPositionNormalized,
                    isPlaying = isPlaying,
                    onPlayPauseClicked = viewModel::pauseResume,
                    onClick = {
                        scope.launch {
                            sheetState.expand()
                        }
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
                    if (miniPlayerHeight.value == 0.dp && placeable.height != 0) {
                        miniPlayerHeight.value = placeable.height.toDp()
                        log.debug("BottomPadding ${miniPlayerHeight.value}")
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

    /**
     * The [MiniPlayer] Layout cannot be in the LazyColumn. Thus we set the top padding of
     * the LazyColumn to be the [miniPlayerHeight] and animate it with the current fraction of the
     * bottom sheet swipe
     */

    if (!sheetState.isAtBottom) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = miniPlayerHeight.value * (1f - sheetState.currentFraction))
                .graphicsLayer(alpha = sheetState.currentFraction + .25f),
            contentPadding = PaddingValues(bottom = Theme.padding.small)
        ) {
            item {
                val artworkModifier = Modifier
                    .fillMaxWidth()
                    .padding(Theme.padding.small)
                    .aspectRatio(1f)
                    .shadow(Theme.shadow.small, shape = Theme.shapes.large)

                val artwork by playbackState.artwork.collectAsState()
                val isArtworkLoading by playbackState.isArtworkLoading.collectAsState()

                if (isArtworkLoading)
                    CircularProgressIndicator(modifier = artworkModifier)
                else
                    artwork?.Image(modifier = artworkModifier, contentDescription = null)
                        ?: PlaceholderArtwork(R.drawable.artwork_image_placeholder).Image(
                            contentDescription = null,
                            modifier = artworkModifier
                        )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text(
                            modifier = Modifier.padding(
                                start = Theme.padding.medium,
                                top = Theme.padding.medium,
                                end = Theme.padding.medium
                            ),
                            text = playbackState.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        Text(
                            modifier = Modifier.padding(
                                start = Theme.padding.medium * 2,
                                end = Theme.padding.medium
                            ),
                            text = playbackState.artist,
                            fontSize = 18.sp,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(Theme.padding.medium),
                        onClick = { /*TODO: Save to playlist*/ }) {
                        Icon(
                            painterResource(R.drawable.ic_add_circle),
                            null,
                            tint = Theme.colors.contrastHigh.copy(alpha = .8f),
                            modifier = Modifier.scale(1.7f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Theme.padding.medium,
                            end = Theme.padding.medium
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = viewModel.getTextForPosition(currentPosition),
                        fontSize = 16.sp
                    )

                    Text(
                        text = viewModel.getTextForPosition(playbackState.duration),
                        fontSize = 16.sp
                    )
                }
            }

            item {
                SliderValueHorizontal(
                    modifier = Modifier.padding(
                        start = Theme.padding.small,
                        bottom = Theme.padding.medium,
                        end = Theme.padding.small
                    ),
                    value = currentPosition.toFloat(),
                    onValueChange = {
                        isSeeking = true
                        currentPosition = it.toLong()
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        viewModel.onSeekTo(currentPosition)
                    },
                    valueRange = 0f..playbackState.duration.toFloat(),
                    thumbSizeInDp = DpSize(16.dp, 16.dp),
                    track = { modifier, fraction, interactionSource, tickFractions, enabled ->
                        DefaultTrack(
                            modifier,
                            fraction,
                            interactionSource,
                            tickFractions,
                            enabled,
                            colorTrack = Theme.colors.partialOrange1,
                            colorProgress = Theme.colors.orange
                        )
                    },

                    thumb = { modifier, offset, interactionSource, enabled, thumbSize ->
                        DefaultThumb(
                            modifier,
                            offset,
                            interactionSource,
                            enabled,
                            thumbSize,
                            color = Theme.colors.orange,
                            scaleOnPress = 1.2f
                        )
                    }
                )
            }

            /**
             * Media Controls
             */
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val buttonScale = 1.2f
                    val iconScale = 1.2f

                    // TODO: Decide if icons should seek e.g. 15s back/forward or seek to previous/next item
                    // TODO: Adjust icons if needed

                    IconButton(
                        modifier = Modifier.scale(buttonScale),
                        onClick = viewModel::onSeekBack
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_rewind_10),
                            contentDescription = null,
                            modifier = Modifier.scale(iconScale)
                        )
                    }

                    // TODO: IconToggleButton?
                    IconButton(
                        modifier = Modifier.scale(buttonScale),
                        onClick = viewModel::pauseResume
                    ) {
                        Icon(
                            painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                            contentDescription = null,
                            modifier = Modifier.scale(iconScale)
                        )
                    }

                    IconButton(
                        modifier = Modifier.scale(buttonScale),
                        onClick = viewModel::onSeekForward
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_forward_10),
                            contentDescription = null,
                            modifier = Modifier.scale(iconScale)
                        )
                    }
                }
            }

            ////////////////////////////////////////////////////////////////////////////////////
            // Second Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Theme.padding.extraSmall),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val buttonScale = 1.15f
                    val iconScale = 1.15f

                    IconButton(
                        modifier = Modifier.scale(buttonScale),
                        onClick = viewModel::nextRepeatMode
                    ) {
                        Icon(
                            painterResource(repeatMode.icon),
                            contentDescription = null,
                            modifier = Modifier.scale(iconScale)
                        )
                    }

                    IconButton(
                        modifier = Modifier.scale(buttonScale),
                        onClick = { /*TODO: Equalizer*/ }
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_equalizer),
                            contentDescription = null,
                            modifier = Modifier.scale(iconScale)
                        )
                    }

                    IconButton(
                        modifier = Modifier.scale(buttonScale),
                        onClick = { /*TODO: Loop Edit Screen*/ }
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_loop),
                            contentDescription = null,
                            modifier = Modifier.scale(iconScale)
                        )
                    }
                }
            }

            if (playbackState.children.isNotEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(
                            start = Theme.padding.small,
                            top = Theme.padding.medium,
                            end = Theme.padding.medium,
                            bottom = Theme.padding.extraSmall
                        ),
                        text = if (playbackState.children.size == 1) "Up Next" else "Playlist",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(playbackState.children) { playback ->

                    val artwork by playback.artwork.collectAsState()
                    val isArtworkLoading by playback.isArtworkLoading.collectAsState()

                    HorizontalPlaybackView(
                        playback,
                        artwork ?: PlaceholderArtwork(R.drawable.artwork_image_placeholder),
                        isArtworkLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = Theme.padding.medium,
                                end = Theme.padding.medium,
                                bottom = Theme.padding.extraSmall
                            )
                            .clickable {
                                viewModel.onItemClicked(playback)
                            }
                    )
                }
            }
        }
    }
}