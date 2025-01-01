package com.tachyonmusic.presentation.library.model

import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.util.displaySubtitle
import com.tachyonmusic.util.displayTitle

data class LibraryEntity(
    val title: String = "",
    val artist: String = "",
    val displayTitle: String = "",
    val displaySubtitle: String = "",
    val mediaId: MediaId,
    val isPlayable: Boolean = false,
    val album: String = "",

    val artwork: Artwork? = null,
    val albumArtworkSearchQuery: String = "",
    val playbackType: PlaybackType = mediaId.playbackType
)

fun Playback.toLibraryEntity() = LibraryEntity(
    title,
    artist,
    displayTitle,
    displaySubtitle,
    mediaId,
    isPlayable,
    album ?: "Unknown Album",
    artwork,
    album ?: "$artist $title"
)

fun Playlist.toLibraryEntity(): LibraryEntity {
    val firstPlayable = playbacks.find { it.isPlayable }
    val album = playbacks.find { it.isPlayable && it.album != null }?.album

    return LibraryEntity(
        firstPlayable?.title ?: "",
        firstPlayable?.artist ?: "",
        name,
        "${playbacks.size} Item${if (playbacks.size == 1) "" else "s"}",
        mediaId,
        firstPlayable != null,
        album ?: "",
        playbacks.find { it.hasArtwork }?.artwork,
        album
            ?: if (firstPlayable != null) "${firstPlayable.artist} ${firstPlayable.title}" else ""
    )
}