package com.tachyonmusic.presentation.main

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.presentation.BottomNavigationItem
import com.tachyonmusic.presentation.main.component.MiniPlayer
import com.tachyonmusic.presentation.theme.Theme
import kotlinx.coroutines.delay
import com.tachyonmusic.presentation.main.component.VerticalPlaybackView
import com.tachyonmusic.presentation.player.PlayerScreen


object HomeScreen :
    BottomNavigationItem(R.string.btmNav_home, R.drawable.ic_home, "home") {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: HomeViewModel = hiltViewModel()
    ) {
        var searchText by remember { mutableStateOf("") }
        val history by viewModel.history.collectAsState()
        val recentlyPlayed by viewModel.recentlyPlayed

        val isPlaying by viewModel.isPlaying
        var currentPosition by remember { mutableStateOf(viewModel.currentPositionNormalized) }

        var bottomPaddingRequiredByMiniPlayer by remember { mutableStateOf(0.dp) }

        DisposableEffect(Unit) {
            viewModel.registerPlayerListener()
            onDispose {
                viewModel.unregisterPlayerListener()
            }
        }

        if (isPlaying) {
            LaunchedEffect(Unit) {
                while (true) {
                    currentPosition = viewModel.currentPositionNormalized
                    delay(viewModel.audioUpdateInterval)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomPaddingRequiredByMiniPlayer)
        ) {

            item {
                val interactionSource: MutableInteractionSource =
                    remember { MutableInteractionSource() }

                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Theme.padding.small,
                            end = Theme.padding.small,
                            top = Theme.padding.medium
                        )
                        .shadow(Theme.shadow.medium, shape = Theme.shapes.medium)
                        .background(Theme.colors.secondary, shape = Theme.shapes.medium)
                        .defaultMinSize(
                            minWidth = TextFieldDefaults.MinWidth,
                            minHeight = TextFieldDefaults.MinHeight
                        ),
                    value = searchText,
                    onValueChange = { searchText = it },
                    textStyle = TextStyle.Default.copy(
                        fontSize = 25.sp,
                        color = Theme.colors.contrastLow
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Theme.colors.contrastLow)
                ) { innerTextField ->
                    TextFieldDefaults.TextFieldDecorationBox(
                        value = searchText,
                        innerTextField = innerTextField,
                        placeholder = { //
                            Text(
                                text = stringResource(androidx.appcompat.R.string.search_menu_title),
                                fontSize = 25.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_search),
                                contentDescription = "Search Playbacks",
                                modifier = Modifier.scale(1.4f)
                            )
                        },
                        interactionSource = interactionSource,
                        visualTransformation = VisualTransformation.None,
                        singleLine = true,
                        enabled = true,
                        isError = false,
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Theme.colors.secondary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Theme.colors.contrastLow
                        ),
                        contentPadding = PaddingValues(0.dp)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(
                            start = Theme.padding.medium,
                            top = Theme.padding.large,
                            end = Theme.padding.small
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Recently Played",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = {},
                        ) {
                            Text(
                                "View All",
                                color = Theme.colors.blue,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }


            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Theme.padding.small, top = Theme.padding.extraSmall),
                ) {
                    playbacksView(history) {
                        viewModel.onItemClicked(it)
                        navController.navigate(PlayerScreen.route)
                    }
                }
            }

            item {
                Text(
                    "Recommended for You",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        start = Theme.padding.medium,
                        top = Theme.padding.large,
                        end = Theme.padding.medium
                    )
                )
            }

            item {
                // TODO: Recommendations
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Theme.padding.small, top = Theme.padding.small)
                ) {
                    playbacksView(playbacks = history) {
                        viewModel.onItemClicked(it)
                        navController.navigate(PlayerScreen.route)
                    }
                }
            }
        }

        if (recentlyPlayed != null) {
            Layout(
                modifier = Modifier.fillMaxSize(),
                content = {
                    MiniPlayer(
                        playback = recentlyPlayed ?: return,
                        artwork = (recentlyPlayed as Song).artwork?.asImageBitmap(),
                        currentPosition = currentPosition
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
                        // TODO: Many recompositions?
                        if (bottomPaddingRequiredByMiniPlayer == 0.dp && placeable.height != 0) {
                            bottomPaddingRequiredByMiniPlayer = placeable.height.toDp()
                            println("BottomPadding $bottomPaddingRequiredByMiniPlayer")
                        }

                        // Position items at the bottom of the screen, excluding BottomNavBar
                        placeable.placeRelative(x = 0, y = constraints.maxHeight - placeable.height)
                    }
                }
            }
        }
    }
}


fun LazyListScope.playbacksView(playbacks: List<Playback>, onClick: (Playback) -> Unit) {
    items(playbacks.size) { i ->

        // Apply extra padding to the start of the first playback and to the end of the last
        val padding = if (i == 0) {
            PaddingValues(start = Theme.padding.medium, end = Theme.padding.extraSmall / 2f)
        } else if (i > 0 && i < playbacks.size - 1) {
            PaddingValues(
                start = Theme.padding.extraSmall / 2f,
                end = Theme.padding.extraSmall / 2f
            )
        } else {
            PaddingValues(
                start = Theme.padding.extraSmall / 2f,
                end = Theme.padding.medium
            )
        }

        VerticalPlaybackView(
            modifier = Modifier
                .padding(padding)
                .clickable {
                    onClick(playbacks[i])
                },
            playback = playbacks[i],
            artwork = (playbacks[i] as Song).artwork?.asImageBitmap(),
        )
    }
}
