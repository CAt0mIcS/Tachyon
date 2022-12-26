package com.tachyonmusic.presentation.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.data.PlaceholderArtwork
import com.tachyonmusic.presentation.BottomNavigationItem
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.library.component.FilterItem
import com.tachyonmusic.presentation.player.PlayerScreen
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.theme.extraLarge
import kotlinx.coroutines.launch

object LibraryScreen :
    BottomNavigationItem(R.string.btmNav_library, R.drawable.ic_library, "library") {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    operator fun invoke(
        navController: NavController,
        sheetState: BottomSheetState,
        viewModel: LibraryViewModel = hiltViewModel()
    ) {
        var selectedFilter by remember { mutableStateOf(0) }
        var sortOptionsExpanded by remember { mutableStateOf(false) }
        var sortText by remember { mutableStateOf("Alphabetically") }

        val scope = rememberCoroutineScope()

        val playbackItems = viewModel.items.collectAsLazyPagingItems()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = Theme.padding.medium,
                    top = Theme.padding.medium,
                    end = Theme.padding.medium
                ),
            contentPadding = PaddingValues(bottom = Theme.padding.small)
        ) {

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            Theme.shadow.small,
                            shape = Theme.shapes.extraLarge
                        ) // TODO: Shadow not working?
                        .horizontalScroll(rememberScrollState())
                        .background(Theme.colors.secondary, shape = Theme.shapes.extraLarge)
                        .padding(
                            start = Theme.padding.medium,
                            top = Theme.padding.extraSmall,
                            end = Theme.padding.medium,
                            bottom = Theme.padding.extraSmall
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    FilterItem("Songs", selectedFilter == 0) {
                        selectedFilter = 0
                        viewModel.onFilterSongs()
                    }

                    FilterItem("Loops", selectedFilter == 1) {
                        selectedFilter = 1
                        viewModel.onFilterLoops()
                    }

                    FilterItem("Playlists", selectedFilter == 2) {
                        selectedFilter = 2
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
                    }
                ) {
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

                        DropdownMenu(
                            expanded = sortOptionsExpanded,
                            onDismissRequest = { sortOptionsExpanded = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                sortText = "Alphabetically"
                                sortOptionsExpanded = false
                            }) {
                                Text("Alphabetically")
                            }

                            DropdownMenuItem(onClick = {
                                sortText = "Creation Date"
                                sortOptionsExpanded = false
                            }) {
                                Text("Creation Date")
                            }
                        }
                    }

                    Text(
                        modifier = Modifier.padding(start = Theme.padding.medium),
                        text = sortText,
                        fontSize = 18.sp,
                        color = iconAndTextColor
                    )
                }
            }

            items(playbackItems) { playback ->

                if (playback != null) {
                    val artwork by if (playback is SinglePlayback)
                        playback.artwork.collectAsState()
                    else
                        (playback as Playlist).playbacks.first().artwork.collectAsState()

                    val isArtworkLoading by playback.isArtworkLoading.collectAsState()

                    HorizontalPlaybackView(
                        playback,
                        artwork ?: PlaceholderArtwork(R.drawable.artwork_image_placeholder),
                        isArtworkLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Theme.padding.extraSmall),
                        onClick = {
                            viewModel.onItemClicked(playback)
                            scope.launch {
                                sheetState.expand()
                            }
                        }
                    )
                }
            }
        }
    }
}

