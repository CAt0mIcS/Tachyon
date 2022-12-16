package com.daton.database.data.data_source.firestore

import androidx.paging.PagingSource
import com.daton.database.data.data_source.SongDao
import com.daton.database.domain.model.SongEntity

class SongDaoImpl : SongDao {
    override fun getPagedSongs(): PagingSource<Int, SongEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getSongs(): List<SongEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getSongWithMediaId(mediaId: String): SongEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun getSongsWithArtworkTypes(artworkTypes: Array<out String>): List<SongEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun add(song: SongEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun addAll(songs: List<SongEntity>) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(song: SongEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun updateArtwork(id: Int, artworkType: String?, artworkUrl: String?) {
        TODO("Not yet implemented")
    }
}