package com.tachyonmusic.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.*

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
                when (playback) {
                    is Song -> albumArts[playback] ?: false
                    is Loop -> albumArts[playback.song] ?: false
                    else -> false
                }
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