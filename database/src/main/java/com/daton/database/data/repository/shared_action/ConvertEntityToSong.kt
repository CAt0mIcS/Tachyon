package com.daton.database.data.repository.shared_action

import com.daton.database.domain.model.SongEntity
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Song

class ConvertEntityToSong(
    private val getArtworkForPlayback: GetArtworkForPlayback
) {
    operator fun invoke(song: SongEntity): Song =
        LocalSongImpl(song.mediaId, song.title, song.artist, song.duration).apply {
            this.artwork.value = getArtworkForPlayback(song)
        }
}