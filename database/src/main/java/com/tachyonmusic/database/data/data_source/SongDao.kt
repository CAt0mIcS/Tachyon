package com.tachyonmusic.database.data.data_source

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import androidx.room.*
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SongEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface SongDao {
    // TODO: Temporary, user should be able to choose how to sort
    @Query("SELECT * FROM SongEntity")
    fun getPagedSongs(): PagingSource<Int, SongEntity>

    // TODO: Temporary, user should be able to choose how to sort
    @Query("SELECT * FROM SongEntity")
    suspend fun getSongs(): List<SongEntity>

    // TODO: Temporary, user should be able to choose how to sort
    @Query("SELECT * FROM SongEntity")
    fun observe(): Flow<List<SongEntity>>

    @Query("SELECT * FROM SongEntity WHERE mediaId=:mediaId")
    fun observeByMediaId(mediaId: MediaId): Flow<SongEntity>

    @Query("SELECT * FROM SongEntity WHERE mediaId=:mediaId")
    suspend fun getSongWithMediaId(mediaId: MediaId): SongEntity?

    @Query("SELECT * FROM SongEntity WHERE artworkType IN (:artworkTypes)")
    suspend fun getSongsWithArtworkTypes(artworkTypes: Array<out String>): List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Throws(SQLiteException::class)
    suspend fun add(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Throws(SQLiteException::class)
    suspend fun addAll(songs: List<SongEntity>)

    @Delete
    suspend fun delete(song: SongEntity)

    @Query("UPDATE SongEntity SET artworkType=:artworkType, artworkUrl=:artworkUrl WHERE id=:id")
    suspend fun updateArtwork(id: Int, artworkType: String?, artworkUrl: String? = null)
}