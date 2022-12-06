package com.daton.database.data.repository

import com.daton.database.data.data_source.SongDao
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Song

class SongRepositoryImpl(
    private val dao: SongDao
) : SongRepository {

    override suspend fun getSongs(): List<Song> =
        dao.getSongs().map {
            LocalSongImpl(it.mediaId, it.title, it.artist, it.duration).apply {
                // TODO: Artwork
            }
        }

    override suspend fun removeIf(pred: (Song) -> Boolean) {
        getSongs().forEach {
            if (pred(it))
                dao.delete(it.toEntity())
        }
    }

    override suspend fun addAll(songs: List<Song>) {
        addAllEntity(songs.map { it.toEntity() })
    }

    override suspend fun addAllEntity(songs: List<SongEntity>) {
        dao.addAll(songs)
    }
}

// TODO: AlbumArt
private fun Song.toEntity() = SongEntity(
    mediaId, title, artist, duration
)