package com.tachyonmusic.presentation.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.model.Loop
import com.tachyonmusic.core.domain.model.Playlist
import com.tachyonmusic.core.domain.model.Song
import com.tachyonmusic.presentation.authentication.SignInScreen
import com.tachyonmusic.presentation.main.component.BottomNavigationItem

object LibraryScreen :
    BottomNavigationItem(R.string.btmNav_library, R.drawable.ic_library, "library") {

    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: LibraryViewModel = hiltViewModel()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Button(
                    onClick = { navController.navigate(SignInScreen.route) },
                ) {
                    Text("Sign In")
                }
            }
            items(viewModel.playbacks.value) { playback ->
                Text(
                    text = when (playback) {
                        is Playlist -> playback.name
                        is Loop -> "${playback.name} - ${playback.title} - ${playback.artist}"
                        is Song -> "${playback.title} - ${playback.artist}"
                    },
                    modifier = Modifier.clickable { viewModel.onPlaybackClicked(playback) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

    }
}
