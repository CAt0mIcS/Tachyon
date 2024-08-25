package com.tachyonmusic.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.presentation.BottomNavigationItem
import com.tachyonmusic.presentation.core_components.model.PlaybackUiEntity
import com.tachyonmusic.presentation.entry.SwipingStates
import com.tachyonmusic.presentation.home.component.VerticalPlaybackView
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.isEnabled
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import kotlinx.coroutines.launch


object HomeScreen :
    BottomNavigationItem(R.string.btmNav_home, R.drawable.ic_home, "home") {

    @Composable
    operator fun invoke(
        miniPlayerHeight: Dp,
        draggable: AnchoredDraggableState<SwipingStates>,
        viewModel: HomeViewModel = hiltViewModel()
    ) {
        val history by viewModel.history.collectAsState()
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
                            onClick = { viewModel.debugAction() },
                        ) {
                            Text(
                                "Debug Action",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }


            item {
                val rowState = rememberLazyListState()

                LaunchedEffect(remember { derivedStateOf { rowState.firstVisibleItemIndex } }) {
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
                        scope.launch {
                            draggable.animateTo(SwipingStates.EXPANDED)
                        }
                        viewModel.onItemClicked(it)
                    }
                }
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
                .isEnabled(playback.isPlayable)
                .shadow(Theme.shadow.small, shape = Theme.shapes.medium)
                .clip(Theme.shapes.medium)
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceContainerHighest),
                    shape = Theme.shapes.medium
                )
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, Theme.shapes.medium),
            playback = playback,
            artwork = playback.artwork ?: PlaceholderArtwork,
        )
    }
}
