package com.daton.database.data.data_source.room

import androidx.room.TypeConverters
import com.daton.database.data.data_source.DataDao
import com.daton.database.data.data_source.Database
import com.daton.database.data.data_source.HistoryDao
import com.daton.database.data.data_source.LoopDao
import com.daton.database.data.data_source.PlaylistDao
import com.daton.database.data.data_source.SettingsDao
import com.daton.database.data.data_source.SongDao
import com.daton.database.domain.Converters
import com.daton.database.domain.model.*

@androidx.room.Database(
    entities = [
        SettingsEntity::class,
        SongEntity::class,
        LoopEntity::class,
        PlaylistEntity::class,
        PlaybackEntity::class,
        HistoryEntity::class,
        DataEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RoomDatabase : androidx.room.RoomDatabase(), Database {
    abstract override val settingsDao: SettingsDao
    abstract override val songDao: SongDao
    abstract override val loopDao: LoopDao
    abstract override val playlistDao: PlaylistDao
    abstract override val historyDao: HistoryDao
    abstract override val dataDao: DataDao
}