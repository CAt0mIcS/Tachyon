package com.daton.database.data.repository

import com.daton.database.data.data_source.SongDao
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongRepositoryImpl(
    private val dao: SongDao
) : SongRepository {

    override fun getAll(): List<Song> =
        dao.getAll().map {
            LocalSongImpl(it.mediaId, it.title, it.artist, it.duration).apply {
                // TODO: Artwork
            }
        }

    override suspend fun removeIf(pred: (Song) -> Boolean) {
        getAll().forEach {
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