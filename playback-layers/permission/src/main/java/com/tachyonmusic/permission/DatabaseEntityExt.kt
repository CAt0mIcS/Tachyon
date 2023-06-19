package com.tachyonmusic.permission

import com.tachyonmusic.core.data.playback.*
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.CustomizedSongEntity
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.model.SongEntity

fun SongEntity.toSong(isPlayable: Boolean, artwork: Artwork? = null): Song =
    if (mediaId.isLocalSong) {
        LocalSong(mediaId.uri!!, mediaId, title, artist, duration).let {
            it.isPlayable = isPlayable
            it.artwork = artwork
            it
        }
    } else if (mediaId.isSpotifySong) {
        SpotifySong(mediaId, title, artist, duration).let {
            it.isPlayable = isPlayable
            it.artwork = artwork
            it
        }
    } else {
        TODO("Invalid song conversion media id $mediaId")
    }

fun CustomizedSongEntity.toCustomizedSong(
    isPlayable: Boolean,
    song: Song = LocalSong(
        mediaId.underlyingMediaId!!.uri!!,
        mediaId.underlyingMediaId!!,
        title,
        artist,
        duration
    ).let {
        it.isPlayable = isPlayable
        it
    }
): CustomizedSong = LocalCustomizedSong(
    mediaId,
    song
).let {
    it.timingData = timingData?.let { TimingDataController(it, currentTimingDataIndex) }
    it.bassBoost = bassBoost
    it.virtualizerStrength = virtualizerStrength
    it.equalizerBands = equalizerBands
    it.playbackParameters = playbackParameters
    it.reverb = reverb
    it
}

fun SinglePlaybackEntity.toPlayback(isPlayable: Boolean): SinglePlayback = when (this) {
    is SongEntity -> toSong(isPlayable)
    is CustomizedSongEntity -> toCustomizedSong(isPlayable)
    else -> TODO("Invalid SinglePlayback type ${this::class.java.name}")
}

fun PlaylistEntity.toPlaylist(items: List<SinglePlayback>): Playlist =
    if (mediaId.isSpotifyPlaylist) {
        SpotifyPlaylist(
            name,
            mediaId,
            items.map { it as SpotifySong }.toMutableList(),
            currentItemIndex
        )
    } else if (mediaId.isLocalPlaylist) {
        LocalPlaylist.build(mediaId, items.toMutableList(), currentItemIndex)
    } else {
        TODO("Invalid playlist conversion media id $mediaId")
    }


