package com.daton.database.data.data_source

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daton.database.domain.model.PlaybackEntity
import com.daton.database.domain.model.SongEntity

@Dao
interface HistoryDao {

    @Query("SELECT * FROM playbackEntity")
    suspend fun getHistory(): List<PlaybackEntity>

    @Query("SELECT * FROM playbackEntity")
    fun getPagedHistory(): PagingSource<Int, PlaybackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistory(history: PlaybackEntity)

    @Delete
    suspend fun removeHistory(history: PlaybackEntity)

    @Delete
    suspend fun removeHistory(histories: List<PlaybackEntity>)

    @Query("DELETE FROM playbackEntity")
    suspend fun clear()

    @Query("SELECT * FROM playbackEntity ORDER BY ROWID ASC LIMIT 1")
    suspend fun getMostRecent(): PlaybackEntity?
}