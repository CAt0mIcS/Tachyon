package com.tachyonmusic.database.data.data_source

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.HISTORY_DATABASE_TABLE_NAME
import com.tachyonmusic.database.domain.model.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM $HISTORY_DATABASE_TABLE_NAME ORDER BY timestamp DESC")
    suspend fun getHistory(): List<HistoryEntity>

    @Query("SELECT * FROM $HISTORY_DATABASE_TABLE_NAME ORDER BY timestamp DESC")
    fun getPagedHistory(): PagingSource<Int, HistoryEntity>

    @Query("SELECT * FROM $HISTORY_DATABASE_TABLE_NAME ORDER BY timestamp DESC")
    fun observe(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistory(history: HistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Throws(SQLiteException::class)
    suspend fun addAll(history: List<HistoryEntity>)

    @Query("DELETE FROM $HISTORY_DATABASE_TABLE_NAME WHERE mediaId LIKE '%' || :mediaId || '%'")
    suspend fun removeHistoryHierarchical(mediaId: MediaId)

    @Query("DELETE FROM $HISTORY_DATABASE_TABLE_NAME WHERE mediaId=:mediaId")
    suspend fun removeHistory(mediaId: MediaId)

    @Query("DELETE FROM $HISTORY_DATABASE_TABLE_NAME WHERE mediaId IN (:mediaIds)")
    suspend fun removeHistory(mediaIds: List<MediaId>)

    @Query("DELETE FROM $HISTORY_DATABASE_TABLE_NAME")
    suspend fun clear()

    @Query("SELECT * FROM $HISTORY_DATABASE_TABLE_NAME ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecent(): HistoryEntity?
}