package com.tachyonmusic.presentation.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.krottv.compose.sliders.DefaultThumb
import com.github.krottv.compose.sliders.DefaultTrack
import com.github.krottv.compose.sliders.SliderValueHorizontal
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.player.component.SaveToPlaylistDialog
import com.tachyonmusic.presentation.player.data.LoopEditor
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.currentFraction
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.toReadableString

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlayerScreen(
    sheetState: BottomSheetState,
    miniPlayerHeight: Dp,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val shouldShowPlayer by viewModel.shouldShowPlayer.collectAsState()
    if (!shouldShowPlayer)
        return

    val playback by viewModel.playback.collectAsState()
    var currentPosition by remember { mutableStateOf(0.ms) }

    var isSeeking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            if (!isSeeking)
                currentPosition = viewModel.getCurrentPosition()
            delay(viewModel.audioUpdateInterval)
        }
    }


    var showSaveToPlaylistDialog by remember { mutableStateOf(false) }

    if (showSaveToPlaylistDialog) {
        val playlists by viewModel.playlists.collectAsState()

        SaveToPlaylistDialog(
            playlists,
            onDismiss = {
                showSaveToPlaylistDialog = false
            },
            onCheckedChanged = viewModel::editPlaylist,
            onCreatePlaylist = viewModel::createPlaylist
        )
    }

    var isEditingLoop by remember { mutableStateOf(false) }

    val subPlaybackItems by viewModel.subPlaybackItems.collectAsState()
    val playbackType by viewModel.playbackType.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = miniPlayerHeight * (1f - sheetState.currentFraction))
            .graphicsLayer(alpha = sheetState.currentFraction + .25f),
        contentPadding = PaddingValues(bottom = Theme.padding.small)
    ) {
        item {
            val artworkModifier = Modifier
                .fillMaxWidth()
                .padding(Theme.padding.small)
                .aspectRatio(1f)
                .shadow(Theme.shadow.small, shape = Theme.shapes.large)

//            val artworkInfo by viewModel.artwork.collectAsState()
            val artwork by playback.artwork.collectAsState()
            val isLoading by playback.isArtworkLoading.collectAsState()

            if (isLoading)
                CircularProgressIndicator(modifier = artworkModifier)
            else
                artwork?.Image(modifier = artworkModifier, contentDescription = null)
                    ?: PlaceholderArtwork(modifier = artworkModifier, contentDescription = null)
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
                        text = playback.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Text(
                        modifier = Modifier.padding(
                            start = Theme.padding.medium * 2,
                            end = Theme.padding.medium
                        ),
                        text = playback.artist,
                        fontSize = 18.sp,
                        maxLines = 1
                    )
                }

                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(Theme.padding.medium),
                    onClick = { showSaveToPlaylistDialog = true }) {
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
                    text = currentPosition.toReadableString(viewModel.showMillisecondsInPositionText),
                    fontSize = 16.sp
                )

                Text(
                    text = playback.duration.toReadableString(viewModel.showMillisecondsInPositionText),
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
                value = currentPosition.inWholeMilliseconds.toFloat(),
                onValueChange = {
                    isSeeking = true
                    currentPosition = it.ms
                },
                onValueChangeFinished = {
                    viewModel.seekTo(currentPosition)
                    isSeeking = false
                },
                valueRange = 0f..playback.duration.inWholeMilliseconds.toFloat(),
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
                    onClick = viewModel::seekBack
                ) {
                    Icon(
                        painterResource(R.drawable.ic_rewind_10),
                        contentDescription = null,
                        modifier = Modifier.scale(iconScale)
                    )
                }

                val isPlaying by viewModel.isPlaying.collectAsState()

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
                    onClick = viewModel::seekForward
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
                    val repeatMode by viewModel.repeatMode.collectAsState()

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
                    onClick = { isEditingLoop = !isEditingLoop }
                ) {
                    Icon(
                        painterResource(R.drawable.ic_loop),
                        contentDescription = null,
                        modifier = Modifier.scale(iconScale)
                    )
                }
            }
        }

        if (isEditingLoop) {
            item {
                LoopEditor(modifier = Modifier.fillMaxWidth())
            }
        }

        if (subPlaybackItems.isNotEmpty()) {
            item {
                Text(
                    modifier = Modifier.padding(
                        start = Theme.padding.small,
                        top = Theme.padding.medium,
                        end = Theme.padding.medium,
                        bottom = Theme.padding.extraSmall
                    ),
                    text = if (playbackType !is PlaybackType.Playlist) "Up Next" else "Playlist",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(subPlaybackItems) { playback ->

                val artwork by playback.artwork.collectAsState()
                val isArtworkLoading by playback.isArtworkLoading.collectAsState()

                HorizontalPlaybackView(
                    playback,
                    artwork ?: PlaceholderArtwork,
                    isArtworkLoading,
                    onClick = { viewModel.play(playback) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Theme.padding.medium,
                            end = Theme.padding.medium,
                            bottom = Theme.padding.extraSmall
                        )
                )
            }
        }
    }
}