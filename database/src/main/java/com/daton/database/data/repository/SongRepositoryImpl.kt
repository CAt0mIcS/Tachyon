package com.daton.database.data.repository

import com.daton.database.data.data_source.SongDao
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongRepositoryImpl(
    private val dao: SongDao
) : SongRepository {
    override fun getAll(): Flow<List<Song>> =
        dao.getAll().map {
            List(it.size) { i ->
                LocalSongImpl(it[i].mediaId, it[i].title, it[i].artist, it[i].duration).apply {
                    // TODO: Artwork
                }
            }
        }
}