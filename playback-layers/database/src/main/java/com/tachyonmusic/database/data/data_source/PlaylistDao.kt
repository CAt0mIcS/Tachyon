package com.tachyonmusic.database.data.data_source

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import androidx.room.*
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM PlaylistEntity")
    fun getPagedPlaylists(): PagingSource<Int, PlaylistEntity>

    @Query("SELECT * FROM PlaylistEntity")
    suspend fun getPlaylists(): List<PlaylistEntity>

    @Query("SELECT * FROM PlaylistEntity")
    fun observe(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM PlaylistEntity WHERE mediaId=:mediaId")
    suspend fun getPlaylistWithMediaId(mediaId: MediaId): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Throws(SQLiteException::class)
    suspend fun add(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Throws(SQLiteException::class)
    suspend fun addAll(playlists: List<PlaylistEntity>)

    @Query("UPDATE PlaylistEntity SET items=:playbacks WHERE mediaId=:mediaId")
    suspend fun setPlaybacks(mediaId: MediaId, playbacks: List<MediaId>)

    @Query("DELETE FROM PlaylistEntity WHERE mediaId=:mediaId")
    suspend fun delete(mediaId: MediaId)

    @Query("UPDATE PlaylistEntity SET name=:name, mediaId=:newMediaId WHERE mediaId=:mediaId")
    suspend fun updateMetadata(mediaId: MediaId, name: String, newMediaId: MediaId)
}