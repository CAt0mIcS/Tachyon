package com.tachyonmusic.presentation.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.ExperimentalComposeUiApi
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.krottv.compose.sliders.DefaultThumb
import com.github.krottv.compose.sliders.DefaultTrack
import com.github.krottv.compose.sliders.SliderValueHorizontal
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.logger.LoggerImpl
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.main.component.MiniPlayer
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.currentFraction
import com.tachyonmusic.presentation.util.isAtBottom
import com.tachyonmusic.presentation.util.isAtTop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Player(
    sheetState: BottomSheetState,
    miniPlayerHeight: MutableState<Dp>,
    viewModel: PlayerViewModel = hiltViewModel(),
    log: Logger = LoggerImpl()
) {
    var currentPositionNormalized by remember {
        mutableStateOf(
            viewModel.currentPositionNormalized ?: viewModel.recentlyPlayedPositionNormalized
        )
    }
    var currentPosition by remember { mutableStateOf(viewModel.currentPosition) }
    val isPlaying by viewModel.isPlaying

    val playbackState by viewModel.playbackState
    val artworkState by viewModel.artworkState

    val scope = rememberCoroutineScope()
    val recentlyPlayed by viewModel.recentlyPlayed

    val repeatMode by viewModel.repeatMode

    var isSeeking by remember { mutableStateOf(false) }

    var isEditingLoop by remember { mutableStateOf(false) }
    val timingData = viewModel.timingData
    val currentTimingDataIndex by viewModel.currentTimingDataIndex

    var showSaveToPlaylistDialog by remember { mutableStateOf(false) }
    val playlists = viewModel.playlists


    LaunchedEffect(Unit) {
        log.debug("Entered currentPosition update effect composition")
        while (true) {
            if (!isSeeking) {
                currentPositionNormalized = viewModel.currentPositionNormalized
                    ?: viewModel.recentlyPlayedPositionNormalized
                currentPosition = viewModel.currentPosition
            }

            delay(viewModel.audioUpdateInterval)
        }
    }

    DisposableEffect(Unit) {
        viewModel.registerMediaListener()
        onDispose {
            viewModel.unregisterMediaListener()
        }
    }

    // TODO: Should be somewhere else?
    if (showSaveToPlaylistDialog) {
        SaveToPlaylistDialog(
            playlists,
            onDismiss = {
                showSaveToPlaylistDialog = false
            },
            onCheckedChanged = viewModel::editPlaylist,
            onCreatePlaylist = viewModel::createPlaylist
        )
    }

    // TODO: Different layout in landscape mode

    /**
     * If the bottom sheet is collapsed we show the MiniPlayer in the HomeScreen through
     * the bottom sheet peak height.
     */
    if (recentlyPlayed != null && !sheetState.isAtTop) {

        val artwork by artworkState.artwork.collectAsState()

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
                    artwork = artwork ?: PlaceholderArtwork,
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
        BackHandler {
            scope.launch {
                sheetState.collapse()
            }
        }

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

                val artwork by artworkState.artwork.collectAsState()
                val isArtworkLoading by artworkState.isArtworkLoading.collectAsState()

                if (isArtworkLoading)
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
                    LoopEditor(
                        timingData,
                        currentTimingDataIndex,
                        playbackState.duration,
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = viewModel::updateTimingData,
                        onSeekCompleted = viewModel::setNewTimingData,
                        onAddNewTimingData = viewModel::addNewTimingData,
                        onRemoveTimingData = viewModel::removeTimingData,
                        onSaveLoop = viewModel::saveNewLoop
                    )
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
                        artwork ?: PlaceholderArtwork,
                        isArtworkLoading,
                        onClick = { viewModel.onItemClicked(playback) },
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
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoopEditor(
    timingData: List<TimingData>,
    currentTimingDataIndex: Int,
    duration: Long,
    onValueChange: (Int, Long, Long) -> Unit,
    onSeekCompleted: () -> Unit,
    onRemoveTimingData: (Int) -> Unit,
    onAddNewTimingData: () -> Unit,
    onSaveLoop: suspend (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(start = Theme.padding.extraSmall)) {
        IconButton(
            modifier = modifier.padding(Theme.padding.small),
            onClick = onAddNewTimingData
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_circle),
                contentDescription = "Add new loop time point"
            )
        }

        IconButton(
            modifier = Modifier.padding(Theme.padding.small),
            onClick = { onRemoveTimingData(timingData.size - 1) }) {
            Icon(
                painterResource(R.drawable.ic_rewind_10),
                contentDescription = "Remove loop time point"
            )
        }

        for (i in timingData.indices) {
            RangeSlider(
                value = timingData[i].startTime.toFloat()..timingData[i].endTime.toFloat(),
                onValueChange = {
                    onValueChange(
                        i,
                        it.start.toLong(),
                        it.endInclusive.toLong()
                    )
                },
                onValueChangeFinished = onSeekCompleted,
                valueRange = 0f..duration.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = if (i == currentTimingDataIndex) Theme.colors.orange else Theme.colors.contrastLow,
                    activeTrackColor = Theme.colors.orange,
                    inactiveTrackColor = Theme.colors.partialOrange1
                )
            )

            Spacer(modifier = Modifier.padding(top = 6.dp))
        }

        // TODO: State should be in view model to be able to move coroutine stuff to view model
        var openAlertDialog by remember { mutableStateOf(false) }
        var loopName by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        Button(onClick = {
            openAlertDialog = true
        }) {
            Text("Save")
        }

        if (openAlertDialog) {
            AlertDialog(
                onDismissRequest = { openAlertDialog = false },
                text = {
                    TextField(value = loopName, onValueChange = { loopName = it })
                },
                buttons = {
                    Button(
                        onClick = {
                            scope.launch((Dispatchers.IO)) {
                                if (onSaveLoop(loopName))
                                    openAlertDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SaveToPlaylistDialog(
    playlists: List<Pair<String, Boolean>>,
    onDismiss: () -> Unit,
    onCheckedChanged: (Int, Boolean) -> Unit,
    onCreatePlaylist: (String) -> Unit
) {
    var createNewPlaylist by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            elevation = 5.dp,
            shape = Theme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth(.95f)
                .border(1.dp, Theme.colors.orange, Theme.shapes.medium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Theme.padding.medium)
            ) {
                Text(text = "Save to...")

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(playlists.size) { i ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Checkbox(
                                checked = playlists[i].second,
                                onCheckedChange = { onCheckedChanged(i, it) })

                            Text(text = playlists[i].first)
                        }
                    }
                }


                if (!createNewPlaylist) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { createNewPlaylist = true }
                    ) {
                        Text("Create New Playlist")
                    }
                } else {
                    TextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        singleLine = true
                    )
                    Button(onClick = { onCreatePlaylist(newPlaylistName) }) {
                        Text("Create")
                    }
                }
            }
        }
    }
}