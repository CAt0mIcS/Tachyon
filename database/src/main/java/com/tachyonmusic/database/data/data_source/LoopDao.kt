package com.tachyonmusic.database.data.data_source

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.Flow

@Dao
interface LoopDao {
    @Query("SELECT * FROM LoopEntity ORDER BY mediaId ASC")
    fun getPagedLoops(): PagingSource<Int, LoopEntity>

    @Query("SELECT * FROM LoopEntity ORDER BY mediaId ASC")
    suspend fun getLoops(): List<LoopEntity>

    @Query("SELECT * FROM LoopEntity ORDER BY mediaId ASC")
    fun observe(): Flow<List<LoopEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Throws(SQLiteException::class)
    suspend fun add(loop: LoopEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Throws(SQLiteException::class)
    suspend fun addAll(loops: List<LoopEntity>)

    @Query("SELECT * FROM LoopEntity WHERE songTitle=:songTitle AND songArtist=:songArtist AND songDuration=:songDuration")
    suspend fun findBySong(songTitle: String, songArtist: String, songDuration: Duration): LoopEntity?

    @Query("SELECT * FROM LoopEntity WHERE mediaId=:mediaId")
    suspend fun findByMediaId(mediaId: MediaId): LoopEntity?

    @Delete
    suspend fun delete(loop: LoopEntity)
}