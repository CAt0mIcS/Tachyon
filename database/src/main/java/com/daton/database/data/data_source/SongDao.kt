package com.daton.database.data.data_source

import androidx.paging.PagingSource
import androidx.room.*
import com.daton.database.domain.ArtworkType
import com.daton.database.domain.model.SongEntity


@Dao
interface SongDao {
    @Query("SELECT * FROM songEntity")
    fun getPagedSongs(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songEntity")
    suspend fun getSongs(): List<SongEntity>

    @Query("SELECT * FROM songEntity WHERE mediaId=:mediaId")
    suspend fun getSongWithMediaId(mediaId: String): SongEntity?

    @Query("SELECT * FROM songEntity WHERE artworkType IN (:artworkTypes)")
    suspend fun getSongsWithArtworkTypes(artworkTypes: Array<out String>): List<SongEntity>

    // TODO: Handle abort
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun add(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addAll(songs: List<SongEntity>)


    @Delete
    suspend fun delete(song: SongEntity)

    @Query("UPDATE songEntity SET artworkType=:artworkType, artworkUrl=:artworkUrl WHERE id=:id")
    suspend fun updateArtwork(id: Int, artworkType: String?, artworkUrl: String? = null)
}