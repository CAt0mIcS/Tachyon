package com.tachyonmusic.presentation.util

import androidx.compose.runtime.Composable
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song


val Playback.displayTitle: String
    get() = when (this) {
        is Song -> title
        is CustomizedSong -> name
        is Playlist -> name
        else -> TODO("Invalid playback type ${this.javaClass.name}")
    }

val Playback.displaySubtitle: String
    //    @Composable
    get() = when (this) {
        is Song -> artist
//        is CustomizedSong -> stringResource(R.string.song_name_and_artist, title, artist)
        is CustomizedSong -> "$title by $artist"
        is Playlist -> "${playbacks.size} Item(s)"
        else -> TODO("Invalid playback type ${this.javaClass.name}")
    }