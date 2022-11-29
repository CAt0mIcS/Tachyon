package com.tachyonmusic.presentation.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.*
import com.tachyonmusic.presentation.main.component.BottomNavigationItem
import com.tachyonmusic.presentation.theme.NoRippleTheme
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
                .padding(Theme.padding.medium)
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
                        .background(Theme.colors.surface, shape = Theme.shapes.extraLarge)
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
                        if (sortOptionsExpanded) Theme.colors.onSurface else Theme.colors.onBackground,
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

                PlaybackView(
                    playback,
                    if (playback is SinglePlayback)
                        playback.artwork?.asImageBitmap()
                    else
                        (playback as Playlist).playbacks.firstOrNull()?.artwork?.asImageBitmap()
                )
            }
        }
    }
}


@Composable
private fun FilterItem(text: String, selected: Boolean = false, onClick: () -> Unit) {

    val selectedColor = Theme.colors.onSecondary
    val unselectedColor = Theme.colors.primary

    val color by animateColorAsState(
        if (selected) selectedColor else unselectedColor,
        tween(Theme.animation.medium)
    )

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Button(
            shape = Theme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = color,
                contentColor = Theme.colors.onSurface
            ),
            onClick = {
                onClick()
            }
        ) {
            Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun PlaybackView(playback: Playback, artwork: ImageBitmap? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Theme.padding.extraSmall)
            .shadow(Theme.shadow.small, shape = Theme.shapes.medium)
            .background(Theme.colors.surface, shape = Theme.shapes.medium)
            .border(
                BorderStroke(
                    1.dp,
                    Color(
                        Theme.colors.secondary.red,
                        Theme.colors.secondary.green,
                        Theme.colors.secondary.blue,
                        Theme.colors.secondary.alpha * .7f
                    )
                ), shape = Theme.shapes.medium
            )
    ) {
        if (artwork != null)
            Image(
                bitmap = artwork,
                contentDescription = "Album Artwork",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(50.dp, 50.dp)
                    .clip(Theme.shapes.medium)
            )
        else
            Image(
                painterResource(R.drawable.artwork_image_placeholder),
                "Album Artwork Placeholder",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(50.dp, 50.dp)
                    .clip(Theme.shapes.medium)
            )

        Column(modifier = Modifier.padding(start = Theme.padding.small)) {
            Text(
                modifier = Modifier.padding(top = Theme.padding.small),
                text = playback.title ?: "No Title",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                modifier = Modifier.padding(
                    start = Theme.padding.small,
                    bottom = Theme.padding.small
                ),
                text = playback.artist ?: "No Artist",
                fontSize = 12.sp
            )
        }


    }
}
