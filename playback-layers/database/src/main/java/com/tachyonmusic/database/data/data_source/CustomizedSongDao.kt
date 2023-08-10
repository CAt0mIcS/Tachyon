package com.tachyonmusic.database.data.data_source

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import androidx.room.*
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.CustomizedSongEntity
import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomizedSongDao {
    @Query("SELECT * FROM CustomizedSongEntity")
    fun getPagedCustomizedSongs(): PagingSource<Int, CustomizedSongEntity>

    @Query("SELECT * FROM CustomizedSongEntity")
    suspend fun getCustomizedSongs(): List<CustomizedSongEntity>

    @Query("SELECT * FROM CustomizedSongEntity")
    fun observe(): Flow<List<CustomizedSongEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Throws(SQLiteException::class)
    suspend fun add(customizedSong: CustomizedSongEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Throws(SQLiteException::class)
    suspend fun addAll(customizedSongs: List<CustomizedSongEntity>)

    @Query("SELECT * FROM CustomizedSongEntity WHERE songTitle=:songTitle AND songArtist=:songArtist AND songDuration=:songDuration")
    suspend fun findBySong(
        songTitle: String,
        songArtist: String,
        songDuration: Duration
    ): CustomizedSongEntity?

    @Query("SELECT * FROM CustomizedSongEntity WHERE mediaId=:mediaId")
    suspend fun findByMediaId(mediaId: MediaId): CustomizedSongEntity?

    @Query("DELETE FROM CustomizedSongEntity WHERE mediaId=:mediaId")
    suspend fun delete(mediaId: MediaId)

    @Query("UPDATE CustomizedSongEntity SET mediaId=:newMediaId WHERE mediaId=:mediaId")
    suspend fun updateMetadata(mediaId: MediaId, newMediaId: MediaId)
}