package com.daton.database.data.data_source

import androidx.room.RoomDatabase
import com.daton.database.domain.model.*

@androidx.room.Database(
    entities = [
        SettingsEntity::class,
        SongEntity::class,
        LoopEntity::class,
        PlaylistEntity::class,
        PlaybackEntity::class
    ],
    version = 1
)
abstract class Database : RoomDatabase() {
    abstract val settingsDao: SettingsDao
    abstract val songDao: SongDao
    abstract val loopDao: LoopDao
    abstract val playlistDao: PlaylistDao
    abstract val historyDao: HistoryDao
}