package com.tachyonmusic.presentation.main

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
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
import com.tachyonmusic.presentation.main.component.BottomNavigationItem
import com.tachyonmusic.presentation.theme.Theme
import kotlinx.coroutines.delay


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
                    playbacksView(history)
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
                    playbacksView(playbacks = history)
                }
            }

            item {
                // This ensures that the shadow isn't cut off by the BottomNavigationBar's padding
                // TODO: Maybe use LazyColum.contentPadding(bottom)
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Theme.shadow.large)
                )
            }
        }

        if(recentlyPlayed != null) {
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


fun LazyListScope.playbacksView(playbacks: List<Playback>) {
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

        PlaybackView(
            modifier = Modifier.padding(padding),
            playback = playbacks[i],
            artwork = (playbacks[i] as Song).artwork?.asImageBitmap()
        )
    }
}


@Composable
fun PlaybackView(playback: Playback, artwork: ImageBitmap? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shadow(Theme.shadow.small, shape = Theme.shapes.medium)
            .background(Theme.colors.secondary, shape = Theme.shapes.medium)
            .border(BorderStroke(1.dp, Theme.colors.border), shape = Theme.shapes.medium)
    ) {
        if (artwork != null)
            Image(
                bitmap = artwork,
                contentDescription = "Album Artwork",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(100.dp, 100.dp)
                    .clip(Theme.shapes.medium)
            )
        else
            Image(
                painterResource(R.drawable.artwork_image_placeholder),
                "Album Artwork Placeholder",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(100.dp, 100.dp)
                    .clip(Theme.shapes.medium)
            )

        Text(
            modifier = Modifier.padding(start = Theme.padding.small),
            text = playback.title ?: "No Title",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Text(
            modifier = Modifier.padding(
                start = Theme.padding.small * 2,
                bottom = Theme.padding.small
            ),
            text = playback.artist ?: "No Artist",
            fontSize = 12.sp
        )
    }
}


@Composable
fun MiniPlayer(
    playback: Playback,
    currentPosition: Float,
    artwork: ImageBitmap? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Theme.padding.extraSmall,
                top = Theme.padding.extraSmall,
                end = Theme.padding.extraSmall
            )
            .shadow(Theme.shadow.small, shape = Theme.shapes.medium)
            .background(Theme.colors.tertiary, shape = Theme.shapes.medium)
    ) {
        if (artwork != null)
            Image(
                bitmap = artwork,
                contentDescription = "Album Artwork",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(48.dp, 48.dp)
                    .clip(Theme.shapes.medium)
            )
        else
            Image(
                painterResource(R.drawable.artwork_image_placeholder),
                "Album Artwork Placeholder",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(48.dp, 48.dp)
                    .clip(Theme.shapes.medium)
            )

        Row(
            modifier = Modifier.padding(start = Theme.padding.small),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    modifier = Modifier.padding(top = Theme.padding.small),
                    text = playback.title ?: "No Title",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2
                )

                Text(
                    modifier = Modifier.padding(
                        start = Theme.padding.small,
                        bottom = Theme.padding.small
                    ),
                    text = playback.artist ?: "No Artist",
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            IconButton(onClick = { /*TODO*/ }) {
                Icon(painterResource(R.drawable.ic_play), contentDescription = "Play")
            }
        }
    }

    ProgressIndicator(
        progress = currentPosition,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Theme.padding.medium, end = Theme.padding.medium),
        color = Theme.colors.orange,
        backgroundColor = Theme.colors.partialOrange
    )
}


@Composable
fun ProgressIndicator(
    progress: Float,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width

        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(x = 0f, y = -10f),
            size = Size(width = width, height = 10f),
            cornerRadius = CornerRadius(100f, 100f)
        )

        drawRoundRect(
            color = color,
            topLeft = Offset(x = 0f, y = -10f),
            size = Size(width = width * progress, height = 10f),
            cornerRadius = CornerRadius(100f, 100f)
        )
    }
}