package com.tachyonmusic.presentation.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.playback_layers.SortType
import com.tachyonmusic.presentation.BottomNavigationItem
import com.tachyonmusic.presentation.core_components.ErrorDialog
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.core_components.SwipeDelete
import com.tachyonmusic.presentation.entry.SwipingStates
import com.tachyonmusic.presentation.library.component.FilterItem
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.AdmobBanner
import com.tachyonmusic.presentation.util.asString
import com.tachyonmusic.presentation.util.isEnabled
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.sec
import kotlinx.coroutines.launch

object LibraryScreen :
    BottomNavigationItem(R.string.btmNav_library, R.drawable.ic_library, "library") {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    operator fun invoke(
        draggable: AnchoredDraggableState<SwipingStates>,
        viewModel: LibraryViewModel = hiltViewModel()
    ) {
        var sortOptionsExpanded by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        val filterPlaybackType by viewModel.filterType.collectAsState()
        val playbackItems by viewModel.items.collectAsState()

        val listState = rememberLazyListState()
        LaunchedEffect(listState.firstVisibleItemIndex) { // TODO: Optimize
            /**
             * If [listState.firstVisibleItemIndex] changes the coroutine will get cancelled and
             * relaunched. If it changes too fast we don't want to always load artwork, waiting for
             * the value to stay the same before updating artwork
             */
            delay(200.ms)
            viewModel.loadArtwork(
                kotlin.math.max(
                    listState.firstVisibleItemIndex - 2,
                    0
                )..listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size + 4
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = Theme.padding.medium,
                    top = Theme.padding.medium,
                    end = Theme.padding.medium
                ), contentPadding = PaddingValues(bottom = Theme.padding.small)
        ) {

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(Theme.shadow.small, shape = Theme.shapes.extraLarge)
                        .horizontalScroll(rememberScrollState())
                        .clip(Theme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(
                            start = Theme.padding.medium,
                            top = Theme.padding.extraSmall,
                            end = Theme.padding.medium,
                            bottom = Theme.padding.extraSmall
                        ), horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterItem(R.string.songs, filterPlaybackType is PlaybackType.Song) {
                        viewModel.onFilterSongs()
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    FilterItem(
                        R.string.customized_songs,
                        filterPlaybackType is PlaybackType.CustomizedSong
                    ) {
                        viewModel.onFilterCustomizedSongs()
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    FilterItem(R.string.playlists, filterPlaybackType is PlaybackType.Playlist) {
                        viewModel.onFilterPlaylists()
                    }
                }
            }

            item {
                Row(modifier = Modifier
                    .padding(Theme.padding.medium)
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null,
                    ) {
                        sortOptionsExpanded = true
                    }) {
                    val iconAndTextColor by animateColorAsState(
                        if (sortOptionsExpanded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
                        tween(Theme.animation.short)
                    )

                    Box {

                        Icon(
                            painter = painterResource(R.drawable.ic_sort),
                            contentDescription = "Open Sorting Options",
                            tint = iconAndTextColor,
                            modifier = Modifier.scale(1.3f)
                        )


                        DropdownMenu(
                            modifier = Modifier.padding(Theme.padding.medium),
                            expanded = sortOptionsExpanded,
                            onDismissRequest = { sortOptionsExpanded = false }) {
                            SortType.entries.forEach {
                                DropdownMenuItem(
                                    text = {
                                        Text(it.asString())
                                    },
                                    onClick = {
                                        viewModel.onSortTypeChanged(it)
                                        sortOptionsExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    val sortParams by viewModel.sortParams.collectAsState()

                    Text(
                        modifier = Modifier.padding(start = Theme.padding.medium),
                        text = sortParams.type.asString(sortParams.order),
                        fontSize = 18.sp,
                        color = iconAndTextColor
                    )
                }
            }

            items(playbackItems, key = { it.mediaId.toString() }) { playback ->

                if (playback.playbackType is PlaybackType.Ad.Banner) {
                    AdmobBanner(
                        optionalAdView = viewModel.cachedBannerAds[playback.mediaId],
                        onNewAdViewCreated = {
                            viewModel.cacheAd(playback.mediaId, it)
                        }
                    )

                } else {
                    val updatedPlayback by rememberUpdatedState(playback)
                    var showArtworkSelectionDialog by remember { mutableStateOf(false) }
                    var showMetadataDialog by remember { mutableStateOf(false) }
                    var showDropDownMenu by remember { mutableStateOf(false) }

                    SwipeDelete(
                        shape = Theme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Theme.padding.extraSmall),
                        onClick = {
                            viewModel.excludePlayback(updatedPlayback)
                        }
                    ) {
                        HorizontalPlaybackView(
                            playback,
                            playback.artwork ?: PlaceholderArtwork,
                            modifier = Modifier.isEnabled(playback.isPlayable),
                            showDropDownMenu,
                            onOptionsMenuClicked = {
                                showDropDownMenu = !showDropDownMenu
                            },
                            dropDownMenuContent = {
                                Button(
                                    onClick = {
                                        showMetadataDialog = true
                                        showDropDownMenu = false
                                    }
                                ) {
                                    Text("Set Metadata")
                                }
                                Button(
                                    onClick = {
                                        viewModel.queryArtwork(playback)
                                        showArtworkSelectionDialog = true
                                        showDropDownMenu = false
                                    }
                                ) {
                                    Text("Select Artwork")
                                }
                            },
                            onClick = {
                                viewModel.onItemClicked(playback)
                                scope.launch {
                                    draggable.animateTo(SwipingStates.EXPANDED)
                                }
                            })
                    }

                    if (showArtworkSelectionDialog) {
                        Dialog(
                            onDismissRequest = { showArtworkSelectionDialog = false }
                        ) {
                            val artworks by viewModel.queriedArtwork.collectAsState()
                            val error by viewModel.artworkLoadingError.collectAsState()

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(Theme.shapes.extraLarge)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    var searchQuery by remember { mutableStateOf("${playback.artist} ${playback.title}") }

                                    LaunchedEffect(searchQuery) {
                                        delay(1.sec)
                                        viewModel.queryArtwork(playback, searchQuery)
                                    }

                                    Text(
                                        "Select artwork to assign to playback",
                                        modifier = Modifier.padding(Theme.padding.medium)
                                    )

                                    Text("Search Query")
                                    TextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it })

                                    if (error != null) {
                                        Text(
                                            error?.asString() ?: "Unknown error occurred",
                                            modifier = Modifier.padding(Theme.padding.medium)
                                        )
                                    }

                                    LazyVerticalGrid(
                                        modifier = Modifier.padding(Theme.padding.medium),
                                        columns = GridCells.Adaptive(100.dp),
                                        contentPadding = PaddingValues(Theme.padding.small)
                                    ) {
                                        items(artworks) { artwork ->
                                            artwork(null,
                                                Modifier
                                                    .size(100.dp)
                                                    .clickable {
                                                        showArtworkSelectionDialog = false
                                                        viewModel.assignArtworkToPlayback(
                                                            artwork,
                                                            playback
                                                        )
                                                    })
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showMetadataDialog) {
                        Dialog(
                            onDismissRequest = { showMetadataDialog = false }
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(Theme.shapes.extraLarge)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    var title by remember { mutableStateOf(playback.title) }
                                    var artist by remember { mutableStateOf(playback.artist) }
                                    var name by remember { mutableStateOf(playback.displayTitle) }

                                    val playbackType = playback.mediaId.playbackType

                                    if (playbackType !is PlaybackType.Playlist) {
                                        Text("Title")
                                        TextField(value = title, onValueChange = { title = it })

                                        Text("Artist")
                                        TextField(value = artist, onValueChange = { artist = it })
                                    }
                                    if (playbackType is PlaybackType.CustomizedSong || playbackType is PlaybackType.Playlist) {
                                        Text("Name")
                                        TextField(value = name, onValueChange = { name = it })
                                    }

                                    Button(
                                        onClick = {
                                            showMetadataDialog = false
                                            viewModel.updateMetadata(playback, title, artist, name)
                                        }
                                    ) {
                                        Text("Confirm")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}