package com.daton.database.data.data_source

import androidx.room.Dao
import androidx.room.Query
import com.daton.database.domain.model.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songEntity")
    fun getAll(): Flow<List<SongEntity>>
}