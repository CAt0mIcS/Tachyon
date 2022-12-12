package com.daton.database.data.data_source

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daton.database.domain.model.HistoryEntity
import com.tachyonmusic.core.domain.MediaId

@Dao
interface HistoryDao {

    @Query("SELECT * FROM historyEntity ORDER BY timestamp DESC")
    suspend fun getHistory(): List<HistoryEntity>

    @Query("SELECT * FROM historyEntity ORDER BY timestamp DESC")
    fun getPagedHistory(): PagingSource<Int, HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistory(history: HistoryEntity)

    @Query("DELETE FROM historyEntity WHERE mediaId=:mediaId")
    suspend fun removeHistory(mediaId: MediaId)

    @Query("DELETE FROM historyEntity WHERE mediaId IN (:mediaIds)")
    suspend fun removeHistory(mediaIds: List<MediaId>)

    @Query("DELETE FROM historyEntity")
    suspend fun clear()

    @Query("SELECT * FROM historyEntity ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecent(): HistoryEntity?
}