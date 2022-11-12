package com.tachyonmusic.presentation.main

import android.view.KeyEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.*
import com.tachyonmusic.presentation.main.component.BottomNavigationItem
import com.tachyonmusic.presentation.player.PlayerScreen


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

        LaunchedEffect(true) {
            // Load album art when the view is active, TODO: Unload when view becomes inactive and don't load on main coroutine
            viewModel.loadArtworkState(history.filterIsInstance<Song>())
        }

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
                    PlaybacksView(items = searchResults, albumArts = albumArts) {
                        viewModel.onItemClicked(it)
                        navController.navigate(PlayerScreen.route)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        PlaybacksView(items = history, albumArts = albumArts) {
                            viewModel.onItemClicked(it)
                            navController.navigate(PlayerScreen.route)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaybacksView(
    items: List<Playback>,
    albumArts: Map<Song, Boolean>,
    onClick: (Playback) -> Unit
) {
    LazyColumn {
        items(items) { playback ->
            PlaybackView(
                playback,
                albumArts[playback] ?: false,
            ) {
                onClick(playback)
            }
        }
    }
}


@Composable
fun PlaybackView(playback: Playback, artworkLoading: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        when (playback) {
            is Song -> {
                PlaybackArtwork(playback, artworkLoading)
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(playback.title)
                    Text(modifier = Modifier.padding(start = 6.dp), text = playback.artist)
                }
            }
            is Loop -> {
                PlaybackArtwork(playback, artworkLoading)
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(playback.name)
                    Text(modifier = Modifier.padding(start = 6.dp), text = playback.title)
                    Text(modifier = Modifier.padding(start = 6.dp), text = playback.artist)
                }
            }
            is Playlist -> {

            }
        }
    }
}

@Composable
fun PlaybackArtwork(playback: SinglePlayback, artworkLoading: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
    ) {
        if (playback.artwork != null)
            Image(
                bitmap = playback.artwork!!.asImageBitmap(),
                contentDescription = "Album Art"
            )
        else if (artworkLoading)
            CircularProgressIndicator()
        else
            Image(
                painterResource(R.drawable.artwork_image_placeholder),
                "Placeholder Playback Artwork"
            )
    }
}