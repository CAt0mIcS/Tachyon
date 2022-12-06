package com.daton.database.data.data_source

import androidx.room.Dao
import androidx.room.Query
import com.daton.database.domain.model.PlaybackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM playbackEntity")
    fun getAll(): Flow<List<PlaybackEntity>>

    @Query("SELECT * FROM playbackEntity ORDER BY ROWID ASC LIMIT 1")
    suspend fun getMostRecent(): PlaybackEntity?
}