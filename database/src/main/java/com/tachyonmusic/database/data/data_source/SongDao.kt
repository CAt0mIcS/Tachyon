package com.tachyonmusic.database.data.data_source

import androidx.paging.PagingSource
import androidx.room.*
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SongEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface SongDao {
    // TODO: Temporary, user should be able to choose how to sort
    @Query("SELECT * FROM SongEntity ORDER BY title ASC")
    fun getPagedSongs(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM SongEntity")
    suspend fun getSongs(): List<SongEntity>

    @Query("SELECT * FROM SongEntity")
    fun observe(): Flow<List<SongEntity>>

    @Query("SELECT * FROM SongEntity WHERE mediaId=:mediaId")
    suspend fun getSongWithMediaId(mediaId: MediaId): SongEntity?

    @Query("SELECT * FROM SongEntity WHERE artworkType IN (:artworkTypes)")
    suspend fun getSongsWithArtworkTypes(artworkTypes: Array<out String>): List<SongEntity>

    // TODO: Handle abort
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun add(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addAll(songs: List<SongEntity>)

    @Delete
    suspend fun delete(song: SongEntity)

    @Query("UPDATE SongEntity SET artworkType=:artworkType, artworkUrl=:artworkUrl WHERE id=:id")
    suspend fun updateArtwork(id: Int, artworkType: String?, artworkUrl: String? = null)
}