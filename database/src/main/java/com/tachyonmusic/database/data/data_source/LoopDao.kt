package com.tachyonmusic.database.data.data_source

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.LoopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoopDao {
    @Query("SELECT * FROM LoopEntity")
    fun getPagedLoops(): PagingSource<Int, LoopEntity>

    @Query("SELECT * FROM LoopEntity")
    suspend fun getLoops(): List<LoopEntity>

    @Query("SELECT * FROM LoopEntity")
    fun observe(): Flow<List<LoopEntity>>

    // TODO: Handle abort
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun add(loop: LoopEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addAll(loops: List<LoopEntity>)

    @Query("SELECT * FROM LoopEntity WHERE songTitle=:songTitle AND songArtist=:songArtist AND songDuration=:songDuration")
    suspend fun findBySong(songTitle: String, songArtist: String, songDuration: Long): LoopEntity?

    @Query("SELECT * FROM LoopEntity WHERE mediaId=:mediaId")
    suspend fun findByMediaId(mediaId: MediaId): LoopEntity?

    @Query("UPDATE LoopEntity SET artworkType=:artworkType, artworkUrl=:artworkUrl WHERE id=:id")
    suspend fun updateArtwork(id: Int, artworkType: String?, artworkUrl: String? = null)

    @Delete
    suspend fun delete(loop: LoopEntity)
}