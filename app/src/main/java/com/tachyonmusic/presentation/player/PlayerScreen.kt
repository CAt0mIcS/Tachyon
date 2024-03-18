package com.tachyonmusic.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import com.github.krottv.compose.sliders.DefaultThumb
import com.github.krottv.compose.sliders.DefaultTrack
import com.github.krottv.compose.sliders.SliderValueHorizontal
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.presentation.core_components.AnimatedText
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.core_components.SwipeDelete
import com.tachyonmusic.presentation.player.component.EqualizerEditor
import com.tachyonmusic.presentation.player.component.IconForward
import com.tachyonmusic.presentation.player.component.IconRewind
import com.tachyonmusic.presentation.player.component.SaveToPlaylistDialog
import com.tachyonmusic.presentation.player.component.TimingDataEditor
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.isEnabled
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.toReadableString

@Composable
fun PlayerScreen(
    motionLayoutProgress: Float,
    miniPlayerHeight: Dp,
    navController: NavController,
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

    var isEditingTimingData by remember { mutableStateOf(false) }
    var isEditingEqualizer by remember { mutableStateOf(false) }

    val subPlaybackItems by viewModel.subPlaybackItems.collectAsState()
    val playbackType by viewModel.playbackType.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = miniPlayerHeight * (1f - motionLayoutProgress))
            .graphicsLayer(alpha = motionLayoutProgress + .25f),
        contentPadding = PaddingValues(bottom = Theme.padding.small)
    ) {
        item {
            val artworkModifier = Modifier
                .fillMaxWidth()
                .padding(Theme.padding.small)
                .aspectRatio(1f)
                .shadow(Theme.shadow.small, shape = Theme.shapes.large)

            playback.artwork?.Image(modifier = artworkModifier, contentDescription = null)
                ?: PlaceholderArtwork(modifier = artworkModifier, contentDescription = null)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    AnimatedText(
                        modifier = Modifier
                            .padding(
                                start = Theme.padding.medium,
                                top = Theme.padding.medium,
                                end = Theme.padding.medium
                            ),
                        text = playback.displayTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    AnimatedText(
                        modifier = Modifier
                            .padding(
                                start = Theme.padding.medium * 2,
                                end = Theme.padding.medium
                            ),
                        text = playback.displaySubtitle,
                        fontSize = 18.sp
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
                modifier = Modifier
                    .padding(
                        start = Theme.padding.small,
                        bottom = Theme.padding.medium,
                        end = Theme.padding.small
                    )
                    .systemGestureExclusion(),
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
                        colorTrack = MaterialTheme.colorScheme.surfaceVariant,
                        colorProgress = MaterialTheme.colorScheme.primary
                    )
                },

                thumb = { modifier, offset, interactionSource, enabled, thumbSize ->
                    DefaultThumb(
                        modifier,
                        offset,
                        interactionSource,
                        enabled,
                        thumbSize,
                        color = MaterialTheme.colorScheme.primary,
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

                val seekIncrements by viewModel.seekIncrements.collectAsState()
                IconButton(
                    modifier = Modifier.scale(buttonScale),
                    onClick = viewModel::seekBack
                ) {
                    IconRewind(
                        timeSeconds = seekIncrements.back.inWholeSeconds,
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
                    IconForward(
                        timeSeconds = seekIncrements.forward.inWholeSeconds,
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
                    onClick = { isEditingEqualizer = !isEditingEqualizer }
                ) {
                    Icon(
                        painterResource(R.drawable.ic_equalizer),
                        contentDescription = null,
                        modifier = Modifier.scale(iconScale)
                    )
                }

                IconButton(
                    modifier = Modifier.scale(buttonScale),
                    onClick = { isEditingTimingData = !isEditingTimingData }
                ) {
                    Icon(
                        painterResource(R.drawable.ic_customized_song),
                        contentDescription = null,
                        modifier = Modifier.scale(iconScale)
                    )
                }
            }
        }

        if (isEditingTimingData) {
            item {
                TimingDataEditor(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Theme.padding.medium)
                )
            }
        }

        if (isEditingEqualizer) {
            item {
                EqualizerEditor(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Theme.padding.medium)
                )
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

            items(subPlaybackItems, key = { it.mediaId.toString() }) { playback ->
                val updatedPlayback by rememberUpdatedState(playback)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Theme.padding.medium,
                            end = Theme.padding.medium,
                            bottom = Theme.padding.extraSmall
                        )
                        .isEnabled(playback.isPlayable)
                ) {
                    SwipeDelete(
                        shape = Theme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.tertiaryContainer,
                                Theme.shapes.medium
                            ),
                        onClick = { viewModel.removeFromCurrentPlaylist(updatedPlayback) }
                    ) {
                        HorizontalPlaybackView(
                            playback,
                            playback.artwork ?: PlaceholderArtwork,
                            onClick = {
                                if (playback.isPlayable) viewModel.play(
                                    playback,
                                    PlaybackLocation.CUSTOM_PLAYLIST
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}