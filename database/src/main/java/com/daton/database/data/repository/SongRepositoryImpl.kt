package com.daton.database.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.daton.database.data.data_source.SongDao
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.SongRepository
import com.daton.database.data.repository.shared_action.ConvertEntityToSong
import com.daton.database.data.repository.shared_action.UpdateArtwork
import com.daton.database.domain.repository.HistoryRepository
import com.daton.database.domain.repository.LoopRepository
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongRepositoryImpl(
    private val dao: SongDao,
    private val convertEntityToSong: ConvertEntityToSong,
) : SongRepository {

    override suspend fun getSongs(): List<Song> =
        dao.getSongs().map { song ->
            convertEntityToSong(song)
        }

    override fun getPagedSongs(
        pageSize: Int,
        prefetchDistance: Int,
        initialLoadSize: Int
    ): Flow<PagingData<Song>> {
        val pagingSourceFactory = { dao.getPagedSongs() }
        return Pager(
            config = PagingConfig(pageSize, prefetchDistance, initialLoadSize = initialLoadSize),
            // TODO: Mediator (https://farhan-tanvir.medium.com/clean-architecture-in-android-jetpack-compose-paging-3-0-kotlin-mvvm-%E3%83%BCpart-2-8d97cee4dffe)
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { songData ->
            songData.map { song ->
                convertEntityToSong(song)
            }
        }
    }

    override suspend fun findByMediaId(mediaId: MediaId): SongEntity? =
        dao.getSongWithMediaId(mediaId.toString())

    override suspend fun getSongEntities(): List<SongEntity> = dao.getSongs()

    // TODO: Bad performance? Should be changed to have less db queries
    override suspend fun removeIf(pred: (SongEntity) -> Boolean) {
        getSongEntities().forEach {
            if (pred(it))
                dao.delete(it)
        }
    }

    override suspend fun addAll(songs: List<SongEntity>) {
        dao.addAll(songs)
    }

    override suspend fun updateArtwork(song: SongEntity, artworkType: String, artworkUrl: String?) {
        dao.updateArtwork(song.id ?: return, artworkType, artworkUrl)
    }

    override suspend fun getSongsWithArtworkType(artworkType: String) =
        dao.getSongsWithArtworkType(artworkType)

}