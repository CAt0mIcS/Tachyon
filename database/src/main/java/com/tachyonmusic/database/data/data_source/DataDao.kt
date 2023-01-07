package com.tachyonmusic.database.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.DataEntity

@Dao
interface DataDao {
    /**
     * Selects the first row in the data table. We can only have one [DataEntity] in the table
     */
    @Query("SELECT * FROM DataEntity ORDER BY ROWID ASC LIMIT 1")
    suspend fun getData(): DataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setData(data: DataEntity)

    @Query(
        "UPDATE DataEntity SET " +
                "recentlyPlayedMediaId=:mediaId, " +
                "currentPositionInRecentlyPlayedPlaybackMs=:positionMs, " +
                "recentlyPlayedDurationMs=:durationMs, " +
                "recentlyPlayedArtworkType=:artworkType, " +
                "recentlyPlayedArtworkUrl=:artworkUrl"
    )
    suspend fun updateRecentlyPlayed(
        mediaId: MediaId,
        positionMs: Long,
        durationMs: Long,
        artworkType: String,
        artworkUrl: String? = null
    )
}