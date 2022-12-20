package com.daton.database.data.repository.shared_action

import com.daton.database.domain.model.PlaylistEntity
import com.daton.database.domain.repository.LoopRepository
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.playback.RemotePlaylistImpl
import com.tachyonmusic.core.domain.playback.Playlist

class ConvertEntityToPlaylist(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val convertEntityToSong: ConvertEntityToSong,
    private val convertEntityToLoop: ConvertEntityToLoop
) {
    suspend operator fun invoke(playlist: PlaylistEntity): Playlist? =
        RemotePlaylistImpl.build(
            playlist.mediaId,
            playlist.items.map {
                val song = songRepository.findByMediaId(it)
                if (song != null)
                    return@map convertEntityToSong(song)

                val loop = loopRepository.findByMediaId(it)
                if (loop != null)
                    convertEntityToLoop(loop)
                else
                    return null
            }.toMutableList(),
            playlist.currentItemIndex
        )
}