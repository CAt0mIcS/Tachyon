package com.tachyonmusic.presentation.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.presentation.BottomNavigationItem
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.library.component.FilterItem
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.theme.extraLarge
import com.tachyonmusic.presentation.util.asString
import com.tachyonmusic.presentation.util.isEnabled
import com.tachyonmusic.playback_layers.SortType
import kotlinx.coroutines.launch

object LibraryScreen :
    BottomNavigationItem(R.string.btmNav_library, R.drawable.ic_library, "library") {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    operator fun invoke(
        sheetState: BottomSheetState,
        onSheetStateFraction: (Float) -> Unit,
        viewModel: LibraryViewModel = hiltViewModel()
    ) {
        var sortOptionsExpanded by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        val playbackType by viewModel.filterType.collectAsState()
        val playbackItems by viewModel.items.collectAsState()

        LazyColumn(
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
                        .background(Theme.colors.secondary, shape = Theme.shapes.extraLarge)
                        .padding(
                            start = Theme.padding.medium,
                            top = Theme.padding.extraSmall,
                            end = Theme.padding.medium,
                            bottom = Theme.padding.extraSmall
                        ), horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterItem(R.string.songs, playbackType is PlaybackType.Song) {
                        viewModel.onFilterSongs()
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    FilterItem(
                        R.string.customized_songs,
                        playbackType is PlaybackType.CustomizedSong
                    ) {
                        viewModel.onFilterCustomizedSongs()
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    FilterItem(R.string.playlists, playbackType is PlaybackType.Playlist) {
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
                        if (sortOptionsExpanded) Theme.colors.contrastHigh else Theme.colors.contrastLow,
                        tween(Theme.animation.short)
                    )

                    Box {

                        Icon(
                            painter = painterResource(R.drawable.ic_sort),
                            contentDescription = "Open Sorting Options",
                            tint = iconAndTextColor,
                            modifier = Modifier.scale(1.3f)
                        )


                        DropdownMenu(expanded = sortOptionsExpanded,
                            onDismissRequest = { sortOptionsExpanded = false }) {
                            SortType.values().forEach {
                                DropdownMenuItem(
                                    onClick = {
                                        viewModel.onSortTypeChanged(it)
                                        sortOptionsExpanded = false
                                    }
                                ) {
                                    Text(it.asString())
                                }
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
                // TODO: Shouldn't be checked in UI
                val artwork: Artwork?
                val isLoading: Boolean
                val isPlayable: Boolean
                when (playback) {
                    is SinglePlayback -> {
                        artwork = playback.artwork
                        isLoading = playback.isArtworkLoading
                        isPlayable = playback.isPlayable
                    }
                    is Playlist -> {
                        artwork = playback.playbacks.firstOrNull()?.artwork
                        isLoading = playback.playbacks.firstOrNull()?.isArtworkLoading ?: false
                        isPlayable = true
                    }
                    else -> error("Invalid playback type ${playback::class.java.name}")
                }

                HorizontalPlaybackView(
                    playback,
                    artwork ?: PlaceholderArtwork,
                    isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Theme.padding.extraSmall)
                        .isEnabled(isPlayable),
                    onClick = {
                        if (isPlayable) {
                            viewModel.onItemClicked(playback)
                            scope.launch {
                                sheetState.expand()
                            }
                            onSheetStateFraction(1f)
                        }
                    })
            }
        }
    }
}

