package com.tachyonmusic.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song


val Playback.displayTitle: String
    get() = when (this) {
        is Song -> title
        is Loop -> name
        is Playlist -> name
        else -> TODO("Invalid playback type ${this.javaClass.name}")
    }

val Playback.displaySubtitle: String
    @Composable
    get() = when (this) {
        is Song -> artist
        is Loop -> stringResource(R.string.song_name_and_artist, title, artist)
        is Playlist -> "${playbacks.size} Item(s)"
        else -> TODO("Invalid playback type ${this.javaClass.name}")
    }