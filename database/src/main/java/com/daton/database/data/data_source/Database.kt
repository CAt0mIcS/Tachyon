package com.daton.database.data.data_source

import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.daton.database.domain.Converters
import com.daton.database.domain.model.*

@androidx.room.Database(
    entities = [
        SettingsEntity::class,
        SongEntity::class,
        LoopEntity::class,
        PlaylistEntity::class,
        PlaybackEntity::class,
        HistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract val settingsDao: SettingsDao
    abstract val songDao: SongDao
    abstract val loopDao: LoopDao
    abstract val playlistDao: PlaylistDao
    abstract val historyDao: HistoryDao
}