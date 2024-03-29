package com.tachyonmusic.presentation.core_components.model

import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms

data class PlaybackUiEntity(
    val title: String,
    val artist: String,
    val displayTitle: String,
    val displaySubtitle: String,
    val duration: Duration,
    val mediaId: MediaId,
    val playbackType: PlaybackType,
    val artwork: Artwork?,
    val isPlayable: Boolean
)

fun Playback.toUiEntity() = when (this) {
    is Song -> PlaybackUiEntity(
        title,
        artist,
        title,
        artist,
        duration,
        mediaId,
        playbackType,
        artwork,
        isPlayable
    )
    is CustomizedSong -> PlaybackUiEntity(
        title,
        artist,
        name,
        if(isPlayable) "$title by $artist" else "Missing: ${mediaId.uri}",
        duration,
        mediaId,
        playbackType,
        artwork,
        isPlayable
    )
    is Playlist -> PlaybackUiEntity(
        playbacks.first().title,
        playbacks.first().artist,
        name,
        "${playbacks.size} Item(s)",
        0.ms,
        mediaId,
        playbackType,
        playbacks.find { it.hasArtwork }?.artwork,
        true
    )
    else -> TODO("Invalid playback type ${this.javaClass.name}")
}