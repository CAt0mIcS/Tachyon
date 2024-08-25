package com.tachyonmusic.database.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.DATA_DATABASE_TABLE_NAME
import com.tachyonmusic.database.domain.model.DataEntity
import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.Flow

@Dao
interface DataDao {
    /**
     * Selects the first row in the data table. We can only have one [DataEntity] in the table
     */
    @Query("SELECT * FROM $DATA_DATABASE_TABLE_NAME ORDER BY ROWID ASC LIMIT 1")
    suspend fun getData(): DataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setData(data: DataEntity)

    @Query("SELECT * FROM $DATA_DATABASE_TABLE_NAME ORDER BY ROWID ASC LIMIT 1")
    fun observe(): Flow<DataEntity?>

    @Query(
        "UPDATE $DATA_DATABASE_TABLE_NAME SET " +
                "recentlyPlayedMediaId=:mediaId, " +
                "currentPositionInRecentlyPlayedPlayback=:position, " +
                "recentlyPlayedDuration=:duration, " +
                "recentlyPlayedArtworkType=:artworkType, " +
                "recentlyPlayedArtworkUrl=:artworkUrl"
    )
    suspend fun setRecentlyPlayed(
        mediaId: MediaId,
        position: Duration,
        duration: Duration,
        artworkType: String,
        artworkUrl: String? = null
    )

    @Query("UPDATE $DATA_DATABASE_TABLE_NAME SET maxRemixCount=:remixCount")
    suspend fun setMaxRemixCount(remixCount: Int)

    @Query("UPDATE $DATA_DATABASE_TABLE_NAME SET repeatMode=:repeatMode")
    suspend fun setRepeatMode(repeatMode: RepeatMode)
}