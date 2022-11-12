package com.tachyonmusic.presentation.main

import android.view.KeyEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.presentation.main.component.BottomNavigationItem


object HomeScreen :
    BottomNavigationItem(R.string.btmNav_home, R.drawable.ic_home, "home") {

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: HomeViewModel = hiltViewModel()
    ) {

        val searchString by viewModel.searchString
        val searchResults by viewModel.searchResults
        var searchFocus by remember { mutableStateOf(false) }
        val history by viewModel.history.collectAsState()
        val albumArts = viewModel.albumArtworkLoading

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            TextField(
                modifier = Modifier
                    .onFocusChanged { searchFocus = it.isFocused }
                    .onKeyEvent {
                        // TODO: Better way to go back to home page when searching
                        if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                            searchFocus = false
                            viewModel.onSearch("")
                            return@onKeyEvent true
                        }
                        false
                    },
                value = searchString,
                singleLine = true,
                onValueChange = { viewModel.onSearch(it) })

            AnimatedContent(
                targetState = searchFocus,
                modifier = Modifier.fillMaxWidth(),
                transitionSpec = {
                    fadeIn(tween(1000)) with fadeOut(tween(1000))
                }
            ) {
                if (searchFocus) {

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(searchResults) { playback ->
                            Column {
                                PlaybackView(
                                    playback as SinglePlayback,
                                    albumArts[playback] ?: false
                                )
                                Text(playback.toString())
                            }

                        }
                    }

                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {

                        LazyColumn {
                            items(history) { playback ->
                                PlaybackView(
                                    playback as SinglePlayback,
                                    albumArts[playback] ?: false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PlaybackView(playback: SinglePlayback, artworkLoading: Boolean) {
    if (playback.artwork != null) {
        Image(
            bitmap = playback.artwork!!.asImageBitmap(),
            contentDescription = "Album Art"
        )
    } else if (artworkLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            CircularProgressIndicator()
        }
    }
}