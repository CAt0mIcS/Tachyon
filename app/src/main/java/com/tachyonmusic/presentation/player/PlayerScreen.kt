package com.tachyonmusic.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.krottv.compose.sliders.DefaultThumb
import com.github.krottv.compose.sliders.DefaultTrack
import com.github.krottv.compose.sliders.SliderValueHorizontal
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.presentation.core_components.AnimatedText
import com.tachyonmusic.presentation.core_components.ErrorDialog
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.core_components.SwipeDelete
import com.tachyonmusic.presentation.core_components.highlight
import com.tachyonmusic.presentation.player.component.EqualizerEditor
import com.tachyonmusic.presentation.player.component.IconForward
import com.tachyonmusic.presentation.player.component.IconRewind
import com.tachyonmusic.presentation.player.component.SaveToPlaylistDialog
import com.tachyonmusic.presentation.player.component.RemixEditor
import com.tachyonmusic.presentation.player.data.TutorialStep
import com.tachyonmusic.presentation.player.model.PlayerEntity
import com.tachyonmusic.presentation.theme.Padding
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.asString
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.toReadableString
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt


@Composable
fun PlayerScreen(
    motionLayoutProgress: Float,
    miniPlayerHeight: Dp,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val shouldShowPlayer by viewModel.shouldShowPlayer.collectAsState()
    if (!shouldShowPlayer)
        return

    val playback by viewModel.playback.collectAsState()
    val tutorialStep by viewModel.tutorialStep.collectAsState()
    var currentPosition by remember { mutableStateOf(0.ms) }

    var isSeeking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (isActive) {
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

    val error by viewModel.error.collectAsState()
    if (error != null)
        ErrorDialog(title = stringResource(R.string.warning), subtitle = error.asString())

    var isEditingTimingData by rememberSaveable { mutableStateOf(false) }
    var isEditingEqualizer by rememberSaveable { mutableStateOf(false) }

    val subPlaybackItems by viewModel.subPlaybackItems.collectAsState()
    val recommendedItems by viewModel.recommendedItems.collectAsState()
    val playbackType by viewModel.playbackType.collectAsState()

    var targetHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val listState = rememberLazyListState()
    var rootOffset by remember { mutableStateOf(Offset.Zero) }
    var targetRect by remember { mutableStateOf<Rect?>(null) }
    var targetIndex by remember { mutableIntStateOf(1) }

    LaunchedEffect(tutorialStep, listState) {
        snapshotFlow { listState.layoutInfo }
            .distinctUntilChanged()
            .collect { layoutInfo ->
                if (tutorialStep !is TutorialStep.Finished) {
                    val viewportHeightPx = layoutInfo.viewportEndOffset
                    val centerOffset = (viewportHeightPx / 2f - targetHeightPx / 2f).toInt()
                    listState.animateScrollToItem(
                        index = targetIndex,
                        scrollOffset = centerOffset
                    )
                }
            }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    rootOffset = it.localToWindow(Offset.Zero)
                }
                .highlight(if (tutorialStep is TutorialStep.Finished) null else targetRect)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = miniPlayerHeight * (1f - motionLayoutProgress))
                    .graphicsLayer(alpha = motionLayoutProgress + .25f),
                contentPadding = PaddingValues(bottom = Theme.padding.small),
                state = listState
            ) {
                item {
                    val artworkModifier = Modifier
                        .fillMaxWidth()
                        .padding(Theme.padding.small)
                        .aspectRatio(1f)
                        .shadow(Theme.shadow.small, shape = Theme.shapes.large)
                        .background(
                            MaterialTheme.colorScheme.background,
                            shape = Theme.shapes.large
                        )

                    playback.artwork?.Image(modifier = artworkModifier, contentDescription = null)
                        ?: Spacer(
                            modifier = Modifier.height(24.dp)
                        )
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
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .onHighlightPositioned(
                            rootOffset,
                            tutorialStep is TutorialStep.PlaybackControls
                        ) { target, height ->
                            targetHeightPx = height
                            targetRect = target
                            targetIndex = 4
                        }) {
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
                                modifier = Modifier
                                    .scale(buttonScale)
                                    .onHighlightPositioned(
                                        rootOffset,
                                        tutorialStep is TutorialStep.SoundEffectButton
                                    ) { target, height ->
                                        targetHeightPx = height
                                        targetRect = target
                                        targetIndex = 4
                                    },
                                onClick = { isEditingEqualizer = !isEditingEqualizer }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_equalizer),
                                    contentDescription = "Edit Sound Effects",
                                    modifier = Modifier.scale(iconScale)
                                )
                            }

                            IconButton(
                                modifier = Modifier
                                    .scale(buttonScale)
                                    .onHighlightPositioned(
                                        rootOffset,
                                        tutorialStep is TutorialStep.RemixButton
                                    ) { target, height ->
                                        targetHeightPx = height
                                        targetRect = target
                                        targetIndex = 4
                                    },
                                onClick = { isEditingTimingData = !isEditingTimingData }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_remix),
                                    contentDescription = "Edit Remix",
                                    modifier = Modifier.scale(iconScale)
                                )
                            }
                        }
                    }
                }

                if (isEditingTimingData) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(Theme.padding.medium))

                        RemixEditor(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Theme.padding.medium)
                                .onHighlightPositioned(
                                    rootOffset,
                                    tutorialStep is TutorialStep.RemixInterface ||
                                            tutorialStep is TutorialStep.RemixButtons ||
                                            tutorialStep is TutorialStep.SaveRemixButton
                                ) { target, height ->
                                    targetHeightPx = height
                                    targetRect = target
                                    targetIndex = 5
                                }
                        )
                    }
                }

                if (isEditingEqualizer) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(Theme.padding.medium))

                        EqualizerEditor(modifier = Modifier.onHighlightPositioned(
                            rootOffset,
                            tutorialStep is TutorialStep.SoundEffectCheckboxes ||
                                    tutorialStep is TutorialStep.SpeedPitchSliders
                        ) { target, height ->
                            targetHeightPx = height
                            targetRect = target
                            targetIndex = 6
                        })
                    }
                }

                if (subPlaybackItems.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(
                                start = Theme.padding.medium,
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
                        ) {
                            if (playbackType !is PlaybackType.Playlist) {
                                SubPlaybackView(
                                    viewModel,
                                    playback,
                                    PlaybackLocation.PREDEFINED_PLAYLIST
                                )
                            } else {
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
                                    SubPlaybackView(
                                        viewModel,
                                        playback,
                                        PlaybackLocation.CUSTOM_PLAYLIST
                                    )
                                }
                            }
                        }
                    }
                }


                if (recommendedItems.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(
                                start = Theme.padding.medium,
                                top = Theme.padding.medium,
                                end = Theme.padding.medium,
                                bottom = Theme.padding.extraSmall
                            ),
                            text = "Similar Items",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(recommendedItems) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = Theme.padding.medium,
                                    end = Theme.padding.medium,
                                    bottom = Theme.padding.extraSmall
                                )
                        ) {
                            SubPlaybackView(viewModel, item, PlaybackLocation.PREDEFINED_PLAYLIST)
                        }
                    }
                }
            }
        }

        if (targetRect != null && tutorialStep !is TutorialStep.Finished) {
            TutorialBubble(
                title = stringResource(tutorialStep.title),
                description = stringResource(tutorialStep.description),
                targetRect = targetRect ?: Rect(0f, 0f, 0f, 0f),
                previous = {
                    TextButton(onClick = viewModel::previousTutorialStep) { Text("Previous") }
                },
                skip = {
                    TextButton(onClick = viewModel::skipTutorial) { Text("Skip") }
                },
                next = {
                    Button(onClick = {
                        when (tutorialStep) {
                            is TutorialStep.RemixButton -> {
                                isEditingTimingData = true
                                viewModel.advanceTutorial()
                            }

                            is TutorialStep.SoundEffectButton -> {
                                isEditingTimingData = true
                                isEditingEqualizer = true
                                viewModel.advanceTutorial()
                            }

                            else -> viewModel.advanceTutorial()
                        }
                    }) {
                        Text(if(tutorialStep is TutorialStep.SpeedPitchSliders) "Finish" else "Next")
                    }
                }
            )
        }
    }
}

@Composable
fun TutorialBubble(
    title: String,
    description: String,
    targetRect: Rect,
    next: @Composable () -> Unit,
    previous: @Composable () -> Unit,
    skip: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    // We'll need density to convert dp ↔ px
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        // Parent size in px
        val parentWidthPx = with(density) { maxWidth.toPx() }
        val parentHeightPx = with(density) { maxHeight.toPx() }

        // Track the bubble's size once it's laid out
        var bubbleSize by remember { mutableStateOf(Size.Zero) }

        // Compute the offset once we have both the targetRect and the bubbleSize
        val bubbleOffset = remember(targetRect, bubbleSize) {
            // center the bubble on the target horizontally
            val idealX = targetRect.center.x - bubbleSize.width / 2f
            // clamp to [0, parentWidth - bubbleWidth]
            val x = idealX.coerceIn(0f, parentWidthPx - bubbleSize.width)

            // try placing above the target, with 12.dp gap
            val gapPx = with(density) { 12.dp.toPx() }
            val yAbove = targetRect.top - bubbleSize.height - gapPx
            // otherwise place below
            val yBelow = targetRect.bottom + gapPx

            // pick the one that stays on screen
            val y = when {
                yAbove >= 0f -> yAbove
                yBelow + bubbleSize.height <= parentHeightPx -> yBelow
                else -> // if it absolutely doesn't fit, center vertically
                    (parentHeightPx - bubbleSize.height) / 2f
            }

            IntOffset(x.roundToInt(), y.roundToInt())
        }

        // This Box is our bubble itself
        Box(
            modifier = Modifier
                .offset { bubbleOffset }
                .onGloballyPositioned { coords ->
                    bubbleSize = coords.size.toSize()
                }
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = Theme.shapes.large
                )
                .padding(horizontal = Theme.padding.medium)
                .padding(top = Theme.padding.medium, bottom = Theme.padding.extraSmall)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Theme.padding.small)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(text = description, fontSize = 14.sp)

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Theme.padding.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    previous()
                    skip()
                    Spacer(Modifier.weight(1f))
                    next()
                }
            }
        }
    }
}


@Composable
private fun SubPlaybackView(
    viewModel: PlayerViewModel,
    playback: PlayerEntity,
    playbackLocation: PlaybackLocation
) {
    HorizontalPlaybackView(
        playback.displayTitle,
        playback.displaySubtitle,
        playback.artwork ?: PlaceholderArtwork,
        onClick = {
            if (playback.isPlayable) viewModel.play(
                playback,
                playbackLocation
            )
        },
        isPlayable = playback.isPlayable
    )
}


private fun Modifier.onHighlightPositioned(
    rootOffset: Offset,
    condition: Boolean,
    onHighlightPositioned: (Rect, Int) -> Unit
) = then(
    Modifier.onGloballyPositioned {
        if (condition) {
            // get the child’s window position...
            val childWindowPos = it.localToWindow(Offset.Zero)
            // ...then subtract the root’s window offset to get a position
            // relative to the Box canvas:
            val topLeft = childWindowPos - rootOffset
            val size = it.size.toSize()
            onHighlightPositioned(Rect(topLeft, size), it.size.height)
        }
    }
)