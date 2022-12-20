package com.tachyonmusic.database.data.data_source

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.core.domain.MediaId

@Dao
interface HistoryDao {

    @Query("SELECT * FROM HistoryEntity ORDER BY timestamp DESC")
    suspend fun getHistory(): List<HistoryEntity>

    @Query("SELECT * FROM HistoryEntity ORDER BY timestamp DESC")
    fun getPagedHistory(): PagingSource<Int, HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistory(history: HistoryEntity)

    @Query("DELETE FROM HistoryEntity WHERE mediaId=:mediaId")
    suspend fun removeHistory(mediaId: MediaId)

    @Query("DELETE FROM HistoryEntity WHERE mediaId IN (:mediaIds)")
    suspend fun removeHistory(mediaIds: List<MediaId>)

    @Query("DELETE FROM HistoryEntity")
    suspend fun clear()

    @Query("SELECT * FROM HistoryEntity ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecent(): HistoryEntity?
}