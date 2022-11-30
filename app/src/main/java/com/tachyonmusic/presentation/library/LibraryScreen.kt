package com.tachyonmusic.presentation.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.*
import com.tachyonmusic.presentation.BottomNavigationItem
import com.tachyonmusic.presentation.library.component.FilterItem
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.theme.extraLarge

object LibraryScreen :
    BottomNavigationItem(R.string.btmNav_library, R.drawable.ic_library, "library") {

    @Composable
    operator fun invoke(
        viewModel: LibraryViewModel = hiltViewModel()
    ) {
        var selectedFilter by remember { mutableStateOf(0) }
        var sortOptionsExpanded by remember { mutableStateOf(false) }
        var sortText by remember { mutableStateOf("Alphabetically") }

        val playbackItems by viewModel.items

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

                HorizontalPlaybackView(
                    playback,
                    if (playback is SinglePlayback)
                        playback.artwork?.asImageBitmap()
                    else
                        (playback as Playlist).playbacks.firstOrNull()?.artwork?.asImageBitmap(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Theme.padding.extraSmall)
                )
            }
        }
    }
}

