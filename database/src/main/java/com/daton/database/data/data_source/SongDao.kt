package com.daton.database.data.data_source

import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daton.database.domain.model.SongEntity
import com.tachyonmusic.core.domain.Artwork

@Dao
interface SongDao {
    @Query("SELECT * FROM songEntity")
    fun getPagedSongs(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songEntity")
    suspend fun getSongs(): List<SongEntity>

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