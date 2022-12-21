package com.tachyonmusic.database.data.data_source

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.PlaylistEntity

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM PlaylistEntity")
    fun getPagedPlaylists(): PagingSource<Int, PlaylistEntity>

    @Query("SELECT * FROM PlaylistEntity")
    suspend fun getPlaylists(): List<PlaylistEntity>

    @Query("SELECT * FROM PlaylistEntity WHERE mediaId=:mediaId")
    suspend fun getPlaylistWithMediaId(mediaId: MediaId): PlaylistEntity?

    // TODO: Handle abort
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun add(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addAll(playlists: List<PlaylistEntity>)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)
}