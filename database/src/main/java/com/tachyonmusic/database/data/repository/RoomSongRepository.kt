package com.tachyonmusic.database.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.data.data_source.SongDao
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.toSong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSongRepository(
    private val dao: SongDao
) : SongRepository {

    override suspend fun getSongs(): List<Song> = dao.getSongs().map { it.toSong() }

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
                song.toSong()
            }
        }
    }

    override fun observe() = dao.observe().map { songs ->
        songs.map {
            it.toSong()
        }
    }

    override fun observeByMediaId(mediaId: MediaId) = dao.observeByMediaId(mediaId).map {
        it.toSong()
    }

    override suspend fun findByMediaId(mediaId: MediaId): SongEntity? =
        dao.getSongWithMediaId(mediaId)

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

    override suspend fun getSongsWithArtworkTypes(vararg artworkTypes: String) =
        dao.getSongsWithArtworkTypes(artworkTypes)
}