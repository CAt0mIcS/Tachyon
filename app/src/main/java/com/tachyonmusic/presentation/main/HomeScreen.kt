package com.tachyonmusic.presentation.main

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
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
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.presentation.BottomNavigationItem
import com.tachyonmusic.presentation.main.component.VerticalPlaybackView
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.isEnabled
import kotlinx.coroutines.launch


object HomeScreen :
    BottomNavigationItem(R.string.btmNav_home, R.drawable.ic_home, "home") {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    operator fun invoke(
        navController: NavController,
        sheetState: BottomSheetState,
        miniPlayerHeight: MutableState<Dp>,
        viewModel: HomeViewModel = hiltViewModel()
    ) {
        var searchText by remember { mutableStateOf("") }
        val history by viewModel.history.collectAsState()

        val scope = rememberCoroutineScope()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = Theme.padding.medium,
                bottom = miniPlayerHeight.value + Theme.padding.medium
            )
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
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Theme.padding.small, top = Theme.padding.extraSmall),
                ) {
                    playbacksView(history) {
                        viewModel.onItemClicked(it)
                        scope.launch {
                            sheetState.expand()
                        }
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
                        scope.launch {
                            sheetState.expand()
                        }
                    }
                }

            }
        }
    }
}


private fun LazyListScope.playbacksView(
    playbacks: List<SinglePlayback>,
    onClick: (SinglePlayback) -> Unit
) {
    items(playbacks) { playback ->

        val artwork by playback.artwork.collectAsState()
        val isArtworkLoading by playback.isArtworkLoading.collectAsState()
        val isPlayable by playback.isPlayable.collectAsState()

        VerticalPlaybackView(
            modifier = Modifier
                .padding(
                    start = Theme.padding.extraSmall / 2f,
                    end = Theme.padding.extraSmall / 2f
                )
                .clickable {
                    if (isPlayable)
                        onClick(playback)
                }
                .isEnabled(isPlayable),
            playback = playback,
            artwork = artwork ?: PlaceholderArtwork,
            isArtworkLoading = isArtworkLoading
        )
    }
}
