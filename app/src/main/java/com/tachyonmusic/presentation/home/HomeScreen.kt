package com.tachyonmusic.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.domain.use_case.search.SearchLocation
import com.tachyonmusic.presentation.BottomNavigationItem
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.core_components.LoadingBox
import com.tachyonmusic.presentation.core_components.model.PlaybackUiEntity
import com.tachyonmusic.presentation.home.component.VerticalPlaybackView
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.isEnabled
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import kotlinx.coroutines.launch


object HomeScreen :
    BottomNavigationItem(R.string.btmNav_home, R.drawable.ic_home, "home") {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    operator fun invoke(
        navController: NavController,
        sheetState: BottomSheetState,
        onSheetStateFraction: (Float) -> Unit,
        miniPlayerHeight: Dp,
        viewModel: HomeViewModel = hiltViewModel()
    ) {
        val isLoading by viewModel.isLoading.collectAsState()
        if (isLoading) {
            LoadingBox(zIndex = 0f)
        }

        var isSearching by remember { mutableStateOf(false) }
        var searchLocation by remember { mutableStateOf<SearchLocation>(SearchLocation.Local) }
        val history by viewModel.history.collectAsState()
        val searchResults by viewModel.searchResults.collectAsState()

        var searchText by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current

        val scope = rememberCoroutineScope()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = Theme.padding.medium,
                bottom = miniPlayerHeight + Theme.padding.medium
            )
        ) {

            item {
                val interactionSource: MutableInteractionSource =
                    remember { MutableInteractionSource() }

                BackHandler(isSearching) {
                    isSearching = false
                    searchText = ""
                    focusManager.clearFocus()
                }

                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            end = Theme.padding.medium,
                            top = Theme.padding.medium
                        )
                        .shadow(Theme.shadow.medium, shape = Theme.shapes.medium)
                        .background(Theme.colors.secondary, shape = Theme.shapes.medium)
                        .defaultMinSize(
                            minWidth = TextFieldDefaults.MinWidth,
                            minHeight = TextFieldDefaults.MinHeight
                        ),
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        isSearching = true
                        viewModel.search(it, searchLocation)
                    },

                    textStyle = TextStyle.Default.copy(
                        fontSize = 22.sp,
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
                                fontSize = 22.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_search),
                                contentDescription = "Search Playbacks",
                                modifier = Modifier.scale(1.2f)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                painterResource(searchLocation.icon),
                                contentDescription = "Change search location",
                                modifier = Modifier
                                    .scale(.9f)
                                    .clickable {
                                        searchLocation = searchLocation.next
                                        if (isSearching)
                                            viewModel.search(searchText, searchLocation)
                                    }
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

            // TODO: Don't rely on states to display search view/normal home view
            if (isSearching) {
                items(searchResults, key = { it.mediaId.toString() }) {
                    HorizontalPlaybackView(
                        playback = it,
                        artwork = it.artwork ?: PlaceholderArtwork,
                        modifier = Modifier.padding(
                            top = Theme.padding.small,
                            end = Theme.padding.medium
                        ),
                        onClick = {
                            viewModel.onItemClicked(it)
                            isSearching = false
                            searchText = ""
                            focusManager.clearFocus()
                            scope.launch {
                                sheetState.expand()
                            }
                            onSheetStateFraction(1f)
                        }
                    )
                }
            } else {

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
                                onClick = { viewModel.refreshArtwork() },
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
                    val rowState = rememberLazyListState()

                    LaunchedEffect(rowState.firstVisibleItemIndex) {
                        delay(200.ms)
                        viewModel.loadArtwork(
                            kotlin.math.max(
                                rowState.firstVisibleItemIndex - 2,
                                0
                            )..rowState.firstVisibleItemIndex + rowState.layoutInfo.visibleItemsInfo.size + 4
                        )
                    }

                    LazyRow(
                        state = rowState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = Theme.padding.small, top = Theme.padding.extraSmall),
                    ) {
                        playbacksView(history) {
                            viewModel.onItemClicked(it)
                            scope.launch {
                                sheetState.expand()
                            }
                            onSheetStateFraction(1f)
                        }
                    }
                }

//                item {
//
//                    Text(
//                        "Recommended for You",
//                        fontSize = 24.sp,
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier.padding(
//                            start = Theme.padding.medium,
//                            top = Theme.padding.large,
//                            end = Theme.padding.medium
//                        )
//                    )
//                }

//                item {
//                    // TODO: Recommendations
//                    LazyRow(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(start = Theme.padding.small, top = Theme.padding.small)
//                    ) {
//                        playbacksView(playbacks = history) {
//                            viewModel.onItemClicked(it)
//                            scope.launch {
//                                sheetState.expand()
//                            }
//                            onSheetStateFraction(1f)
//                        }
//                    }
//
//                }
            }
        }
    }
}


private fun LazyListScope.playbacksView(
    playbacks: List<PlaybackUiEntity>,
    onClick: (PlaybackUiEntity) -> Unit
) {
    items(playbacks) { playback ->

        VerticalPlaybackView(
            modifier = Modifier
                .padding(
                    start = Theme.padding.extraSmall / 2f,
                    end = Theme.padding.extraSmall / 2f
                )
                .clickable {
                    if (playback.isPlayable)
                        onClick(playback)
                }
                .isEnabled(playback.isPlayable),
            playback = playback,
            artwork = playback.artwork ?: PlaceholderArtwork,
        )
    }
}
