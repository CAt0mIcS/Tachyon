package com.tachyonmusic.presentation.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.presentation.authentication.SignInScreen
import com.tachyonmusic.presentation.main.component.BottomNavigationItem
import com.tachyonmusic.presentation.player.PlayerScreen
import kotlinx.coroutines.runBlocking

object LibraryScreen :
    BottomNavigationItem(R.string.btmNav_library, R.drawable.ic_library, "library") {

    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: LibraryViewModel = hiltViewModel()
    ) {
        val songs by viewModel.songs.collectAsState()
        val loops by viewModel.loops.collectAsState()
        val playlists by viewModel.playlist.collectAsState()

        LazyColumn(
            modifier = Modifier
                .padding(bottom = HEIGHT)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Button(
                    onClick = { navController.navigate(SignInScreen.route) },
                ) {
                    Text("Sign In")
                }
            }

            item {
                Button(
                    onClick = { viewModel.addItem() }
                )
                {
                    Text("Add item to list")
                }
            }

            runBlocking {
                items(songs + loops + playlists) { playback ->
                    Text(
                        text =
                        when (playback) {
                            is Song -> "${playback.title} - ${playback.artist}"
                            is Loop -> "${playback.name} - ${playback.title} - ${playback.artist}"
                            else -> (playback as Playlist).name
                        },
                        modifier = Modifier.clickable {
                            viewModel.onItemClicked(playback)
                            navController.navigate(PlayerScreen.route)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

