package com.tachyonmusic.presentation.player

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.krottv.compose.sliders.DefaultThumb
import com.github.krottv.compose.sliders.DefaultTrack
import com.github.krottv.compose.sliders.SliderValueHorizontal
import com.tachyonmusic.app.R
import com.tachyonmusic.core.NavigationItem
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.theme.Theme
import kotlinx.coroutines.delay

object PlayerScreen : NavigationItem("player_screen") {

    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: PlayerViewModel = hiltViewModel()
    ) {
        var currentPosition by remember { mutableStateOf(viewModel.currentPosition) }
        val isPlaying by viewModel.isPlaying

        var loopName by remember { mutableStateOf("") }
        val playbackState by viewModel.playbackState
        val artwork by viewModel.artwork
        val repeatMode by viewModel.repeatMode

        var isSeeking by remember { mutableStateOf(false) }

        val loopState = viewModel.loopState

        DisposableEffect(Unit) {
            viewModel.registerPlayerListeners()
            Log.d("PlayerScreen", "Registering player listeners")
            onDispose {
                viewModel.unregisterPlayerListeners()
                Log.d("PlayerScreen", "Unregistering player listeners")
            }
        }

        LaunchedEffect(Unit) {
            Log.d("PlayerScreen", "Entered currentPosition update effect composition")
            while (true) {
                if (!isSeeking)
                    currentPosition = viewModel.currentPosition
                delay(viewModel.audioUpdateInterval)
            }
        }

        // TODO: Different layout in landscape mode


        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Theme.padding.small)
        ) {

            item {
                val artworkModifier = Modifier
                    .fillMaxWidth()
                    .padding(Theme.padding.small)
                    .aspectRatio(1f)
                    .shadow(Theme.shadow.small, shape = Theme.shapes.large)

                if (artwork != null) {
                    Image(
                        modifier = artworkModifier,
                        painter = artwork!!.painter,
                        contentDescription = null,
                    )
                } else {
                    Image(
                        modifier = artworkModifier,
                        painter = painterResource(R.drawable.artwork_image_placeholder),
                        contentDescription = null,
                    )
                }
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
                // TODO: isPlaying sometimes not updated to true(?), thus seekbar not working seeking automatically
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
                        onClick = { viewModel.onSeekBack() }
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
                        onClick = {
                            if (isPlaying)
                                viewModel.pause()
                            else
                                viewModel.resume()
                        }
                    ) {
                        Icon(
                            painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                            contentDescription = null,
                            modifier = Modifier.scale(iconScale)
                        )
                    }

                    IconButton(
                        modifier = Modifier.scale(buttonScale),
                        onClick = { viewModel.onSeekForward() }
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_forward_10),
                            contentDescription = null,
                            modifier = Modifier.scale(iconScale)
                        )
                    }
                }
            }

            ////////////////////////////////////////////////////////////////////////////////////////
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
                        onClick = { viewModel.onRepeatModeChange() }
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

                    HorizontalPlaybackView(
                        playback,
                        artwork?.painter,
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