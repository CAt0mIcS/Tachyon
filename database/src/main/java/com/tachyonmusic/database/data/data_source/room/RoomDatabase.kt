package com.tachyonmusic.database.data.data_source.room

import androidx.room.TypeConverters
import com.tachyonmusic.database.data.data_source.*
import com.tachyonmusic.database.domain.Converters
import com.tachyonmusic.database.domain.model.*

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

    override fun clearAllTables() {
        TODO()
    }
}