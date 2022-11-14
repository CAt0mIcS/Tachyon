package com.tachyonmusic.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.presentation.component.PlaybacksView
import com.tachyonmusic.presentation.main.component.BottomNavigationItem
import com.tachyonmusic.presentation.player.PlayerScreen
import com.tachyonmusic.presentation.search.PlaybackSearchScreen


object HomeScreen :
    BottomNavigationItem(R.string.btmNav_home, R.drawable.ic_home, "home") {

    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: HomeViewModel = hiltViewModel()
    ) {
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
                modifier = Modifier.onFocusChanged {
                    if (it.hasFocus) navController.navigate(
                        PlaybackSearchScreen.route
                    )
                },
                value = "",
                onValueChange = {})

            PlaybacksView(items = history, albumArts = albumArts) {
                viewModel.onItemClicked(it)
                navController.navigate(PlayerScreen.route)
            }
        }
    }
}