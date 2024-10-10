package com.tachyonmusic.database.data.data_source

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import androidx.room.*
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.REMIX_DATABASE_TABLE_NAME
import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.Flow

@Dao
interface RemixDao {
    @Query("SELECT * FROM $REMIX_DATABASE_TABLE_NAME")
    fun getPagedRemixes(): PagingSource<Int, RemixEntity>

    @Query("SELECT * FROM $REMIX_DATABASE_TABLE_NAME")
    suspend fun getRemixes(): List<RemixEntity>

    @Query("SELECT * FROM $REMIX_DATABASE_TABLE_NAME")
    fun observe(): Flow<List<RemixEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Throws(SQLiteException::class)
    suspend fun add(remix: RemixEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(newRemix: RemixEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Throws(SQLiteException::class)
    suspend fun addAll(remixes: List<RemixEntity>)

    @Query("SELECT * FROM $REMIX_DATABASE_TABLE_NAME WHERE songTitle=:songTitle AND songArtist=:songArtist AND songDuration=:songDuration")
    suspend fun findBySong(
        songTitle: String,
        songArtist: String,
        songDuration: Duration
    ): RemixEntity?

    @Query("SELECT * FROM $REMIX_DATABASE_TABLE_NAME WHERE mediaId=:mediaId")
    suspend fun findByMediaId(mediaId: MediaId): RemixEntity?

    @Query("DELETE FROM $REMIX_DATABASE_TABLE_NAME WHERE mediaId=:mediaId")
    suspend fun delete(mediaId: MediaId)

    @Query("UPDATE $REMIX_DATABASE_TABLE_NAME SET mediaId=:newMediaId WHERE mediaId=:mediaId")
    suspend fun updateMetadata(mediaId: MediaId, newMediaId: MediaId)
}